package br.com.ekan.beneficiarios.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BeneficiarioResult(UUID id,
                                 String nome,
                                 String telefone,
                                 LocalDate dataNascimento,
                                 LocalDateTime dataInclusao,
                                 LocalDateTime dataAtualizacao,
                                 List<DocumentoResult> documentos) {
}
