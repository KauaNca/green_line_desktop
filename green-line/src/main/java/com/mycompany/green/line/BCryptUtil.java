package com.mycompany.green.line;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptUtil {

    // Verifica se a senha fornecida corresponde ao hash armazenado
    public static boolean checkPassword(String plaintext, String storedHash) {
        try {
            // Normaliza o prefixo $2b$ para $2a$ por compatibilidade
            String normalizedHash = storedHash.replaceFirst("^\\$2b\\$", "\\$2a\\$");
            return BCrypt.checkpw(plaintext, normalizedHash);
        } catch (Exception e) {
            System.err.println("Erro na verificação: " + e.getMessage());
            return false;
        }
    }

    // Gera um hash seguro da senha
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
