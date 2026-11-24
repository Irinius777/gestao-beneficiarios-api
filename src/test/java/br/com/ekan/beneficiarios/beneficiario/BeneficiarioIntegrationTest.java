package br.com.ekan.beneficiarios.beneficiario;

import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioCriadoResponse;
import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioDetalhadoResponse;
import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioListResponse;
import br.com.ekan.beneficiarios.beneficiario.application.service.BeneficiarioRepository;
import br.com.ekan.beneficiarios.beneficiario.application.service.BeneficiarioService;
import br.com.ekan.beneficiarios.beneficiario.domain.Beneficiario;
import br.com.ekan.beneficiarios.beneficiario.domain.BeneficiarioNaoEncontradoException;
import br.com.ekan.beneficiarios.beneficiario.domain.CpfJaCadastradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.CPF;
import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.CPF_SEM_MASCARA;
import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.OUTRO_CPF;
import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.criaBeneficiarioAlteracaoRequest;
import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.criaBeneficiarioNovoRequest;
import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.criaDocumentosRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class BeneficiarioIntegrationTest {

    @Autowired
    private BeneficiarioService beneficiarioService;

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void limpaBase() {
        jdbcTemplate.update("DELETE FROM documentos");
        jdbcTemplate.update("DELETE FROM beneficiarios");
    }

    private int contaDocumentos() {
        Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM documentos", Integer.class);
        return total == null ? 0 : total;
    }

    @Test
    void deveExecutarFluxoCompletoDeCrud() {
        BeneficiarioCriadoResponse criado = beneficiarioService.criar(criaBeneficiarioNovoRequest());
        UUID idBeneficiario = criado.idBeneficiario();
        assertThat(idBeneficiario).isNotNull();

        BeneficiarioDetalhadoResponse buscado = beneficiarioService.buscarPorId(idBeneficiario);
        assertThat(buscado.nome()).isEqualTo("Maria da Silva");
        assertThat(buscado.cpf()).isEqualTo(CPF_SEM_MASCARA);
        assertThat(buscado.telefone()).isEqualTo("11999999999");
        assertThat(buscado.documentos()).hasSize(2)
                .allSatisfy(documento -> assertThat(documento.idDocumento()).isNotNull());

        assertThat(beneficiarioService.buscarPorCpf(CPF).idBeneficiario()).isEqualTo(idBeneficiario);
        assertThat(beneficiarioService.listar()).hasSize(1);
        assertThat(beneficiarioService.listarDocumentos(idBeneficiario)).hasSize(2);

        BeneficiarioDetalhadoResponse atualizado =
                beneficiarioService.atualizar(idBeneficiario, criaBeneficiarioAlteracaoRequest());
        assertThat(atualizado.nome()).isEqualTo("Maria Souza");
        assertThat(atualizado.telefone()).isEqualTo("11988888888");
        assertThat(atualizado.cpf()).isEqualTo(CPF_SEM_MASCARA);
        assertThat(atualizado.documentos()).hasSize(2);
        assertThat(atualizado.dataInclusao()).isEqualToIgnoringNanos(buscado.dataInclusao());
        assertThat(beneficiarioService.buscarPorId(idBeneficiario).nome()).isEqualTo("Maria Souza");

        beneficiarioService.remover(idBeneficiario);

        assertThat(beneficiarioService.listar()).isEmpty();
        assertThatThrownBy(() -> beneficiarioService.buscarPorId(idBeneficiario))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class);
        assertThat(contaDocumentos()).isZero();
    }

    @Test
    void deveCriarBeneficiarioSemDocumentos() {
        BeneficiarioCriadoResponse criado = beneficiarioService.criar(criaBeneficiarioNovoRequest("Maria", CPF, null));

        assertThat(beneficiarioService.listarDocumentos(criado.idBeneficiario())).isEmpty();
    }

    @Test
    void deveListarBeneficiariosEmOrdemAlfabeticaIgnorandoMaiusculas() {
        beneficiarioService.criar(criaBeneficiarioNovoRequest("Zeca", CPF, List.of()));
        beneficiarioService.criar(criaBeneficiarioNovoRequest("ana", OUTRO_CPF, List.of()));

        assertThat(beneficiarioService.listar())
                .extracting(BeneficiarioListResponse::nome)
                .containsExactly("ana", "Zeca");
    }

    @Test
    void devePersistirTipoDoDocumentoComoTexto() {
        beneficiarioService.criar(criaBeneficiarioNovoRequest());

        List<String> tipos = jdbcTemplate.queryForList(
                "SELECT tipo_documento FROM documentos ORDER BY tipo_documento", String.class);

        assertThat(tipos).containsExactly("CNH", "RG");
    }

    @Test
    void deveRejeitarCpfDuplicadoSemPersistir() {
        beneficiarioService.criar(criaBeneficiarioNovoRequest());

        assertThatThrownBy(() -> beneficiarioService.criar(
                criaBeneficiarioNovoRequest("Outra Maria", CPF, criaDocumentosRequest())))
                .isInstanceOf(CpfJaCadastradoException.class);

        assertThat(beneficiarioService.listar()).hasSize(1);
        assertThat(contaDocumentos()).isEqualTo(2);
    }

    @Test
    void deveImpedirCpfDuplicadoNoBanco() {
        beneficiarioService.criar(criaBeneficiarioNovoRequest());
        Beneficiario duplicado = new Beneficiario(
                criaBeneficiarioNovoRequest("Outra Maria", CPF, List.of()), LocalDateTime.now());

        assertThatThrownBy(() -> beneficiarioRepository.salvar(duplicado))
                .isInstanceOf(CpfJaCadastradoException.class);

        assertThat(beneficiarioService.listar()).hasSize(1);
    }

    @Test
    void deveLancarExcecaoAoBuscarCpfInexistente() {
        assertThatThrownBy(() -> beneficiarioService.buscarPorCpf(OUTRO_CPF))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecaoParaBeneficiarioInexistente() {
        UUID idBeneficiario = UUID.randomUUID();

        assertThatThrownBy(() -> beneficiarioService.buscarPorId(idBeneficiario))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class);
        assertThatThrownBy(() -> beneficiarioService.listarDocumentos(idBeneficiario))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class);
        assertThatThrownBy(() -> beneficiarioService.atualizar(idBeneficiario, criaBeneficiarioAlteracaoRequest()))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class);
        assertThatThrownBy(() -> beneficiarioService.remover(idBeneficiario))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class);
    }
}
