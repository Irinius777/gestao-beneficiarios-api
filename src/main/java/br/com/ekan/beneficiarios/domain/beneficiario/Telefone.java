package br.com.ekan.beneficiarios.domain.beneficiario;

import java.util.regex.Pattern;

/**
 * Value object que representa um telefone valido.
 * O valor armazenado contem apenas digitos, com 10 ou 11 posicoes.
 */
public record Telefone(String value) {

    private static final Pattern APENAS_CARACTERES_VALIDOS = Pattern.compile("[0-9()\\-\\s+]+");

    public Telefone {
        String normalizado = normalizar(value);
        if (normalizado.length() != 10 && normalizado.length() != 11) {
            throw new IllegalArgumentException("telefone deve conter 10 ou 11 digitos");
        }
        value = normalizado;
    }

    private static String normalizar(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            throw new IllegalArgumentException("telefone nao pode ser vazio");
        }
        if (!APENAS_CARACTERES_VALIDOS.matcher(telefone).matches()) {
            throw new IllegalArgumentException("telefone contem caracteres invalidos");
        }
        return telefone.replaceAll("\\D", "");
    }

    @Override
    public String toString() {
        return value;
    }
}
