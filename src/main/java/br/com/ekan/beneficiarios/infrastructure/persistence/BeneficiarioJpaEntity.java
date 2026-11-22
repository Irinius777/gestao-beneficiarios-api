package br.com.ekan.beneficiarios.infrastructure.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "beneficiarios")
public class BeneficiarioJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "data_inclusao", nullable = false)
    private LocalDateTime dataInclusao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiario_id", nullable = false)
    private List<DocumentoJpaEntity> documentos = new ArrayList<>();

    protected BeneficiarioJpaEntity() {
    }

    public BeneficiarioJpaEntity(UUID id,
                                 String nome,
                                 String telefone,
                                 LocalDate dataNascimento,
                                 LocalDateTime dataInclusao,
                                 LocalDateTime dataAtualizacao) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.dataNascimento = dataNascimento;
        this.dataInclusao = dataInclusao;
        this.dataAtualizacao = dataAtualizacao;
    }

    public void atualizarDados(String nome,
                               String telefone,
                               LocalDate dataNascimento,
                               LocalDateTime dataAtualizacao) {
        this.nome = nome;
        this.telefone = telefone;
        this.dataNascimento = dataNascimento;
        this.dataAtualizacao = dataAtualizacao;
    }

    public void adicionarDocumento(DocumentoJpaEntity documento) {
        documentos.add(documento);
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getTelefone() {
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

    public List<DocumentoJpaEntity> getDocumentos() {
        return documentos;
    }
}
