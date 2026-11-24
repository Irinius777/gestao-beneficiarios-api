package br.com.ekan.beneficiarios.beneficiario.application.service;

import br.com.ekan.beneficiarios.beneficiario.domain.Beneficiario;

import java.util.List;
import java.util.UUID;

public interface BeneficiarioRepository {

    Beneficiario salvar(Beneficiario beneficiario);

    List<Beneficiario> listarTodosOrdenadosPorNome();

    Beneficiario buscarPorId(UUID idBeneficiario);

    Beneficiario buscarPorCpf(String cpf);

    boolean existePorCpf(String cpf);

    void remover(Beneficiario beneficiario);
}
