
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
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
    private static final String SELECT_SUBCATEGORY_ID = "SELECT id_subcat FROM subcategorias WHERE subcategoria = ?";
    private static final String SELECT_LAST_PRODUCT_ID = "SELECT id_produto FROM produto ORDER BY id_produto DESC LIMIT 1";
    private static final String INSERT_IMAGES = "INSERT INTO imagens(endereco,endereco2,id_produto) VALUES(?,?,?)";
    private static final String INSERT_PRODUCT = "INSERT INTO produto(nome_produto,descricao,preco,marca,estoque,id_subcat) "
            + "VALUES (?,?,?,?,?,(SELECT id_subcat FROM subcategorias WHERE subcategoria = ?))";
    private static final String SELECT_CATEGORIES = "SELECT id_categoria, categoria FROM categoria ORDER BY id_categoria ASC";
    private static final String DEFAULT_IMAGE_PATH = "imagens/sem_imagem.jpg";
    private static final String RIGHT_ARROW_PATH = "imagens/seta-direita.png";
    private static final String PRODUCT_IMAGE_PATH = "imagens/produtos/";
    private static final String ERROR_IMAGE_NOT_FOUND = "Imagem não encontrada";
    private static final String ERROR_DB_ACCESS = "Erro ao carregar dados: ";
    private static final String ERROR_GENERIC = "Erro: ";

    // Variáveis de estado
    private int numeroImagens = 0;
    private int contagem = 0;
    private String id_produto;
    private String enderecoImagem1;
    private String enderecoImagem2;
    private File arquivo;
    private String[] enderecosImagens;
    private String Subcategoria;
    private String id_subcat;
    private EscolhaDeSubcategoria janela;
    private String espaco = "";
    private int repeticao = 0;
    private boolean atualizandoMascara = false;

    // Timer para slideshow de imagens
    private final Timer slide = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            LOGGER.info("Alternando imagem no slideshow. Contagem: " + contagem);
            enderecoImagem1 = imagem1.getText();
            enderecoImagem2 = imagem2.getText();
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
     * Busca o ID da subcategoria com base no nome fornecido.
     *
     * @param subcategoria Nome da subcategoria.
     * @return ID da subcategoria.
     */
    public String puxarSubcategoria(String subcategoria) {
        LOGGER.info("Buscando ID da subcategoria: " + subcategoria);
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_SUBCATEGORY_ID)) {
            stmt.setString(1, subcategoria);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Subcategoria = rs.getString("id_subcat");
                    LOGGER.info("ID da subcategoria encontrado: " + Subcategoria);
                }
            }
        } catch (SQLException ex) {
            LOGGER.severe(ERROR_DB_ACCESS + ex.getMessage());
            JOptionPane.showMessageDialog(null, ERROR_DB_ACCESS + ex.getMessage());
        } catch (Exception e) {
            LOGGER.severe(ERROR_GENERIC + e.getMessage());
            JOptionPane.showMessageDialog(null, ERROR_GENERIC + e.getMessage());
        }
        return Subcategoria;
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
            new CadastroProdutos().Avisos("imagens/sinal-de-aviso.png", "Selecione uma imagem");
            return 0;
        }

        if (imagem1.getText().isBlank()) {
            LOGGER.warning("Campo de imagem 1 está vazio.");
            new CadastroProdutos().Avisos("imagens/sinal-de-aviso.png", "Selecione uma imagem para o campo 1");
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
                    new CadastroProdutos().Avisos("imagens/sinal-de-aviso.png",
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
     * Salva as imagens do produto no banco de dados, associando-as ao último ID
     * de produto.
     */
    public void salvarImagem() {
        LOGGER.info("Salvando imagens do produto.");
        try (Connection conexao = Conexao.conexaoBanco(); PreparedStatement stmt = conexao.prepareStatement(SELECT_LAST_PRODUCT_ID); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                id_produto = rs.getString("id_produto");
                LOGGER.info("Último ID de produto obtido: " + id_produto);
            }

            try (PreparedStatement stmt2 = conexao.prepareStatement(INSERT_IMAGES)) {
                stmt2.setString(1, imagem1.getText());
                stmt2.setString(2, imagem2.getText().isBlank() ? espaco : imagem2.getText());
                stmt2.setString(3, id_produto);
                stmt2.execute();
                LOGGER.info("Imagens salvas no banco de dados.");
            }
        } catch (Exception e) {
            LOGGER.severe("Erro ao salvar imagens: " + e.getMessage());
            e.printStackTrace();
        }
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
            marca.getText(), estoqueInicial.getText(), janela.getSubcategoria()
        };

        int camposVazios = 0;
        for (String valor : valoresFormularios) {
            if (valor.isBlank()) {
                camposVazios++;
            }
        }

        if (camposVazios > 0) {
            LOGGER.warning("Encontrados " + camposVazios + " campos vazios.");
            new CadastroProdutos().Avisos("imagens/sinal-de-aviso.png", "Campos não preenchidos");
            return 1;
        }

        LOGGER.info("Todos os campos estão preenchidos.");
        return 0;
    }

    /**
     * Cadastra um novo produto no banco de dados.
     */
    public void cadastrarProduto() {
        LOGGER.info("Cadastrando novo produto.");
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(INSERT_PRODUCT)) {
            LOGGER.info("Dados do produto: " + nomeProduto.getText() + ", " + descricao.getText() + ", "
                    + preco.getText() + ", " + marca.getText() + ", " + estoqueInicial.getText() + ", " + janela.getSubcategoria());

            stmt.setString(1, nomeProduto.getText());
            stmt.setString(2, descricao.getText());
            stmt.setString(3, preco.getText());
            stmt.setString(4, marca.getText());
            stmt.setString(5, estoqueInicial.getText());
            stmt.setString(6, janela.getSubcategoria());
            stmt.execute();
            LOGGER.info("Produto cadastrado com sucesso.");
            janela.setSubcategoria("");
        } catch (Exception e) {
            LOGGER.severe("Erro ao cadastrar produto: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Aconteceu algum erro", "Exceção", JOptionPane.INFORMATION_MESSAGE);
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
        }
    }

    /**
     * Carrega as categorias do banco de dados e preenche o JComboBox.
     */
    public void carregarCategorias() {
        LOGGER.info("Carregando categorias do banco de dados.");
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_CATEGORIES); ResultSet rs = stmt.executeQuery()) {
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
                        textField.setText(currencyFormat.format(Double.parseDouble(text.replaceAll("[^0-9]", ""))));
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
        jLabel3.setText("Produto");

        nomeProduto.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        nomeProduto.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                nomeProdutoFocusLost(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel4.setText("Descrição:");

        descricao.setColumns(20);
        descricao.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        descricao.setRows(5);
        jScrollPane1.setViewportView(descricao);

        jLabel5.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel5.setText("Preço:");

        jLabel6.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel6.setText("Marca:");

        preco.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N

        jLabel7.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel7.setText("Estoque inicial:");

        estoqueInicial.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel9.setText("Categoria:");

        categorias.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        categorias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                categoriasActionPerformed(evt);
            }
        });

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
        imagem1.setEnabled(false);

        imagem2.setBackground(new java.awt.Color(204, 204, 255));
        imagem2.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        imagem2.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        imagem2.setEnabled(false);

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

        javax.swing.GroupLayout painelProdutosLayout = new javax.swing.GroupLayout(painelProdutos);
        painelProdutos.setLayout(painelProdutosLayout);
        painelProdutosLayout.setHorizontalGroup(
            painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelProdutosLayout.createSequentialGroup()
                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelProdutosLayout.createSequentialGroup()
                        .addGap(0, 1, Short.MAX_VALUE)
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
                        .addGap(64, 64, 64)
                        .addComponent(btSelecionarImagens)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(painelProdutosLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(nomeProduto))
                    .addComponent(jScrollPane1)
                    .addComponent(jLabel4)
                    .addGroup(painelProdutosLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(47, 47, 47)
                        .addComponent(preco, javax.swing.GroupLayout.PREFERRED_SIZE, 389, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(123, 123, 123)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(marca, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelProdutosLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(categorias, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(70, 70, 70)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(estoqueInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(painelProdutosLayout.createSequentialGroup()
                            .addComponent(btCadastrar)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(btCancelar))
                        .addGroup(painelProdutosLayout.createSequentialGroup()
                            .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(imagem2, javax.swing.GroupLayout.DEFAULT_SIZE, 797, Short.MAX_VALUE)
                                .addComponent(imagem1))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(btExcluir)
                                .addComponent(btExcluir2)))))
                .addContainerGap(13, Short.MAX_VALUE))
        );
        painelProdutosLayout.setVerticalGroup(
            painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelProdutosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE))
            .addGroup(painelProdutosLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(painelProdutosLayout.createSequentialGroup()
                        .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nomeProduto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(32, 32, 32)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel6)
                                .addComponent(marca))
                            .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(preco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel5)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btSelecionarImagens))
                    .addGroup(painelProdutosLayout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelProdutosLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(passarImagem))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelProdutosLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 21, Short.MAX_VALUE)
                                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel9)
                                        .addComponent(categorias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel7)
                                        .addComponent(estoqueInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel10))
                                .addGap(13, 13, 13)))))
                .addGap(28, 28, 28)
                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(imagem1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(imagem2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btExcluir2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(painelProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btCadastrar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btCancelar))
                .addGap(18, 18, 18))
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
                    salvarImagem();
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

    private void categoriasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_categoriasActionPerformed
        JComboBox source = (JComboBox) evt.getSource();
        if (source.getSelectedItem() != null && !source.getSelectedItem().toString().isEmpty()) {  // Verifica se há uma categoria selecionada
            String id_categoria = String.valueOf(source.getSelectedItem().toString().charAt(0));

            try {
                janela = new EscolhaDeSubcategoria(id_categoria);
                janela.setLocation(categorias.getX(), categorias.getY());
                janela.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace(); // Imprime qualquer erro que possa ocorrer
            }
        }
    }//GEN-LAST:event_categoriasActionPerformed

    private void nomeProdutoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nomeProdutoFocusLost
        try (Connection con = Conexao.conexaoBanco()) {
            PreparedStatement stmt = con.prepareStatement("SELECT nome_produto FROM produto WHERE nome_produto = ?");
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCadastrar;
    private javax.swing.JButton btCancelar;
    private javax.swing.JButton btExcluir;
    private javax.swing.JButton btExcluir2;
    private javax.swing.JButton btSelecionarImagens;
    private javax.swing.JComboBox<String> categorias;
    private javax.swing.JTextArea descricao;
    private javax.swing.JTextField estoqueInicial;
    private javax.swing.JTextField imagem1;
    private javax.swing.JTextField imagem2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField marca;
    private javax.swing.JTextField nomeProduto;
    private javax.swing.JPanel painelProdutos;
    private javax.swing.JLabel passarImagem;
    private javax.swing.JTextField preco;
    private javax.swing.JLabel sem_imagem;
    // End of variables declaration//GEN-END:variables
}
