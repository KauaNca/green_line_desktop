package com.mycompany.green.line;


import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

/**
 * JInternalFrame para pesquisa de usuários (pessoas físicas ou jurídicas) no
 * sistema. Permite buscar dados de usuários pelo nome, ID ou CPF e exibir
 * informações detalhadas.
 *
 * @author guilherme53961526
 */
public class PesquisarUsuario extends javax.swing.JInternalFrame {

    private static final Logger LOGGER = Logger.getLogger(PesquisarUsuario.class.getName());
    private static final String SELECT_PERSON_DATA = "SELECT * FROM view_pessoa_endereco WHERE nome = ?";
    private static final String SELECT_PERSON_BY_ID = "SELECT * FROM view_pessoa_endereco WHERE id_pessoa = ?";
    private static final String SELECT_PERSON_BY_CPF = "SELECT * FROM view_pessoa_endereco WHERE cpf = ?";
    private static final String SELECT_USER_NAMES = "SELECT nome FROM view_pessoa_endereco WHERE LOWER(nome) LIKE ?";
    private static final String ERROR_DB_CONNECTION = "Erro ao conectar ao banco de dados: ";
    private static final String ERROR_GENERIC = "Erro inesperado: ";

    private CardLayout card;
    private final JPopupMenu sugestoesNomes = new JPopupMenu();
    private List<String> usuarios;
    private final Font fonteItem = new Font("Arial", Font.PLAIN, 15);
    private final Funcoes funcoes = new Funcoes();

    public PesquisarUsuario() {
        initComponents();
        perfil.setIcon(new ImageIcon("imagens/perfil.png"));
        LOGGER.info("Inicializando PesquisarUsuario");
        setupFieldMasks();
        usuarios = new ArrayList<>();
        desativarTextField(painelPessoa);
        nomesUsuarios();
        ImageIcon originalIcon = new ImageIcon(TelaComImagem.class.getResource("/imagens/logo.png"));
Image img = originalIcon.getImage();
Image resizedImg = img.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
ImageIcon resizedIcon = new ImageIcon(resizedImg);
setFrameIcon(resizedIcon);

setVisible(true);

    }

    public void desativarTextField(JPanel painel) {
        LOGGER.info("Desativando campos de texto no painel: " + painel.getName());
        for (Component component : painel.getComponents()) {
            if (component instanceof JTextField) {
                component.setEnabled(false);
            }
        }
        codigoUsuario.setEnabled(true);
        cpf.setEnabled(true);
    }
    private void setupFieldMasks() {
        funcoes.aplicarMascaraNome(nome);
        funcoes.aplicarMascaraInteiro(codigoUsuario);
        funcoes.aplicarMascaraTelefone(telefone);
        funcoes.aplicarMascaraCPF(cpf);
        funcoes.aplicarMascaraCEP(cep);
    }
    public ImageIcon redimensionamentoDeImagem(ImageIcon imagem, int largura, int altura) {
        LOGGER.info("Redimensionando imagem para " + largura + "x" + altura);
        try {
            Image pegarImagem = imagem.getImage();
            Image redimensionando = pegarImagem.getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
            return new ImageIcon(redimensionando);
        } catch (Exception e) {
            LOGGER.warning("Erro ao redimensionar imagem: " + e.getMessage());
            return null;
        }
    }

    public void nomesUsuarios() {
        LOGGER.info("Carregando nomes com filtro: " + tfPesquisar.getText());
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_USER_NAMES)) {
            stmt.setString(1, "%" + tfPesquisar.getText().toLowerCase().trim() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                usuarios = new ArrayList<>();
                while (rs.next()) {
                    String nome = rs.getString("nome");
                    if (nome != null) {
                        usuarios.add(nome);
                    }
                }
                LOGGER.info("Nomes carregados: " + usuarios.size());
            }
        } catch (SQLException ex) {
            LOGGER.severe(ERROR_DB_CONNECTION + ex.getMessage());
            JOptionPane.showMessageDialog(null, ERROR_DB_CONNECTION + ex.getMessage());
        }
    }

    public void pesquisarNome() {
        String pesquisa = tfPesquisar.getText().toLowerCase().trim();
        LOGGER.info("Filtrando nomes com texto: " + pesquisa);
        sugestoesNomes.setVisible(false);

        if (!pesquisa.isEmpty()) {
            List<String> filtro = usuarios.stream()
                    .filter(nome -> nome != null && nome.toLowerCase().contains(pesquisa))
                    .collect(Collectors.toList());

            if (!filtro.isEmpty()) {
                sugestoesNomes.removeAll();
                for (String p : filtro) {
                    JMenuItem item = new JMenuItem(p);
                    item.setFont(fonteItem);
                    item.addActionListener(e -> {
                        tfPesquisar.setText(p);
                        sugestoesNomes.setVisible(false);
                        LOGGER.info("Nome selecionado: " + p);
                        btnProcurarActionPerformed(null);
                    });
                    sugestoesNomes.add(item);
                }
                sugestoesNomes.show(tfPesquisar, 0, tfPesquisar.getHeight());
            }
        }
    }

    private void preencherCampos(ResultSet rs) throws SQLException {
        int rowCount = 0;
        if (rs.next()) {
            rowCount++;
            codigoUsuario.setText(rs.getString("id_pessoa") != null ? rs.getString("id_pessoa") : "");
            nome.setText(rs.getString("nome") != null ? rs.getString("nome") : "");
            email.setText(rs.getString("email") != null ? rs.getString("email") : "");
            telefone.setText(rs.getString("telefone") != null ? rs.getString("telefone") : "");
            cpf.setText(rs.getString("cpf") != null ? rs.getString("cpf") : "");
            estado.setText(rs.getString("uf") != null ? rs.getString("uf") : "");
            cep.setText(rs.getString("cep") != null ? rs.getString("cep") : "");
            cidade.setText(rs.getString("cidade") != null ? rs.getString("cidade") : "");
            bairro.setText(rs.getString("bairro") != null ? rs.getString("bairro") : "");
            endereco.setText(rs.getString("endereco") != null ? rs.getString("endereco") : "");
            complemento.setText(rs.getString("complemento") != null ? rs.getString("complemento") : "");

            String imagemPath = rs.getString("imagem_perfil");
            if (imagemPath != null && !imagemPath.isEmpty()) {
                File imageFile = new File("imagens/usuarios/" + imagemPath);
                if (imageFile.exists()) {
                    ImageIcon foto = new ImageIcon(imageFile.getPath());
                    perfil.setIcon(redimensionamentoDeImagem(foto, 205, 233));
                } else {
                    LOGGER.warning("Imagem não encontrada: " + imageFile.getPath());
                    perfil.setIcon(new ImageIcon("imagens/perfil.png"));
                }
            } else {
                perfil.setIcon(new ImageIcon("imagens/perfil.png"));
            }

            LOGGER.info("Dados da pessoa carregados com sucesso.");
        }

        if (rs.next()) {
            JOptionPane.showMessageDialog(null, "<html><h3>Múltiplos registros encontrados. Especifique um CPF ou ID.</h3></html>");
        } else if (rowCount == 0) {
            JOptionPane.showMessageDialog(null, "<html><h3>Nenhum registro encontrado.</h3></html>");
        }
    }

    private void pesquisarPessoa() {
        LOGGER.info("Pesquisando pessoa com nome: " + tfPesquisar.getText());
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_PERSON_DATA)) {
            stmt.setString(1, tfPesquisar.getText().trim());
            try (ResultSet rs = stmt.executeQuery()) {
                preencherCampos(rs);
            }
        } catch (SQLException e) {
            LOGGER.severe(ERROR_DB_CONNECTION + e.getMessage());
            JOptionPane.showMessageDialog(null, "<html><h3>" + ERROR_DB_CONNECTION + e.getMessage() + "</h3></html>");
        } catch (Exception e) {
            LOGGER.severe(ERROR_GENERIC + e.getMessage());
            JOptionPane.showMessageDialog(null, "<html><h3>" + ERROR_GENERIC + e.getMessage() + "</h3></html>");
        }
    }

    @SuppressWarnings("unchecked")
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
        email = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        telefone = new javax.swing.JTextField();
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
        codigoUsuario = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        complemento = new javax.swing.JTextField();
        btComprasUsuario = new javax.swing.JButton();
        cpf = new javax.swing.JFormattedTextField();
        cep = new javax.swing.JFormattedTextField();
        btCancelar1 = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setTitle("Pesquisar Usuários");

        tfPesquisar.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        tfPesquisar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfPesquisarKeyReleased(evt);
            }
        });

        btnProcurar.setBackground(new java.awt.Color(50, 205, 50));
        btnProcurar.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
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

        email.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        email.setEnabled(false);
        email.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel3.setText("Cadastro de Pessoa Física (CPF)");

        telefone.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        telefone.setEnabled(false);
        telefone.setSelectedTextColor(new java.awt.Color(51, 51, 51));

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

        codigoUsuario.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        codigoUsuario.setSelectedTextColor(new java.awt.Color(51, 51, 51));
        codigoUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codigoUsuarioActionPerformed(evt);
            }
        });
        codigoUsuario.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                codigoUsuarioKeyPressed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel14.setText("Complemento");

        complemento.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        complemento.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        btComprasUsuario.setBackground(new java.awt.Color(255, 165, 0));
        btComprasUsuario.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btComprasUsuario.setForeground(new java.awt.Color(255, 255, 255));
        btComprasUsuario.setText("Compras");
        btComprasUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btComprasUsuarioActionPerformed(evt);
            }
        });

        cpf.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
        cpf.setEnabled(false);
        cpf.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        cpf.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                cpfKeyReleased(evt);
            }
        });

        try {
            cep.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("#####-###")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        cep.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N

        btCancelar1.setBackground(new java.awt.Color(255, 0, 0));
        btCancelar1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btCancelar1.setForeground(new java.awt.Color(255, 255, 255));
        btCancelar1.setText("Cancelar");
        btCancelar1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btCancelar1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout painelPessoaLayout = new javax.swing.GroupLayout(painelPessoa);
        painelPessoa.setLayout(painelPessoaLayout);
        painelPessoaLayout.setHorizontalGroup(
            painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelPessoaLayout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(painelPessoaLayout.createSequentialGroup()
                        .addComponent(btComprasUsuario)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btCancelar1))
                    .addGroup(painelPessoaLayout.createSequentialGroup()
                        .addComponent(perfil, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(endereco, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, painelPessoaLayout.createSequentialGroup()
                                            .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel8)
                                                .addComponent(estado, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(painelPessoaLayout.createSequentialGroup()
                                                    .addComponent(jLabel9)
                                                    .addGap(195, 195, 195))
                                                .addGroup(painelPessoaLayout.createSequentialGroup()
                                                    .addComponent(cep)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                            .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel10)
                                                .addComponent(cidade, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addComponent(jLabel12))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(bairro)
                                    .addComponent(complemento)
                                    .addGroup(painelPessoaLayout.createSequentialGroup()
                                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel11)
                                            .addComponent(jLabel14))
                                        .addGap(0, 169, Short.MAX_VALUE))))
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, 438, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(painelPessoaLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 222, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPessoaLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(telefone, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(jLabel3)
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel13)
                                    .addComponent(codigoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(26, 26, 26)
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(nome)))
                            .addComponent(cpf))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        painelPessoaLayout.setVerticalGroup(
            painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelPessoaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPessoaLayout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(estado, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPessoaLayout.createSequentialGroup()
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(painelPessoaLayout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(nome, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(painelPessoaLayout.createSequentialGroup()
                                        .addComponent(jLabel13)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(codigoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(35, 35, 35))
                            .addComponent(telefone, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cpf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel10))
                                .addGap(35, 35, 35))
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(bairro, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cidade, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cep)))))
                    .addComponent(perfil, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addGap(18, 18, 18)
                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btComprasUsuario)
                    .addComponent(btCancelar1))
                .addContainerGap(45, Short.MAX_VALUE))
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
                .addGap(32, 32, 32))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnProcurar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(51, 51, 51)
                .addComponent(JCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Ação executada ao liberar uma tecla no campo de pesquisa. Realiza a
     * filtragem de nomes com base no texto digitado.
     *
     * @param evt Evento de tecla liberada.
     */

    private void tfPesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfPesquisarKeyReleased
        LOGGER.info("Filtrando nomes com base no texto digitado.");
        nomesUsuarios(); // Atualiza a lista de nomes com base no texto digitado
        pesquisarNome();
    }//GEN-LAST:event_tfPesquisarKeyReleased
    /**
     * Ação executada ao clicar no botão "Procurar". Desativa campos específicos
     * e realiza a pesquisa de pessoa física ou jurídica com base na seleção.
     *
     * @param evt Evento de ação do botão.
     */

    private void btnProcurarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProcurarActionPerformed
        LOGGER.info("Iniciando pesquisa de usuário.");
        codigoUsuario.setEnabled(false);
        email.setEnabled(false);
        cpf.setEnabled(false);
        pesquisarPessoa();
        tfPesquisar.setText("");
    }//GEN-LAST:event_btnProcurarActionPerformed
    /**
     * Ação executada ao pressionar a tecla Enter no campo de código de usuário
     * (pessoa física). Pesquisa os dados da pessoa física pelo código de
     * usuário e preenche os campos.
     *
     * @param evt Evento de tecla pressionada.
     */

    private void codigoUsuarioKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_codigoUsuarioKeyPressed
        if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
            LOGGER.info("Pesquisando pessoa com ID: " + codigoUsuario.getText());
            try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_PERSON_BY_ID)) {
                stmt.setString(1, codigoUsuario.getText().trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    preencherCampos(rs);
                }
            } catch (SQLException e) {
                LOGGER.severe(ERROR_DB_CONNECTION + e.getMessage());
                JOptionPane.showMessageDialog(null, "<html><h3>" + ERROR_DB_CONNECTION + e.getMessage() + "</h3></html>");
            } catch (Exception e) {
                LOGGER.severe(ERROR_GENERIC + e.getMessage());
                JOptionPane.showMessageDialog(null, "<html><h3>" + ERROR_GENERIC + e.getMessage() + "</h3></html>");
            }
        }

    }//GEN-LAST:event_codigoUsuarioKeyPressed

    private void btCancelar1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btCancelar1MouseClicked
        LOGGER.info("Cancelando e limpando campos");
        perfil.setIcon(new ImageIcon("imagens/perfil.png"));
        for (Component component : painelPessoa.getComponents()) {
            if (component instanceof JTextField) {
                ((JTextField) component).setText("");
            }
        }
        desativarTextField(painelPessoa);
        usuarios.clear();
        nomesUsuarios();
    }//GEN-LAST:event_btCancelar1MouseClicked
    /**
     * Ação executada ao pressionar a tecla Enter no campo de CPF. Pesquisa os
     * dados da pessoa física pelo CPF e preenche os campos.
     *
     * @param evt Evento de tecla liberada.
     */

    private void cpfKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cpfKeyReleased
        if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
            LOGGER.info("Pesquisando pessoa com CPF: " + cpf.getText());
            try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_PERSON_BY_CPF)) {
                stmt.setString(1, cpf.getText());
                try (ResultSet rs = stmt.executeQuery()) {
                    preencherCampos(rs);
                }
            } catch (SQLException e) {
                LOGGER.severe(ERROR_DB_CONNECTION + e.getMessage());
                JOptionPane.showMessageDialog(null, "<html><h3>" + ERROR_DB_CONNECTION + e.getMessage() + "</h3></html>");
            } catch (Exception e) {
                LOGGER.severe(ERROR_GENERIC + e.getMessage());
                JOptionPane.showMessageDialog(null, "<html><h3>" + ERROR_GENERIC + e.getMessage() + "</h3></html>");
            }
        }
    }//GEN-LAST:event_cpfKeyReleased

    private void codigoUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codigoUsuarioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_codigoUsuarioActionPerformed

    private void btComprasUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btComprasUsuarioActionPerformed
        LOGGER.info("Botão 'Compras' clicado. Aguardando implementação.");
        funcoes.Avisos("sinal-de-aviso.png", "Lógica não implementada ainda");
    }//GEN-LAST:event_btComprasUsuarioActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel JCard;
    private javax.swing.JTextField bairro;
    private javax.swing.JButton btCancelar1;
    private javax.swing.JButton btComprasUsuario;
    private javax.swing.JButton btnProcurar;
    private javax.swing.JFormattedTextField cep;
    private javax.swing.JTextField cidade;
    private javax.swing.JTextField codigoUsuario;
    private javax.swing.JTextField complemento;
    private javax.swing.JFormattedTextField cpf;
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
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField nome;
    private javax.swing.JPanel painelPessoa;
    private javax.swing.JLabel perfil;
    private javax.swing.JTextField telefone;
    private javax.swing.JTextField tfPesquisar;
    private javax.swing.ButtonGroup tipoPessoa;
    // End of variables declaration//GEN-END:variables
}
