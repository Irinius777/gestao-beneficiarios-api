package br.com.ekan.beneficiarios.api.mapper;

import br.com.ekan.beneficiarios.api.dto.BeneficiarioRequest;
import br.com.ekan.beneficiarios.api.dto.BeneficiarioResponse;
import br.com.ekan.beneficiarios.api.dto.BeneficiarioUpdateRequest;
import br.com.ekan.beneficiarios.api.dto.DocumentoRequest;
import br.com.ekan.beneficiarios.api.dto.DocumentoResponse;
import br.com.ekan.beneficiarios.application.command.AtualizarBeneficiarioCommand;
import br.com.ekan.beneficiarios.application.command.CriarBeneficiarioCommand;
import br.com.ekan.beneficiarios.application.command.DocumentoCommand;
import br.com.ekan.beneficiarios.application.dto.BeneficiarioResult;
import br.com.ekan.beneficiarios.application.dto.DocumentoResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BeneficiarioWebMapper {

    public CriarBeneficiarioCommand toCommand(BeneficiarioRequest request) {
        List<DocumentoCommand> documentos = Optional.ofNullable(request.documentos())
                .orElseGet(List::of)
                .stream()
                .map(this::toCommand)
                .toList();

        return new CriarBeneficiarioCommand(
                request.nome(),
                request.cpf(),
                request.telefone(),
                request.dataNascimento(),
                documentos
        );
    }

    public AtualizarBeneficiarioCommand toCommand(BeneficiarioUpdateRequest request) {
        return new AtualizarBeneficiarioCommand(
                request.nome(),
                request.telefone(),
                request.dataNascimento()
        );
    }

    public BeneficiarioResponse toResponse(BeneficiarioResult result) {
        List<DocumentoResponse> documentos = result.documentos().stream()
                .map(this::toResponse)
                .toList();

        return new BeneficiarioResponse(
                result.id(),
                result.nome(),
                result.cpf(),
                result.telefone(),
                result.dataNascimento(),
                result.dataInclusao(),
                result.dataAtualizacao(),
                documentos
        );
    }

    public List<BeneficiarioResponse> toResponse(List<BeneficiarioResult> results) {
        return results.stream()
                .map(this::toResponse)
                .toList();
    }

    public DocumentoResponse toResponse(DocumentoResult result) {
        return new DocumentoResponse(
                result.id(),
                result.tipoDocumento(),
                result.descricao(),
                result.dataInclusao(),
                result.dataAtualizacao()
        );
    }

    private DocumentoCommand toCommand(DocumentoRequest request) {
        return new DocumentoCommand(request.tipoDocumento(), request.descricao());
    }
}
