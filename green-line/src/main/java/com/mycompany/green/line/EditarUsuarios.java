package com.mycompany.green.line;

import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.swing.ImageIcon;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class EditarUsuarios extends javax.swing.JInternalFrame {

    private static final Logger LOGGER = Logger.getLogger(EditarUsuarios.class.getName());
    private static final String PATH_IMAGES = "imagens/usuarios/";
    private static final String DEFAULT_PROFILE_IMAGE = "imagens/perfil.png";

    // SQL Queries
    private static final String SELECT_ALL_PERSONS = "SELECT * FROM pessoa ORDER BY id_pessoa DESC";
    private static final String SELECT_PERSON_BY_NAME = "SELECT * FROM view_pessoa_endereco WHERE nome LIKE ? LIMIT 100";
    private static final String SELECT_PERSON_BY_ID = "SELECT * FROM pessoa WHERE id_pessoa = ?";
    private static final String SELECT_ADDRESS_BY_PERSON_ID = "SELECT * FROM enderecos WHERE id_pessoa = ?";
    private static final String UPDATE_PERSON = "UPDATE pessoa SET nome = ?, email = ?, telefone = ?, cpf = ?, id_tipo_usuario = ?, situacao = ? WHERE id_pessoa = ?";
    private static final String CHECK_ADDRESS_EXISTS = "SELECT COUNT(*) FROM enderecos WHERE id_pessoa = ?";
    private static final String UPDATE_ADDRESS = "UPDATE enderecos SET uf = ?, cep = ?, cidade = ?, bairro = ?, endereco = ?, complemento = ? WHERE id_pessoa = ?";
    private static final String INSERT_ADDRESS = "INSERT INTO enderecos (uf, cep, cidade, bairro, endereco, complemento, id_pessoa) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private final Funcoes funcoes = new Funcoes();
    private final DefaultTableModel tableModel;
    private final JPopupMenu nameSuggestionsPopup = new JPopupMenu();
    private final ArrayList<String> userNames = new ArrayList<>();
    private final Font suggestionFont = new Font("Arial", Font.PLAIN, 19);

    private enum UserStatus {
        ATIVO("A", "Ativo"),
        INATIVO("P", "Inativo"),
        BLOQUEADO("I", "Bloqueado");

        private final String code;
        private final String displayName;

        UserStatus(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String getCode() {
            return code;
        }

        public static UserStatus fromDisplayName(String displayName) {
            for (UserStatus status : values()) {
                if (status.displayName.equals(displayName)) {
                    return status;
                }
            }
            return ATIVO; // Default
        }
    }

    /**
     * Enum for user type.
     */
    private enum UserType {
        ADM(1, "ADM"),
        FUNCIONARIO(2, "Funcionario");

        private final int id;
        private final String displayName;

        UserType(int id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public int getId() {
            return id;
        }

        public static UserType fromDisplayName(String displayName) {
            for (UserType type : values()) {
                if (type.displayName.equals(displayName)) {
                    return type;
                }
            }
            return FUNCIONARIO; // Default
        }
    }

    /**
     * Construtor da classe PesquisarUsuario.
     */
    String situacaoValor;
    int tipoUsuarioId;
    String tipoUsuario;

    public EditarUsuarios() {
        initComponents();
        tableModel = (DefaultTableModel) tabela.getModel();
        setupFieldMasks();
        initTable();
        disableNonEditableFields();
    }

    /**
     * Applies input masks to text fields.
     */
    private void setupFieldMasks() {
        funcoes.aplicarMascaraNome(nome);
        funcoes.aplicarMascaraInteiro(campoId);
        funcoes.aplicarMascaraTelefone(telefone);
        funcoes.aplicarMascaraCPF(cpf);
        funcoes.aplicarMascaraCEP(cep);
        ImageIcon originalIcon = new ImageIcon(TelaComImagem.class.getResource("/imagens/logo.png"));
        Image img = originalIcon.getImage();
        Image resizedImg = img.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(resizedImg);
        setFrameIcon(resizedIcon);

        setVisible(true);

    }

    /**
     * Disables fields that should not be edited.
     */
    private void disableNonEditableFields() {
        campoId.setEnabled(false);
        email.setEnabled(false);
        cpf.setEnabled(false);
    }

    private void initTable() {
        tableModel.setRowCount(0);
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_ALL_PERSONS); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id_pessoa"),
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("telefone"),
                    rs.getString("cpf"),
                    rs.getInt("id_tipo_usuario"),
                    rs.getString("situacao")
                });
                userNames.add(rs.getString("nome")); // Cache names for autocomplete
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load table data", e);
            JOptionPane.showMessageDialog(this, "Erro ao carregar tabela: " + e.getMessage());
        }
    }

    public ImageIcon redimensionamentoDeImagem(ImageIcon imagem, int largura, int altura) {
        Image pegarImagem = imagem.getImage();
        Image redimensionando = pegarImagem.getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
        return new ImageIcon(redimensionando);
    }

    private void loadUserData(int idPessoa) {
        try (Connection con = Conexao.conexaoBanco()) {
            // Load person data
            try (PreparedStatement psPessoa = con.prepareStatement(SELECT_PERSON_BY_ID)) {
                psPessoa.setInt(1, idPessoa);
                try (ResultSet rsPessoa = psPessoa.executeQuery()) {
                    if (rsPessoa.next()) {
                        nome.setText(rsPessoa.getString("nome"));
                        email.setText(rsPessoa.getString("email"));
                        telefone.setText(rsPessoa.getString("telefone"));
                        cpf.setText(rsPessoa.getString("cpf"));
                        tipo.setSelectedItem(rsPessoa.getInt("id_tipo_usuario") == 1 ? "ADM" : "Funcionario");
                        situacao.setSelectedItem(getStatusDisplayName(rsPessoa.getString("situacao")));

                        String imageUrl = rsPessoa.getString("imagem_perfil");
                        if (!imageUrl.contains("http")) {
                            perfil.setIcon(redimensionamentoDeImagem(new ImageIcon("imagens/usuarios/usuario.png"), 200, 132));
                        } else {
                            try {
                                URL url = new URL(imageUrl);
                                BufferedImage image = ImageIO.read(url);
                                if (image != null) {
                                    perfil.setIcon(redimensionamentoDeImagem(new ImageIcon(image), 200, 132));
                                }
                            } catch (IOException e) {
                                funcoes.Avisos("erro.png", "Falha ao carregar URL. Tente novamente.");
                            }
                        }
                    }
                }
            }

            // Load address data
            try (PreparedStatement psEndereco = con.prepareStatement(SELECT_ADDRESS_BY_PERSON_ID)) {
                psEndereco.setInt(1, idPessoa);
                try (ResultSet rsEndereco = psEndereco.executeQuery()) {
                    if (rsEndereco.next()) {
                        estado.setText(rsEndereco.getString("uf"));
                        cep.setText(rsEndereco.getString("cep"));
                        cidade.setText(rsEndereco.getString("cidade"));
                        bairro.setText(rsEndereco.getString("bairro"));
                        endereco.setText(rsEndereco.getString("endereco"));
                        complemento.setText(rsEndereco.getString("complemento"));
                    } else {
                        clearAddressFields();
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load user data for ID: " + idPessoa, e);
            JOptionPane.showMessageDialog(this, "Erro ao carregar usuário: " + e.getMessage());
        }
    }

    /**
     * Converts database status code to display name.
     */
    private String getStatusDisplayName(String code) {
        for (UserStatus status : UserStatus.values()) {
            if (status.getCode().equals(code)) {
                return status.displayName;
            }
        }
        return UserStatus.ATIVO.displayName;
    }

    /**
     * Clears address-related fields.
     */
    private void clearAddressFields() {
        estado.setText("");
        cep.setText("");
        cidade.setText("");
        bairro.setText("");
        endereco.setText("");
        complemento.setText("");
    }

    /**
     * Resizes an image to the specified dimensions.
     */
    private ImageIcon resizeImage(ImageIcon image, int width, int height) {
        return new ImageIcon(image.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    /**
     * Validates form fields before updating.
     */
    private boolean validateFields() {
        if (nome.getText().trim().isEmpty() || email.getText().trim().isEmpty()
                || telefone.getText().trim().isEmpty() || cpf.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos obrigatórios!");
            return false;
        }

        if (!email.getText().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            JOptionPane.showMessageDialog(this, "E-mail inválido!");
            return false;
        }

        if (estado.getText().trim().isEmpty() || cep.getText().trim().isEmpty()
                || cidade.getText().trim().isEmpty() || endereco.getText().trim().isEmpty()) {
            return JOptionPane.showConfirmDialog(this,
                    "Alguns campos de endereço estão vazios. Deseja continuar?",
                    "Aviso", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }

        return true;
    }

    /**
     * Clears all form fields.
     */
    private void clearFields() {
        nome.setText("");
        email.setText("");
        telefone.setText("");
        cpf.setText("");
        clearAddressFields();
        campoId.setText("");
        tipo.setSelectedIndex(0);
        situacao.setSelectedIndex(0);
        perfil.setIcon(new ImageIcon(DEFAULT_PROFILE_IMAGE));
    }

    @SuppressWarnings("unchecked")

    /**
     * Displays name suggestions in a popup menu based on the search text.
     */
    private void showNameSuggestions() {
        String searchText = tfPesquisar.getText().trim();
        nameSuggestionsPopup.removeAll();
        if (!searchText.isEmpty()) {
            ArrayList<String> filteredNames = new ArrayList<>();
            for (String name : userNames) {
                if (name.toLowerCase().contains(searchText.toLowerCase())) {
                    filteredNames.add(name);
                }
            }

            for (String name : filteredNames) {
                JMenuItem item = new JMenuItem(name);
                item.setFont(suggestionFont);
                item.addActionListener(e -> {
                    tfPesquisar.setText(name);
                    nameSuggestionsPopup.setVisible(false);
                    searchUserByName(name);
                });
                nameSuggestionsPopup.add(item);
            }

            if (!filteredNames.isEmpty()) {
                nameSuggestionsPopup.show(tfPesquisar, 0, tfPesquisar.getHeight());
            } else {
                nameSuggestionsPopup.setVisible(false);
            }
        } else {
            nameSuggestionsPopup.setVisible(false);
        }
    }

    /**
     * Searches for a user by name and populates the form.
     */
    private void searchUserByName(String name) {
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_PERSON_BY_NAME)) {
            stmt.setString(1, "%" + name + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    campoId.setText(rs.getString("id_pessoa"));
                    loadUserData(Integer.parseInt(rs.getString("id_pessoa")));
                    LOGGER.info("User found: " + name);
                } else {
                    JOptionPane.showMessageDialog(this, "Usuário não encontrado: " + name);
                    LOGGER.warning("No user found for name: " + name);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to search user by name: " + name, e);
            JOptionPane.showMessageDialog(this, "Erro ao pesquisar usuário: " + e.getMessage());
        }
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tipoPessoa = new javax.swing.ButtonGroup();
        tfPesquisar = new javax.swing.JTextField();
        btnProcurar = new javax.swing.JButton();
        JCard = new javax.swing.JPanel();
        painelPessoa = new javax.swing.JPanel();
        perfil = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        nome = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        estado = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        cidade = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        bairro = new javax.swing.JTextField();
        endereco = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        campoId = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        complemento = new javax.swing.JTextField();
        btAtualizar = new javax.swing.JButton();
        btCancelar1 = new javax.swing.JButton();
        situacao = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        tipo = new javax.swing.JComboBox<>();
        email = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        cpf = new javax.swing.JTextField();
        telefone = new javax.swing.JTextField();
        cep = new javax.swing.JTextField();

        setClosable(true);
        setIconifiable(true);
        setTitle("Editar Usuários");

        tfPesquisar.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        tfPesquisar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfPesquisarKeyReleased(evt);
            }
        });

        btnProcurar.setBackground(new java.awt.Color(50, 205, 50));
        btnProcurar.setFont(new java.awt.Font("Arial", 1, 17)); // NOI18N
        btnProcurar.setForeground(new java.awt.Color(255, 255, 255));
        btnProcurar.setText("Procurar");
        btnProcurar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProcurarActionPerformed(evt);
            }
        });

        JCard.setLayout(new java.awt.CardLayout());

        jLabel1.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel1.setText("Nome");

        nome.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        nome.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel2.setText("E-mail");

        jLabel3.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel3.setText("Cadastro de Pessoa Física (CPF)");

        jLabel5.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel5.setText("Telefone");

        jLabel8.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel8.setText("Estado");

        estado.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        estado.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        jLabel9.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel9.setText("CEP");

        jLabel10.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel10.setText("Cidade");

        cidade.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        cidade.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        jLabel11.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel11.setText("Bairro");

        bairro.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        bairro.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        endereco.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        endereco.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        jLabel12.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel12.setText("Endereço");

        jLabel13.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel13.setText("Código");

        campoId.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        campoId.setSelectedTextColor(new java.awt.Color(51, 51, 51));
        campoId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoIdActionPerformed(evt);
            }
        });
        campoId.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                campoIdKeyPressed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel14.setText("Complemento");

        complemento.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        complemento.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        btAtualizar.setBackground(new java.awt.Color(255, 165, 0));
        btAtualizar.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btAtualizar.setForeground(new java.awt.Color(255, 255, 255));
        btAtualizar.setText("Atualizar");
        btAtualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAtualizarActionPerformed(evt);
            }
        });

        btCancelar1.setBackground(new java.awt.Color(255, 0, 0));
        btCancelar1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btCancelar1.setForeground(new java.awt.Color(255, 255, 255));
        btCancelar1.setText("Cancelar");
        btCancelar1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btCancelar1MouseClicked(evt);
            }
        });
        btCancelar1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelar1ActionPerformed(evt);
            }
        });

        situacao.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        situacao.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Ativo", "Inativo", "Bloqueado" }));
        situacao.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        situacao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                situacaoActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel6.setText("Tipo");

        jLabel7.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel7.setText("Situação");

        tipo.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        tipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ADM", "Funcionario" }));
        tipo.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tipoActionPerformed(evt);
            }
        });

        email.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N

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
        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tabela);

        cpf.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        cpf.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        telefone.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        telefone.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        cep.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        cep.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        javax.swing.GroupLayout painelPessoaLayout = new javax.swing.GroupLayout(painelPessoa);
        painelPessoa.setLayout(painelPessoaLayout);
        painelPessoaLayout.setHorizontalGroup(
            painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPessoaLayout.createSequentialGroup()
                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(painelPessoaLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btAtualizar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btCancelar1))
                    .addGroup(painelPessoaLayout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(jScrollPane1))
                    .addGroup(painelPessoaLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(perfil, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(45, 45, 45)
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, 465, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(painelPessoaLayout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addGap(196, 196, 196))
                                    .addComponent(telefone)))
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel13)
                                    .addComponent(campoId, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(26, 26, 26)
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(nome)))
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(endereco)
                                    .addComponent(jLabel12)
                                    .addGroup(painelPessoaLayout.createSequentialGroup()
                                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel3)
                                            .addComponent(cpf, javax.swing.GroupLayout.PREFERRED_SIZE, 389, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel7)
                                            .addComponent(situacao, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(painelPessoaLayout.createSequentialGroup()
                                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel8)
                                            .addComponent(estado, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                                .addComponent(jLabel9)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                                .addGap(6, 6, 6)
                                                .addComponent(cep)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel10)
                                            .addComponent(cidade, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(bairro, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(complemento, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tipo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, painelPessoaLayout.createSequentialGroup()
                                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, painelPessoaLayout.createSequentialGroup()
                                                .addGap(25, 25, 25)
                                                .addComponent(jLabel11))
                                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING))
                                        .addGap(0, 0, Short.MAX_VALUE)))))))
                .addGap(49, 49, 49))
        );
        painelPessoaLayout.setVerticalGroup(
            painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelPessoaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPessoaLayout.createSequentialGroup()
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel8)
                                .addComponent(jLabel9))
                            .addComponent(perfil, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(estado, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cep, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPessoaLayout.createSequentialGroup()
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nome, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(campoId, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(telefone, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(1, 1, 1)
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel6))
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(situacao)
                                .addComponent(cpf, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(tipo))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bairro, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cidade, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(painelPessoaLayout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(endereco, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(painelPessoaLayout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(complemento, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btAtualizar)
                    .addComponent(btCancelar1))
                .addGap(20, 20, 20)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
        );

        JCard.add(painelPessoa, "painelPessoa");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(JCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addComponent(tfPesquisar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnProcurar)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnProcurar))
                .addGap(51, 51, 51)
                .addComponent(JCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tfPesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfPesquisarKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            searchUserByName(tfPesquisar.getText());
        } else {
            showNameSuggestions();
        }
    }//GEN-LAST:event_tfPesquisarKeyReleased
    private void btnProcurarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProcurarActionPerformed
        searchUserByName(tfPesquisar.getText());
        tfPesquisar.setText("");
    }//GEN-LAST:event_btnProcurarActionPerformed

    private void campoIdKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoIdKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                int id = Integer.parseInt(campoId.getText());
                loadUserData(id);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Código inválido!");
                LOGGER.warning("Invalid ID entered: " + campoId.getText());
            }
        }


    }//GEN-LAST:event_campoIdKeyPressed

    private void btCancelar1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btCancelar1MouseClicked
        clearFields();
    }//GEN-LAST:event_btCancelar1MouseClicked


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
    }//GEN-LAST:event_situacaoActionPerformed

    private void tipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tipoActionPerformed
        if (tipo.getSelectedItem().equals("ADM")) {
            tipoUsuario = "1";
        } else if (tipo.getSelectedItem().equals("Funcionario")) {
            tipoUsuario = "2";
        }
    }//GEN-LAST:event_tipoActionPerformed


    private void tabelaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabelaMouseClicked
        int selectedRow = tabela.getSelectedRow();
        if (selectedRow != -1) {
            int idPessoa = Integer.parseInt(tabela.getValueAt(selectedRow, 0).toString());
            campoId.setText(String.valueOf(idPessoa));
            loadUserData(idPessoa);
        }
    }//GEN-LAST:event_tabelaMouseClicked

    private void btAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAtualizarActionPerformed
        if (!validateFields()) {
            return;
        }

        try (Connection con = Conexao.conexaoBanco()) {
            con.setAutoCommit(false);
            int idPessoa = Integer.parseInt(campoId.getText());
            String cpfFormatado = funcoes.removePontuacaoEEspacos(cpf.getText().trim());
            String telefoneFormatado = funcoes.removePontuacaoEEspacos(telefone.getText().trim());
            String cepFormatado = funcoes.removePontuacaoEEspacos(cep.getText().trim());

            // Update person
            try (PreparedStatement psPessoa = con.prepareStatement(UPDATE_PERSON)) {
                psPessoa.setString(1, nome.getText());
                psPessoa.setString(2, email.getText());
                psPessoa.setString(3, telefoneFormatado);
                psPessoa.setString(4, cpfFormatado);
                psPessoa.setInt(5, UserType.fromDisplayName(tipo.getSelectedItem().toString()).getId());
                psPessoa.setString(6, UserStatus.fromDisplayName(situacao.getSelectedItem().toString()).getCode());
                psPessoa.setInt(7, idPessoa);
                int rowsPessoa = psPessoa.executeUpdate();

                // Update or insert address
                try (PreparedStatement psCheck = con.prepareStatement(CHECK_ADDRESS_EXISTS)) {
                    psCheck.setInt(1, idPessoa);
                    try (ResultSet rsCheck = psCheck.executeQuery()) {
                        rsCheck.next();
                        boolean addressExists = rsCheck.getInt(1) > 0;
                        String sqlEndereco = addressExists ? UPDATE_ADDRESS : INSERT_ADDRESS;

                        try (PreparedStatement psEndereco = con.prepareStatement(sqlEndereco)) {
                            psEndereco.setString(1, estado.getText());
                            psEndereco.setString(2, cepFormatado);
                            psEndereco.setString(3, cidade.getText());
                            psEndereco.setString(4, bairro.getText());
                            psEndereco.setString(5, endereco.getText());
                            psEndereco.setString(6, complemento.getText());
                            psEndereco.setInt(7, idPessoa);
                            int rowsEndereco = psEndereco.executeUpdate();

                            if (rowsPessoa > 0 && rowsEndereco > 0) {
                                con.commit();
                                JOptionPane.showMessageDialog(this, "Usuário atualizado com sucesso!");
                                initTable();
                                clearFields();
                            } else {
                                con.rollback();
                                JOptionPane.showMessageDialog(this, "Nenhuma alteração foi salva.");
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update user", e);
            JOptionPane.showMessageDialog(this, "Erro ao atualizar: " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID inválido!");
            LOGGER.warning("Invalid ID for update: " + campoId.getText());
        }
    }//GEN-LAST:event_btAtualizarActionPerformed

    private void btCancelar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelar1ActionPerformed

    }//GEN-LAST:event_btCancelar1ActionPerformed

    private void campoIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoIdActionPerformed

    }//GEN-LAST:event_campoIdActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel JCard;
    private javax.swing.JTextField bairro;
    private javax.swing.JButton btAtualizar;
    private javax.swing.JButton btCancelar1;
    private javax.swing.JButton btnProcurar;
    private javax.swing.JTextField campoId;
    private javax.swing.JTextField cep;
    private javax.swing.JTextField cidade;
    private javax.swing.JTextField complemento;
    private javax.swing.JTextField cpf;
    private javax.swing.JTextField email;
    private javax.swing.JTextField endereco;
    private javax.swing.JTextField estado;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField nome;
    private javax.swing.JPanel painelPessoa;
    private javax.swing.JLabel perfil;
    private javax.swing.JComboBox<String> situacao;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField telefone;
    private javax.swing.JTextField tfPesquisar;
    private javax.swing.JComboBox<String> tipo;
    private javax.swing.ButtonGroup tipoPessoa;
    // End of variables declaration//GEN-END:variables

}
