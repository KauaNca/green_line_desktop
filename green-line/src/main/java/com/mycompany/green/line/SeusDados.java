package com.mycompany.green.line;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * JInternalFrame para gerenciar dados pessoais do usuário, incluindo
 * informações de perfil e imagem. Exibe e atualiza os dados do usuário
 * armazenados no banco de dados.
 *
 * @author kauan
 */
public class SeusDados extends javax.swing.JInternalFrame {

    private static final Logger LOGGER = Logger.getLogger(SeusDados.class.getName());

    // Constantes para queries SQL
    private static final String SELECT_USER_DATA = "SELECT * FROM view_pessoa_endereco WHERE id_pessoa = ?";
    private static final String SELECT_PERSON_ID = "SELECT id_pessoa FROM pessoa WHERE cpf = ?";
    private static final String UPDATE_PERSON = "UPDATE pessoa SET nome = ?, email = ?, telefone = ?, cpf = ?, imagem_perfil = ? WHERE id_pessoa = ?";
    private static final String UPDATE_ADDRESS = "UPDATE enderecos SET uf = ?, cep = ?, cidade = ?, bairro = ?, endereco = ?, complemento = ? WHERE id_pessoa = ?";
    private static final String UPDATE_IMAGE = "UPDATE pessoa SET imagem_perfil = ? WHERE id_pessoa = ?";

    private final TelaInicial tela;
    private final String codigoUsuario;
    private int imageSelectionCount = 0;
    private String caminhoImagem;
    private String[] dadosBanco;
    private String[] novosDados;
    private String arquivoEscolhido;
    Funcoes funcoes = new Funcoes();
    int contadorMensagem = 0;

    /**
     * Construtor da classe SeusDados.
     *
     * @param codigo ID do usuário para buscar e exibir os dados.
     */
    public SeusDados(String codigo) {
        this.tela = null; // Considere inicializar adequadamente se for usado
        this.codigoUsuario = codigo;
        initComponents();
        Funcoes.aplicarMascaraNome(usuario);
        Funcoes.aplicarMascaraTelefone(telefone);
        Funcoes.aplicarMascaraCPF(cpf);
        Funcoes.aplicarMascaraCEP(cep);
        inicializarInterface();
        carregarDadosUsuario(codigoUsuario);

        ImageIcon originalIcon = new ImageIcon(TelaComImagem.class.getResource("/imagens/logo.png"));
        Image img = originalIcon.getImage();
        Image resizedImg = img.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(resizedImg);
        setFrameIcon(resizedIcon);

        setVisible(true);

    }

    /**
     * Inicializa a interface desabilitando campos de texto e ocultando botões
     * de ação.
     */
    private void inicializarInterface() {
        for (Component component : getContentPane().getComponents()) {
            if (component instanceof JTextField) {
                ((JTextField) component).setEnabled(false);
            }
        }
        btSalvar.setVisible(false);
        btCancelar.setVisible(false);
    }

    /**
     * Carrega os dados do usuário do banco de dados com base no código
     * fornecido.
     *
     * @param codigo ID do usuário.
     */
    private void carregarDadosUsuario(String codigo) {
        LOGGER.info("Iniciando recuperação de dados do usuário.");
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_USER_DATA)) {
            stmt.setString(1, codigo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    LOGGER.info("Dados do usuário encontrados.");
                    dadosBanco = new String[]{
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("telefone"),
                        rs.getString("cpf"),
                        rs.getString("cep"),
                        rs.getString("uf"),
                        rs.getString("cidade"),
                        rs.getString("bairro"),
                        rs.getString("endereco"),
                        rs.getString("complemento"),
                        rs.getString("imagem_perfil")
                    };

                    imagemUsuario.setText(rs.getString("imagem_perfil"));

                    // Preenche os campos da interface
                    usuario.setText(Objects.toString(rs.getString("nome"), ""));
                    codigoPessoa.setText(Objects.toString(rs.getString("id_pessoa"), ""));
                    email.setText(Objects.toString(rs.getString("email"), ""));
                    telefone.setText(Objects.toString(rs.getString("telefone"), ""));
                    cpf.setText(Objects.toString(rs.getString("cpf"), ""));
                    cep.setText(Objects.toString(rs.getString("cep"), ""));

                    // Seleciona a UF correspondente
                    for (int i = 0; i < uf.getItemCount(); i++) {
                        if (uf.getItemAt(i).equals(rs.getString("uf"))) {
                            uf.setSelectedIndex(i);
                            break;
                        }
                    }

                    cidade.setText(Objects.toString(rs.getString("cidade"), ""));
                    bairro.setText(Objects.toString(rs.getString("bairro"), ""));
                    endereco.setText(Objects.toString(rs.getString("endereco"), ""));
                    complemento.setText(Objects.toString(rs.getString("complemento"), ""));
                    carregarImagemURL(imagemUsuario);

                } else {
                    LOGGER.warning("Nenhum dado encontrado para o código: " + codigo);
                }
            }
        } catch (SQLException ex) {
            LOGGER.severe("Erro ao carregar dados do usuário: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados do usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
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
    private ImageIcon redimensionarImagem(ImageIcon imagem, int largura, int altura) {
        Image imagemOriginal = imagem.getImage();
        Image imagemRedimensionada = imagemOriginal.getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
        return new ImageIcon(imagemRedimensionada);
    }

    public void carregarImagemURL(JTextField campo) {

        String imageUrl = campo.getText().trim();
        if (imageUrl.isEmpty() || !imageUrl.contains("http")) {
            return;
        }
        try {
            URL url = new URL(imageUrl);
            BufferedImage image = ImageIO.read(url);
            if (image != null) {
                imagem.setIcon(redimensionarImagem(new ImageIcon(image), 221, 271));
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
     * Verifica se houve alterações nos dados do usuário.
     *
     * @return true se os dados foram alterados, false caso contrário.
     */
    private boolean verificarAlteracoesDados() {
        novosDados = new String[]{
            usuario.getText(),
            email.getText(),
            telefone.getText(),
            cpf.getText(),
            cep.getText(),
            uf.getSelectedItem().toString(),
            cidade.getText(),
            bairro.getText(),
            endereco.getText(),
            complemento.getText(),
            imagemUsuario.getText()
        };

        for (int i = 0; i < novosDados.length; i++) {
            if (!Objects.equals(dadosBanco[i], novosDados[i])) {
                LOGGER.info("Alterações detectadas nos dados do usuário.");
                return true;
            }
        }
        LOGGER.info("Nenhuma alteração detectada nos dados do usuário.");
        return false;
    }

    /**
     * Atualiza os dados do usuário no banco de dados.
     *
     * @throws SQLException Se ocorrer um erro durante a atualização.
     */
    private void atualizarDados() throws SQLException {
        try (Connection con = Conexao.conexaoBanco()) {
            con.setAutoCommit(false);
            LOGGER.info("Iniciando transação para atualização de dados.");

            // Busca o ID da pessoa
            String idPessoa;
            try (PreparedStatement stmt = con.prepareStatement(SELECT_PERSON_ID)) {
                stmt.setString(1, cpf.getText());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        idPessoa = rs.getString("id_pessoa");
                        LOGGER.info("ID da pessoa encontrado: " + idPessoa);
                    } else {
                        LOGGER.warning("Pessoa não encontrada para o CPF: " + cpf.getText());
                        con.rollback();
                        JOptionPane.showMessageDialog(this, "Pessoa não encontrada.", "Erro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
            String cpfFormatado = funcoes.removePontuacaoEEspacos(cpf.getText().trim());
            String telefoneFormatado = funcoes.removePontuacaoEEspacos(telefone.getText().trim());
            String cepFormatado = funcoes.removePontuacaoEEspacos(cep.getText().trim());

            // Atualiza tabela "pessoa"
            try (PreparedStatement stmt = con.prepareStatement(UPDATE_PERSON)) {
                stmt.setString(1, usuario.getText());
                stmt.setString(2, email.getText());
                stmt.setString(3, telefoneFormatado);
                stmt.setString(4, cpfFormatado);
                stmt.setString(5, imagemUsuario.getText());
                stmt.setString(6, idPessoa);
                stmt.executeUpdate();
                LOGGER.info("Tabela 'pessoa' atualizada com sucesso.");
            }

            // Atualiza tabela "enderecos"
            try (PreparedStatement stmt = con.prepareStatement(UPDATE_ADDRESS)) {
                stmt.setString(1, uf.getSelectedItem().toString());
                stmt.setString(2, cepFormatado);
                stmt.setString(3, cidade.getText());
                stmt.setString(4, bairro.getText());
                stmt.setString(5, endereco.getText());
                stmt.setString(6, complemento.getText());
                stmt.setString(7, idPessoa);
                stmt.executeUpdate();
                LOGGER.info("Tabela 'enderecos' atualizada com sucesso.");
            }

            con.commit();
            LOGGER.info("Transação concluída com sucesso.");
            JOptionPane.showMessageDialog(this, "Dados atualizados com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            LOGGER.severe("Erro ao atualizar dados: " + ex.getMessage());
            throw ex; // Propaga a exceção para tratamento externo, se necessário
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        imagem = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        usuario = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        codigoPessoa = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        email = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        telefone = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        cpf = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        uf = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        cidade = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        bairro = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        endereco = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        complemento = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        senha = new javax.swing.JTextField();
        btModificar = new javax.swing.JButton();
        btSalvar = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        cep = new javax.swing.JTextField();
        btCancelar = new javax.swing.JButton();
        imagemURL = new javax.swing.JLabel();
        imagemUsuario = new javax.swing.JTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        setClosable(true);
        setIconifiable(true);
        setTitle("Seus dados");

        jLabel1.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel1.setText("Nome");

        usuario.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        usuario.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        usuario.setEnabled(false);

        jLabel2.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel2.setText("Usuário");

        codigoPessoa.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        codigoPessoa.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        codigoPessoa.setEnabled(false);

        jLabel4.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel4.setText("Email");

        email.setEditable(false);
        email.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        email.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        email.setEnabled(false);

        jLabel5.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel5.setText("Telefone");

        telefone.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        telefone.setDisabledTextColor(new java.awt.Color(0, 0, 0));

        jLabel6.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel6.setText("CPF");

        cpf.setEditable(false);
        cpf.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        cpf.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        cpf.setEnabled(false);

        jLabel8.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel8.setText("UF");

        uf.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        uf.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO", "MA", "MG", "MS", "MT", "PA", "PB", "PE", "PI", "PR", "RJ", "RN", "RO", "RR", "RS", "SC", "SE", "SP", "TO" }));
        uf.setEnabled(false);

        jLabel9.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel9.setText("Cidade");

        cidade.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        cidade.setDisabledTextColor(new java.awt.Color(0, 0, 0));

        jLabel10.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel10.setText("Bairro");

        bairro.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        bairro.setDisabledTextColor(new java.awt.Color(0, 0, 0));

        jLabel11.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel11.setText("Endereço");

        endereco.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        endereco.setDisabledTextColor(new java.awt.Color(0, 0, 0));

        jLabel12.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel12.setText("Complemento");

        complemento.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        complemento.setDisabledTextColor(new java.awt.Color(0, 0, 0));

        jLabel13.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel13.setText("Senha");

        senha.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        senha.setDisabledTextColor(new java.awt.Color(0, 0, 0));

        btModificar.setBackground(new java.awt.Color(255, 102, 0));
        btModificar.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btModificar.setForeground(new java.awt.Color(255, 255, 255));
        btModificar.setText("Modificar");
        btModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btModificarActionPerformed(evt);
            }
        });

        btSalvar.setBackground(new java.awt.Color(0, 204, 51));
        btSalvar.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btSalvar.setForeground(new java.awt.Color(255, 255, 255));
        btSalvar.setText("Salvar");
        btSalvar.setMaximumSize(new java.awt.Dimension(104, 34));
        btSalvar.setMinimumSize(new java.awt.Dimension(104, 34));
        btSalvar.setPreferredSize(new java.awt.Dimension(104, 34));
        btSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSalvarActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel14.setText("CEP");

        cep.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        cep.setDisabledTextColor(new java.awt.Color(0, 0, 0));

        btCancelar.setBackground(new java.awt.Color(255, 0, 51));
        btCancelar.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btCancelar.setForeground(new java.awt.Color(255, 255, 255));
        btCancelar.setText("Cancelar");
        btCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelarActionPerformed(evt);
            }
        });

        imagemURL.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        imagemURL.setText("Imagem (URL)");

        imagemUsuario.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        imagemUsuario.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        imagemUsuario.setEnabled(false);
        imagemUsuario.setMaximumSize(new java.awt.Dimension(64, 28));
        imagemUsuario.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                imagemUsuarioFocusGained(evt);
            }
        });
        imagemUsuario.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                imagemUsuarioKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(imagem, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(usuario, javax.swing.GroupLayout.PREFERRED_SIZE, 509, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(codigoPessoa, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2)))
                            .addComponent(email, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(endereco)
                            .addComponent(senha)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addComponent(uf, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel14)
                                    .addComponent(cep, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(29, 29, 29)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9)
                                    .addComponent(cidade)))
                            .addComponent(imagemUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel11)
                                    .addComponent(jLabel13)
                                    .addComponent(imagemURL))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(telefone, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10)
                            .addComponent(bairro, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(complemento, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btModificar)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btSalvar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btCancelar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(53, 53, 53))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cpf, javax.swing.GroupLayout.PREFERRED_SIZE, 932, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(51, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(codigoPessoa, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(usuario, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(telefone, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(44, 44, 44))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cpf, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(imagem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bairro, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cep, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(endereco, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(complemento, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(uf, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cidade, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(senha, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                    .addComponent(btCancelar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btModificar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(imagemURL, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(imagemUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(35, 35, 35))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /**
     * Ação executada ao clicar no botão "Modificar". Habilita os campos de
     * texto para edição e exibe os botões de ação (Selecionar, Salvar,
     * Cancelar).
     *
     * @param evt Evento de clique do botão.
     */

    private void btModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btModificarActionPerformed
        int resposta = JOptionPane.showConfirmDialog(null,
                "Deseja realmente alterar seus dados?",
                "Confirmação",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);

        if (resposta == JOptionPane.OK_OPTION) {
            LOGGER.info("Modo de edição ativado.");
            for (Component component : getContentPane().getComponents()) {
                if (component instanceof JTextField) {
                    ((JTextField) component).setEnabled(true);
                }
            }
            // Desabilita o campo de código, que não deve ser editado
            codigoPessoa.setEnabled(false);
            // Controla visibilidade dos botões
            btCancelar.setVisible(true);
            btSalvar.setVisible(true);
            btModificar.setVisible(false);
            imagemUsuario.setEnabled(true);
        } else {
            LOGGER.info("Alteração de dados cancelada pelo usuário.");
        }


    }//GEN-LAST:event_btModificarActionPerformed
    /**
     * Ação executada ao clicar no botão "Cancelar". Restaura a interface ao
     * estado inicial, desabilitando os campos de texto e recarregando a imagem
     * original.
     *
     * @param evt Evento de clique do botão.
     */

    private void btCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelarActionPerformed
        LOGGER.info("Cancelando alterações e restaurando interface.");
        inicializarInterface();
        btModificar.setVisible(true);
        // Restaura a imagem original, se disponível
        if (caminhoImagem != null && !caminhoImagem.isEmpty()) {
            imagem.setIcon(new ImageIcon("imagens/usuarios/" + caminhoImagem));
        } else {
            imagem.setIcon(null); // Remove a imagem se não houver uma original
        }

    }//GEN-LAST:event_btCancelarActionPerformed

    /**
     * Ação executada ao clicar no botão "Salvar". Verifica se houve alterações
     * nos dados, atualiza o banco de dados e restaura a interface ao estado
     * inicial.
     *
     * @param evt Evento de clique do botão.
     */

    private void btSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSalvarActionPerformed
        if (verificarAlteracoesDados()) {
            LOGGER.info("Alterações nos dados detectadas. Iniciando atualização.");
            try {
                atualizarDados();
                inicializarInterface();
                btModificar.setVisible(true);
                carregarDadosUsuario(codigoUsuario); // Recarrega os dados atualizados
            } catch (SQLException ex) {
                LOGGER.severe("Erro ao atualizar dados no banco: " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Erro ao atualizar os dados. Tente novamente.",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                dispose(); // Fecha a janela em caso de erro
            }
        } else {
            LOGGER.info("Nenhuma alteração detectada. Salvamento não necessário.");
            JOptionPane.showMessageDialog(this,
                    "Nenhuma alteração foi feita nos dados.",
                    "Informação",
                    JOptionPane.INFORMATION_MESSAGE);
            inicializarInterface();
            btModificar.setVisible(true);
        }

    }//GEN-LAST:event_btSalvarActionPerformed

    private void imagemUsuarioKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_imagemUsuarioKeyReleased
        carregarImagemURL(imagemUsuario);
    }//GEN-LAST:event_imagemUsuarioKeyReleased

    private void imagemUsuarioFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_imagemUsuarioFocusGained
        if (contadorMensagem == 0) {
            contadorMensagem += 1;
            funcoes.Avisos("aviso.png", "Atenção: Para imagens, você deve fornecer URLs válidos de imagens da internet.\nExemplo: https://www.exemplo.com/imagem.jpg");
        }
    }//GEN-LAST:event_imagemUsuarioFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField bairro;
    private javax.swing.JButton btCancelar;
    private javax.swing.JButton btModificar;
    private javax.swing.JButton btSalvar;
    private javax.swing.JTextField cep;
    private javax.swing.JTextField cidade;
    private javax.swing.JTextField codigoPessoa;
    private javax.swing.JTextField complemento;
    private javax.swing.JTextField cpf;
    private javax.swing.JTextField email;
    private javax.swing.JTextField endereco;
    private javax.swing.JLabel imagem;
    private javax.swing.JLabel imagemURL;
    private javax.swing.JTextField imagemUsuario;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField senha;
    private javax.swing.JTextField telefone;
    private javax.swing.JComboBox<String> uf;
    private javax.swing.JTextField usuario;
    // End of variables declaration//GEN-END:variables
}
