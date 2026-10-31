package com.mycompany.candisemantikapp;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;

public class CandiSemantikApp {
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