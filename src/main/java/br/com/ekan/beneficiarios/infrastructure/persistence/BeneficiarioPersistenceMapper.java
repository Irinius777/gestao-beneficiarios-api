package br.com.ekan.beneficiarios.infrastructure.persistence;

import br.com.ekan.beneficiarios.domain.beneficiario.Beneficiario;
import br.com.ekan.beneficiarios.domain.beneficiario.BeneficiarioId;
import br.com.ekan.beneficiarios.domain.beneficiario.Cpf;
import br.com.ekan.beneficiarios.domain.beneficiario.Documento;
import br.com.ekan.beneficiarios.domain.beneficiario.DocumentoId;
import br.com.ekan.beneficiarios.domain.beneficiario.Telefone;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BeneficiarioPersistenceMapper {

    public BeneficiarioJpaEntity toNewEntity(Beneficiario beneficiario) {
        BeneficiarioJpaEntity entity = new BeneficiarioJpaEntity(
                beneficiario.getId().value(),
                beneficiario.getNome(),
                beneficiario.getCpf().value(),
                beneficiario.getTelefone().value(),
                beneficiario.getDataNascimento(),
                beneficiario.getDataInclusao(),
                beneficiario.getDataAtualizacao()
        );

        beneficiario.getDocumentos().forEach(documento -> entity.adicionarDocumento(toEntity(documento)));
        return entity;
    }

    public void atualizarDados(BeneficiarioJpaEntity entity, Beneficiario beneficiario) {
        entity.atualizarDados(
                beneficiario.getNome(),
                beneficiario.getTelefone().value(),
                beneficiario.getDataNascimento(),
                beneficiario.getDataAtualizacao()
        );
    }

    public Beneficiario toDomain(BeneficiarioJpaEntity entity) {
        List<Documento> documentos = entity.getDocumentos().stream()
                .map(this::toDomain)
                .toList();

        return new Beneficiario(
                new BeneficiarioId(entity.getId()),
                entity.getNome(),
                new Cpf(entity.getCpf()),
                new Telefone(entity.getTelefone()),
                entity.getDataNascimento(),
                entity.getDataInclusao(),
                entity.getDataAtualizacao(),
                documentos
        );
    }

    private DocumentoJpaEntity toEntity(Documento documento) {
        return new DocumentoJpaEntity(
                documento.getId().value(),
                documento.getTipoDocumento(),
                documento.getDescricao(),
                documento.getDataInclusao(),
                documento.getDataAtualizacao()
        );
    }

    private Documento toDomain(DocumentoJpaEntity entity) {
        return new Documento(
                new DocumentoId(entity.getId()),
                entity.getTipoDocumento(),
                entity.getDescricao(),
                entity.getDataInclusao(),
                entity.getDataAtualizacao()
        );
    }
}
