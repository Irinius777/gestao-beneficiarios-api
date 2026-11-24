package br.com.ekan.beneficiarios.beneficiario.application.api;

import br.com.ekan.beneficiarios.beneficiario.application.service.BeneficiarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/beneficiario")
@RequiredArgsConstructor
@Tag(name = "BeneficiarioAPI", description = "Cadastro de beneficiarios e seus documentos")
public class BeneficiarioAPI {

    private final BeneficiarioService beneficiarioService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar beneficiario com seus documentos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Beneficiario cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "409", description = "CPF ja cadastrado")
    })
    public BeneficiarioCriadoResponse criar(@Valid @RequestBody BeneficiarioNovoRequest request) {
        log.info("[request] POST /beneficiario - tiposDocumento={}", request.tiposDocumento());
        return beneficiarioService.criar(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todos os beneficiarios, sem os documentos")
    @ApiResponse(responseCode = "200", description = "Lista de beneficiarios")
    public List<BeneficiarioListResponse> listar() {
        log.info("[request] GET /beneficiario");
        return beneficiarioService.listar();
    }

    @GetMapping("/{idBeneficiario}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar beneficiario por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Beneficiario encontrado"),
            @ApiResponse(responseCode = "404", description = "Beneficiario nao encontrado")
    })
    public BeneficiarioDetalhadoResponse buscarPorId(@PathVariable UUID idBeneficiario) {
        log.info("[request] GET /beneficiario/{}", idBeneficiario);
        return beneficiarioService.buscarPorId(idBeneficiario);
    }

    @GetMapping("/cpf/{cpf}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar beneficiario por CPF, com ou sem mascara")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Beneficiario encontrado"),
            @ApiResponse(responseCode = "400", description = "CPF invalido"),
            @ApiResponse(responseCode = "404", description = "Beneficiario nao encontrado")
    })
    public BeneficiarioDetalhadoResponse buscarPorCpf(@PathVariable @CPF(message = "cpf invalido") String cpf) {
        log.info("[request] GET /beneficiario/cpf/{cpf}");
        return beneficiarioService.buscarPorCpf(cpf);
    }

    @GetMapping("/{idBeneficiario}/documentos")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todos os documentos de um beneficiario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documentos do beneficiario"),
            @ApiResponse(responseCode = "404", description = "Beneficiario nao encontrado")
    })
    public List<DocumentoResponse> listarDocumentos(@PathVariable UUID idBeneficiario) {
        log.info("[request] GET /beneficiario/{}/documentos", idBeneficiario);
        return beneficiarioService.listarDocumentos(idBeneficiario);
    }

    @PutMapping("/{idBeneficiario}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Atualizar os dados cadastrais do beneficiario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Beneficiario atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "404", description = "Beneficiario nao encontrado")
    })
    public BeneficiarioDetalhadoResponse atualizar(@PathVariable UUID idBeneficiario,
                                                   @Valid @RequestBody BeneficiarioAlteracaoRequest request) {
        log.info("[request] PUT /beneficiario/{}", idBeneficiario);
        return beneficiarioService.atualizar(idBeneficiario, request);
    }

    @DeleteMapping("/{idBeneficiario}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover um beneficiario e seus documentos")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Beneficiario removido"),
            @ApiResponse(responseCode = "404", description = "Beneficiario nao encontrado")
    })
    public void remover(@PathVariable UUID idBeneficiario) {
        log.info("[request] DELETE /beneficiario/{}", idBeneficiario);
        beneficiarioService.remover(idBeneficiario);
    }
}
