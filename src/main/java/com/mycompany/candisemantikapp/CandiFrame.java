package com.mycompany.candisemantikapp;

import com.formdev.flatlaf.FlatLightLaf;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

public class CandiFrame extends JFrame {

    // =====================================
    // VARIABEL & PALET WARNA
    // =====================================
    private final Color COLOR_BG = new Color(250, 247, 240);
    private final Color COLOR_SURFACE = new Color(255, 255, 255);
    private final Color COLOR_HERO = new Color(59, 42, 32);
    private final Color COLOR_TEXT_HERO = new Color(250, 247, 240);
    private final Color COLOR_MUTED = new Color(110, 85, 67);
    private final Color COLOR_LINE = new Color(230, 216, 194);
    private final Color COLOR_ACCENT = new Color(166, 124, 82);
    private final Color COLOR_BUDDHA = new Color(213, 184, 154);
    private final Color COLOR_HINDU = new Color(166, 124, 82);
    private final Color COLOR_CHIP = new Color(242, 231, 213);

    private List<CandiModel> allData = new ArrayList<>();
    private CandiModel selectedCandi;
    private String filterAgama = "";
    private String filterQuery = "";
    private String filterProvinsi = "";

    private JTextField searchInput;
    private JComboBox<String> provinsiComboBox;
    private RoundedButton btnBuddha, btnHindu;
    private JPanel cardsGridPanel;
    private JLabel countLabel;
    private JLabel statsLabel;
    private final List<Runnable> penyegarHero = new ArrayList<>();

    private JPanel detailSectionWrapper;
    private JLabel detailTitle, detailSubtitle, detailDesc;
    private JPanel detailTagsPanel;
    private JPanel detailArchPreview;
    private JLabel detailArchLabel;
    private JTabbedPane detailTabPane;
    private JPanel ringkasanTab, tripleTab, grafTab, sparqlTab;

    // =====================================
    // KONSTRUKTOR UTAMA
    // =====================================
    public CandiFrame() {
        setTitle("Candinesia — Graf Pengetahuan Candi Nusantara");
        setSize(1260, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);

        allData = OntologyConnector.getCandiList();
        if (!allData.isEmpty()) {
            selectedCandi = allData.get(0);
        }

        JPanel webContainer = new JPanel();
        webContainer.setLayout(new BoxLayout(webContainer, BoxLayout.Y_AXIS));
        webContainer.setBackground(COLOR_BG);
        webContainer.setMaximumSize(new Dimension(1180, Integer.MAX_VALUE));

        webContainer.add(createHeroPanel());
        webContainer.add(createDockFilterPanel());
        webContainer.add(createGallerySection());

        detailSectionWrapper = createDetailSection();
        webContainer.add(detailSectionWrapper);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(COLOR_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        centerWrapper.add(webContainer, gbc);

        JScrollPane rootScrollPane = new JScrollPane(centerWrapper);
        rootScrollPane.setBorder(null);
        rootScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(rootScrollPane);

        applyFilters();
    }

    // =====================================
    // HERO SECTION
    // =====================================
    private JPanel createHeroPanel() {
        JPanel hero = new JPanel(new BorderLayout());
        hero.setBackground(COLOR_HERO);
        hero.setBorder(new EmptyBorder(35, 50, 45, 50));
        hero.setMaximumSize(new Dimension(1180, 440));

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandPanel.setOpaque(false);

        JLabel logoIcon = new JLabel("🏛️");
        logoIcon.setFont(new Font("SansSerif", Font.PLAIN, 26));

        JLabel brandTitle = new JLabel("Candinesia");
        brandTitle.setFont(new Font("Serif", Font.BOLD, 20));
        brandTitle.setForeground(COLOR_TEXT_HERO);

        JLabel brandSub = new JLabel("Graf pengetahuan candi Nusantara");
        brandSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        brandSub.setForeground(new Color(210, 200, 190));

        JPanel brandTextPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        brandTextPanel.setOpaque(false);
        brandTextPanel.add(brandTitle);
        brandTextPanel.add(brandSub);

        brandPanel.add(logoIcon);
        brandPanel.add(brandTextPanel);

        RoundedButton themeBtn = new RoundedButton("Tema", new Color(250, 247, 240, 30), COLOR_TEXT_HERO, new Color(250, 247, 240, 60));
        themeBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        themeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        topHeader.add(brandPanel, BorderLayout.WEST);
        topHeader.add(themeBtn, BorderLayout.EAST);

        JPanel heroContent = new JPanel(new GridLayout(1, 2, 40, 0));
        heroContent.setOpaque(false);
        heroContent.setBorder(new EmptyBorder(25, 0, 0, 0));

        JPanel leftHero = new JPanel();
        leftHero.setLayout(new BoxLayout(leftHero, BoxLayout.Y_AXIS));
        leftHero.setOpaque(false);

        JLabel heroTitle = new JLabel("<html><h1 style='font-family: Georgia; font-size: 34px; font-weight: normal; line-height: 1.1; margin:0; color: #FAF7F0;'>Jelajahi candi lewat relasi, bukan sekadar daftar.</h1></html>");
        JLabel heroDesc = new JLabel("<html><body style='width: 420px; font-size: 12px; line-height: 1.4; color: #D1B89A; margin-top: 8px;'>Setiap candi dimodelkan sebagai entitas dengan hubungan bermakna: agama, wilayah, dinasti, dan masa pembangunan. Klik kartu candi di bawah untuk melihat detail ontologi dan kueri SPARQL.</body></html>");
        JPanel heroButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 14));
        heroButtons.setOpaque(false);
        heroButtons.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedButton btnJelajahi = new RoundedButton("Jelajahi candi", COLOR_BUDDHA, COLOR_HERO, COLOR_BUDDHA);
        btnJelajahi.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnJelajahi.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnJelajahi.addActionListener(e -> {
            gulirKe(cardsGridPanel);
        });

        RoundedButton btnGraf = new RoundedButton("Lihat graf relasi", new Color(250, 247, 240, 20), COLOR_TEXT_HERO, new Color(250, 247, 240, 50));
        btnGraf.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnGraf.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGraf.addActionListener(e -> {
            if (detailSectionWrapper != null) {
                gulirKe(detailSectionWrapper);
                if (detailTabPane != null) {
                    detailTabPane.setSelectedIndex(2);
                }
            }
        });

        RoundedButton btnFoto = new RoundedButton("Unduh foto", new Color(250, 247, 240, 20), COLOR_TEXT_HERO, new Color(250, 247, 240, 50));
        btnFoto.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnFoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFoto.setToolTipText("Unduh foto semua candi dari Wikipedia ke folder foto/ (cukup sekali)");
        btnFoto.addActionListener(e -> mulaiUnduhFoto(btnFoto));

        heroButtons.add(btnJelajahi);
        heroButtons.add(btnGraf);
        heroButtons.add(btnFoto);

        statsLabel = new JLabel("Saat ini memuat " + allData.size() + " candi dari file ontologi OWL. Foto: Wikipedia/Wikimedia Commons.");
        statsLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        statsLabel.setForeground(new Color(200, 180, 160));
        statsLabel.setBorder(new EmptyBorder(6, 0, 0, 0));

        leftHero.add(heroTitle);
        leftHero.add(heroDesc);
        leftHero.add(Box.createVerticalStrut(6));
        leftHero.add(heroButtons);
        leftHero.add(statsLabel);

        JPanel rightArches = new JPanel(new GridLayout(1, 3, 12, 0));
        rightArches.setOpaque(false);
        rightArches.add(createArchCard("P", "Prambanan", COLOR_HINDU, 25, "Prambanan"));
        rightArches.add(createArchCard("B", "Borobudur", COLOR_BUDDHA, 0, "Borobudur"));
        rightArches.add(createArchCard("P", "Penataran", COLOR_HINDU, 40, "Penataran"));

        heroContent.add(leftHero);
        heroContent.add(rightArches);

        hero.add(topHeader, BorderLayout.NORTH);
        hero.add(heroContent, BorderLayout.CENTER);

        return hero;
    }

    private JPanel createArchCard(String initial, String name, Color bgCol, int topOffset, String candiId) {
        final CandiModel candi = allData.stream().filter(x -> x.getId().equals(candiId)).findFirst().orElse(null);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(topOffset, 0, 0, 0));

        JPanel arch = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgCol);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), getWidth(), getWidth()));
                BufferedImage foto = PhotoService.get(candi);
                if (foto != null) {
                    PhotoService.gambarDalamBentuk(g2, foto,
                        new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), getWidth(), getWidth()), getWidth(), getHeight());
                }
                g2.dispose();
            }
        };
        arch.setOpaque(false);
        arch.setPreferredSize(new Dimension(0, 170));

        JLabel lbl = new JLabel(initial);
        lbl.setFont(new Font("Serif", Font.BOLD, 36));
        lbl.setForeground(new Color(59, 42, 32, 160));
        arch.add(lbl);
        Runnable muatFoto = () -> PhotoService.request(candi, () -> {
            lbl.setVisible(false);
            arch.repaint();
        });
        muatFoto.run();
        penyegarHero.add(muatFoto);
        if (candi != null) {
            arch.setCursor(new Cursor(Cursor.HAND_CURSOR));
            arch.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) { tampilkanPopup(candi); }
            });
        }

        JLabel caption = new JLabel(name, SwingConstants.CENTER);
        caption.setFont(new Font("SansSerif", Font.PLAIN, 11));
        caption.setForeground(new Color(250, 247, 240, 200));
        caption.setBorder(new EmptyBorder(5, 0, 0, 0));

        wrapper.add(arch, BorderLayout.CENTER);
        wrapper.add(caption, BorderLayout.SOUTH);

        return wrapper;
    }

    // =====================================
    // FILTER & SEARCH PANEL
    // =====================================
    private JPanel createDockFilterPanel() {
        JPanel dock = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        dock.setBackground(COLOR_SURFACE);
        dock.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_LINE),
            new EmptyBorder(12, 50, 12, 50)
        ));
        dock.setMaximumSize(new Dimension(1180, 70));

        JLabel lblCari = new JLabel("cari");
        lblCari.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblCari.setForeground(COLOR_MUTED);

        searchInput = new JTextField(20);
        searchInput.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchInput.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(20, COLOR_LINE),
            new EmptyBorder(6, 12, 6, 12)
        ));
        searchInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterQuery = searchInput.getText().trim();
                applyFilters();
            }
        });

        JLabel lblAgama = new JLabel("beragama");
        lblAgama.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblAgama.setForeground(COLOR_MUTED);

        btnBuddha = new RoundedButton("Buddha", COLOR_CHIP, COLOR_HERO, COLOR_LINE);
        btnHindu = new RoundedButton("Hindu", COLOR_CHIP, COLOR_HERO, COLOR_LINE);
        btnBuddha.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnHindu.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnBuddha.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHindu.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnBuddha.addActionListener(e -> {
            filterAgama = filterAgama.equals("Buddha") ? "" : "Buddha";
            updateChipStyles();
            applyFilters();
        });

        btnHindu.addActionListener(e -> {
            filterAgama = filterAgama.equals("Hindu") ? "" : "Hindu";
            updateChipStyles();
            applyFilters();
        });

        JLabel lblWilayah = new JLabel("terletakDi");
        lblWilayah.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblWilayah.setForeground(COLOR_MUTED);

         List<String> provinces = allData.stream()
       .map(c -> { String l = c.getLokasi(); int i = l.lastIndexOf(", "); return i >= 0 ? l.substring(i + 2) : l; })
       .distinct().sorted().collect(Collectors.toList());
        provinces.add(0, "Semua provinsi");
        provinsiComboBox = new JComboBox<>(provinces.toArray(new String[0]));
        provinsiComboBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        provinsiComboBox.setBackground(COLOR_BG);
        provinsiComboBox.setBorder(new RoundedBorder(18, COLOR_LINE));
        provinsiComboBox.addActionListener(e -> {
            String selected = (String) provinsiComboBox.getSelectedItem();
            filterProvinsi = (selected == null || selected.equals("Semua provinsi")) ? "" : selected;
            applyFilters();
        });

        dock.add(lblCari);
        dock.add(searchInput);
        dock.add(Box.createHorizontalStrut(10));
        dock.add(lblAgama);
        dock.add(btnBuddha);
        dock.add(btnHindu);
        dock.add(Box.createHorizontalStrut(10));
        dock.add(lblWilayah);
        dock.add(provinsiComboBox);

        return dock;
    }

    private void updateChipStyles() {
        if ("Buddha".equals(filterAgama)) {
            btnBuddha.setBackgroundColor(COLOR_HERO);
            btnBuddha.setForeground(COLOR_TEXT_HERO);
            btnBuddha.setBorderColor(COLOR_HERO);
        } else {
            btnBuddha.setBackgroundColor(COLOR_CHIP);
            btnBuddha.setForeground(COLOR_HERO);
            btnBuddha.setBorderColor(COLOR_LINE);
        }

        if ("Hindu".equals(filterAgama)) {
            btnHindu.setBackgroundColor(COLOR_HERO);
            btnHindu.setForeground(COLOR_TEXT_HERO);
            btnHindu.setBorderColor(COLOR_HERO);
        } else {
            btnHindu.setBackgroundColor(COLOR_CHIP);
            btnHindu.setForeground(COLOR_HERO);
            btnHindu.setBorderColor(COLOR_LINE);
        }
    }

    // =====================================
    // GALLERY GRID SECTION
    // =====================================
    private JPanel createGallerySection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(COLOR_BG);
        section.setBorder(new EmptyBorder(30, 50, 15, 50));
        section.setMaximumSize(new Dimension(1180, Integer.MAX_VALUE));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        header.setOpaque(false);

        JLabel h2 = new JLabel("Entitas candi");
        h2.setFont(new Font("Serif", Font.BOLD, 24));
        h2.setForeground(COLOR_HERO);

        countLabel = new JLabel("(0 dari 0)");
        countLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
        countLabel.setForeground(COLOR_MUTED);

        header.add(h2);
        header.add(countLabel);

        cardsGridPanel = new JPanel(new GridLayout(0, 4, 18, 18));
        cardsGridPanel.setOpaque(false);
        cardsGridPanel.setBorder(new EmptyBorder(15, 0, 15, 0));

        section.add(header, BorderLayout.NORTH);
        section.add(cardsGridPanel, BorderLayout.CENTER);

        return section;
    }

    private void renderCards(List<CandiModel> list) {
        cardsGridPanel.removeAll();
        for (CandiModel c : list) {
            boolean isSelected = (selectedCandi != null && c.equals(selectedCandi));
            JPanel card = new JPanel(new BorderLayout(5, 5));
            card.setBackground(COLOR_SURFACE);
            card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(22, isSelected ? COLOR_ACCENT : COLOR_LINE),
                new EmptyBorder(12, 12, 12, 12)
            ));

            JPanel photoCard = new JPanel(new GridBagLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(warnaAgama(c));
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 35, 35));
                    BufferedImage foto = PhotoService.get(c);
                    if (foto != null) {
                        PhotoService.gambarDalamBentuk(g2, foto,
                            new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 35, 35), getWidth(), getHeight());
                    }
                    g2.dispose();
                }
            };
            photoCard.setOpaque(false);
            photoCard.setPreferredSize(new Dimension(0, 120));

            JLabel initialLbl = new JLabel(String.valueOf(inisial(c)));
            initialLbl.setFont(new Font("Serif", Font.BOLD, 34));
            initialLbl.setForeground(new Color(59, 42, 32, 140));
            photoCard.add(initialLbl);
            PhotoService.request(c, () -> {
                initialLbl.setVisible(false);
                photoCard.repaint();
            });
            photoCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
            photoCard.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) { tampilkanPopup(c); }
            });

            JLabel nameLbl = new JLabel(c.getNama());
            nameLbl.setFont(new Font("Serif", Font.BOLD, 15));
            nameLbl.setForeground(COLOR_HERO);

            JLabel loc = new JLabel(c.getLokasi());
            loc.setFont(new Font("SansSerif", Font.PLAIN, 12));
            loc.setForeground(COLOR_MUTED);

            JLabel era = new JLabel(c.getTahunBerdiri());
            era.setFont(new Font("SansSerif", Font.ITALIC, 11));
            era.setForeground(COLOR_MUTED);

            JPanel textP = new JPanel(new GridLayout(3, 1, 2, 2));
            textP.setOpaque(false);
            textP.setBorder(new EmptyBorder(8, 0, 0, 0));
            textP.add(nameLbl);
            textP.add(loc);
            textP.add(era);

            card.add(photoCard, BorderLayout.NORTH);
            card.add(textP, BorderLayout.CENTER);
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));

            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) {
                    selectedCandi = c;
                    applyFilters();
                    gulirKe(detailSectionWrapper);
                }
            });

            cardsGridPanel.add(card);
        }
        cardsGridPanel.revalidate();
        cardsGridPanel.repaint();
    }

    // =====================================
    // DETAIL POP-UP & SEMANTIC TABS SECTION
    // =====================================
    private JPanel createDetailSection() {
        JPanel wrapper = new JPanel(new BorderLayout(30, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(210, 195, 175, 80));
                g2.fillRoundRect(4, 4, getWidth() - 9, getHeight() - 9, 28, 28);
                g2.setColor(new Color(220, 205, 185, 120));
                g2.fillRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 26, 26);

                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(1120, 480));
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(25, 50, 50, 50),
            BorderFactory.createCompoundBorder(
                new RoundedBorder(24, COLOR_ACCENT),
                new EmptyBorder(24, 24, 24, 24)
            )
        ));
        wrapper.setBackground(COLOR_SURFACE);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(340, 420));

        detailTitle = new JLabel("Pilih Candi di Atas");
        detailTitle.setFont(new Font("Serif", Font.BOLD, 22));
        detailTitle.setForeground(COLOR_HERO);
        detailTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailSubtitle = new JLabel("Klik salah satu kartu untuk melihat detail ontologi");
        detailSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        detailSubtitle.setForeground(COLOR_MUTED);
        detailSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailArchPreview = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(warnaAgama(selectedCandi));

                int w = getWidth();
                int h = getHeight();
                g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, w, w));
                BufferedImage foto = PhotoService.get(selectedCandi);
                if (foto != null) {
                    PhotoService.gambarDalamBentuk(g2, foto, new RoundRectangle2D.Double(0, 0, w - 1, h - 1, w, w), w, h);
                }
                g2.dispose();
            }
        };
        detailArchPreview.setOpaque(false);
        detailArchPreview.setPreferredSize(new Dimension(110, 190));
        detailArchPreview.setMaximumSize(new Dimension(110, 190));
        detailArchPreview.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailArchPreview.setCursor(new Cursor(Cursor.HAND_CURSOR));
        detailArchPreview.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (selectedCandi != null) tampilkanPopup(selectedCandi);
            }
        });

        detailArchLabel = new JLabel("B");
        detailArchLabel.setFont(new Font("Serif", Font.BOLD, 46));
        detailArchLabel.setForeground(new Color(59, 42, 32, 160));
        detailArchPreview.add(detailArchLabel);

        detailTagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        detailTagsPanel.setOpaque(false);
        detailTagsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailDesc = new JLabel("<html><body style='width: 310px; color: #3B2A20; line-height: 1.4;'>Deskripsi lengkap candi akan muncul di sini saat Anda mengeklik salah satu entitas candi pada galeri di atas.</body></html>");
        detailDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        detailDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(detailTitle);
        leftPanel.add(detailSubtitle);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(detailArchPreview);
        leftPanel.add(Box.createVerticalStrut(12));
        leftPanel.add(detailTagsPanel);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(detailDesc);

        detailTabPane = new JTabbedPane();
        detailTabPane.setFont(new Font("SansSerif", Font.BOLD, 12));

        ringkasanTab = new JPanel(new BorderLayout());
        tripleTab = new JPanel(new BorderLayout());
        grafTab = new JPanel(new BorderLayout());
        sparqlTab = new JPanel(new BorderLayout());

        detailTabPane.addTab("Ringkasan", ringkasanTab);
        detailTabPane.addTab("Triple RDF", tripleTab);
        detailTabPane.addTab("Graf", grafTab);
        detailTabPane.addTab("SPARQL", sparqlTab);

        wrapper.add(leftPanel, BorderLayout.WEST);
        wrapper.add(detailTabPane, BorderLayout.CENTER);

        return wrapper;
    }

    private void updateDetailPanel() {
        if (selectedCandi == null) {
            detailSectionWrapper.setVisible(false);
            return;
        }

        detailSectionWrapper.setVisible(true);
        detailTitle.setText(selectedCandi.getNama());
        detailSubtitle.setText(selectedCandi.getLokasi());

        detailArchLabel.setText(String.valueOf(inisial(selectedCandi)));
        final CandiModel dipilih = selectedCandi;
        detailArchLabel.setVisible(PhotoService.get(dipilih) == null);
        detailArchPreview.repaint();
        PhotoService.request(dipilih, () -> {
            if (dipilih == selectedCandi) {
                detailArchLabel.setVisible(false);
                detailArchPreview.repaint();
            }
        });

        detailTagsPanel.removeAll();
        detailTagsPanel.add(createMetaTag(selectedCandi.getAgama()));
        detailTagsPanel.add(createMetaTag(selectedCandi.getTahunBerdiri()));
        detailTagsPanel.revalidate();

        detailDesc.setText("<html><body style='width: 300px; color: #3B2A20; line-height: 1.4;'>" + selectedCandi.getDeskripsi() + "</body></html>");

        ringkasanTab.removeAll();
        String[] cols = {"Properti Ontologi", "Nilai Entitas"};
        Object[][] data = {
            {"rdf:type", "candi:Candi"},
            {"namaCandi", selectedCandi.getNama()},
            {"memilikiLokasi", selectedCandi.getLokasi()},
            {"tahunBerdiri", selectedCandi.getTahunBerdiri()},
            {"beragama", selectedCandi.getAgama()},
            {"deskripsi", selectedCandi.getDeskripsi()}
        };
        JTable tRingkasan = new JTable(data, cols);
        styleTable(tRingkasan);
        ringkasanTab.add(new JScrollPane(tRingkasan), BorderLayout.CENTER);

        tripleTab.removeAll();
        String[] tCols = {"Subjek", "Predikat", "Objek"};
        Object[][] tData = {
            {"candi:" + selectedCandi.getId(), "rdf:type", "candi:Candi"},
            {"candi:" + selectedCandi.getId(), "rdfs:label", "\"" + selectedCandi.getNama() + "\"@id"},
            {"candi:" + selectedCandi.getId(), "candi:beragama", "agama:" + selectedCandi.getAgama()},
            {"candi:" + selectedCandi.getId(), "candi:terletakDi", "wilayah:" + selectedCandi.getLokasi()}
        };
        JTable tTriple = new JTable(tData, tCols);
        styleTable(tTriple);
        tripleTab.add(new JScrollPane(tTriple), BorderLayout.CENTER);

        grafTab.removeAll();
        JLabel grafSimulasi = new JLabel("<html><div style='text-align: center; padding: 25px; font-family: monospace;'>"
            + "<span style='background: #3B2A20; color: #FAF7F0; padding: 8px 16px; border-radius: 16px;'><b>" + selectedCandi.getNama() + "</b></span><br>"
            + "<span style='color: #A67C52; font-size: 16px;'>│</span><br>"
            + "├── (beragama) ────────▶ <b>[ " + selectedCandi.getAgama() + " ]</b><br>"
            + "├── (terletakDi) ──────▶ <b>[ " + selectedCandi.getLokasi() + " ]</b><br>"
            + "└── (dibangunPada) ────▶ <b>[ " + selectedCandi.getTahunBerdiri() + " ]</b>"
            + "</div></html>", SwingConstants.CENTER);
        grafTab.add(grafSimulasi, BorderLayout.CENTER);

        sparqlTab.removeAll();
        JTextArea sparqlArea = new JTextArea(
            "PREFIX candi:   <https://candinesia.example/ontologi#>\n" +
            "PREFIX agama:   <https://candinesia.example/agama/>\n" +
            "PREFIX wilayah: <https://candinesia.example/wilayah/>\n" +
            "PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>\n\n" +
            "SELECT ?candi ?nama WHERE {\n" +
            "  ?candi a candi:Candi ;\n" +
            "         rdfs:label \"" + selectedCandi.getNama() + "\"@id ;\n" +
            "         candi:beragama agama:" + selectedCandi.getAgama() + " .\n" +
            "}"
        );
        sparqlArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        sparqlArea.setBackground(COLOR_HERO);
        sparqlArea.setForeground(COLOR_BUDDHA);
        sparqlArea.setEditable(false);
        sparqlArea.setBorder(new EmptyBorder(12, 12, 12, 12));
        sparqlTab.add(new JScrollPane(sparqlArea), BorderLayout.CENTER);

        detailTabPane.revalidate();
        detailTabPane.repaint();
    }

    private void applyFilters() {
        List<CandiModel> filtered = allData.stream().filter(c -> {
            // 1. Filter dropdown Agama
            boolean matchAgama = filterAgama.isEmpty() || c.getAgama().equalsIgnoreCase(filterAgama);

            // 2. Filter dropdown Provinsi
            boolean matchProv = filterProvinsi.isEmpty() || c.getLokasi().toLowerCase().contains(filterProvinsi.toLowerCase());

            // 3. Pencarian Semantik Otomatis (Smart Search mirip Jamu Kating)
            boolean matchQ = true;
            if (!filterQuery.isEmpty()) {
                String queryLower = filterQuery.toLowerCase();
                String fullText = (c.getNama() + " " + c.getLokasi() + " " + c.getDeskripsi() + " " + c.getTahunBerdiri()).toLowerCase();

                // Mapping kata kunci fleksibel berdasarkan relasi geografis/karakteristik di OWL
                if (queryLower.contains("sumatra") || queryLower.contains("sumatera")) {
                    matchQ = fullText.contains("sumatra") || fullText.contains("jambi") || fullText.contains("riau") || fullText.contains("sumatera");
                } else if (queryLower.contains("jawa timur")) {
                    matchQ = fullText.contains("malang") || fullText.contains("blitar") || fullText.contains("mojokerto") || fullText.contains("jawa timur");
                } else if (queryLower.contains("jawa tengah")) {
                    matchQ = fullText.contains("magelang") || fullText.contains("klaten") || fullText.contains("wonosobo") || fullText.contains("semarang") || fullText.contains("karanganyar");
                } else if (queryLower.contains("dieng") || queryLower.contains("gunung")) {
                    matchQ = fullText.contains("wonosobo") || fullText.contains("dieng") || fullText.contains("lereng");
                } else if (queryLower.contains("buddha") || queryLower.contains("budha")) {
                    matchQ = c.getAgama().equalsIgnoreCase("Buddha") || fullText.contains("buddha");
                } else if (queryLower.contains("hindu")) {
                    matchQ = c.getAgama().equalsIgnoreCase("Hindu") || fullText.contains("hindu");
                } else {
                    // Pencarian teks biasa jika tidak masuk kategori semantik khusus
                    matchQ = fullText.contains(queryLower);
                }
            }

            return matchAgama && matchProv && matchQ;
        }).collect(Collectors.toList());

        // Memperbarui UI Card dan Label Jumlah
        countLabel.setText("(" + filtered.size() + " dari " + allData.size() + ")");
        renderCards(filtered);

        if (!filtered.isEmpty() && !filtered.contains(selectedCandi)) {
            selectedCandi = filtered.get(0);
        }
        updateDetailPanel();
    }

    private void styleTable(JTable table) {
        table.setRowHeight(24);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(COLOR_ACCENT);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(COLOR_LINE);
    }

    // =====================================
    // HELPER
    // =====================================

    /** Warna latar candi: cokelat tua untuk Hindu, krem untuk selainnya (juga saat candi null). */
    private Color warnaAgama(CandiModel c) {
        return c != null && "Hindu".equalsIgnoreCase(c.getAgama()) ? COLOR_HINDU : COLOR_BUDDHA;
    }

    /** Huruf pertama nama candi (tanpa kata "Candi"), dipakai sebagai monogram. */
    private static char inisial(CandiModel c) {
        return c.getNama().replace("Candi ", "").trim().charAt(0);
    }

    /** Menggulir halaman sampai komponen target berada di bagian atas. */
    private void gulirKe(JComponent target) {
        JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
        if (scrollPane != null) {
            scrollPane.getVerticalScrollBar().setValue(target.getY());
        }
    }

    // =====================================
    // POP-UP FOTO & INFO CANDI
    // =====================================
    private void tampilkanPopup(CandiModel c) {
        JDialog dlg = new JDialog(this, c.getNama(), false);
        dlg.setSize(780, 500);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(24, 0));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(22, 22, 22, 22));

        final Color warna = warnaAgama(c);
        JPanel foto = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                RoundRectangle2D.Double bentuk = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 40, 40);
                g2.setColor(warna);
                g2.fill(bentuk);
                BufferedImage img = PhotoService.get(c);
                if (img != null) PhotoService.gambarDalamBentuk(g2, img, bentuk, getWidth(), getHeight());
                g2.dispose();
            }
        };
        foto.setOpaque(false);
        foto.setPreferredSize(new Dimension(360, 0));
        JLabel huruf = new JLabel(String.valueOf(inisial(c)));
        huruf.setFont(new Font("Serif", Font.BOLD, 72));
        huruf.setForeground(new Color(59, 42, 32, 140));
        huruf.setVisible(PhotoService.get(c) == null);
        foto.add(huruf);
        PhotoService.request(c, () -> {
            huruf.setVisible(false);
            foto.repaint();
        });

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel judul = new JLabel(c.getNama());
        judul.setFont(new Font("Serif", Font.BOLD, 28));
        judul.setForeground(COLOR_HERO);
        judul.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lokasi = new JLabel(c.getLokasi());
        lokasi.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lokasi.setForeground(COLOR_MUTED);
        lokasi.setBorder(new EmptyBorder(4, 0, 10, 0));
        lokasi.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel tags = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tags.setOpaque(false);
        tags.setAlignmentX(Component.LEFT_ALIGNMENT);
        tags.add(createMetaTag(c.getAgama()));
        tags.add(Box.createHorizontalStrut(6));
        tags.add(createMetaTag(c.getTahunBerdiri()));
        tags.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel desk = new JLabel("<html><body style='width: 300px; color: #3B2A20; line-height: 1.5;'>"
                + c.getDeskripsi() + "</body></html>");
        desk.setFont(new Font("SansSerif", Font.PLAIN, 13));
        desk.setBorder(new EmptyBorder(14, 0, 0, 0));
        desk.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedButton btnDetail = new RoundedButton("Lihat detail ontologi", COLOR_HERO, COLOR_TEXT_HERO, COLOR_HERO);
        btnDetail.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnDetail.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDetail.addActionListener(e -> {
            dlg.dispose();
            selectedCandi = c;
            applyFilters();
            SwingUtilities.invokeLater(() -> gulirKe(detailSectionWrapper));
        });
        RoundedButton btnTutup = new RoundedButton("Tutup", COLOR_CHIP, COLOR_HERO, COLOR_LINE);
        btnTutup.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnTutup.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTutup.addActionListener(e -> dlg.dispose());

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tombol.setOpaque(false);
        tombol.setAlignmentX(Component.LEFT_ALIGNMENT);
        tombol.add(btnDetail);
        tombol.add(btnTutup);
        tombol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        info.add(judul);
        info.add(lokasi);
        info.add(tags);
        info.add(desk);
        info.add(Box.createVerticalGlue());
        info.add(tombol);

        root.add(foto, BorderLayout.WEST);
        root.add(info, BorderLayout.CENTER);
        root.registerKeyboardAction(e -> dlg.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // =====================================
    // UNDUH SEMUA FOTO KE FOLDER foto/
    // =====================================
    private void mulaiUnduhFoto(RoundedButton tombol) {
        if (allData.isEmpty()) return;
        tombol.setEnabled(false);
        final java.nio.file.Path folder = java.nio.file.Paths.get("foto");
        final List<CandiModel> daftar = new ArrayList<>(allData);

        SwingWorker<int[], String> worker = new SwingWorker<>() {
            @Override
            protected int[] doInBackground() {
                int ok = 0, ada = 0, gagal = 0, i = 0;
                List<String> daftarGagal = new ArrayList<>();
                for (CandiModel c : daftar) {
                    i++;
                    publish("Mengunduh foto " + i + "/" + daftar.size() + ": " + c.getNama() + " ...");
                    String s = PhotoService.simpanKeFolder(c, folder);
                    if ("ok".equals(s)) ok++;
                    else if ("ada".equals(s)) ada++;
                    else { gagal++; daftarGagal.add(c.getId() + ".jpg  (" + c.getNama() + ")"); }
                    try { Thread.sleep(250); } catch (InterruptedException ie) { break; }
                }
                try {
                    java.nio.file.Files.createDirectories(folder);
                    java.nio.file.Files.write(folder.resolve("DAFTAR-GAGAL.txt"), daftarGagal);
                } catch (Exception ignore) { }
                return new int[]{ok, ada, gagal};
            }

            @Override
            protected void process(List<String> chunks) {
                statsLabel.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                try {
                    int[] r = get();
                    statsLabel.setText("Selesai: " + r[0] + " foto baru, " + r[1] + " sudah ada, " + r[2]
                            + " gagal (isi manual di folder foto/, lihat foto/DAFTAR-GAGAL.txt).");
                } catch (Exception e) {
                    statsLabel.setText("Unduh foto gagal: " + e.getMessage());
                }
                tombol.setEnabled(true);
                PhotoService.reset();
                penyegarHero.forEach(Runnable::run);
                applyFilters();
            }
        };
        worker.execute();
    }

    private JLabel createMetaTag(String text) {
        JLabel tag = new JLabel(" " + text + " ");
        tag.setFont(new Font("SansSerif", Font.BOLD, 11));
        tag.setOpaque(true);
        tag.setBackground(COLOR_CHIP);
        tag.setForeground(COLOR_HERO);
        tag.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(18, COLOR_ACCENT),
            new EmptyBorder(5, 12, 5, 12)
        ));
        return tag;
    }

    // =====================================
    // CUSTOM UI COMPONENTS (ROUNDED ELEMENTS)
    // =====================================
    private static class RoundedButton extends JButton {
        private Color backgroundColor;
        private Color textColor;
        private Color borderColor;

        public RoundedButton(String text, Color bgColor, Color fgColor, Color borderColor) {
            super(text);
            this.backgroundColor = bgColor;
            this.textColor = fgColor;
            this.borderColor = borderColor;
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(fgColor);
            setBorder(new EmptyBorder(10, 20, 10, 20));
        }

        public void setBackgroundColor(Color bgColor) {
            this.backgroundColor = bgColor;
            repaint();
        }

        public void setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(backgroundColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight()));

            g2.setColor(borderColor);
            g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight()));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius/3, this.radius/2, this.radius/3, this.radius/2);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = this.radius/2;
            insets.top = insets.bottom = this.radius/3;
            return insets;
        }
    }

    // =====================================
    // MAIN METHOD
    // =====================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Gagal memuat FlatLaf.");
        }

        EventQueue.invokeLater(() -> {
            new CandiFrame().setVisible(true);
        });
    }
}