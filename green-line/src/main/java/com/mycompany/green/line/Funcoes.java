package com.mycompany.green.line;

import java.awt.Image;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import javax.swing.JTextField;
import javax.swing.*;
import javax.swing.text.*;

/**
 *
 * @author kaua-n-c
 */
public class Funcoes {

    private String endereco = "imagens/";

// Método para aplicar a máscara de nome em qualquer JTextField
    public static void aplicarMascaraNome(JTextField textField) {
        PlainDocument doc = (PlainDocument) textField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                if (text == null) {
                    return;
                }
                // Regex atualizada para incluir TODOS os caracteres acentuados do português:
                if (text.matches("[a-zA-ZÀ-ÿÇçÃãÕõÁáÉéÍíÓóÚúÂâÊêÎîÔôÛûÄäËëÏïÖöÜüÑñ ]+")) {
                    super.insertString(fb, offset, text, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) {
                    return;
                }
                if (text.matches("[a-zA-ZÀ-ÿÇçÃãÕõÁáÉéÍíÓóÚúÂâÊêÎîÔôÛûÄäËëÏïÖöÜüÑñ ]+")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }
    // Método para aplicar a máscara de nome (letras, acentos, espaços e números)

    public static void aplicarMascaraNomeNumero(JTextField textField) {
        PlainDocument doc = (PlainDocument) textField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                if (text == null) {
                    return;
                }
                if (text.matches("[a-zA-ZÀ-ú0-9 ]+")) { // Agora também permite números
                    super.insertString(fb, offset, text, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) {
                    return;
                }
                if (text.matches("[a-zA-ZÀ-ú0-9 ]+")) { // Agora também permite números
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    public static void aplicarMascaraPreco(JTextField textField) {
        PlainDocument doc = (PlainDocument) textField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + text;
                if (validarPreco(newText)) {
                    super.insertString(fb, offset, text, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
                if (validarPreco(newText)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            private boolean validarPreco(String text) {
                // Permite: números, um único ponto, e até 2 casas decimais
                return text.matches("^\\d*\\.?\\d{0,2}$") || text.isEmpty();
            }
        });
    }

    public static void aplicarMascaraCPF(JTextField textField) {
        PlainDocument doc = (PlainDocument) textField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                if (text == null || !text.matches("\\d+")) {
                    return; // Só aceita dígitos
                }

                StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
                sb.insert(offset, text);
                String numeros = sb.toString().replaceAll("\\D", "");
                String formatado = formatarCPF(numeros);

                fb.remove(0, fb.getDocument().getLength());
                super.insertString(fb, 0, formatado, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null || !text.matches("\\d*")) {
                    return;
                }

                StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
                sb.replace(offset, offset + length, text);
                String numeros = sb.toString().replaceAll("\\D", "");
                String formatado = formatarCPF(numeros);

                fb.remove(0, fb.getDocument().getLength());
                super.insertString(fb, 0, formatado, attrs);
            }

            private String formatarCPF(String numeros) {
                if (numeros.length() > 11) {
                    numeros = numeros.substring(0, 11);
                }

                if (numeros.length() <= 3) {
                    return numeros;
                } else if (numeros.length() <= 6) {
                    return numeros.substring(0, 3) + "." + numeros.substring(3);
                } else if (numeros.length() <= 9) {
                    return numeros.substring(0, 3) + "."
                            + numeros.substring(3, 6) + "."
                            + numeros.substring(6);
                } else {
                    return numeros.substring(0, 3) + "."
                            + numeros.substring(3, 6) + "."
                            + numeros.substring(6, 9) + "-"
                            + numeros.substring(9);
                }
            }
        });
    }

    public static void aplicarMascaraCEP(JTextField textField) {
        PlainDocument doc = (PlainDocument) textField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                if (text == null || !text.matches("\\d+")) {
                    return; // Só permite números
                }

                StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
                sb.insert(offset, text);
                String numeros = sb.toString().replaceAll("\\D", "");
                String formatado = formatarCEP(numeros);

                fb.remove(0, fb.getDocument().getLength());
                super.insertString(fb, 0, formatado, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null || !text.matches("\\d*")) {
                    return;
                }

                StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
                sb.replace(offset, offset + length, text);
                String numeros = sb.toString().replaceAll("\\D", "");
                String formatado = formatarCEP(numeros);

                fb.remove(0, fb.getDocument().getLength());
                super.insertString(fb, 0, formatado, attrs);
            }

            private String formatarCEP(String numeros) {
                if (numeros.length() > 8) {
                    numeros = numeros.substring(0, 8);
                }

                if (numeros.length() <= 5) {
                    return numeros;
                } else {
                    return numeros.substring(0, 5) + "-" + numeros.substring(5);
                }
            }
        });
    }

    public static void aplicarMascaraTelefone(JTextField textField) {
        PlainDocument doc = (PlainDocument) textField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                if (text == null || !text.matches("\\d+")) {
                    return; // Só permite números
                }

                StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
                sb.insert(offset, text);
                String numeros = sb.toString().replaceAll("\\D", "");
                String formatado = formatarTelefone(numeros);

                fb.remove(0, fb.getDocument().getLength());
                super.insertString(fb, 0, formatado, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null || !text.matches("\\d*")) {
                    return; // Só permite números
                }

                StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
                sb.replace(offset, offset + length, text);
                String numeros = sb.toString().replaceAll("\\D", "");
                String formatado = formatarTelefone(numeros);

                fb.remove(0, fb.getDocument().getLength());
                super.insertString(fb, 0, formatado, attrs);
            }

            private String formatarTelefone(String numeros) {
                if (numeros.length() > 11) {
                    numeros = numeros.substring(0, 11); // Limita a 11 dígitos
                }

                if (numeros.length() <= 2) {
                    return "(" + numeros;
                } else if (numeros.length() <= 7) {
                    return "(" + numeros.substring(0, 2) + ") " + numeros.substring(2);
                } else if (numeros.length() <= 11) {
                    return "(" + numeros.substring(0, 2) + ") "
                            + numeros.substring(2, 7) + "-"
                            + numeros.substring(7);
                }
                return numeros;
            }
        });
    }

    public static void aplicarMascaraInteiro(JTextField textField) {
        PlainDocument doc = (PlainDocument) textField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                if (text.matches("\\d*")) { // Permite apenas dígitos (0-9)
                    super.insertString(fb, offset, text, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text.matches("\\d*")) { // Permite apenas dígitos (0-9)
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    public static void aplicarMascaraPeso(JTextField textField) {
        PlainDocument doc = (PlainDocument) textField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + text;
                if (validarPeso(newText)) {
                    super.insertString(fb, offset, text, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
                if (validarPeso(newText)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            private boolean validarPeso(String text) {
                // Permite: números, um único ponto, e até 3 casas decimais (ex.: 1.500)
                return text.matches("^\\d*\\.?\\d{0,3}$") || text.isEmpty();
            }
        });
    }

    // Permite letras (com acentos), números, espaços e símbolos básicos (. , ! ? etc)
    public static void aplicarMascaraTextoNumerico(JTextArea textArea) {
        if (textArea == null) {
            return;
        }

        ((AbstractDocument) textArea.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                if (text == null) {
                    return;
                }
                if (text.matches("[a-zA-ZÀ-ú0-9\\s,.!?;:()'\"-]*")) {
                    super.insertString(fb, offset, text, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) {
                    return;
                }
                if (text.matches("[a-zA-ZÀ-ú0-9\\s,.!?;:()'\"-]*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    public static void aplicarValidacaoEmail(JTextField textField) {
        // Máscara para permitir apenas caracteres válidos em e-mails
        PlainDocument doc = (PlainDocument) textField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + text;
                if (isValidEmailInput(newText)) {
                    super.insertString(fb, offset, text, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
                if (isValidEmailInput(newText)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            private boolean isValidEmailInput(String input) {
                // Permite caracteres comuns de e-mail durante a digitação
                return input.matches("^[a-zA-Z0-9@._-]*$");
            }
        });

        // Validação quando o campo perde o foco
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String email = textField.getText();
                if (!email.isEmpty() && !isValidEmail(email)) {
                    JOptionPane.showMessageDialog(textField,
                            "Por favor, insira um e-mail válido.\nExemplo: usuario@dominio.com",
                            "E-mail inválido",
                            JOptionPane.WARNING_MESSAGE);
                    textField.requestFocus();
                }
            }

            private boolean isValidEmail(String email) {
                // Regex para validar e-mail conforme RFC 5322
                String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
                return email.matches(regex);
            }
        });
    }

    public static void aplicarMascaraSenha(JPasswordField passwordField) {
        PlainDocument doc = (PlainDocument) passwordField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + text + currentText.substring(offset);

                if (newText.length() <= 5 && validarSenha(newText)) {
                    super.insertString(fb, offset, text, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);

                if (newText.length() <= 5 && validarSenha(newText)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            private boolean validarSenha(String senha) {
                // Só valida quando atingir 5 caracteres
                if (senha.length() != 5) {
                    return true; // Permite digitação intermediária
                }

                boolean temLetra = senha.matches(".*[a-zA-Z].*");
                boolean temNumero = senha.matches(".*\\d.*");

                return temLetra && temNumero;
            }
        });

        // Validação final quando o campo perde o foco
        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String senha = new String(passwordField.getPassword());
                if (senha.length() != 5 || !validarSenha(senha)) {
                    JOptionPane.showMessageDialog(passwordField,
                            "A senha deve ter exatamente 5 caracteres, com pelo menos 1 letra e 1 número.",
                            "Senha inválida",
                            JOptionPane.WARNING_MESSAGE);
                   
                }
            }

            private boolean validarSenha(String senha) {
                return senha.matches("^(?=.*[A-Za-z])(?=.*\\d).{5}$");
            }
        });
    }

    public static String removePontuacaoEEspacos(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("[.\\-\\s()]", "");
    }

    public void Avisos(String icone, String mensagem) {
        ImageIcon imagem = new ImageIcon(endereco + icone);
        if (imagem.getIconWidth() == -1) {
            JOptionPane.showMessageDialog(null, mensagem, "Mensagem", JOptionPane.INFORMATION_MESSAGE);
        } else {
            Image image = imagem.getImage();
            Image scaledImage = image.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            String mensagemFormatada = "<html><h2>" + mensagem + "</h2></html>";
            JLabel titulo = new JLabel(mensagemFormatada);
            JOptionPane.showMessageDialog(null, titulo, "Mensagem", JOptionPane.INFORMATION_MESSAGE, scaledIcon);
        }
    }

    public static void mostrarMensagemCarregando() {
        // Mostra mensagem modal (bloqueante)
        JOptionPane.showMessageDialog(null,
                "Carregando imagem...",
                "Aguarde",
                JOptionPane.INFORMATION_MESSAGE);
    }

}
