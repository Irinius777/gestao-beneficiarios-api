package br.com.ekan.beneficiarios.api.dto;

import br.com.ekan.beneficiarios.domain.beneficiario.TipoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Documento enviado no cadastro do beneficiario")
public record DocumentoRequest(
        @NotNull(message = "tipoDocumento e obrigatorio")
        @Schema(description = "Tipo do documento", example = "CPF")
        TipoDocumento tipoDocumento,

        @NotBlank(message = "descricao e obrigatoria")
        @Size(max = 255, message = "descricao deve ter no maximo 255 caracteres")
        @Schema(description = "Descricao ou numero do documento", example = "123.456.789-00")
        String descricao
) {
}
