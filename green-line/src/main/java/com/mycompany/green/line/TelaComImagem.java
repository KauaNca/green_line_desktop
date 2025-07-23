package com.mycompany.green.line;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class TelaComImagem {

    private static Image iconImage;

    // Retorna imagem do ícone (logo)
    public static Image getIconImage() {
        if (iconImage == null) {
            URL imgURL = TelaComImagem.class.getResource("/imagens/logo.png");
            if (imgURL != null) {
                iconImage = Toolkit.getDefaultToolkit().getImage(imgURL);
                System.out.println("Ícone carregado: " + imgURL);
            } else {
                System.err.println("Ícone NÃO encontrado em /imagens/logo.png");
            }
        }
        return iconImage;
    }

    // Aplica ícone na barra de título (para JFrame, JDialog, JWindow)
    public static void applyIcon(Window window) {
        Image icon = getIconImage();
        if (icon != null && window != null) {
            if (window instanceof JFrame) {
                ((JFrame) window).setIconImage(icon);
            } else if (window instanceof JDialog) {
                ((JDialog) window).setIconImage(icon);
            } else if (window instanceof JWindow) {
                ((JWindow) window).setIconImage(icon);
            } else {
                System.err.println("Tipo de janela não suportado para ícone: " + window.getClass().getName());
            }
        }
    }

    // Aplica logo visualmente dentro de um JPanel (ex: JInternalFrame)
    public static void applyLogo(JPanel painel) {
        try {
            URL imgURL = TelaComImagem.class.getResource("/imagens/logo.png");
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                JLabel label = new JLabel(icon);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                painel.add(label, BorderLayout.NORTH); // Ou outro layout se desejar
            } else {
                System.err.println("Logo não encontrado em /imagens/logo.png");
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar logo: " + e.getMessage());
        }
    }
}
