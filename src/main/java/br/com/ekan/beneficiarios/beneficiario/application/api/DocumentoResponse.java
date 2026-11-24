package br.com.ekan.beneficiarios.beneficiario.application.api;

import br.com.ekan.beneficiarios.beneficiario.domain.Documento;
import br.com.ekan.beneficiarios.beneficiario.domain.TipoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Documento do beneficiario")
public record DocumentoResponse(
        @Schema(description = "Identificador do documento")
        UUID idDocumento,

        @Schema(description = "Tipo do documento", example = "RG")
        TipoDocumento tipoDocumento,

        @Schema(description = "Descricao ou numero do documento")
        String descricao,

        @Schema(description = "Data de inclusao")
        LocalDateTime dataInclusao,

        @Schema(description = "Data da ultima atualizacao")
        LocalDateTime dataAtualizacao
) {

    public DocumentoResponse(Documento documento) {
        this(documento.getIdDocumento(),
                documento.getTipoDocumento(),
                documento.getDescricao(),
                documento.getDataInclusao(),
                documento.getDataAtualizacao());
    }

    public static List<DocumentoResponse> converte(List<Documento> documentos) {
        return documentos.stream()
                .map(DocumentoResponse::new)
                .toList();
    }
}
