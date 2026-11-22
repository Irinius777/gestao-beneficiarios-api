package br.com.ekan.beneficiarios.domain.beneficiario;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade filha do agregado {@link Beneficiario}.
 */
public final class Documento {

    private static final int DESCRICAO_MAXIMA = 255;

    private final DocumentoId id;
    private final TipoDocumento tipoDocumento;
    private final String descricao;
    private final LocalDateTime dataInclusao;
    private final LocalDateTime dataAtualizacao;

    public Documento(DocumentoId id,
                     TipoDocumento tipoDocumento,
                     String descricao,
                     LocalDateTime dataInclusao,
                     LocalDateTime dataAtualizacao) {
        this.id = Objects.requireNonNull(id, "id do documento nao pode ser nulo");
        this.tipoDocumento = Objects.requireNonNull(tipoDocumento, "tipo do documento nao pode ser nulo");
        this.descricao = validarDescricao(descricao);
        this.dataInclusao = Objects.requireNonNull(dataInclusao, "data de inclusao do documento nao pode ser nula");
        this.dataAtualizacao = Objects.requireNonNull(dataAtualizacao, "data de atualizacao do documento nao pode ser nula");
    }

    public static Documento novo(TipoDocumento tipoDocumento, String descricao, LocalDateTime agora) {
        Objects.requireNonNull(agora, "data/hora de criacao nao pode ser nula");
        return new Documento(DocumentoId.novo(), tipoDocumento, descricao, agora, agora);
    }

    public Documento atualizar(TipoDocumento tipoDocumento, String descricao, LocalDateTime agora) {
        Objects.requireNonNull(agora, "data/hora de atualizacao nao pode ser nula");
        return new Documento(id, tipoDocumento, descricao, dataInclusao, agora);
    }

    private static String validarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("descricao do documento nao pode ser vazia");
        }
        String normalizada = descricao.trim();
        if (normalizada.length() > DESCRICAO_MAXIMA) {
            throw new IllegalArgumentException("descricao do documento deve ter no maximo 255 caracteres");
        }
        return normalizada;
    }

    public DocumentoId getId() {
        return id;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getDataInclusao() {
        return dataInclusao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Documento documento)) {
            return false;
        }
        return Objects.equals(id, documento.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
