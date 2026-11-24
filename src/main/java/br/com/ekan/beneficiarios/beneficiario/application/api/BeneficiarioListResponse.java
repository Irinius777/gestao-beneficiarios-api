package br.com.ekan.beneficiarios.beneficiario.application.api;

import br.com.ekan.beneficiarios.beneficiario.domain.Beneficiario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Beneficiario na listagem, sem os documentos")
public record BeneficiarioListResponse(
        @Schema(description = "Identificador do beneficiario")
        UUID idBeneficiario,

        @Schema(description = "Nome completo")
        String nome,

        @Schema(description = "CPF normalizado, somente digitos")
        String cpf,

        @Schema(description = "Telefone normalizado, somente digitos")
        String telefone,

        @Schema(description = "Data de nascimento")
        LocalDate dataNascimento,

        @Schema(description = "Data de inclusao")
        LocalDateTime dataInclusao,

        @Schema(description = "Data da ultima atualizacao")
        LocalDateTime dataAtualizacao
) {

    public BeneficiarioListResponse(Beneficiario beneficiario) {
        this(beneficiario.getIdBeneficiario(),
                beneficiario.getNome(),
                beneficiario.getCpf(),
                beneficiario.getTelefone(),
                beneficiario.getDataNascimento(),
                beneficiario.getDataInclusao(),
                beneficiario.getDataAtualizacao());
    }

    public static List<BeneficiarioListResponse> converte(List<Beneficiario> beneficiarios) {
        return beneficiarios.stream()
                .map(BeneficiarioListResponse::new)
                .toList();
    }
}
