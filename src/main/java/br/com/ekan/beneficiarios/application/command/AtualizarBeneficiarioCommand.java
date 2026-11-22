package br.com.ekan.beneficiarios.application.command;

import java.time.LocalDate;

public record AtualizarBeneficiarioCommand(String nome,
                                           String telefone,
                                           LocalDate dataNascimento) {
}
