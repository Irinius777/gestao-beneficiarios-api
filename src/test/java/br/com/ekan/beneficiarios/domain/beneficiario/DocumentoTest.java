package br.com.ekan.beneficiarios.domain.beneficiario;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentoTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 1, 15, 10, 0);

    @Test
    void deveCriarDocumentoComDataDeInclusaoIgualDeAtualizacao() {
        Documento documento = Documento.novo(TipoDocumento.RG, "123.456.789-00", AGORA);

        assertThat(documento.getId()).isNotNull();
        assertThat(documento.getTipoDocumento()).isEqualTo(TipoDocumento.RG);
        assertThat(documento.getDescricao()).isEqualTo("123.456.789-00");
        assertThat(documento.getDataInclusao()).isEqualTo(AGORA);
        assertThat(documento.getDataAtualizacao()).isEqualTo(AGORA);
    }

    @Test
    void deveRemoverEspacosDaDescricao() {
        Documento documento = Documento.novo(TipoDocumento.RG, "  12.345.678-9  ", AGORA);

        assertThat(documento.getDescricao()).isEqualTo("12.345.678-9");
    }

    @Test
    void deveRejeitarDescricaoNula() {
        assertThatThrownBy(() -> Documento.novo(TipoDocumento.RG, null, AGORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descricao");
    }

    @Test
    void deveRejeitarDescricaoEmBranco() {
        assertThatThrownBy(() -> Documento.novo(TipoDocumento.RG, "   ", AGORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descricao");
    }

    @Test
    void deveRejeitarDescricaoMaiorQueLimite() {
        String descricao = "x".repeat(256);

        assertThatThrownBy(() -> Documento.novo(TipoDocumento.OUTRO, descricao, AGORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("255");
    }

    @Test
    void deveRejeitarTipoNulo() {
        assertThatThrownBy(() -> Documento.novo(null, "descricao", AGORA))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("tipo");
    }

    @Test
    void deveRejeitarDataDeCriacaoNula() {
        assertThatThrownBy(() -> Documento.novo(TipoDocumento.RG, "123", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("data/hora");
    }

    @Test
    void deveAtualizarDocumentoPreservandoIdEDataDeInclusao() {
        Documento original = Documento.novo(TipoDocumento.RG, "123", AGORA);
        LocalDateTime depois = AGORA.plusDays(1);

        Documento atualizado = original.atualizar(TipoDocumento.RG, "456", depois);

        assertThat(atualizado.getId()).isEqualTo(original.getId());
        assertThat(atualizado.getDataInclusao()).isEqualTo(AGORA);
        assertThat(atualizado.getDataAtualizacao()).isEqualTo(depois);
        assertThat(atualizado.getDescricao()).isEqualTo("456");
    }

    @Test
    void deveCompararDocumentosPeloId() {
        Documento original = Documento.novo(TipoDocumento.RG, "123", AGORA);
        Documento mesmoId = new Documento(original.getId(), TipoDocumento.RG, "outro",
                AGORA, AGORA);

        assertThat(original).isEqualTo(mesmoId);
        assertThat(original).hasSameHashCodeAs(mesmoId);
    }
}
