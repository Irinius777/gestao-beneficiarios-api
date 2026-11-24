# Gestão de Beneficiários API

API REST para gerenciamento de beneficiários de um plano de saúde e seus documentos, desenvolvida como solução para o desafio técnico da Ekan.

> **Branch `refactor/padrao-camadas-wakanda`:** esta versão reorganiza o projeto na arquitetura em camadas usada no curso da Wakanda (pacote por feature, domínio como entidade JPA, Request/Response sem mappers). A versão original, hexagonal, continua na `main` para comparação. As diferenças estão resumidas em [Mudanças em relação à main](#mudancas-em-relacao-a-main).

## Tecnologias

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA / Hibernate
- H2 Database (banco relacional embarcado)
- Bean Validation (incluindo `@CPF` do Hibernate Validator)
- Springdoc OpenAPI / Swagger UI
- Lombok
- JUnit 5, Mockito e AssertJ
- JaCoCo
- Maven Wrapper
- GitHub Actions

## Arquitetura

Arquitetura em camadas, com pacotes organizados por feature:

```text
src/main/java/br/com/ekan/beneficiarios
├── beneficiario
│   ├── application
│   │   ├── api          # BeneficiarioAPI (controller) + Requests e Responses
│   │   └── service      # BeneficiarioService, BeneficiarioApplicationService e o port BeneficiarioRepository
│   ├── domain           # Beneficiario e Documento (@Entity), TipoDocumento e excecoes de negocio
│   └── infra            # BeneficiarioInfraRepository + BeneficiarioSpringDataJpaRepository
├── config               # ClockConfig e OpenApiConfig
├── handler              # GlobalExceptionHandler (ProblemDetail)
└── utils                # FormatacaoUtils e ValidacaoUtils
```

### Fluxo entre camadas

```text
BeneficiarioAPI -> BeneficiarioService (BeneficiarioApplicationService) -> BeneficiarioRepository (port)
                                                                                   ^
                                                                                   |
                                                         BeneficiarioInfraRepository -> BeneficiarioSpringDataJpaRepository
```

- **API**: recebe e valida o Request (Bean Validation) e delega ao service. Responde com `@ResponseStatus`.
- **Service**: orquestra o caso de uso, controla a transação e monta o Response a partir do domínio (`new BeneficiarioDetalhadoResponse(beneficiario)`).
- **Domain**: entidades JPA com comportamento. O agregado é criado a partir do Request (`new Beneficiario(request, agora)`) e alterado por métodos de negócio (`altera(request, agora)`), sem setters.
- **Infra**: implementa o port com Spring Data, lança `BeneficiarioNaoEncontradoException` e traduz a violação de CPF único para `CpfJaCadastradoException`.

### Modelo de dominio

```text
Beneficiario 1 ───── * Documento
```

**Beneficiario**

- `idBeneficiario` (UUID gerado pelo JPA)
- `nome`
- `cpf` (validado no Request, guardado só com dígitos e único)
- `telefone` (validado no Request, guardado só com dígitos)
- `dataNascimento`
- `dataInclusao`
- `dataAtualizacao`
- `documentos`

**Documento**

- `idDocumento` (UUID gerado pelo JPA)
- `tipoDocumento` (`RG`, `CNH`, `CERTIDAO_NASCIMENTO`, `COMPROVANTE_RESIDENCIA`, `OUTRO`)
- `descricao`
- `dataInclusao`
- `dataAtualizacao`

`Beneficiario` é a raiz do agregado. `Documento` tem construtor package-private, então só o agregado cria documentos, e a lista exposta por `getDocumentos()` é somente leitura.

O CPF é a chave natural do beneficiário: validado no Request (incluindo os dígitos verificadores), guardado só com dígitos e protegido por uma constraint de unicidade no banco.

## Endpoints

Base URL: `http://localhost:8080/ekan-beneficiarios/api`

| Metodo | Endpoint | Status | Descricao |
|---|---|---|---|
| `POST` | `/beneficiario` | `201` | Cadastra um beneficiario com seus documentos e devolve o id |
| `GET` | `/beneficiario` | `200` | Lista os beneficiarios em ordem alfabetica, sem os documentos |
| `GET` | `/beneficiario/{idBeneficiario}` | `200` | Busca um beneficiario por id, com os documentos |
| `GET` | `/beneficiario/cpf/{cpf}` | `200` | Busca um beneficiario por CPF, com ou sem mascara |
| `GET` | `/beneficiario/{idBeneficiario}/documentos` | `200` | Lista os documentos de um beneficiario |
| `PUT` | `/beneficiario/{idBeneficiario}` | `200` | Atualiza os dados cadastrais do beneficiario |
| `DELETE` | `/beneficiario/{idBeneficiario}` | `204` | Remove o beneficiario e seus documentos |

### Exemplo de cadastro

```http
POST /ekan-beneficiarios/api/beneficiario
Content-Type: application/json
```

```json
{
  "nome": "Maria da Silva",
  "cpf": "529.982.247-25",
  "telefone": "(11) 99999-9999",
  "dataNascimento": "1990-05-20",
  "documentos": [
    {
      "tipoDocumento": "RG",
      "descricao": "12.345.678-9"
    },
    {
      "tipoDocumento": "CNH",
      "descricao": "12345678900"
    }
  ]
}
```

Resposta `201 Created`:

```json
{
  "idBeneficiario": "3c1f0e6a-6d7b-4f1e-9c2a-0b7d5e4f8a11"
}
```

### Exemplo de consulta por id

```http
GET /ekan-beneficiarios/api/beneficiario/3c1f0e6a-6d7b-4f1e-9c2a-0b7d5e4f8a11
```

Resposta `200 OK`:

```json
{
  "idBeneficiario": "3c1f0e6a-6d7b-4f1e-9c2a-0b7d5e4f8a11",
  "nome": "Maria da Silva",
  "cpf": "52998224725",
  "telefone": "11999999999",
  "dataNascimento": "1990-05-20",
  "dataInclusao": "2026-01-15T10:00:00",
  "dataAtualizacao": "2026-01-15T10:00:00",
  "documentos": [
    {
      "idDocumento": "a1b2c3d4-1111-2222-3333-444455556666",
      "tipoDocumento": "RG",
      "descricao": "12.345.678-9",
      "dataInclusao": "2026-01-15T10:00:00",
      "dataAtualizacao": "2026-01-15T10:00:00"
    }
  ]
}
```

### Exemplo de atualizacao

```http
PUT /ekan-beneficiarios/api/beneficiario/{idBeneficiario}
Content-Type: application/json
```

```json
{
  "nome": "Maria Souza",
  "telefone": "(11) 98888-8888",
  "dataNascimento": "1992-08-10"
}
```

A atualização altera apenas nome, telefone e data de nascimento. O CPF e os documentos são preservados.

### Exemplo de consulta por CPF

```http
GET /ekan-beneficiarios/api/beneficiario/cpf/529.982.247-25
```

A mascara é opcional. O endpoint aceita `529.982.247-25` e `52998224725`; um CPF inválido devolve `400`.

### Erros

Os erros seguem o formato `ProblemDetail` do Spring:

```json
{
  "type": "about:blank",
  "title": "Erro de validacao",
  "status": 400,
  "detail": "Um ou mais campos sao invalidos",
  "instance": "/ekan-beneficiarios/api/beneficiario",
  "errors": [
    "nome: nome e obrigatorio",
    "cpf: cpf invalido"
  ]
}
```

Status tratados:

- `400 Bad Request`: dados invalidos, JSON malformado, UUID invalido, CPF invalido ou regra de dominio violada.
- `404 Not Found`: beneficiario inexistente (por id ou por CPF).
- `409 Conflict`: CPF ja cadastrado para outro beneficiario.
- `500 Internal Server Error`: erro inesperado.

## Validacoes

Validações de formato ficam nos Requests (Bean Validation); invariantes do negócio ficam no construtor e nos métodos do domínio.

- `nome`: obrigatorio, no maximo 150 caracteres (espaços nas pontas são removidos).
- `cpf`: obrigatorio e valido (`@CPF`, com ou sem mascara).
- `telefone`: obrigatorio, DDD + 8 ou 9 digitos, com ou sem mascara.
- `dataNascimento`: obrigatoria e nao pode ser futura.
- `documentos`: lista opcional; quando enviada, cada documento exige `tipoDocumento` e `descricao`.
- `descricao` do documento: obrigatoria, no maximo 255 caracteres.
- O CPF é unico no banco. A aplicacao verifica antes de salvar e o banco garante a regra em caso de concorrencia.

## Logs

Os logs seguem o formato `[marcador] mensagem` e registram só informação que agrega valor:

- `[request]`: uma linha por requisição no controller, apenas com dados não sensíveis (ids e tipos de documento; nunca nome, CPF, telefone ou data de nascimento).
- `[beneficiario-criado]`, `[beneficiario-atualizado]`, `[beneficiario-removido]`: resultado das operações de escrita no service.
- `[cpf-duplicado]`: tentativa de cadastro com CPF existente (sem logar o CPF).

## Como executar

### Pre-requisitos

- Java 17 ou superior
- Nenhuma instalacao separada do Maven (o projeto usa Maven Wrapper)

### Rodar a aplicacao

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

A aplicacao sobe em `http://localhost:8080/ekan-beneficiarios/api`.

### Swagger UI

```text
http://localhost:8080/ekan-beneficiarios/api/swagger-ui.html
```

Especificacao OpenAPI:

```text
http://localhost:8080/ekan-beneficiarios/api/v3/api-docs
```

### Console do H2

```text
http://localhost:8080/ekan-beneficiarios/api/h2-console
```

Configuracao:

```text
JDBC URL: jdbc:h2:file:./data/ekan
User Name: sa
Password: (vazio)
```

O banco fica no diretorio `./data`, que nao e versionado. Para zerar os dados, pare a aplicacao e remova a pasta `data`. Ao trocar da `main` para esta branch, remova a pasta: as colunas de id mudaram (`id_beneficiario`, `id_documento`).

### Profile de producao

O profile `prod` desabilita a documentacao Swagger/OpenAPI:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## Testes

Para executar todos os testes:

```bash
./mvnw test
```

Para executar a verificacao completa do build, com relatorio de cobertura do JaCoCo em `target/site/jacoco/index.html`:

```bash
./mvnw verify
```

A suite cobre:

- Regras de dominio de `Beneficiario` e `Documento`.
- Casos de uso do `BeneficiarioApplicationService` com Mockito e `Clock` fixo.
- Contrato HTTP com `@WebMvcTest`: Bean Validation (CPF, telefone, datas), serializacao e tratamento de erros.
- Integracao real com Spring, JPA e H2: persistencia, atualizacao, remocao em cascata, constraint unica de CPF e ordenacao.

Os dados de teste ficam centralizados em `BeneficiarioDataHelper`. Os testes de integracao usam o profile `test` com H2 em memoria.

## Decisoes de projeto

- **Camadas por feature**: tudo o que é de beneficiário fica em `beneficiario/`, separado em `application/api`, `application/service`, `domain` e `infra`.
- **Domínio como entidade JPA**: evita duplicar o modelo (entidade de domínio + entidade de persistência + mappers). As regras continuam no domínio, por meio de construtor e métodos de negócio.
- **Request/Response sem mappers**: o domínio é criado a partir do Request e o Response a partir do domínio, cada um no próprio construtor.
- **Port/Adapter de repositório**: o service depende só de `BeneficiarioRepository`; o Spring Data fica escondido em `infra`.
- **`@Transactional` só onde precisa**: escritas no service e leituras que navegam o agregado (`readOnly = true`). A listagem é uma única consulta e o adapter não abre transações próprias.
- **CPF como chave natural**: validado com `@CPF`, coluna única no banco, consulta dedicada e imutável via `PUT`.
- **Clock injetado**: permite testar datas de inclusao e atualizacao de forma deterministica.
- **H2 em arquivo**: atende ao requisito de banco relacional e mantem os dados entre reinicios. Em testes, o H2 e em memoria.
- **ProblemDetail**: padroniza respostas de erro conforme a especificacao HTTP.
- **Sem autenticacao**: item opcional no desafio; a API foi mantida focada nos requisitos obrigatorios.

## Mudancas em relacao a main

| Tema | `main` (hexagonal) | Esta branch (camadas Wakanda) |
|---|---|---|
| Pacotes | Por camada (`api`, `application`, `domain`, `infrastructure`) | Por feature (`beneficiario/application/api`, `.../service`, `domain`, `infra`) |
| Persistência | `BeneficiarioJpaEntity` + `BeneficiarioPersistenceMapper` | O próprio `Beneficiario` é a `@Entity` |
| Contratos | `Command`, `Result`, `BeneficiarioWebMapper`, `BeneficiarioApplicationMapper` | `BeneficiarioNovoRequest`, `BeneficiarioAlteracaoRequest`, `BeneficiarioCriadoResponse`, `BeneficiarioDetalhadoResponse`, `BeneficiarioListResponse` |
| Port do repositório | Em `domain` | Em `application/service` |
| Nomes | `BeneficiarioController`, `JpaBeneficiarioRepositoryAdapter`, `BeneficiarioJpaRepository` | `BeneficiarioAPI`, `BeneficiarioInfraRepository`, `BeneficiarioSpringDataJpaRepository` |
| Modelo | Imutável, factory `novo()`, IDs tipados, VOs `Cpf`/`Telefone` | Mutável por métodos de negócio, construtor público, `UUID` gerado pelo JPA, CPF/telefone validados no Request |
| Respostas HTTP | `ResponseEntity` + header `Location`; POST devolve o beneficiário completo | `@ResponseStatus`; POST devolve só o `idBeneficiario`; listagem sem documentos |
| URL | `/v1/beneficiarios` | context-path `/ekan-beneficiarios/api` + `/beneficiario` |
| Not found | Port devolve `Optional`, service lança a exceção | Adapter lança a exceção (`buscarPorId` devolve a entidade) |
| Logs | `@Slf4j`, poucos eventos | `@Log4j2`, `[request]` no controller e eventos de escrita no service, sem dados sensíveis |
| Transações | Em todos os métodos do service e do adapter | Só nas escritas e nas leituras que navegam o agregado |
| Testes | Dados inline | `BeneficiarioDataHelper` + JaCoCo |

## Limitacoes e proximos passos

- Substituir H2 por PostgreSQL/MySQL e adicionar Flyway/Liquibase em um cenario de producao.
- Adicionar autenticacao e autorizacao (Spring Security + JWT ou Basic Auth).
- Adicionar paginacao e filtros na listagem de beneficiarios.
- Criar endpoints para atualizar ou remover documentos individualmente.
- Criar um fluxo especifico para correção de CPF, caso o negócio permita alterar a chave natural.

## CI

O workflow em `.github/workflows/ci.yml` executa `./mvnw -B verify` em cada push e pull request para as branches principais, usando Java 17.
