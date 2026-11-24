package br.com.ekan.beneficiarios.beneficiario.application.service;

import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioAlteracaoRequest;
import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioCriadoResponse;
import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioDetalhadoResponse;
import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioListResponse;
import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioNovoRequest;
import br.com.ekan.beneficiarios.beneficiario.application.api.DocumentoResponse;

import java.util.List;
import java.util.UUID;

public interface BeneficiarioService {

    BeneficiarioCriadoResponse criar(BeneficiarioNovoRequest request);

    List<BeneficiarioListResponse> listar();

    BeneficiarioDetalhadoResponse buscarPorId(UUID idBeneficiario);

    BeneficiarioDetalhadoResponse buscarPorCpf(String cpf);

    List<DocumentoResponse> listarDocumentos(UUID idBeneficiario);

    BeneficiarioDetalhadoResponse atualizar(UUID idBeneficiario, BeneficiarioAlteracaoRequest request);

    void remover(UUID idBeneficiario);
}
