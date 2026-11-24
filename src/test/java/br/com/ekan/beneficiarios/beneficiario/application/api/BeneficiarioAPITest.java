package br.com.ekan.beneficiarios.beneficiario.application.api;

import br.com.ekan.beneficiarios.beneficiario.application.service.BeneficiarioService;
import br.com.ekan.beneficiarios.beneficiario.domain.Beneficiario;
import br.com.ekan.beneficiarios.beneficiario.domain.BeneficiarioNaoEncontradoException;
import br.com.ekan.beneficiarios.beneficiario.domain.CpfJaCadastradoException;
import br.com.ekan.beneficiarios.beneficiario.domain.TipoDocumento;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static br.com.ekan.beneficiarios.beneficiario.datahelper.BeneficiarioDataHelper.criaBeneficiario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BeneficiarioAPI.class)
class BeneficiarioAPITest {

    private static final String URL = "/beneficiario";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BeneficiarioService beneficiarioService;

    private static final String JSON_VALIDO = """
            {
              "nome": "Maria da Silva",
              "cpf": "529.982.247-25",
              "telefone": "(11) 99999-9999",
              "dataNascimento": "1990-05-20",
              "documentos": [
                { "tipoDocumento": "RG", "descricao": "12.345.678-9" },
                { "tipoDocumento": "CNH", "descricao": "12345678900" }
              ]
            }
            """;

    private static final String JSON_ALTERACAO_VALIDO = """
            {
              "nome": "Maria Souza",
              "telefone": "(11) 98888-8888",
              "dataNascimento": "1992-08-10"
            }
            """;

    private String jsonComCampo(String campo, String valor) {
        return JSON_VALIDO.replaceFirst("\"" + campo + "\": \"[^\"]*\"", "\"" + campo + "\": " + valor);
    }

    @Test
    void deveCriarBeneficiarioComDocumentos() throws Exception {
        UUID idBeneficiario = UUID.randomUUID();
        when(beneficiarioService.criar(any())).thenReturn(new BeneficiarioCriadoResponse(idBeneficiario));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idBeneficiario").value(idBeneficiario.toString()));

        ArgumentCaptor<BeneficiarioNovoRequest> captor = ArgumentCaptor.forClass(BeneficiarioNovoRequest.class);
        verify(beneficiarioService).criar(captor.capture());
        assertThat(captor.getValue().tiposDocumento()).containsExactly(TipoDocumento.RG, TipoDocumento.CNH);
    }

    @Test
    void deveCriarBeneficiarioSemDocumentos() throws Exception {
        when(beneficiarioService.criar(any())).thenReturn(new BeneficiarioCriadoResponse(UUID.randomUUID()));
        String json = """
                {
                  "nome": "Maria da Silva",
                  "cpf": "52998224725",
                  "telefone": "11999999999",
                  "dataNascimento": "1990-05-20"
                }
                """;

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());

        ArgumentCaptor<BeneficiarioNovoRequest> captor = ArgumentCaptor.forClass(BeneficiarioNovoRequest.class);
        verify(beneficiarioService).criar(captor.capture());
        assertThat(captor.getValue().documentos()).isEmpty();
    }

    @Test
    void deveRetornar400QuandoCamposObrigatoriosFaltarem() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Erro de validacao"))
                .andExpect(jsonPath("$.errors", hasSize(4)));

        verify(beneficiarioService, never()).criar(any());
    }

    @Test
    void deveRetornar400QuandoCpfForInvalido() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(jsonComCampo("cpf", "\"529.982.247-26\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem("cpf: cpf invalido")));
    }

    @Test
    void deveRetornar400QuandoCpfTiverTodosOsDigitosIguais() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(jsonComCampo("cpf", "\"111.111.111-11\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem("cpf: cpf invalido")));
    }

    @Test
    void deveRetornar400QuandoTelefoneForInvalido() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(jsonComCampo("telefone", "\"123\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem("telefone: telefone deve conter DDD e 8 ou 9 digitos")));
    }

    @Test
    void deveRetornar400QuandoDataDeNascimentoForFutura() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(jsonComCampo("dataNascimento", "\"2999-01-01\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem("dataNascimento: dataNascimento deve ser uma data passada")));
    }

    @Test
    void deveRetornar400QuandoTipoDocumentoForInvalido() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO.replace("\"RG\"", "\"PASSAPORTE\"")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400QuandoDescricaoDoDocumentoForVazia() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO.replace("\"12.345.678-9\"", "\"\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem("documentos[0].descricao: descricao e obrigatoria")));
    }

    @Test
    void deveRetornar400QuandoJsonForInvalido() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{ invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Corpo da requisicao invalido ou mal formatado"));
    }

    @Test
    void deveRetornar400QuandoDominioLancarIllegalArgumentException() throws Exception {
        when(beneficiarioService.criar(any())).thenThrow(new IllegalArgumentException("regra violada"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("regra violada"));
    }

    @Test
    void deveRetornar409QuandoCpfJaEstiverCadastrado() throws Exception {
        when(beneficiarioService.criar(any())).thenThrow(new CpfJaCadastradoException());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("CPF ja cadastrado"));
    }

    @Test
    void deveListarBeneficiarios() throws Exception {
        Beneficiario beneficiario = criaBeneficiario();
        when(beneficiarioService.listar())
                .thenReturn(BeneficiarioListResponse.converte(List.of(beneficiario)));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idBeneficiario").value(beneficiario.getIdBeneficiario().toString()))
                .andExpect(jsonPath("$[0].documentos").doesNotExist());
    }

    @Test
    void deveBuscarBeneficiarioPorId() throws Exception {
        Beneficiario beneficiario = criaBeneficiario();
        when(beneficiarioService.buscarPorId(beneficiario.getIdBeneficiario()))
                .thenReturn(new BeneficiarioDetalhadoResponse(beneficiario));

        mockMvc.perform(get(URL + "/{id}", beneficiario.getIdBeneficiario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria da Silva"))
                .andExpect(jsonPath("$.cpf").value("52998224725"))
                .andExpect(jsonPath("$.telefone").value("11999999999"))
                .andExpect(jsonPath("$.dataNascimento").value("1990-05-20"))
                .andExpect(jsonPath("$.documentos", hasSize(2)))
                .andExpect(jsonPath("$.documentos[0].tipoDocumento").value("RG"));
    }

    @Test
    void deveRetornar404QuandoBeneficiarioNaoExistir() throws Exception {
        UUID idBeneficiario = UUID.randomUUID();
        when(beneficiarioService.buscarPorId(idBeneficiario))
                .thenThrow(new BeneficiarioNaoEncontradoException(idBeneficiario));

        mockMvc.perform(get(URL + "/{id}", idBeneficiario))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBuscarBeneficiarioPorCpf() throws Exception {
        Beneficiario beneficiario = criaBeneficiario();
        when(beneficiarioService.buscarPorCpf("529.982.247-25"))
                .thenReturn(new BeneficiarioDetalhadoResponse(beneficiario));

        mockMvc.perform(get(URL + "/cpf/{cpf}", "529.982.247-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("52998224725"));
    }

    @Test
    void deveRetornar400AoBuscarCpfInvalido() throws Exception {
        mockMvc.perform(get(URL + "/cpf/{cpf}", "12345678900"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem("cpf invalido")));

        verify(beneficiarioService, never()).buscarPorCpf(anyString());
    }

    @Test
    void deveRetornar404AoBuscarCpfInexistente() throws Exception {
        when(beneficiarioService.buscarPorCpf(anyString()))
                .thenThrow(new BeneficiarioNaoEncontradoException("52998224725"));

        mockMvc.perform(get(URL + "/cpf/{cpf}", "52998224725"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarDocumentosDoBeneficiario() throws Exception {
        Beneficiario beneficiario = criaBeneficiario();
        when(beneficiarioService.listarDocumentos(beneficiario.getIdBeneficiario()))
                .thenReturn(DocumentoResponse.converte(beneficiario.getDocumentos()));

        mockMvc.perform(get(URL + "/{id}/documentos", beneficiario.getIdBeneficiario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].tipoDocumento").value("CNH"));
    }

    @Test
    void deveAtualizarBeneficiario() throws Exception {
        Beneficiario beneficiario = criaBeneficiario();
        when(beneficiarioService.atualizar(eq(beneficiario.getIdBeneficiario()), any()))
                .thenReturn(new BeneficiarioDetalhadoResponse(beneficiario));

        mockMvc.perform(put(URL + "/{id}", beneficiario.getIdBeneficiario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_ALTERACAO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idBeneficiario").value(beneficiario.getIdBeneficiario().toString()));
    }

    @Test
    void deveRetornar400AoAtualizarComDadosInvalidos() throws Exception {
        mockMvc.perform(put(URL + "/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"nome\": \"\", \"telefone\": \"abc\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(3)));

        verify(beneficiarioService, never()).atualizar(any(), any());
    }

    @Test
    void deveRemoverBeneficiario() throws Exception {
        UUID idBeneficiario = UUID.randomUUID();

        mockMvc.perform(delete(URL + "/{id}", idBeneficiario))
                .andExpect(status().isNoContent());

        verify(beneficiarioService).remover(idBeneficiario);
    }

    @Test
    void deveRetornar404AoRemoverBeneficiarioInexistente() throws Exception {
        UUID idBeneficiario = UUID.randomUUID();
        doThrow(new BeneficiarioNaoEncontradoException(idBeneficiario))
                .when(beneficiarioService).remover(idBeneficiario);

        mockMvc.perform(delete(URL + "/{id}", idBeneficiario))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400QuandoIdForInvalido() throws Exception {
        mockMvc.perform(get(URL + "/{id}", "nao-e-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar404ParaRecursoInexistente() throws Exception {
        mockMvc.perform(get("/rota-inexistente"))
                .andExpect(status().isNotFound());
    }
}
