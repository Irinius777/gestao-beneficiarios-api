package br.com.ekan.beneficiarios.beneficiario.application.service;

import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioAlteracaoRequest;
import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioCriadoResponse;
import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioDetalhadoResponse;
import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioListResponse;
import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioNovoRequest;
import br.com.ekan.beneficiarios.beneficiario.application.api.DocumentoResponse;
import br.com.ekan.beneficiarios.beneficiario.domain.Beneficiario;
import br.com.ekan.beneficiarios.beneficiario.domain.CpfJaCadastradoException;
import br.com.ekan.beneficiarios.utils.FormatacaoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class BeneficiarioApplicationService implements BeneficiarioService {

    private final BeneficiarioRepository beneficiarioRepository;
    private final Clock clock;

    @Override
    @Transactional
    public BeneficiarioCriadoResponse criar(BeneficiarioNovoRequest request) {
        Beneficiario beneficiario = new Beneficiario(request, LocalDateTime.now(clock));
        validaCpfDisponivel(beneficiario.getCpf());
        beneficiarioRepository.salvar(beneficiario);
        log.info("[beneficiario-criado] idBeneficiario={}, documentos={}",
                beneficiario.getIdBeneficiario(), beneficiario.getDocumentos().size());
        return new BeneficiarioCriadoResponse(beneficiario);
    }

    @Override
    public List<BeneficiarioListResponse> listar() {
        return BeneficiarioListResponse.converte(beneficiarioRepository.listarTodosOrdenadosPorNome());
    }

    @Override
    @Transactional(readOnly = true)
    public BeneficiarioDetalhadoResponse buscarPorId(UUID idBeneficiario) {
        return new BeneficiarioDetalhadoResponse(beneficiarioRepository.buscarPorId(idBeneficiario));
    }

    @Override
    @Transactional(readOnly = true)
    public BeneficiarioDetalhadoResponse buscarPorCpf(String cpf) {
        Beneficiario beneficiario = beneficiarioRepository.buscarPorCpf(FormatacaoUtils.removeMascara(cpf));
        return new BeneficiarioDetalhadoResponse(beneficiario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoResponse> listarDocumentos(UUID idBeneficiario) {
        Beneficiario beneficiario = beneficiarioRepository.buscarPorId(idBeneficiario);
        return DocumentoResponse.converte(beneficiario.getDocumentos());
    }

    @Override
    @Transactional
    public BeneficiarioDetalhadoResponse atualizar(UUID idBeneficiario, BeneficiarioAlteracaoRequest request) {
        Beneficiario beneficiario = beneficiarioRepository.buscarPorId(idBeneficiario);
        beneficiario.altera(request, LocalDateTime.now(clock));
        beneficiarioRepository.salvar(beneficiario);
        log.info("[beneficiario-atualizado] idBeneficiario={}", idBeneficiario);
        return new BeneficiarioDetalhadoResponse(beneficiario);
    }

    @Override
    @Transactional
    public void remover(UUID idBeneficiario) {
        Beneficiario beneficiario = beneficiarioRepository.buscarPorId(idBeneficiario);
        beneficiarioRepository.remover(beneficiario);
        log.info("[beneficiario-removido] idBeneficiario={}", idBeneficiario);
    }

    private void validaCpfDisponivel(String cpf) {
        if (beneficiarioRepository.existePorCpf(cpf)) {
            log.warn("[cpf-duplicado] tentativa de cadastro com CPF ja existente");
            throw new CpfJaCadastradoException();
        }
    }
}
