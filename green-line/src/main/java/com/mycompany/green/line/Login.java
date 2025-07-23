package com.mycompany.green.line;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.util.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;
import com.mycompany.green.line.TelaComImagem;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 * JFrame para a tela de login do sistema. Configura a interface gráfica para
 * autenticação de usuários, incluindo campos para código, usuário e senha, e
 * ícones personalizados.
 *
 * @author [Autor não especificado]
 */
public class Login extends javax.swing.JFrame {

    // Logger para rastreamento de eventos e erros
    private static final Logger LOGGER = Logger.getLogger(Login.class.getName());

    // Constantes para configurações de UI
    private static final String USER_IMAGE_PATH = "imagens/usuarios/usuario.png";
    private static final Color GREEN_COLOR = new Color(29, 68, 53);
    private static final EmptyBorder FIELD_BORDER = new EmptyBorder(5, 5, 0, 0);
    private static final String INSERT_ACCESS = "INSERT INTO acessos(id_pessoa,usuario,local) VALUES (?,?,?)";
    private static final String SELECT_USER_LOGIN = "SELECT id_pessoa, nome, id_tipo_usuario, senha, situacao, imagem_perfil FROM pessoa WHERE nome = ?";
// Remova completamente a condição "AND senha = ?"
    private static final String SELECT_USER_BY_ID = "SELECT * FROM pessoa WHERE id_pessoa = ?";
    private static final String ERROR_GENERIC = "Erro: ";
    Funcoes funcoes = new Funcoes();

    // Variáveis de estado
    private String codigo;
    private String nome;
    private String situacao;
    private String tipo_usuario;
    private final JLabel imagem2 = new JLabel();
    private final Color corVerde = GREEN_COLOR;
    private final EmptyBorder margensInternas = FIELD_BORDER;

    /**
     * Construtor da classe Login. Inicializa os componentes da interface,
     * configura as bordas, cores, ícones e desativa o campo de usuário.
     */
    public Login() {
        initComponents();
        LOGGER.info("Inicializando tela de login.");
        frame();
        inicio();
        testHashCompatibilidade();
        ImageIcon originalIcon = new ImageIcon(TelaComImagem.class.getResource("/imagens/logo.png"));
        Image img = originalIcon.getImage();
        Image resizedImg = img.getScaledInstance(64, 64, Image.SCALE_SMOOTH);

        setIconImage(resizedImg);

    }

    /*MÉTODOS*/
    private void limpar() {
        codigoCampo.setText("");
        usuario.setText("");
        senha.setText("");
        imagem.setIcon(new ImageIcon(USER_IMAGE_PATH));
    }

    private void inicio() {
        usuario.setBorder(margensInternas);// Configura bordas dos campos de entrada
        codigoCampo.setBorder(margensInternas);
        senha.setBorder(margensInternas);
        painelPrincipal.setBackground(corVerde);// Define a cor de fundo do painel principal
        usuario.setEnabled(false); // Desativa e torna o campo de usuário não editável
        usuario.setEditable(false);
        imagem.setIcon(new ImageIcon(USER_IMAGE_PATH));// Define o ícone padrão para o campo de imagem  
        codigoCampo.setFocusTraversalKeysEnabled(false);// Desativa a navegação por tabulação no campo de código
    }

    /**
     * Registra o acesso do usuário no banco de dados, inserindo o ID e nome do
     * usuário na tabela de acessos.
     */
    public void numeroAcesso() {
        LOGGER.info("Registrando acesso do usuário: " + codigo);
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(INSERT_ACCESS)) {
            stmt.setString(1, codigo);
            stmt.setString(2, usuario.getText());
            stmt.setString(3, "Desktop");
            stmt.execute();
            LOGGER.info("Acesso registrado com sucesso.");
        } catch (Exception ex) {
            LOGGER.severe("Erro ao registrar acesso: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, ERROR_GENERIC + ex.getMessage());
        }
    }

    public static void testHashCompatibilidade() {
        String hashExemplo = "$2b$10$Gqjn9q..AQC09Gzm94je8umnE2cB4dLWRtVtHGsXhrcDUKbIXJVbK";
        String senhaTeste = "senhaTeste123"; // Substitua pela senha real

        System.out.println("Teste de compatibilidade:");
        System.out.println("Hash original: " + hashExemplo);
        System.out.println("Hash normalizado: " + hashExemplo.replaceFirst("^\\$2b\\$", "\\$2a\\$"));
        System.out.println("Resultado: " + BCryptUtil.checkPassword(senhaTeste, hashExemplo));
    }

    /**
     * Realiza o processo de login, validando o código, usuário e senha. Abre a
     * tela inicial se o login for bem-sucedido e registra o acesso.
     */
    private void Login() {

        // Validação de campos vazios (OR em vez de AND)
        if (codigoCampo.getText().trim().isEmpty() || usuario.getText().trim().isEmpty() || senha.getText().trim().isEmpty()) {
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

                    // Use o método seguro de verificação
                    if (!BCryptUtil.checkPassword(senha.getText(), senhaHash)) {
                        LOGGER.warning("Senha incorreta para: " + usuario.getText());
                        JOptionPane.showMessageDialog(null, "Credenciais inválidas");
                        return;
                    }
                    // Autenticação bem-sucedida
                    codigo = rs.getString("id_pessoa");
                    tipo_usuario = rs.getString("id_tipo_usuario");

                    if ("1".equals(tipo_usuario)) {
                        new TelaInicial(codigo, tipo_usuario);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, "Acesso não autorizado");
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.severe("Erro SQL: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Erro no banco de dados");
        }
    }

    private boolean verificarSenha(String senhaDigitada, String hashArmazenado) {
        try {
            // Verificação extra do hash antes da comparação
            if (!hashArmazenado.matches("^\\$2[aby]\\$\\d{2}\\$[./0-9A-Za-z]{53}$")) {
                throw new IllegalArgumentException("Formato de hash inválido");
            }

            return BCrypt.checkpw(senhaDigitada, hashArmazenado);
        } catch (Exception e) {
            LOGGER.severe("Falha na verificação: " + e.getMessage());
            return false;
        }
    }

    /**
     * Configura as propriedades do JFrame, incluindo layout, posição,
     * comportamento de fechamento e visibilidade.
     */
    private void frame() {
        LOGGER.info("Configurando propriedades do JFrame.");
        setLayout(new BorderLayout()); // Define o layout como BorderLayout
        setLocationRelativeTo(null); // Centraliza a janela na tela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Fecha a aplicação ao fechar a janela
        setResizable(false); // Impede redimensionamento da janela
        setVisible(true); // Torna a janela visível
    }

    private void buscarUsuario() {
        if (codigoCampo.getText().trim().isEmpty() && usuario.getText().trim().isEmpty() && senha.getText().trim().isEmpty()) {
            LOGGER.warning("Campos de código ou senha estão vazios.");
            funcoes.Avisos("erro.png", "Preencha os campos");
            return;
        }
        LOGGER.info("Buscando usuário pelo código: " + codigoCampo.getText());
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_USER_BY_ID)) {
            stmt.setString(1, codigoCampo.getText());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String nomeUsuario = rs.getString("nome");
                    String imageUrl = rs.getString("imagem_perfil");
                    if (!imageUrl.contains("http")) {
                        imagem.setIcon(redimensionamentoDeImagem(new ImageIcon("imagens/usuarios/usuario.png"), 200, 132));
                    } else {
                        try {
                            URL url = new URL(imageUrl);
                            BufferedImage image = ImageIO.read(url);
                            if (image != null) {
                                imagem.setIcon(redimensionamentoDeImagem(new ImageIcon(image), 200, 132));
                            }
                        } catch (IOException e) {
                            funcoes.Avisos("erro.png", "Falha ao carregar URL. Tente novamente.");
                        }
                    }

                    usuario.setText(nomeUsuario);
                    codigoCampo.setFocusTraversalKeysEnabled(true);
                    painelPrincipal.add(imagem2); // Adiciona novamente ao painel
                    painelPrincipal.revalidate();
                    painelPrincipal.repaint();
                    LOGGER.info("Usuário encontrado: " + nomeUsuario);

                } else {
                    LOGGER.warning("Usuário não encontrado para o código: " + codigoCampo.getText());
                    funcoes.Avisos("sinal-de-aviso.png",
                            "Usuário não encontrado. Faça um cadastro ou contate os administradores.");
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Erro ao buscar usuário: " + e.getMessage());
            funcoes.Avisos("erro.png", "Houve um erro. Tente novamente mais tarde");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        senha = new javax.swing.JTextField();

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

        senha.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        senha.setBorder(javax.swing.BorderFactory.createEtchedBorder());
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(imagem, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(titulo, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPrincipalLayout.createSequentialGroup()
                .addContainerGap(92, Short.MAX_VALUE)
                .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(painelPrincipalLayout.createSequentialGroup()
                            .addGap(6, 6, 6)
                            .addComponent(textoSenha)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(senha, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(painelPrincipalLayout.createSequentialGroup()
                            .addComponent(textoCodigo)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(codigoCampo, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(painelPrincipalLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(usuario, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(apagarSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(103, 103, 103))
            .addGroup(painelPrincipalLayout.createSequentialGroup()
                .addGap(199, 199, 199)
                .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btLogin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btSair, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        painelPrincipalLayout.setVerticalGroup(
            painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(imagem, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textoCodigo)
                    .addComponent(codigoCampo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textoSenha)
                    .addComponent(senha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(apagarSenha)
                .addGap(18, 18, 18)
                .addComponent(btLogin)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btSair)
                .addContainerGap(53, Short.MAX_VALUE))
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
        Login();

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
        codigoCampo.setFocusTraversalKeysEnabled(false);
        if (evt.getKeyCode() == KeyEvent.VK_TAB || evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (codigoCampo.getText().trim().isEmpty() && usuario.getText().trim().isEmpty() && senha.getText().trim().isEmpty()) {
                LOGGER.warning("Campos de código ou senha estão vazios.");
                funcoes.Avisos("erro.png", "Preencha os campos");
                return;
            }
            buscarUsuario();
        }
    }//GEN-LAST:event_codigoCampoKeyPressed
    public ImageIcon redimensionamentoDeImagem(ImageIcon imagem, int largura, int altura) {
        LOGGER.info("Redimensionando imagem para " + largura + "x" + altura);
        Image pegarImagem = imagem.getImage();
        Image redimensionando = pegarImagem.getScaledInstance(largura, altura, Image.SCALE_DEFAULT);
        return new ImageIcon(redimensionando);
    }

    /**
     * Ação executada ao pressionar a tecla Enter no formulário. Chama o método
     * de login.
     *
     * @param evt Evento de tecla pressionada.
     */

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed

    }//GEN-LAST:event_formKeyPressed

    private void senhaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_senhaKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (codigoCampo.getText().trim().isEmpty() || usuario.getText().trim().isEmpty() || senha.getText().trim().isEmpty()) {
                LOGGER.warning("Campos de código ou senha estão vazios.");
                funcoes.Avisos("erro.png", "Preencha os campos");
                return;
            }
            LOGGER.info("Tecla Enter pressionada no formulário. Iniciando login.");
            Login();
        }
    }//GEN-LAST:event_senhaKeyPressed

    private void senhaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_senhaFocusGained
        if (codigoCampo.getText().trim().isEmpty() && usuario.getText().trim().isEmpty()) {
            funcoes.Avisos("sinal-de-aviso.png", "Preencha primeiro o código e usuário!");
            if (codigoCampo.isEnabled() && codigoCampo.isFocusable()) {
                codigoCampo.requestFocusInWindow();
            }
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

    /*SEÇÃO ACESSIBILIDADE*/
    private void acessibilidadeInicial() {
        // Adicionar descrições acessíveis
        codigoCampo.getAccessibleContext().setAccessibleName("Campo de código do usuário");
        codigoCampo.getAccessibleContext().setAccessibleDescription("Digite seu código de identificação");

        usuario.getAccessibleContext().setAccessibleName("Nome do usuário");
        usuario.getAccessibleContext().setAccessibleDescription("Nome do usuário autenticado");

        senha.getAccessibleContext().setAccessibleName("Senha do usuário");
        senha.getAccessibleContext().setAccessibleDescription("Digite sua senha de acesso");

        btLogin.getAccessibleContext().setAccessibleName("Botão de login");
        btLogin.getAccessibleContext().setAccessibleDescription("Clique para realizar o login no sistema");

        btSair.getAccessibleContext().setAccessibleName("Botão sair");
        btSair.getAccessibleContext().setAccessibleDescription("Clique para fechar a aplicação");
    }

    public class BCryptUtil {

        public static boolean checkPassword(String plaintext, String storedHash) {
            try {
                // Normaliza o prefixo para $2a$ se for $2b$
                String normalizedHash = storedHash.replaceFirst("^\\$2b\\$", "\\$2a\\$");
                return BCrypt.checkpw(plaintext, normalizedHash);
            } catch (Exception e) {
                System.err.println("Erro na verificação: " + e.getMessage());
                return false;
            }
        }

        public static String hashPassword(String password) {
            return BCrypt.hashpw(password, BCrypt.gensalt());
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel apagarSenha;
    private javax.swing.JButton btLogin;
    private javax.swing.JButton btSair;
    private javax.swing.JTextField codigoCampo;
    private javax.swing.JLabel imagem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel painelPrincipal;
    private javax.swing.JTextField senha;
    private javax.swing.JLabel textoCodigo;
    private javax.swing.JLabel textoSenha;
    private javax.swing.JLabel titulo;
    private javax.swing.JTextField usuario;
    // End of variables declaration//GEN-END:variables
}
