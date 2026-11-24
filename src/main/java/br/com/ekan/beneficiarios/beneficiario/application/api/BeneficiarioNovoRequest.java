package br.com.ekan.beneficiarios.beneficiario.application.api;

import br.com.ekan.beneficiarios.beneficiario.domain.TipoDocumento;
import br.com.ekan.beneficiarios.utils.ValidacaoUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Dados para cadastro de um beneficiario e seus documentos")
public record BeneficiarioNovoRequest(
        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 150, message = "nome deve ter no maximo 150 caracteres")
        @Schema(description = "Nome completo do beneficiario", example = "Maria da Silva")
        String nome,

        @NotBlank(message = "cpf e obrigatorio")
        @CPF(message = "cpf invalido")
        @Schema(description = "CPF do beneficiario, com ou sem mascara", example = "529.982.247-25")
        String cpf,

        @NotBlank(message = "telefone e obrigatorio")
        @Pattern(regexp = ValidacaoUtils.TELEFONE_REGEX,
                message = "telefone deve conter DDD e 8 ou 9 digitos")
        @Schema(description = "Telefone com DDD, com ou sem mascara", example = "(11) 99999-9999")
        String telefone,

        @NotNull(message = "dataNascimento e obrigatoria")
        @Past(message = "dataNascimento deve ser uma data passada")
        @Schema(description = "Data de nascimento", example = "1990-05-20")
        LocalDate dataNascimento,

        @Schema(description = "Documentos do beneficiario")
        List<@Valid DocumentoRequest> documentos
) {

    public BeneficiarioNovoRequest {
        documentos = documentos == null ? List.of() : documentos;
    }

    public List<TipoDocumento> tiposDocumento() {
        return documentos.stream()
                .map(DocumentoRequest::tipoDocumento)
                .toList();
    }
}
