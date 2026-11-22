package br.com.ekan.beneficiarios.application.dto;

import br.com.ekan.beneficiarios.domain.beneficiario.TipoDocumento;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentoResult(UUID id,
                              TipoDocumento tipoDocumento,
                              String descricao,
                              LocalDateTime dataInclusao,
                              LocalDateTime dataAtualizacao) {
}
