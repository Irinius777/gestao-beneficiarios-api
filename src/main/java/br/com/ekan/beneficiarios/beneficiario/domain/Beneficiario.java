package br.com.ekan.beneficiarios.beneficiario.domain;

import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioAlteracaoRequest;
import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioNovoRequest;
import br.com.ekan.beneficiarios.utils.FormatacaoUtils;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Raiz do agregado de beneficiario. Os documentos pertencem ao agregado
 * e so sao criados por ele.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "beneficiarios",
        uniqueConstraints = @UniqueConstraint(name = "uk_beneficiarios_cpf", columnNames = "cpf"))
public class Beneficiario {

    private static final int NOME_MAXIMO = 150;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_beneficiario", updatable = false, nullable = false)
    private UUID idBeneficiario;

    @Column(nullable = false, length = NOME_MAXIMO)
    private String nome;

    @Column(nullable = false, length = 11, updatable = false)
    private String cpf;

    @Column(nullable = false, length = 11)
    private String telefone;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "data_inclusao", nullable = false, updatable = false)
    private LocalDateTime dataInclusao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_beneficiario", nullable = false)
    private List<Documento> documentos = new ArrayList<>();

    public Beneficiario(BeneficiarioNovoRequest request, LocalDateTime agora) {
        this.nome = validaNome(request.nome());
        this.cpf = FormatacaoUtils.removeMascara(request.cpf());
        this.telefone = FormatacaoUtils.removeMascara(request.telefone());
        this.dataNascimento = validaDataNascimento(request.dataNascimento(), agora);
        this.dataInclusao = agora;
        this.dataAtualizacao = agora;
        request.documentos().forEach(documento -> this.documentos.add(new Documento(documento, agora)));
    }

    public void altera(BeneficiarioAlteracaoRequest request, LocalDateTime agora) {
        this.nome = validaNome(request.nome());
        this.telefone = FormatacaoUtils.removeMascara(request.telefone());
        this.dataNascimento = validaDataNascimento(request.dataNascimento(), agora);
        this.dataAtualizacao = agora;
    }

    public List<Documento> getDocumentos() {
        return Collections.unmodifiableList(documentos);
    }

    private String validaNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome do beneficiario nao pode ser vazio");
        }
        String nomeSemEspacos = nome.trim();
        if (nomeSemEspacos.length() > NOME_MAXIMO) {
            throw new IllegalArgumentException("nome do beneficiario deve ter no maximo 150 caracteres");
        }
        return nomeSemEspacos;
    }

    private LocalDate validaDataNascimento(LocalDate dataNascimento, LocalDateTime agora) {
        if (dataNascimento == null) {
            throw new IllegalArgumentException("data de nascimento nao pode ser nula");
        }
        if (dataNascimento.isAfter(agora.toLocalDate())) {
            throw new IllegalArgumentException("data de nascimento nao pode ser futura");
        }
        return dataNascimento;
    }
}
