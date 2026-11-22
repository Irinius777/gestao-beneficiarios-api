package br.com.ekan.beneficiarios.domain.beneficiario;

import java.util.regex.Pattern;

/**
 * Value object que representa um CPF valido.
 * O valor armazenado contem apenas digitos, sem mascara.
 */
public record Cpf(String value) {

    private static final int TAMANHO = 11;
    private static final Pattern APENAS_CARACTERES_VALIDOS = Pattern.compile("[0-9.\\-\\s]+");

    public Cpf {
        value = validar(value);
    }

    private static String validar(String cpf) {
        String normalizado = normalizar(cpf);

        if (normalizado.length() != TAMANHO) {
            throw new IllegalArgumentException("cpf deve conter 11 digitos");
        }
        if (normalizado.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("cpf invalido");
        }
        if (!digitosVerificadoresValidos(normalizado)) {
            throw new IllegalArgumentException("cpf invalido");
        }

        return normalizado;
    }

    private static String normalizar(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("cpf nao pode ser vazio");
        }
        if (!APENAS_CARACTERES_VALIDOS.matcher(cpf).matches()) {
            throw new IllegalArgumentException("cpf contem caracteres invalidos");
        }
        return cpf.replaceAll("\\D", "");
    }

    private static boolean digitosVerificadoresValidos(String cpf) {
        int primeiroDigito = calcularDigito(cpf, 9, 10);
        int segundoDigito = calcularDigito(cpf, 10, 11);

        return primeiroDigito == Character.getNumericValue(cpf.charAt(9))
                && segundoDigito == Character.getNumericValue(cpf.charAt(10));
    }

    private static int calcularDigito(String cpf, int quantidadeDigitos, int pesoInicial) {
        int soma = 0;

        for (int i = 0; i < quantidadeDigitos; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (pesoInicial - i);
        }

        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    @Override
    public String toString() {
        return value;
    }
}
