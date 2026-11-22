package br.com.ekan.beneficiarios.domain.beneficiario;

import java.util.UUID;

public class BeneficiarioNaoEncontradoException extends RuntimeException {

    public BeneficiarioNaoEncontradoException(UUID id) {
        super("Beneficiario nao encontrado: " + id);
    }
}
