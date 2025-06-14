import com.jtattoo.plaf.mcwin.McWinLookAndFeel;
import java.awt.*;
import java.awt.event.*;
import java.util.Properties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class TelaInicial extends JFrame {

    // Componentes da interface
    private final JMenuBar menuSuperior = new JMenuBar();
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
    
    // Estilos
    private final Font fontePadrao = new Font("Arial", Font.PLAIN, 28);
    private final Font fonteItem = new Font("Arial", Font.PLAIN, 25);
    private final JPanel linhaDeBaixo = new JPanel();
    private final Color corVerde = new Color(29, 68, 53);
    private final Color corDeFundo = new Color(255, 242, 207);
    private final EmptyBorder bordaItemMenu = new EmptyBorder(0, 20, 0, 20);

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
    }

    public TelaInicial(String codigo, String tipo_usuario) {
        this.codigo = codigo;
        this.tipo_usuario = tipo_usuario;

        mensagemBoasVindas();
        configurarUIManager();
        inicializarComponentes();

        // Verifica se usuário não forneceu dados válidos
        if (codigo.isBlank() && tipo_usuario.isBlank()) {
            dispose();
        }

        // Restringe acesso ao menu de vendas para usuários do tipo "2"
        if ("2".equals(tipo_usuario)) {
            Vendas.setEnabled(false);
        }
    }

    // Configura aparência do UI utilizando JTattoo
    private void configurarUIManager() {
        try {
            Properties props = new Properties();
            props.put("logoString", ""); // Remove o texto padrão "JTattoo"
            McWinLookAndFeel.setCurrentTheme(props);
            UIManager.setLookAndFeel("com.jtattoo.plaf.mcwin.McWinLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        UIManager.put("MenuBar.background", corVerde);
        UIManager.put("MenuBar.foreground", Color.WHITE);
    }

    private void inicializarComponentes() {
        configurarMenu();
        configurarFrame();
        configurarEventos();
    }

    private void mensagemBoasVindas() {
        JOptionPane.showMessageDialog(
                null,
                "<html><h2>Bem-vindo!</h2></html>",
                "Mensagem",
                JOptionPane.OK_OPTION,
                new ImageIcon("imagens/notificacao.png")
        );
    }

    private void configurarMenu() {
        menuSuperior.setPreferredSize(new Dimension(1300, 90));
        menuSuperior.setBorderPainted(false);

        // Personalizando menus e itens
        personalizacaoJMenu(Cadastro);
        personalizacaoJMenu(Usuario);
        personalizacaoJMenu(Produtos);
        personalizacaoJMenu(Vendas);
        personalizacaoJMenu(Notificacoes);
        personalizacaoJMenu(Configuracoes);
        personalizacaoJMenu(SuaConta);

        personalizacaoJMenuItem(cadastroUsuario);
        personalizacaoJMenuItem(cadastroProdutos);
        personalizacaoJMenuItem(cadastroCategorias);
        personalizacaoJMenuItem(pesquisarUsuario);
        personalizacaoJMenuItem(editarUsuario);
        personalizacaoJMenuItem(pesquisarProduto);
        personalizacaoJMenuItem(editarProduto);
        personalizacaoJMenuItem(editarCategorias);
        personalizacaoJMenuItem(sair);
        personalizacaoJMenuItem(seusDados);
        personalizacaoJMenuItem(ItemVendas);

        // Adicionando itens aos menus
        Cadastro.add(cadastroUsuario);
        Cadastro.add(cadastroProdutos);
        Usuario.add(pesquisarUsuario);
        Usuario.add(editarUsuario);
        Produtos.add(pesquisarProduto);
        Produtos.add(editarProduto);
        Produtos.add(cadastroCategorias);
        SuaConta.add(seusDados);
        SuaConta.add(sair);
        Vendas.add(ItemVendas);

        // Adicionando menus à barra de menus
        menuSuperior.add(Cadastro);
        menuSuperior.add(Usuario);
        menuSuperior.add(Produtos);
        menuSuperior.add(Vendas);
        menuSuperior.add(Notificacoes);
        menuSuperior.add(Configuracoes);
        menuSuperior.add(SuaConta);

        // Adicionando o menu ao JFrame
        setJMenuBar(menuSuperior);
    }

    private void personalizacaoJMenu(JMenu item) {
        item.setFont(fontePadrao);
        item.setBorder(bordaItemMenu);
        item.setForeground(Color.WHITE);
    }

    private void personalizacaoJMenuItem(JMenuItem item) {
        item.setForeground(Color.BLACK);
        item.setFont(fonteItem);
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
        // Eventos para mudança de cor ao passar o mouse nos menus
        adicionarEventoMouse(Cadastro);
        adicionarEventoMouse(Usuario);
        adicionarEventoMouse(Produtos);
        adicionarEventoMouse(Vendas);
        adicionarEventoMouse(Notificacoes);
        adicionarEventoMouse(Configuracoes);
        adicionarEventoMouse(SuaConta);

        // Eventos de clique nos itens do menu
        sair.addActionListener(e -> {
            new Login();
            dispose();
        });

        cadastroProdutos.addActionListener(e -> centralizarTela(new CadastroProdutos()));
        cadastroCategorias.addActionListener(e -> centralizarTela(new CadastroCategoria()));
        ItemVendas.addActionListener(e -> centralizarTela(new TelaVendas()));
        editarProduto.addActionListener(e -> centralizarTela(new EditarProdutos()));
        pesquisarProduto.addActionListener(e -> centralizarTela(new PesquisarProdutos()));
        seusDados.addActionListener(e -> centralizarTela(new SeusDados(codigo)));
        pesquisarUsuario.addActionListener(e -> centralizarTela(new PesquisarUsuario()));
    }

    private void adicionarEventoMouse(JMenu menu) {
        menu.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { menu.setForeground(Color.BLACK); }
            public void mouseExited(MouseEvent e) { menu.setForeground(Color.WHITE); }
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
    public static void main(String[] args){
        new TelaInicial();
    }
}