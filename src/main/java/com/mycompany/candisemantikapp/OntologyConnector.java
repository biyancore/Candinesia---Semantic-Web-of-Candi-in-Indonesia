package com.mycompany.candisemantikapp;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Membaca ontologi ProjectCandiFinal.owl (Apache Jena) dan mengubahnya menjadi daftar CandiModel. */
public class OntologyConnector {

    private static final String NS = "http://www.semanticweb.org/candi-indonesia#";

    private static final String CLASSPATH_FILE = "/ProjectCandiFinal.owl";
    private static final String LOCAL_FILE = "resources/ProjectCandiFinal.owl";

    private static Model model;

    private static InputStream openOwl() throws Exception {
        InputStream in = OntologyConnector.class.getResourceAsStream(CLASSPATH_FILE);
        if (in != null) return in;
        File f = new File(LOCAL_FILE);
        if (f.exists()) return new FileInputStream(f);
        throw new java.io.FileNotFoundException(
            "ProjectCandiFinal.owl tidak ditemukan. Dicari di: " + f.getAbsolutePath());
    }

    private static Model loadModel() {
        if (model != null) return model;
        Model m = ModelFactory.createDefaultModel();
        try (InputStream in = openOwl()) {
            RDFDataMgr.read(m, in, Lang.RDFXML);
            System.out.println("[Ontologi] Berhasil dibaca: " + m.size() + " triple");
        } catch (Exception e) {
            System.err.println("[Ontologi] GAGAL membaca OWL: " + e.getMessage());
        }
        model = m;
        return m;
    }

    private static Property p(Model m, String name) { return m.createProperty(NS + name); }

    private static String lit(Model m, Resource s, String prop) {
        Statement st = s.getProperty(p(m, prop));
        return (st != null && st.getObject().isLiteral()) ? st.getObject().asLiteral().getString().trim() : null;
    }

    private static Resource res(Model m, Resource s, String prop) {
        Statement st = s.getProperty(p(m, prop));
        if (st != null) {
            RDFNode o = st.getObject();
            if (o.isURIResource()) return o.asResource();
        }
        return null;
    }

    private static String prettify(String localName) {
        return localName.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
    }

    private static String namaWilayah(Model m, Resource r) {
        String n = lit(m, r, "namaWilayah");
        return n != null ? n : prettify(r.getLocalName());
    }

    /** Semua candi (Hindu, Buddha, Siwa-Buddha) dari ontologi, diurutkan menurut nama. */
    public static List<CandiModel> getCandiList() {
        Model m = loadModel();

        Map<Resource, String> candi = new LinkedHashMap<>();
        String[][] kelasAgama = { {"CandiHindu", "Hindu"}, {"CandiBuddha", "Buddha"}, {"CandiSiwaBudha", "Siwa-Buddha"} };
        for (String[] ka : kelasAgama) {
            ResIterator it = m.listSubjectsWithProperty(RDF.type, m.createResource(NS + ka[0]));
            while (it.hasNext()) {
                Resource r = it.nextResource();
                if (!r.isURIResource()) continue;
                String sebelum = candi.get(r);
                if (sebelum == null || ka[1].equals("Siwa-Buddha")) candi.put(r, ka[1]);
            }
        }

        List<CandiModel> hasil = new ArrayList<>();
        for (Map.Entry<Resource, String> e : candi.entrySet()) {
            Resource c = e.getKey();

            String id = c.getLocalName();
            String nama = lit(m, c, "namaCandi");
            if (nama == null) nama = prettify(id);

            String deskripsi = lit(m, c, "deskripsiCandi");
            if (deskripsi == null) deskripsi = "Belum ada deskripsi pada ontologi.";

            hasil.add(new CandiModel(id, nama, e.getValue(), buildLokasi(m, c), buildTahun(m, c), deskripsi,
                    lit(m, c, "judulWikipedia"), lit(m, c, "fotoCandi")));
        }

        hasil.sort(Comparator.comparing(CandiModel::getNama, String.CASE_INSENSITIVE_ORDER));
        return hasil;
    }

    private static String buildLokasi(Model m, Resource candi) {
        Resource kab = res(m, candi, "memilikiLokasi");
        if (kab == null) kab = res(m, candi, "lokasiDariCandi");
        if (kab == null) {
            // arah sebaliknya: kabupaten yang menunjuk candi ini lewat lokasiDariCandi
            ResIterator it = m.listSubjectsWithProperty(p(m, "lokasiDariCandi"), candi);
            if (it.hasNext()) kab = it.nextResource();
        }
        if (kab == null) return "Lokasi belum tercatat";

        String lokasi = namaWilayah(m, kab);
        Resource prov = res(m, kab, "terletakDiProvinsi");
        if (prov != null) lokasi += ", " + namaWilayah(m, prov);
        return lokasi;
    }

    private static String buildTahun(Model m, Resource candi) {
        String thn = lit(m, candi, "tahunBerdiri");
        if (thn != null) {
            try {
                int y = Integer.parseInt(thn);
                int abad = (y - 1) / 100 + 1;
                return "Abad ke-" + abad + " (" + y + " M)";
            } catch (NumberFormatException ex) {
                return thn;
            }
        }
        Resource abad = res(m, candi, "dibangunPadaAbad");
        if (abad != null) {
            String n = lit(m, abad, "nilaiAbad");
            if (n != null) return "Abad ke-" + n;
        }
        return "Tahun belum tercatat";
    }
}