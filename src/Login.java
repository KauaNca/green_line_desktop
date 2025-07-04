
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.util.logging.Logger;

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
    private static final String SELECT_USER_LOGIN = "SELECT id_pessoa, nome,id_tipo_usuario, senha, situacao, imagem_perfil FROM pessoa WHERE nome = ? AND senha = ?"; 
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

        Frame();
        // Configura bordas dos campos de entrada
        usuario.setBorder(margensInternas);
        codigoCampo.setBorder(margensInternas);
        senha.setBorder(margensInternas);
        // Define a cor de fundo do painel principal
        painelPrincipal.setBackground(corVerde);
        // Desativa e torna o campo de usuário não editável
        usuario.setEnabled(false);
        usuario.setEditable(false);
        // Define o ícone padrão para o campo de imagem
        imagem.setIcon(new ImageIcon(USER_IMAGE_PATH));
        // Desativa a navegação por tabulação no campo de código
        codigoCampo.setFocusTraversalKeysEnabled(false);
    }

    /**
     * Configura as propriedades do JFrame, incluindo layout, posição,
     * comportamento de fechamento e visibilidade.
     */
    public void Frame() {
        LOGGER.info("Configurando propriedades do JFrame.");
        setLayout(new BorderLayout()); // Define o layout como BorderLayout
        setLocationRelativeTo(null); // Centraliza a janela na tela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Fecha a aplicação ao fechar a janela
        setResizable(false); // Impede redimensionamento da janela
        setVisible(true); // Torna a janela visível
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelPrincipal = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        senha = new javax.swing.JPasswordField();
        jLabel1 = new javax.swing.JLabel();
        btLogin = new javax.swing.JButton();
        btSair = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        codigoCampo = new javax.swing.JTextField();
        apagarCodigo = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        usuario = new javax.swing.JTextField();
        imagem = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 242, 207));
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        painelPrincipal.setBackground(new java.awt.Color(0, 51, 0));
        painelPrincipal.setForeground(new java.awt.Color(255, 255, 255));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Senha");

        senha.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        senha.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Usuário");

        btLogin.setBackground(new java.awt.Color(50, 205, 50));
        btLogin.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        btLogin.setForeground(new java.awt.Color(255, 255, 255));
        btLogin.setText("Entrar");
        btLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btLoginMouseClicked(evt);
            }
        });

        btSair.setBackground(new java.awt.Color(169, 169, 169));
        btSair.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        btSair.setForeground(new java.awt.Color(255, 255, 255));
        btSair.setText("Sair");
        btSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSairActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Berlin Sans FB Demi", 0, 40)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Green Line");

        jLabel5.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(245, 245, 245));
        jLabel5.setText("Código");

        codigoCampo.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        codigoCampo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        codigoCampo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                codigoCampoKeyPressed(evt);
            }
        });

        apagarCodigo.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        apagarCodigo.setForeground(new java.awt.Color(255, 255, 255));
        apagarCodigo.setText("X");
        apagarCodigo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                apagarCodigoMouseClicked(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("X");
        jLabel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel8MouseClicked(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("X");
        jLabel9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel9MouseClicked(evt);
            }
        });

        usuario.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        usuario.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        usuario.setDisabledTextColor(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout painelPrincipalLayout = new javax.swing.GroupLayout(painelPrincipal);
        painelPrincipal.setLayout(painelPrincipalLayout);
        painelPrincipalLayout.setHorizontalGroup(
            painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelPrincipalLayout.createSequentialGroup()
                .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelPrincipalLayout.createSequentialGroup()
                        .addGap(135, 135, 135)
                        .addComponent(jLabel3))
                    .addGroup(painelPrincipalLayout.createSequentialGroup()
                        .addGap(120, 120, 120)
                        .addComponent(imagem, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPrincipalLayout.createSequentialGroup()
                .addContainerGap(32, Short.MAX_VALUE)
                .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPrincipalLayout.createSequentialGroup()
                        .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel5))
                        .addGap(24, 24, 24)
                        .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(senha)
                                .addComponent(codigoCampo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(usuario, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(apagarCodigo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPrincipalLayout.createSequentialGroup()
                        .addComponent(btLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btSair, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(85, 85, 85)))
                .addGap(18, 18, 18))
        );
        painelPrincipalLayout.setVerticalGroup(
            painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(imagem, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(painelPrincipalLayout.createSequentialGroup()
                        .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(codigoCampo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(23, 23, 23)
                        .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(usuario, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(24, 24, 24)
                        .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(senha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)))
                    .addGroup(painelPrincipalLayout.createSequentialGroup()
                        .addComponent(apagarCodigo)
                        .addGap(49, 49, 49)
                        .addComponent(jLabel8)
                        .addGap(40, 40, 40)
                        .addComponent(jLabel9)))
                .addGap(27, 27, 27)
                .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btLogin)
                    .addComponent(btSair))
                .addContainerGap(82, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(painelPrincipal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(painelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Registra o acesso do usuário no banco de dados, inserindo o ID e nome do
     * usuário na tabela de acessos.
     */
    
    public void numeroAcesso() {
        LOGGER.info("Registrando acesso do usuário: " + codigo);
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(INSERT_ACCESS)) {
            stmt.setString(1, codigo);
            stmt.setString(2, usuario.getText());
            stmt.setString(3,"Desktop");
            stmt.execute();
            LOGGER.info("Acesso registrado com sucesso.");
        } catch (Exception ex) {
            LOGGER.severe("Erro ao registrar acesso: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, ERROR_GENERIC + ex.getMessage());
        }
    }

    /**
     * Realiza o processo de login, validando o código, usuário e senha. Abre a
     * tela inicial se o login for bem-sucedido e registra o acesso.
     */
    private void Login() {
        if (codigoCampo.getText().isBlank() || senha.getText().isBlank()) {
            LOGGER.warning("Campos de código ou senha estão vazios.");
            funcoes.Avisos("erro.png", "Preencha os campos");
            return;
        }

        LOGGER.info("Iniciando processo de login para usuário: " + usuario.getText());
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_USER_LOGIN)) {
            stmt.setString(1, usuario.getText());
            stmt.setString(2, senha.getText());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    codigo = rs.getString("id_pessoa");
                    tipo_usuario = String.valueOf(rs.getString("id_tipo_usuario"));
                    nome = rs.getString("nome");
                    situacao = String.valueOf(rs.getString("situacao"));
                    System.out.println(codigo + tipo_usuario + situacao);
                    if (tipo_usuario.equals("1") && situacao.equals("A")) {
                        LOGGER.info("Login bem-sucedido. Abrindo TelaInicial.");
                        new TelaInicial(codigo, tipo_usuario);
                        dispose();
                    } else {
                        LOGGER.warning("Usuário sem permissão de acesso: " + usuario.getText());
                        JOptionPane.showMessageDialog(null, "Usuário comum | Sem acesso | Fale com ADM");
                    }
                } else {
                    LOGGER.warning("Credenciais inválidas para usuário: " + usuario.getText());
                    JOptionPane.showMessageDialog(null, "Senha ou Usuário incorreto!");
                }
            }
            //numeroAcesso(); // Registra o acesso após a tentativa de login
        } catch (Exception ex) {
            LOGGER.severe("Erro durante o login: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, ERROR_GENERIC + ex.getMessage());
        }
    }

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
     * Ação executada ao clicar no ícone de apagar código. Limpa os campos de
     * código e usuário.
     *
     * @param evt Evento de clique do mouse.
     */

    private void apagarCodigoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_apagarCodigoMouseClicked
        LOGGER.info("Limpando campos de código e usuário.");
        codigoCampo.setText("");
        usuario.setText("");


    }//GEN-LAST:event_apagarCodigoMouseClicked
    /**
     * Ação executada ao clicar no rótulo de apagar código. Limpa os campos de
     * código e usuário.
     *
     * @param evt Evento de clique do mouse.
     */

    private void jLabel8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MouseClicked
        LOGGER.info("Limpando campos de código e usuário via jLabel8.");
        codigoCampo.setText("");
        usuario.setText("");
    }//GEN-LAST:event_jLabel8MouseClicked
    /**
     * Ação executada ao clicar no rótulo de apagar senha. Limpa o campo de
     * senha.
     *
     * @param evt Evento de clique do mouse.
     */

    private void jLabel9MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MouseClicked
        LOGGER.info("Limpando campo de senha.");
        senha.setText("");

    }//GEN-LAST:event_jLabel9MouseClicked
    /**
     * Ação executada ao pressionar uma tecla no campo de código. Busca o
     * usuário pelo código e atualiza o nome e a imagem do perfil, se
     * encontrados.
     *
     * @param evt Evento de tecla pressionada.
     */

    private void codigoCampoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_codigoCampoKeyPressed
        codigoCampo.setFocusTraversalKeysEnabled(false);
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_TAB) {
            LOGGER.info("Buscando usuário pelo código: " + codigoCampo.getText());
            try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_USER_BY_ID)) {
                stmt.setString(1, codigoCampo.getText());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String nomeUsuario = rs.getString("nome");
                        String usuarioImagem = rs.getString("imagem_perfil");
                        ImageIcon imagemUsuario = new ImageIcon("imagens/usuarios/" + usuarioImagem);

                        if (imagemUsuario.getIconWidth() == -1) {
                            LOGGER.warning("Imagem não encontrada: " + usuarioImagem);
                        } else {
                            usuario.setText(nomeUsuario);
                            codigoCampo.setFocusTraversalKeysEnabled(true);
                            imagem.setIcon(redimensionamentoDeImagem(imagemUsuario, 200, 142));
                            painelPrincipal.add(imagem2); // Adiciona novamente ao painel
                            painelPrincipal.revalidate();
                            painelPrincipal.repaint();
                            LOGGER.info("Usuário encontrado: " + nomeUsuario);
                        }
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
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            LOGGER.info("Tecla Enter pressionada no formulário. Iniciando login.");
            Login();
        }

    }//GEN-LAST:event_formKeyPressed
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
    private javax.swing.JLabel apagarCodigo;
    private javax.swing.JButton btLogin;
    private javax.swing.JButton btSair;
    private javax.swing.JTextField codigoCampo;
    private javax.swing.JLabel imagem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel painelPrincipal;
    private javax.swing.JPasswordField senha;
    private javax.swing.JTextField usuario;
    // End of variables declaration//GEN-END:variables
}
