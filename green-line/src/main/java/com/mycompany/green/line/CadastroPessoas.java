package com.mycompany.green.line;

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
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author HP
 */
public class CadastroPessoas extends javax.swing.JInternalFrame {

    private String situacaoValor;
    private String tipoUsuarioId;
    Funcoes funcoes = new Funcoes();

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
        funcoes.aplicarMascaraNome(nome);
        funcoes.aplicarMascaraTelefone(telefone);
        funcoes.aplicarMascaraCPF(cpf);
        funcoes.aplicarMascaraSenha(Senha);
        funcoes.aplicarValidacaoEmail(email);

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
        jLabel6 = new javax.swing.JLabel();
        tipo = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        telefone = new javax.swing.JTextField();
        Cancelar = new javax.swing.JButton();
        Senha = new javax.swing.JPasswordField();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setIconifiable(true);

        situacao.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        situacao.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Qual a Situacão?", "Ativo", "Inativo", "Bloqueado" }));
        situacao.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        situacao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                situacaoActionPerformed(evt);
            }
        });

        cadastrar.setBackground(new java.awt.Color(50, 205, 50));
        cadastrar.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        cadastrar.setForeground(new java.awt.Color(255, 255, 255));
        cadastrar.setText("Cadastrar");
        cadastrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cadastrarActionPerformed(evt);
            }
        });

        tabela.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
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

        jLabel1.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel1.setText("Nome:");

        jLabel2.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel2.setText("Email:");

        jLabel3.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel3.setText("CPF:");

        jLabel4.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel4.setText("Situação:");

        cpf.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N

        nome.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N

        email.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel5.setText("Senha:");

        jLabel6.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel6.setText("Tipo:");

        tipo.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        tipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Função?", "ADM", "Funcionario" }));
        tipo.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tipoActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel7.setText("Telefone:");

        telefone.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N

        Cancelar.setBackground(new java.awt.Color(255, 51, 51));
        Cancelar.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        Cancelar.setForeground(new java.awt.Color(255, 255, 255));
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
                .addGap(41, 41, 41)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cadastrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(Cancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(nome, javax.swing.GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE)
                            .addComponent(email))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Senha)
                            .addComponent(cpf)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(situacao, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addComponent(tipo, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addComponent(telefone, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)))
                .addGap(30, 30, 30))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(nome, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(Senha, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(cpf, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(telefone, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 2, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(situacao, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(tipo)))
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cadastrar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Cancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(56, 56, 56)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void situacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_situacaoActionPerformed
        switch (situacao.getSelectedItem().toString()) {
            case "Ativo":
                situacaoValor = "A";
                break;
            case "Inativo":
                situacaoValor = "P";
                break;
            case "Bloqueado":
                situacaoValor = "I";
                break;
            default:
                situacaoValor = ""; // ou algum valor padrão
                break;
        }
        System.out.println("Situação: " + situacaoValor);
    }//GEN-LAST:event_situacaoActionPerformed

    private void cadastrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cadastrarActionPerformed
        if (!validarCampos()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos");
            return;
        }

        try (Connection con = Conexao.conexaoBanco()) {
            // Verificar duplicações
            String sqlCheck = "SELECT COUNT(*) FROM pessoa WHERE cpf = ? OR email = ?";
            PreparedStatement stmtCheck = con.prepareStatement(sqlCheck);
            String cpfFormatado = funcoes.removePontuacaoEEspacos(cpf.getText().trim());
            String telefoneFormatado = funcoes.removePontuacaoEEspacos(telefone.getText().trim());
            stmtCheck.setString(1, cpfFormatado);
            stmtCheck.setString(2, email.getText().trim());
            ResultSet rs = stmtCheck.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            stmtCheck.close();
            rs.close();

            if (count > 0) {
                funcoes.Avisos("sinal-de-aviso.png", "Dados idênticos: Email ou CPF vão ser duplicados.Por favor, insira dados corretos");
                return;
            }
            /*CRIPTOGRAFANDO*/
            String senhaHash = "";
            try {
                senhaHash = BCrypt.hashpw(Senha.getText(), BCrypt.gensalt());
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
                senhaHash = "12345";
            }

            // Prosseguir com o cadastro se não houver duplicatas
            String sql = "INSERT INTO pessoa (nome, email, telefone, cpf, id_tipo_usuario, senha, situacao,imagem_perfil) VALUES (?, ?, ?, ?, ?, ?, ?, 'perfil.png')";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, nome.getText());
            stmt.setString(2, email.getText());
            stmt.setString(3, telefoneFormatado);
            stmt.setString(4, cpfFormatado);
            stmt.setInt(5, Integer.parseInt(tipoUsuarioId));
            stmt.setString(6, senhaHash);
            stmt.setString(7, situacaoValor);
            stmt.executeUpdate();
            stmt.close();

            // Obter o ID da pessoa recém-inserida
            String sqlLastId = "SELECT LAST_INSERT_ID()";
            PreparedStatement stmtLastId = con.prepareStatement(sqlLastId);
            ResultSet rsLastId = stmtLastId.executeQuery();
            rsLastId.next();
            int idPessoa = rsLastId.getInt(1);
            stmtLastId.close();
            rsLastId.close();

            // Inserir endereço com valores NULL
            String sqlEndereco = "INSERT INTO enderecos (uf, cep, cidade, bairro, endereco, complemento, id_pessoa) VALUES (NULL, NULL, NULL, NULL, NULL, NULL, ?)";
            PreparedStatement stmtEndereco = con.prepareStatement(sqlEndereco);
            stmtEndereco.setInt(1, idPessoa);
            stmtEndereco.executeUpdate();
            stmtEndereco.close();

            JOptionPane.showMessageDialog(this, "Cadastro realizado com sucesso!");
            carregarTabela();
            limparCampos();

        } catch (SQLException ex) {
            Logger.getLogger(CadastroPessoas.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Erro ao realizar cadastro: " + ex.getMessage());
        }

        //perfil.setIcon(new ImageIcon("imagens/perfil.png"));
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

    private void tipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tipoActionPerformed
        if (tipo.getSelectedItem().equals("ADM")) {
            tipoUsuarioId = "1";
        } else if (tipo.getSelectedItem().equals("Funcionario")) {
            tipoUsuarioId = "2";
        }
        System.out.println("Tipo ID: " + tipoUsuarioId);
    }//GEN-LAST:event_tipoActionPerformed

    private void limparCampos() {
        nome.setText("");
        Senha.setText("");
        email.setText("");
        cpf.setText("");
        telefone.setText("");
        //perfil.setIcon(new ImageIcon("imagens/perfil.png"));
    }

    private void CancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelarActionPerformed
        limparCampos();
        carregarTabela();
    }//GEN-LAST:event_CancelarActionPerformed

    private boolean validarCampos() {
        return !nome.getText().trim().isEmpty()
                && !email.getText().trim().isEmpty()
                && !telefone.getText().trim().isEmpty()
                && !cpf.getText().trim().isEmpty()
                && !Senha.getText().trim().isEmpty()
                && situacao.getSelectedIndex() != 0
                && tipo.getSelectedIndex() != 0;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Cancelar;
    private javax.swing.JPasswordField Senha;
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
    private javax.swing.JComboBox<String> situacao;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField telefone;
    private javax.swing.JComboBox<String> tipo;
    // End of variables declaration//GEN-END:variables
}
