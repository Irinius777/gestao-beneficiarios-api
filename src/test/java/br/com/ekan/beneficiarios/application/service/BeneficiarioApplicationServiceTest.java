package br.com.ekan.beneficiarios.application.service;

import br.com.ekan.beneficiarios.application.command.AtualizarBeneficiarioCommand;
import br.com.ekan.beneficiarios.application.command.CriarBeneficiarioCommand;
import br.com.ekan.beneficiarios.application.command.DocumentoCommand;
import br.com.ekan.beneficiarios.application.dto.BeneficiarioResult;
import br.com.ekan.beneficiarios.application.dto.DocumentoResult;
import br.com.ekan.beneficiarios.domain.beneficiario.Beneficiario;
import br.com.ekan.beneficiarios.domain.beneficiario.BeneficiarioId;
import br.com.ekan.beneficiarios.domain.beneficiario.BeneficiarioNaoEncontradoException;
import br.com.ekan.beneficiarios.domain.beneficiario.BeneficiarioRepository;
import br.com.ekan.beneficiarios.domain.beneficiario.Cpf;
import br.com.ekan.beneficiarios.domain.beneficiario.CpfJaCadastradoException;
import br.com.ekan.beneficiarios.domain.beneficiario.Documento;
import br.com.ekan.beneficiarios.domain.beneficiario.Telefone;
import br.com.ekan.beneficiarios.domain.beneficiario.TipoDocumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeneficiarioApplicationServiceTest {

    private static final LocalDate NASCIMENTO = LocalDate.of(1990, 5, 20);
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 1, 15, 10, 0);
    private static final String CPF = "529.982.247-25";
    private static final String CPF_NORMALIZADO = "52998224725";
    private static final String OUTRO_CPF = "11144477735";

    @Mock
    private BeneficiarioRepository repository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC);

    private BeneficiarioApplicationService service;

    @BeforeEach
    void setUp() {
        service = new BeneficiarioApplicationService(repository, clock);
    }

    private CriarBeneficiarioCommand criarCommand(String nome, String cpf, String telefone,
                                                  List<DocumentoCommand> documentos) {
        return new CriarBeneficiarioCommand(nome, cpf, telefone, NASCIMENTO, documentos);
    }

    private CriarBeneficiarioCommand criarCommand() {
        return criarCommand("Maria da Silva", CPF, "(11) 99999-9999", List.of(
                new DocumentoCommand(TipoDocumento.RG, "12.345.678-9"),
                new DocumentoCommand(TipoDocumento.CNH, "12345678900")
        ));
    }

    private Beneficiario beneficiario(String nome, String cpf, String telefone,
                                      List<Documento> documentos) {
        return Beneficiario.novo(nome, new Cpf(cpf), new Telefone(telefone),
                NASCIMENTO, documentos, AGORA);
    }

    private Beneficiario beneficiario() {
        return beneficiario("Maria da Silva", CPF, "(11) 99999-9999", List.of(
                Documento.novo(TipoDocumento.RG, "12.345.678-9", AGORA)
        ));
    }

    @Test
    void deveCriarBeneficiarioComDocumentos() {
        when(repository.salvar(any(Beneficiario.class))).thenAnswer(inv -> inv.getArgument(0));

        BeneficiarioResult response = service.criar(criarCommand());

        ArgumentCaptor<Beneficiario> captor = ArgumentCaptor.forClass(Beneficiario.class);
        verify(repository).salvar(captor.capture());
        Beneficiario salvo = captor.getValue();

        assertThat(salvo.getNome()).isEqualTo("Maria da Silva");
        assertThat(salvo.getCpf().value()).isEqualTo(CPF_NORMALIZADO);
        assertThat(salvo.getTelefone().value()).isEqualTo("11999999999");
        assertThat(salvo.getDataNascimento()).isEqualTo(NASCIMENTO);
        assertThat(salvo.getDataInclusao()).isEqualTo(AGORA);
        assertThat(salvo.getDataAtualizacao()).isEqualTo(AGORA);
        assertThat(salvo.getDocumentos()).hasSize(2);
        assertThat(salvo.getDocumentos())
                .extracting(Documento::getTipoDocumento)
                .containsExactly(TipoDocumento.RG, TipoDocumento.CNH);
        assertThat(salvo.getDocumentos())
                .allSatisfy(documento -> {
                    assertThat(documento.getId()).isNotNull();
                    assertThat(documento.getDataInclusao()).isEqualTo(AGORA);
                    assertThat(documento.getDataAtualizacao()).isEqualTo(AGORA);
                });

        assertThat(response.id()).isEqualTo(salvo.getId().value());
        assertThat(response.cpf()).isEqualTo(CPF_NORMALIZADO);
        assertThat(response.documentos()).hasSize(2);
    }

    @Test
    void deveCriarBeneficiarioSemDocumentosQuandoListaForNula() {
        when(repository.salvar(any(Beneficiario.class))).thenAnswer(inv -> inv.getArgument(0));

        BeneficiarioResult response = service.criar(
                criarCommand("Maria", CPF, "11999999999", null));

        assertThat(response.documentos()).isEmpty();
    }

    @Test
    void deveNormalizarNomeCpfETelefoneAoCriar() {
        when(repository.salvar(any(Beneficiario.class))).thenAnswer(inv -> inv.getArgument(0));

        BeneficiarioResult response = service.criar(
                criarCommand("  Maria  ", CPF, "(11) 98888-7777", List.of()));

        assertThat(response.nome()).isEqualTo("Maria");
        assertThat(response.cpf()).isEqualTo(CPF_NORMALIZADO);
        assertThat(response.telefone()).isEqualTo("11988887777");
    }

    @Test
    void deveRejeitarTelefoneInvalidoAntesDeSalvar() {
        assertThatThrownBy(() -> service.criar(
                criarCommand("Maria", CPF, "123", List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("10 ou 11 digitos");

        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarCpfInvalidoAntesDeSalvar() {
        assertThatThrownBy(() -> service.criar(
                criarCommand("Maria", "11111111111", "11999999999", List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalido");

        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarCpfDuplicadoAntesDeSalvar() {
        when(repository.existePorCpf(any(Cpf.class))).thenReturn(true);

        assertThatThrownBy(() -> service.criar(criarCommand()))
                .isInstanceOf(CpfJaCadastradoException.class)
                .hasMessageContaining("CPF ja cadastrado");

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveListarBeneficiariosEmOrdemAlfabetica() {
        when(repository.listarTodos()).thenReturn(List.of(
                beneficiario("Zeca", CPF, "11999999999", List.of()),
                beneficiario("Ana", OUTRO_CPF, "11888888888", List.of())
        ));

        List<BeneficiarioResult> response = service.listar();

        assertThat(response).extracting(BeneficiarioResult::nome)
                .containsExactly("Ana", "Zeca");
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverBeneficiarios() {
        when(repository.listarTodos()).thenReturn(List.of());

        assertThat(service.listar()).isEmpty();
    }

    @Test
    void deveBuscarBeneficiarioPorId() {
        Beneficiario beneficiario = beneficiario();
        when(repository.buscarPorId(beneficiario.getId())).thenReturn(Optional.of(beneficiario));

        BeneficiarioResult response = service.buscarPorId(beneficiario.getId().value());

        assertThat(response.id()).isEqualTo(beneficiario.getId().value());
        assertThat(response.nome()).isEqualTo("Maria da Silva");
        assertThat(response.cpf()).isEqualTo(CPF_NORMALIZADO);
        assertThat(response.documentos()).hasSize(1);
    }

    @Test
    void deveBuscarBeneficiarioPorCpf() {
        Beneficiario beneficiario = beneficiario();
        when(repository.buscarPorCpf(new Cpf(CPF))).thenReturn(Optional.of(beneficiario));

        BeneficiarioResult response = service.buscarPorCpf(CPF);

        assertThat(response.id()).isEqualTo(beneficiario.getId().value());
        assertThat(response.cpf()).isEqualTo(CPF_NORMALIZADO);
    }

    @Test
    void deveLancarExcecaoAoBuscarCpfInexistente() {
        when(repository.buscarPorCpf(any(Cpf.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorCpf(CPF))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class)
                .hasMessageContaining("CPF");
    }

    @Test
    void deveRejeitarCpfInvalidoAoBuscar() {
        assertThatThrownBy(() -> service.buscarPorCpf("111.111.111-11"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalido");

        verifyNoInteractions(repository);
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.buscarPorId(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(id))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void deveListarDocumentosDoBeneficiario() {
        Beneficiario beneficiario = beneficiario();
        when(repository.buscarPorId(beneficiario.getId())).thenReturn(Optional.of(beneficiario));

        List<DocumentoResult> documentos = service.listarDocumentos(beneficiario.getId().value());

        assertThat(documentos).hasSize(1);
        assertThat(documentos.get(0).tipoDocumento()).isEqualTo(TipoDocumento.RG);
    }

    @Test
    void deveLancarExcecaoAoListarDocumentosDeBeneficiarioInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.buscarPorId(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listarDocumentos(id))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class);
    }

    @Test
    void deveAtualizarDadosDoBeneficiario() {
        Beneficiario original = beneficiario();
        when(repository.buscarPorId(original.getId())).thenReturn(Optional.of(original));
        when(repository.salvar(any(Beneficiario.class))).thenAnswer(inv -> inv.getArgument(0));

        BeneficiarioResult response = service.atualizar(original.getId().value(),
                new AtualizarBeneficiarioCommand("Maria Souza", "(11) 98888-8888",
                        LocalDate.of(1992, 8, 10)));

        assertThat(response.id()).isEqualTo(original.getId().value());
        assertThat(response.nome()).isEqualTo("Maria Souza");
        assertThat(response.cpf()).isEqualTo(CPF_NORMALIZADO);
        assertThat(response.telefone()).isEqualTo("11988888888");
        assertThat(response.dataNascimento()).isEqualTo(LocalDate.of(1992, 8, 10));
        assertThat(response.dataInclusao()).isEqualTo(AGORA);
        assertThat(response.dataAtualizacao()).isEqualTo(AGORA);
        assertThat(response.documentos()).hasSize(1);

        ArgumentCaptor<Beneficiario> captor = ArgumentCaptor.forClass(Beneficiario.class);
        verify(repository).salvar(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(original.getId());
        assertThat(captor.getValue().getCpf()).isEqualTo(original.getCpf());
        assertThat(captor.getValue().getDocumentos()).isEqualTo(original.getDocumentos());

        assertThat(original.getNome()).isEqualTo("Maria da Silva");
    }

    @Test
    void deveLancarExcecaoAoAtualizarIdInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.buscarPorId(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(id,
                new AtualizarBeneficiarioCommand("Maria", "11999999999", NASCIMENTO)))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class);

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveRemoverBeneficiarioExistente() {
        UUID id = UUID.randomUUID();
        when(repository.remover(any())).thenReturn(true);

        service.remover(id);

        verify(repository).remover(new BeneficiarioId(id));
    }

    @Test
    void deveLancarExcecaoAoRemoverIdInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.remover(any())).thenReturn(false);

        assertThatThrownBy(() -> service.remover(id))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class);
    }
}
