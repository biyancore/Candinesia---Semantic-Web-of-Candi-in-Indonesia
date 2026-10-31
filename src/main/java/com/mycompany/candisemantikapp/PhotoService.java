package com.mycompany.candisemantikapp;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * Mengambil foto candi di latar belakang (tidak membekukan GUI).
 * Urutan sumber foto:
 *   1. properti fotoCandi di ontologi (URL http/https atau path file lokal)
 *   2. file lokal berdasarkan ID candi: foto/<ID>.jpg|jpeg|png
 *      (folder foto/ di direktori kerja, atau di dalam classpath/JAR)
 *   3. cache hasil unduhan lama di ~/.candinesia/foto
 *   4. Wikipedia -- HANYA jika dijalankan dengan -Dcandinesia.online=true (default: mati)
 * Jika semuanya tidak ada, GUI tetap memakai huruf monogram.
 */
public class PhotoService {

    // ---------------------------------------------------------------
    // Pengaturan
    // ---------------------------------------------------------------

    /** Pengambilan foto dari internet mati secara default (supaya tidak ada delay). */
    private static final boolean ONLINE = Boolean.parseBoolean(System.getProperty("candinesia.online", "false"));

    /** Foto yang sisi terpanjangnya melebihi ini (piksel) diperkecil agar hemat memori. */
    private static final int SISI_MAKS = 1000;

    private static final String UA = "CandinesiaApp/1.0 (proyek web semantik; Java HttpClient)";
    private static final Pattern THUMB =
            Pattern.compile("\"thumbnail\"\\s*:\\s*\\{\\s*\"source\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern BAHASA = Pattern.compile("^([a-z]{2,3}):(.+)$");
    private static final Path DISK = Paths.get(System.getProperty("user.home"), ".candinesia", "foto");

    // ---------------------------------------------------------------
    // Status & proses latar belakang
    // ---------------------------------------------------------------

    /** Foto yang sudah termuat, per ID candi. */
    private static final Map<String, BufferedImage> CACHE = new ConcurrentHashMap<>();
    /** ID candi yang sudah menyerah (tidak dicoba lagi sampai reset()). */
    private static final Set<String> GAGAL = ConcurrentHashMap.newKeySet();
    /** Jumlah percobaan ulang per ID candi (hanya mode online). */
    private static final Map<String, Integer> PERCOBAAN = new ConcurrentHashMap<>();
    /** Callback yang menunggu foto selesai dimuat, per ID candi. */
    private static final Map<String, List<Runnable>> TUNGGU = new HashMap<>();

    private static final ExecutorService POOL = Executors.newFixedThreadPool(2, daemon("foto-candi"));
    private static final ScheduledExecutorService JADWAL =
            Executors.newSingleThreadScheduledExecutor(daemon("foto-candi-ulang"));

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private static ThreadFactory daemon(String nama) {
        return r -> {
            Thread t = new Thread(r, nama);
            t.setDaemon(true);
            return t;
        };
    }

    private static void log(String s) {
        System.err.println("[Foto] " + s);
    }

    // ---------------------------------------------------------------
    // API untuk GUI
    // ---------------------------------------------------------------

    /** Foto yang sudah termuat, atau null kalau belum/tidak ada. */
    public static BufferedImage get(CandiModel c) {
        return c == null ? null : CACHE.get(c.getId());
    }

    /** Minta foto dimuat. onLoaded dipanggil di EDT hanya jika foto berhasil didapat. */
    public static void request(CandiModel c, Runnable onLoaded) {
        if (c == null) return;
        final String id = c.getId();
        synchronized (TUNGGU) {
            if (CACHE.containsKey(id)) { SwingUtilities.invokeLater(onLoaded); return; }
            if (GAGAL.contains(id)) return;
            List<Runnable> antri = TUNGGU.get(id);
            if (antri != null) { antri.add(onLoaded); return; }
            antri = new ArrayList<>();
            antri.add(onLoaded);
            TUNGGU.put(id, antri);
        }
        jalankan(c);
    }

    // ---------------------------------------------------------------
    // Pemuatan di latar belakang
    // ---------------------------------------------------------------

    private static void jalankan(CandiModel c) {
        final String id = c.getId();
        POOL.submit(() -> {
            BufferedImage img = null;
            try {
                img = muat(c);
            } catch (Throwable e) {
                log(c.getNama() + ": " + e);
            }
            if (img == null && ONLINE) {
                int n = PERCOBAAN.merge(id, 1, Integer::sum);
                if (n < 3) {
                    log(c.getNama() + ": belum berhasil, dicoba lagi (" + n + "/3)");
                    JADWAL.schedule(() -> jalankan(c), 8L * n, TimeUnit.SECONDS);
                    return;
                }
                log(c.getNama() + ": menyerah, memakai monogram.");
            }
            List<Runnable> selesai;
            synchronized (TUNGGU) {
                if (img != null) CACHE.put(id, img); else GAGAL.add(id);
                selesai = TUNGGU.remove(id);
            }
            if (img != null && selesai != null) {
                for (Runnable r : selesai) SwingUtilities.invokeLater(r);
            }
        });
    }

    // ---------------------------------------------------------------
    private static BufferedImage muat(CandiModel c) throws Exception {
        // 1. fotoCandi eksplisit dari ontologi
        String sumber = c.getFotoUrl();
        if (sumber != null && !sumber.isBlank()) {
            sumber = sumber.trim();
            byte[] data;
            if (sumber.startsWith("http://") || sumber.startsWith("https://")) {
                data = unduh(sumber);
            } else {
                data = bacaLokal(sumber);
            }
            BufferedImage img = decode(data);
            if (img != null) return img;
        }

        // 2. file lokal berdasarkan ID candi, mis. foto/Borobudur.jpg
        for (String ext : new String[]{"jpg", "jpeg", "png", "JPG", "JPEG", "PNG"}) {
            BufferedImage img = decode(bacaLokal("foto/" + c.getId() + "." + ext));
            if (img != null) return img;
        }

        // 3. cache disk dari unduhan sebelumnya
        Path f = DISK.resolve(c.getId() + ".img");
        if (Files.exists(f)) {
            BufferedImage img = decode(Files.readAllBytes(f));
            if (img != null) return img;
        }

        // 4. Wikipedia (hanya jika -Dcandinesia.online=true)
        if (!ONLINE) return null;
        byte[] data = unduhFotoWikipedia(c);
        BufferedImage img = decode(data);
        if (img != null) {
            try {
                Files.createDirectories(DISK);
                Files.write(f, data);
            } catch (Exception ignore) { }
        }
        return img;
    }

    /** Mencari foto di Wikipedia: tebakan judul artikel dulu, lalu pencarian sebagai cadangan. */
    private static byte[] unduhFotoWikipedia(CandiModel c) {
        for (String[] k : kandidat(c)) {
            byte[] data = ambilThumb(cariThumbnail(k[0], k[1]));
            if (data != null) return data;
        }
        String nama = c.getNama().trim();
        String inti = nama.startsWith("Candi ") ? nama.substring(6).trim() : nama;
        String[][] cari = { {"id", "Candi " + inti}, {"en", inti + " temple Indonesia"} };
        for (String[] s : cari) {
            byte[] data = ambilThumb(cariViaPencarian(s[0], s[1]));
            if (data != null) return data;
        }
        log(c.getNama() + ": tidak ditemukan di Wikipedia");
        return null;
    }

    private static byte[] ambilThumb(String thumb) {
        if (thumb == null) return null;
        byte[] data = unduh(thumb.replaceFirst("/\\d+px-", "/500px-"));
        if (data == null) data = unduh(thumb);
        return decode(data) != null ? data : null;
    }

    private static String cariViaPencarian(String bahasa, String kata) {
        try {
            String q = URLEncoder.encode(kata, StandardCharsets.UTF_8);
            URI u = URI.create("https://" + bahasa + ".wikipedia.org/w/api.php?action=query&generator=search&gsrsearch="
                    + q + "&gsrlimit=1&prop=pageimages&piprop=thumbnail&pithumbsize=500&format=json");
            HttpRequest req = HttpRequest.newBuilder(u).timeout(Duration.ofSeconds(10))
                    .header("User-Agent", UA).GET().build();
            HttpResponse<String> r = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() != 200) return null;
            Matcher m = THUMB.matcher(r.body());
            if (m.find()) return m.group(1).replace("\\/", "/");
        } catch (Exception ignore) { }
        return null;
    }

    /** Hapus semua status agar foto dicari ulang (dipakai setelah tombol Unduh foto). */
    public static void reset() {
        synchronized (TUNGGU) {
            CACHE.clear();
            GAGAL.clear();
            PERCOBAAN.clear();
        }
    }

    /**
     * Menyimpan foto candi ke folder (mis. foto/Borobudur.jpg). Tidak menimpa foto yang sudah ada.
     * @return "ada" (sudah ada), "ok" (berhasil disimpan), atau "gagal"
     */
    public static String simpanKeFolder(CandiModel c, Path folder) {
        try {
            Files.createDirectories(folder);
            for (String ext : new String[]{"jpg", "jpeg", "png", "JPG", "JPEG", "PNG"}) {
                if (Files.exists(folder.resolve(c.getId() + "." + ext))) return "ada";
            }
            byte[] data = null;
            Path lama = DISK.resolve(c.getId() + ".img");
            if (Files.exists(lama)) {
                data = Files.readAllBytes(lama);
                if (decode(data) == null) data = null;
            }
            if (data == null) data = unduhFotoWikipedia(c);
            if (data == null) return "gagal";
            boolean png = data.length > 4 && (data[0] & 0xFF) == 0x89 && data[1] == 'P';
            Files.write(folder.resolve(c.getId() + (png ? ".png" : ".jpg")), data);
            return "ok";
        } catch (Exception e) {
            log(c.getNama() + ": " + e);
            return "gagal";
        }
    }

    private static List<String[]> kandidat(CandiModel c) {
        List<String[]> k = new ArrayList<>();
        String j = c.getJudulWikipedia();
        if (j != null && !j.isBlank()) {
            Matcher m = BAHASA.matcher(j.trim());
            if (m.matches()) k.add(new String[]{m.group(1), m.group(2)});
            else k.add(new String[]{"en", j.trim()});
        }
        String nama = c.getNama().trim();
        String inti = nama.startsWith("Candi ") ? nama.substring(6).trim() : nama;
        k.add(new String[]{"id", "Candi " + inti});
        k.add(new String[]{"en", inti + " Temple"});
        return k;
    }

    private static String cariThumbnail(String bahasa, String judul) {
        try {
            String t = URLEncoder.encode(judul.replace(' ', '_'), StandardCharsets.UTF_8).replace("+", "%20");
            URI u = URI.create("https://" + bahasa + ".wikipedia.org/api/rest_v1/page/summary/" + t);
            HttpRequest req = HttpRequest.newBuilder(u).timeout(Duration.ofSeconds(10))
                    .header("User-Agent", UA).header("Accept", "application/json").GET().build();
            HttpResponse<String> r = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() == 429) {
                log("HTTP 429 dari " + bahasa + ".wikipedia.org untuk '" + judul + "'");
                Thread.sleep(2000);
                r = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            }
            if (r.statusCode() != 200) return null;
            Matcher m = THUMB.matcher(r.body());
            if (m.find()) return m.group(1).replace("\\/", "/");
        } catch (Exception ignore) { }
        return null;
    }

    private static byte[] unduh(String url) {
        for (int i = 1; i <= 3; i++) {
            try {
                HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20))
                        .header("User-Agent", UA).GET().build();
                HttpResponse<byte[]> r = HTTP.send(req, HttpResponse.BodyHandlers.ofByteArray());
                int s = r.statusCode();
                if (s == 200 && r.body().length > 0) return r.body();
                if (s == 429 || s >= 500) {
                    log("HTTP " + s + " untuk " + url + " (percobaan " + i + "/3)");
                    Thread.sleep(1500L * i);
                    continue;
                }
                log("HTTP " + s + " untuk " + url);
                return null;
            } catch (Exception e) {
                log("gagal unduh " + url + ": " + e);
                try { Thread.sleep(1000L * i); } catch (InterruptedException ie) { return null; }
            }
        }
        return null;
    }

    private static byte[] bacaLokal(String path) {
        try {
            File f = new File(path);
            if (f.exists()) return Files.readAllBytes(f.toPath());
            try (InputStream in = PhotoService.class.getResourceAsStream(path.startsWith("/") ? path : "/" + path)) {
                if (in != null) return in.readAllBytes();
            }
        } catch (Exception ignore) { }
        return null;
    }

    private static BufferedImage decode(byte[] data) {
        if (data == null) return null;
        try {
            return perkecil(ImageIO.read(new ByteArrayInputStream(data)));
        } catch (Exception e) {
            return null;
        }
    }

    private static BufferedImage perkecil(BufferedImage img) {
        if (img == null) return null;
        while (Math.max(img.getWidth(), img.getHeight()) / 2 >= SISI_MAKS) {
            img = skala(img, img.getWidth() / 2, img.getHeight() / 2);
        }
        int sisi = Math.max(img.getWidth(), img.getHeight());
        if (sisi > SISI_MAKS) {
            double r = (double) SISI_MAKS / sisi;
            img = skala(img, Math.max(1, (int) Math.round(img.getWidth() * r)),
                             Math.max(1, (int) Math.round(img.getHeight() * r)));
        }
        return img;
    }

    private static BufferedImage skala(BufferedImage src, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    // ---------------------------------------------------------------
    // Menggambar foto memenuhi bentuk (kartu membulat / lengkung gapura)
    public static void gambarDalamBentuk(Graphics2D g, BufferedImage img, Shape bentuk, int w, int h) {
        if (img == null || w <= 0 || h <= 0) return;
        BufferedImage buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D b = buf.createGraphics();
        b.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        b.fill(bentuk);
        b.setComposite(AlphaComposite.SrcIn);
        b.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        b.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        double s = Math.max((double) w / img.getWidth(), (double) h / img.getHeight());
        int dw = (int) Math.ceil(img.getWidth() * s);
        int dh = (int) Math.ceil(img.getHeight() * s);
        b.drawImage(img, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
        b.dispose();
        g.drawImage(buf, 0, 0, null);
    }
}
