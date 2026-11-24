package br.com.ekan.beneficiarios.beneficiario.application.api;

import br.com.ekan.beneficiarios.beneficiario.domain.Beneficiario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Identificador do beneficiario cadastrado")
public record BeneficiarioCriadoResponse(
        @Schema(description = "Identificador do beneficiario", example = "3c1f0e6a-6d7b-4f1e-9c2a-0b7d5e4f8a11")
        UUID idBeneficiario
) {

    public BeneficiarioCriadoResponse(Beneficiario beneficiario) {
        this(beneficiario.getIdBeneficiario());
    }
}
