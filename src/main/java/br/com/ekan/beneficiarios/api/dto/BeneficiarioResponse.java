package br.com.ekan.beneficiarios.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Beneficiario cadastrado")
public record BeneficiarioResponse(
        @Schema(description = "Identificador do beneficiario")
        UUID id,

        @Schema(description = "Nome completo")
        String nome,

        @Schema(description = "Telefone com DDD")
        String telefone,

        @Schema(description = "Data de nascimento")
        LocalDate dataNascimento,

        @Schema(description = "Data de inclusao")
        LocalDateTime dataInclusao,

        @Schema(description = "Data da ultima atualizacao")
        LocalDateTime dataAtualizacao,

        @Schema(description = "Documentos do beneficiario")
        List<DocumentoResponse> documentos
) {
}
