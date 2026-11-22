package br.com.ekan.beneficiarios.api;

import br.com.ekan.beneficiarios.api.dto.BeneficiarioRequest;
import br.com.ekan.beneficiarios.api.dto.BeneficiarioResponse;
import br.com.ekan.beneficiarios.api.dto.BeneficiarioUpdateRequest;
import br.com.ekan.beneficiarios.api.dto.DocumentoResponse;
import br.com.ekan.beneficiarios.api.mapper.BeneficiarioWebMapper;
import br.com.ekan.beneficiarios.application.dto.BeneficiarioResult;
import br.com.ekan.beneficiarios.application.service.BeneficiarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/beneficiarios")
@RequiredArgsConstructor
@Tag(name = "Beneficiarios", description = "Cadastro de beneficiarios e seus documentos")
public class BeneficiarioController {

    private final BeneficiarioService service;
    private final BeneficiarioWebMapper mapper;

    @PostMapping
    @Operation(summary = "Cadastrar beneficiario com seus documentos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Beneficiario cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    public ResponseEntity<BeneficiarioResponse> criar(@Valid @RequestBody BeneficiarioRequest request) {
        BeneficiarioResult result = service.criar(mapper.toCommand(request));
        BeneficiarioResponse response = mapper.toResponse(result);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os beneficiarios")
    @ApiResponse(responseCode = "200", description = "Lista de beneficiarios")
    public List<BeneficiarioResponse> listar() {
        return mapper.toResponse(service.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar beneficiario por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Beneficiario encontrado"),
            @ApiResponse(responseCode = "404", description = "Beneficiario nao encontrado")
    })
    public BeneficiarioResponse buscarPorId(@PathVariable UUID id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @GetMapping("/{id}/documentos")
    @Operation(summary = "Listar todos os documentos de um beneficiario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documentos do beneficiario"),
            @ApiResponse(responseCode = "404", description = "Beneficiario nao encontrado")
    })
    public List<DocumentoResponse> listarDocumentos(@PathVariable UUID id) {
        return service.listarDocumentos(id).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar os dados cadastrais do beneficiario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Beneficiario atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "404", description = "Beneficiario nao encontrado")
    })
    public BeneficiarioResponse atualizar(@PathVariable UUID id,
                                          @Valid @RequestBody BeneficiarioUpdateRequest request) {
        return mapper.toResponse(service.atualizar(id, mapper.toCommand(request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover um beneficiario e seus documentos")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Beneficiario removido"),
            @ApiResponse(responseCode = "404", description = "Beneficiario nao encontrado")
    })
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
