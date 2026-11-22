package br.com.ekan.beneficiarios.application.mapper;

import br.com.ekan.beneficiarios.application.dto.BeneficiarioResult;
import br.com.ekan.beneficiarios.application.dto.DocumentoResult;
import br.com.ekan.beneficiarios.domain.beneficiario.Beneficiario;
import br.com.ekan.beneficiarios.domain.beneficiario.Documento;

import java.util.List;

public final class BeneficiarioApplicationMapper {

    private BeneficiarioApplicationMapper() {
    }

    public static BeneficiarioResult toResult(Beneficiario beneficiario) {
        List<DocumentoResult> documentos = beneficiario.getDocumentos().stream()
                .map(BeneficiarioApplicationMapper::toResult)
                .toList();

        return new BeneficiarioResult(
                beneficiario.getId().value(),
                beneficiario.getNome(),
                beneficiario.getCpf().value(),
                beneficiario.getTelefone().value(),
                beneficiario.getDataNascimento(),
                beneficiario.getDataInclusao(),
                beneficiario.getDataAtualizacao(),
                documentos
        );
    }

    public static DocumentoResult toResult(Documento documento) {
        return new DocumentoResult(
                documento.getId().value(),
                documento.getTipoDocumento(),
                documento.getDescricao(),
                documento.getDataInclusao(),
                documento.getDataAtualizacao()
        );
    }
}
