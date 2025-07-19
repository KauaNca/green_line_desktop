package com.mycompany.green.line;


import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author kaua-n-c
 */
public class CadastroProdutos extends javax.swing.JInternalFrame {

    private String produtoRepetido = "SELECT produto FROM produto WHERE produto = ?";
    private String cadastrarProduto = "INSERT INTO produto("
            + "produto, descricao, descricao_curta, preco, preco_promocional, "
            + "promocao, marca, avaliacao, quantidade_avaliacoes, estoque, "
            + "parcelas_permitidas, peso_kg, dimensoes, ativo, "
            + "imagem_1, imagem_2, categoria) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private String buscarCategorias = "SELECT categoria FROM categorias";
    private final String semImagemEndereco = "imagens/sem_imagem.jpg";
    Funcoes funcoes = new Funcoes();
    Connection conexao = null;

    //Campos
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

    public CadastroProdutos() {
        initComponents();
        promocao = false;
        produtoAtivo = true;
        imagemProduto.setIcon(semImagem());
        excluirImagem1.setIcon(redimensionamentoDeImagem(new ImageIcon("imagens/erro.png"), 42, 40));
        excluirImagem2.setIcon(redimensionamentoDeImagem(new ImageIcon("imagens/erro.png"), 42, 40));
        promoNao.setSelected(true);
        precoPromocional.setEnabled(false);
        funcoes.aplicarMascaraNomeNumero(nomeProduto);
        funcoes.aplicarMascaraNome(marca);
        funcoes.aplicarMascaraPreco(preco);
        funcoes.aplicarMascaraPreco(precoPromocional);
        funcoes.aplicarMascaraInteiro(estoque);
        funcoes.aplicarMascaraInteiro(totalAvaliacao);
        funcoes.aplicarMascaraPeso(peso);
        funcoes.aplicarMascaraTextoNumerico(descricaoGeral);
        funcoes.aplicarMascaraTextoNumerico(descricaoCurta);
        buscarCategorias();
    }

    private void cadastrarProduto() {
        // 1. Validar campos obrigatórios
        if (!camposObrigatorios()) {
            funcoes.Avisos("aviso.jpg", "Preencha todos os campos obrigatórios!");
            return;
        }

        // 2. Verificar imagens (opcional)
        if (verificacaoDeImagens()) {
            return;
        }

        Connection conexao = null;
        PreparedStatement stmtCheck = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            System.out.println("Iniciando cadastro...");
            pegarRespostas(); // Obter valores dos campos

            // 3. Validar valores numéricos
            try {
                new BigDecimal(campoPreco);
                new BigDecimal(preco_promocional);
                Integer.parseInt(campoEstoque);
                Integer.parseInt(campoParcelas);
                Double.parseDouble(campoPeso);
                Double.parseDouble(campoAvaliacao);
                Integer.parseInt(campoQuantidadeAvaliacoes);
            } catch (NumberFormatException e) {
                funcoes.Avisos("erro.png", "Valores numéricos inválidos! Verifique os campos numéricos.");
                return;
            }

            // 4. Estabelecer conexão
            conexao = Conexao.conexaoBanco();
            if (conexao == null) {
                funcoes.Avisos("aviso.jpg", "Erro na conexão com o banco de dados!");
                return;
            }

            // 5. Verificar duplicidade de produto e categoria
            String sqlCheckProduto = "SELECT COUNT(*) FROM produtos WHERE produto = ?";
            stmtCheck = conexao.prepareStatement(sqlCheckProduto);
            stmtCheck.setString(1, produto.trim());
            rs = stmtCheck.executeQuery();
            rs.next();
            int countProduto = rs.getInt(1);
            rs.close();
            stmtCheck.close();

            if (countProduto > 0) {
                funcoes.Avisos("sinal-de-aviso.png", "Produto ja existente. Por gentileza, insira outra");
                return;

            }

            // 6. Inserir produto
            stmt = conexao.prepareStatement(cadastrarProduto);
            stmt.setString(1, produto.trim());
            stmt.setString(2, descricao);
            stmt.setString(3, descricao_curta);
            stmt.setBigDecimal(4, new BigDecimal(campoPreco));
            stmt.setBigDecimal(5, new BigDecimal(preco_promocional));
            stmt.setBoolean(6, promocao);
            stmt.setString(7, campoMarca);
            stmt.setDouble(8, Double.parseDouble(campoAvaliacao));
            stmt.setInt(9, Integer.parseInt(campoQuantidadeAvaliacoes));
            stmt.setInt(10, Integer.parseInt(campoEstoque));
            stmt.setInt(11, Integer.parseInt(campoParcelas));
            stmt.setDouble(12, Double.parseDouble(campoPeso));
            stmt.setString(13, campoDimensoes);
            stmt.setBoolean(14, produtoAtivo);
            stmt.setString(15, campoImagem1);
            stmt.setString(16, campoImagem2);
            stmt.setString(17, categoria.trim());

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                funcoes.Avisos("confirmacao.png", "Produto cadastrado com sucesso!");
                limpar();
            } else {
                funcoes.Avisos("erro.png", "Nenhum produto foi cadastrado!");
            }

        } catch (SQLException e) {
            funcoes.Avisos("erro.png", "Erro no banco de dados: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            funcoes.Avisos("erro.png", "Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 7. Fechar recursos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmtCheck != null) {
                    stmtCheck.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conexao != null) {
                    conexao.close();
                }
            } catch (SQLException e) {
                funcoes.Avisos("erro.png", "Erro ao fechar recursos: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void pegarRespostas() {
        produto = nomeProduto.getText();
        descricao = descricaoGeral.getText();
        descricao_curta = descricaoCurta.getText();
        campoPreco = preco.getText();
        preco_promocional = precoPromocional.getText().trim().isEmpty() ? "0.00" : precoPromocional.getText();
        campoMarca = marca.getText();
        campoAvaliacao = avaliacao.getSelectedItem() == null ? "0" : String.valueOf(avaliacao.getSelectedItem());
        campoQuantidadeAvaliacoes = totalAvaliacao.getText().trim().isEmpty() ? "0" : totalAvaliacao.getText();
        campoEstoque = estoque.getText().trim().isEmpty() ? "1" : estoque.getText();
        campoParcelas = parcelas.getSelectedItem() == null ? "1" : String.valueOf(parcelas.getSelectedItem());
        campoPeso = peso.getText().trim().isEmpty() ? "0" : peso.getText();
        campoDimensoes = dimensoes.getText().trim().isEmpty() ? "0x0x0" : dimensoes.getText();
        categoria = categorias.getSelectedItem().toString();
        campoImagem1 = imagem1.getText();
        campoImagem2 = imagem2.getText().trim().isEmpty() ? "" : imagem2.getText();

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
        btCadastrar = new javax.swing.JButton();
        btCancelar = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        descricaoGeral = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        descricaoCurta = new javax.swing.JTextArea();
        excluirImagem2 = new javax.swing.JLabel();
        excluirImagem1 = new javax.swing.JLabel();

        setClosable(true);
        setIconifiable(true);
        setTitle("Cadastrar Produtos");

        nomeProduto.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N

        jLabel1.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel1.setText("Produto*");

        jLabel2.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel2.setText("Descrição*");

        jLabel3.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel3.setText("Descrição curta*");

        jLabel4.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel4.setText("Preço*");

        preco.setFont(new java.awt.Font("Inter Light", 0, 16)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel5.setText("Preço promocional*");

        precoPromocional.setFont(new java.awt.Font("Inter Light", 0, 16)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel6.setText("Promoção*");

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
        jLabel7.setText("Marca*");

        totalAvaliacao.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N

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
        jLabel11.setText("Estoque*");

        estoque.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N

        jLabel12.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel12.setText("Peso (kg)");

        peso.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N

        jLabel13.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel13.setText("Dimensões");

        dimensoes.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N

        jLabel14.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel14.setText("Ativo*");

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
        jLabel9.setText("Parcelas*");

        parcelas.setFont(new java.awt.Font("Inter Light", 0, 18)); // NOI18N
        parcelas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", " " }));
        parcelas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parcelasActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel15.setText("Categoria*");

        categorias.setFont(new java.awt.Font("Inter Light", 0, 16)); // NOI18N
        categorias.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " " }));

        jLabel16.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel16.setText("Imagens (URL)*");

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

        btCadastrar.setBackground(new java.awt.Color(102, 255, 51));
        btCadastrar.setFont(new java.awt.Font("Inter SemiBold", 1, 18)); // NOI18N
        btCadastrar.setForeground(new java.awt.Color(255, 255, 255));
        btCadastrar.setText("Cadastrar");
        btCadastrar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btCadastrarMouseClicked(evt);
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(imagem1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(imagem2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 1234, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(excluirImagem2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(excluirImagem1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btCancelar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btCadastrar)))
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
                        .addComponent(categorias, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(marca, javax.swing.GroupLayout.PREFERRED_SIZE, 567, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(parcelas, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(12, 12, 12))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(estoque, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel13)
                                .addGap(14, 14, 14)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(dimensoes, javax.swing.GroupLayout.PREFERRED_SIZE, 496, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(avaliacao, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(totalAvaliacao, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(imagemProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 346, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(83, 83, 83)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3)
                            .addComponent(jScrollPane4)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nomeProduto))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ativoSim)
                        .addGap(18, 18, 18)
                        .addComponent(ativoNao)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(peso, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(658, 658, 658)))
                .addGap(18, 18, 18))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nomeProduto, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(imagemProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(promoNao, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(promoSim, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(preco, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(precoPromocional)
                    .addComponent(categorias, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(dimensoes, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(peso, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(ativoNao, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(ativoSim, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(12, 12, 12)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(estoque, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(1, 1, 1)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(totalAvaliacao, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(parcelas, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                                .addComponent(avaliacao))))
                    .addComponent(marca, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(excluirImagem1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(imagem1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(imagem2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(excluirImagem2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btCadastrar, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(58, 58, 58))
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

    private void btCadastrarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btCadastrarMouseClicked
        cadastrarProduto();
    }//GEN-LAST:event_btCadastrarMouseClicked

    private void btCancelarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btCancelarMouseClicked
        limpar();
    }//GEN-LAST:event_btCancelarMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton ativoNao;
    private javax.swing.JRadioButton ativoSim;
    private javax.swing.JComboBox<String> avaliacao;
    private javax.swing.JButton btCadastrar;
    private javax.swing.JButton btCancelar;
    private javax.swing.JComboBox<String> categorias;
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
