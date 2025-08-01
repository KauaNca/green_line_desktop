package com.mycompany.green.line;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.mindrot.jbcrypt.BCrypt;

/**
 * JFrame para a tela de login do sistema. Configura a interface gráfica para
 * autenticação de usuários, incluindo campos para código, usuário e senha.
 */
public class Login extends javax.swing.JFrame {

    private static final Logger LOGGER = Logger.getLogger(Login.class.getName());
    private static final String USER_IMAGE_PATH = "imagens/usuarios/usuario.png";
    private static final Color GREEN_COLOR = new Color(29, 68, 53);
    private static final EmptyBorder FIELD_BORDER = new EmptyBorder(5, 5, 0, 0);
    private static final String INSERT_ACCESS = "INSERT INTO acessos(id_pessoa, usuario, local) VALUES (?, ?, ?)";
    private static final String SELECT_USER_LOGIN = "SELECT id_pessoa, nome, id_tipo_usuario, senha FROM pessoa WHERE nome = ?";
    private static final String UPDATE_PASSWORD = "UPDATE pessoa SET senha = ? WHERE id_pessoa = ? AND cpf = ?";
    private static final String SELECT_USER_BY_ID = "SELECT nome, imagem_perfil,id_tipo_usuario FROM pessoa WHERE id_pessoa = ?";
    private static final String ERROR_GENERIC = "Erro: ";

    private final Funcoes funcoes = new Funcoes();
    private String codigo;
    private final Color corVerde = GREEN_COLOR;
    private final EmptyBorder margensInternas = FIELD_BORDER;
    private final JLabel imagem2 = new JLabel();

    public String getCodigo() {
        return codigo;
    }

    /**
     * Construtor da classe Login. Inicializa os componentes da interface
     * gráfica.
     */
    public Login() {
        initComponents();
        LOGGER.info("Inicializando tela de login.");
        configureFrame();
        initializeUI();
        setIconImage(new ImageIcon(TelaComImagem.class.getResource("/imagens/logo.png"))
                .getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
    }

    /**
     * Configura as propriedades do JFrame.
     */
    private void configureFrame() {
        LOGGER.info("Configurando propriedades do JFrame.");
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }

    /**
     * Inicializa os componentes da interface gráfica.
     */
    private void initializeUI() {
        usuario.setBorder(margensInternas);
        codigoCampo.setBorder(margensInternas);
        senha.setBorder(margensInternas);
        painelPrincipal.setBackground(corVerde);
        usuario.setEnabled(false);
        usuario.setEditable(false);
        imagem.setIcon(new ImageIcon(USER_IMAGE_PATH));
        codigoCampo.setFocusTraversalKeysEnabled(false);
        funcoes.aplicarMascaraInteiro(codigoUsuario);
        funcoes.aplicarMascaraSenha(senha);
        funcoes.aplicarMascaraInteiro(codigoCampo);
        funcoes.aplicarMascaraCPF(cpfSenhaEsquecida);
    }

    /**
     * Limpa os campos de entrada e redefine a imagem padrão.
     */
    private void limpar() {
        codigoCampo.setText("");
        usuario.setText("");
        senha.setText("");
        imagem.setIcon(new ImageIcon(USER_IMAGE_PATH));
        codigoCampo.setEnabled(true);
    }

    /**
     * Registra o acesso do usuário no banco de dados.
     */
    private void registrarAcesso() {
        LOGGER.info("Registrando acesso do usuário: " + codigo);
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(INSERT_ACCESS)) {
            stmt.setString(1, codigo);
            stmt.setString(2, usuario.getText());
            stmt.setString(3, "Desktop");
            stmt.executeUpdate();
            LOGGER.info("Acesso registrado com sucesso.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao registrar acesso: {0}", ex.getMessage());
            JOptionPane.showMessageDialog(null, ERROR_GENERIC + ex.getMessage());
        }
    }

    /**
     * Realiza o processo de login, validando código, usuário e senha.
     */
    private void login() {
        if (codigoCampo.getText().trim().isEmpty()
                || usuario.getText().trim().isEmpty()
                || senha.getText().trim().isEmpty()) {
            LOGGER.warning("Campos de login estão vazios.");
            funcoes.Avisos("erro.png", "Preencha todos os campos");
            return;
        }

        LOGGER.info("Iniciando processo de login para usuário: " + usuario.getText());
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_USER_LOGIN)) {
            stmt.setString(1, usuario.getText());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String senhaHash = rs.getString("senha");
                    if (!BCrypt.checkpw(senha.getText(), senhaHash)) {
                        LOGGER.warning("Senha incorreta para: " + usuario.getText());
                        JOptionPane.showMessageDialog(null, "Credenciais inválidas");
                        return;
                    }
                    codigo = rs.getString("id_pessoa");
                    String tipo_usuario = rs.getString("id_tipo_usuario");
                    if ("1".equals(tipo_usuario)) {
                        registrarAcesso();
                        new TelaInicial(codigo, tipo_usuario).setVisible(true);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, "Acesso não autorizado");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Usuário não encontrado");
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro SQL: {0}", ex.getMessage());
            JOptionPane.showMessageDialog(null, "Erro no banco de dados");
        }
    }

    /**
     * Busca usuário pelo código e atualiza a interface com nome e imagem.
     */
    private void buscarUsuario() {
        String codigoInput = codigoCampo.getText().trim();
        if (codigoInput.isEmpty()) {
            LOGGER.warning("Campo de código está vazio.");
            funcoes.Avisos("erro.png", "Preencha o campo de código");
            return;
        }
        if (!codigoInput.matches("\\d+")) {
            LOGGER.warning("Código contém caracteres inválidos.");
            funcoes.Avisos("erro.png", "O código deve conter apenas números");
            return;
        }

        LOGGER.info("Buscando usuário pelo código: " + codigoCampo.getText());
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_USER_BY_ID)) {
            stmt.setString(1, codigoCampo.getText());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    usuario.setText(rs.getString("nome"));
                    String imageUrl = rs.getString("imagem_perfil");
                    String tipo_usuario = rs.getString("id_tipo_usuario");
                    if(tipo_usuario.equals("2")){
                        funcoes.Avisos("sinal-de-aviso.png","Acesso não permitido. Encerrando...");      
                        dispose();
                        return;
                    }
                    ImageIcon icon;
                    if (imageUrl == null || imageUrl.trim().isEmpty() || !imageUrl.contains("http")) {
                        icon = new ImageIcon(USER_IMAGE_PATH);
                    } else {
                        try {
                            BufferedImage image = ImageIO.read(new URL(imageUrl));
                            if (image == null) {
                                throw new IOException("Imagem retornada é nula");
                            }
                            icon = new ImageIcon(image);
                        } catch (IOException e) {
                            LOGGER.log(Level.WARNING, "Falha ao carregar imagem: {0}", e.getMessage());
                            icon = new ImageIcon(USER_IMAGE_PATH);
                            funcoes.Avisos("erro.png", "Falha ao carregar imagem. Usando padrão.");
                        }
                    }
                    imagem.setIcon(redimensionamentoDeImagem(icon, 200, 152));
                    painelPrincipal.revalidate();
                    painelPrincipal.repaint();
                    LOGGER.info("Usuário encontrado: " + usuario.getText());
                    codigoCampo.setEnabled(false);
                } else {
                    LOGGER.warning("Usuário não encontrado para o código: " + codigoCampo.getText());
                    funcoes.Avisos("sinal-de-aviso.png",
                            "Usuário não encontrado. Faça um cadastro ou contate os administradores.");
                    return;
                }
                
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar usuário: {0}", e.getMessage());
            funcoes.Avisos("erro.png", "Houve um erro. Tente novamente mais tarde");
        }
    }

    /**
     * Redimensiona uma imagem para as dimensões especificadas.
     */
    private ImageIcon redimensionamentoDeImagem(ImageIcon imagem, int largura, int altura) {
        LOGGER.info("Redimensionando imagem para " + largura + "x" + altura);
        Image redimensionada = imagem.getImage().getScaledInstance(largura, altura, Image.SCALE_DEFAULT);
        return new ImageIcon(redimensionada);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        janelaEsqueceuSenha = new javax.swing.JDialog();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        btRedefinirSenha = new javax.swing.JButton();
        cpfSenhaEsquecida = new javax.swing.JTextField();
        codigoUsuario = new javax.swing.JTextField();
        painelPrincipal = new javax.swing.JPanel();
        textoSenha = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        btLogin = new javax.swing.JButton();
        btSair = new javax.swing.JButton();
        titulo = new javax.swing.JLabel();
        textoCodigo = new javax.swing.JLabel();
        codigoCampo = new javax.swing.JTextField();
        apagarSenha = new javax.swing.JLabel();
        imagem = new javax.swing.JLabel();
        usuario = new javax.swing.JTextField();
        esqueceuSenha = new javax.swing.JLabel();
        senha = new javax.swing.JPasswordField();

        janelaEsqueceuSenha.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        janelaEsqueceuSenha.setResizable(false);

        jLabel2.setFont(new java.awt.Font("Inter", 1, 15)); // NOI18N
        jLabel2.setText("Digite seu código de usuário e CPF,");

        jLabel3.setFont(new java.awt.Font("Inter", 1, 15)); // NOI18N
        jLabel3.setText("respectivamente,nos campo abaixos");

        jLabel4.setFont(new java.awt.Font("Inter", 1, 15)); // NOI18N
        jLabel4.setText("Recomenda-se que após o processo, ");

        jLabel5.setFont(new java.awt.Font("Inter", 1, 15)); // NOI18N
        jLabel5.setText("o usuário altere para sua própria segurança. ");

        jLabel6.setFont(new java.awt.Font("Inter", 1, 15)); // NOI18N
        jLabel6.setText("A sua senha será redefinida para: 123GL.");

        btRedefinirSenha.setBackground(new java.awt.Color(50, 205, 50));
        btRedefinirSenha.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        btRedefinirSenha.setForeground(new java.awt.Color(255, 255, 255));
        btRedefinirSenha.setText("Redefinir senha");
        btRedefinirSenha.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btRedefinirSenhaMouseClicked(evt);
            }
        });
        btRedefinirSenha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRedefinirSenhaActionPerformed(evt);
            }
        });

        cpfSenhaEsquecida.setFont(new java.awt.Font("Inter SemiBold", 0, 15)); // NOI18N
        cpfSenhaEsquecida.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        codigoUsuario.setFont(new java.awt.Font("Inter SemiBold", 0, 15)); // NOI18N
        codigoUsuario.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        javax.swing.GroupLayout janelaEsqueceuSenhaLayout = new javax.swing.GroupLayout(janelaEsqueceuSenha.getContentPane());
        janelaEsqueceuSenha.getContentPane().setLayout(janelaEsqueceuSenhaLayout);
        janelaEsqueceuSenhaLayout.setHorizontalGroup(
            janelaEsqueceuSenhaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaEsqueceuSenhaLayout.createSequentialGroup()
                .addContainerGap(45, Short.MAX_VALUE)
                .addGroup(janelaEsqueceuSenhaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, janelaEsqueceuSenhaLayout.createSequentialGroup()
                        .addComponent(btRedefinirSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(122, 122, 122))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, janelaEsqueceuSenhaLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(67, 67, 67))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, janelaEsqueceuSenhaLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(59, 59, 59))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, janelaEsqueceuSenhaLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(28, 28, 28))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, janelaEsqueceuSenhaLayout.createSequentialGroup()
                        .addGroup(janelaEsqueceuSenhaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(cpfSenhaEsquecida)
                            .addComponent(codigoUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE))
                        .addGap(100, 100, 100))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, janelaEsqueceuSenhaLayout.createSequentialGroup()
                        .addGroup(janelaEsqueceuSenhaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(janelaEsqueceuSenhaLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel4))
                            .addComponent(jLabel6))
                        .addGap(46, 46, 46))))
        );
        janelaEsqueceuSenhaLayout.setVerticalGroup(
            janelaEsqueceuSenhaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaEsqueceuSenhaLayout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addGap(2, 2, 2)
                .addComponent(codigoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cpfSenhaEsquecida, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btRedefinirSenha)
                .addContainerGap(70, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 242, 207));
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        painelPrincipal.setBackground(new java.awt.Color(0, 51, 0));
        painelPrincipal.setForeground(new java.awt.Color(255, 255, 255));

        textoSenha.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        textoSenha.setForeground(new java.awt.Color(255, 255, 255));
        textoSenha.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        textoSenha.setText("Senha");

        jLabel1.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Usuário");

        btLogin.setBackground(new java.awt.Color(50, 205, 50));
        btLogin.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        btLogin.setForeground(new java.awt.Color(255, 255, 255));
        btLogin.setText("Entrar");
        btLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btLoginMouseClicked(evt);
            }
        });

        btSair.setBackground(new java.awt.Color(169, 169, 169));
        btSair.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        btSair.setText("Sair");
        btSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSairActionPerformed(evt);
            }
        });

        titulo.setFont(new java.awt.Font("Berlin Sans FB Demi", 0, 40)); // NOI18N
        titulo.setForeground(new java.awt.Color(255, 255, 255));
        titulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titulo.setText("Green Line");
        titulo.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        titulo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        textoCodigo.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        textoCodigo.setForeground(new java.awt.Color(245, 245, 245));
        textoCodigo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        textoCodigo.setText("Código");

        codigoCampo.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        codigoCampo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        codigoCampo.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        codigoCampo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                codigoCampoKeyPressed(evt);
            }
        });

        apagarSenha.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        apagarSenha.setForeground(new java.awt.Color(255, 255, 255));
        apagarSenha.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        apagarSenha.setText("X");
        apagarSenha.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                apagarSenhaMouseClicked(evt);
            }
        });

        imagem.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        usuario.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        usuario.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        usuario.setDisabledTextColor(new java.awt.Color(0, 0, 0));

        esqueceuSenha.setFont(new java.awt.Font("Inter SemiBold", 0, 14)); // NOI18N
        esqueceuSenha.setForeground(new java.awt.Color(255, 255, 255));
        esqueceuSenha.setText("Esqueceu sua senha?");
        esqueceuSenha.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                esqueceuSenhaMouseClicked(evt);
            }
        });

        senha.setFont(new java.awt.Font("Inter", 0, 18)); // NOI18N
        senha.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                senhaFocusGained(evt);
            }
        });
        senha.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                senhaKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout painelPrincipalLayout = new javax.swing.GroupLayout(painelPrincipal);
        painelPrincipal.setLayout(painelPrincipalLayout);
        painelPrincipalLayout.setHorizontalGroup(
            painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelPrincipalLayout.createSequentialGroup()
                .addContainerGap(148, Short.MAX_VALUE)
                .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(imagem, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(titulo, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap(148, Short.MAX_VALUE))
            .addGroup(painelPrincipalLayout.createSequentialGroup()
                .addGap(87, 87, 87)
                .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPrincipalLayout.createSequentialGroup()
                        .addComponent(textoCodigo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(codigoCampo, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPrincipalLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(usuario, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(apagarSenha, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPrincipalLayout.createSequentialGroup()
                        .addComponent(textoSenha)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(senha, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(painelPrincipalLayout.createSequentialGroup()
                        .addGap(107, 107, 107)
                        .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btLogin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btSair, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPrincipalLayout.createSequentialGroup()
                        .addComponent(esqueceuSenha)
                        .addGap(61, 61, 61)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        painelPrincipalLayout.setVerticalGroup(
            painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(imagem, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textoCodigo)
                    .addComponent(codigoCampo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelPrincipalLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(textoSenha))
                    .addGroup(painelPrincipalLayout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(senha, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(apagarSenha)
                .addGap(35, 35, 35)
                .addComponent(btLogin)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btSair)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(esqueceuSenha)
                .addContainerGap(52, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(painelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(painelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Ação executada ao clicar no botão de login. Chama o método de
     * autenticação.
     *
     * @param evt Evento de clique do mouse.
     */

    private void btLoginMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btLoginMouseClicked
        LOGGER.info("Botão de login clicado.");
        login();

    }//GEN-LAST:event_btLoginMouseClicked

    /**
     * Ação executada ao clicar no botão "Sair". Fecha a janela de login.
     *
     * @param evt Evento de ação do botão.
     */

    private void btSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSairActionPerformed
        LOGGER.info("Fechando janela de login.");
        dispose();

    }//GEN-LAST:event_btSairActionPerformed

    /**
     * Ação executada ao clicar no rótulo de apagar senha. Limpa o campo de
     * senha.
     *
     * @param evt Evento de clique do mouse.
     */

    private void apagarSenhaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_apagarSenhaMouseClicked
        LOGGER.info("Limpando campo de senha.");
        limpar();

    }//GEN-LAST:event_apagarSenhaMouseClicked
    /**
     * Ação executada ao pressionar uma tecla no campo de código. Busca o
     * usuário pelo código e atualiza o nome e a imagem do perfil, se
     * encontrados.
     *
     * @param evt Evento de tecla pressionada.
     */

    private void codigoCampoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_codigoCampoKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_TAB || evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (codigoCampo.getText().trim().isEmpty() && usuario.getText().trim().isEmpty() && senha.getText().trim().isEmpty()) {
                LOGGER.warning("Campos de código ou senha estão vazios.");
                funcoes.Avisos("erro.png", "Preencha os campos");
                return;
            }
            buscarUsuario();
            senha.requestFocus();
        }
    }//GEN-LAST:event_codigoCampoKeyPressed

    /**
     * Ação executada ao pressionar a tecla Enter no formulário. Chama o método
     * de login.
     *
     * @param evt Evento de tecla pressionada.
     */

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed

    }//GEN-LAST:event_formKeyPressed

    private void esqueceuSenhaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_esqueceuSenhaMouseClicked
        janelaEsqueceuSenha.pack();
        janelaEsqueceuSenha.setLocationRelativeTo(painelPrincipal);
        janelaEsqueceuSenha.setVisible(true);
    }//GEN-LAST:event_esqueceuSenhaMouseClicked

    private void btRedefinirSenhaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btRedefinirSenhaMouseClicked
        // Verificação dos campos obrigatórios
        if (codigoUsuario.getText().trim().isEmpty() || cpfSenhaEsquecida.getText().trim().isEmpty()) {
            funcoes.Avisos("sinal-de-aviso.png", "Preencha todos os campos para redefinir a senha");
            return;
        }

        // Verificação do formato do CPF
        if (cpfSenhaEsquecida.getText().length() < 14) {
            funcoes.Avisos("sinal-de-aviso.png", "CPF incompleto. Formato esperado: 000.000.000-00");
            return;
        }
        // Verificação do código de usuário
        if (!codigoUsuario.getText().matches("[A-Za-z0-9]+")) {
            funcoes.Avisos("sinal-de-aviso.png", "Código de usuário contém caracteres inválidos");
            return;
        }

// Verificação se é um administrador primário (id <= 5)
        try {
            int id = Integer.parseInt(codigoUsuario.getText());
            if (id <= 5) {
                funcoes.Avisos("sinal-de-aviso.png", "Não é permitido redefinir a senha de administradores primários por este meio");
                return;
            }
        } catch (NumberFormatException e) {
            funcoes.Avisos("sinal-de-aviso.png", "Código de usuário inválido");
            return;
        }

        String cpfFormatado = funcoes.removePontuacaoEEspacos(cpfSenhaEsquecida.getText().trim());

        // Verificação do código de usuário
        if (!codigoUsuario.getText().matches("[A-Za-z0-9]+")) {
            funcoes.Avisos("sinal-de-aviso.png", "Código de usuário contém caracteres inválidos");
            return;
        }

        try {
            Connection con = Conexao.conexaoBanco();

            // Primeiro verifica se o usuário existe
            String CHECK_USER = "SELECT COUNT(*) FROM pessoa WHERE id_pessoa = ? AND cpf = ? ";
            PreparedStatement checkStmt = con.prepareStatement(CHECK_USER);
            checkStmt.setString(1, codigoUsuario.getText());
            checkStmt.setString(2, cpfFormatado);

            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                funcoes.Avisos("sinal-de-aviso.png", "Combinação de código de usuário e CPF não encontrada");
                con.close();
                return;
            }

            // Se o usuário existe, prossegue com a atualização
            String senhaHash = "";
            try {
                senhaHash = BCrypt.hashpw("123GL", BCrypt.gensalt());
            } catch (Exception e) {
                System.err.println("Erro na criptografia: " + e.getMessage());
                funcoes.Avisos("sinal-de-aviso.png", "Erro ao processar a senha");
                con.close();
                dispose();
                return;
            }

            PreparedStatement stmt = con.prepareStatement(UPDATE_PASSWORD);
            stmt.setString(1, senhaHash);
            stmt.setString(2, codigoUsuario.getText());
            stmt.setString(3, cpfFormatado);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                funcoes.Avisos("sucesso.png", "Senha redefinida com sucesso para 123GL");
            } else {
                funcoes.Avisos("sinal-de-aviso.png", "Nenhum registro foi atualizado");
            }

            con.close();
            codigoUsuario.setText("");
            cpfSenhaEsquecida.setText("");
            janelaEsqueceuSenha.dispose();
        } catch (SQLException ex) {
            Logger.getLogger(CadastroPessoas.class.getName()).log(Level.SEVERE, null, ex);
            funcoes.Avisos("erro.png", "Erro ao atualizar: " + ex.getMessage());
            janelaEsqueceuSenha.dispose();
        }
    }//GEN-LAST:event_btRedefinirSenhaMouseClicked

    private void btRedefinirSenhaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRedefinirSenhaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btRedefinirSenhaActionPerformed

    private void senhaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_senhaKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (codigoCampo.getText().trim().isEmpty() || usuario.getText().trim().isEmpty() || senha.getText().trim().isEmpty()) {
                LOGGER.warning("Campos de código ou senha estão vazios.");
                funcoes.Avisos("erro.png", "Preencha os campos");
                return;
            }
            LOGGER.info("Tecla Enter pressionada no formulário. Iniciando login.");
            login();
        }
    }//GEN-LAST:event_senhaKeyPressed

    private void senhaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_senhaFocusGained
        if(codigoCampo.getText().isEmpty()){
            funcoes.Avisos("sinal-de-aviso.png", "Preencha o campo de código inicialmente");
            codigoCampo.requestFocus();
            return;
        }
        if(!codigoCampo.getText().isEmpty() && usuario.getText().isEmpty()){
            buscarUsuario();
        }
    }//GEN-LAST:event_senhaFocusGained
    /**
     * Método principal para inicializar a aplicação com o Look and Feel
     * personalizado e exibir a tela de login.
     *
     * @param args Argumentos da linha de comando.
     */
    public static void main(String args[]) {
        try {
            LOGGER.info("Configurando Look and Feel nativo do sistema");
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            LOGGER.severe("Erro ao configurar Look and Feel: " + e.getMessage());
            e.printStackTrace();
        }
        java.awt.EventQueue.invokeLater(() -> {
            LOGGER.info("Inicializando aplicação de login.");
            new Login().setVisible(true);
        });

    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel apagarSenha;
    private javax.swing.JButton btLogin;
    private javax.swing.JButton btRedefinirSenha;
    private javax.swing.JButton btSair;
    private javax.swing.JTextField codigoCampo;
    private javax.swing.JTextField codigoUsuario;
    private javax.swing.JTextField cpfSenhaEsquecida;
    private javax.swing.JLabel esqueceuSenha;
    private javax.swing.JLabel imagem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JDialog janelaEsqueceuSenha;
    private javax.swing.JPanel painelPrincipal;
    private javax.swing.JPasswordField senha;
    private javax.swing.JLabel textoCodigo;
    private javax.swing.JLabel textoSenha;
    private javax.swing.JLabel titulo;
    private javax.swing.JTextField usuario;
    // End of variables declaration//GEN-END:variables
}
