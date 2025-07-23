package com.mycompany.green.line;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.formdev.flatlaf.FlatLightLaf;

public class TelaInicial extends JFrame {

    // Componentes da interface
    private final JMenuBar menuSuperior = new JMenuBar() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(new GradientPaint(0, 0, corVerde, getWidth(), getHeight(), corVerde.brighter()));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    };
    private final JLabel logoLabel = new JLabel("Green Line");
    private final JMenu Cadastro = new JMenu("Cadastro");
    private final JMenu Usuario = new JMenu("Usuário");
    private final JMenu Produtos = new JMenu("Produtos");
    private final JMenu Vendas = new JMenu("Vendas");
    private final JMenu Notificacoes = new JMenu("Notificações");
    private final JMenu Configuracoes = new JMenu("Configurações");
    private final JMenu SuaConta = new JMenu("Sua conta");
    private final JMenuItem ItemVendas = new JMenuItem("Nova venda");
    private final JMenuItem cadastroUsuario = new JMenuItem("Usuário");
    private final JMenuItem cadastroProdutos = new JMenuItem("Produtos");
    private final JMenuItem pesquisarUsuario = new JMenuItem("Pesquisar");
    private final JMenuItem editarUsuario = new JMenuItem("Editar");
    private final JMenuItem cadastroCategorias = new JMenuItem("Categorias");
    private final JMenuItem pesquisarProduto = new JMenuItem("Pesquisar");
    private final JMenuItem editarProduto = new JMenuItem("Editar");
    private final JMenuItem editarCategorias = new JMenuItem("Categorias");
    private final JMenuItem sair = new JMenuItem("Sair");
    private final JMenuItem seusDados = new JMenuItem("Seus dados");
    private final JDesktopPane painelPrincipal = new JDesktopPane();
    private final JPanel linhaDeBaixo = new JPanel();

    // Estilos
    private final Font fontePadrao = new Font("Segoe UI", Font.PLAIN, 24);
    private final Font fonteItem = new Font("Segoe UI", Font.PLAIN, 20);
    private final Font fonteLogo = new Font("Segoe UI", Font.BOLD, 24);
    private final Color corVerde = new Color(29, 68, 53);
    private final Color corDeFundo = new Color(255, 242, 207);
    private final EmptyBorder bordaItemMenu = new EmptyBorder(5, 15, 5, 15);

    private String codigo;
    private String tipo_usuario;

    public String getCodigo() {
        return codigo;
    }

    public String getTipo_usuario() {
        return tipo_usuario;
    }

    public TelaInicial() {
        configurarFrame();
         ImageIcon originalIcon = new ImageIcon(TelaComImagem.class.getResource("/imagens/logo.png"));
    Image img = originalIcon.getImage();
    Image resizedImg = img.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
    setIconImage(resizedImg);

    }

    public TelaInicial(String codigo, String tipo_usuario) {
    this(); // chama o construtor padrão, que configura frame e ícone
    this.codigo = codigo;
    this.tipo_usuario = tipo_usuario;

    mensagemBoasVindas();
    inicializarComponentes();
    configurarUIManager();

    if (codigo.trim().isEmpty() && tipo_usuario.trim().isEmpty()) {
        dispose();
    }

    if ("2".equals(tipo_usuario)) {
        Produtos.setEnabled(false);
        Usuario.setEnabled(false);
        Configuracoes.setEnabled(false);
    }
}

    private void configurarUIManager() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("MenuBar.background", corVerde);
            UIManager.put("MenuBar.foreground", Color.WHITE);
            UIManager.put("Menu.background", corVerde);
            UIManager.put("Menu.foreground", Color.WHITE);
            UIManager.put("MenuItem.background", Color.WHITE);
            UIManager.put("MenuItem.foreground", Color.BLACK);
            UIManager.put("Menu.selectionBackground", corVerde.darker());
            UIManager.put("Menu.selectionForeground", Color.BLACK);

            SwingUtilities.invokeLater(() -> {
                SwingUtilities.updateComponentTreeUI(this);
                menuSuperior.repaint();
            });

        } catch (Exception e) {
            System.err.println("Erro ao configurar FlatLightLaf: ");
            e.printStackTrace();
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                SwingUtilities.updateComponentTreeUI(this);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void inicializarComponentes() {
        configurarMenu();
        configurarFrame();
        configurarEventos();
    }

    private void mensagemBoasVindas() {
        JOptionPane.showMessageDialog(
                null,
                "<html><h2>Bem-vindo ao Green Line!</h2></html>",
                "Mensagem",
                JOptionPane.OK_OPTION,
                new ImageIcon("imagens/notificacao.png")
        );
    }

    private void configurarMenu() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        menuSuperior.setPreferredSize(new Dimension(screenSize.width, 50));
        menuSuperior.setBorderPainted(false);
        menuSuperior.setLayout(new BoxLayout(menuSuperior, BoxLayout.X_AXIS));

        logoLabel.setFont(fonteLogo);
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setBorder(new EmptyBorder(0, 10, 0, 20));
        menuSuperior.add(logoLabel);
        menuSuperior.add(Box.createHorizontalGlue());

        // Personalizar menus
        personalizacaoJMenu(Cadastro);
        personalizacaoJMenu(Usuario);
        personalizacaoJMenu(Produtos);
        personalizacaoJMenu(Vendas);
        personalizacaoJMenu(Notificacoes);
        personalizacaoJMenu(Configuracoes);
        personalizacaoJMenu(SuaConta);

        // Personalizar itens de menu
        personalizacaoJMenuItem(ItemVendas);
        personalizacaoJMenuItem(cadastroUsuario);
        personalizacaoJMenuItem(cadastroProdutos);
        personalizacaoJMenuItem(pesquisarUsuario);
        personalizacaoJMenuItem(editarUsuario);
        personalizacaoJMenuItem(cadastroCategorias);
        personalizacaoJMenuItem(pesquisarProduto);
        personalizacaoJMenuItem(editarProduto);
        personalizacaoJMenuItem(editarCategorias);
        personalizacaoJMenuItem(sair);
        personalizacaoJMenuItem(seusDados);

        // Adicionar itens aos menus
        Vendas.add(ItemVendas);

        Cadastro.add(cadastroUsuario);
        Cadastro.add(cadastroProdutos);
        Cadastro.add(cadastroCategorias);

        Usuario.add(pesquisarUsuario);
        Usuario.add(editarUsuario);

        Produtos.add(pesquisarProduto);
        Produtos.add(editarProduto);
        //Produtos.add(editarCategorias);

        SuaConta.add(seusDados);
        SuaConta.add(sair);

        // Adicionar menus à barra de menu
        menuSuperior.add(Cadastro);
        menuSuperior.add(Usuario);
        menuSuperior.add(Produtos);
        /*menuSuperior.add(Vendas);
        menuSuperior.add(Notificacoes);
        menuSuperior.add(Configuracoes);*/
        menuSuperior.add(SuaConta);

        setJMenuBar(menuSuperior);
    }

    private void personalizacaoJMenu(JMenu item) {
        item.setFont(fontePadrao);
        item.setBorder(bordaItemMenu);
        item.setForeground(Color.WHITE);
        item.setBackground(corVerde);
        item.setOpaque(true);
    }

    private void personalizacaoJMenuItem(JMenuItem item) {
        item.setForeground(Color.BLACK);
        item.setFont(fonteItem);
        item.setBackground(Color.WHITE);
        item.setOpaque(true);
        item.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    private void configurarFrame() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        linhaDeBaixo.setPreferredSize(new Dimension(1400, 20));
        linhaDeBaixo.setBackground(corVerde);
        painelPrincipal.setBackground(corDeFundo);

        add(painelPrincipal, BorderLayout.CENTER);
        add(linhaDeBaixo, BorderLayout.SOUTH);

        setVisible(true);
        setResizable(false);
    }

    private void configurarEventos() {
        adicionarEventoMouse(Cadastro);
        adicionarEventoMouse(Usuario);
        adicionarEventoMouse(Produtos);
        adicionarEventoMouse(Vendas);
        adicionarEventoMouse(Notificacoes);
        adicionarEventoMouse(Configuracoes);
        adicionarEventoMouse(SuaConta);

        sair.addActionListener(e -> {
            dispose();
            // Adicionar código para voltar à tela de login se necessário
        });

        cadastroProdutos.addActionListener(e -> centralizarTela(new CadastroProdutos()));
        cadastroCategorias.addActionListener(e -> centralizarTela(new CadastroCategoria()));
        ItemVendas.addActionListener(e -> centralizarTela(new TelaVendas()));
        editarProduto.addActionListener(e -> centralizarTela(new EditarProdutos()));
        pesquisarProduto.addActionListener(e -> centralizarTela(new PesquisarProdutos()));
        seusDados.addActionListener(e -> centralizarTela(new SeusDados(codigo)));
        pesquisarUsuario.addActionListener(e -> centralizarTela(new PesquisarUsuario()));
        cadastroUsuario.addActionListener(e -> centralizarTela(new CadastroPessoas()));
        editarUsuario.addActionListener(e -> centralizarTela(new EditarUsuarios()));
    }

    private void adicionarEventoMouse(JMenu menu) {
        menu.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                menu.setBackground(corVerde.brighter());
            }

            public void mouseExited(MouseEvent e) {
                menu.setBackground(corVerde);
            }
        });
    }

    private void centralizarTela(JInternalFrame tela) {
        painelPrincipal.add(tela);
        tela.setLocation(
                (painelPrincipal.getWidth() - tela.getWidth()) / 2,
                (painelPrincipal.getHeight() - tela.getHeight()) / 2
        );
        tela.setVisible(true);
        tela.toFront();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TelaInicial().setVisible(true);
        });
    }
}
