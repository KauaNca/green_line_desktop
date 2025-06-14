
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.ImageIcon;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Random;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * JInternalFrame para cadastro de produtos no sistema. Permite adicionar novos
 * produtos com detalhes como nome, preço, descrição, imagens e categorias, com
 * validação de entrada, slideshow de imagens e armazenamento de imagens na
 * pasta 'imagens/produtos/'.
 *
 * @author Kaua33500476
 */
public class CadastroProduto extends javax.swing.JInternalFrame {

    // Logger para rastreamento de eventos e erros
    private static final Logger LOGGER = Logger.getLogger(CadastroProduto.class.getName());

    // Constantes para queries SQL, caminhos de imagens e mensagens
    private static final String SELECT_CATEGORY = "SELECT id_categoria, categoria FROM categorias";
    private static final String INSERT_PRODUCT = "INSERT INTO produto(produto, descricao, descricao_curta, preco, preco_promocional, promocao, "
            + "marca, avaliacao, quantidade_avaliacoes, estoque, parcelas_permitidas, peso_kg, dimensoes, ativo, imagem_1, imagem_2, categoria) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String CHECK_PRODUCT = "SELECT produto FROM produto WHERE produto = ?";
    private static final String DEFAULT_IMAGE_PATH = "imagens/sem_imagem.jpg";
    private static final String RIGHT_ARROW_PATH = "imagens/seta-direita.png";
    private static final String PRODUCT_IMAGE_PATH = "imagens/produtos/";
    private static final String ERROR_IMAGE_NOT_FOUND = "Imagem não encontrada";
    private static final String ERROR_DB_ACCESS = "Erro ao carregar dados: ";
    private static final String ERROR_GENERIC = "Erro: ";

    // Variáveis de estado
    private int numeroImagens = 0;
    private int contagem = 0;
    private File arquivo;
    private String[] enderecosImagens = new String[2];
    private boolean atualizandoMascara = false;
    private boolean promocaoAtiva = false;
    private boolean produtoAtivo = true;

    // Timer para slideshow de imagens
    private final Timer slide = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            LOGGER.info("Alternando imagem no slideshow. Contagem: " + contagem);
            String enderecoImagem1 = imagem1.getText();
            String enderecoImagem2 = imagem2.getText();
            enderecosImagens = new String[]{enderecoImagem1, enderecoImagem2};
            contagem = (contagem + 1) % enderecosImagens.length;
            if (enderecosImagens[contagem] == null || enderecosImagens[contagem].isEmpty()) {
                LOGGER.warning("Nenhuma imagem válida para exibir no slideshow.");
                return;
            }
            ImageIcon proximaImagem;
            if (enderecosImagens[contagem].contains("http")) {
                try {
                    URL url = new URL(enderecosImagens[contagem]);
                    BufferedImage image = ImageIO.read(url);
                    proximaImagem = new ImageIcon(image);
                } catch (IOException ex) {
                    LOGGER.warning("Erro ao carregar imagem de URL: " + enderecosImagens[contagem]);
                    proximaImagem = sem_imagem();
                }
            } else {
                proximaImagem = new ImageIcon(PRODUCT_IMAGE_PATH + enderecosImagens[contagem]);
            }
            if (proximaImagem.getIconWidth() == -1) {
                LOGGER.warning(ERROR_IMAGE_NOT_FOUND + ": " + enderecosImagens[contagem]);
                proximaImagem = sem_imagem();
            }
            sem_imagem.setIcon(redimensionamentoDeImagem(proximaImagem, 245, 270));
        }
    });

    private javax.swing.JLabel jLabel9;

    /**
     * Construtor padrão. Inicializa a interface, configura ícones, aplica
     * máscaras de entrada e carrega categorias.
     */
    public CadastroProduto() {
        initComponents();
        LOGGER.info("Inicializando interface de cadastro de produtos.");
        jPanel1.setBackground(Color.WHITE);
        sem_imagem.setIcon(sem_imagem());
        passarImagem.setIcon(new ImageIcon(RIGHT_ARROW_PATH));
        atualizarMascara();
        applyNumberOnlyMask(estoqueInicial);
        applyNumberOnlyMask(quantidade_avaliacao);
        applyNumberOnlyMask(avaliacao);
        applyNumberOnlyMask(peso);
        applyMoneyMask();
        passarImagem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                LOGGER.info("Iniciando slideshow de imagens.");
                slide.start();
            }
        });
        carregarCategorias();
        preco_promocional.setEnabled(false); // Inicialmente desabilitado
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
     * Redimensiona uma imagem a partir de um arquivo para as dimensões
     * especificadas.
     *
     * @param arquivo Arquivo da imagem.
     * @return Ícone da imagem redimensionada.
     */
    public ImageIcon redimensionamentoDeImagem(File arquivo) {
        LOGGER.info("Redimensionando imagem do arquivo: " + arquivo.getPath());
        ImageIcon imagem = new ImageIcon(arquivo.getPath());
        return redimensionamentoDeImagem(imagem, 245, 270);
    }

    /**
     * Redimensiona uma imagem para as dimensões especificadas.
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
     * Salva uma imagem (local ou URL) na pasta 'imagens/produtos/' com um nome
     * único.
     *
     * @param source Pode ser um File (imagem local) ou String (URL da imagem).
     * @return Nome do arquivo salvo ou null se falhar.
     */
    private String salvarImagem(Object source) {
        LOGGER.info("Salvando imagem na pasta: " + PRODUCT_IMAGE_PATH);
        try {
            String extension = "jpg"; // Padrão
            File sourceFile = null;
            if (source instanceof File) {
                sourceFile = (File) source;
                String fileName = sourceFile.getName().toLowerCase();
                if (fileName.endsWith(".png")) {
                    extension = "png";
                } else if (fileName.endsWith(".jpeg")) {
                    extension = "jpeg";
                }
            } else if (source instanceof String) {
                String urlStr = (String) source;
                if (!urlStr.contains("http")) {
                    LOGGER.warning("URL inválida: " + urlStr);
                    return null;
                }
                URL url = new URL(urlStr);
                BufferedImage image = ImageIO.read(url);
                if (image == null) {
                    LOGGER.warning("Falha ao carregar imagem da URL: " + urlStr);
                    return null;
                }
                sourceFile = File.createTempFile("temp", "." + extension);
                ImageIO.write(image, extension, sourceFile);
            } else {
                LOGGER.warning("Fonte de imagem inválida.");
                return null;
            }

            // Gera nome único com timestamp e sufixo aleatório
            String timestamp = String.valueOf(System.currentTimeMillis());
            String randomSuffix = String.valueOf(new Random().nextInt(10000));
            String newFileName = "produto_" + timestamp + "_" + randomSuffix + "." + extension;
            Path destination = Paths.get(PRODUCT_IMAGE_PATH, newFileName);

            // Cria diretório se não existir
            Files.createDirectories(destination.getParent());

            // Copia arquivo
            Files.copy(sourceFile.toPath(), destination);
            LOGGER.info("Imagem salva com sucesso: " + newFileName);

            // Deleta arquivo temporário, se criado
            if (source instanceof String) {
                sourceFile.delete();
            }

            return newFileName;
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.severe("Erro ao salvar imagem: " + e.getMessage());
            Avisos("imagens/sinal-de-aviso.png", "Erro ao salvar imagem: " + e.getMessage());
            return null;
        }
    }

    /**
     * Valida os campos de imagens e solicita a seleção de imagens, se
     * necessário.
     *
     * @return 1 se as imagens são válidas, 0 se há problemas.
     */
    public int camposImagens() {
        LOGGER.info("Validando campos de imagens.");
        if (imagem1.getText().isBlank() && imagem2.getText().isBlank()) {
            LOGGER.warning("Ambos os campos de imagem estão vazios.");
            Avisos("imagens/sinal-de-aviso.png", "Selecione pelo menos uma imagem.");
            return 0;
        }

        if (imagem1.getText().isBlank()) {
            LOGGER.warning("Campo de imagem 1 está vazio.");
            Avisos("imagens/sinal-de-aviso.png", "Selecione uma imagem para o campo 1.");
            return 0;
        }

        if (imagem2.getText().isBlank()) {
            LOGGER.info("Campo de imagem 2 está vazio. Solicitando confirmação do usuário.");
            int resposta = JOptionPane.showConfirmDialog(
                    null,
                    "Deseja manter apenas uma imagem para o produto?",
                    "Imagem",
                    JOptionPane.YES_NO_OPTION
            );

            if (resposta == JOptionPane.NO_OPTION) {
                Avisos("imagens/sinal-de-aviso.png", "Escolha imagens com largura mínima de 245px e altura de 270px.");
                return 0;
            } else {
                imagem2.setText("");
                LOGGER.info("Usuário optou por manter apenas uma imagem.");
            }
        }

        LOGGER.info("Campos de imagem validados com sucesso.");
        return 1;
    }

    /**
     * Verifica se há campos obrigatórios vazios no formulário.
     *
     * @return 1 se há campos vazios, 0 se todos estão preenchidos.
     */
    private int camposVazios() {
        LOGGER.info("Verificando campos vazios no formulário.");
        String[] valoresFormularios = new String[]{
            nomeProduto.getText(), preco.getText(), descricao.getText(),
            marca.getText(), estoqueInicial.getText()
        };
        String selectedCategory = categorias.getSelectedItem() != null ? categorias.getSelectedItem().toString() : "";

        int camposVazios = 0;
        for (String valor : valoresFormularios) {
            if (valor.isBlank()) {
                camposVazios++;
            }
        }
        if (selectedCategory.isBlank()) {
            LOGGER.warning("Campo vazio detectado: categoria");
            camposVazios++;
        }

        if (camposVazios > 0) {
            LOGGER.warning("Encontrados " + camposVazios + " campos vazios.");
            Avisos("imagens/sinal-de-aviso.png", "Preencha todos os campos obrigatórios.");
            return 1;
        }

        LOGGER.info("Todos os campos obrigatórios estão preenchidos.");
        return 0;
    }

    /**
     * Cadastra um novo produto no banco de dados.
     */
    public void cadastrarProduto() {
        LOGGER.info("Cadastrando novo produto.");
        if (categorias.getSelectedItem() == null || categorias.getSelectedItem().toString().isBlank()) {
            LOGGER.warning("Nenhuma categoria selecionada.");
            Avisos("imagens/sinal-de-aviso.png", "Selecione uma categoria.");
            return;
        }

        // Validação de formatos numéricos
        try {
            if (!preco.getText().isEmpty()) {
                NumberFormat.getCurrencyInstance().parse(preco.getText()).doubleValue();
            }
            if (promocaoAtiva && !preco_promocional.getText().isEmpty()) {
                NumberFormat.getCurrencyInstance().parse(preco_promocional.getText()).doubleValue();
            }
            if (!avaliacao.getText().isEmpty()) {
                Double.parseDouble(avaliacao.getText());
            }
            if (!quantidade_avaliacao.getText().isEmpty()) {
                Integer.parseInt(quantidade_avaliacao.getText());
            }
            if (!estoqueInicial.getText().isEmpty()) {
                Integer.parseInt(estoqueInicial.getText());
            }
            if (!peso.getText().isEmpty()) {
                Double.parseDouble(peso.getText());
            }
        } catch (ParseException | NumberFormatException e) {
            LOGGER.severe("Erro nos formatos de número: " + e.getMessage());
            Avisos("imagens/sinal-de-aviso.png", "Verifique os formatos de preço, avaliação, quantidade de avaliações, estoque ou peso.");
            return;
        }

        // Salva imagens e obtém novos nomes
        String imagem1Path = null;
        String imagem2Path = null;
        if (!imagem1.getText().isBlank()) {
            imagem1Path = salvarImagem(imagem1.getText().contains("http") ? imagem1.getText() : new File(imagem1.getText()));
        }
        if (!imagem2.getText().isBlank()) {
            imagem2Path = salvarImagem(imagem2.getText().contains("http") ? imagem2.getText() : new File(imagem2.getText()));
        }

        if (imagem1Path == null && !imagem2.getText().isBlank()) {
            LOGGER.warning("Falha ao salvar imagem 1.");
            return;
        }

        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(INSERT_PRODUCT)) {
            String selectedCategory = categorias.getSelectedItem().toString();
            // Extrai apenas o nome da categoria (após o ID)
            String categoryName = selectedCategory.contains(" ") ? selectedCategory.substring(selectedCategory.indexOf(" ") + 1) : selectedCategory;

            stmt.setString(1, nomeProduto.getText());
            stmt.setString(2, descricao.getText());
            stmt.setString(3, descricao_curta.getText().isBlank() ? null : descricao_curta.getText());
            stmt.setDouble(4, NumberFormat.getCurrencyInstance().parse(preco.getText()).doubleValue());
            if (promocaoAtiva && !preco_promocional.getText().isBlank()) {
                stmt.setDouble(5, NumberFormat.getCurrencyInstance().parse(preco_promocional.getText()).doubleValue());
            } else {
                stmt.setNull(5, java.sql.Types.DECIMAL);
            }
            stmt.setBoolean(6, promocaoAtiva);
            stmt.setString(7, marca.getText().isBlank() ? null : marca.getText());
            if (!avaliacao.getText().isEmpty()) {
                stmt.setDouble(8, Double.parseDouble(avaliacao.getText()));
            } else {
                stmt.setNull(8, java.sql.Types.DECIMAL);
            }
            stmt.setInt(9, quantidade_avaliacao.getText().isBlank() ? 0 : Integer.parseInt(quantidade_avaliacao.getText()));
            stmt.setInt(10, Integer.parseInt(estoqueInicial.getText()));
            stmt.setInt(11, parcelas.getSelectedItem() != null ? Integer.parseInt((String) parcelas.getSelectedItem()) : 1);
            if (!peso.getText().isEmpty()) {
                stmt.setDouble(12, Double.parseDouble(peso.getText()));
            } else {
                stmt.setNull(12, java.sql.Types.DECIMAL);
            }
            stmt.setString(13, dimensoes.getText().isBlank() ? null : dimensoes.getText());
            stmt.setBoolean(14, produtoAtivo);
            stmt.setString(15, imagem1Path);
            stmt.setString(16, imagem2Path);
            stmt.setString(17, categoryName);

            stmt.executeUpdate();
            LOGGER.info("Produto cadastrado com sucesso.");
        } catch (SQLException e) {
            LOGGER.severe("Erro ao cadastrar produto: " + e.getMessage());
            Avisos("imagens/sinal-de-aviso.png", "Erro ao cadastrar produto: " + e.getMessage());
        } catch (ParseException e) {
            LOGGER.severe("Erro ao converter valores numéricos: " + e.getMessage());
            Avisos("imagens/sinal-de-aviso.png", "Erro nos valores numéricos.");
        }
    }

    /**
     * Exibe mensagens de aviso com ícone personalizado.
     *
     * @param endereco Caminho do ícone.
     * @param mensagem Mensagem a ser exibida.
     */
    public void Avisos(String endereco, String mensagem) {
        LOGGER.info("Exibindo aviso: " + mensagem);
        ImageIcon imagem = new ImageIcon(endereco);
        if (imagem.getIconWidth() == -1) {
            LOGGER.warning("Ícone não encontrado: " + endereco);
            JOptionPane.showMessageDialog(null, mensagem, "Mensagem", JOptionPane.INFORMATION_MESSAGE);
        } else {
            Image image = imagem.getImage();
            Image scaledImage = image.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            String mensagemFormatada = "<html><h2>" + mensagem + "</h2></html>";
            JLabel titulo = new JLabel(mensagemFormatada);
            JOptionPane.showMessageDialog(null, titulo, "Mensagem", JOptionPane.INFORMATION_MESSAGE, scaledIcon);
        }
    }

    /**
     * Carrega as categorias do banco de dados e preenche o JComboBox.
     */
    public void carregarCategorias() {
        LOGGER.info("Carregando categorias do banco de dados.");
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_CATEGORY); ResultSet rs = stmt.executeQuery()) {
            categorias.removeAllItems();
            categorias.addItem("");
            while (rs.next()) {
                categorias.addItem(rs.getInt("id_categoria") + " " + rs.getString("categoria"));
            }
            LOGGER.info("Categorias carregadas com sucesso.");
        } catch (SQLException ex) {
            LOGGER.severe(ERROR_DB_ACCESS + ex.getMessage());
            JOptionPane.showMessageDialog(null, ERROR_DB_ACCESS + ex.getMessage());
        }
    }

    /**
     * Limpa todos os campos do formulário e restaura a imagem padrão.
     */
    public void Apagar() {
        LOGGER.info("Limpando campos do formulário.");
        nomeProduto.setText("");
        descricao.setText("");
        preco.setText("");
        marca.setText("");
        estoqueInicial.setText("");
        imagem1.setText("");
        imagem2.setText("");
        sem_imagem.setIcon(sem_imagem());
        descricao_curta.setText("");
        preco_promocional.setText("");
        prom_sim.setSelected(false);
        prom_nao.setSelected(true);
        preco_promocional.setEnabled(false);
        promocaoAtiva = false;
        avaliacao.setText("");
        quantidade_avaliacao.setText("");
        peso.setText("");
        dimensoes.setText("");
        categorias.setSelectedIndex(0);
        ativo_sim.setSelected(true);
        ativo_nao.setSelected(false);
        produtoAtivo = true;
        slide.stop();
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
        nomeProduto.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                SwingUtilities.invokeLater(() -> {
                    String texto = nomeProduto.getText();
                    nomeProduto.setText(texto.replaceAll("[^a-zA-Z0-9áéíóúâêîôûãõçÁÉÍÓÚÂÊÎÔÛÃÕÇñÑ~\\s]", ""));
                    LOGGER.info("Máscara aplicada ao nome do produto: " + nomeProduto.getText());
                });
            }
        });
        atualizandoMascara = false;
    }

    /**
     * Aplica um filtro de entrada para permitir apenas números.
     *
     * @param textField Campo de texto a ser filtrado.
     */
    public void applyNumberOnlyMask(JTextField textField) {
        LOGGER.info("Aplicando filtro de números ao campo: " + textField.getName());
        AbstractDocument doc = (AbstractDocument) textField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string.matches("[0-9.]*")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("[0-9.]*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    /**
     * Aplica uma máscara de entrada para formatar valores monetários.
     */
    public void applyMoneyMask() {
        LOGGER.info("Aplicando máscara de valor monetário aos campos de preço.");
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        for (JTextField textField : new JTextField[]{preco, preco_promocional}) {
            AbstractDocument doc = (AbstractDocument) textField.getDocument();
            doc.setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                    if (string.matches("[0-9.,]*")) {
                        super.insertString(fb, offset, string, attr);
                    }
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                    if (text.matches("[0-9.,]*")) {
                        super.replace(fb, offset, length, text, attrs);
                    }
                }
            });

            textField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    try {
                        String text = textField.getText();
                        if (!text.isEmpty()) {
                            double value = Double.parseDouble(text.replaceAll("[^0-9]", "")) / 100;
                            textField.setText(currencyFormat.format(value));
                        }
                    } catch (NumberFormatException ex) {
                        LOGGER.warning("Erro ao formatar valor monetário: " + ex.getMessage());
                    }
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        promocional = new javax.swing.ButtonGroup();
        ativo = new javax.swing.ButtonGroup();
        painelProdutos = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        sem_imagem = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        nomeProduto = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        descricao = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        preco = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        estoqueInicial = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel(); // Inicialização correta
        categorias = new javax.swing.JComboBox<>();
        btSelecionarImagens = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        marca = new javax.swing.JTextField();
        btCadastrar = new javax.swing.JButton();
        btCancelar = new javax.swing.JButton();
        imagem1 = new javax.swing.JTextField();
        imagem2 = new javax.swing.JTextField();
        passarImagem = new javax.swing.JLabel();
        btExcluir = new javax.swing.JButton();
        btExcluir2 = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        descricao_curta = new javax.swing.JTextArea();
        preco_promocional = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        prom_sim = new javax.swing.JRadioButton();
        prom_nao = new javax.swing.JRadioButton();
        jLabel14 = new javax.swing.JLabel();
        avaliacao = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        quantidade_avaliacao = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        parcelas = new javax.swing.JComboBox<>();
        jLabel18 = new javax.swing.JLabel();
        peso = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        dimensoes = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        ativo_sim = new javax.swing.JRadioButton();
        ativo_nao = new javax.swing.JRadioButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Produtos");
        setPreferredSize(new java.awt.Dimension(1435, 790));

        painelProdutos.setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));

        sem_imagem.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(sem_imagem, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(sem_imagem, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel3.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel3.setText("Nome");

        nomeProduto.setFont(new java.awt.Font("Arial", 0, 20));
        nomeProduto.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                nomeProdutoFocusLost(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel4.setText("Descrição");

        descricao.setColumns(20);
        descricao.setFont(new java.awt.Font("Arial", 0, 20));
        descricao.setRows(5);
        jScrollPane1.setViewportView(descricao);

        jLabel5.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel5.setText("Promoção");

        jLabel6.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel6.setText("Marca:");

        preco.setFont(new java.awt.Font("Arial", 0, 20));

        jLabel7.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel7.setText("Estoque");

        estoqueInicial.setFont(new java.awt.Font("Arial", 0, 20));

        jLabel9.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel9.setText("Categoria:");

        categorias.setFont(new java.awt.Font("Arial", 0, 20));

        btSelecionarImagens.setBackground(new java.awt.Color(102, 102, 255));
        btSelecionarImagens.setFont(new java.awt.Font("Arial", 0, 18));
        btSelecionarImagens.setForeground(new java.awt.Color(255, 255, 255));
        btSelecionarImagens.setText("Selecionar imagens");
        btSelecionarImagens.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btSelecionarImagensMouseClicked(evt);
            }
        });

        marca.setFont(new java.awt.Font("Arial", 0, 20));

        btCadastrar.setBackground(new java.awt.Color(50, 205, 50));
        btCadastrar.setFont(new java.awt.Font("Arial", 0, 20));
        btCadastrar.setForeground(new java.awt.Color(255, 255, 255));
        btCadastrar.setText("Cadastrar");
        btCadastrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCadastrarActionPerformed(evt);
            }
        });

        btCancelar.setBackground(new java.awt.Color(169, 169, 169));
        btCancelar.setFont(new java.awt.Font("Arial", 0, 20));
        btCancelar.setForeground(new java.awt.Color(255, 255, 255));
        btCancelar.setText("Cancelar");
        btCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelarActionPerformed(evt);
            }
        });

        imagem1.setBackground(new java.awt.Color(204, 204, 255));
        imagem1.setFont(new java.awt.Font("Arial", 0, 20));
        imagem1.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        imagem1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                imagem1FocusLost(evt);
            }
        });

        imagem2.setBackground(new java.awt.Color(204, 204, 255));
        imagem2.setFont(new java.awt.Font("Arial", 0, 20));
        imagem2.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        imagem2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                imagem2FocusLost(evt);
            }
        });

        btExcluir.setBackground(new java.awt.Color(255, 0, 0));
        btExcluir.setFont(new java.awt.Font("Arial", 0, 18));
        btExcluir.setForeground(new java.awt.Color(255, 255, 255));
        btExcluir.setText("X");
        btExcluir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btExcluirMouseClicked(evt);
            }
        });

        btExcluir2.setBackground(new java.awt.Color(255, 0, 0));
        btExcluir2.setFont(new java.awt.Font("Arial", 0, 18));
        btExcluir2.setForeground(new java.awt.Color(255, 255, 255));
        btExcluir2.setText("X");
        btExcluir2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btExcluir2MouseClicked(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel11.setText("Descrição curta");

        descricao_curta.setColumns(20);
        descricao_curta.setFont(new java.awt.Font("Arial", 0, 20));
        descricao_curta.setRows(5);
        jScrollPane2.setViewportView(descricao_curta);

        preco_promocional.setFont(new java.awt.Font("Arial", 0, 20));

        jLabel12.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel12.setText("Preço promocional");

        jLabel13.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel13.setText("Preço:");

        promocional.add(prom_sim);
        prom_sim.setText("Sim");
        prom_sim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prom_simActionPerformed(evt);
            }
        });

        promocional.add(prom_nao);
        prom_nao.setText("Não");
        prom_nao.setSelected(true);
        prom_nao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prom_naoActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel14.setText("Avaliação");

        avaliacao.setFont(new java.awt.Font("Arial", 0, 20));

        jLabel15.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel15.setText("Quantidade de avaliações");

        quantidade_avaliacao.setFont(new java.awt.Font("Arial", 0, 20));

        jLabel16.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel16.setText("Imagens do produto");

        jLabel17.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel17.setText("Parcelas");

        parcelas.setFont(new java.awt.Font("Arial", 0, 20));
        parcelas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"}));

        jLabel18.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel18.setText("Peso (kg)");

        peso.setFont(new java.awt.Font("Arial", 0, 20));

        jLabel19.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel19.setText("Dimensões");

        dimensoes.setFont(new java.awt.Font("Arial", 0, 20));

        jLabel20.setFont(new java.awt.Font("Arial", 0, 20));
        jLabel20.setText("Ativo");

        ativo.add(ativo_sim);
        ativo_sim.setText("Sim");
        ativo_sim.setSelected(true);
        ativo_sim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ativo_simActionPerformed(evt);
            }
        });

        ativo.add(ativo_nao);
        ativo_nao.setText("Não");
        ativo_nao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ativo_naoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout painelProdutosLayout = new javax.swing.GroupLayout(painelProdutos);
        painelProdutos.setLayout(painelProdutosLayout);
        painelProdutosLayout.setHorizontalGroup(
                painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(painelProdutosLayout.createSequentialGroup()
                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(painelProdutosLayout.createSequentialGroup()
                                                .addGap(61, 61, 61)
                                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(painelProdutosLayout.createSequentialGroup()
                                                                .addGap(36, 36, 36)
                                                                .addComponent(btSelecionarImagens))
                                                        .addGroup(painelProdutosLayout.createSequentialGroup()
                                                                .addGap(108, 108, 108)
                                                                .addComponent(passarImagem, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                // Removed jLabel10 as it seems unused; uncomment if needed
                                // .addGroup(painelProdutosLayout.createSequentialGroup()
                                //     .addGap(121, 121, 121)
                                //     .addComponent(jLabel10))
                                )
                                .addGap(62, 62, 62)
                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel4)
                                        .addComponent(jScrollPane1)
                                        .addComponent(jLabel11)
                                        .addComponent(jScrollPane2)
                                        .addGroup(painelProdutosLayout.createSequentialGroup()
                                                .addComponent(jLabel3)
                                                .addGap(18, 18, 18)
                                                .addComponent(nomeProduto))
                                        .addGroup(painelProdutosLayout.createSequentialGroup()
                                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel13)
                                                        .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(marca, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                                                        .addComponent(preco))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addGroup(painelProdutosLayout.createSequentialGroup()
                                                                .addComponent(jLabel14)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(avaliacao, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(painelProdutosLayout.createSequentialGroup()
                                                                .addComponent(jLabel5)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(prom_sim, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(prom_nao)))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelProdutosLayout.createSequentialGroup()
                                                                .addComponent(jLabel15)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(quantidade_avaliacao, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelProdutosLayout.createSequentialGroup()
                                                                .addComponent(jLabel12)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(preco_promocional, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                        .addGroup(painelProdutosLayout.createSequentialGroup()
                                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(painelProdutosLayout.createSequentialGroup()
                                                                .addComponent(jLabel17)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(parcelas, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(jLabel18))
                                                        .addGroup(painelProdutosLayout.createSequentialGroup()
                                                                .addComponent(jLabel20)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(ativo_sim)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(ativo_nao)
                                                                .addGap(29, 29, 29)
                                                                .addComponent(jLabel7)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(estoqueInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(painelProdutosLayout.createSequentialGroup()
                                                                .addComponent(jLabel9)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(categorias, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                        .addGroup(painelProdutosLayout.createSequentialGroup()
                                                                .addComponent(peso, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(jLabel19)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(dimensoes))))
                                        .addComponent(jLabel16)
                                        .addGroup(painelProdutosLayout.createSequentialGroup()
                                                .addComponent(imagem1, javax.swing.GroupLayout.PREFERRED_SIZE, 926, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btExcluir))
                                        .addGroup(painelProdutosLayout.createSequentialGroup()
                                                .addComponent(imagem2, javax.swing.GroupLayout.PREFERRED_SIZE, 926, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btExcluir2))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelProdutosLayout.createSequentialGroup()
                                                .addComponent(btCadastrar)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btCancelar)))
                                .addContainerGap(65, Short.MAX_VALUE))
        );

        painelProdutosLayout.setVerticalGroup(
                painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(painelProdutosLayout.createSequentialGroup()
                                .addGap(33, 33, 33)
                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(painelProdutosLayout.createSequentialGroup()
                                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(nomeProduto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel3))
                                                .addGap(32, 32, 32)
                                                .addComponent(jLabel4)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(29, 29, 29)
                                                .addComponent(jLabel11)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(painelProdutosLayout.createSequentialGroup()
                                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(btSelecionarImagens)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(passarImagem, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(preco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel13)
                                        .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel5)
                                                        .addComponent(prom_sim)
                                                        .addComponent(prom_nao))
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(preco_promocional, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel12))))
                                .addGap(31, 31, 31)
                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(marca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel14)
                                                .addComponent(avaliacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLabel15)
                                                .addComponent(quantidade_avaliacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(33, 33, 33)
                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLabel17)
                                                .addComponent(parcelas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jLabel18)
                                        .addComponent(peso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel19)
                                        .addComponent(dimensoes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(32, 32, 32)
                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel20)
                                        .addComponent(ativo_sim)
                                        .addComponent(ativo_nao)
                                        .addComponent(jLabel7)
                                        .addComponent(estoqueInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel9)
                                        .addComponent(categorias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(imagem1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(imagem2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btExcluir2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btCadastrar)
                                        .addComponent(btCancelar))
                                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(painelProdutos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(painelProdutos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }

    private void btExcluir2MouseClicked(java.awt.event.MouseEvent evt) {
        LOGGER.info("Excluindo conteúdo do campo imagem2.");
        imagem2.setText("");
        numeroImagens = imagem1.getText().isBlank() ? 0 : 1;
    }

    private void btExcluirMouseClicked(java.awt.event.MouseEvent evt) {
        LOGGER.info("Excluindo conteúdo do campo imagem1.");
        imagem1.setText("");
        if (!imagem2.getText().isBlank()) {
            LOGGER.info("Transferindo conteúdo de imagem2 para imagem1.");
            imagem1.setText(imagem2.getText());
            imagem2.setText("");
            numeroImagens = 1;
        } else {
            numeroImagens = 0;
        }
    }

    private void btCancelarActionPerformed(java.awt.event.ActionEvent evt) {
        LOGGER.info("Cancelando e limpando formulário.");
        Apagar();
    }

    private void btCadastrarActionPerformed(java.awt.event.ActionEvent evt) {
        LOGGER.info("Iniciando processo de cadastro de produto.");
        int resposta = camposVazios();
        if (resposta == 0) {
            int resposta2 = camposImagens();
            if (resposta2 == 1) {
                try {
                    cadastrarProduto();
                    Avisos("imagens/confirmacao.png", "Produto cadastrado com sucesso!");
                    Apagar();
                    LOGGER.info("Produto cadastrado com sucesso.");
                } catch (Exception e) {
                    LOGGER.severe("Erro inesperado ao cadastrar produto: " + e.getMessage());
                    Avisos("imagens/sinal-de-aviso.png", "Erro inesperado ao cadastrar. Tente novamente!");
                }
            }
        }
    }

    private void btSelecionarImagensMouseClicked(java.awt.event.MouseEvent evt) {
        LOGGER.info("Selecionando imagens para o produto.");
        if (contagem == 0) {
            Avisos("imagens/sinal-de-aviso.png", "Escolha imagens com largura mínima de 245px e altura de 270px.");
            contagem++;
        }
        JFileChooser selecionar = new JFileChooser();
        selecionar.setCurrentDirectory(new File("imagens"));
        selecionar.setDialogTitle("Escolha a imagem do produto");
        selecionar.setFileSelectionMode(JFileChooser.FILES_ONLY);
        selecionar.setMultiSelectionEnabled(false);
        selecionar.setApproveButtonText("Selecionar");
        selecionar.setAcceptAllFileFilterUsed(false);
        selecionar.setDialogType(JFileChooser.OPEN_DIALOG);
        FileNameExtensionFilter filtro = new FileNameExtensionFilter("Imagens", "jpg", "png", "jpeg");
        selecionar.setFileFilter(filtro);

        int retorno = selecionar.showOpenDialog(this);

        if (retorno == JFileChooser.APPROVE_OPTION) {
            arquivo = selecionar.getSelectedFile();
            String nomeArquivo = arquivo.getName();
            if (imagem1.getText().isEmpty()) {
                imagem1.setText(nomeArquivo);
                numeroImagens = 1;
                LOGGER.info("Imagem 1 selecionada: " + nomeArquivo);
            } else {
                imagem2.setText(nomeArquivo);
                numeroImagens = 2;
                LOGGER.info("Imagem 2 selecionada: " + nomeArquivo);
            }
            sem_imagem.setIcon(redimensionamentoDeImagem(arquivo));
        }
    }

    private void nomeProdutoFocusLost(java.awt.event.FocusEvent evt) {
        String produto = nomeProduto.getText().trim();
        if (produto.isEmpty()) {
            return;
        }
        LOGGER.info("Verificando existência do produto: " + produto);
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(CHECK_PRODUCT)) {
            stmt.setString(1, produto);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    LOGGER.warning("Produto já existe: " + produto);
                    Avisos("imagens/sinal-de-aviso.png", "Produto já existe.");
                    nomeProduto.setText("");
                }
            }
        } catch (SQLException ex) {
            LOGGER.severe("Erro ao verificar produto: " + ex.getMessage());
            Avisos("imagens/sinal-de-aviso.png", "Erro ao verificar produto: " + ex.getMessage());
        }
    }

    private void prom_simActionPerformed(java.awt.event.ActionEvent evt) {
        LOGGER.info("Promoção marcada como 'Sim'.");
        promocaoAtiva = true;
        preco_promocional.setEnabled(true);
    }

    private void prom_naoActionPerformed(java.awt.event.ActionEvent evt) {
        LOGGER.info("Promoção marcada como 'Não'.");
        promocaoAtiva = false;
        preco_promocional.setEnabled(false);
        preco_promocional.setText("");
    }

    private void ativo_simActionPerformed(java.awt.event.ActionEvent evt) {
        LOGGER.info("Produto marcado como 'Ativo'.");
        produtoAtivo = true;
    }

    private void ativo_naoActionPerformed(java.awt.event.ActionEvent evt) {
        LOGGER.info("Produto marcado como 'Inativo'.");
        produtoAtivo = false;
    }

    private void imagem1FocusLost(java.awt.event.FocusEvent evt) {
        String imageUrl = imagem1.getText().trim();
        if (imageUrl.isEmpty() || !imageUrl.contains("http")) {
            return;
        }
        LOGGER.info("Carregando imagem de URL para imagem1: " + imageUrl);
        try {
            URL url = new URL(imageUrl);
            BufferedImage image = ImageIO.read(url);
            if (image != null) {
                sem_imagem.setIcon(redimensionamentoDeImagem(new ImageIcon(image), 245, 270));
                numeroImagens = imagem2.getText().isBlank() ? 1 : 2;
            } else {
                LOGGER.warning("Imagem inválida na URL: " + imageUrl);
                Avisos("imagens/sinal-de-aviso.png", "Imagem inválida. Tente outra URL.");
                imagem1.setText("");
            }
        } catch (IOException e) {
            LOGGER.warning("Erro ao carregar URL: " + imageUrl);
            Avisos("imagens/sinal-de-aviso.png", "Falha ao carregar URL. Tente novamente.");
            imagem1.setText("");
        }
    }

    private void imagem2FocusLost(java.awt.event.FocusEvent evt) {
        String imageUrl = imagem2.getText().trim();
        if (imageUrl.isEmpty() || !imageUrl.contains("http")) {
            return;
        }
        LOGGER.info("Carregando imagem de URL para imagem2: " + imageUrl);
        try {
            URL url = new URL(imageUrl);
            BufferedImage image = ImageIO.read(url);
            if (image != null) {
                sem_imagem.setIcon(redimensionamentoDeImagem(new ImageIcon(image), 245, 270));
                numeroImagens = 2;
            } else {
                LOGGER.warning("Imagem inválida na URL: " + imageUrl);
                Avisos("imagens/sinal-de-aviso.png", "Imagem inválida. Tente outra URL.");
                imagem2.setText("");
            }
        } catch (IOException e) {
            LOGGER.warning("Erro ao carregar URL: " + imageUrl);
            Avisos("imagens/sinal-de-aviso.png", "Falha ao carregar URL. Tente novamente.");
            imagem2.setText("");
        }
    }

    // Variables declaration
    private javax.swing.ButtonGroup ativo;
    private javax.swing.JRadioButton ativo_nao;
    private javax.swing.JRadioButton ativo_sim;
    private javax.swing.JTextField avaliacao;
    private javax.swing.JButton btCadastrar;
    private javax.swing.JButton btCancelar;
    private javax.swing.JButton btExcluir;
    private javax.swing.JButton btExcluir2;
    private javax.swing.JButton btSelecionarImagens;
    private javax.swing.JComboBox<String> categorias;
    private javax.swing.JTextArea descricao;
    private javax.swing.JTextArea descricao_curta;
    private javax.swing.JTextField dimensoes;
    private javax.swing.JTextField estoqueInicial;
    private javax.swing.JTextField imagem1;
    private javax.swing.JTextField imagem2;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField marca;
    private javax.swing.JTextField nomeProduto;
    private javax.swing.JPanel painelProdutos;
    private javax.swing.JComboBox<String> parcelas;
    private javax.swing.JLabel passarImagem;
    private javax.swing.JTextField peso;
    private javax.swing.JTextField preco;
    private javax.swing.JTextField preco_promocional;
    private javax.swing.JRadioButton prom_nao;
    private javax.swing.JRadioButton prom_sim;
    private javax.swing.ButtonGroup promocional;
    private javax.swing.JTextField quantidade_avaliacao;
    private javax.swing.JLabel sem_imagem;
}
