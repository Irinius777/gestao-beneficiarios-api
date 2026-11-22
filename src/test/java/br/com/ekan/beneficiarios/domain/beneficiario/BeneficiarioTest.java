package br.com.ekan.beneficiarios.domain.beneficiario;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeneficiarioTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 1, 15, 10, 0);
    private static final LocalDate NASCIMENTO = LocalDate.of(1990, 5, 20);

    private List<Documento> documentos() {
        return List.of(
                Documento.novo(TipoDocumento.CPF, "123.456.789-00", AGORA),
                Documento.novo(TipoDocumento.RG, "12.345.678-9", AGORA)
        );
    }

    @Test
    void deveCriarBeneficiarioComDocumentos() {
        Beneficiario beneficiario = Beneficiario.novo(
                "Maria da Silva",
                new Telefone("(11) 99999-9999"),
                NASCIMENTO,
                documentos(),
                AGORA
        );

        assertThat(beneficiario.getId()).isNotNull();
        assertThat(beneficiario.getNome()).isEqualTo("Maria da Silva");
        assertThat(beneficiario.getTelefone().value()).isEqualTo("11999999999");
        assertThat(beneficiario.getDataNascimento()).isEqualTo(NASCIMENTO);
        assertThat(beneficiario.getDataInclusao()).isEqualTo(AGORA);
        assertThat(beneficiario.getDataAtualizacao()).isEqualTo(AGORA);
        assertThat(beneficiario.getDocumentos()).hasSize(2);
    }

    @Test
    void deveCriarBeneficiarioSemDocumentos() {
        Beneficiario beneficiario = Beneficiario.novo(
                "Maria",
                new Telefone("11999999999"),
                NASCIMENTO,
                List.of(),
                AGORA
        );

        assertThat(beneficiario.getDocumentos()).isEmpty();
    }

    @Test
    void deveRemoverEspacosDoNome() {
        Beneficiario beneficiario = Beneficiario.novo(
                "  Maria da Silva  ",
                new Telefone("11999999999"),
                NASCIMENTO,
                List.of(),
                AGORA
        );

        assertThat(beneficiario.getNome()).isEqualTo("Maria da Silva");
    }

    @Test
    void deveRejeitarNomeNulo() {
        assertThatThrownBy(() -> Beneficiario.novo(
                null, new Telefone("11999999999"), NASCIMENTO, List.of(), AGORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome");
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        assertThatThrownBy(() -> Beneficiario.novo(
                "   ", new Telefone("11999999999"), NASCIMENTO, List.of(), AGORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome");
    }

    @Test
    void deveRejeitarNomeMaiorQueLimite() {
        assertThatThrownBy(() -> Beneficiario.novo(
                "x".repeat(151), new Telefone("11999999999"), NASCIMENTO, List.of(), AGORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("150");
    }

    @Test
    void deveRejeitarDataDeNascimentoFutura() {
        LocalDate futura = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> Beneficiario.novo(
                "Maria", new Telefone("11999999999"), futura, List.of(), AGORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("futura");
    }

    @Test
    void deveRejeitarDataDeNascimentoNula() {
        assertThatThrownBy(() -> Beneficiario.novo(
                "Maria", new Telefone("11999999999"), null, List.of(), AGORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("data de nascimento");
    }

    @Test
    void deveRejeitarListaDeDocumentosNula() {
        assertThatThrownBy(() -> Beneficiario.novo(
                "Maria", new Telefone("11999999999"), NASCIMENTO, null, AGORA))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("documentos");
    }

    @Test
    void deveProtegerListaDeDocumentosContraAlteracaoExterna() {
        Beneficiario beneficiario = Beneficiario.novo(
                "Maria", new Telefone("11999999999"), NASCIMENTO, documentos(), AGORA);

        assertThatThrownBy(() -> beneficiario.getDocumentos()
                .add(Documento.novo(TipoDocumento.CNH, "123", AGORA)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void deveAtualizarDadosPreservandoIdDataDeInclusaoEDocumentos() {
        Beneficiario original = Beneficiario.novo(
                "Maria da Silva",
                new Telefone("(11) 99999-9999"),
                NASCIMENTO,
                documentos(),
                AGORA
        );
        LocalDate novoNascimento = LocalDate.of(1992, 8, 10);
        LocalDateTime depois = AGORA.plusDays(2);

        Beneficiario atualizado = original.atualizarDados(
                "Maria Souza",
                new Telefone("(11) 98888-8888"),
                novoNascimento,
                depois
        );

        assertThat(atualizado.getId()).isEqualTo(original.getId());
        assertThat(atualizado.getNome()).isEqualTo("Maria Souza");
        assertThat(atualizado.getTelefone().value()).isEqualTo("11988888888");
        assertThat(atualizado.getDataNascimento()).isEqualTo(novoNascimento);
        assertThat(atualizado.getDataInclusao()).isEqualTo(AGORA);
        assertThat(atualizado.getDataAtualizacao()).isEqualTo(depois);
        assertThat(atualizado.getDocumentos()).isEqualTo(original.getDocumentos());
        assertThat(original.getNome()).isEqualTo("Maria da Silva");
    }

    @Test
    void deveCompararBeneficiariosPeloId() {
        Beneficiario original = Beneficiario.novo(
                "Maria", new Telefone("11999999999"), NASCIMENTO, List.of(), AGORA);
        Beneficiario mesmoId = new Beneficiario(
                original.getId(), "Outra", new Telefone("11888888888"), NASCIMENTO,
                AGORA, AGORA, List.of());

        assertThat(original).isEqualTo(mesmoId);
        assertThat(original).hasSameHashCodeAs(mesmoId);
    }
}
