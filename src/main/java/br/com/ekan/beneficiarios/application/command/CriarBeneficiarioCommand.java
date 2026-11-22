package br.com.ekan.beneficiarios.application.command;

import java.time.LocalDate;
import java.util.List;

public record CriarBeneficiarioCommand(String nome,
                                       String telefone,
                                       LocalDate dataNascimento,
                                       List<DocumentoCommand> documentos) {
}
