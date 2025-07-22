package com.mycompany.green.line;

import javax.swing.*;

public class TelaComImagem extends JFrame {

    public TelaComImagem() {
        ImageIcon img = new ImageIcon(getClass().getResource("/imagens/logo.png"));
        
        if (img.getImage() == null) {
            System.err.println("Imagem não encontrada!");
            return;
        }
        
        setIconImage(img.getImage());
        JLabel label = new JLabel(img);
        add(label);

        setTitle("Imagem no Executável");
        setSize(300, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaComImagem().setVisible(true));
    }
}
