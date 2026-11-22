package br.com.ekan.beneficiarios.infrastructure.persistence;

import br.com.ekan.beneficiarios.domain.beneficiario.Beneficiario;
import br.com.ekan.beneficiarios.domain.beneficiario.BeneficiarioId;
import br.com.ekan.beneficiarios.domain.beneficiario.BeneficiarioRepository;
import br.com.ekan.beneficiarios.domain.beneficiario.Cpf;
import br.com.ekan.beneficiarios.domain.beneficiario.CpfJaCadastradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaBeneficiarioRepositoryAdapter implements BeneficiarioRepository {

    private final BeneficiarioJpaRepository jpaRepository;
    private final BeneficiarioPersistenceMapper mapper;

    @Override
    @Transactional
    public Beneficiario salvar(Beneficiario beneficiario) {
        BeneficiarioJpaEntity entity = jpaRepository.findById(beneficiario.getId().value())
                .map(existente -> {
                    mapper.atualizarDados(existente, beneficiario);
                    return existente;
                })
                .orElseGet(() -> mapper.toNewEntity(beneficiario));

        try {
            return mapper.toDomain(jpaRepository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException ex) {
            throw new CpfJaCadastradoException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Beneficiario> buscarPorId(BeneficiarioId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Beneficiario> buscarPorCpf(Cpf cpf) {
        return jpaRepository.findByCpf(cpf.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCpf(Cpf cpf) {
        return jpaRepository.existsByCpf(cpf.value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Beneficiario> listarTodos() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public boolean remover(BeneficiarioId id) {
        if (!jpaRepository.existsById(id.value())) {
            return false;
        }
        jpaRepository.deleteById(id.value());
        return true;
    }
}
