package br.com.ekan.beneficiarios.beneficiario.domain;

import br.com.ekan.beneficiarios.beneficiario.application.api.DocumentoRequest;
import org.junit.jupiter.api.Test;

import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.AGORA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentoTest {

    @Test
    void deveCriarDocumentoComDataDeInclusaoIgualDeAtualizacao() {
        Documento documento = new Documento(new DocumentoRequest(TipoDocumento.RG, "12.345.678-9"), AGORA);

        assertThat(documento.getTipoDocumento()).isEqualTo(TipoDocumento.RG);
        assertThat(documento.getDescricao()).isEqualTo("12.345.678-9");
        assertThat(documento.getDataInclusao()).isEqualTo(AGORA);
        assertThat(documento.getDataAtualizacao()).isEqualTo(AGORA);
    }

    @Test
    void deveRemoverEspacosDaDescricao() {
        Documento documento = new Documento(new DocumentoRequest(TipoDocumento.RG, "  123  "), AGORA);

        assertThat(documento.getDescricao()).isEqualTo("123");
    }

    @Test
    void deveRejeitarDescricaoNula() {
        DocumentoRequest request = new DocumentoRequest(TipoDocumento.RG, null);

        assertThatThrownBy(() -> new Documento(request, AGORA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveRejeitarDescricaoEmBranco() {
        DocumentoRequest request = new DocumentoRequest(TipoDocumento.RG, "  ");

        assertThatThrownBy(() -> new Documento(request, AGORA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveRejeitarDescricaoMaiorQueLimite() {
        DocumentoRequest request = new DocumentoRequest(TipoDocumento.RG, "a".repeat(256));

        assertThatThrownBy(() -> new Documento(request, AGORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("255");
    }

    @Test
    void deveRejeitarTipoNulo() {
        DocumentoRequest request = new DocumentoRequest(null, "123");

        assertThatThrownBy(() -> new Documento(request, AGORA))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
