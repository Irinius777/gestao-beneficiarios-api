package br.com.ekan.beneficiarios.domain.beneficiario;

/**
 * Excecao de dominio disparada quando o CPF informado ja pertence a outro
 * beneficiario.
 */
public class CpfJaCadastradoException extends RuntimeException {

    public CpfJaCadastradoException() {
        super("CPF ja cadastrado");
    }
}
