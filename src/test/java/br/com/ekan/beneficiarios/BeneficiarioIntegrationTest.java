package br.com.ekan.beneficiarios;

import br.com.ekan.beneficiarios.application.command.AtualizarBeneficiarioCommand;
import br.com.ekan.beneficiarios.application.command.CriarBeneficiarioCommand;
import br.com.ekan.beneficiarios.application.command.DocumentoCommand;
import br.com.ekan.beneficiarios.application.dto.BeneficiarioResult;
import br.com.ekan.beneficiarios.application.service.BeneficiarioService;
import br.com.ekan.beneficiarios.domain.beneficiario.BeneficiarioNaoEncontradoException;
import br.com.ekan.beneficiarios.domain.beneficiario.TipoDocumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class BeneficiarioIntegrationTest {

    @Autowired
    private BeneficiarioService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void limparBase() {
        jdbcTemplate.update("DELETE FROM documentos");
        jdbcTemplate.update("DELETE FROM beneficiarios");
    }

    private CriarBeneficiarioCommand criarCommand(String nome, String telefone,
                                                  List<DocumentoCommand> documentos) {
        return new CriarBeneficiarioCommand(nome, telefone, LocalDate.of(1990, 5, 20), documentos);
    }

    private CriarBeneficiarioCommand criarCommand(String nome) {
        return criarCommand(nome, "(11) 99999-9999", List.of(
                new DocumentoCommand(TipoDocumento.CPF, "123.456.789-00"),
                new DocumentoCommand(TipoDocumento.RG, "12.345.678-9")
        ));
    }

    private int contarDocumentos() {
        Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM documentos", Integer.class);
        return total == null ? 0 : total;
    }

    @Test
    void deveExecutarFluxoCompletoDeCrud() {
        BeneficiarioResult criado = service.criar(criarCommand("Maria da Silva"));

        assertThat(criado.id()).isNotNull();
        assertThat(criado.nome()).isEqualTo("Maria da Silva");
        assertThat(criado.telefone()).isEqualTo("11999999999");
        assertThat(criado.documentos()).hasSize(2);
        assertThat(criado.dataInclusao()).isNotNull();
        assertThat(criado.dataAtualizacao()).isNotNull();

        BeneficiarioResult buscado = service.buscarPorId(criado.id());
        assertThat(buscado.nome()).isEqualTo("Maria da Silva");
        assertThat(buscado.documentos()).hasSize(2);

        assertThat(service.listar()).hasSize(1);
        assertThat(service.listarDocumentos(criado.id())).hasSize(2);

        BeneficiarioResult atualizado = service.atualizar(criado.id(),
                new AtualizarBeneficiarioCommand(
                        "Maria Souza",
                        "(11) 98888-8888",
                        LocalDate.of(1992, 8, 10)
                ));

        assertThat(atualizado.id()).isEqualTo(criado.id());
        assertThat(atualizado.nome()).isEqualTo("Maria Souza");
        assertThat(atualizado.telefone()).isEqualTo("11988888888");
        assertThat(atualizado.documentos()).hasSize(2);
        assertThat(atualizado.dataInclusao()).isEqualToIgnoringNanos(criado.dataInclusao());
        assertThat(atualizado.dataAtualizacao()).isAfterOrEqualTo(criado.dataAtualizacao());

        service.remover(criado.id());

        assertThat(service.listar()).isEmpty();
        assertThatThrownBy(() -> service.buscarPorId(criado.id()))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class);
        assertThat(contarDocumentos()).isZero();
    }

    @Test
    void deveCriarBeneficiarioSemDocumentos() {
        BeneficiarioResult criado = service.criar(criarCommand("Maria", "11999999999", null));

        assertThat(criado.documentos()).isEmpty();
        assertThat(service.listarDocumentos(criado.id())).isEmpty();
    }

    @Test
    void deveListarBeneficiariosEmOrdemAlfabetica() {
        service.criar(criarCommand("Zeca", "11999999999", List.of()));
        service.criar(criarCommand("Ana", "11888888888", List.of()));

        assertThat(service.listar())
                .extracting(BeneficiarioResult::nome)
                .containsExactly("Ana", "Zeca");
    }

    @Test
    void devePersistirTipoDoDocumentoComoTexto() {
        service.criar(criarCommand("Maria"));

        List<String> tipos = jdbcTemplate.queryForList(
                "SELECT tipo_documento FROM documentos ORDER BY tipo_documento", String.class);

        assertThat(tipos).containsExactly("CPF", "RG");
    }

    @Test
    void deveRemoverDocumentosEmCascata() {
        BeneficiarioResult criado = service.criar(criarCommand("Maria"));
        assertThat(contarDocumentos()).isEqualTo(2);

        service.remover(criado.id());

        assertThat(contarDocumentos()).isZero();
    }

    @Test
    void deveLancarExcecaoAoBuscarBeneficiarioInexistente() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> service.buscarPorId(id))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class);
        assertThatThrownBy(() -> service.listarDocumentos(id))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecaoAoAtualizarBeneficiarioInexistente() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> service.atualizar(id,
                new AtualizarBeneficiarioCommand("Maria", "11999999999", LocalDate.of(1990, 5, 20))))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecaoAoRemoverBeneficiarioInexistente() {
        assertThatThrownBy(() -> service.remover(UUID.randomUUID()))
                .isInstanceOf(BeneficiarioNaoEncontradoException.class);
    }

    @Test
    void deveRejeitarTelefoneInvalidoSemPersistir() {
        assertThatThrownBy(() -> service.criar(criarCommand("Maria", "123", List.of())))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(service.listar()).isEmpty();
        assertThat(contarDocumentos()).isZero();
    }

    @Test
    void deveRejeitarDataDeNascimentoFuturaSemPersistir() {
        CriarBeneficiarioCommand command = new CriarBeneficiarioCommand(
                "Maria",
                "11999999999",
                LocalDate.now().plusDays(1),
                List.of()
        );

        assertThatThrownBy(() -> service.criar(command))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(service.listar()).isEmpty();
    }
}
