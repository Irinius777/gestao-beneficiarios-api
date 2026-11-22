package br.com.ekan.beneficiarios.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BeneficiarioJpaRepository extends JpaRepository<BeneficiarioJpaEntity, UUID> {

    @Override
    @EntityGraph(attributePaths = "documentos")
    Optional<BeneficiarioJpaEntity> findById(UUID id);

    @Override
    @EntityGraph(attributePaths = "documentos")
    List<BeneficiarioJpaEntity> findAll();

    @EntityGraph(attributePaths = "documentos")
    Optional<BeneficiarioJpaEntity> findByCpf(String cpf);

    boolean existsByCpf(String cpf);
}
