package br.com.ekan.beneficiarios.beneficiario.application.service;

import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioCriadoResponse;
import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioDetalhadoResponse;
import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioListResponse;
import br.com.ekan.beneficiarios.beneficiario.application.api.DocumentoResponse;
import br.com.ekan.beneficiarios.beneficiario.domain.Beneficiario;
import br.com.ekan.beneficiarios.beneficiario.domain.BeneficiarioNaoEncontradoException;
import br.com.ekan.beneficiarios.beneficiario.domain.CpfJaCadastradoException;
import br.com.ekan.beneficiarios.beneficiario.domain.TipoDocumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.AGORA;
import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.CPF;
import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.CPF_SEM_MASCARA;
import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.criaBeneficiario;
import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.criaBeneficiarioAlteracaoRequest;
import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.criaBeneficiarioNovoRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeneficiarioApplicationServiceTest {

    @Mock
    private BeneficiarioRepository beneficiarioRepository;

    private BeneficiarioApplicationService beneficiarioService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AGORA.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        beneficiarioService = new BeneficiarioApplicationService(beneficiarioRepository, clock);
    }

    @Test
    void deveCriarBeneficiarioComDocumentos() {
        when(beneficiarioRepository.existePorCpf(CPF_SEM_MASCARA)).thenReturn(false);

        BeneficiarioCriadoResponse response = beneficiarioService.criar(criaBeneficiarioNovoRequest());

        ArgumentCaptor<Beneficiario> captor = ArgumentCaptor.forClass(Beneficiario.class);
        verify(beneficiarioRepository).salvar(captor.capture());
        Beneficiario salvo = captor.getValue();
        assertThat(response.idBeneficiario()).isEqualTo(salvo.getIdBeneficiario());
        assertThat(salvo.getCpf()).isEqualTo(CPF_SEM_MASCARA);
        assertThat(salvo.getDataInclusao()).isEqualTo(AGORA);
        assertThat(salvo.getDocumentos()).hasSize(2);
    }

    @Test
    void deveRejeitarCpfDuplicadoAntesDeSalvar() {
        when(beneficiarioRepository.existePorCpf(CPF_SEM_MASCARA)).thenReturn(true);

        assertThatThrownBy(() -> beneficiarioService.criar(criaBeneficiarioNovoRequest()))
                .isInstanceOf(CpfJaCadastradoException.class);

        verify(beneficiarioRepository, never()).salvar(any());
    }

    @Test
    void deveListarBeneficiariosSemDocumentos() {
        Beneficiario beneficiario = criaBeneficiario();
        when(beneficiarioRepository.listarTodosOrdenadosPorNome()).thenReturn(List.of(beneficiario));

        List<BeneficiarioListResponse> response = beneficiarioService.listar();

        assertThat(response)
                .extracting(BeneficiarioListResponse::idBeneficiario)
                .containsExactly(beneficiario.getIdBeneficiario());
    }

    @Test
    void deveBuscarBeneficiarioPorId() {
        Beneficiario beneficiario = criaBeneficiario();
        when(beneficiarioRepository.buscarPorId(beneficiario.getIdBeneficiario())).thenReturn(beneficiario);

        BeneficiarioDetalhadoResponse response = beneficiarioService.buscarPorId(beneficiario.getIdBeneficiario());

        assertThat(response.idBeneficiario()).isEqualTo(beneficiario.getIdBeneficiario());
        assertThat(response.documentos()).hasSize(2);
    }

    @Test
    void deveBuscarBeneficiarioPorCpfRemovendoMascara() {
        Beneficiario beneficiario = criaBeneficiario();
        when(beneficiarioRepository.buscarPorCpf(CPF_SEM_MASCARA)).thenReturn(beneficiario);

        BeneficiarioDetalhadoResponse response = beneficiarioService.buscarPorCpf(CPF);

        assertThat(response.cpf()).isEqualTo(CPF_SEM_MASCARA);
    }

    @Test
    void deveListarDocumentosDoBeneficiario() {
        Beneficiario beneficiario = criaBeneficiario();
        when(beneficiarioRepository.buscarPorId(beneficiario.getIdBeneficiario())).thenReturn(beneficiario);

        List<DocumentoResponse> response = beneficiarioService.listarDocumentos(beneficiario.getIdBeneficiario());

        assertThat(response)
                .extracting(DocumentoResponse::tipoDocumento)
                .containsExactly(TipoDocumento.RG, TipoDocumento.CNH);
    }

    @Test
    void devePropagarExcecaoQuandoBeneficiarioNaoExistir() {
        UUID idBeneficiario = UUID.randomUUID();
        when(beneficiarioRepository.buscarPorId(idBeneficiario))
                .thenThrow(new BeneficiarioNaoEncontradoException(idBeneficiario));

        assertThatThrownBy(() -> beneficiarioService.buscarPorId(idBeneficiario))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class);
    }

    @Test
    void deveAtualizarDadosDoBeneficiario() {
        Beneficiario beneficiario = criaBeneficiario();
        when(beneficiarioRepository.buscarPorId(beneficiario.getIdBeneficiario())).thenReturn(beneficiario);

        BeneficiarioDetalhadoResponse response = beneficiarioService.atualizar(
                beneficiario.getIdBeneficiario(), criaBeneficiarioAlteracaoRequest());

        verify(beneficiarioRepository).salvar(beneficiario);
        assertThat(response.nome()).isEqualTo("Maria Souza");
        assertThat(response.telefone()).isEqualTo("11988888888");
        assertThat(response.cpf()).isEqualTo(CPF_SEM_MASCARA);
    }

    @Test
    void deveNaoSalvarQuandoBeneficiarioAtualizadoNaoExistir() {
        UUID idBeneficiario = UUID.randomUUID();
        when(beneficiarioRepository.buscarPorId(idBeneficiario))
                .thenThrow(new BeneficiarioNaoEncontradoException(idBeneficiario));

        assertThatThrownBy(() -> beneficiarioService.atualizar(idBeneficiario, criaBeneficiarioAlteracaoRequest()))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class);

        verify(beneficiarioRepository, never()).salvar(any());
    }

    @Test
    void deveRemoverBeneficiarioExistente() {
        Beneficiario beneficiario = criaBeneficiario();
        when(beneficiarioRepository.buscarPorId(beneficiario.getIdBeneficiario())).thenReturn(beneficiario);

        beneficiarioService.remover(beneficiario.getIdBeneficiario());

        verify(beneficiarioRepository).remover(beneficiario);
    }

    @Test
    void deveNaoRemoverQuandoBeneficiarioNaoExistir() {
        UUID idBeneficiario = UUID.randomUUID();
        when(beneficiarioRepository.buscarPorId(idBeneficiario))
                .thenThrow(new BeneficiarioNaoEncontradoException(idBeneficiario));

        assertThatThrownBy(() -> beneficiarioService.remover(idBeneficiario))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class);

        verify(beneficiarioRepository, never()).remover(any());
    }
}
