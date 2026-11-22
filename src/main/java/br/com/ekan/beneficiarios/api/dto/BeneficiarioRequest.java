package br.com.ekan.beneficiarios.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Dados para cadastro de um beneficiario e seus documentos")
public record BeneficiarioRequest(
        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 150, message = "nome deve ter no maximo 150 caracteres")
        @Schema(description = "Nome completo do beneficiario", example = "Maria da Silva")
        String nome,

        @NotBlank(message = "telefone e obrigatorio")
        @Size(max = 20, message = "telefone deve ter no maximo 20 caracteres")
        @Schema(description = "Telefone com DDD", example = "(11) 99999-9999")
        String telefone,

        @NotNull(message = "dataNascimento e obrigatoria")
        @Past(message = "dataNascimento deve ser uma data passada")
        @Schema(description = "Data de nascimento", example = "1990-05-20")
        LocalDate dataNascimento,

        @Schema(description = "Documentos do beneficiario")
        List<@Valid DocumentoRequest> documentos
) {
}
