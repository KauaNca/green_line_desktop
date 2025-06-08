
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
import java.io.IOException;
import java.net.URL;
import javax.swing.ImageIcon;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
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
 * validação de entrada e slideshow de imagens.
 *
 * @author Kaua33500476
 */
public class CadastroProdutos extends javax.swing.JInternalFrame {

    // Logger para rastreamento de eventos e erros
    private static final Logger LOGGER = Logger.getLogger(CadastroProdutos.class.getName());

    // Constantes para queries SQL, caminhos de imagens e mensagens
    private static final String SELECT_CATEGORY = "SELECT id_categoria,categoria FROM categorias";
    private static final String INSERT_PRODUCT = "INSERT INTO produto(produto,descricao,descricao_curta,preco,preco_promocional,promocao,"
            + "marca,avaliacao,quantidade_avaliacoes,estoque,parcelas_permitidas,peso_kg,dimensoes,ativo,imagem_1,imagem_2,categoria) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
    private String[] enderecosImagens;
    private String espaco = "";
    private int repeticao = 0;
    private boolean atualizandoMascara = false;

    // Timer para slideshow de imagens
    private final Timer slide = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            LOGGER.info("Alternando imagem no slideshow. Contagem: " + contagem);
            String enderecoImagem1 = imagem1.getText();
            String enderecoImagem2 = imagem2.getText();
            enderecosImagens = new String[]{enderecoImagem1, enderecoImagem2};
            contagem = (contagem + 1) % enderecosImagens.length;
            ImageIcon proximaImagem = new ImageIcon(PRODUCT_IMAGE_PATH + enderecosImagens[contagem]);
            if (proximaImagem.getIconWidth() == -1) {
                LOGGER.warning(ERROR_IMAGE_NOT_FOUND + ": " + enderecosImagens[contagem]);
                slide.stop();
            } else {
                sem_imagem.setIcon(redimensionamentoDeImagem(proximaImagem, 250, 216));
                slide.stop();
            }
        }
    });

    /**
     * Construtor padrão. Inicializa a interface, configura ícones, aplica
     * máscaras de entrada e carrega categorias.
     */
    public CadastroProdutos() {
        initComponents();
        LOGGER.info("Inicializando interface de cadastro de produtos.");
        jPanel1.setBackground(Color.white);
        sem_imagem.setIcon(sem_imagem());
        passarImagem.setIcon(new ImageIcon(RIGHT_ARROW_PATH));
        atualizarMascara();
        applyTextAndNumberFilter(preco);
        applyNumberOnlyMask(estoqueInicial);
        applyMoneyMask(preco);
        passarImagem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                LOGGER.info("Iniciando slideshow de imagens.");
                slide.start();
            }
        });
        carregarCategorias();
    }

    /**
     * Retorna o ícone padrão para quando não há imagem disponível.
     *
     * @return Ícone redimensionado da imagem padrão.
     */
    public ImageIcon sem_imagem() {
        LOGGER.info("Carregando imagem padrão: " + DEFAULT_IMAGE_PATH);
        ImageIcon imagem = new ImageIcon(DEFAULT_IMAGE_PATH);
        Image redimensionar = imagem.getImage();
        Image redimensionar2 = redimensionar.getScaledInstance(250, 216, Image.SCALE_SMOOTH);
        return new ImageIcon(redimensionar2);
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
        Image pegarImagem = imagem.getImage();
        Image redimensionando = pegarImagem.getScaledInstance(250, 216, Image.SCALE_SMOOTH);
        return new ImageIcon(redimensionando);
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
     * Valida os campos de imagens e solicita a seleção de imagens, se
     * necessário.
     *
     * @return 1 se as imagens são válidas, 0 se há problemas.
     */
    public int camposImagens() {
        LOGGER.info("Validando campos de imagens.");
        if (imagem1.getText().isBlank() && imagem2.getText().isBlank()) {
            LOGGER.warning("Ambos os campos de imagem estão vazios.");
            Avisos("imagens/sinal-de-aviso.png", "Selecione uma imagem");
            return 0;
        }

        if (imagem1.getText().isBlank()) {
            LOGGER.warning("Campo de imagem 1 está vazio.");
            Avisos("imagens/sinal-de-aviso.png", "Selecione uma imagem para o campo 1");
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
                if (repeticao == 0) {
                    LOGGER.warning("Imagens devem ter largura acima de 250px e altura de 216px.");
                    Avisos("imagens/sinal-de-aviso.png",
                            "Escolha imagens que possuam largura acima de 250px e altura de 216px");
                    repeticao++;
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
                    } else {
                        imagem2.setText(nomeArquivo);
                    }
                    sem_imagem.setIcon(redimensionamentoDeImagem(arquivo));
                    LOGGER.info("Imagem selecionada: " + nomeArquivo);
                }
                repeticao = 0;
                return 0;
            } else {
                imagem2.setText("");
                LOGGER.info("Usuário optou por manter apenas uma imagem.");
            }
            return 1;
        }

        LOGGER.info("Ambos os campos de imagem estão preenchidos.");
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
            Avisos("imagens/sinal-de-aviso.png", "Campos obrigatórios não preenchidos");
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
            Avisos("imagens/sinal-de-aviso.png", "Selecione uma categoria antes de cadastrar o produto.");
            return;
        }
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(INSERT_PRODUCT)) {
            String selectedCategory = categorias.getSelectedItem().toString();
            stmt.setString(1, nomeProduto.getText());
            stmt.setString(2, descricao.getText());
            stmt.setString(3, descricao_curta.getText().isBlank() ? null : descricao_curta.getText());
            stmt.setString(4, preco.getText());
            stmt.setString(5, preco_promocional.getText().isBlank() ? null : preco_promocional.getText());
            stmt.setBoolean(6, prom_sim.isSelected());
            stmt.setString(7, marca.getText());
            stmt.setString(8, avaliacao.getText().isBlank() ? null : avaliacao.getText());
            stmt.setString(9, quantidade_avaliacao.getText().isBlank() ? null : quantidade_avaliacao.getText());
            stmt.setString(10, estoqueInicial.getText());
            stmt.setString(11, parcelas.getSelectedItem() != null ? parcelas.getSelectedItem().toString() : null);
            stmt.setString(12, peso.getText().isBlank() ? null : peso.getText());
            stmt.setString(13, dimensoes.getText().isBlank() ? null : dimensoes.getText());
            stmt.setBoolean(14, ativo_sim.isSelected());
            stmt.setString(15, imagem1.getText().isBlank() ? null : imagem1.getText());
            stmt.setString(16, imagem2.getText().isBlank() ? null : imagem2.getText());
            stmt.setString(17, selectedCategory);
            stmt.execute();
            LOGGER.info("Produto cadastrado com sucesso.");
            Apagar();
        } catch (SQLException e) {
            LOGGER.severe("Erro ao cadastrar produto: " + e.getMessage());
            Avisos("imagens/sinal-de-aviso.png", "Erro ao cadastrar produto: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Erro inesperado ao cadastrar produto: " + e.getMessage());
            Avisos("imagens/sinal-de-aviso.png", "Houve um erro inesperado. Tente novamente!");
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
        } else {
            Image image = imagem.getImage();
            Image scaledImage = image.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            String mensagemFormatada = "<html><h2>" + mensagem + "</h2></html>";
            JLabel titulo = new JLabel(mensagemFormatada);
            JOptionPane.showMessageDialog(null, titulo, "Mensagem", JOptionPane.INFORMATION_MESSAGE, scaledIcon);
            return;
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
        } catch (Exception e) {
            LOGGER.severe(ERROR_GENERIC + e.getMessage());
            JOptionPane.showMessageDialog(null, ERROR_GENERIC + e.getMessage());
        }
    }

    /**
     * Limpa todos os campos do formulário e restaura a imagem padrão.
     */
    public void Apagar() {
        LOGGER.info("Limpando campos do formulário.");
        if (nomeProduto != null) {
            nomeProduto.setText("");
        }
        if (descricao != null) {
            descricao.setText("");
        }
        if (preco != null) {
            preco.setText("");
        }
        if (marca != null) {
            marca.setText("");
        }
        if (estoqueInicial != null) {
            estoqueInicial.setText("");
        }
        if (imagem1 != null) {
            imagem1.setText("");
        }
        if (imagem2 != null) {
            imagem2.setText("");
        }
        if (sem_imagem != null) {
            sem_imagem.setIcon(sem_imagem());
        }
        if (descricao_curta != null) {
            descricao_curta.setText("");
        }
        if (preco_promocional != null) {
            preco_promocional.setText("");
        }
        if (avaliacao != null) {
            avaliacao.setText("");
        }
        if (quantidade_avaliacao != null) {
            quantidade_avaliacao.setText("");
        }
        if (peso != null) {
            peso.setText("");
        }
        if (dimensoes != null) {
            dimensoes.setText("");
        }
        if (categorias != null) {
            categorias.setSelectedIndex(0);
        }
    }

    /**
     * Aplica uma máscara ao campo de nome do produto, permitindo apenas
     * caracteres alfanuméricos e acentuados. Evita loops de atualização.
     */
    private void atualizarMascara() {
        if (atualizandoMascara) {
            return; // Evita loops
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
     * Aplica um filtro de entrada para permitir apenas letras, números e
     * espaços.
     *
     * @param textField Campo de texto a ser filtrado.
     */
    public void applyTextAndNumberFilter(JTextField textField) {
        LOGGER.info("Aplicando filtro de letras e números ao campo: " + textField.getName());
        AbstractDocument doc = (AbstractDocument) textField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string.matches("[a-zA-Z0-9\\s]*")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("[a-zA-Z0-9\\s]*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
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
                if (string.matches("[0-9]*")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("[0-9]*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                super.remove(fb, offset, length);
            }
        });
    }

    /**
     * Aplica uma máscara de entrada para formatar valores monetários.
     *
     * @param textField Campo de texto a ser formatado.
     */
    public void applyMoneyMask(JTextField textField) {
        LOGGER.info("Aplicando máscara de valor monetário ao campo: " + textField.getName());
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
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
                        textField.setText(currencyFormat.format(Double.parseDouble(text.replaceAll("[^0-9]", "")) / 100));
                    }
                } catch (NumberFormatException ex) {
                    LOGGER.warning("Erro ao formatar valor monetário: " + ex.getMessage());
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        promocional = new javax.swing.ButtonGroup();
        ativo = new javax.swing.ButtonGroup();
        painelProdutos = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
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
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
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

        jLabel3.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel3.setText("Nome");

        nomeProduto.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        nomeProduto.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                nomeProdutoFocusLost(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel4.setText("Descrição");

        descricao.setColumns(20);
        descricao.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        descricao.setRows(5);
        jScrollPane1.setViewportView(descricao);

        jLabel5.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel5.setText("Promoção");

        jLabel6.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel6.setText("Marca:");

        preco.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N

        jLabel7.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel7.setText("Estoque");

        estoqueInicial.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel9.setText("Categoria:");

        categorias.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N

        btSelecionarImagens.setBackground(new java.awt.Color(102, 102, 255));
        btSelecionarImagens.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        btSelecionarImagens.setForeground(new java.awt.Color(255, 255, 255));
        btSelecionarImagens.setText("Selecionar imagens");
        btSelecionarImagens.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btSelecionarImagensMouseClicked(evt);
            }
        });

        marca.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N

        btCadastrar.setBackground(new java.awt.Color(50, 205, 50));
        btCadastrar.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        btCadastrar.setForeground(new java.awt.Color(255, 255, 255));
        btCadastrar.setText("Cadastrar");
        btCadastrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCadastrarActionPerformed(evt);
            }
        });

        btCancelar.setBackground(new java.awt.Color(169, 169, 169));
        btCancelar.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        btCancelar.setForeground(new java.awt.Color(255, 255, 255));
        btCancelar.setText("Cancelar");
        btCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelarActionPerformed(evt);
            }
        });

        imagem1.setBackground(new java.awt.Color(204, 204, 255));
        imagem1.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        imagem1.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        imagem1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                imagem1FocusLost(evt);
            }
        });

        imagem2.setBackground(new java.awt.Color(204, 204, 255));
        imagem2.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        imagem2.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        imagem2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                imagem2FocusLost(evt);
            }
        });

        btExcluir.setBackground(new java.awt.Color(255, 0, 0));
        btExcluir.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        btExcluir.setForeground(new java.awt.Color(255, 255, 255));
        btExcluir.setText("X");
        btExcluir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btExcluirMouseClicked(evt);
            }
        });

        btExcluir2.setBackground(new java.awt.Color(255, 0, 0));
        btExcluir2.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        btExcluir2.setForeground(new java.awt.Color(255, 255, 255));
        btExcluir2.setText("X");
        btExcluir2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btExcluir2MouseClicked(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel11.setText("Descrição curta");

        descricao_curta.setColumns(20);
        descricao_curta.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        descricao_curta.setRows(5);
        jScrollPane2.setViewportView(descricao_curta);

        preco_promocional.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N

        jLabel12.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel12.setText("Preço promocional");

        jLabel13.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
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
        prom_nao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prom_naoActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel14.setText("Avaliação");

        avaliacao.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N

        jLabel15.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel15.setText("Quantidade de avaliações");

        quantidade_avaliacao.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N

        jLabel16.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel16.setText("Imagens do produto");

        jLabel17.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel17.setText("Parcelas");

        parcelas.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        parcelas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }));

        jLabel18.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel18.setText("Peso (kg)");

        peso.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N

        jLabel19.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel19.setText("Dimensões");

        dimensoes.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N

        jLabel20.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel20.setText("Ativo");

        ativo.add(ativo_sim);
        ativo_sim.setText("Sim");
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
                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(painelProdutosLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btCadastrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btCancelar))
                    .addGroup(painelProdutosLayout.createSequentialGroup()
                        .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelProdutosLayout.createSequentialGroup()
                                .addGap(0, 61, Short.MAX_VALUE)
                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10)
                                    .addGroup(painelProdutosLayout.createSequentialGroup()
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(62, 62, 62))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelProdutosLayout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(passarImagem)
                                .addGap(73, 73, 73))
                            .addGroup(painelProdutosLayout.createSequentialGroup()
                                .addGap(84, 84, 84)
                                .addComponent(btSelecionarImagens)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelProdutosLayout.createSequentialGroup()
                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(imagem1)
                                    .addComponent(imagem2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btExcluir)
                                    .addComponent(btExcluir2)))
                            .addGroup(painelProdutosLayout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(18, 18, 18)
                                .addComponent(nomeProduto))
                            .addComponent(jScrollPane1)
                            .addComponent(jLabel4)
                            .addComponent(jLabel8)
                            .addComponent(jLabel11)
                            .addComponent(jScrollPane2)
                            .addComponent(jLabel16)
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
                                        .addComponent(ativo_sim, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(ativo_nao)
                                        .addGap(41, 41, 41)
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
                                        .addComponent(dimensoes)))))))
                .addContainerGap(65, Short.MAX_VALUE))
        );
        painelProdutosLayout.setVerticalGroup(
            painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelProdutosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE))
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
                        .addGap(28, 28, 28)
                        .addComponent(btSelecionarImagens)))
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
                    .addGroup(painelProdutosLayout.createSequentialGroup()
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                        .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(painelProdutosLayout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(imagem1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(imagem2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btExcluir2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(btCadastrar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btCancelar))
                                .addGap(72, 72, 72))
                            .addGroup(painelProdutosLayout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addGap(242, 242, 242))))
                    .addGroup(painelProdutosLayout.createSequentialGroup()
                        .addComponent(passarImagem)
                        .addContainerGap())))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(painelProdutos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(painelProdutos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
/**
     * Remove o texto do campo imagem2.
     *
     * @param evt Evento de clique do mouse.
     */

    private void btExcluir2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btExcluir2MouseClicked
        LOGGER.info("Excluindo conteúdo do campo imagem2.");
        imagem2.setText("");

    }//GEN-LAST:event_btExcluir2MouseClicked
    /**
     * Remove o texto do campo imagem1. Se imagem2 não estiver vazia, transfere
     * seu conteúdo para imagem1 e limpa imagem2.
     *
     * @param evt Evento de clique do mouse.
     */

    private void btExcluirMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btExcluirMouseClicked
        LOGGER.info("Excluindo conteúdo do campo imagem1.");
        imagem1.setText("");
        if (!imagem2.getText().isBlank()) {
            LOGGER.info("Transferindo conteúdo de imagem2 para imagem1.");
            imagem1.setText(imagem2.getText());
            imagem2.setText("");
        }

    }//GEN-LAST:event_btExcluirMouseClicked
    /**
     * Limpa todos os campos do formulário e restaura a imagem padrão.
     *
     * @param evt Evento de ação do botão.
     */

    private void btCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelarActionPerformed
        LOGGER.info("Cancelando e limpando formulário.");
        nomeProduto.setText(null);
        descricao.setText(null);
        preco.setText(null);
        marca.setText(null);
        estoqueInicial.setText(null);
        imagem1.setText(null);
        imagem2.setText(null);
        sem_imagem.setIcon(sem_imagem());
    }//GEN-LAST:event_btCancelarActionPerformed
    /**
     * Cadastra um novo produto se todos os campos forem válidos. Salva imagens
     * associadas e exibe confirmação.
     *
     * @param evt Evento de ação do botão.
     */

    private void btCadastrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCadastrarActionPerformed
        LOGGER.info("Iniciando processo de cadastro de produto.");
        int resposta = camposVazios();
        if (resposta == 0) {
            int resposta2 = camposImagens();
            if (resposta2 == 1) {
                try {
                    cadastrarProduto();
                    Avisos("imagens/confirmacao.png", "Produto cadastrado");
                    Apagar();
                    LOGGER.info("Produto cadastrado com sucesso.");
                } catch (Exception e) {
                    LOGGER.severe("Erro inesperado ao cadastrar produto: " + e.getMessage());
                    JOptionPane.showMessageDialog(null, "Houve um erro inesperado. Tente daqui a pouco!",
                            "Mensagem", JOptionPane.ERROR_MESSAGE);
                    dispose();
                }
            }
        }


    }//GEN-LAST:event_btCadastrarActionPerformed

    private void btSelecionarImagensMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btSelecionarImagensMouseClicked

        if (contagem == 0) {
            Avisos("imagens/sinal-de-aviso.png", "Escolha imagens que possuam largura acima de 250px e altura de 216px");
            contagem++;
        }
        JFileChooser selecionar = new JFileChooser();
        // Caminho completo para o diretório na Área de Trabalho
        selecionar.setCurrentDirectory(new File("imagens"));//Define o diretório inicial que será exibido quando o diálogo for aberto.

        selecionar.setDialogTitle("Escolha a imagem do produto"); //Define o título da caixa de diálogo.
        selecionar.setFileSelectionMode(JFileChooser.FILES_ONLY); //Define se o usuário pode selecionar arquivos, diretórios ou ambos.
        selecionar.setMultiSelectionEnabled(false); // Permite selecionar vários arquivos
        selecionar.setApproveButtonText("Selecionar"); //Define o texto do botão OPEN. Mais usado quando o DialogType é CUSTOM_DIALOG
        selecionar.setAcceptAllFileFilterUsed(false); //Define se terá a opção de Aceitar Todos Os Arquivos.
        selecionar.setDialogType(JFileChooser.OPEN_DIALOG); //Define o tipo de processo que será: normal,salvar ou customizado.

        FileNameExtensionFilter filtro = new FileNameExtensionFilter("Imagens", "jpg", "png", "jpge"); //Permite definir filtros para limitar os tipos de arquivos que podem ser selecionados.
        selecionar.setFileFilter(filtro); //Apenas passar o filtro.

        int retorno = selecionar.showOpenDialog(this);

        if (retorno == JFileChooser.APPROVE_OPTION) {
            arquivo = selecionar.getSelectedFile(); //Pega o endereço do arquivo, logo é possível manipular.
            String nomeArquivo = arquivo.getName();
            if (imagem1.getText().isEmpty() || imagem1.getText() == null) {
                imagem1.setText(nomeArquivo);
                numeroImagens = 1;

            } else {
                imagem2.setText(nomeArquivo);
                numeroImagens = 2;
            }
            sem_imagem.setIcon(redimensionamentoDeImagem(arquivo));
        }
    }//GEN-LAST:event_btSelecionarImagensMouseClicked

    private void nomeProdutoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nomeProdutoFocusLost
        try (Connection con = Conexao.conexaoBanco()) {
            PreparedStatement stmt = con.prepareStatement("SELECT produto FROM produto WHERE produto = ?");
            stmt.setString(1, nomeProduto.getText());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(null, "Produto já existe", "Mensagem", JOptionPane.INFORMATION_MESSAGE);
                nomeProduto.setText("");
            }
            rs.close();
            stmt.close();
            con.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "ERRO", "Mensagem", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }//GEN-LAST:event_nomeProdutoFocusLost

    private void prom_simActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prom_simActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_prom_simActionPerformed

    private void prom_naoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prom_naoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_prom_naoActionPerformed

    private void ativo_simActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ativo_simActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ativo_simActionPerformed

    private void ativo_naoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ativo_naoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ativo_naoActionPerformed

    private void imagem1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_imagem1FocusLost
        String imageUrl = imagem1.getText();
        try {
            if (imageUrl.contains("http")) {
                URL url = new URL(imageUrl);
                BufferedImage image = ImageIO.read(url);

                Image imagemRedimensionada = image.getScaledInstance(245, 270, Image.SCALE_SMOOTH);
                sem_imagem.setIcon(new ImageIcon(imagemRedimensionada));

            } else {
                return;
            }

        } catch (IOException e) {
            Avisos("imagens/sinal-de-aviso.png", "Carregamento de URL falho. Tente novamente");
        }
    }//GEN-LAST:event_imagem1FocusLost

    private void imagem2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_imagem2FocusLost
        String imageUrl = imagem2.getText();
        try {
            if (imageUrl.contains("http")) {
                URL url = new URL(imageUrl);
                BufferedImage image = ImageIO.read(url);

                Image imagemRedimensionada = image.getScaledInstance(245, 270, Image.SCALE_SMOOTH);
                sem_imagem.setIcon(new ImageIcon(imagemRedimensionada));

            } else {
                return;
            }

        } catch (IOException e) {
            Avisos("imagens/sinal-de-aviso.png", "Carregamento de URL falho. Tente novamente");
        }    }//GEN-LAST:event_imagem2FocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
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
    private javax.swing.JLabel jLabel1;
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
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
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
    // End of variables declaration//GEN-END:variables
}
