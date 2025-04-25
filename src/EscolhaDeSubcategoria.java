
/**
 * JFrame para seleção de subcategorias associadas a uma categoria específica.
 * Exibe opções de subcategorias como botões de rádio e permite ao usuário escolher uma.
 *
 * @author kauan
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import java.util.logging.Logger;

/**
 * Classe para gerenciar a seleção de subcategorias no sistema.
 */
public class EscolhaDeSubcategoria extends javax.swing.JFrame {

    // Logger para rastreamento de eventos e erros
    private static final Logger LOGGER = Logger.getLogger(EscolhaDeSubcategoria.class.getName());

    // Constantes para queries SQL e configurações de UI
    private static final String COUNT_SUBCATEGORIES = "SELECT COUNT(*) FROM subcategorias WHERE id_categoria = ?";
    private static final String SELECT_SUBCATEGORIES = "SELECT subcategoria FROM subcategorias WHERE id_categoria = ?";
    private static final Color PANEL_BACKGROUND_COLOR = new Color(255, 242, 207);
    private static final Color TEXT_COLOR = Color.BLACK;
    private static final Font RADIO_BUTTON_FONT = new Font("Arial", Font.PLAIN, 18);
    private static final Dimension PANEL_SIZE = new Dimension(400, 106);
    private static final String ERROR_MESSAGE = "Desculpe, um erro aconteceu";
    private static final String DEFAULT_SUBCATEGORY = "Nenhuma";

    // Componentes e variáveis
    private JRadioButton[] subcategorias;
    private final ButtonGroup escolhas = new ButtonGroup();
    private String subcategoria;
    private String nomeCategoria;
    private String id_categoria;

    /**
     * Retorna a subcategoria selecionada.
     *
     * @return Nome da subcategoria.
     */
    public String getSubcategoria() {
        return subcategoria;
    }

    /**
     * Define a subcategoria selecionada.
     *
     * @param subcategoria Nome da subcategoria.
     */
    public void setSubcategoria(String subcategoria) {
        this.subcategoria = subcategoria;
    }

    /**
     * Construtor padrão. Inicializa a interface gráfica.
     */
    public EscolhaDeSubcategoria() {
        initUI();
    }

    /**
     * Construtor com ID da categoria. Inicializa a interface gráfica e carrega
     * as subcategorias associadas ao ID fornecido.
     *
     * @param id_categoria ID da categoria para buscar subcategorias.
     */
    public EscolhaDeSubcategoria(String id_categoria) {
        this.id_categoria = id_categoria;
        initUI();
        Subcategorias(id_categoria);
    }

    /**
     * Inicializa a interface gráfica, configurando o layout, painel, cores e
     * componentes visuais.
     */
    public void initUI() {
        LOGGER.info("Inicializando interface gráfica.");
        initComponents();
        setLayout(new BorderLayout());
        painelEscolhas.setPreferredSize(PANEL_SIZE);
        painelEscolhas.setBackground(PANEL_BACKGROUND_COLOR);
        painelEscolhas.setLayout(new GridLayout(0, 1));
        painelEscolhas.add(titulo);
        painelEscolhas.add(btOK);

        setResizable(false); // Impede redimensionamento da janela
        add(painelEscolhas, BorderLayout.CENTER);
        setVisible(true); // Torna a janela visível
    }

    /**
     * Carrega as subcategorias do banco de dados com base no ID da categoria e
     * adiciona botões de rádio para cada subcategoria no painel.
     *
     * @param id_categoria ID da categoria para buscar subcategorias.
     */
    public void Subcategorias(String id_categoria) {
        LOGGER.info("Carregando subcategorias para a categoria ID: " + id_categoria);
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(COUNT_SUBCATEGORIES)) {
            // Conta o número de subcategorias
            stmt.setString(1, id_categoria);
            try (ResultSet rs = stmt.executeQuery()) {
                int numeroDeLinhas = 0;
                if (rs.next()) {
                    numeroDeLinhas = rs.getInt(1);
                }

                // Inicializa o array de botões de rádio
                subcategorias = new JRadioButton[numeroDeLinhas];

                // Busca as subcategorias
                try (PreparedStatement stmt2 = con.prepareStatement(SELECT_SUBCATEGORIES)) {
                    stmt2.setString(1, id_categoria);
                    try (ResultSet rs2 = stmt2.executeQuery()) {
                        int x = 0;
                        while (rs2.next()) {
                            String nomeSubcategoria = rs2.getString("subcategoria");
                            LOGGER.info("Subcategoria encontrada: " + nomeSubcategoria);
                            subcategorias[x] = new JRadioButton(nomeSubcategoria);
                            subcategorias[x].setActionCommand(nomeSubcategoria); // Define o comando de ação
                            subcategorias[x].setFont(RADIO_BUTTON_FONT);
                            subcategorias[x].setForeground(TEXT_COLOR);
                            subcategorias[x].setBackground(PANEL_BACKGROUND_COLOR);
                            escolhas.add(subcategorias[x]);
                            painelEscolhas.add(subcategorias[x]);
                            x++;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.severe("Erro ao carregar subcategorias: " + ex.getMessage());
            new CadastroProdutos().Avisos("imagens/erro.png", ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelEscolhas = new javax.swing.JPanel();
        titulo = new javax.swing.JLabel();
        btOK = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        titulo.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        titulo.setText("Escolha:");

        btOK.setBackground(new java.awt.Color(50, 205, 50));
        btOK.setForeground(new java.awt.Color(255, 255, 255));
        btOK.setText("OK");
        btOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btOKActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout painelEscolhasLayout = new javax.swing.GroupLayout(painelEscolhas);
        painelEscolhas.setLayout(painelEscolhasLayout);
        painelEscolhasLayout.setHorizontalGroup(
            painelEscolhasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelEscolhasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titulo, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(259, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelEscolhasLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btOK)
                .addContainerGap())
        );
        painelEscolhasLayout.setVerticalGroup(
            painelEscolhasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelEscolhasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 132, Short.MAX_VALUE)
                .addComponent(btOK))
        );

        btOK.getAccessibleContext().setAccessibleParent(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(painelEscolhas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(painelEscolhas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(42, 42, 42))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /**
     * Ação executada ao fechar a janela. Define a subcategoria selecionada ou
     * um valor padrão se nenhuma for escolhida.
     *
     * @param evt Evento de fechamento da janela.
     */
    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        LOGGER.info("Janela de escolha de subcategoria fechada.");
        if (escolhas.getSelection() != null) {
            subcategoria = escolhas.getSelection().getActionCommand();
            LOGGER.info("Subcategoria selecionada: " + subcategoria);
        } else {
            subcategoria = DEFAULT_SUBCATEGORY;
            LOGGER.info("Nenhuma subcategoria selecionada. Usando valor padrão: " + DEFAULT_SUBCATEGORY);
        }

    }//GEN-LAST:event_formWindowClosed

    /**
     * Ação executada ao clicar no botão "OK". Define a subcategoria selecionada
     * ou um valor padrão e fecha a janela.
     *
     * @param evt Evento de ação do botão.
     */

    private void btOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btOKActionPerformed
        LOGGER.info("Botão OK clicado.");
        if (escolhas.getSelection() != null) {
            subcategoria = escolhas.getSelection().getActionCommand();
            LOGGER.info("Subcategoria selecionada: " + subcategoria);
        } else {
            subcategoria = DEFAULT_SUBCATEGORY;
            LOGGER.info("Nenhuma subcategoria selecionada. Usando valor padrão: " + DEFAULT_SUBCATEGORY);
        }
        this.dispose();

    }//GEN-LAST:event_btOKActionPerformed
    /**
     * Método principal para inicializar a aplicação com o Look and Feel Nimbus
     * e exibir a janela de escolha de subcategoria.
     *
     * @param args Argumentos da linha de comando.
     */

    public static void main(String args[]) {
        LOGGER.info("Inicializando aplicação EscolhaDeSubcategoria.");
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    LOGGER.info("Configurando Look and Feel: Nimbus");
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            LOGGER.severe("Erro ao configurar Look and Feel: " + ex.getMessage());
            java.util.logging.Logger.getLogger(EscolhaDeSubcategoria.class.getName()).log(
                    java.util.logging.Level.SEVERE, null, ex);
        }

        EventQueue.invokeLater(() -> {
            LOGGER.info("Criando e exibindo janela de escolha de subcategoria.");
            new EscolhaDeSubcategoria().setVisible(true);
        });

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btOK;
    private javax.swing.JPanel painelEscolhas;
    private javax.swing.JLabel titulo;
    // End of variables declaration//GEN-END:variables
}
