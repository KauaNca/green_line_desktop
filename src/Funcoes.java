
import java.awt.Image;
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
                if (text.matches("[a-zA-ZÀ-ú ]+")) { // Permite letras, acentos e espaços
                    super.insertString(fb, offset, text, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) {
                    return;
                }
                if (text.matches("[a-zA-ZÀ-ú ]+")) { // Permite letras, acentos e espaços
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
