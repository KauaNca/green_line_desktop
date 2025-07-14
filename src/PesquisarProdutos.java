
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.swing.ImageIcon;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JMenuItem;
import javax.swing.JTextField;

/**
 * JInternalFrame para pesquisa de produtos no sistema. Permite buscar produtos
 * pelo nome ou ID, exibir detalhes e imagens associadas, com funcionalidade de
 * slideshow para múltiplas imagens.
 *
 * @author Kaua33500476
 */
public class PesquisarProdutos extends javax.swing.JInternalFrame {

    // Logger para rastreamento de eventos e erros
    private static final Logger LOGGER = Logger.getLogger(PesquisarProdutos.class.getName());

    // Constantes para query SQL e caminhos de imagens
    private static final String SELECT_PRODUCT_NAMES = "SELECT produto FROM produto WHERE LOWER(produto) LIKE ? AND ativo = TRUE";
    private static final String DEFAULT_IMAGE_PATH = "imagens/sem_imagem.jpg";
    private static final String PRODUCT_IMAGE_PATH = "imagens/produtos/";
    private static final String RIGHT_ARROW_PATH = "imagens/seta-direita.png";
    private static final String SEARCH_ICON_PATH = "imagens/lupa.png";
    private static final String SELECT_PRODUCT_BY_ID = "SELECT id_produto, produto, descricao, descricao_curta, preco, preco_promocional, promocao, marca, estoque, ativo, imagem_1, imagem_2, categoria FROM produto WHERE id_produto = ? AND ativo = TRUE";
    private static final String SELECT_PRODUCT_BY_NAME = "SELECT id_produto, produto, descricao, descricao_curta, preco, preco_promocional, promocao, marca, estoque, ativo, imagem_1, imagem_2, categoria FROM produto WHERE produto = ? AND ativo = TRUE";
    private static final String ERROR_DB_CONNECTION = "Erro ao conectar ao banco de dados: ";
    private static final String ERROR_GENERIC = "Erro: ";
    Funcoes funcoes = new Funcoes();

    // Variáveis de estado
    private int contagem = 0;
    private File arquivo;
    private boolean atualizandoMascara = false;
    private String id_produto;
    private String Produto;
    private String Preco;
    private String PrecoPromocional;
    private boolean Promocao;
    private String Descricao;
    private String DescricaoCurta;
    private String Marca;
    private String Estoque;
    private String Categoria;
    private boolean Ativo;
    private final JPopupMenu sugestoesProdutos = new JPopupMenu();
    private List<String> produtos;
    private final Font fonteItem = new Font("Arial", Font.PLAIN, 15);
    String imagem1;

    /**
     * Construtor da classe PesquisarProdutos. Inicializa a interface, configura
     * ícones padrão e carrega a lista de nomes de produtos.
     */
    public PesquisarProdutos() {
        initComponents();
        Inicio();
        nomesProdutos();

    }

    /**
     * Carrega os nomes dos produtos do banco de dados com base no texto de
     * pesquisa.
     */
    public void nomesProdutos() {
        LOGGER.info("Carregando nomes de produtos com filtro: " + pesquisar.getText());
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_PRODUCT_NAMES)) {
            stmt.setString(1, "%" + pesquisar.getText().toLowerCase() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                produtos = new ArrayList<>();
                while (rs.next()) {
                    produtos.add(rs.getString("produto"));
                }
                LOGGER.info("Nomes de produtos carregados: " + produtos.size());
            }
        } catch (SQLException ex) {
            LOGGER.severe("Erro ao carregar nomes dos produtos: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, ERROR_DB_CONNECTION + ex.getMessage());
        }
    }

    public void carregarImagemURL(String campo) {

        String imageUrl = campo.trim();
        if (imageUrl.isEmpty() || !imageUrl.contains("http")) {
            return;
        }
        funcoes.mostrarMensagemCarregando();
        System.out.println("Carregando imagem de URL para o campo: " + campo + " " + imageUrl);
        try {
            URL url = new URL(imageUrl);
            BufferedImage image = ImageIO.read(url);
            if (image != null) {
                sem_imagem.setIcon(redimensionamentoDeImagem(new ImageIcon(image), 346, 349));
            } else {
                funcoes.Avisos("aviso.jpg", "Imagem inválida. Tente outra URL.");
            }
        } catch (IOException e) {
            funcoes.Avisos("erro.png", "Falha ao carregar URL. Tente novamente.");
        }
    }

    /**
     * Inicializa a interface desabilitando campos de texto, configurando ícones
     * padrão e ocultando componentes de imagem.
     */
    public void Inicio() {
        LOGGER.info("Inicializando interface de pesquisa de produtos.");
        // Desabilita campos
        nomeProduto.setEnabled(false);
        preco.setEnabled(false);
        preco_promocional.setEnabled(false);
        descricao.setEnabled(false);
        descricao_curta.setEnabled(false);
        marca.setEnabled(false);
        estoque.setEnabled(false);
        categoria.setEnabled(false);
        prom_sim.setEnabled(false);
        prom_nao.setEnabled(false);
        ativo_sim.setEnabled(false);
        ativo_nao.setEnabled(false);
        // Configura ícones
        sem_imagem.setIcon(sem_imagem());
        pesquisa.setIcon(new ImageIcon(SEARCH_ICON_PATH));
    }

    /**
     * Aplica uma máscara ao campo de nome do produto, permitindo apenas
     * caracteres alfanuméricos e acentuados. Evita loops de atualização.
     */
    private void atualizarMascara() {
        if (atualizandoMascara) {
            return;
        }
        atualizandoMascara = true;
        SwingUtilities.invokeLater(() -> {
            String texto = nomeProduto.getText();
            nomeProduto.setText(texto.replaceAll("[^a-zA-Z0-9áéíóúâêîôûãõçÁÉÍÓÚÂÊÎÔÛÃÕÇñÑ~\\s]", ""));
            atualizandoMascara = false;
            LOGGER.info("Máscara aplicada ao nome do produto: " + nomeProduto.getText());
        });
    }

    /**
     * Retorna o ícone padrão para quando não há imagem disponível.
     *
     * @return Ícone redimensionado da imagem padrão.
     */
    public ImageIcon sem_imagem() {
        LOGGER.info("Carregando imagem padrão: " + DEFAULT_IMAGE_PATH);
        ImageIcon imagem = new ImageIcon(DEFAULT_IMAGE_PATH);
        return redimensionamentoDeImagem(imagem, 250, 216);
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
        Image redimensionada = imagem.getImage().getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
        return new ImageIcon(redimensionada);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sem_imagem = new javax.swing.JLabel();
        pesquisar = new javax.swing.JTextField();
        pesquisa = new javax.swing.JLabel();
        codigo = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        nomeProduto = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        preco = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        descricao = new javax.swing.JTextArea();
        jLabel6 = new javax.swing.JLabel();
        marca = new javax.swing.JTextField();
        estoque = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        btUltimasVendas = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        categoria = new javax.swing.JTextField();
        btCancelar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        descricao_curta = new javax.swing.JTextArea();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        preco_promocional = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        prom_sim = new javax.swing.JRadioButton();
        prom_nao = new javax.swing.JRadioButton();
        jLabel20 = new javax.swing.JLabel();
        ativo_sim = new javax.swing.JRadioButton();
        ativo_nao = new javax.swing.JRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setClosable(true);
        setIconifiable(true);
        setTitle("Produtos");

        sem_imagem.setBackground(new java.awt.Color(255, 255, 255));

        pesquisar.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        pesquisar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pesquisarActionPerformed(evt);
            }
        });
        pesquisar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                pesquisarKeyReleased(evt);
            }
        });

        pesquisa.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pesquisaMouseClicked(evt);
            }
        });

        codigo.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        codigo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                codigoKeyReleased(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Inter", 0, 24)); // NOI18N
        jLabel3.setText("Código");

        jLabel11.setFont(new java.awt.Font("Inter", 0, 24)); // NOI18N
        jLabel11.setText("Produto");

        nomeProduto.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        nomeProduto.setDisabledTextColor(new java.awt.Color(51, 51, 51));

        jLabel5.setFont(new java.awt.Font("Inter", 0, 24)); // NOI18N
        jLabel5.setText("Preço:");

        preco.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        preco.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        preco.setEnabled(false);

        jLabel4.setFont(new java.awt.Font("Inter", 0, 24)); // NOI18N
        jLabel4.setText("Descrição");

        descricao.setColumns(20);
        descricao.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        descricao.setRows(5);
        descricao.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        descricao.setEnabled(false);
        jScrollPane1.setViewportView(descricao);

        jLabel6.setFont(new java.awt.Font("Inter", 0, 24)); // NOI18N
        jLabel6.setText("Marca");

        marca.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        marca.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        marca.setEnabled(false);

        estoque.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        estoque.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        estoque.setEnabled(false);

        jLabel7.setFont(new java.awt.Font("Inter", 0, 24)); // NOI18N
        jLabel7.setText("Estoque");

        btUltimasVendas.setBackground(new java.awt.Color(204, 204, 255));
        btUltimasVendas.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        btUltimasVendas.setForeground(new java.awt.Color(51, 51, 51));
        btUltimasVendas.setText("Últimas vendas");
        btUltimasVendas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btUltimasVendasActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Inter", 0, 24)); // NOI18N
        jLabel9.setText("Categoria");

        categoria.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        categoria.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        categoria.setEnabled(false);

        btCancelar.setBackground(new java.awt.Color(169, 169, 169));
        btCancelar.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        btCancelar.setForeground(new java.awt.Color(255, 255, 255));
        btCancelar.setText("Cancelar");
        btCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelarActionPerformed(evt);
            }
        });

        descricao_curta.setColumns(20);
        descricao_curta.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        descricao_curta.setRows(5);
        descricao_curta.setEnabled(false);
        jScrollPane2.setViewportView(descricao_curta);

        jLabel12.setFont(new java.awt.Font("Inter", 0, 24)); // NOI18N
        jLabel12.setText("Descrição curta");

        jLabel13.setFont(new java.awt.Font("Inter", 0, 24)); // NOI18N
        jLabel13.setText("Preço promocional");

        preco_promocional.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        preco_promocional.setEnabled(false);

        jLabel8.setFont(new java.awt.Font("Inter", 0, 24)); // NOI18N
        jLabel8.setText("Promoção");

        prom_sim.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        prom_sim.setText("Sim");
        prom_sim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prom_simActionPerformed(evt);
            }
        });

        prom_nao.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        prom_nao.setText("Não");
        prom_nao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prom_naoActionPerformed(evt);
            }
        });

        jLabel20.setFont(new java.awt.Font("Inter", 0, 24)); // NOI18N
        jLabel20.setText("Ativo");

        ativo_sim.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        ativo_sim.setText("Sim");
        ativo_sim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ativo_simActionPerformed(evt);
            }
        });

        ativo_nao.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        ativo_nao.setText("Não");
        ativo_nao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ativo_naoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(sem_imagem, javax.swing.GroupLayout.PREFERRED_SIZE, 343, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(pesquisar)
                                .addGap(10, 10, 10)
                                .addComponent(pesquisa, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nomeProduto)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)
                                .addComponent(preco, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(codigo, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jScrollPane1)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(categoria))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(marca)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(estoque, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btUltimasVendas))
                            .addComponent(btCancelar)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel20)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ativo_sim, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ativo_nao, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(prom_sim, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(prom_nao, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(24, 24, 24)
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(preco_promocional, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(18, 18, 18))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sem_imagem, javax.swing.GroupLayout.PREFERRED_SIZE, 343, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(pesquisar)
                            .addComponent(pesquisa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel3)
                                .addComponent(codigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(36, 36, 36)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nomeProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(preco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel11)
                                .addComponent(jLabel5)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel20)
                                .addComponent(jLabel8)
                                .addComponent(ativo_nao)
                                .addComponent(ativo_sim)
                                .addComponent(prom_sim)
                                .addComponent(prom_nao))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(preco_promocional, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel13)))
                        .addGap(20, 20, 20)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel6)
                                .addComponent(marca))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel7)
                                .addComponent(estoque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btUltimasVendas, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel9)
                                    .addComponent(categoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(38, 38, 38)
                                .addComponent(btCancelar)))
                        .addGap(33, 33, 33))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
/**
     * Ação executada ao liberar a tecla Enter no campo de código do produto.
     * Pesquisa os detalhes do produto pelo código e atualiza a interface.
     *
     * @param evt Evento de tecla liberada.
     */
    private void codigoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_codigoKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_PRODUCT_BY_ID)) {
                stmt.setString(1, codigo.getText());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Mapeia os dados do produto
                        id_produto = rs.getString("id_produto");
                        Produto = rs.getString("produto");
                        Preco = NumberFormat.getCurrencyInstance().format(rs.getDouble("preco"));
                        PrecoPromocional = rs.getObject("preco_promocional") != null ? NumberFormat.getCurrencyInstance().format(rs.getDouble("preco_promocional")) : "";
                        Promocao = rs.getBoolean("promocao");
                        Descricao = rs.getString("descricao");
                        DescricaoCurta = rs.getString("descricao_curta");
                        Marca = rs.getString("marca");
                        Estoque = rs.getString("estoque");
                        Categoria = rs.getString("categoria");
                        Ativo = rs.getBoolean("ativo");
                        imagem1 = rs.getString("imagem_1");

                        carregarImagemURL(imagem1);

                        // Atualiza campos na interface
                        codigo.setText(id_produto);
                        nomeProduto.setText(Produto);
                        preco.setText(Preco);
                        preco_promocional.setText(PrecoPromocional);
                        prom_sim.setSelected(Promocao);
                        prom_nao.setSelected(!Promocao);
                        preco_promocional.setEnabled(Promocao);
                        descricao.setText(Descricao);
                        descricao_curta.setText(DescricaoCurta);
                        marca.setText(Marca);
                        estoque.setText(Estoque);
                        categoria.setText(Categoria);
                        ativo_sim.setSelected(Ativo);
                        ativo_nao.setSelected(!Ativo);

                    } else {
                        LOGGER.warning("Produto não encontrado para o nome: " + pesquisar.getText());
                        funcoes.Avisos("erro.png", "Produto não encontrado");
                    }
                }
            } catch (SQLException e) {
                LOGGER.severe("Erro ao conectar ao banco de dados: " + e.getMessage());
                JOptionPane.showMessageDialog(null, ERROR_DB_CONNECTION + e.getMessage());
            } catch (Exception e) {
                LOGGER.severe("Erro inesperado: " + e.getMessage());
                JOptionPane.showMessageDialog(null, ERROR_GENERIC + e.getMessage());
            }
        }

    }//GEN-LAST:event_codigoKeyReleased
    /**
     * Ação executada ao clicar no botão "Últimas Vendas". (Método vazio,
     * aguardando implementação.)
     *
     * @param evt Evento de ação do botão.
     */

    private void btUltimasVendasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btUltimasVendasActionPerformed
        LOGGER.info("Botão 'Últimas Vendas' clicado. Aguardando implementação.");
        funcoes.Avisos("sinal-de-aviso.png","Lógica não implementada ainda");
    }//GEN-LAST:event_btUltimasVendasActionPerformed
    /**
     * Ação executada ao liberar uma tecla no campo de pesquisa. Filtra os nomes
     * de produtos e exibe sugestões em um menu suspenso.
     *
     * @param evt Evento de tecla liberada.
     */


    private void pesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pesquisarKeyReleased
        String pesquisa = pesquisar.getText().toLowerCase();
        LOGGER.info("Filtrando produtos com texto: " + pesquisa);
        sugestoesProdutos.setVisible(false);

        if (!pesquisa.isEmpty()) {
            // Filtra a lista usando Streams
            List<String> filtro = produtos.stream()
                    .filter(produto -> produto.toLowerCase().contains(pesquisa))
                    .collect(Collectors.toList());

            if (!filtro.isEmpty()) {
                sugestoesProdutos.removeAll();
                for (String p : filtro) {
                    JMenuItem item = new JMenuItem(p);
                    item.setFont(fonteItem);
                    item.addActionListener(e -> {
                        pesquisar.setText(p);
                        sugestoesProdutos.setVisible(false);
                        LOGGER.info("Produto selecionado nas sugestões: " + p);
                        pesquisaMouseClicked(null); // Dispara a pesquisa automaticamente
                    });
                    sugestoesProdutos.add(item);
                }
                sugestoesProdutos.show(pesquisar, 0, pesquisar.getHeight());
            }
        }


    }//GEN-LAST:event_pesquisarKeyReleased
    /**
     * Ação executada ao clicar no ícone de pesquisa. Pesquisa os detalhes do
     * produto pelo nome e atualiza a interface.
     *
     * @param evt Evento de clique do mouse.
     */

    private void pesquisaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pesquisaMouseClicked
        LOGGER.info("Pesquisando produto por nome: " + pesquisar.getText());
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_PRODUCT_BY_NAME)) {
            stmt.setString(1, pesquisar.getText());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Mapeia os dados do produto
                    id_produto = rs.getString("id_produto");
                    Produto = rs.getString("produto");
                    Preco = NumberFormat.getCurrencyInstance().format(rs.getDouble("preco"));
                    PrecoPromocional = rs.getObject("preco_promocional") != null ? NumberFormat.getCurrencyInstance().format(rs.getDouble("preco_promocional")) : "";
                    Promocao = rs.getBoolean("promocao");
                    Descricao = rs.getString("descricao");
                    DescricaoCurta = rs.getString("descricao_curta");
                    Marca = rs.getString("marca");
                    Estoque = rs.getString("estoque");
                    Categoria = rs.getString("categoria");
                    Ativo = rs.getBoolean("ativo");
                    imagem1 = rs.getString("imagem_1");

                    carregarImagemURL(imagem1);

                    // Atualiza campos na interface
                    codigo.setText(id_produto);
                    nomeProduto.setText(Produto);
                    preco.setText(Preco);
                    preco_promocional.setText(PrecoPromocional);
                    prom_sim.setSelected(Promocao);
                    prom_nao.setSelected(!Promocao);
                    preco_promocional.setEnabled(Promocao);
                    descricao.setText(Descricao);
                    descricao_curta.setText(DescricaoCurta);
                    marca.setText(Marca);
                    estoque.setText(Estoque);
                    categoria.setText(Categoria);
                    ativo_sim.setSelected(Ativo);
                    ativo_nao.setSelected(!Ativo);

                } else {
                    LOGGER.warning("Produto não encontrado para o nome: " + pesquisar.getText());
                    funcoes.Avisos("erro.png", "Produto não encontrado");
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Erro ao conectar ao banco de dados: " + e.getMessage());
            JOptionPane.showMessageDialog(null, ERROR_DB_CONNECTION + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Erro inesperado: " + e.getMessage());
            JOptionPane.showMessageDialog(null, ERROR_GENERIC + e.getMessage());
        }

    }//GEN-LAST:event_pesquisaMouseClicked
    /**
     * Ação executada ao clicar no botão "Cancelar". Limpa todos os campos da
     * interface e restaura a imagem padrão.
     *
     * @param evt Evento de ação do botão.
     */

    private void btCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelarActionPerformed
        LOGGER.info("Cancelando e limpando campos da interface.");
        codigo.setText("");
        pesquisar.setText("");
        nomeProduto.setText("");
        preco.setText("");
        preco_promocional.setText("");
        prom_sim.setSelected(false);
        prom_nao.setSelected(false);
        preco_promocional.setEnabled(false);
        descricao.setText("");
        descricao_curta.setText("");
        marca.setText("");
        estoque.setText("");
        categoria.setText("");
        ativo_sim.setSelected(false);
        ativo_nao.setSelected(false);
        sem_imagem.setIcon(sem_imagem());


    }//GEN-LAST:event_btCancelarActionPerformed

    private void prom_simActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prom_simActionPerformed
        LOGGER.info("Promoção marcada como 'Sim'.");
        Promocao = true;
        preco_promocional.setEnabled(true);

    }//GEN-LAST:event_prom_simActionPerformed

    private void prom_naoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prom_naoActionPerformed
        LOGGER.info("Promoção marcada como 'Não'.");
        Promocao = false;
        preco_promocional.setEnabled(false);
        preco_promocional.setText("");

    }//GEN-LAST:event_prom_naoActionPerformed

    private void ativo_simActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ativo_simActionPerformed
        LOGGER.info("Produto marcado como 'Ativo'.");
        Ativo = true;


    }//GEN-LAST:event_ativo_simActionPerformed

    private void ativo_naoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ativo_naoActionPerformed
        LOGGER.info("Produto marcado como 'Inativo'.");
        Ativo = false;

    }//GEN-LAST:event_ativo_naoActionPerformed

    private void pesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pesquisarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pesquisarActionPerformed
    /**
     * Ação executada ao liberar uma tecla no campo de nome do produto. Aplica a
     * máscara ao texto inserido.
     *
     * @param evt Evento de tecla liberada.
     */
    private void nomeProdutoKeyReleased(java.awt.event.KeyEvent evt) {
        atualizarMascara();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton ativo_nao;
    private javax.swing.JRadioButton ativo_sim;
    private javax.swing.JButton btCancelar;
    private javax.swing.JButton btUltimasVendas;
    private javax.swing.JTextField categoria;
    private javax.swing.JTextField codigo;
    private javax.swing.JTextArea descricao;
    private javax.swing.JTextArea descricao_curta;
    private javax.swing.JTextField estoque;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField marca;
    private javax.swing.JTextField nomeProduto;
    private javax.swing.JLabel pesquisa;
    private javax.swing.JTextField pesquisar;
    private javax.swing.JTextField preco;
    private javax.swing.JTextField preco_promocional;
    private javax.swing.JRadioButton prom_nao;
    private javax.swing.JRadioButton prom_sim;
    private javax.swing.JLabel sem_imagem;
    // End of variables declaration//GEN-END:variables

}
