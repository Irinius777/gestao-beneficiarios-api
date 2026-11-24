package br.com.ekan.beneficiarios.beneficiario.infra;

import br.com.ekan.beneficiarios.beneficiario.application.service.BeneficiarioRepository;
import br.com.ekan.beneficiarios.beneficiario.domain.Beneficiario;
import br.com.ekan.beneficiarios.beneficiario.domain.BeneficiarioNaoEncontradoException;
import br.com.ekan.beneficiarios.beneficiario.domain.CpfJaCadastradoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Log4j2
@Repository
@RequiredArgsConstructor
public class BeneficiarioInfraRepository implements BeneficiarioRepository {

    private static final Sort ORDENACAO_POR_NOME = Sort.by(Sort.Order.asc("nome").ignoreCase());

    private final BeneficiarioSpringDataJpaRepository beneficiarioSpringDataJpaRepository;

    @Override
    public Beneficiario salvar(Beneficiario beneficiario) {
        try {
            return beneficiarioSpringDataJpaRepository.saveAndFlush(beneficiario);
        } catch (DataIntegrityViolationException ex) {
            log.warn("[cpf-duplicado] constraint de CPF unico violada ao salvar");
            throw new CpfJaCadastradoException();
        }
    }

    @Override
    public List<Beneficiario> listarTodosOrdenadosPorNome() {
        return beneficiarioSpringDataJpaRepository.findAll(ORDENACAO_POR_NOME);
    }

    @Override
    public Beneficiario buscarPorId(UUID idBeneficiario) {
        return beneficiarioSpringDataJpaRepository.findById(idBeneficiario)
                .orElseThrow(() -> new BeneficiarioNaoEncontradoException(idBeneficiario));
    }

    @Override
    public Beneficiario buscarPorCpf(String cpf) {
        return beneficiarioSpringDataJpaRepository.findByCpf(cpf)
                .orElseThrow(() -> new BeneficiarioNaoEncontradoException(cpf));
    }

    @Override
    public boolean existePorCpf(String cpf) {
        return beneficiarioSpringDataJpaRepository.existsByCpf(cpf);
    }

    @Override
    public void remover(Beneficiario beneficiario) {
        beneficiarioSpringDataJpaRepository.delete(beneficiario);
    }
}
