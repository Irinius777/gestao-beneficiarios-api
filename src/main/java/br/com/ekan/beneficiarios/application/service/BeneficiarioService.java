package br.com.ekan.beneficiarios.application.service;

import br.com.ekan.beneficiarios.application.command.AtualizarBeneficiarioCommand;
import br.com.ekan.beneficiarios.application.command.CriarBeneficiarioCommand;
import br.com.ekan.beneficiarios.application.dto.BeneficiarioResult;
import br.com.ekan.beneficiarios.application.dto.DocumentoResult;

import java.util.List;
import java.util.UUID;

public interface BeneficiarioService {

    BeneficiarioResult criar(CriarBeneficiarioCommand command);

    List<BeneficiarioResult> listar();

    BeneficiarioResult buscarPorId(UUID id);

    List<DocumentoResult> listarDocumentos(UUID beneficiarioId);

    BeneficiarioResult atualizar(UUID id, AtualizarBeneficiarioCommand command);

    void remover(UUID id);
}
