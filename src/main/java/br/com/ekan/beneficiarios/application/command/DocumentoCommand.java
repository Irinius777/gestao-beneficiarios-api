package br.com.ekan.beneficiarios.application.command;

import br.com.ekan.beneficiarios.domain.beneficiario.TipoDocumento;

public record DocumentoCommand(TipoDocumento tipoDocumento, String descricao) {
}
