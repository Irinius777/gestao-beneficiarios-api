package br.com.ekan.beneficiarios.domain.beneficiario;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelefoneTest {

    @Test
    void deveNormalizarTelefoneComMascara() {
        assertThat(new Telefone("(11) 99999-9999").value()).isEqualTo("11999999999");
    }

    @Test
    void deveAceitarTelefoneFixoComDezDigitos() {
        assertThat(new Telefone("(11) 3333-4444").value()).isEqualTo("1133334444");
    }

    @Test
    void deveAceitarTelefoneJaNormalizado() {
        assertThat(new Telefone("11988887777").value()).isEqualTo("11988887777");
    }

    @Test
    void deveRejeitarTelefoneNulo() {
        assertThatThrownBy(() -> new Telefone(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vazio");
    }

    @Test
    void deveRejeitarTelefoneEmBranco() {
        assertThatThrownBy(() -> new Telefone("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vazio");
    }

    @Test
    void deveRejeitarTelefoneComCaracteresInvalidos() {
        assertThatThrownBy(() -> new Telefone("11abc99999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("caracteres invalidos");
    }

    @Test
    void deveRejeitarTelefoneComQuantidadeInvalidaDeDigitos() {
        assertThatThrownBy(() -> new Telefone("123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("10 ou 11 digitos");
    }

    @Test
    void deveConsiderarTelefonesEquivalentesQuandoNormalizados() {
        assertThat(new Telefone("(11) 99999-9999"))
                .isEqualTo(new Telefone("11999999999"));
    }
}
