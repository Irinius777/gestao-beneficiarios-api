package br.com.ekan.beneficiarios.beneficiario.domain;

import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioAlteracaoRequest;
import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioNovoRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.AGORA;
import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.CPF;
import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.CPF_SEM_MASCARA;
import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.NASCIMENTO;
import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.TELEFONE;
import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.criaBeneficiarioAlteracaoRequest;
import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.criaBeneficiarioNovoRequest;
import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.criaBeneficiarioNovoRequestComNascimento;
import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.criaDocumentosRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeneficiarioTest {

    @Test
    void deveCriarBeneficiarioComDocumentos() {
        Beneficiario beneficiario = new Beneficiario(criaBeneficiarioNovoRequest(), AGORA);

        assertThat(beneficiario.getNome()).isEqualTo("Maria da Silva");
        assertThat(beneficiario.getDataNascimento()).isEqualTo(NASCIMENTO);
        assertThat(beneficiario.getDataInclusao()).isEqualTo(AGORA);
        assertThat(beneficiario.getDataAtualizacao()).isEqualTo(AGORA);
        assertThat(beneficiario.getDocumentos())
                .extracting(Documento::getTipoDocumento)
                .containsExactly(TipoDocumento.RG, TipoDocumento.CNH);
    }

    @Test
    void deveCriarBeneficiarioSemDocumentosQuandoListaForNula() {
        Beneficiario beneficiario = new Beneficiario(criaBeneficiarioNovoRequest("Maria", CPF, null), AGORA);

        assertThat(beneficiario.getDocumentos()).isEmpty();
    }

    @Test
    void deveRemoverMascaraDoCpfEDoTelefone() {
        Beneficiario beneficiario = new Beneficiario(criaBeneficiarioNovoRequest(), AGORA);

        assertThat(beneficiario.getCpf()).isEqualTo(CPF_SEM_MASCARA);
        assertThat(beneficiario.getTelefone()).isEqualTo("11999999999");
    }

    @Test
    void deveRemoverEspacosDoNome() {
        Beneficiario beneficiario = new Beneficiario(criaBeneficiarioNovoRequest("  Maria  ", CPF, List.of()), AGORA);

        assertThat(beneficiario.getNome()).isEqualTo("Maria");
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        BeneficiarioNovoRequest request = criaBeneficiarioNovoRequest("   ", CPF, List.of());

        assertThatThrownBy(() -> new Beneficiario(request, AGORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome");
    }

    @Test
    void deveRejeitarNomeNulo() {
        BeneficiarioNovoRequest request = criaBeneficiarioNovoRequest(null, CPF, List.of());

        assertThatThrownBy(() -> new Beneficiario(request, AGORA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveRejeitarNomeMaiorQueLimite() {
        BeneficiarioNovoRequest request = criaBeneficiarioNovoRequest("a".repeat(151), CPF, List.of());

        assertThatThrownBy(() -> new Beneficiario(request, AGORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("150");
    }

    @Test
    void deveRejeitarDataDeNascimentoFutura() {
        BeneficiarioNovoRequest request = criaBeneficiarioNovoRequestComNascimento(AGORA.toLocalDate().plusDays(1));

        assertThatThrownBy(() -> new Beneficiario(request, AGORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("futura");
    }

    @Test
    void deveRejeitarDataDeNascimentoNula() {
        BeneficiarioNovoRequest request = criaBeneficiarioNovoRequestComNascimento(null);

        assertThatThrownBy(() -> new Beneficiario(request, AGORA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveProtegerListaDeDocumentosContraAlteracaoExterna() {
        Beneficiario beneficiario = new Beneficiario(criaBeneficiarioNovoRequest(), AGORA);

        assertThatThrownBy(() -> beneficiario.getDocumentos().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void deveAlterarDadosPreservandoCpfDataDeInclusaoEDocumentos() {
        Beneficiario beneficiario = new Beneficiario(criaBeneficiarioNovoRequest(), AGORA);

        beneficiario.altera(criaBeneficiarioAlteracaoRequest(), AGORA.plusDays(1));

        assertThat(beneficiario.getNome()).isEqualTo("Maria Souza");
        assertThat(beneficiario.getTelefone()).isEqualTo("11988888888");
        assertThat(beneficiario.getDataNascimento()).isEqualTo(LocalDate.of(1992, 8, 10));
        assertThat(beneficiario.getDataAtualizacao()).isEqualTo(AGORA.plusDays(1));
        assertThat(beneficiario.getDataInclusao()).isEqualTo(AGORA);
        assertThat(beneficiario.getCpf()).isEqualTo(CPF_SEM_MASCARA);
        assertThat(beneficiario.getDocumentos()).hasSize(2);
    }

    @Test
    void deveRejeitarAlteracaoComNomeEmBranco() {
        Beneficiario beneficiario = new Beneficiario(criaBeneficiarioNovoRequest(), AGORA);
        BeneficiarioAlteracaoRequest request = new BeneficiarioAlteracaoRequest(" ", TELEFONE, NASCIMENTO);

        assertThatThrownBy(() -> beneficiario.altera(request, AGORA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(beneficiario.getNome()).isEqualTo("Maria da Silva");
    }

    @Test
    void deveCriarDocumentosComAsDatasDoBeneficiario() {
        Beneficiario beneficiario = new Beneficiario(
                criaBeneficiarioNovoRequest("Maria", CPF, criaDocumentosRequest()), AGORA);

        assertThat(beneficiario.getDocumentos())
                .allSatisfy(documento -> {
                    assertThat(documento.getDataInclusao()).isEqualTo(AGORA);
                    assertThat(documento.getDataAtualizacao()).isEqualTo(AGORA);
                });
    }
}
