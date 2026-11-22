package br.com.ekan.beneficiarios.domain.beneficiario;

import java.util.Objects;
import java.util.UUID;

public record DocumentoId(UUID value) {

    public DocumentoId {
        Objects.requireNonNull(value, "id do documento nao pode ser nulo");
    }

    public static DocumentoId novo() {
        return new DocumentoId(UUID.randomUUID());
    }
}
