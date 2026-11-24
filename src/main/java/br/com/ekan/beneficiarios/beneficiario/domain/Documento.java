package br.com.ekan.beneficiarios.beneficiario.domain;

import br.com.ekan.beneficiarios.beneficiario.application.api.DocumentoRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade interna do agregado {@link Beneficiario}. O construtor e
 * package-private: so o agregado raiz cria documentos.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "documentos")
public class Documento {

    private static final int DESCRICAO_MAXIMA = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_documento", updatable = false, nullable = false)
    private UUID idDocumento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 40)
    private TipoDocumento tipoDocumento;

    @Column(nullable = false, length = DESCRICAO_MAXIMA)
    private String descricao;

    @Column(name = "data_inclusao", nullable = false, updatable = false)
    private LocalDateTime dataInclusao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    Documento(DocumentoRequest request, LocalDateTime agora) {
        this.tipoDocumento = validaTipoDocumento(request.tipoDocumento());
        this.descricao = validaDescricao(request.descricao());
        this.dataInclusao = agora;
        this.dataAtualizacao = agora;
    }

    private TipoDocumento validaTipoDocumento(TipoDocumento tipoDocumento) {
        if (tipoDocumento == null) {
            throw new IllegalArgumentException("tipo do documento nao pode ser nulo");
        }
        return tipoDocumento;
    }

    private String validaDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("descricao do documento nao pode ser vazia");
        }
        String descricaoSemEspacos = descricao.trim();
        if (descricaoSemEspacos.length() > DESCRICAO_MAXIMA) {
            throw new IllegalArgumentException("descricao do documento deve ter no maximo 255 caracteres");
        }
        return descricaoSemEspacos;
    }
}
