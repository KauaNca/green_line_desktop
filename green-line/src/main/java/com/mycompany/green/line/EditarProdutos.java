package com.mycompany.green.line;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Tela para edição de produtos do sistema
 */
public class EditarProdutos extends javax.swing.JInternalFrame {

    // Consultas SQL
    private static final String SQL_PESQUISAR_PRODUTO = "SELECT * FROM produto WHERE produto = ?";
    private static final String SQL_PESQUISAR_POR_ID = "SELECT * FROM produto WHERE id_produto = ?";
    private static final String SQL_ATUALIZAR_PRODUTO
            = "UPDATE produto SET produto = ?, descricao = ?, descricao_curta = ?, "
            + "preco = ?, preco_promocional = ?, promocao = ?, marca = ?, avaliacao = ?, "
            + "quantidade_avaliacoes = ?, estoque = ?, parcelas_permitidas = ?, "
            + "peso_kg = ?, dimensoes = ?, ativo = ?, imagem_1 = ?, imagem_2 = ?, "
            + "categoria = ?, data_alteracao = CURRENT_TIMESTAMP WHERE id_produto = ?";
    private static final String SQL_BUSCAR_CATEGORIAS = "SELECT categoria FROM categorias";
    private static final String SQL_BUSCAR_PRODUTOS
            = "SELECT produto FROM produto WHERE LOWER(produto) LIKE ? AND ativo = TRUE";

    // Mensagens
    private static final String ERRO_CONEXAO = "Erro ao conectar ao banco de dados: ";
    private static final String ERRO_GERAL = "Erro inesperado";
    private static final String CAMPO_OBRIGATORIO = "Campo obrigatório não preenchido";
    private static final String CAMPO_INVALIDO = "Valor inválido no campo";
    private static final String PATH_SEM_IMAGEM = "imagens/sem_imagem.jpg";

    // Componentes de UI
    private final JPopupMenu menuSugestoes = new JPopupMenu();
    private List<String> listaProdutos;
    private final Font fonteItens = new Font("Arial", Font.PLAIN, 15);

    // Classes auxiliares
    private final Funcoes funcoes = new Funcoes();
    private Connection conexao = null;

    // Campos do produto
    private String idProduto;
    private String nomeProduto;
    private String descricao;
    private String descricaoCurta;
    private String preco;
    private String precoPromocional;
    private boolean emPromocao;
    private boolean ativo;
    private String marca;
    private String avaliacao;
    private int qtdAvaliacoes;
    private int estoque;
    private int parcelasPermitidas;
    private double peso;
    private String dimensoes;
    private String categoria;
    private String imagem1;
    private String imagem2;
    int contadorMensagens = 0;

    public EditarProdutos() {
        initComponents();
        configurarEstadoInicial();
    }

    private void configurarEstadoInicial() {
        desativarCampos(false);
        carregarCategorias();
        carregarTodosProdutos();
        configurarImagensPadrao();
        aplicarMascarasCampos();
        configurarIconeJanela();
        configurarListeners();
        configurarAvisoImagens();
    }

    private void configurarImagensPadrao() {
        imagemProduto.setIcon(obterIconeSemImagem());
        botaoExcluirImagem1.setIcon(redimensionarImagem(new ImageIcon("imagens/erro.png"), 42, 40));
        botaoExcluirImagem2.setIcon(redimensionarImagem(new ImageIcon("imagens/erro.png"), 42, 40));
    }

    private void aplicarMascarasCampos() {
        funcoes.aplicarMascaraNomeNumero(campoPesquisa);
        funcoes.aplicarMascaraNomeNumero(campoNomeProduto);
        funcoes.aplicarMascaraNome(campoMarca);
        funcoes.aplicarMascaraPreco(campoPreco);
        funcoes.aplicarMascaraPreco(campoPrecoPromocional);
        funcoes.aplicarMascaraInteiro(campoEstoque);
        funcoes.aplicarMascaraInteiro(campoTotalAvaliacoes);
        funcoes.aplicarMascaraInteiro(campoCodigo);
        funcoes.aplicarMascaraPeso(campoPeso);
        funcoes.aplicarMascaraTextoNumerico(campoDescricaoGeral);
        funcoes.aplicarMascaraTextoNumerico(campoDescricaoCurta);
    }

    private void configurarAvisoImagens() {
        FocusAdapter avisoImagem = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {

                if (contadorMensagens == 0) {
                    funcoes.Avisos("aviso.png",
                            "Atenção: Informe URLs válidos de imagens\nExemplo: https://www.exemplo.com/imagem.jpg");
                    contadorMensagens++;
                }
            }
        };

        campoImagem1.addFocusListener(avisoImagem);
        campoImagem2.addFocusListener(avisoImagem);
    }

    private void configurarIconeJanela() {
        ImageIcon iconeOriginal = new ImageIcon(getClass().getResource("/imagens/logo.png"));
        Image imagemRedimensionada = iconeOriginal.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        setFrameIcon(new ImageIcon(imagemRedimensionada));
    }

    private void configurarListeners() {
        // Listeners para os campos de pesquisa
        campoPesquisa.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                mostrarSugestoesProdutos(campoPesquisa);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                mostrarSugestoesProdutos(campoPesquisa);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        campoCodigo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    carregarProdutoPorId(campoCodigo.getText());
                }
            }
        });
    }

    private void desativarCampos(boolean ativar) {
        Component[] campos = {
            campoNomeProduto, campoPreco, campoDescricaoGeral, campoDescricaoCurta,
            campoPrecoPromocional, campoEstoque, campoMarca, comboAvaliacao,
            campoTotalAvaliacoes, comboParcelas, campoPeso, campoDimensoes,
            comboCategorias, campoImagem1, campoImagem2, radioPromocaoSim,
            radioPromocaoNao, radioAtivoSim, radioAtivoNao
        };

        for (Component campo : campos) {
            campo.setEnabled(ativar);
        }
    }

    private void mostrarSugestoesProdutos(JTextField campo) {
        String termoPesquisa = campo.getText().trim().toLowerCase();
        menuSugestoes.setVisible(false);

        if (!termoPesquisa.isEmpty()) {
            List<String> produtosFiltrados = listaProdutos.stream()
                    .filter(prod -> prod.toLowerCase().contains(termoPesquisa))
                    .limit(10)
                    .collect(Collectors.toList());

            if (!produtosFiltrados.isEmpty()) {
                menuSugestoes.removeAll();

                for (String produto : produtosFiltrados) {
                    JMenuItem item = criarItemSugestao(produto, campo);
                    menuSugestoes.add(item);
                }

                mostrarMenuPopup(campo, menuSugestoes);
            }
        }
    }

    private JMenuItem criarItemSugestao(String produto, JTextField campo) {
        JMenuItem item = new JMenuItem(produto);
        item.setFont(fonteItens);
        item.addActionListener(e -> {
            campo.setText(produto);
            menuSugestoes.setVisible(false);
            carregarProduto(SQL_PESQUISAR_PRODUTO, produto);
        });

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(220, 220, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(null);
            }
        });

        return item;
    }

    private void mostrarMenuPopup(JTextField campo, JPopupMenu menu) {
        menu.show(campo, 0, campo.getHeight());
        menu.setPreferredSize(new Dimension(
                campo.getWidth(),
                Math.min(menu.getComponentCount() * 25, 200)
        ));
    }

    public void atualizarProduto() {
        if (!validarCamposObrigatorios()) {
            funcoes.Avisos("aviso.jpg", "Preencha todos os campos obrigatórios!");
            return;
        }

        if (!validarImagens()) {
            return;
        }

        try {
            coletarDadosFormulario();

            try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SQL_ATUALIZAR_PRODUTO)) {

                configurarParametrosStatement(stmt);
                int linhasAfetadas = stmt.executeUpdate();

                if (linhasAfetadas > 0) {
                    JOptionPane.showMessageDialog(this, "Produto atualizado com sucesso!",
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    limparFormulario();
                } else {
                    JOptionPane.showMessageDialog(this, "Nenhum produto foi atualizado.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (SQLException e) {
            mostrarErro(ERRO_CONEXAO + e.getMessage());
        } catch (NumberFormatException e) {
            mostrarErro(CAMPO_INVALIDO + ": " + e.getMessage());
        } catch (Exception e) {
            mostrarErro(ERRO_GERAL + ": " + e.getMessage());
        }
    }

    private void configurarParametrosStatement(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, nomeProduto);
        stmt.setString(2, descricao);
        stmt.setString(3, descricaoCurta);
        stmt.setString(4, preco);
        stmt.setString(5, precoPromocional);
        stmt.setBoolean(6, emPromocao);
        stmt.setString(7, marca);
        stmt.setString(8, avaliacao);
        stmt.setInt(9, qtdAvaliacoes);
        stmt.setInt(10, estoque);
        stmt.setInt(11, parcelasPermitidas);
        stmt.setDouble(12, peso);
        stmt.setString(13, dimensoes);
        stmt.setBoolean(14, ativo);
        stmt.setString(15, imagem1);
        stmt.setString(16, imagem2.isEmpty() ? null : imagem2);
        stmt.setString(17, categoria);
        stmt.setString(18, idProduto);
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private boolean validarCamposObrigatorios() {
        if (campoNomeProduto.getText().trim().isEmpty()) {
            campoNomeProduto.requestFocus();
            return false;
        }
        if (campoDescricaoGeral.getText().trim().isEmpty()) {
            campoDescricaoGeral.requestFocus();
            return false;
        }
        if (campoPreco.getText().trim().isEmpty()) {
            campoPreco.requestFocus();
            return false;
        }
        if (campoEstoque.getText().trim().isEmpty()) {
            campoEstoque.requestFocus();
            return false;
        }
        if (campoImagem1.getText().trim().isEmpty()) {
            campoImagem1.requestFocus();
            return false;
        }
        if (comboCategorias.getSelectedItem() == null) {
            comboCategorias.requestFocus();
            return false;
        }
        if (radioPromocaoSim.isSelected() && campoPrecoPromocional.getText().trim().isEmpty()) {
            campoPrecoPromocional.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validarImagens() {
        String img1 = campoImagem1.getText().trim();
        String img2 = campoImagem2.getText().trim();

        if (img1.isEmpty()) {
            mostrarErro("Adicione ao menos uma imagem.");
            campoImagem1.requestFocus();
            return false;
        }

        if (img2.isEmpty()) {
            int resposta = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja adicionar somente uma imagem?",
                    "Configuração de Imagens",
                    JOptionPane.YES_NO_OPTION
            );

            if (resposta != JOptionPane.YES_OPTION) {
                campoImagem2.requestFocus();
                return false;
            }
        }

        return true;
    }

    private void coletarDadosFormulario() {
        // Coleta dos dados com valores padrão alternativos
        idProduto = campoCodigo.getText().trim();

        nomeProduto = campoNomeProduto.getText().trim();
        if (nomeProduto.isEmpty()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório");
        }

        descricao = campoDescricaoGeral.getText().trim();
        if (descricao.isEmpty()) {
            descricao = "Sem descrição disponível";
        }

        descricaoCurta = campoDescricaoCurta.getText().trim();
        if (descricaoCurta.isEmpty()) {
            descricaoCurta = descricao.length() > 100 ? descricao.substring(0, 100) + "..." : descricao;
        }

        // Tratamento para preço com valor padrão 0.00 se vazio
        preco = campoPreco.getText().trim();
        if (preco.isEmpty()) {
            preco = "0.00";
        }

        // Tratamento para preço promocional
        precoPromocional = campoPrecoPromocional.getText().trim();
        if (precoPromocional.isEmpty()) {
            precoPromocional = emPromocao ? preco : "0.00"; // Se estiver em promoção mas sem valor, usa o preço normal
        }

        // Status de promoção e ativo
        emPromocao = radioPromocaoSim.isSelected(); // Já tem valor padrão (false)
        ativo = radioAtivoSim.isSelected(); // Já tem valor padrão (true)

        marca = campoMarca.getText().trim();
        if (marca.isEmpty()) {
            marca = "Genérico";
        }

        // Avaliação com valor padrão
        avaliacao = comboAvaliacao.getSelectedItem() != null
                ? comboAvaliacao.getSelectedItem().toString() : "0";

        // Quantidade de avaliações com valor padrão 0
        try {
            qtdAvaliacoes = campoTotalAvaliacoes.getText().trim().isEmpty()
                    ? 0 : Integer.parseInt(campoTotalAvaliacoes.getText());
        } catch (NumberFormatException e) {
            qtdAvaliacoes = 0;
        }

        // Estoque com valor padrão 0
        try {
            estoque = campoEstoque.getText().trim().isEmpty()
                    ? 0 : Integer.parseInt(campoEstoque.getText());
        } catch (NumberFormatException e) {
            estoque = 0;
        }

        // Parcelas permitidas com valor padrão 1
        try {
            parcelasPermitidas = comboParcelas.getSelectedItem() != null
                    ? Integer.parseInt(comboParcelas.getSelectedItem().toString()) : 1;
        } catch (NumberFormatException e) {
            parcelasPermitidas = 1;
        }

        // Peso com valor padrão 0
        try {
            peso = campoPeso.getText().trim().isEmpty()
                    ? 0.0 : Double.parseDouble(campoPeso.getText());
        } catch (NumberFormatException e) {
            peso = 0.0;
        }

        dimensoes = campoDimensoes.getText().trim();
        if (dimensoes.isEmpty()) {
            dimensoes = "0x0x0";
        }

        // Categoria com valor padrão "Outros"
        categoria = comboCategorias.getSelectedItem() != null
                ? comboCategorias.getSelectedItem().toString() : "Outros";

        imagem1 = campoImagem1.getText().trim();
        if (imagem1.isEmpty()) {
            throw new IllegalArgumentException("A imagem principal é obrigatória");
        }
        // Imagem 2 com URL padrão se estiver vazia
        imagem2 = campoImagem2.getText().trim();
        if (imagem2.isEmpty()) {
            imagem2 = "https://eletropeldistribuidora.com.br/wp-content/uploads/2020/12/sem-foto.jpg";
        }
    }

    private void carregarProduto(String query, String parametro) {
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, parametro);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    preencherFormulario(rs);
                    desativarCampos(true);
                } else {
                    funcoes.Avisos("aviso.jpg", "Produto não encontrado!");
                }
            }
        } catch (SQLException e) {
            mostrarErro(ERRO_CONEXAO + e.getMessage());
        }
    }

    private void preencherFormulario(ResultSet rs) throws SQLException {
        campoCodigo.setText(rs.getString("id_produto"));
        campoNomeProduto.setText(rs.getString("produto"));
        campoPreco.setText(rs.getString("preco"));
        campoPrecoPromocional.setText(rs.getString("preco_promocional"));

        boolean promocao = rs.getBoolean("promocao");
        radioPromocaoSim.setSelected(promocao);
        radioPromocaoNao.setSelected(!promocao);
        campoPrecoPromocional.setEnabled(promocao);

        campoDescricaoGeral.setText(rs.getString("descricao"));
        campoDescricaoCurta.setText(rs.getString("descricao_curta"));
        campoMarca.setText(rs.getString("marca"));
        comboAvaliacao.setSelectedItem(rs.getString("avaliacao"));
        campoTotalAvaliacoes.setText(rs.getString("quantidade_avaliacoes"));
        campoEstoque.setText(rs.getString("estoque"));
        comboParcelas.setSelectedItem(rs.getString("parcelas_permitidas"));
        campoPeso.setText(rs.getString("peso_kg"));
        campoDimensoes.setText(rs.getString("dimensoes"));
        comboCategorias.setSelectedItem(rs.getString("categoria"));

        boolean ativo = rs.getBoolean("ativo");
        radioAtivoSim.setSelected(ativo);
        radioAtivoNao.setSelected(!ativo);

        campoImagem1.setText(rs.getString("imagem_1"));
        campoImagem2.setText(rs.getString("imagem_2"));

        carregarImagemURL(campoImagem1);
    }

    private void carregarProdutoPorId(String id) {
        carregarProduto(SQL_PESQUISAR_POR_ID, id);
    }

    private void carregarImagemURL(JTextField campo) {
        String urlImagem = campo.getText().trim();
        if (urlImagem.isEmpty() || !urlImagem.startsWith("http")) {
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(urlImagem);
                BufferedImage imagem = ImageIO.read(url);

                SwingUtilities.invokeLater(() -> {
                    if (imagem != null) {
                        imagemProduto.setIcon(redimensionarImagem(new ImageIcon(imagem), 346, 349));
                    } else {
                        funcoes.Avisos("aviso.jpg", "Imagem inválida. Tente outra URL.");
                        campo.setText("");
                    }
                });
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    funcoes.Avisos("erro.png", "Falha ao carregar URL. Verifique o link.");
                    campo.setText("");
                });
            }
        }).start();
    }

    private ImageIcon obterIconeSemImagem() {
        return redimensionarImagem(new ImageIcon(PATH_SEM_IMAGEM), 346, 349);
    }

    private ImageIcon redimensionarImagem(ImageIcon icone, int largura, int altura) {
        Image imagem = icone.getImage();
        Image redimensionada = imagem.getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
        return new ImageIcon(redimensionada);
    }

    private void carregarCategorias() {
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SQL_BUSCAR_CATEGORIAS); ResultSet rs = stmt.executeQuery()) {

            comboCategorias.removeAllItems();
            comboCategorias.addItem("");

            while (rs.next()) {
                comboCategorias.addItem(rs.getString("categoria"));
            }
        } catch (SQLException e) {
            mostrarErro(ERRO_CONEXAO + e.getMessage());
        }
    }

    private void carregarTodosProdutos() {
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SQL_BUSCAR_PRODUTOS)) {

            stmt.setString(1, "%" + campoPesquisa.getText().toLowerCase() + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                listaProdutos = new ArrayList<>();
                while (rs.next()) {
                    listaProdutos.add(rs.getString("produto"));
                }
            }
        } catch (SQLException e) {
            mostrarErro(ERRO_CONEXAO + e.getMessage());
        }
    }

    private void limparFormulario() {
        // Limpar campos de texto
        campoPesquisa.setText("");
        campoNomeProduto.setText("");
        campoMarca.setText("");
        campoPreco.setText("");
        campoPrecoPromocional.setText("");
        campoEstoque.setText("");
        campoPeso.setText("");
        campoDimensoes.setText("");
        campoTotalAvaliacoes.setText("");
        campoImagem1.setText("");
        campoImagem2.setText("");
        campoDescricaoGeral.setText("");
        campoDescricaoCurta.setText("");

        // Resetar comboboxes
        comboCategorias.setSelectedIndex(0);
        comboAvaliacao.setSelectedIndex(0);
        comboParcelas.setSelectedIndex(0);

        // Resetar radio buttons
        radioPromocaoNao.setSelected(true);
        radioAtivoSim.setSelected(true);

        // Resetar imagem
        imagemProduto.setIcon(obterIconeSemImagem());

        // Desativar campos
        desativarCampos(false);

        // Focar no campo de pesquisa
        campoPesquisa.requestFocus();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grupoPromocao = new javax.swing.ButtonGroup();
        grupoAtivo = new javax.swing.ButtonGroup();
        imagemProduto = new javax.swing.JLabel();
        campoPesquisa = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        campoPreco = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        campoPrecoPromocional = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        radioPromocaoSim = new javax.swing.JRadioButton();
        radioPromocaoNao = new javax.swing.JRadioButton();
        jLabel7 = new javax.swing.JLabel();
        campoTotalAvaliacoes = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        comboAvaliacao = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        campoMarca = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        campoEstoque = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        campoPeso = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        campoDimensoes = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        radioAtivoSim = new javax.swing.JRadioButton();
        radioAtivoNao = new javax.swing.JRadioButton();
        jLabel9 = new javax.swing.JLabel();
        comboParcelas = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        comboCategorias = new javax.swing.JComboBox<>();
        jLabel16 = new javax.swing.JLabel();
        campoImagem2 = new javax.swing.JTextField();
        campoImagem1 = new javax.swing.JTextField();
        botaoAtualizar = new javax.swing.JButton();
        botaoCancelar = new javax.swing.JButton();
        scrollDescricao = new javax.swing.JScrollPane();
        campoDescricaoGeral = new javax.swing.JTextArea();
        scrollDescricaoCurta = new javax.swing.JScrollPane();
        campoDescricaoCurta = new javax.swing.JTextArea();
        botaoExcluirImagem2 = new javax.swing.JLabel();
        botaoExcluirImagem1 = new javax.swing.JLabel();
        campoCodigo = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        campoNomeProduto = new javax.swing.JTextField();

        setClosable(true);
        setIconifiable(true);
        setTitle("Editar Produtos");

        campoPesquisa.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        campoPesquisa.setMaximumSize(new java.awt.Dimension(64, 29));
        campoPesquisa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                campoPesquisaKeyReleased(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel1.setText("Produto*");

        jLabel2.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel2.setText("Descrição*");

        jLabel3.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel3.setText("Descrição curta*");

        jLabel4.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel4.setText("Preço*");

        campoPreco.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel5.setText("Preço promocional");

        campoPrecoPromocional.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel6.setText("Promoção*");

        grupoPromocao.add(radioPromocaoSim);
        radioPromocaoSim.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        radioPromocaoSim.setText("Sim");
        radioPromocaoSim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioPromocaoSimActionPerformed(evt);
            }
        });

        grupoPromocao.add(radioPromocaoNao);
        radioPromocaoNao.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        radioPromocaoNao.setText("Não");
        radioPromocaoNao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioPromocaoNaoActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel7.setText("Marca*");

        campoTotalAvaliacoes.setFont(new java.awt.Font("Inter Light", 0, 16)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel8.setText("Avaliação");

        comboAvaliacao.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        comboAvaliacao.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "0.5", "1", "1.5", "2", "2.5", "3", "3.5", "4", "4.5", "5" }));
        comboAvaliacao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboAvaliacaoActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel10.setText("Total de avaliações");

        campoMarca.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N

        jLabel11.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel11.setText("Estoque*");

        campoEstoque.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N

        jLabel12.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel12.setText("Peso (kg)");

        campoPeso.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N

        jLabel13.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel13.setText("Dimensões");

        campoDimensoes.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N

        jLabel14.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel14.setText("Ativo*");

        grupoAtivo.add(radioAtivoSim);
        radioAtivoSim.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        radioAtivoSim.setText("Sim");
        radioAtivoSim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioAtivoSimActionPerformed(evt);
            }
        });

        grupoAtivo.add(radioAtivoNao);
        radioAtivoNao.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        radioAtivoNao.setText("Não");
        radioAtivoNao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioAtivoNaoActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel9.setText("Parcelas*");

        comboParcelas.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        comboParcelas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", " " }));
        comboParcelas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboParcelasActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel15.setText("Categorias*");

        comboCategorias.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        comboCategorias.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " " }));

        jLabel16.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel16.setText("Imagens (URL)");

        campoImagem2.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        campoImagem2.setMaximumSize(new java.awt.Dimension(64, 29));
        campoImagem2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                campoImagem2FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoImagem2FocusLost(evt);
            }
        });

        campoImagem1.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        campoImagem1.setMaximumSize(new java.awt.Dimension(64, 29));
        campoImagem1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                campoImagem1FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoImagem1FocusLost(evt);
            }
        });

        botaoAtualizar.setBackground(new java.awt.Color(102, 255, 51));
        botaoAtualizar.setFont(new java.awt.Font("Inter SemiBold", 1, 18)); // NOI18N
        botaoAtualizar.setForeground(new java.awt.Color(255, 255, 255));
        botaoAtualizar.setText("Atualizar");
        botaoAtualizar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botaoAtualizarMouseClicked(evt);
            }
        });

        botaoCancelar.setBackground(new java.awt.Color(153, 153, 153));
        botaoCancelar.setFont(new java.awt.Font("Inter SemiBold", 1, 18)); // NOI18N
        botaoCancelar.setForeground(new java.awt.Color(255, 255, 255));
        botaoCancelar.setText("Cancelar");
        botaoCancelar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botaoCancelarMouseClicked(evt);
            }
        });

        campoDescricaoGeral.setColumns(20);
        campoDescricaoGeral.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        campoDescricaoGeral.setRows(5);
        scrollDescricao.setViewportView(campoDescricaoGeral);

        campoDescricaoCurta.setColumns(20);
        campoDescricaoCurta.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        campoDescricaoCurta.setRows(5);
        scrollDescricaoCurta.setViewportView(campoDescricaoCurta);

        botaoExcluirImagem2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botaoExcluirImagem2MouseClicked(evt);
            }
        });

        botaoExcluirImagem1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botaoExcluirImagem1MouseClicked(evt);
            }
        });

        campoCodigo.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        campoCodigo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                campoCodigoKeyPressed(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel17.setText("Cod");

        campoNomeProduto.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        campoNomeProduto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                campoNomeProdutoKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(imagemProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 346, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(scrollDescricaoCurta)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(campoPesquisa, javax.swing.GroupLayout.PREFERRED_SIZE, 741, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel17)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(campoCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel2)
                                            .addComponent(jLabel3))
                                        .addGap(0, 789, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(campoNomeProduto)))
                                .addGap(4, 4, 4))
                            .addComponent(scrollDescricao)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(campoMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(35, 35, 35)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(comboParcelas, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel16))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel8)
                        .addGap(61, 61, 61)
                        .addComponent(comboAvaliacao, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(campoTotalAvaliacoes, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(campoImagem1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(campoImagem2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 1234, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(botaoExcluirImagem2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(botaoExcluirImagem1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(campoPreco, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(campoEstoque, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioAtivoSim)
                                .addGap(18, 18, 18)
                                .addComponent(radioAtivoNao)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(campoPeso, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel13)
                                .addGap(36, 36, 36)
                                .addComponent(campoDimensoes, javax.swing.GroupLayout.PREFERRED_SIZE, 382, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioPromocaoSim)
                                .addGap(18, 18, 18)
                                .addComponent(radioPromocaoNao)
                                .addGap(28, 28, 28)
                                .addComponent(jLabel5)
                                .addGap(18, 18, 18)
                                .addComponent(campoPrecoPromocional)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboCategorias, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(54, 54, 54))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(botaoAtualizar)
                .addGap(18, 18, 18)
                .addComponent(botaoCancelar)
                .addGap(37, 37, 37))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(campoPesquisa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(campoCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(campoNomeProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scrollDescricaoCurta, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scrollDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(imagemProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(radioPromocaoNao, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                                        .addComponent(radioPromocaoSim, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(comboCategorias, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
                                    .addComponent(campoPreco, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(campoPrecoPromocional, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(campoEstoque, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(radioAtivoSim, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(radioAtivoNao, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(campoPeso, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(campoDimensoes, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 18, Short.MAX_VALUE)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(comboAvaliacao, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(campoTotalAvaliacoes, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(campoMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(comboParcelas, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(campoImagem1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botaoExcluirImagem1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(campoImagem2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botaoExcluirImagem2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botaoAtualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botaoCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void radioPromocaoNaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioPromocaoNaoActionPerformed
        emPromocao = false;
        campoPrecoPromocional.setEnabled(false);
        campoPrecoPromocional.setText("");
    }//GEN-LAST:event_radioPromocaoNaoActionPerformed

    private void radioAtivoNaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioAtivoNaoActionPerformed
        ativo = false;
    }//GEN-LAST:event_radioAtivoNaoActionPerformed

    private void radioPromocaoSimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioPromocaoSimActionPerformed
        emPromocao = true;
        campoPrecoPromocional.setEnabled(true);
    }//GEN-LAST:event_radioPromocaoSimActionPerformed

    private void radioAtivoSimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioAtivoSimActionPerformed
        ativo = true;
    }//GEN-LAST:event_radioAtivoSimActionPerformed

    private void comboAvaliacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboAvaliacaoActionPerformed
        avaliacao = comboAvaliacao.getSelectedItem().toString();
    }//GEN-LAST:event_comboAvaliacaoActionPerformed

    private void comboParcelasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboParcelasActionPerformed
        parcelasPermitidas = Integer.parseInt(comboParcelas.getSelectedItem().toString());
    }//GEN-LAST:event_comboParcelasActionPerformed

    private void campoImagem1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoImagem1FocusLost
        carregarImagemURL(campoImagem1);
    }//GEN-LAST:event_campoImagem1FocusLost

    private void campoImagem2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoImagem2FocusLost
        carregarImagemURL(campoImagem2);
    }//GEN-LAST:event_campoImagem2FocusLost

    private void botaoExcluirImagem1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botaoExcluirImagem1MouseClicked
        campoImagem1.setText("");
        if (!campoImagem2.getText().trim().isEmpty()) {
            campoImagem1.setText(campoImagem2.getText());
            campoImagem2.setText("");
        }
    }//GEN-LAST:event_botaoExcluirImagem1MouseClicked

    private void botaoExcluirImagem2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botaoExcluirImagem2MouseClicked
        if (!campoImagem1.getText().trim().isEmpty()) {
            campoImagem1.setText(campoImagem2.getText());
            campoImagem2.setText("");
        } else {
            campoImagem2.setText("");
        }
    }//GEN-LAST:event_botaoExcluirImagem2MouseClicked

    private void botaoAtualizarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botaoAtualizarMouseClicked
        atualizarProduto();
    }//GEN-LAST:event_botaoAtualizarMouseClicked

    private void botaoCancelarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botaoCancelarMouseClicked
        limparFormulario();

    }//GEN-LAST:event_botaoCancelarMouseClicked

    private void campoPesquisaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoPesquisaKeyReleased

        // nomeProdutoCaixaDeNomes(campoPesquisa);

    }//GEN-LAST:event_campoPesquisaKeyReleased

    private void campoCodigoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoCodigoKeyPressed
        /* if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            carregarProduto(pesquisarPorId, campoCodigo.getText());
        }*/
    }//GEN-LAST:event_campoCodigoKeyPressed

    private void campoNomeProdutoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoNomeProdutoKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_campoNomeProdutoKeyReleased

    private void campoImagem1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoImagem1FocusGained
        /*  if (contadorMensagem == 0) {
            contadorMensagem += 1;
            funcoes.Avisos("aviso.png", "Atenção: Para imagens, você deve fornecer URLs válidos de imagens da internet.\nExemplo: https://www.exemplo.com/imagem.jpg");
        }*/

    }//GEN-LAST:event_campoImagem1FocusGained

    private void campoImagem2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoImagem2FocusGained
        /* if (contadorMensagem == 0) {
            funcoes.Avisos("aviso.png", "Atenção: Para imagens, você deve fornecer URLs válidos de imagens da internet.\nExemplo: https://www.exemplo.com/imagem.jpg");
            contadorMensagem += 1;
        }*/

    }//GEN-LAST:event_campoImagem2FocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botaoAtualizar;
    private javax.swing.JButton botaoCancelar;
    private javax.swing.JLabel botaoExcluirImagem1;
    private javax.swing.JLabel botaoExcluirImagem2;
    private javax.swing.JTextField campoCodigo;
    private javax.swing.JTextArea campoDescricaoCurta;
    private javax.swing.JTextArea campoDescricaoGeral;
    private javax.swing.JTextField campoDimensoes;
    private javax.swing.JTextField campoEstoque;
    private javax.swing.JTextField campoImagem1;
    private javax.swing.JTextField campoImagem2;
    private javax.swing.JTextField campoMarca;
    private javax.swing.JTextField campoNomeProduto;
    private javax.swing.JTextField campoPeso;
    private javax.swing.JTextField campoPesquisa;
    private javax.swing.JTextField campoPreco;
    private javax.swing.JTextField campoPrecoPromocional;
    private javax.swing.JTextField campoTotalAvaliacoes;
    private javax.swing.JComboBox<String> comboAvaliacao;
    private javax.swing.JComboBox<String> comboCategorias;
    private javax.swing.JComboBox<String> comboParcelas;
    private javax.swing.ButtonGroup grupoAtivo;
    private javax.swing.ButtonGroup grupoPromocao;
    private javax.swing.JLabel imagemProduto;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JRadioButton radioAtivoNao;
    private javax.swing.JRadioButton radioAtivoSim;
    private javax.swing.JRadioButton radioPromocaoNao;
    private javax.swing.JRadioButton radioPromocaoSim;
    private javax.swing.JScrollPane scrollDescricao;
    private javax.swing.JScrollPane scrollDescricaoCurta;
    // End of variables declaration//GEN-END:variables
}
