
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

/**
 *
 * @author kaua-n-c
 */
public class EditarProdutos extends javax.swing.JInternalFrame {

    private String produtoPesquisado = "SELECT * FROM produto WHERE produto = ?";
    private String pesquisarPorId = "SELECT * FROM produto WHERE id_produto = ?";
    private String atualizarProduto = "UPDATE produto SET "
            + "produto = ?, "
            + "descricao = ?, "
            + "descricao_curta = ?, "
            + "preco = ?, "
            + "preco_promocional = ?, "
            + "promocao = ?, "
            + "marca = ?, "
            + "avaliacao = ?, "
            + "quantidade_avaliacoes = ?, "
            + "estoque = ?, "
            + "parcelas_permitidas = ?, "
            + "peso_kg = ?, "
            + "dimensoes = ?, "
            + "ativo = ?, "
            + "imagem_1 = ?, "
            + "imagem_2 = ?, "
            + "categoria = ?, "
            + "data_alteracao = CURRENT_TIMESTAMP "
            + "WHERE id_produto = ?";
    private String buscarCategorias = "SELECT categoria FROM categorias";
    private String buscarTodosProdutos = "SELECT produto FROM produto WHERE LOWER(produto) LIKE ? AND ativo = TRUE";
    private static final String conexaoFalha = "Erro ao conectar ao banco de dados: ";
    private static final String erro_generico = "Erro genêrico";
    private final String semImagemEndereco = "imagens/sem_imagem.jpg";
    private final JPopupMenu sugestoesProdutos = new JPopupMenu();
    private List<String> produtos;
    private final Font fonteItem = new Font("Arial", Font.PLAIN, 15);
    Funcoes funcoes = new Funcoes();
    Connection conexao = null;

    //Campos
    private String id_produto;
    private String produto;
    private String descricao;
    private String descricao_curta;
    private String campoPreco;
    private String preco_promocional;
    private String campoPromocao;
    private String campoMarca;
    private String campoQuantidadeAvaliacoes;
    private String campoEstoque;
    private String campoPeso;
    private String campoDimensoes;
    private Boolean promocao;
    private Boolean produtoAtivo;
    private String categoria;
    private String campoAvaliacao;
    private String campoParcelas;
    private String campoImagem1;
    private String campoImagem2;

    public EditarProdutos() {
        initComponents();
        // Desabilitando campos
        preco.setEnabled(false);
        descricaoGeral.setEnabled(false);
        descricaoCurta.setEnabled(false);
        preco.setEnabled(false);
        precoPromocional.setEnabled(false);
        estoque.setEnabled(false);
        marca.setEnabled(false);
        avaliacao.setEnabled(true);
        totalAvaliacao.setEnabled(true);
        estoque.setEnabled(true);
        parcelas.setEnabled(true);
        peso.setEnabled(true);
        dimensoes.setEnabled(true);
        categorias.setEnabled(true);
        imagem1.setEnabled(true);
        imagem2.setEnabled(true);
        promoSim.setEnabled(true);
        promoNao.setEnabled(true);
        ativoSim.setEnabled(true);
        ativoNao.setEnabled(true);

        buscarCategorias();
        buscarTodosProdutos();
        imagemProduto.setIcon(semImagem());
        excluirImagem1.setIcon(redimensionamentoDeImagem(new ImageIcon("imagens/erro.png"), 42, 40));
        excluirImagem2.setIcon(redimensionamentoDeImagem(new ImageIcon("imagens/erro.png"), 42, 40));
        funcoes.aplicarMascaraNomeNumero(nomeProduto);
        funcoes.aplicarMascaraNome(marca);
        funcoes.aplicarMascaraPreco(preco);
        funcoes.aplicarMascaraPreco(precoPromocional);
        funcoes.aplicarMascaraInteiro(estoque);
        funcoes.aplicarMascaraInteiro(totalAvaliacao);
        funcoes.aplicarMascaraPeso(peso);
        funcoes.aplicarMascaraTextoNumerico(descricaoGeral);
        funcoes.aplicarMascaraTextoNumerico(descricaoCurta);

    }

    public void nomeProdutoCaixaDeNomes(JTextField campo) {
        String pesquisa = campo.getText().trim().toLowerCase();
        sugestoesProdutos.setVisible(false);

        if (!pesquisa.isEmpty()) {
            // Filtra produtos que contêm o texto pesquisado (case insensitive)
            List<String> produtosFiltrados = produtos.stream()
                    .filter(produto -> produto.toLowerCase().contains(pesquisa))
                    .limit(10) // Limita o número de sugestões
                    .collect(Collectors.toList());

            if (!produtosFiltrados.isEmpty()) {
                sugestoesProdutos.removeAll(); // Limpa sugestões anteriores

                for (String produto : produtosFiltrados) {
                    JMenuItem item = new JMenuItem(produto);
                    item.setFont(fonteItem);

                    item.addActionListener(e -> {
                        campo.setText(produto);
                        sugestoesProdutos.setVisible(false);
                        carregarProduto(produtoPesquisado, produto);
                    });

                    // Melhor tratamento do clique do mouse
                    item.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            item.setBackground(new Color(220, 220, 255)); // Feedback visual
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            item.setBackground(null);
                        }
                    });

                    sugestoesProdutos.add(item);
                }

                // Mostra o popup alinhado com o campo
                sugestoesProdutos.show(campo, 0, campo.getHeight());
                sugestoesProdutos.setPreferredSize(new Dimension(
                        campo.getWidth(),
                        Math.min(produtosFiltrados.size() * 25, 200) // Altura máxima
                ));
            }
        }
    }

    private void atualizarProduto() {
        //1. Verificar campos obrigatórios primeiro
        if (camposObrigatorios()) {
            funcoes.Avisos("aviso.jpg", "Preencha todos os campos obrigatórios!");
            return;
        }
        // 2. Verificação de imagens (opcional)
        if (verificacaoDeImagens()) { // Usuário cancelou ou escolheu a opção não 
            return;
        }
        try {
            pegarRespostas();

            try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(atualizarProduto)) {

                // Preenche os parâmetros na mesma ordem da query
                stmt.setString(1, produto);
                stmt.setString(2, descricao);
                stmt.setString(3, descricao_curta);
                stmt.setString(4, campoPreco);
                stmt.setString(5, preco_promocional);
                stmt.setBoolean(6, promocao);
                stmt.setString(7, campoMarca);
                stmt.setString(8, campoAvaliacao);
                stmt.setInt(9, Integer.parseInt(campoQuantidadeAvaliacoes));
                stmt.setInt(10, Integer.parseInt(campoEstoque));
                stmt.setInt(11, Integer.parseInt(campoParcelas));
                stmt.setDouble(12, campoPeso.isEmpty() ? 0 : Double.parseDouble(campoPeso));
                stmt.setString(13, campoDimensoes);
                stmt.setBoolean(14, produtoAtivo);
                stmt.setString(15, campoImagem1);
                stmt.setString(16, campoImagem2.equals("Nenhuma imagem") ? null : campoImagem2);
                stmt.setString(17, categoria);
                stmt.setString(18, id_produto); // ID para a cláusula WHERE

                int linhasAfetadas = stmt.executeUpdate();

                if (linhasAfetadas > 0) {
                    JOptionPane.showMessageDialog(null, "Produto atualizado com sucesso!");
                    limpar();
                } else {
                    JOptionPane.showMessageDialog(null, "Nenhum produto foi atualizado. Verifique o ID.");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao atualizar no banco de dados: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Erro em campos numéricos: " + e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro inesperado: " + e.getMessage());
        }
    }

    private void buscarTodosProdutos() {
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(buscarTodosProdutos)) {
            stmt.setString(1, "%" + nomeProduto.getText().toLowerCase() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                produtos = new ArrayList<>();
                while (rs.next()) {
                    produtos.add(rs.getString("produto"));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, conexaoFalha + ex.getMessage());
        }
    }

    private boolean camposObrigatorios() {
        // Validação 1: Nome do Produto
        if (nomeProduto.getText().trim().isEmpty()) {
            funcoes.Avisos("incorreto.jpg", "O campo 'Nome do Produto' é obrigatório!");
            nomeProduto.requestFocus();
            return true; // Sai no primeiro erro
        }
        if (descricaoCurta.getText().trim().isEmpty()) {
            funcoes.Avisos("incorreto.jpg", "O campo 'Nome do Produto' é obrigatório!");
            descricaoCurta.requestFocus();
            return true; // Sai no primeiro erro
        }

        // Validação 2: Descrição Geral
        if (descricaoGeral.getText().trim().isEmpty()) {
            funcoes.Avisos("incorreto.jpg", "O campo 'Descrição Geral' é obrigatório!");
            descricaoGeral.requestFocus();
            return true;
        }

        // Validação 3: Preço
        if (preco.getText().trim().isEmpty()) {
            funcoes.Avisos("incorreto.jpg", "O campo 'Preço' é obrigatório!");
            preco.requestFocus();
            return true;
        }

        // Validação 4: Estoque
        if (estoque.getText().trim().isEmpty()) {
            funcoes.Avisos("incorreto.jpg", "O campo 'Estoque' é obrigatório!");
            estoque.requestFocus();
            return true;
        }

        // Validação 5: Imagem Principal
        if (imagem1.getText().trim().isEmpty()) {
            funcoes.Avisos("incorreto.jpg", "O campo 'Imagem Principal' é obrigatório!");
            imagem1.requestFocus();
            return true;
        }

        // Validação 6: Categoria
        if (categorias.getSelectedItem() == null || categorias.getSelectedItem().toString().trim().isEmpty()) {
            funcoes.Avisos("incorreto.jpg", "Selecione uma 'Categoria'!");
            categorias.requestFocus();
            return true;
        }

        // Validação 7: Preço Promocional (se promoção ativa)
        if (promoSim.isSelected() && precoPromocional.getText().trim().isEmpty()) {
            funcoes.Avisos("incorreto.jpg", "Com promoção ativa, o 'Preço Promocional' é obrigatório!");
            precoPromocional.requestFocus();
            return true;
        }

        // Validação 8: Status Ativo/Inativo
        if (!ativoSim.isSelected() && !ativoNao.isSelected()) {
            funcoes.Avisos("incorreto.jpg", "Selecione o status 'Ativo' do produto!");
            return true;
        }

        return false;
    }

    private void carregarProduto(String query, String parametro) {
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, parametro);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Mapeia os dados do produto
                    id_produto = rs.getString("id_produto");
                    produto = rs.getString("produto");
                    campoPreco = rs.getString("preco");
                    preco_promocional = rs.getString("preco_promocional");
                    promocao = rs.getBoolean("promocao");
                    descricao = rs.getString("descricao");
                    descricao_curta = rs.getString("descricao_curta");
                    campoMarca = rs.getString("marca");
                    campoAvaliacao = rs.getString("avaliacao");
                    campoQuantidadeAvaliacoes = rs.getString("quantidade_avaliacoes");
                    campoEstoque = rs.getString("estoque");
                    campoParcelas = rs.getString("parcelas_permitidas");
                    campoPeso = rs.getString("peso_kg");
                    campoDimensoes = rs.getString("dimensoes");
                    categoria = rs.getString("categoria");
                    produtoAtivo = rs.getBoolean("ativo");
                    campoImagem1 = rs.getString("imagem_1");
                    campoImagem2 = rs.getString("imagem_2");

                    // Atualiza campos na interface
                    codigo.setText(id_produto);
                    nomeProduto.setText(produto);
                    preco.setText(campoPreco);
                    precoPromocional.setText(preco_promocional);
                    if (promocao) {
                        promoSim.setSelected(true);
                        precoPromocional.setEnabled(true);
                    } else {
                        promoNao.setSelected(true);
                        precoPromocional.setEnabled(false);
                    }
                    descricaoGeral.setText(descricao);
                    descricaoCurta.setText(descricao_curta);
                    marca.setText(campoMarca);
                    avaliacao.setSelectedItem(campoAvaliacao);
                    totalAvaliacao.setText(campoQuantidadeAvaliacoes);
                    estoque.setText(campoEstoque);
                    parcelas.setSelectedItem(campoParcelas);
                    peso.setText(campoPeso);
                    dimensoes.setText(campoDimensoes);
                    categorias.setSelectedItem(categoria);
                    if (produtoAtivo) {
                        ativoSim.setSelected(true);
                    } else {
                        ativoNao.setSelected(false);
                    }
                    imagem1.setText(campoImagem1);
                    imagem2.setText(campoImagem2);

                    carregarImagemURL(imagem1);

                    // Habilita campos para edição
                    nomeProduto.setEnabled(true);
                    preco.setEnabled(true);
                    descricaoGeral.setEnabled(true);
                    descricaoCurta.setEnabled(true);
                    marca.setEnabled(true);
                    avaliacao.setEnabled(true);
                    totalAvaliacao.setEnabled(true);
                    estoque.setEnabled(true);
                    parcelas.setEnabled(true);
                    peso.setEnabled(true);
                    dimensoes.setEnabled(true);
                    categorias.setEnabled(true);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, conexaoFalha + e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, erro_generico + e.getMessage());
        }
    }

    private void pegarRespostas() {
        produto = nomeProduto.getText();
        descricao = descricaoGeral.getText();
        descricao_curta = descricaoCurta.getText();
        campoPreco = preco.getText();
        campoAvaliacao = avaliacao.getSelectedItem()==null ? "0" : String.valueOf(avaliacao.getSelectedItem());
        preco_promocional = precoPromocional.getText().trim().isEmpty() ? "0.00" : precoPromocional.getText();
        campoMarca = marca.getText();
        campoQuantidadeAvaliacoes = totalAvaliacao.getText().trim().isEmpty() ? "0" : totalAvaliacao.getText();
        campoEstoque = estoque.getText();
        campoPeso = peso.getText().trim().isEmpty() ? "0" : peso.getText();
        campoDimensoes = dimensoes.getText().trim().isEmpty() ? "0x0x0" : dimensoes.getText();
        categoria = categorias.getSelectedItem().toString();
        campoImagem1 = imagem1.getText();
        campoImagem2 = imagem2.getText().trim().isEmpty() ? "Nenhuma imagem" : imagem2.getText();

    }

    public void carregarImagemURL(JTextField campo) {

        String imageUrl = campo.getText().trim();
        if (imageUrl.isEmpty() || !imageUrl.contains("http")) {
            return;
        }
        funcoes.mostrarMensagemCarregando();
        System.out.println("Carregando imagem de URL para o campo: " + campo + " " + imageUrl);
        try {
            URL url = new URL(imageUrl);
            BufferedImage image = ImageIO.read(url);
            if (image != null) {
                imagemProduto.setIcon(redimensionamentoDeImagem(new ImageIcon(image), 346, 349));
            } else {
                funcoes.Avisos("aviso.jpg", "Imagem inválida. Tente outra URL.");
                campo.setText("");
            }
        } catch (IOException e) {
            funcoes.Avisos("erro.png", "Falha ao carregar URL. Tente novamente.");
            campo.setText("");
        }
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
        Image pegarImagem = imagem.getImage();
        Image redimensionando = pegarImagem.getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
        return new ImageIcon(redimensionando);
    }

    private boolean verificacaoDeImagens() {
        String img1 = imagem1.getText().trim();
        String img2 = imagem2.getText().trim();

        // Se imagem1 estiver vazia, é erro direto (caso necessário)
        if (img1.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Adicione ao menos uma imagem.");
            imagem1.requestFocus();
            return true;
        }

        // Se imagem2 estiver vazia, perguntar ao usuário se quer continuar com uma só
        if (img2.isEmpty()) {
            int resposta = JOptionPane.showConfirmDialog(
                    null,
                    "Deseja adicionar somente uma imagem?",
                    "Configuração de Imagens",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (resposta == JOptionPane.YES_OPTION) {
                return false; // OK com uma imagem só
            } else if (resposta == JOptionPane.NO_OPTION) {
                imagem2.requestFocus();
                System.out.println("Usuário quer múltiplas imagens");
                return true;
            } else {
                System.out.println("Usuário fechou a janela");
                return true;
            }
        }

        // Se ambas as imagens estiverem preenchidas
        return false;
    }

    private ImageIcon semImagem() {
        ImageIcon imagem = new ImageIcon(semImagemEndereco);
        return redimensionamentoDeImagem(imagem, 346, 349);
    }

    private void buscarCategorias() {
        try {
            conexao = Conexao.conexaoBanco();
            if (conexao == null) {
                funcoes.Avisos("aviso.jpg", "Categorias: conexão não foi possível");
                return;
            }
            PreparedStatement stmt = conexao.prepareStatement(buscarCategorias);
            ResultSet rs = stmt.executeQuery();
            categorias.removeAllItems();
            categorias.addItem("");
            while (rs.next()) {
                categorias.addItem(rs.getString("categoria"));
            }
        } catch (Exception e) {
            funcoes.Avisos("erro.png", e.getMessage() + "Tente novamente mais tarde");
            dispose();
        }
    }

    private void limpar() {
        // Limpa TextFields
        nomeProduto.setText("");
        marca.setText("");
        preco.setText("");
        precoPromocional.setText("");
        estoque.setText("");
        peso.setText("");
        dimensoes.setText("");
        totalAvaliacao.setText("");
        imagem1.setText("");
        imagem2.setText("");

        // Limpa TextAreas
        descricaoGeral.setText("");
        descricaoCurta.setText("");

        // Reseta ComboBoxes
        categorias.setSelectedIndex(0);
        avaliacao.setSelectedIndex(0);
        parcelas.setSelectedIndex(0);

        // Configura RadioButtons
        promoNao.setSelected(true);  // Desativa promoção por padrão
        ativoSim.setSelected(true);  // Ativa produto por padrão

        // Limpa imagens exibidas (se houver)
        imagemProduto.setIcon(null);
        excluirImagem1.setVisible(false);
        excluirImagem2.setVisible(false);

        // Foca no primeiro campo
        nomeProduto.requestFocus();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        gpPromocao = new javax.swing.ButtonGroup();
        gpAtivo = new javax.swing.ButtonGroup();
        imagemProduto = new javax.swing.JLabel();
        nomeProduto = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        preco = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        precoPromocional = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        promoSim = new javax.swing.JRadioButton();
        promoNao = new javax.swing.JRadioButton();
        jLabel7 = new javax.swing.JLabel();
        totalAvaliacao = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        avaliacao = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        marca = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        estoque = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        peso = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        dimensoes = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        ativoSim = new javax.swing.JRadioButton();
        ativoNao = new javax.swing.JRadioButton();
        jLabel9 = new javax.swing.JLabel();
        parcelas = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        categorias = new javax.swing.JComboBox<>();
        jLabel16 = new javax.swing.JLabel();
        imagem2 = new javax.swing.JTextField();
        imagem1 = new javax.swing.JTextField();
        btAtualizar = new javax.swing.JButton();
        btCancelar = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        descricaoGeral = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        descricaoCurta = new javax.swing.JTextArea();
        excluirImagem2 = new javax.swing.JLabel();
        excluirImagem1 = new javax.swing.JLabel();
        codigo = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();

        setClosable(true);
        setIconifiable(true);
        setTitle("Cadastrar Produtos");

        nomeProduto.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        nomeProduto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nomeProdutoKeyReleased(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel1.setText("Produto");

        jLabel2.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel2.setText("Descrição");

        jLabel3.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel3.setText("Descrição curta");

        jLabel4.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel4.setText("Preço");

        preco.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel5.setText("Preço promocional");

        precoPromocional.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel6.setText("Promoção");

        gpPromocao.add(promoSim);
        promoSim.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        promoSim.setText("Sim");
        promoSim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                promoSimActionPerformed(evt);
            }
        });

        gpPromocao.add(promoNao);
        promoNao.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        promoNao.setText("Não");
        promoNao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                promoNaoActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel7.setText("Marca");

        totalAvaliacao.setFont(new java.awt.Font("Inter Light", 0, 16)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel8.setText("Avaliação");

        avaliacao.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        avaliacao.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "0.5", "1", "1.5", "2", "2.5", "3", "3.5", "4", "4.5", "5" }));
        avaliacao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                avaliacaoActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel10.setText("Total de avaliações");

        marca.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N

        jLabel11.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel11.setText("Estoque");

        estoque.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N

        jLabel12.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel12.setText("Peso (kg)");

        peso.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N

        jLabel13.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel13.setText("Dimensões");

        dimensoes.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N

        jLabel14.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel14.setText("Ativo");

        gpAtivo.add(ativoSim);
        ativoSim.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        ativoSim.setText("Sim");
        ativoSim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ativoSimActionPerformed(evt);
            }
        });

        gpAtivo.add(ativoNao);
        ativoNao.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        ativoNao.setText("Não");
        ativoNao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ativoNaoActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel9.setText("Parcelas");

        parcelas.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        parcelas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", " " }));
        parcelas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parcelasActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel15.setText("Categorias");

        categorias.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        categorias.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " " }));

        jLabel16.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel16.setText("Imagens (URL)");

        imagem2.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        imagem2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                imagem2FocusLost(evt);
            }
        });

        imagem1.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        imagem1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                imagem1FocusLost(evt);
            }
        });

        btAtualizar.setBackground(new java.awt.Color(102, 255, 51));
        btAtualizar.setFont(new java.awt.Font("Inter SemiBold", 1, 18)); // NOI18N
        btAtualizar.setForeground(new java.awt.Color(255, 255, 255));
        btAtualizar.setText("Atualizar");
        btAtualizar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btAtualizarMouseClicked(evt);
            }
        });

        btCancelar.setBackground(new java.awt.Color(153, 153, 153));
        btCancelar.setFont(new java.awt.Font("Inter SemiBold", 1, 18)); // NOI18N
        btCancelar.setForeground(new java.awt.Color(255, 255, 255));
        btCancelar.setText("Cancelar");
        btCancelar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btCancelarMouseClicked(evt);
            }
        });

        descricaoGeral.setColumns(20);
        descricaoGeral.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        descricaoGeral.setRows(5);
        jScrollPane3.setViewportView(descricaoGeral);

        descricaoCurta.setColumns(20);
        descricaoCurta.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        descricaoCurta.setRows(5);
        jScrollPane4.setViewportView(descricaoCurta);

        excluirImagem2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                excluirImagem2MouseClicked(evt);
            }
        });

        excluirImagem1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                excluirImagem1MouseClicked(evt);
            }
        });

        codigo.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        codigo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                codigoKeyPressed(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel17.setText("Cod");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(imagem1, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(imagem2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 1234, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel11)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(estoque, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(ativoSim)
                                        .addGap(18, 18, 18)
                                        .addComponent(ativoNao)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel12)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(peso, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(preco, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel6)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(promoSim)
                                        .addGap(18, 18, 18)
                                        .addComponent(promoNao)
                                        .addGap(28, 28, 28)
                                        .addComponent(jLabel5)
                                        .addGap(18, 18, 18)
                                        .addComponent(precoPromocional)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel15)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(categorias, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(758, 758, 758)
                                                .addComponent(jLabel13))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(jLabel7)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(marca, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addComponent(jLabel16))
                                                .addGap(35, 35, 35)
                                                .addComponent(jLabel9)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(parcelas, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel8)))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(avaliacao, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jLabel10)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(totalAvaliacao, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(36, 36, 36)
                                                .addComponent(dimensoes, javax.swing.GroupLayout.PREFERRED_SIZE, 382, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(excluirImagem2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(excluirImagem1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(imagemProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 346, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nomeProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 672, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(codigo, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3)
                                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 915, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(58, 58, 58))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btAtualizar)
                .addGap(18, 18, 18)
                .addComponent(btCancelar)
                .addGap(37, 37, 37))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nomeProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(codigo, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(imagemProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(preco, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(precoPromocional)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(promoNao, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(categorias, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 20, Short.MAX_VALUE))
                            .addComponent(promoSim, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(dimensoes, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(peso, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(estoque, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(ativoNao, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ativoSim, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(12, 12, 12)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(avaliacao, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(totalAvaliacao, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(parcelas, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(marca, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 22, Short.MAX_VALUE)
                                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(imagem1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(imagem2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(excluirImagem1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(excluirImagem2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btAtualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void promoNaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_promoNaoActionPerformed
        promocao = false;
        precoPromocional.setEnabled(false);
        precoPromocional.setText("");
    }//GEN-LAST:event_promoNaoActionPerformed

    private void ativoNaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ativoNaoActionPerformed
        produtoAtivo = false;
        System.out.println("produtoAtivo = " + produtoAtivo);
    }//GEN-LAST:event_ativoNaoActionPerformed

    private void promoSimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_promoSimActionPerformed
        promocao = true;
        precoPromocional.setEnabled(true);
        precoPromocional.setText("");
        System.out.println("promoção = " + promocao);
    }//GEN-LAST:event_promoSimActionPerformed

    private void ativoSimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ativoSimActionPerformed
        produtoAtivo = true;
        System.out.println("produtoAtivo = " + produtoAtivo);
    }//GEN-LAST:event_ativoSimActionPerformed

    private void avaliacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_avaliacaoActionPerformed
        campoAvaliacao = avaliacao.getSelectedItem().toString();
        System.out.println("campoAvaliacao = " + campoAvaliacao);
    }//GEN-LAST:event_avaliacaoActionPerformed

    private void parcelasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_parcelasActionPerformed
        campoParcelas = parcelas.getSelectedItem().toString();
        System.out.println("campoParcelas = " + campoParcelas);
    }//GEN-LAST:event_parcelasActionPerformed

    private void imagem1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_imagem1FocusLost
        carregarImagemURL(imagem1);
    }//GEN-LAST:event_imagem1FocusLost

    private void imagem2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_imagem2FocusLost
        carregarImagemURL(imagem2);
    }//GEN-LAST:event_imagem2FocusLost

    private void excluirImagem1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_excluirImagem1MouseClicked
        imagem1.setText("");
        if (!imagem2.getText().trim().isEmpty()) {
            imagem1.setText(imagem2.getText());
            imagem2.setText("");
        }
    }//GEN-LAST:event_excluirImagem1MouseClicked

    private void excluirImagem2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_excluirImagem2MouseClicked
        if (!imagem1.getText().trim().isEmpty()) {
            imagem1.setText(imagem2.getText());
            imagem2.setText("");
        } else {
            imagem2.setText("");
        }
    }//GEN-LAST:event_excluirImagem2MouseClicked

    private void btAtualizarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btAtualizarMouseClicked
        atualizarProduto();
    }//GEN-LAST:event_btAtualizarMouseClicked

    private void btCancelarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btCancelarMouseClicked
        limpar();
    }//GEN-LAST:event_btCancelarMouseClicked

    private void nomeProdutoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nomeProdutoKeyReleased
        nomeProdutoCaixaDeNomes(nomeProduto);
    }//GEN-LAST:event_nomeProdutoKeyReleased

    private void codigoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_codigoKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            carregarProduto(pesquisarPorId, codigo.getText());
        }
    }//GEN-LAST:event_codigoKeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton ativoNao;
    private javax.swing.JRadioButton ativoSim;
    private javax.swing.JComboBox<String> avaliacao;
    private javax.swing.JButton btAtualizar;
    private javax.swing.JButton btCancelar;
    private javax.swing.JComboBox<String> categorias;
    private javax.swing.JTextField codigo;
    private javax.swing.JTextArea descricaoCurta;
    private javax.swing.JTextArea descricaoGeral;
    private javax.swing.JTextField dimensoes;
    private javax.swing.JTextField estoque;
    private javax.swing.JLabel excluirImagem1;
    private javax.swing.JLabel excluirImagem2;
    private javax.swing.ButtonGroup gpAtivo;
    private javax.swing.ButtonGroup gpPromocao;
    private javax.swing.JTextField imagem1;
    private javax.swing.JTextField imagem2;
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
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextField marca;
    private javax.swing.JTextField nomeProduto;
    private javax.swing.JComboBox<String> parcelas;
    private javax.swing.JTextField peso;
    private javax.swing.JTextField preco;
    private javax.swing.JTextField precoPromocional;
    private javax.swing.JRadioButton promoNao;
    private javax.swing.JRadioButton promoSim;
    private javax.swing.JTextField totalAvaliacao;
    // End of variables declaration//GEN-END:variables
}
