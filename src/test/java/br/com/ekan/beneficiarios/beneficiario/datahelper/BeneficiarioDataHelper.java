package br.com.ekan.beneficiarios.beneficiario.datahelper;

import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioAlteracaoRequest;
import br.com.ekan.beneficiarios.beneficiario.application.api.BeneficiarioNovoRequest;
import br.com.ekan.beneficiarios.beneficiario.application.api.DocumentoRequest;
import br.com.ekan.beneficiarios.beneficiario.domain.Beneficiario;
import br.com.ekan.beneficiarios.beneficiario.domain.TipoDocumento;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class BeneficiarioDataHelper {

    public static final String CPF = "529.982.247-25";
    public static final String CPF_SEM_MASCARA = "52998224725";
    public static final String OUTRO_CPF = "111.444.777-35";
    public static final String TELEFONE = "(11) 99999-9999";
    public static final LocalDate NASCIMENTO = LocalDate.of(1990, 5, 20);
    public static final LocalDateTime AGORA = LocalDateTime.of(2026, 1, 15, 10, 0);

    private BeneficiarioDataHelper() {
    }

    public static BeneficiarioNovoRequest criaBeneficiarioNovoRequest() {
        return criaBeneficiarioNovoRequest("Maria da Silva", CPF, criaDocumentosRequest());
    }

    public static BeneficiarioNovoRequest criaBeneficiarioNovoRequest(String nome, String cpf,
                                                                      List<DocumentoRequest> documentos) {
        return new BeneficiarioNovoRequest(nome, cpf, TELEFONE, NASCIMENTO, documentos);
    }

    public static BeneficiarioNovoRequest criaBeneficiarioNovoRequestComNascimento(LocalDate dataNascimento) {
        return new BeneficiarioNovoRequest("Maria da Silva", CPF, TELEFONE, dataNascimento, List.of());
    }

    public static List<DocumentoRequest> criaDocumentosRequest() {
        return List.of(
                new DocumentoRequest(TipoDocumento.RG, "12.345.678-9"),
                new DocumentoRequest(TipoDocumento.CNH, "12345678900")
        );
    }

    public static BeneficiarioAlteracaoRequest criaBeneficiarioAlteracaoRequest() {
        return new BeneficiarioAlteracaoRequest("Maria Souza", "(11) 98888-8888", LocalDate.of(1992, 8, 10));
    }

    public static Beneficiario criaBeneficiario() {
        return criaBeneficiario(UUID.randomUUID());
    }

    public static Beneficiario criaBeneficiario(UUID idBeneficiario) {
        Beneficiario beneficiario = new Beneficiario(criaBeneficiarioNovoRequest(), AGORA);
        ReflectionTestUtils.setField(beneficiario, "idBeneficiario", idBeneficiario);
        beneficiario.getDocumentos()
                .forEach(documento -> ReflectionTestUtils.setField(documento, "idDocumento", UUID.randomUUID()));
        return beneficiario;
    }
}
