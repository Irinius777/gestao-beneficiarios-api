package br.com.ekan.beneficiarios.utils;

public final class FormatacaoUtils {

    private FormatacaoUtils() {
    }

    public static String removeMascara(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }
}
