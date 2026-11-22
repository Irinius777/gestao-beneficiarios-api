package br.com.ekan.beneficiarios.domain.beneficiario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Raiz do agregado de beneficiario. A lista de documentos pertence ao agregado
 * e nao deve ser alterada fora dele.
 */
public final class Beneficiario {

    private static final int NOME_MAXIMO = 150;

    private final BeneficiarioId id;
    private final String nome;
    private final Cpf cpf;
    private final Telefone telefone;
    private final LocalDate dataNascimento;
    private final LocalDateTime dataInclusao;
    private final LocalDateTime dataAtualizacao;
    private final List<Documento> documentos;

    public Beneficiario(BeneficiarioId id,
                        String nome,
                        Cpf cpf,
                        Telefone telefone,
                        LocalDate dataNascimento,
                        LocalDateTime dataInclusao,
                        LocalDateTime dataAtualizacao,
                        List<Documento> documentos) {
        this.id = Objects.requireNonNull(id, "id do beneficiario nao pode ser nulo");
        this.nome = validarNome(nome);
        this.cpf = Objects.requireNonNull(cpf, "cpf nao pode ser nulo");
        this.telefone = Objects.requireNonNull(telefone, "telefone nao pode ser nulo");
        this.dataNascimento = validarDataNascimento(dataNascimento);
        this.dataInclusao = Objects.requireNonNull(dataInclusao, "data de inclusao nao pode ser nula");
        this.dataAtualizacao = Objects.requireNonNull(dataAtualizacao, "data de atualizacao nao pode ser nula");
        this.documentos = List.copyOf(Objects.requireNonNull(documentos, "documentos nao podem ser nulos"));
    }

    public static Beneficiario novo(String nome,
                                    Cpf cpf,
                                    Telefone telefone,
                                    LocalDate dataNascimento,
                                    List<Documento> documentos,
                                    LocalDateTime agora) {
        Objects.requireNonNull(agora, "data/hora de criacao nao pode ser nula");
        return new Beneficiario(BeneficiarioId.novo(), nome, cpf, telefone, dataNascimento, agora, agora, documentos);
    }

    public Beneficiario atualizarDados(String nome,
                                       Telefone telefone,
                                       LocalDate dataNascimento,
                                       LocalDateTime agora) {
        Objects.requireNonNull(agora, "data/hora de atualizacao nao pode ser nula");
        return new Beneficiario(id, nome, cpf, telefone, dataNascimento, dataInclusao, agora, documentos);
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome do beneficiario nao pode ser vazio");
        }
        String normalizado = nome.trim();
        if (normalizado.length() > NOME_MAXIMO) {
            throw new IllegalArgumentException("nome do beneficiario deve ter no maximo 150 caracteres");
        }
        return normalizado;
    }

    private static LocalDate validarDataNascimento(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            throw new IllegalArgumentException("data de nascimento nao pode ser nula");
        }
        if (dataNascimento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("data de nascimento nao pode ser futura");
        }
        return dataNascimento;
    }

    public BeneficiarioId getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Cpf getCpf() {
        return cpf;
    }

    public Telefone getTelefone() {
        return telefone;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public LocalDateTime getDataInclusao() {
        return dataInclusao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public List<Documento> getDocumentos() {
        return documentos;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Beneficiario beneficiario)) {
            return false;
        }
        return Objects.equals(id, beneficiario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
