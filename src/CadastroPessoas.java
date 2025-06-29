/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.Component;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author HP
 */
public class CadastroPessoas extends javax.swing.JInternalFrame {

    private String situacaoValor;
    private String tipoUsuarioId;
   
    
    public CadastroPessoas() {
        initComponents();
        try (Connection con = Conexao.conexaoBanco()) {
            String sql = "SELECT * FROM pessoa ORDER BY id_pessoa DESC";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel modeloTabela = (DefaultTableModel) tabela.getModel();
            modeloTabela.setNumRows(0);
            while (rs.next()) {
               
                Object[] dados = {
                    rs.getInt("id_pessoa"),
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("telefone"),
                    rs.getString("cpf"),
                    rs.getInt("id_tipo_usuario"),
                    rs.getString("situacao")
                };
                modeloTabela.addRow(dados);
            }

            stmt.close();
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(CadastroPessoas.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        situacao = new javax.swing.JComboBox<>();
        cadastrar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cpf = new javax.swing.JTextField();
        nome = new javax.swing.JTextField();
        email = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        Senha = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        tipo = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        telefone = new javax.swing.JTextField();
        Cancelar = new javax.swing.JButton();
        perfil = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        situacao.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Qual a Situacão?", "Ativo", "Inativo", "Bloqueado" }));
        situacao.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        situacao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                situacaoActionPerformed(evt);
            }
        });

        cadastrar.setBackground(new java.awt.Color(0, 153, 0));
        cadastrar.setText("Cadastrar");
        cadastrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cadastrarActionPerformed(evt);
            }
        });

        tabela.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "id_pessoa", "nome", "Email", "telefone", "cpf", "tipo", "situacao"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tabela);

        jLabel1.setText("Nome:");

        jLabel2.setText("Email:");

        jLabel3.setText("CPF:");

        jLabel4.setText("Situação:");

        jLabel5.setText("Senha:");

        Senha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SenhaActionPerformed(evt);
            }
        });

        jLabel6.setText("Tipo:");

        tipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Função?", "ADM", "Funcionario" }));
        tipo.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tipoActionPerformed(evt);
            }
        });

        jLabel7.setText("Telefone:");

        Cancelar.setBackground(new java.awt.Color(255, 102, 0));
        Cancelar.setText("Cancelar");
        Cancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(380, 380, 380)
                        .addComponent(cadastrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(Cancelar)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel1))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(nome, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(Senha, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cpf, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(situacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(telefone, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 127, Short.MAX_VALUE)
                        .addComponent(perfil, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(51, 51, 51))))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(nome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(Senha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(cpf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(situacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(tipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)
                            .addComponent(telefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(perfil, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cadastrar)
                    .addComponent(Cancelar))
                .addGap(78, 78, 78)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void situacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_situacaoActionPerformed
       switch (situacao.getSelectedItem().toString()) {
            case "Ativo" -> situacaoValor = "A";
            case "Inativo" -> situacaoValor = "P";
            case "Bloqueado" -> situacaoValor = "I";
        }
        System.out.println("Situação: " + situacaoValor);
    }//GEN-LAST:event_situacaoActionPerformed

    private void cadastrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cadastrarActionPerformed
        if (!validarCampos()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos obrigatórios.");
            return;
        }

      

        try (Connection con = Conexao.conexaoBanco()) {
            String sql = "INSERT INTO pessoa (nome, email, telefone, cpf, id_tipo_usuario, senha, situacao) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, nome.getText());
            stmt.setString(2, email.getText());
            stmt.setString(3, telefone.getText());
            stmt.setString(4, cpf.getText());
            stmt.setInt(5, Integer.parseInt(tipoUsuarioId));
            stmt.setString(6, Senha.getText());
            stmt.setString(7, situacaoValor);
            stmt.executeUpdate();
            stmt.close();
            JOptionPane.showMessageDialog(this, "Cadastro realizado com sucesso!");
            carregarTabela();
        } catch (SQLException ex) {
            Logger.getLogger(CadastroPessoas.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        perfil.setIcon(new ImageIcon("imagens/perfil.png"));
    }//GEN-LAST:event_cadastrarActionPerformed

     private void carregarTabela() {
        try (Connection con = Conexao.conexaoBanco()) {
            String sql = "SELECT * FROM pessoa ORDER BY id_pessoa DESC";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel modeloTabela = (DefaultTableModel) tabela.getModel();
            modeloTabela.setNumRows(0);
            while (rs.next()) {
               
                Object[] dados = {
                    rs.getInt("id_pessoa"),
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("telefone"),
                    rs.getString("cpf"),
                    rs.getInt("id_tipo_usuario"),
                    rs.getString("situacao")
                };
                modeloTabela.addRow(dados);
            }

            stmt.close();
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(CadastroPessoas.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void SenhaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SenhaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SenhaActionPerformed

    private void tipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tipoActionPerformed
         if (tipo.getSelectedItem().equals("ADM")) {
            tipoUsuarioId = "1";
        } else if (tipo.getSelectedItem().equals("Funcionario")) {
            tipoUsuarioId = "2";
        }
        System.out.println("Tipo ID: " + tipoUsuarioId);
    }//GEN-LAST:event_tipoActionPerformed

     private void limparCampos() {
        Component[] campos = getContentPane().getComponents();
        for (Component c : campos) {
            if (c instanceof JTextField jtf) {
                jtf.setText("");
            }
        }
        perfil.setIcon(new ImageIcon("imagens/perfil.png"));
    }
    
    private void CancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelarActionPerformed
        limparCampos();
        carregarTabela();
    }//GEN-LAST:event_CancelarActionPerformed

    private boolean validarCampos() {
        return !nome.getText().isBlank()
            && !email.getText().isBlank()
            && !telefone.getText().isBlank()
            && !cpf.getText().isBlank()
            && !Senha.getText().isBlank()
            && situacao.getSelectedIndex() != 0
            && tipo.getSelectedIndex() != 0;
    }
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Cancelar;
    private javax.swing.JTextField Senha;
    private javax.swing.JButton cadastrar;
    private javax.swing.JTextField cpf;
    private javax.swing.JTextField email;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField nome;
    private javax.swing.JLabel perfil;
    private javax.swing.JComboBox<String> situacao;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField telefone;
    private javax.swing.JComboBox<String> tipo;
    // End of variables declaration//GEN-END:variables
}
