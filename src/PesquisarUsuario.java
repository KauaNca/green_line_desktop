
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import java.util.logging.Logger;

/**
 * JInternalFrame para pesquisa de usuários (pessoas físicas ou jurídicas) no
 * sistema. Permite buscar dados de usuários pelo nome e exibir informações
 * detalhadas.
 *
 * @author guilherme53961526
 */
public class PesquisarUsuario extends javax.swing.JInternalFrame {

    // Logger para rastreamento de eventos e erros
    private static final Logger LOGGER = Logger.getLogger(PesquisarUsuario.class.getName());

    // Constantes para queries SQL
    private static final String SELECT_USER_NAMES = "SELECT nome FROM pessoa WHERE tipo_pessoa = ?";
    private static final String SELECT_PERSON_DATA = "SELECT * FROM dados_pesquisa WHERE nome = ? AND tipo_pessoa = 'F'";
    private static final String SELECT_COMPANY_DATA = "SELECT * FROM dados_pesquisa WHERE nome = ? AND tipo_pessoa = 'J'";

    // Componentes e variáveis da interface
    private CardLayout card;
    private ArrayList<String> usuarios;
    private JPopupMenu caixaDeNomes = new JPopupMenu();
    private ArrayList<String> filtro;
    private final Font fonteItem = new Font("Arial", Font.PLAIN, 19);

    /**
     * Construtor da classe PesquisarUsuario. Inicializa a interface, configura
     * ícones e carrega a lista de nomes de usuários.
     */
    public PesquisarUsuario() {
        initComponents();
        // Define ícones padrão para os perfis
        perfil.setIcon(new ImageIcon("imagens/perfil.png"));
        usuarios = new ArrayList<>();
        desativarTextField(painelPessoa); // Desativa campos do painel de pessoa
        nomesUsuarios("F"); // Carrega nomes de pessoas físicas
    }

    /**
     * Desativa todos os campos de texto (JTextField) em um painel, exceto os
     * campos de código de usuário e CPF/CNPJ.
     *
     * @param painel Painel contendo os campos a serem desativados.
     */
    public void desativarTextField(JPanel painel) {
        LOGGER.info("Desativando campos de texto no painel: " + painel.getName());
        for (Component component : painel.getComponents()) {
            if (component instanceof JTextField) {
                component.setEnabled(false);
            }
        }
        // Habilita campos específicos para entrada de dados
        codigoUsuario.setEnabled(true);
        cpf.setEnabled(true);
    }

    /**
     * Redimensiona uma imagem para as dimensões especificadas, mantendo
     * suavidade.
     *
     * @param imagem Ícone da imagem original.
     * @param largura Largura desejada.
     * @param altura Altura desejada.
     * @return Ícone da imagem redimensionada.
     */
    public ImageIcon redimensionamentoDeImagem(ImageIcon imagem, int largura, int altura) {
        LOGGER.info("Redimensionando imagem para " + largura + "x" + altura);
        Image pegarImagem = imagem.getImage();
        Image redimensionando = pegarImagem.getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
        return new ImageIcon(redimensionando);
    }

    /**
     * Carrega os nomes dos usuários do banco de dados com base no tipo de
     * pessoa (F para pessoa física, J para jurídica).
     *
     * @param tipo_pessoa Tipo de pessoa ('F' ou 'J').
     */
    public void nomesUsuarios(String tipo_pessoa) {
        LOGGER.info("Carregando nomes de usuários do tipo: " + tipo_pessoa);
        usuarios.clear();
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_USER_NAMES)) {
            stmt.setString(1, tipo_pessoa);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(rs.getString("nome"));
                }
                // Loga os nomes carregados para depuração
                for (String nome : usuarios) {
                    LOGGER.info("Nome carregado: " + nome);
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Erro ao carregar nomes de usuários: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Filtra nomes de usuários com base no texto digitado no campo de pesquisa
     * e exibe uma lista suspensa com os resultados correspondentes.
     */
    public void pesquisarNome() {
        String texto = tfPesquisar.getText();
        LOGGER.info("Pesquisando nomes com texto: " + texto);
        filtro = new ArrayList<>();
        filtro.clear();
        if (!texto.isEmpty()) {
            // Filtra nomes que contêm o texto digitado
            for (String nome : usuarios) {
                if (nome.contains(texto)) {
                    filtro.add(nome);
                }
            }
            caixaDeNomes.removeAll();
            if (!filtro.isEmpty()) {
                // Adiciona itens filtrados ao menu suspenso
                for (String nome : filtro) {
                    JMenuItem item = new JMenuItem(nome);
                    item.addActionListener(e -> {
                        tfPesquisar.setText(nome);
                        item.setFont(fonteItem);
                        caixaDeNomes.setVisible(false);
                    });
                    caixaDeNomes.add(item);
                }
                // Exibe o menu suspenso abaixo do campo de texto
                caixaDeNomes.setVisible(true);
                caixaDeNomes.show(tfPesquisar, 0, tfPesquisar.getHeight());
            } else {
                caixaDeNomes.setVisible(false);
            }
        } else {
            // Limpa e oculta o menu se o campo de texto estiver vazio
            caixaDeNomes.removeAll();
            caixaDeNomes.setVisible(false);
        }
    }

    /**
     * Pesquisa os dados de uma pessoa física no banco de dados com base no nome
     * e preenche os campos da interface com as informações encontradas.
     */
    private void pesquisarPessoa() {
        LOGGER.info("Pesquisando pessoa física com nome: " + tfPesquisar.getText());
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_PERSON_DATA)) {
            stmt.setString(1, tfPesquisar.getText());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Preenche os campos com os dados da pessoa
                    codigoUsuario.setText(rs.getString("id_usuario"));
                    nome.setText(rs.getString("nome"));
                    email.setText(rs.getString("email"));
                    telefone.setText(rs.getString("telefone"));
                    cpf.setText(rs.getString("cpf_cnpj"));
                    rg.setText(rs.getString("rg"));
                    idade.setText(rs.getString("idade"));
                    estado.setText(rs.getString("uf"));
                    cep.setText(rs.getString("cep"));
                    cidade.setText(rs.getString("cidade"));
                    bairro.setText(rs.getString("bairro"));
                    endereco.setText(rs.getString("endereco"));
                    complemento.setText(rs.getString("complemento"));

                    // Carrega e redimensiona a imagem do perfil
                    ImageIcon foto = new ImageIcon("imagens/usuarios/" + rs.getString("caminho_imagem"));
                    perfil.setIcon(redimensionamentoDeImagem(foto, 205, 227));
                    LOGGER.info("Dados da pessoa física carregados com sucesso.");
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Erro ao pesquisar pessoa física: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "<html> <h3> Não foi possível encontrar este nome</h3> </html>");
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
        jLabel4 = new javax.swing.JLabel();
        telefone = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        idade = new javax.swing.JTextField();
        dataNascimento = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
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
        rg = new javax.swing.JFormattedTextField();
        cep = new javax.swing.JFormattedTextField();
        btCancelar1 = new javax.swing.JButton();

        setClosable(true);
        setTitle("Pesquisar Usuários");

        tfPesquisar.setFont(new java.awt.Font("Arial", 0, 17)); // NOI18N
        tfPesquisar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfPesquisarKeyReleased(evt);
            }
        });

        btnProcurar.setBackground(new java.awt.Color(50, 205, 50));
        btnProcurar.setFont(new java.awt.Font("Arial", 0, 17)); // NOI18N
        btnProcurar.setForeground(new java.awt.Color(255, 255, 255));
        btnProcurar.setText("Procurar");
        btnProcurar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProcurarActionPerformed(evt);
            }
        });

        JCard.setLayout(new java.awt.CardLayout());

        jLabel1.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel1.setText("Nome");

        nome.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        nome.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel2.setText("E-mail");

        email.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        email.setEnabled(false);
        email.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel3.setText("Cadastro de Pessoa Física (CPF)");

        jLabel4.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel4.setText("Registro Geral (RG)");

        telefone.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        telefone.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel5.setText("Telefone");

        jLabel6.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel6.setText("Idade");

        idade.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        idade.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        dataNascimento.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        dataNascimento.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        jLabel7.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel7.setText("Data de nascimento");

        jLabel8.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel8.setText("Estado");

        estado.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        estado.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        jLabel9.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel9.setText("CEP");

        jLabel10.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel10.setText("Cidade");

        cidade.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        cidade.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        jLabel11.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel11.setText("Bairro");

        bairro.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        bairro.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        endereco.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        endereco.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        jLabel12.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel12.setText("Endereço");

        jLabel13.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel13.setText("Código");

        codigoUsuario.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        codigoUsuario.setSelectedTextColor(new java.awt.Color(51, 51, 51));
        codigoUsuario.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                codigoUsuarioKeyPressed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel14.setText("Complemento");

        complemento.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        complemento.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        btComprasUsuario.setBackground(new java.awt.Color(255, 165, 0));
        btComprasUsuario.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        btComprasUsuario.setForeground(new java.awt.Color(255, 255, 255));
        btComprasUsuario.setText("Compras");

        cpf.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
        cpf.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        cpf.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                cpfKeyReleased(evt);
            }
        });

        rg.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
        rg.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N

        try {
            cep.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("#####-###")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        cep.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N

        btCancelar1.setBackground(new java.awt.Color(255, 0, 0));
        btCancelar1.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
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
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel2)
                                    .addComponent(email, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                                    .addComponent(cpf))
                                .addGap(18, 18, 18)
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(painelPessoaLayout.createSequentialGroup()
                                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel6)
                                            .addComponent(idade, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel7)
                                            .addComponent(dataNascimento)))
                                    .addGroup(painelPessoaLayout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(rg)))
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel13)
                                    .addComponent(codigoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(26, 26, 26)
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(nome, javax.swing.GroupLayout.PREFERRED_SIZE, 483, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(telefone, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel3))))
                .addContainerGap(49, Short.MAX_VALUE))
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
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(painelPessoaLayout.createSequentialGroup()
                                    .addComponent(jLabel5)
                                    .addGap(35, 35, 35))
                                .addComponent(telefone, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nome, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(codigoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPessoaLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPessoaLayout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(idade, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPessoaLayout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dataNascimento, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cpf)
                            .addComponent(rg))
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
                .addContainerGap(37, Short.MAX_VALUE))
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
                .addComponent(JCard, javax.swing.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
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
        // Desativa campos que não devem ser editados
        codigoUsuario.setEnabled(false);
        email.setEnabled(false);
        cpf.setEnabled(false);
        pesquisarPessoa();
        tfPesquisar.setText(""); // Limpa o campo de pesquisa
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
            try {
                Connection con = Conexao.conexaoBanco();
                PreparedStatement stmt = con.prepareStatement("SELECT * FROM dados_pesquisa WHERE id_usuario = ? AND tipo_pessoa = 'F'");
                stmt.setString(1, codigoUsuario.getText());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    codigoUsuario.setText(rs.getString("id_usuario"));
                    nome.setText(rs.getString("nome"));
                    email.setText(rs.getString("email"));
                    telefone.setText(rs.getString("telefone"));
                    cpf.setText(rs.getString("cpf_cnpj"));
                    rg.setText(rs.getString("rg"));
                    idade.setText(rs.getString("idade"));
                    estado.setText(rs.getString("uf"));
                    cep.setText(rs.getString("cep"));
                    cidade.setText(rs.getString("cidade"));
                    bairro.setText(rs.getString("bairro"));
                    endereco.setText(rs.getString("endereco"));
                    complemento.setText(rs.getString("complemento"));

                    ImageIcon foto = new ImageIcon("imagens/usuarios/" + rs.getString("caminho_imagem"));
                    perfil.setIcon(redimensionamentoDeImagem(foto, 205, 227));

                } else {
                    JOptionPane.showMessageDialog(null, "Código de usuário não encontrado");
                }
                rs.close();
                stmt.close();
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "<html> <h3> Não foi possível encontrar este nome</h3> </html>");

            }
        }

    }//GEN-LAST:event_codigoUsuarioKeyPressed

    private void btCancelar1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btCancelar1MouseClicked
        perfil.setIcon(new ImageIcon("imagens/perfil.png"));
        usuarios = new ArrayList<>();
        for (Component component : painelPessoa.getComponents()) {
            if (component instanceof JTextField) {
                ((JTextField) component).setText("");
            }
            nomesUsuarios("F");
        }

        desativarTextField(painelPessoa);

    }//GEN-LAST:event_btCancelar1MouseClicked
    /**
     * Ação executada ao pressionar a tecla Enter no campo de CPF. Pesquisa os
     * dados da pessoa física pelo CPF e preenche os campos.
     *
     * @param evt Evento de tecla liberada.
     */

    private void cpfKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cpfKeyReleased
        if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
            try {
                Connection con = Conexao.conexaoBanco();
                PreparedStatement stmt = con.prepareStatement("SELECT * FROM dados_pesquisa WHERE cpf_cnpj = ? AND tipo_pessoa = 'F'");
                stmt.setString(1, cpf.getText());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    codigoUsuario.setText(rs.getString("id_usuario"));
                    nome.setText(rs.getString("nome"));
                    email.setText(rs.getString("email"));
                    telefone.setText(rs.getString("telefone"));
                    cpf.setText(rs.getString("cpf_cnpj"));
                    rg.setText(rs.getString("rg"));
                    idade.setText(rs.getString("idade"));
                    estado.setText(rs.getString("uf"));
                    cep.setText(rs.getString("cep"));
                    cidade.setText(rs.getString("cidade"));
                    bairro.setText(rs.getString("bairro"));
                    endereco.setText(rs.getString("endereco"));
                    complemento.setText(rs.getString("complemento"));

                    ImageIcon foto = new ImageIcon("imagens/usuarios/" + rs.getString("caminho_imagem"));
                    perfil.setIcon(redimensionamentoDeImagem(foto, 205, 227));

                } else {
                    JOptionPane.showMessageDialog(null, "CPF não encontrado");
                }
                rs.close();
                stmt.close();
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "<html> <h3> Não foi possível encontrar este nome</h3> </html>");

            }
        }
    }//GEN-LAST:event_cpfKeyReleased


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
    private javax.swing.JTextField dataNascimento;
    private javax.swing.JTextField email;
    private javax.swing.JTextField endereco;
    private javax.swing.JTextField estado;
    private javax.swing.JTextField idade;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField nome;
    private javax.swing.JPanel painelPessoa;
    private javax.swing.JLabel perfil;
    private javax.swing.JFormattedTextField rg;
    private javax.swing.JTextField telefone;
    private javax.swing.JTextField tfPesquisar;
    private javax.swing.ButtonGroup tipoPessoa;
    // End of variables declaration//GEN-END:variables
}
