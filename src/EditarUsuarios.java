import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class EditarUsuarios extends javax.swing.JInternalFrame {
    
    private static final Logger LOGGER = Logger.getLogger(PesquisarUsuario.class.getName());
    private static final String SELECT_PERSON_DATA = "SELECT * FROM view_pessoa_endereco WHERE nome = ?";
    private static final String SELECT_PESSOA_POR_ID = "SELECT * FROM view_pessoa_endereco WHERE id_pessoa = ?";
    
    private DefaultTableModel tableModel;
    private JTable table;
    private JScrollPane scrollPane;
    private ArrayList<String> usuarios;
    private JPopupMenu caixaDeNomes = new JPopupMenu();
    private ArrayList<String> filtro;
    private final Font fonteItem = new Font("Arial", Font.PLAIN, 19);

    /**
     * Construtor da classe PesquisarUsuario.
     */
    String situacaoValor;
    int tipoUsuarioId;
    String tipoUsuario;
    public EditarUsuarios() {
        initComponents();
        initTable();
    }
    
    private void initTable() {
       try (Connection con = Conexao.conexaoBanco()) {
            String sql = "SELECT * FROM pessoa ORDER BY id_pessoa DESC";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel modeloTabela = (DefaultTableModel) tabela.getModel();
            modeloTabela.setNumRows(0);
            while (rs.next()) {
               
                Object[] dados = {
                    rs.getInt("id_pessoa"),
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("telefone"),
                    rs.getString("cpf"),
                    rs.getInt("id_tipo_usuario"),
                    rs.getString("situacao")
                };
                modeloTabela.addRow(dados);
            }

            stmt.close();
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(CadastroPessoas.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void carregarUsuario(int idPessoa) {
        try (Connection con = Conexao.conexaoBanco()) {
            // Carrega dados da pessoa
            String sqlPessoa = "SELECT * FROM pessoa WHERE id_pessoa = ?";
            PreparedStatement psPessoa = con.prepareStatement(sqlPessoa);
            psPessoa.setInt(1, idPessoa);
            ResultSet rsPessoa = psPessoa.executeQuery();

            if (rsPessoa.next()) {
                nome.setText(rsPessoa.getString("nome"));
                email.setText(rsPessoa.getString("email"));
                telefone.setText(rsPessoa.getString("telefone"));
                cpf.setText(rsPessoa.getString("cpf"));
                
                // Tipo de usuário
                tipoUsuarioId = rsPessoa.getInt("id_tipo_usuario");
                tipo.setSelectedItem(tipoUsuarioId == 1 ? "ADM" : "Funcionario");
                
                // Situação
                situacaoValor = rsPessoa.getString("situacao");
                switch(situacaoValor) {
                    case "A": situacao.setSelectedItem("Ativo"); break;
                    case "P": situacao.setSelectedItem("Inativo"); break;
                    case "I": situacao.setSelectedItem("Bloqueado"); break;
                }
                
                // Imagem de perfil
                String imagem = rsPessoa.getString("imagem_perfil");
                if (imagem != null && !imagem.isEmpty()) {
                    ImageIcon foto = new ImageIcon("imagens/usuarios/" + imagem);
                    perfil.setIcon(redimensionamentoDeImagem(foto, 205, 227));
                }
            }
            
            // Carrega dados do endereço
            String sqlEndereco = "SELECT * FROM enderecos WHERE id_pessoa = ?";
            PreparedStatement psEndereco = con.prepareStatement(sqlEndereco);
            psEndereco.setInt(1, idPessoa);
            ResultSet rsEndereco = psEndereco.executeQuery();

            if (rsEndereco.next()) {
                estado.setText(rsEndereco.getString("uf"));
                cep.setText(rsEndereco.getString("cep"));
                cidade.setText(rsEndereco.getString("cidade"));
                bairro.setText(rsEndereco.getString("bairro"));
                endereco.setText(rsEndereco.getString("endereco"));
                complemento.setText(rsEndereco.getString("complemento"));
            } else {
                // Limpa campos se não houver endereço cadastrado
                estado.setText("");
                cep.setText("");
                cidade.setText("");
                bairro.setText("");
                endereco.setText("");
                complemento.setText("");
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar usuário: " + e.getMessage());
            e.printStackTrace();
        }
    }
     /**
     * Pesquisa usuários no banco de dados e exibe os resultados na tabela.
     */
    private void pesquisarUsuarios() {
        String texto = tfPesquisar.getText();
        LOGGER.log(Level.INFO, "Pesquisando usu\u00e1rios com texto: {0}", texto);
        
        // Limpa a tabela antes de nova pesquisa
        tableModel.setRowCount(0);
        
        try (Connection con = Conexao.conexaoBanco(); 
             PreparedStatement stmt = con.prepareStatement(SELECT_PERSON_DATA)) {
            
            stmt.setString(1, "%" + texto + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Adiciona cada registro como uma nova linha na tabela
                    tableModel.addRow(new Object[]{
                        rs.getString("id_pessoa"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("telefone"),
                        rs.getString("cpf")
                    });
                }
                LOGGER.info("Dados carregados na tabela com sucesso.");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao pesquisar usu\u00e1rios: {0}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Erro ao pesquisar usuários");
        }
    }
    
    // Método para redimensionar imagem
    public ImageIcon redimensionamentoDeImagem(ImageIcon imagem, int largura, int altura) {
        Image pegarImagem = imagem.getImage();
        Image redimensionando = pegarImagem.getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
        return new ImageIcon(redimensionando);
    }
    
     // Método para validar campos antes da atualização
    private boolean validarCampos() {
        if (nome.getText().isBlank() || email.getText().isBlank() ||
            telefone.getText().isBlank() || cpf.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos obrigatórios!");
            return false;
        }
        
        if (!email.getText().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            JOptionPane.showMessageDialog(this, "E-mail inválido!");
            return false;
        }
        
        if (!cpf.getText().matches("\\d{3}\\.?\\d{3}\\.?\\d{3}\\-?\\d{2}")) {
            JOptionPane.showMessageDialog(this, "CPF inválido!");
            return false;
        }
        
        if (estado.getText().isBlank() || cep.getText().isBlank() || 
            cidade.getText().isBlank() || endereco.getText().isBlank()) {
            int resposta = JOptionPane.showConfirmDialog(this, 
                "Alguns campos de endereço estão vazios. Deseja continuar?", 
                "Aviso", JOptionPane.YES_NO_OPTION);
            return resposta == JOptionPane.YES_OPTION;
        }
        
        return true;
    }

     // Método para limpar todos os campos
    private void limparCampos() {
        nome.setText("");
        email.setText("");
        telefone.setText("");
        cpf.setText("");
        estado.setText("");
        cep.setText("");
        cidade.setText("");
        bairro.setText("");
        endereco.setText("");
        complemento.setText("");
        campoId.setText("");
        tipo.setSelectedIndex(0);
        situacao.setSelectedIndex(0);
        perfil.setIcon(new ImageIcon("imagens/perfil.png"));
    }
    
     @SuppressWarnings("unchecked")
     

      
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
      
          private void pesquisarPessoa() {
        LOGGER.info("Pesquisando pessoa física com nome: " + tfPesquisar.getText());
        try (Connection con = Conexao.conexaoBanco(); PreparedStatement stmt = con.prepareStatement(SELECT_PERSON_DATA)) {
            stmt.setString(1, tfPesquisar.getText());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Preenche os campos com os dados da pessoa
                    campoId.setText(rs.getString("id_pessoa"));
                    nome.setText(rs.getString("nome"));
                    email.setText(rs.getString("email"));
                    telefone.setText(rs.getString("telefone"));
                    cpf.setText(rs.getString("cpf"));
                    estado.setText(rs.getString("uf"));
                    cep.setText(rs.getString("cep"));
                    cidade.setText(rs.getString("cidade"));
                    bairro.setText(rs.getString("bairro"));
                    endereco.setText(rs.getString("endereco"));
                    complemento.setText(rs.getString("complemento"));

                    // Carrega e redimensiona a imagem do perfil
                    ImageIcon foto = new ImageIcon("imagens/usuarios/" + rs.getString("imagem_perfil"));
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
        jLabel3 = new javax.swing.JLabel();
        telefone = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
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
        campoId = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        complemento = new javax.swing.JTextField();
        btAtualizar = new javax.swing.JButton();
        cpf = new javax.swing.JFormattedTextField();
        cep = new javax.swing.JFormattedTextField();
        btCancelar1 = new javax.swing.JButton();
        situacao = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        tipo = new javax.swing.JComboBox<>();
        email = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();

        setClosable(true);
        setTitle("Editar Usuários");

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

        jLabel3.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel3.setText("Cadastro de Pessoa Física (CPF)");

        telefone.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        telefone.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel5.setText("Telefone");

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

        campoId.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        campoId.setSelectedTextColor(new java.awt.Color(51, 51, 51));
        campoId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoIdActionPerformed(evt);
            }
        });
        campoId.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                campoIdKeyPressed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel14.setText("Complemento");

        complemento.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        complemento.setSelectedTextColor(new java.awt.Color(51, 51, 51));

        btAtualizar.setBackground(new java.awt.Color(255, 165, 0));
        btAtualizar.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        btAtualizar.setForeground(new java.awt.Color(255, 255, 255));
        btAtualizar.setText("Atualizar");
        btAtualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAtualizarActionPerformed(evt);
            }
        });

        cpf.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
        cpf.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        cpf.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                cpfKeyReleased(evt);
            }
        });

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
        btCancelar1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelar1ActionPerformed(evt);
            }
        });

        situacao.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        situacao.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        situacao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                situacaoActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel6.setText("Tipo");

        jLabel7.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        jLabel7.setText("Situação");

        tipo.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        tipo.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tipoActionPerformed(evt);
            }
        });

        tabela.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        tabela.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "id_pessoa", "nome", "Email", "telefone", "cpf", "tipo", "situacao"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tabela);

        javax.swing.GroupLayout painelPessoaLayout = new javax.swing.GroupLayout(painelPessoa);
        painelPessoa.setLayout(painelPessoaLayout);
        painelPessoaLayout.setHorizontalGroup(
            painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelPessoaLayout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(painelPessoaLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btAtualizar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btCancelar1)
                        .addGap(51, 51, 51))
                    .addGroup(painelPessoaLayout.createSequentialGroup()
                        .addComponent(perfil, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, 465, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(telefone, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel13)
                                    .addComponent(campoId, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(26, 26, 26)
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(nome)))
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
                                    .addComponent(jLabel12)
                                    .addComponent(jLabel3)
                                    .addComponent(cpf, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(bairro, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(complemento, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(painelPessoaLayout.createSequentialGroup()
                                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(situacao, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel14)
                                            .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(jLabel7)
                                                .addComponent(jLabel11)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                                .addComponent(jLabel6)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                            .addComponent(tipo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))))
                    .addComponent(jScrollPane1))
                .addGap(49, 49, 49))
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
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nome, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(campoId, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(telefone, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(painelPessoaLayout.createSequentialGroup()
                                .addComponent(email)
                                .addGap(1, 1, 1)))
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel7)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cpf)
                            .addComponent(situacao, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(tipo))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(painelPessoaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btAtualizar)
                    .addComponent(btCancelar1))
                .addGap(20, 20, 20)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE))
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
                .addComponent(JCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tfPesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfPesquisarKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            pesquisarUsuarios();
        }
    }//GEN-LAST:event_tfPesquisarKeyReleased
    private void btnProcurarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProcurarActionPerformed
        LOGGER.info("Iniciando pesquisa de usuário.");
        // Desativa campos que não devem ser editados
        campoId.setEnabled(false);
        email.setEnabled(false);
        cpf.setEnabled(false);
        pesquisarPessoa();
        tfPesquisar.setText(""); // Limpa o campo de pesquisa
    }//GEN-LAST:event_btnProcurarActionPerformed

    private void campoIdKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoIdKeyPressed
         if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
            try {
                Connection con = Conexao.conexaoBanco();
                PreparedStatement stmt = con.prepareStatement("SELECT * FROM pessoa WHERE id_pessoa = ?");
                stmt.setString(1, campoId.getText());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    campoId.setText(rs.getString("id_pessoa"));
                    nome.setText(rs.getString("nome"));
                    email.setText(rs.getString("email"));
                    telefone.setText(rs.getString("telefone"));
                    cpf.setText(rs.getString("cpf"));
                    
                    estado.setText(rs.getString("uf"));
                    cep.setText(rs.getString("cep"));
                    cidade.setText(rs.getString("cidade"));
                    bairro.setText(rs.getString("bairro"));
                    endereco.setText(rs.getString("endereco"));
                    complemento.setText(rs.getString("complemento"));

                    ImageIcon foto = new ImageIcon("imagens/usuarios/" + rs.getString("imagem_perfil"));
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
    }//GEN-LAST:event_campoIdKeyPressed
    }
    private void btCancelar1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btCancelar1MouseClicked
       perfil.setIcon(new ImageIcon("imagens/perfil.png"));
        usuarios = new ArrayList<>();
        for (Component component : painelPessoa.getComponents()) {
            if (component instanceof JTextField) {
                ((JTextField) component).setText("");
            }
        }
    }//GEN-LAST:event_btCancelar1MouseClicked


    private void cpfKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cpfKeyReleased
        if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
            try {
                Connection con = Conexao.conexaoBanco();
                PreparedStatement stmt = con.prepareStatement("SELECT * FROM pessoa WHERE cpf = ?");
                stmt.setString(1, cpf.getText());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    campoId.setText(rs.getString("id_pessoa"));
                    nome.setText(rs.getString("nome"));
                    email.setText(rs.getString("email"));
                    telefone.setText(rs.getString("telefone"));
                    cpf.setText(rs.getString("cpf"));
                    /*
                    estado.setText(rs.getString("uf"));
                    cep.setText(rs.getString("cep"));
                    cidade.setText(rs.getString("cidade"));
                    bairro.setText(rs.getString("bairro"));
                    endereco.setText(rs.getString("endereco"));
                    complemento.setText(rs.getString("complemento"));*/

                    ImageIcon foto = new ImageIcon("imagens/usuarios/" + rs.getString("imagem_perfil"));
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

    private void situacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_situacaoActionPerformed
         switch (situacao.getSelectedItem().toString()) {
            case "Ativo" -> situacaoValor = "A";
            case "Inativo" -> situacaoValor = "P";
            case "Bloqueado" -> situacaoValor = "I";
        }
    }//GEN-LAST:event_situacaoActionPerformed

    private void tipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tipoActionPerformed
        if (tipo.getSelectedItem().equals("ADM")) {
            tipoUsuario = "1";
        } else if (tipo.getSelectedItem().equals("Funcionario")) {
            tipoUsuario = "2";
        }
    }//GEN-LAST:event_tipoActionPerformed


    
    private void tabelaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabelaMouseClicked
        if (tabela.getSelectedRow() == -1) return;
        
        int idPessoa = Integer.parseInt(tabela.getValueAt(tabela.getSelectedRow(), 0).toString());
        campoId.setText(String.valueOf(idPessoa));
        
        // Carrega todos os dados do usuário e endereço
        carregarUsuario(idPessoa);
    }//GEN-LAST:event_tabelaMouseClicked

    private void btAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAtualizarActionPerformed
        if (!validarCampos()) {
            return;
        }
        
        Connection con = null;
        try {
            con = Conexao.conexaoBanco();
            con.setAutoCommit(false); // Inicia transação
            
            int idPessoa = Integer.parseInt(campoId.getText());
            
            // 1. Atualizar dados da pessoa
            String sqlPessoa = "UPDATE pessoa SET nome=?, email=?, telefone=?, cpf=?, "
                    + "id_tipo_usuario=?, situacao=? WHERE id_pessoa=?";
            
            int Tipo = tipo.getSelectedItem().equals("ADM") ? 1 : 2;
            String Situacao = situacao.getSelectedItem().equals("Ativo") ? "A" : 
                             situacao.getSelectedItem().equals("Inativo") ? "P" : "I";

            PreparedStatement psPessoa = con.prepareStatement(sqlPessoa);
            psPessoa.setString(1, nome.getText());
            psPessoa.setString(2, email.getText());
            psPessoa.setString(3, telefone.getText());
            psPessoa.setString(4, cpf.getText());
            psPessoa.setInt(5, Tipo);
            psPessoa.setString(6, Situacao);
            psPessoa.setInt(7, idPessoa);

            int rowsPessoa = psPessoa.executeUpdate();
            
            // 2. Verificar se existe endereço cadastrado
            String checkEndereco = "SELECT COUNT(*) FROM enderecos WHERE id_pessoa=?";
            PreparedStatement psCheck = con.prepareStatement(checkEndereco);
            psCheck.setInt(1, idPessoa);
            ResultSet rsCheck = psCheck.executeQuery();
            rsCheck.next();
            int count = rsCheck.getInt(1);
            
            String sqlEndereco;
            if (count > 0) {
                // Atualizar endereço existente
                sqlEndereco = "UPDATE enderecos SET uf=?, cep=?, cidade=?, bairro=?, "
                        + "endereco=?, complemento=? WHERE id_pessoa=?";
            } else {
                // Inserir novo endereço
                sqlEndereco = "INSERT INTO enderecos (uf, cep, cidade, bairro, endereco, "
                        + "complemento, id_pessoa) VALUES (?, ?, ?, ?, ?, ?, ?)";
            }
            
            PreparedStatement psEndereco = con.prepareStatement(sqlEndereco);
            psEndereco.setString(1, estado.getText());
            psEndereco.setString(2, cep.getText());
            psEndereco.setString(3, cidade.getText());
            psEndereco.setString(4, bairro.getText());
            psEndereco.setString(5, endereco.getText());
            psEndereco.setString(6, complemento.getText());
            psEndereco.setInt(7, idPessoa);

            int rowsEndereco = psEndereco.executeUpdate();
            
            // Se ambos atualizaram com sucesso, confirma a transação
            if (rowsPessoa > 0 && rowsEndereco > 0) {
                con.commit();
                JOptionPane.showMessageDialog(this, "Usuário e endereço atualizados com sucesso!");
            } else {
                con.rollback();
                JOptionPane.showMessageDialog(this, "Erro ao atualizar - nenhuma alteração foi salva.");
            }
            
            initTable(); // Recarrega a tabela com os dados atualizados
            
        } catch (SQLException e) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Erro ao atualizar: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID inválido!");
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_btAtualizarActionPerformed

    private void btCancelar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelar1ActionPerformed
        limparCampos();
    }//GEN-LAST:event_btCancelar1ActionPerformed

    private void campoIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoIdActionPerformed
        
    }//GEN-LAST:event_campoIdActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel JCard;
    private javax.swing.JTextField bairro;
    private javax.swing.JButton btAtualizar;
    private javax.swing.JButton btCancelar1;
    private javax.swing.JButton btnProcurar;
    private javax.swing.JTextField campoId;
    private javax.swing.JFormattedTextField cep;
    private javax.swing.JTextField cidade;
    private javax.swing.JTextField complemento;
    private javax.swing.JFormattedTextField cpf;
    private javax.swing.JTextField email;
    private javax.swing.JTextField endereco;
    private javax.swing.JTextField estado;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField nome;
    private javax.swing.JPanel painelPessoa;
    private javax.swing.JLabel perfil;
    private javax.swing.JComboBox<String> situacao;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField telefone;
    private javax.swing.JTextField tfPesquisar;
    private javax.swing.JComboBox<String> tipo;
    private javax.swing.ButtonGroup tipoPessoa;
    // End of variables declaration//GEN-END:variables

}