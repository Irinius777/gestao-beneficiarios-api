package br.com.ekan.beneficiarios.beneficiario.infra;

import br.com.ekan.beneficiarios.beneficiario.domain.Beneficiario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BeneficiarioSpringDataJpaRepository extends JpaRepository<Beneficiario, UUID> {

    @Override
    @EntityGraph(attributePaths = "documentos")
    Optional<Beneficiario> findById(UUID idBeneficiario);

    @EntityGraph(attributePaths = "documentos")
    Optional<Beneficiario> findByCpf(String cpf);

    boolean existsByCpf(String cpf);
}
