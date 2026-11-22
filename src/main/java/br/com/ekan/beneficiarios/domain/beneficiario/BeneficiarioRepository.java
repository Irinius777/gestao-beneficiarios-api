package br.com.ekan.beneficiarios.domain.beneficiario;

import java.util.List;
import java.util.Optional;

/**
 * Porta de repositorio do agregado de beneficiario.
 */
public interface BeneficiarioRepository {

    Beneficiario salvar(Beneficiario beneficiario);

    Optional<Beneficiario> buscarPorId(BeneficiarioId id);

    List<Beneficiario> listarTodos();

    boolean remover(BeneficiarioId id);
}
