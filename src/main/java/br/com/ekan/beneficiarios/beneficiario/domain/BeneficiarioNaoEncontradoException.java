package br.com.ekan.beneficiarios.beneficiario.domain;

import java.util.UUID;

public class BeneficiarioNaoEncontradoException extends RuntimeException {

    public BeneficiarioNaoEncontradoException(UUID id) {
        super("Beneficiario nao encontrado: " + id);
    }

    public BeneficiarioNaoEncontradoException(String cpf) {
        super("Beneficiario nao encontrado para o CPF informado");
    }
}
