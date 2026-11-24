package br.com.ekan.beneficiarios.beneficiario.application.api;

import br.com.ekan.beneficiarios.utils.ValidacaoUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Dados cadastrais para atualizacao do beneficiario")
public record BeneficiarioAlteracaoRequest(
        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 150, message = "nome deve ter no maximo 150 caracteres")
        @Schema(description = "Nome completo do beneficiario", example = "Maria da Silva")
        String nome,

        @NotBlank(message = "telefone e obrigatorio")
        @Pattern(regexp = ValidacaoUtils.TELEFONE_REGEX,
                message = "telefone deve conter DDD e 8 ou 9 digitos")
        @Schema(description = "Telefone com DDD, com ou sem mascara", example = "(11) 98888-8888")
        String telefone,

        @NotNull(message = "dataNascimento e obrigatoria")
        @Past(message = "dataNascimento deve ser uma data passada")
        @Schema(description = "Data de nascimento", example = "1990-05-20")
        LocalDate dataNascimento
) {
}
