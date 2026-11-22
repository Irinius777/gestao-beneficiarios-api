package br.com.ekan.beneficiarios.api.dto;

import br.com.ekan.beneficiarios.domain.beneficiario.TipoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Documento do beneficiario")
public record DocumentoResponse(
        @Schema(description = "Identificador do documento")
        UUID id,

        @Schema(description = "Tipo do documento", example = "CPF")
        TipoDocumento tipoDocumento,

        @Schema(description = "Descricao ou numero do documento")
        String descricao,

        @Schema(description = "Data de inclusao")
        LocalDateTime dataInclusao,

        @Schema(description = "Data da ultima atualizacao")
        LocalDateTime dataAtualizacao
) {
}
