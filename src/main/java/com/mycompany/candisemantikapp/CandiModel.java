package com.mycompany.candisemantikapp;

/** Data satu candi yang ditampilkan di GUI (hasil pembacaan ontologi). */
public class CandiModel {
    private String id;
    private String nama;
    private String agama;       // Hindu, Buddha, atau Siwa-Buddha
    private String lokasi;      // Kabupaten / Wilayah
    private String tahunBerdiri;
    private String deskripsi;
    private String judulWikipedia; // opsional, dari ontologi
    private String fotoUrl;        // opsional, dari ontologi

    public CandiModel(String id, String nama, String agama, String lokasi, String tahunBerdiri, String deskripsi) {
        this.id = id;
        this.nama = nama;
        this.agama = agama;
        this.lokasi = lokasi;
        this.tahunBerdiri = tahunBerdiri;
        this.deskripsi = deskripsi;
    }

    public CandiModel(String id, String nama, String agama, String lokasi, String tahunBerdiri,
                      String deskripsi, String judulWikipedia, String fotoUrl) {
        this(id, nama, agama, lokasi, tahunBerdiri, deskripsi);
        this.judulWikipedia = judulWikipedia;
        this.fotoUrl = fotoUrl;
    }

    public String getId() { return id; }
    public String getNama() { return nama; }
    public String getAgama() { return agama; }
    public String getLokasi() { return lokasi; }
    public String getTahunBerdiri() { return tahunBerdiri; }
    public String getDeskripsi() { return deskripsi; }
    public String getJudulWikipedia() { return judulWikipedia; }
    public String getFotoUrl() { return fotoUrl; }
}