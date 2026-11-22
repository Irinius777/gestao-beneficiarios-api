package br.com.ekan.beneficiarios.api;

import br.com.ekan.beneficiarios.api.mapper.BeneficiarioWebMapper;
import br.com.ekan.beneficiarios.application.command.CriarBeneficiarioCommand;
import br.com.ekan.beneficiarios.application.dto.BeneficiarioResult;
import br.com.ekan.beneficiarios.application.dto.DocumentoResult;
import br.com.ekan.beneficiarios.application.service.BeneficiarioService;
import br.com.ekan.beneficiarios.domain.beneficiario.BeneficiarioNaoEncontradoException;
import br.com.ekan.beneficiarios.domain.beneficiario.TipoDocumento;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BeneficiarioController.class)
@Import(BeneficiarioWebMapper.class)
class BeneficiarioControllerTest {

    private static final LocalDate NASCIMENTO = LocalDate.of(1990, 5, 20);
    private static final LocalDateTime DATA = LocalDateTime.of(2026, 1, 15, 10, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BeneficiarioService service;

    private final String jsonValido = """
            {
              "nome": "Maria da Silva",
              "telefone": "(11) 99999-9999",
              "dataNascimento": "1990-05-20",
              "documentos": [
                {
                  "tipoDocumento": "CPF",
                  "descricao": "123.456.789-00"
                },
                {
                  "tipoDocumento": "RG",
                  "descricao": "12.345.678-9"
                }
              ]
            }
            """;

    private BeneficiarioResult result(UUID id) {
        DocumentoResult documento = new DocumentoResult(
                UUID.randomUUID(), TipoDocumento.CPF, "123.456.789-00", DATA, DATA);
        return new BeneficiarioResult(
                id, "Maria da Silva", "11999999999", NASCIMENTO, DATA, DATA, List.of(documento));
    }

    @Test
    void deveCriarBeneficiarioComDocumentos() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.criar(any())).thenReturn(result(id));

        mockMvc.perform(post("/v1/beneficiarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Maria da Silva"))
                .andExpect(jsonPath("$.telefone").value("11999999999"))
                .andExpect(jsonPath("$.dataNascimento").value("1990-05-20"))
                .andExpect(jsonPath("$.documentos", hasSize(1)))
                .andExpect(jsonPath("$.documentos[0].tipoDocumento").value("CPF"));
    }

    @Test
    void deveCriarBeneficiarioSemDocumentos() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.criar(any())).thenReturn(result(id));
        String json = """
                {
                  "nome": "Maria da Silva",
                  "telefone": "(11) 99999-9999",
                  "dataNascimento": "1990-05-20"
                }
                """;

        mockMvc.perform(post("/v1/beneficiarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        ArgumentCaptor<CriarBeneficiarioCommand> captor =
                ArgumentCaptor.forClass(CriarBeneficiarioCommand.class);
        verify(service).criar(captor.capture());
        assertThat(captor.getValue().documentos()).isEmpty();
    }

    @Test
    void deveRetornar400QuandoCamposObrigatoriosFaltarem() throws Exception {
        mockMvc.perform(post("/v1/beneficiarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void deveRetornar400QuandoDataDeNascimentoForFutura() throws Exception {
        String json = jsonValido.replace("1990-05-20", LocalDate.now().plusDays(1).toString());

        mockMvc.perform(post("/v1/beneficiarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void deveRetornar400QuandoTipoDocumentoForInvalido() throws Exception {
        String json = jsonValido.replace("CPF", "INVALIDO");

        mockMvc.perform(post("/v1/beneficiarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void deveRetornar400QuandoDescricaoDoDocumentoForVazia() throws Exception {
        String json = jsonValido.replace("123.456.789-00", "   ");

        mockMvc.perform(post("/v1/beneficiarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void deveRetornar400QuandoJsonForInvalido() throws Exception {
        mockMvc.perform(post("/v1/beneficiarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void deveRetornar400QuandoServicoLancarIllegalArgumentException() throws Exception {
        when(service.criar(any())).thenThrow(new IllegalArgumentException("telefone invalido"));

        mockMvc.perform(post("/v1/beneficiarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("telefone invalido"));
    }

    @Test
    void deveListarBeneficiarios() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.listar()).thenReturn(List.of(result(id)));

        mockMvc.perform(get("/v1/beneficiarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id.toString()));
    }

    @Test
    void deveBuscarBeneficiarioPorId() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.buscarPorId(id)).thenReturn(result(id));

        mockMvc.perform(get("/v1/beneficiarios/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.documentos", hasSize(1)));
    }

    @Test
    void deveRetornar404QuandoBeneficiarioNaoExistir() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.buscarPorId(id)).thenThrow(new BeneficiarioNaoEncontradoException(id));

        mockMvc.perform(get("/v1/beneficiarios/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void deveListarDocumentosDoBeneficiario() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.listarDocumentos(id)).thenReturn(result(id).documentos());

        mockMvc.perform(get("/v1/beneficiarios/{id}/documentos", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipoDocumento").value("CPF"));
    }

    @Test
    void deveRetornar404AoListarDocumentosDeBeneficiarioInexistente() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.listarDocumentos(id)).thenThrow(new BeneficiarioNaoEncontradoException(id));

        mockMvc.perform(get("/v1/beneficiarios/{id}/documentos", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarBeneficiario() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.atualizar(eq(id), any())).thenReturn(result(id));
        String json = """
                {
                  "nome": "Maria Souza",
                  "telefone": "(11) 98888-8888",
                  "dataNascimento": "1992-08-10"
                }
                """;

        mockMvc.perform(put("/v1/beneficiarios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(service).atualizar(eq(id), any());
    }

    @Test
    void deveRetornar400AoAtualizarComDadosInvalidos() throws Exception {
        UUID id = UUID.randomUUID();
        String json = """
                {
                  "nome": "",
                  "telefone": "",
                  "dataNascimento": "2999-01-01"
                }
                """;

        mockMvc.perform(put("/v1/beneficiarios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void deveRetornar404AoAtualizarBeneficiarioInexistente() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.atualizar(eq(id), any())).thenThrow(new BeneficiarioNaoEncontradoException(id));
        String json = """
                {
                  "nome": "Maria",
                  "telefone": "11999999999",
                  "dataNascimento": "1990-05-20"
                }
                """;

        mockMvc.perform(put("/v1/beneficiarios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRemoverBeneficiario() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/v1/beneficiarios/{id}", id))
                .andExpect(status().isNoContent());

        verify(service).remover(id);
    }

    @Test
    void deveRetornar404AoRemoverBeneficiarioInexistente() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new BeneficiarioNaoEncontradoException(id)).when(service).remover(id);

        mockMvc.perform(delete("/v1/beneficiarios/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400QuandoIdForInvalido() throws Exception {
        mockMvc.perform(get("/v1/beneficiarios/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void deveRetornar404ParaRecursoInexistente() throws Exception {
        mockMvc.perform(get("/nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Recurso nao encontrado"));
    }
}
