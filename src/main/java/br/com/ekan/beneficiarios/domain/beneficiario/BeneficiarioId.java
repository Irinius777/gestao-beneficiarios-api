package br.com.ekan.beneficiarios.domain.beneficiario;

import java.util.Objects;
import java.util.UUID;

public record BeneficiarioId(UUID value) {

    public BeneficiarioId {
        Objects.requireNonNull(value, "id do beneficiario nao pode ser nulo");
    }

    public static BeneficiarioId novo() {
        return new BeneficiarioId(UUID.randomUUID());
    }
}
