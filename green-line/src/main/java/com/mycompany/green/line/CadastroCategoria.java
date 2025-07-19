package com.mycompany.green.line;

import java.awt.Component;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class CadastroCategoria extends javax.swing.JInternalFrame {

    private static final Logger LOGGER = Logger.getLogger(CadastroCategoria.class.getName());
     private static final String SELECT_CATEGORY = "SELECT id_categoria,categoria FROM categorias";
    private static final String SELECT_CATEGORIES = "SELECT id_categoria, categoria, descricao FROM categorias ORDER BY id_categoria ASC";
    private static final String ERROR_DB_ACCESS = "Erro ao carregar dados: ";
    private static final String ERROR_GENERIC = "Erro inesperado: ";
    private static final Font TABLE_FONT = new Font("Arial", Font.PLAIN, 19);
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 20);

    private String descricaoAntiga;
    private int idCategoria;
    private final DefaultTableModel modeloTabela;
    private final Font font = TABLE_FONT;
    private final Font headerFont = HEADER_FONT;
    Funcoes funcoes = new Funcoes();

    public CadastroCategoria() {
        initComponents();
        LOGGER.info("Inicializando interface de cadastro de categorias.");
        funcoes.aplicarMascaraNome(nomeCategoria);
        
        // Configurando a tabela
        tabela.setDefaultRenderer(Object.class, new MultiLineCellRenderer());
        modeloTabela = (DefaultTableModel) tabela.getModel();
        tabela.setFont(font);
        carregarDadosTabela();
        setResizable(false);

        // Configurando o cabeçalho da tabela
        JTableHeader header = tabela.getTableHeader();
        header.setFont(headerFont);

        // Aplicando filtro de entrada ao campo nomeCategoria
        //funcoes).applyTextAndNumberFilter(nomeCategoria);
    }

    public void Apagar() {
        LOGGER.info("Limpando campos do formulário.");
        nomeCategoria.setText("");
        descricaoArea.setText("");
    }

    private void carregarDadosTabela() {
        LOGGER.info("Carregando dados das categorias na tabela.");
        try (Connection con = Conexao.conexaoBanco();
             PreparedStatement stmt = con.prepareStatement(SELECT_CATEGORIES);
             ResultSet rs = stmt.executeQuery()) {
            modeloTabela.setNumRows(0);
            while (rs.next()) {
                Object[] dados = {
                    rs.getInt("id_categoria"),
                    rs.getString("categoria"),
                    rs.getString("descricao")
                };
                modeloTabela.addRow(dados);
            }
            LOGGER.info("Categorias carregadas com sucesso.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ERROR_DB_ACCESS + ex.getMessage(), ex);
            JOptionPane.showMessageDialog(null, ERROR_DB_ACCESS + ex.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, ERROR_GENERIC + e.getMessage(), e);
            JOptionPane.showMessageDialog(null, ERROR_GENERIC + e.getMessage());
        }
    }

    class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {
        public MultiLineCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value.toString());
            setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
            setFont(table.getFont());

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }

            if (table.getRowHeight(row) != getPreferredSize().height) {
                table.setRowHeight(row, getPreferredSize().height);
            }

            return this;
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btCadastrar = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        descricaoArea = new javax.swing.JTextArea();
        btCancelar = new javax.swing.JButton();
        nomeCategoria = new javax.swing.JTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        setClosable(true);
        setForeground(new java.awt.Color(0, 0, 0));
        setIconifiable(true);

        btCadastrar.setBackground(new java.awt.Color(43, 189, 49));
        btCadastrar.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btCadastrar.setForeground(new java.awt.Color(255, 255, 255));
        btCadastrar.setText("Cadastrar");
        btCadastrar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btCadastrarMouseClicked(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(255, 153, 0));
        jButton2.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Alterar");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });

        tabela.setBackground(new java.awt.Color(255, 242, 207));
        tabela.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        tabela.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Subcategorias ", "Descrição "
            }
        ));
        tabela.setRowHeight(50);
        tabela.setSelectionBackground(new java.awt.Color(102, 102, 102));
        tabela.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tabela);
        if (tabela.getColumnModel().getColumnCount() > 0) {
            tabela.getColumnModel().getColumn(0).setMaxWidth(100);
        }

        jLabel4.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel4.setLabelFor(nomeCategoria);
        jLabel4.setText("Nome:");

        jLabel1.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel1.setLabelFor(descricaoArea);
        jLabel1.setText("Descrição:");

        descricaoArea.setColumns(20);
        descricaoArea.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        descricaoArea.setLineWrap(true);
        descricaoArea.setRows(5);
        descricaoArea.setWrapStyleWord(true);
        jScrollPane2.setViewportView(descricaoArea);

        btCancelar.setBackground(new java.awt.Color(255, 0, 0));
        btCancelar.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btCancelar.setForeground(new java.awt.Color(255, 255, 255));
        btCancelar.setText("Cancelar");
        btCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelarActionPerformed(evt);
            }
        });

        nomeCategoria.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btCadastrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btCancelar))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                            .addComponent(nomeCategoria)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel4))
                                .addGap(0, 381, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 594, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(62, 62, 62))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nomeCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btCancelar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btCadastrar)
                        .addComponent(jButton2)))
                .addGap(48, 48, 48))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btCadastrarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btCadastrarMouseClicked
        if (nomeCategoria.getText() == null || descricaoArea.getText() == null || 
            nomeCategoria.getText().trim().isEmpty() || descricaoArea.getText().trim().isEmpty()) {
            funcoes.Avisos("sinal-de-aviso.png", "Campos vazios não são aceitos");
            return;
        }
        

        try {
            Connection con = Conexao.conexaoBanco();
            // Verificar duplicações
            String sqlCheck = "SELECT COUNT(*) FROM categorias WHERE categoria = ?";
            PreparedStatement stmtCheck = con.prepareStatement(sqlCheck);
            stmtCheck.setString(1, nomeCategoria.getText().trim());
            ResultSet rs = stmtCheck.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            stmtCheck.close();
            rs.close();

            if (count > 0) {
                funcoes.Avisos("sinal-de-aviso.png", "Categoria repetida. Insira outra");
                return;
            }

            String sql = "INSERT INTO categorias (categoria, descricao) VALUES (?, ?);";
            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setString(1, nomeCategoria.getText());
            stmt.setString(2, descricaoArea.getText());

            stmt.executeUpdate();
            stmt.close();
            funcoes.Avisos("confirmacao.png", "Categoria cadastrada");
            Apagar();
            carregarDadosTabela();
        } catch (SQLException ex) {
            Logger.getLogger(CadastroCategoria.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro: " + ex.getMessage());
        }
    }//GEN-LAST:event_btCadastrarMouseClicked

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
   try {
            Connection con = Conexao.conexaoBanco();
            if (con == null) {
                funcoes.Avisos("erro.png", "Erro ao conectar ao banco de dados.");
                return;
            }

            if (idCategoria == 0) {
                funcoes.Avisos("erro.png", "Selecione uma categoria para alterar.");
                return;
            }

            if (nomeCategoria.getText().trim().isEmpty()) {
                funcoes.Avisos("erro.png", "O nome não pode estar vazio.");
                return;
            }

            String sql = "UPDATE categorias SET categoria = ?, descricao = ? WHERE id_categoria = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, nomeCategoria.getText());
            stmt.setString(2, descricaoArea.getText());
            stmt.setInt(3, idCategoria);

            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            con.close();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Categoria alterada com sucesso!");
                Apagar();
                carregarDadosTabela();
            } else {
                JOptionPane.showMessageDialog(null, "Nenhuma categoria foi alterada.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(CadastroCategoria.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao alterar categoria: " + ex.getMessage());
        }
    }//GEN-LAST:event_jButton2MouseClicked


    private void tabelaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabelaMouseClicked
        int linha = tabela.getSelectedRow();
        if (linha >= 0) {
            descricaoAntiga = tabela.getValueAt(linha, 2).toString();
            idCategoria = (int) tabela.getValueAt(linha, 0);

            nomeCategoria.setText((tabela.getValueAt(linha, 1).toString()));
            descricaoArea.setText(descricaoAntiga);
            System.out.println("ID da categoria selecionada: " + idCategoria);
        }
    }//GEN-LAST:event_tabelaMouseClicked

    private void btCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelarActionPerformed
        Apagar();
    }//GEN-LAST:event_btCancelarActionPerformed

    


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCadastrar;
    private javax.swing.JButton btCancelar;
    private javax.swing.JTextArea descricaoArea;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField nomeCategoria;
    private javax.swing.JTable tabela;
    // End of variables declaration//GEN-END:variables

}
