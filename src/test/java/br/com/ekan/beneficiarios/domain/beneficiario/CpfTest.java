package br.com.ekan.beneficiarios.domain.beneficiario;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfTest {

    @Test
    void deveNormalizarCpfComMascara() {
        assertThat(new Cpf("529.982.247-25").value()).isEqualTo("52998224725");
    }

    @Test
    void deveAceitarCpfJaNormalizado() {
        assertThat(new Cpf("52998224725").value()).isEqualTo("52998224725");
    }

    @Test
    void deveRejeitarCpfNulo() {
        assertThatThrownBy(() -> new Cpf(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vazio");
    }

    @Test
    void deveRejeitarCpfEmBranco() {
        assertThatThrownBy(() -> new Cpf("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vazio");
    }

    @Test
    void deveRejeitarCpfComCaracteresInvalidos() {
        assertThatThrownBy(() -> new Cpf("529.982.247-2a"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("caracteres invalidos");
    }

    @Test
    void deveRejeitarCpfComQuantidadeInvalidaDeDigitos() {
        assertThatThrownBy(() -> new Cpf("123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("11 digitos");
    }

    @Test
    void deveRejeitarCpfComDigitosRepetidos() {
        assertThatThrownBy(() -> new Cpf("11111111111"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalido");
    }

    @Test
    void deveRejeitarCpfComDigitoVerificadorInvalido() {
        assertThatThrownBy(() -> new Cpf("529.982.247-24"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalido");
    }

    @Test
    void deveConsiderarCpfsEquivalentesQuandoNormalizados() {
        assertThat(new Cpf("529.982.247-25"))
                .isEqualTo(new Cpf("52998224725"));
    }
}
