
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import java.util.logging.Logger;

/**
 * Classe utilitária para gerenciar a conexão com o banco de dados MySQL.
 *
 * @author Gabriel54274586
 */
public class Conexao {

    // Logger para rastreamento de eventos e erros
    private static final Logger LOGGER = Logger.getLogger(Conexao.class.getName());

    // Constantes para configuração da conexão com o banco de dados
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://sql10.freesqldatabase.com:3306/sql10779613";
    private static final String USER = "sql10779613";
    private static final String PASS = "URLWuELfM1";
    private static final String ERROR_DB_CONNECTION = "Erro ao conectar ao banco de dados: ";
    private static final String ERROR_DRIVER_NOT_FOUND = "Driver do banco de dados não encontrado: ";
    private static final String ERROR_GENERIC = "Erro inesperado: ";
    private static Connection connection = null;

    /**
     * Estabelece uma conexão com o banco de dados MySQL.
     *
     * @return Objeto Connection para interação com o banco de dados.
     * @throws RuntimeException Se houver falha na conexão ou carregamento do
     * driver.
     */
    public static Connection conexaoBanco() {
        LOGGER.info("Tentando estabelecer conexão com o banco de dados.");
        try {
            // Carrega o driver JDBC
            Class.forName(DRIVER);
            LOGGER.info("Driver JDBC carregado com sucesso: " + DRIVER);

            // Estabelece a conexão com o banco de dados
            Connection connection = DriverManager.getConnection(URL, USER, PASS);
            LOGGER.info("Conexão com o banco de dados estabelecida com sucesso.");
            return connection;

        } catch (ClassNotFoundException ex) {
            LOGGER.severe(ERROR_DRIVER_NOT_FOUND + ex.getMessage());
            throw new RuntimeException(ERROR_DRIVER_NOT_FOUND, ex);

        } catch (SQLException e) {
            LOGGER.severe(ERROR_DB_CONNECTION + e.getMessage());
            JOptionPane.showMessageDialog(null, ERROR_DB_CONNECTION + e.getMessage());
            throw new RuntimeException(ERROR_DB_CONNECTION, e);

        } catch (Exception e) {
            LOGGER.severe(ERROR_GENERIC + e.getMessage());
            JOptionPane.showMessageDialog(null, ERROR_GENERIC + e.getMessage());
            throw new RuntimeException(ERROR_GENERIC, e);
        }
    }
    // Método para fechar a conexão

    public static void fecharConexao() {
        if (connection != null) {
            try {
                connection.close();
                connection = null; // Para garantir que não seja reutilizada
                LOGGER.info("Conexão fechada com sucesso");
            } catch (SQLException e) {
                LOGGER.severe("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }

    /**
     * Método principal para testar a conexão com o banco de dados.
     *
     * @param args Argumentos da linha de comando (não utilizados).
     */
    public static void main(String args[]) {
        LOGGER.info("Testando conexão com o banco de dados.");
        try {
            new Conexao();
            conexaoBanco();
            LOGGER.info("Conexão realizada com sucesso.");
        } catch (Exception e) {
            LOGGER.severe("Falha ao testar conexão: " + e.getMessage());
        }
    }
}
