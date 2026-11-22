package br.com.ekan.beneficiarios.application.service;

import br.com.ekan.beneficiarios.application.command.AtualizarBeneficiarioCommand;
import br.com.ekan.beneficiarios.application.command.CriarBeneficiarioCommand;
import br.com.ekan.beneficiarios.application.dto.BeneficiarioResult;
import br.com.ekan.beneficiarios.application.dto.DocumentoResult;
import br.com.ekan.beneficiarios.application.mapper.BeneficiarioApplicationMapper;
import br.com.ekan.beneficiarios.domain.beneficiario.Beneficiario;
import br.com.ekan.beneficiarios.domain.beneficiario.BeneficiarioId;
import br.com.ekan.beneficiarios.domain.beneficiario.BeneficiarioNaoEncontradoException;
import br.com.ekan.beneficiarios.domain.beneficiario.BeneficiarioRepository;
import br.com.ekan.beneficiarios.domain.beneficiario.Cpf;
import br.com.ekan.beneficiarios.domain.beneficiario.CpfJaCadastradoException;
import br.com.ekan.beneficiarios.domain.beneficiario.Documento;
import br.com.ekan.beneficiarios.domain.beneficiario.Telefone;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeneficiarioApplicationService implements BeneficiarioService {

    private final BeneficiarioRepository repository;
    private final Clock clock;

    @Override
    @Transactional
    public BeneficiarioResult criar(CriarBeneficiarioCommand command) {
        Objects.requireNonNull(command, "command nao pode ser nulo");
        LocalDateTime agora = LocalDateTime.now(clock);

        List<Documento> documentos = Optional.ofNullable(command.documentos())
                .orElseGet(List::of)
                .stream()
                .map(documento -> Documento.novo(documento.tipoDocumento(), documento.descricao(), agora))
                .toList();

        Cpf cpf = new Cpf(command.cpf());
        Beneficiario beneficiario = Beneficiario.novo(
                command.nome(),
                cpf,
                new Telefone(command.telefone()),
                command.dataNascimento(),
                documentos,
                agora
        );

        if (repository.existePorCpf(cpf)) {
            throw new CpfJaCadastradoException();
        }

        Beneficiario salvo = repository.salvar(beneficiario);
        log.info("Beneficiario cadastrado: {}", salvo.getId().value());
        return BeneficiarioApplicationMapper.toResult(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiarioResult> listar() {
        return repository.listarTodos().stream()
                .sorted(Comparator.comparing(Beneficiario::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(BeneficiarioApplicationMapper::toResult)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BeneficiarioResult buscarPorId(UUID id) {
        return BeneficiarioApplicationMapper.toResult(buscarBeneficiario(id));
    }

    @Override
    @Transactional(readOnly = true)
    public BeneficiarioResult buscarPorCpf(String cpf) {
        Cpf cpfNormalizado = new Cpf(cpf);
        return repository.buscarPorCpf(cpfNormalizado)
                .map(BeneficiarioApplicationMapper::toResult)
                .orElseThrow(() -> new BeneficiarioNaoEncontradoException(cpfNormalizado.value()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoResult> listarDocumentos(UUID beneficiarioId) {
        return buscarBeneficiario(beneficiarioId).getDocumentos().stream()
                .map(BeneficiarioApplicationMapper::toResult)
                .toList();
    }

    @Override
    @Transactional
    public BeneficiarioResult atualizar(UUID id, AtualizarBeneficiarioCommand command) {
        Objects.requireNonNull(command, "command nao pode ser nulo");
        Beneficiario beneficiario = buscarBeneficiario(id);

        Beneficiario atualizado = beneficiario.atualizarDados(
                command.nome(),
                new Telefone(command.telefone()),
                command.dataNascimento(),
                LocalDateTime.now(clock)
        );

        Beneficiario salvo = repository.salvar(atualizado);
        log.info("Beneficiario atualizado: {}", salvo.getId().value());
        return BeneficiarioApplicationMapper.toResult(salvo);
    }

    @Override
    @Transactional
    public void remover(UUID id) {
        boolean removido = repository.remover(new BeneficiarioId(id));
        if (!removido) {
            throw new BeneficiarioNaoEncontradoException(id);
        }
        log.info("Beneficiario removido: {}", id);
    }

    private Beneficiario buscarBeneficiario(UUID id) {
        return repository.buscarPorId(new BeneficiarioId(id))
                .orElseThrow(() -> new BeneficiarioNaoEncontradoException(id));
    }
}
