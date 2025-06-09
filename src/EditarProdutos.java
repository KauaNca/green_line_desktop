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
import java.net.URL;
import javax.swing.ImageIcon;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
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
import javax.swing.ButtonGroup;

/**
 * JInternalFrame para edição de produtos no sistema. Permite buscar produtos
 * pelo nome ou ID, exibir detalhes, editar campos e atualizar no banco de
 * dados.
 *
 * @author Kaua33500476
 */
public class EditarProdutos extends javax.swing.JInternalFrame {

    // Logger para rastreamento de eventos e erros
    private static final Logger LOGGER = Logger.getLogger(EditarProdutos.class.getName());

    // Constantes para query SQL e caminhos de imagens
    private static final String SELECT_PRODUCT_NAMES = "SELECT produto FROM produto WHERE LOWER(produto) LIKE ? AND ativo = TRUE";
    private static final String DEFAULT_IMAGE_PATH = "imagens/sem_imagem.jpg";
    private static final String PRODUCT_IMAGE_PATH = "imagens/produtos/";
    private static final String RIGHT_ARROW_PATH = "imagens/seta-direita.png";
    private static final String SEARCH_ICON_PATH = "imagens/lupa.png";
    private static final String SELECT_PRODUCT_BY_ID = "SELECT id_produto, produto, descricao, descricao_curta, preco, preco_promocional, promocao, marca, avaliacao, quantidade_avaliacoes, estoque, parcelas_permitidas, peso_kg, dimensoes, ativo, imagem_1, imagem_2, imagem_3, imagem_4, categoria FROM produto WHERE id_produto = ? AND ativo = TRUE";
    private static final String SELECT_PRODUCT_BY_NAME = "SELECT id_produto, produto, descricao, descricao_curta, preco, preco_promocional, promocao, marca, avaliacao, quantidade_avaliacoes, estoque, parcelas_permitidas, peso_kg, dimensoes, ativo, imagem_1, imagem_2, imagem_3, imagem_4, categoria FROM produto WHERE produto = ? AND ativo = TRUE";
    private static final String UPDATE_PRODUCT = "UPDATE produto SET produto = ?, descricao = ?, descricao_curta = ?, preco = ?, preco_promocional = ?, promocao = ?, marca = ?, avaliacao = ?, quantidade_avaliacoes = ?, estoque = ?, parcelas_permitidas = ?, peso_kg = ?, dimensoes = ?, ativo = ?, categoria = ?, data_alteracao = NOW() WHERE id_produto = ?";
    private static final String SELECT_CATEGORIES = "SELECT categoria FROM categorias ORDER BY categoria";
    private static final String ERROR_DB_CONNECTION = "Erro ao conectar ao banco de dados: ";
    private static final String ERROR_GENERIC = "Erro: ";

    // Variáveis de estado
    private int contagem = 0;
    private File arquivo;
    private boolean atualizandoMascara = false;
    private String[] enderecosImagens = new String[4]; // Suporta até 4 imagens
    private String id_produto;
    private String Produto;
    private String Preco;
    private String PrecoPromocional;
    private boolean Promocao;
    private String Descricao;
    private String DescricaoCurta;
    private String Marca;
    private String Avaliacao;
    private String QuantidadeAvaliacoes;
    private String Estoque;
    private String ParcelasPermitidas;
    private String Peso;
    private String Dimensoes;
    private String Categoria;
    private boolean Ativo;
    private final JPopupMenu sugestoesProdutos = new JPopupMenu();
    private List<String> produtos;
    private final Font fonteItem = new Font("Arial", Font.PLAIN, 15);

    // Timer para slideshow de imagens
    private final Timer slide = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            LOGGER.info("Alternando imagem no slideshow. Contagem: " + contagem);
            // Filtra imagens não nulas
            List<String> imagensValidas = new ArrayList<>();
            for (String img : enderecosImagens) {
                if (img != null && !img.isEmpty()) {
                    imagensValidas.add(img);
                }
            }
            if (imagensValidas.isEmpty()) {
                LOGGER.warning("Nenhuma imagem válida para o slideshow.");
                slide.stop();
                return;
            }

            // Lógica para alternar as imagens
            contagem = (contagem + 1) % imagensValidas.size();
            ImageIcon proximaImagem;
            if (imagensValidas.get(contagem).contains("http")) {
                try {
                    URL url = new URL(imagensValidas.get(contagem));
                    BufferedImage image = ImageIO.read(url);
                    proximaImagem = new ImageIcon(image);
                } catch (Exception ex) {
                    LOGGER.warning("Erro ao carregar imagem de URL: " + imagensValidas.get(contagem));
                    proximaImagem = sem_imagem();
                }
            } else {
                proximaImagem = new ImageIcon(PRODUCT_IMAGE_PATH + imagensValidas.get(contagem));
            }

            if (proximaImagem.getIconWidth() == -1) {
                LOGGER.warning("Imagem não encontrada: " + imagensValidas.get(contagem));
                proximaImagem = sem_imagem();
            }
            sem_imagem.setIcon(redimensionamentoDeImagem(proximaImagem, 245, 270));
        }
    });

    /**
     * Construtor da classe EditarProdutos. Inicializa a interface, configura
     * ícones padrão e carrega a lista de nomes de produtos.
     */
    public EditarProdutos() {
        initComponents();
        Inicio();
        nomesProdutos();
        carregarCategorias();

        // Adiciona listener para clique na seta do slideshow
        seta.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                List<String> imagensValidas = new ArrayList<>();
                for (String img : enderecosImagens) {
                    if (img != null && !img.isEmpty()) {
                        imagensValidas.add(img);
                    }
                }
                if (!imagensValidas.isEmpty()) {
                    LOGGER.info("Iniciando slideshow de imagens.");
                    slide.start();
                } else {
                    LOGGER.warning("Nenhuma imagem válida para iniciar o slideshow.");
                }
            }
        });

        // Adiciona listener para nomeProduto
        nomeProduto.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                nomeProdutoKeyReleased(evt);
            }
        });
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

    /**
     * Carrega as categorias do banco de dados para o JComboBox.
     */
    private void carregarCategorias() {
        LOGGER.info("Carregando categorias do banco de dados.");
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_CATEGORIES)) {
            try (ResultSet rs = stmt.executeQuery()) {
                categorias.removeAllItems();
                while (rs.next()) {
                    categorias.addItem(rs.getString("categoria"));
                }
                LOGGER.info("Categorias carregadas: " + categorias.getItemCount());
            }
        } catch (SQLException ex) {
            LOGGER.severe("Erro ao carregar categorias: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, ERROR_DB_CONNECTION + ex.getMessage());
        }
    }

    /**
     * Inicializa a interface desabilitando campos de texto, configurando ícones
     * padrão e ocultando componentes de imagem.
     */
    public void Inicio() {
        LOGGER.info("Inicializando interface de edição de produtos.");
        // Desabilita campos
        nomeProduto.setEnabled(false);
        preco.setEnabled(false);
        preco_promocional.setEnabled(false);
        descricao.setEnabled(false);
        descricao_curta.setEnabled(false);
        marca.setEnabled(false);
        avaliacao.setEnabled(false);
        quantidade_avaliacao.setEnabled(false);
        estoque.setEnabled(false);
        parcelas.setEnabled(false);
        peso.setEnabled(false);
        dimensoes.setEnabled(false);
        categorias.setEnabled(false);
        prom_sim.setEnabled(false);
        prom_nao.setEnabled(false);
        ativo_sim.setEnabled(false);
        ativo_nao.setEnabled(false);
        // Configura ícones
        sem_imagem.setIcon(sem_imagem());
        seta.setIcon(new ImageIcon(RIGHT_ARROW_PATH));
        pesquisa.setIcon(new ImageIcon(SEARCH_ICON_PATH));
        seta.setVisible(false);
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
        return redimensionamentoDeImagem(imagem, 245, 270);
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

    /**
     * Carrega os detalhes do produto a partir do ID ou nome.
     *
     * @param query Consulta SQL (SELECT_PRODUCT_BY_ID ou SELECT_PRODUCT_BY_NAME).
     * @param parametro Parâmetro para a consulta (ID ou nome do produto).
     */
    private void carregarProduto(String query, String parametro) {
        LOGGER.info("Carregando produto com query: " + query + ", parâmetro: " + parametro);
        seta.setVisible(true);
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, parametro);
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
                    Avaliacao = rs.getObject("avaliacao") != null ? rs.getString("avaliacao") : "";
                    QuantidadeAvaliacoes = rs.getString("quantidade_avaliacoes");
                    Estoque = rs.getString("estoque");
                    ParcelasPermitidas = rs.getString("parcelas_permitidas");
                    Peso = rs.getObject("peso_kg") != null ? rs.getString("peso_kg") : "";
                    Dimensoes = rs.getString("dimensoes");
                    Categoria = rs.getString("categoria");
                    Ativo = rs.getBoolean("ativo");
                    enderecosImagens[0] = rs.getString("imagem_1");
                    enderecosImagens[1] = rs.getString("imagem_2");
                    enderecosImagens[2] = rs.getString("imagem_3");
                    enderecosImagens[3] = rs.getString("imagem_4");

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
                    avaliacao.setText(Avaliacao);
                    quantidade_avaliacao.setText(QuantidadeAvaliacoes);
                    estoque.setText(Estoque);
                    parcelas.setSelectedItem(ParcelasPermitidas);
                    peso.setText(Peso);
                    dimensoes.setText(Dimensoes);
                    if (Categoria != null) {
                        categorias.setSelectedItem(Categoria);
                    }
                    ativo_sim.setSelected(Ativo);
                    ativo_nao.setSelected(!Ativo);

                    // Habilita campos para edição
                    nomeProduto.setEnabled(true);
                    preco.setEnabled(true);
                    preco_promocional.setEnabled(Promocao);
                    descricao.setEnabled(true);
                    descricao_curta.setEnabled(true);
                    marca.setEnabled(true);
                    avaliacao.setEnabled(true);
                    quantidade_avaliacao.setEnabled(true);
                    estoque.setEnabled(true);
                    parcelas.setEnabled(true);
                    peso.setEnabled(true);
                    dimensoes.setEnabled(true);
                    categorias.setEnabled(true);
                    prom_sim.setEnabled(true);
                    prom_nao.setEnabled(true);
                    ativo_sim.setEnabled(true);
                    ativo_nao.setEnabled(true);

                    // Exibe a primeira imagem redimensionada
                    if (enderecosImagens[0] != null && !enderecosImagens[0].isEmpty()) {
                        if (enderecosImagens[0].contains("http")) {
                            try {
                                URL url = new URL(enderecosImagens[0]);
                                BufferedImage image = ImageIO.read(url);
                                sem_imagem.setIcon(redimensionamentoDeImagem(new ImageIcon(image), 245, 270));
                                LOGGER.info("Imagem do produto carregada de URL: " + enderecosImagens[0]);
                            } catch (Exception ex) {
                                LOGGER.warning("Erro ao carregar imagem de URL: " + enderecosImagens[0]);
                                sem_imagem.setIcon(sem_imagem());
                            }
                        } else {
                            sem_imagem.setIcon(redimensionamentoDeImagem(
                                    new ImageIcon(PRODUCT_IMAGE_PATH + enderecosImagens[0]), 245, 270));
                            LOGGER.info("Imagem do produto carregada: " + enderecosImagens[0]);
                        }
                    } else {
                        sem_imagem.setIcon(sem_imagem());
                    }
                } else {
                    LOGGER.warning("Produto não encontrado para o parâmetro: " + parametro);
                    new CadastroProdutos().Avisos("imagens/erro.png", "Produto não encontrado");
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

    @SuppressWarnings("unchecked")
    private void initComponents() {
        // Agrupando os JRadioButtons
        ButtonGroup promocaoGroup = new ButtonGroup();
        ButtonGroup ativoGroup = new ButtonGroup();

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
        btUltimasVendas = new javax.swing.JButton();
        seta = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        descricao_curta = new javax.swing.JTextArea();
        jLabel12 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        prom_sim = new javax.swing.JRadioButton();
        prom_nao = new javax.swing.JRadioButton();
        jLabel14 = new javax.swing.JLabel();
        preco_promocional = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        marca = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        avaliacao = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        quantidade_avaliacao = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        parcelas = new javax.swing.JComboBox<>();
        peso = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        dimensoes = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        ativo_sim = new javax.swing.JRadioButton();
        ativo_nao = new javax.swing.JRadioButton();
        estoque = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        categorias = new javax.swing.JComboBox<>();
        btCancelar = new javax.swing.JButton();
        btSalvar = new javax.swing.JButton();

        // Adiciona JRadioButtons aos grupos
        promocaoGroup.add(prom_sim);
        promocaoGroup.add(prom_nao);
        ativoGroup.add(ativo_sim);
        ativoGroup.add(ativo_nao);

        setBackground(new java.awt.Color(255, 255, 255));
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Produtos");

        sem_imagem.setBackground(new java.awt.Color(255, 255, 255));

        pesquisar.setFont(new java.awt.Font("Arial", 0, 20));
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

        codigo.setFont(new java.awt.Font("Arial", 0, 20));
        codigo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                codigoKeyReleased(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Arial", 0, 21));
        jLabel3.setText("Código");

        jLabel11.setFont(new java.awt.Font("Arial", 0, 21));
        jLabel11.setText("Produto");

        nomeProduto.setFont(new java.awt.Font("Arial", 0, 20));
        nomeProduto.setDisabledTextColor(new java.awt.Color(51, 51, 51));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 21));
        jLabel5.setText("Preço:");

        preco.setFont(new java.awt.Font("Arial", 0, 20));
        preco.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        preco.setEnabled(false);

        jLabel4.setFont(new java.awt.Font("Arial", 0, 21));
        jLabel4.setText("Descrição");

        descricao.setColumns(20);
        descricao.setFont(new java.awt.Font("Arial", 0, 20));
        descricao.setRows(5);
        descricao.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        descricao.setEnabled(false);
        jScrollPane1.setViewportView(descricao);

        btUltimasVendas.setBackground(new java.awt.Color(204, 204, 255));
        btUltimasVendas.setFont(new java.awt.Font("Arial", 0, 21));
        btUltimasVendas.setForeground(new java.awt.Color(51, 51, 51));
        btUltimasVendas.setText("Últimas vendas");
        btUltimasVendas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btUltimasVendasActionPerformed(evt);
            }
        });

        seta.setPreferredSize(new java.awt.Dimension(24, 24));

        descricao_curta.setColumns(20);
        descricao_curta.setFont(new java.awt.Font("Arial", 0, 20));
        descricao_curta.setRows(5);
        descricao_curta.setEnabled(false);
        jScrollPane2.setViewportView(descricao_curta);

        jLabel12.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel12.setText("Descrição curta");

        jLabel10.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel10.setText("Promoção");

        prom_sim.setText("Sim");
        prom_sim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prom_simActionPerformed(evt);
            }
        });

        prom_nao.setText("Não");
        prom_nao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prom_naoActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel14.setText("Preço promocional");

        preco_promocional.setFont(new java.awt.Font("Arial", 0, 20));

        jLabel6.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel6.setText("Marca:");

        marca.setFont(new java.awt.Font("Arial", 0, 20));

        jLabel15.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel15.setText("Avaliação");

        avaliacao.setFont(new java.awt.Font("Arial", 0, 20));

        jLabel16.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel16.setText("Quantidade de avaliações");

        quantidade_avaliacao.setFont(new java.awt.Font("Arial", 0, 20));

        jLabel17.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel17.setText("Parcelas");

        parcelas.setFont(new java.awt.Font("Arial", 0, 20));
        parcelas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }));

        peso.setFont(new java.awt.Font("Arial", 0, 20));

        jLabel18.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel18.setText("Peso (kg)");

        jLabel19.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel19.setText("Dimensões");

        dimensoes.setFont(new java.awt.Font("Arial", 0, 20));

        jLabel20.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel20.setText("Ativo");

        ativo_sim.setText("Sim");
        ativo_sim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ativo_simActionPerformed(evt);
            }
        });

        ativo_nao.setText("Não");
        ativo_nao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ativo_naoActionPerformed(evt);
            }
        });

        estoque.setFont(new java.awt.Font("Arial", 0, 20));

        jLabel7.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel7.setText("Estoque");

        jLabel9.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel9.setText("Categoria:");

        categorias.setFont(new java.awt.Font("Arial", 0, 20));

        btCancelar.setBackground(new java.awt.Color(169, 169, 169));
        btCancelar.setFont(new java.awt.Font("Arial", 0, 20));
        btCancelar.setForeground(new java.awt.Color(255, 255, 255));
        btCancelar.setText("Cancelar");
        btCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelarActionPerformed(evt);
            }
        });

        btSalvar.setBackground(new java.awt.Color(43, 189, 49));
        btSalvar.setFont(new java.awt.Font("Arial", 0, 20));
        btSalvar.setForeground(new java.awt.Color(255, 255, 255));
        btSalvar.setText("Salvar");
        btSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSalvarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sem_imagem, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(seta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(53, 53, 53)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(pesquisar)
                                .addGap(10, 10, 10)
                                .addComponent(pesquisa, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nomeProduto, javax.swing.GroupLayout.DEFAULT_SIZE, 661, Short.MAX_VALUE)))
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
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel12)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 960, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(marca, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel15)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(avaliacao, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel10)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(prom_sim, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(prom_nao)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel16)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(quantidade_avaliacao, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel14)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(preco_promocional, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel17)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(parcelas, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel18))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel20)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(ativo_sim, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(ativo_nao)
                                        .addGap(29, 29, 29)
                                        .addComponent(jLabel7)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(estoque, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(categorias, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(peso, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel19)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(dimensoes, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btUltimasVendas)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btSalvar)
                                .addGap(18, 18, 18)
                                .addComponent(btCancelar)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(18, 18, 18))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(pesquisar)
                            .addComponent(pesquisa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel3)
                                .addComponent(codigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(36, 36, 36)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(preco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11)
                            .addComponent(nomeProduto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel10)
                                .addComponent(prom_sim)
                                .addComponent(prom_nao))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(preco_promocional, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel14)))
                        .addGap(31, 31, 31)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(marca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel15)
                                .addComponent(avaliacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel16)
                                .addComponent(quantidade_avaliacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(33, 33, 33)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel17)
                                .addComponent(parcelas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel18)
                            .addComponent(peso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel19)
                            .addComponent(dimensoes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel20)
                            .addComponent(ativo_sim)
                            .addComponent(ativo_nao)
                            .addComponent(jLabel7)
                            .addComponent(estoque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)
                            .addComponent(categorias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sem_imagem, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btUltimasVendas, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btCancelar)
                        .addComponent(btSalvar)))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        pack();
    }

    private void codigoKeyReleased(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            carregarProduto(SELECT_PRODUCT_BY_ID, codigo.getText());
        }
    }

    private void btUltimasVendasActionPerformed(java.awt.event.ActionEvent evt) {
        LOGGER.info("Botão 'Últimas Vendas' clicado. Aguardando implementação.");
        // TODO add your handling code here:
    }

    private void pesquisarKeyReleased(java.awt.event.KeyEvent evt) {
        String pesquisa = pesquisar.getText().toLowerCase();
        LOGGER.info("Filtrando produtos com texto: " + pesquisa);
        sugestoesProdutos.setVisible(false);
        seta.setVisible(true);
        if (!pesquisa.isEmpty()) {
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
                        pesquisaMouseClicked(null);
                    });
                    sugestoesProdutos.add(item);
                }
                sugestoesProdutos.show(pesquisar, 0, pesquisar.getHeight());
            }
        }
    }

    private void pesquisaMouseClicked(java.awt.event.MouseEvent evt) {
        carregarProduto(SELECT_PRODUCT_BY_NAME, pesquisar.getText());
    }

    private void prom_simActionPerformed(java.awt.event.ActionEvent evt) {
        LOGGER.info("Promoção marcada como 'Sim'.");
        Promocao = true;
        preco_promocional.setEnabled(true);
    }

    private void prom_naoActionPerformed(java.awt.event.ActionEvent evt) {
        LOGGER.info("Promoção marcada como 'Não'.");
        Promocao = false;
        preco_promocional.setEnabled(false);
    }

    private void ativo_simActionPerformed(java.awt.event.ActionEvent evt) {
        LOGGER.info("Produto marcado como 'Ativo'.");
        Ativo = true;
    }

    private void ativo_naoActionPerformed(java.awt.event.ActionEvent evt) {
        LOGGER.info("Produto marcado como 'Inativo'.");
        Ativo = false;
    }

    private void nomeProdutoKeyReleased(java.awt.event.KeyEvent evt) {
        atualizarMascara();
    }

    private void btSalvarActionPerformed(java.awt.event.ActionEvent evt) {
        LOGGER.info("Salvando alterações do produto: " + nomeProduto.getText());
        if (id_produto == null || id_produto.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nenhum produto selecionado para edição.");
            return;
        }

        // Validação dos campos obrigatórios
        if (nomeProduto.getText().isEmpty() || preco.getText().isEmpty()
                || descricao.getText().isEmpty() || estoque.getText().isEmpty()
                || categorias.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios: Nome, Preço, Descrição, Estoque e Categoria.");
            return;
        }

        // Validação de formatos numéricos
        try {
            if (!preco.getText().isEmpty()) {
                NumberFormat.getCurrencyInstance().parse(preco.getText()).doubleValue();
            }
            if (Promocao && !preco_promocional.getText().isEmpty()) {
                NumberFormat.getCurrencyInstance().parse(preco_promocional.getText()).doubleValue();
            }
            if (!avaliacao.getText().isEmpty()) {
                Double.parseDouble(avaliacao.getText());
            }
            if (!quantidade_avaliacao.getText().isEmpty()) {
                Integer.parseInt(quantidade_avaliacao.getText());
            }
            if (!estoque.getText().isEmpty()) {
                Integer.parseInt(estoque.getText());
            }
            if (!peso.getText().isEmpty()) {
                Double.parseDouble(peso.getText());
            }
        } catch (ParseException | NumberFormatException e) {
            LOGGER.severe("Erro nos formatos de número: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Verifique os formatos de preço, avaliação, quantidade de avaliações, estoque ou peso.");
            return;
        }

        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(UPDATE_PRODUCT)) {
            stmt.setString(1, nomeProduto.getText());
            stmt.setString(2, descricao.getText());
            stmt.setString(3, descricao_curta.getText());

            // Converte preço
            double precoValue = NumberFormat.getCurrencyInstance().parse(preco.getText()).doubleValue();
            stmt.setDouble(4, precoValue);

            // Converte preço promocional
            if (Promocao && !preco_promocional.getText().isEmpty()) {
                double precoPromocionalValue = NumberFormat.getCurrencyInstance().parse(preco_promocional.getText()).doubleValue();
                stmt.setDouble(5, precoPromocionalValue);
            } else {
                stmt.setNull(5, java.sql.Types.DECIMAL);
            }

            stmt.setBoolean(6, Promocao);
            stmt.setString(7, marca.getText().isEmpty() ? null : marca.getText());

            // Converte avaliação
            if (!avaliacao.getText().isEmpty()) {
                stmt.setDouble(8, Double.parseDouble(avaliacao.getText()));
            } else {
                stmt.setNull(8, java.sql.Types.DECIMAL);
            }

            // Converte quantidade de avaliações
            stmt.setInt(9, Integer.parseInt(quantidade_avaliacao.getText().isEmpty() ? "0" : quantidade_avaliacao.getText()));

            // Converte estoque
            stmt.setInt(10, Integer.parseInt(estoque.getText()));

            // Converte parcelas
            stmt.setInt(11, Integer.parseInt((String) parcelas.getSelectedItem()));

            // Converte peso
            if (!peso.getText().isEmpty()) {
                stmt.setDouble(12, Double.parseDouble(peso.getText()));
            } else {
                stmt.setNull(12, java.sql.Types.DECIMAL);
            }

            stmt.setString(13, dimensoes.getText().isEmpty() ? null : dimensoes.getText());
            stmt.setBoolean(14, Ativo);
            stmt.setString(15, (String) categorias.getSelectedItem());
            stmt.setString(16, id_produto);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("Produto atualizado com sucesso: " + id_produto);
                JOptionPane.showMessageDialog(null, "Produto atualizado com sucesso!");
                btCancelarActionPerformed(null);
            } else {
                LOGGER.warning("Nenhuma alteração realizada para o produto: " + id_produto);
                JOptionPane.showMessageDialog(null, "Nenhuma alteração realizada.");
            }
        } catch (SQLException e) {
            LOGGER.severe("Erro ao atualizar produto: " + e.getMessage());
            JOptionPane.showMessageDialog(null, ERROR_DB_CONNECTION + e.getMessage());
        } catch (ParseException e) {
            LOGGER.severe("Erro ao converter valores numéricos: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Erro nos valores de preço, avaliação ou peso.");
        }
    }

    private void btCancelarActionPerformed(java.awt.event.ActionEvent evt) {
        LOGGER.info("Cancelando e limpando campos da interface.");
        id_produto = null;
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
        avaliacao.setText("");
        quantidade_avaliacao.setText("");
        estoque.setText("");
        parcelas.setSelectedIndex(0);
        peso.setText("");
        dimensoes.setText("");
        categorias.setSelectedIndex(-1);
        ativo_sim.setSelected(false);
        ativo_nao.setSelected(false);
        sem_imagem.setIcon(sem_imagem());
        seta.setVisible(false);
        slide.stop();

        // Desabilita campos
        nomeProduto.setEnabled(false);
        preco.setEnabled(false);
        preco_promocional.setEnabled(false);
        descricao.setEnabled(false);
        descricao_curta.setEnabled(false);
        marca.setEnabled(false);
        avaliacao.setEnabled(false);
        quantidade_avaliacao.setEnabled(false);
        estoque.setEnabled(false);
        parcelas.setEnabled(false);
        peso.setEnabled(false);
        dimensoes.setEnabled(false);
        categorias.setEnabled(false);
        prom_sim.setEnabled(false);
        prom_nao.setEnabled(false);
        ativo_sim.setEnabled(false);
        ativo_nao.setEnabled(false);
    }

    // Variables declaration
    private javax.swing.JRadioButton ativo_nao;
    private javax.swing.JRadioButton ativo_sim;
    private javax.swing.JTextField avaliacao;
    private javax.swing.JButton btCancelar;
    private javax.swing.JButton btSalvar;
    private javax.swing.JButton btUltimasVendas;
    private javax.swing.JComboBox<String> categorias;
    private javax.swing.JTextField codigo;
    private javax.swing.JTextArea descricao;
    private javax.swing.JTextArea descricao_curta;
    private javax.swing.JTextField dimensoes;
    private javax.swing.JTextField estoque;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField marca;
    private javax.swing.JTextField nomeProduto;
    private javax.swing.JComboBox<String> parcelas;
    private javax.swing.JTextField peso;
    private javax.swing.JLabel pesquisa;
    private javax.swing.JTextField pesquisar;
    private javax.swing.JTextField preco;
    private javax.swing.JTextField preco_promocional;
    private javax.swing.JRadioButton prom_nao;
    private javax.swing.JRadioButton prom_sim;
    private javax.swing.JTextField quantidade_avaliacao;
    private javax.swing.JLabel sem_imagem;
    private javax.swing.JLabel seta;
}