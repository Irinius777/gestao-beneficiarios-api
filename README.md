# Gestão de Beneficiários API

API REST para gerenciamento de beneficiários de um plano de saúde e seus documentos, desenvolvida como solução para o desafio técnico da Ekan.

O projeto segue uma organizacao em camadas DDD, com a camada de apresentacao no padrao MVC, codigo de dominio isolado de detalhes de persistencia e testes automatizados cobrindo dominio, aplicacao, API e integracao com banco relacional.

## Tecnologias

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA / Hibernate
- H2 Database (banco relacional embarcado)
- Bean Validation
- Springdoc OpenAPI / Swagger UI
- Lombok
- JUnit 5, Mockito e AssertJ
- Maven Wrapper
- GitHub Actions

## Arquitetura

O projeto combina MVC na camada web com DDD nas regras de negocio:

```text
src/main/java/br/com/ekan/beneficiarios
├── api                  # Camada MVC: controllers, DTOs HTTP, mapper web e tratamento de erros
│   ├── BeneficiarioController.java
│   ├── dto
│   ├── exception
│   └── mapper
├── application          # Casos de uso, commands, results e mapper de aplicacao
│   ├── command
│   ├── dto
│   ├── mapper
│   └── service
├── domain               # Agregado, entidades, value objects, porta de repositorio e excecoes
│   └── beneficiario
└── infrastructure       # Adaptadores JPA, entidades de persistencia, mappers e configuracoes
    ├── config
    └── persistence
```

### Fluxo de dependencias

```text
api -> application -> domain
                         ^
                         |
                  infrastructure
```

- `domain` nao conhece Spring, JPA ou HTTP.
- `application` orquestra os casos de uso e depende apenas do dominio.
- `infrastructure` implementa a porta `BeneficiarioRepository` com JPA/H2.
- `api` traduz HTTP para comandos da aplicacao e respostas da aplicacao para JSON.

### Modelo de dominio

```text
Beneficiario 1 ───── * Documento
```

**Beneficiario**

- `id` (UUID)
- `nome`
- `cpf` (value object normalizado, validado e único)
- `telefone` (value object normalizado para 10 ou 11 dígitos)
- `dataNascimento`
- `dataInclusao`
- `dataAtualizacao`
- `documentos`

**Documento**

- `id` (UUID)
- `tipoDocumento` (`RG`, `CNH`, `CERTIDAO_NASCIMENTO`, `COMPROVANTE_RESIDENCIA`, `OUTRO`)
- `descricao`
- `dataInclusao`
- `dataAtualizacao`

`Beneficiario` é a raiz do agregado. `Documento` é uma entidade filha e só deve ser alterada por meio do agregado. A lista de documentos é imutável fora do domínio.

O CPF foi promovido a atributo do beneficiário por ser uma chave natural do negócio. Ele é validado no domínio (incluindo os dígitos verificadores), normalizado para apenas dígitos e protegido por uma constraint de unicidade no banco. Por isso, `CPF` não é mais aceito como `TipoDocumento`; os documentos ficam reservados para RG, CNH, certidão, comprovante e outros.

## Endpoints

Base URL: `http://localhost:8080`

| Metodo | Endpoint | Descricao |
|---|---|---|
| `POST` | `/v1/beneficiarios` | Cadastra um beneficiario com seus documentos |
| `GET` | `/v1/beneficiarios` | Lista todos os beneficiarios |
| `GET` | `/v1/beneficiarios/{id}` | Busca um beneficiario por id |
| `GET` | `/v1/beneficiarios/cpf/{cpf}` | Busca um beneficiario por CPF, com ou sem mascara |
| `GET` | `/v1/beneficiarios/{id}/documentos` | Lista os documentos de um beneficiario |
| `PUT` | `/v1/beneficiarios/{id}` | Atualiza os dados cadastrais do beneficiario |
| `DELETE` | `/v1/beneficiarios/{id}` | Remove o beneficiario e seus documentos |

### Exemplo de cadastro

```http
POST /v1/beneficiarios
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
  "id": "3c1f0e6a-6d7b-4f1e-9c2a-0b7d5e4f8a11",
  "nome": "Maria da Silva",
  "cpf": "52998224725",
  "telefone": "11999999999",
  "dataNascimento": "1990-05-20",
  "dataInclusao": "2026-01-15T10:00:00",
  "dataAtualizacao": "2026-01-15T10:00:00",
  "documentos": [
    {
      "id": "a1b2c3d4-1111-2222-3333-444455556666",
      "tipoDocumento": "RG",
      "descricao": "12.345.678-9",
      "dataInclusao": "2026-01-15T10:00:00",
      "dataAtualizacao": "2026-01-15T10:00:00"
    },
    {
      "id": "b2c3d4e5-2222-3333-4444-555566667777",
      "tipoDocumento": "CNH",
      "descricao": "12345678900",
      "dataInclusao": "2026-01-15T10:00:00",
      "dataAtualizacao": "2026-01-15T10:00:00"
    }
  ]
}
```

### Exemplo de atualizacao

```http
PUT /v1/beneficiarios/{id}
Content-Type: application/json
```

```json
{
  "nome": "Maria Souza",
  "telefone": "(11) 98888-8888",
  "dataNascimento": "1992-08-10"
}
```

A atualização altera apenas nome, telefone e data de nascimento. O CPF e os documentos são preservados; os documentos podem ser consultados pelo endpoint `/v1/beneficiarios/{id}/documentos`.

### Exemplo de consulta por CPF

```http
GET /v1/beneficiarios/cpf/529.982.247-25
```

A mascara é opcional. O endpoint aceita `529.982.247-25` e `52998224725`. O CPF devolvido na resposta fica normalizado, apenas com dígitos.

### Erros

Os erros seguem o formato `ProblemDetail` do Spring:

```json
{
  "type": "about:blank",
  "title": "Erro de validacao",
  "status": 400,
  "detail": "Um ou mais campos sao invalidos",
  "instance": "/v1/beneficiarios",
  "errors": [
    "nome: nome e obrigatorio",
    "dataNascimento: dataNascimento deve ser uma data passada"
  ]
}
```

Status tratados:

- `400 Bad Request`: dados invalidos, JSON malformado, UUID invalido, CPF invalido ou regra de dominio violada.
- `404 Not Found`: beneficiario inexistente (por id ou por CPF).
- `409 Conflict`: CPF ja cadastrado para outro beneficiario.
- `500 Internal Server Error`: erro inesperado.

## Validacoes

- `nome`: obrigatorio, no maximo 150 caracteres.
- `cpf`: obrigatorio, 11 digitos apos normalizacao e digitos verificadores validos.
- `telefone`: obrigatorio, deve conter 10 ou 11 digitos.
- `dataNascimento`: obrigatoria e nao pode ser futura.
- `documentos`: lista opcional; quando enviada, cada documento exige `tipoDocumento` e `descricao`.
- `descricao` do documento: obrigatoria, no maximo 255 caracteres.
- O CPF é unico no banco. A aplicacao verifica antes de salvar e o banco garante a regra em caso de concorrencia.

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

A aplicacao sobe em `http://localhost:8080`.

### Swagger UI

```text
http://localhost:8080/swagger-ui.html
```

Especificacao OpenAPI:

```text
http://localhost:8080/v3/api-docs
```

### Console do H2

```text
http://localhost:8080/h2-console
```

Configuracao:

```text
JDBC URL: jdbc:h2:file:./data/ekan
User Name: sa
Password: (vazio)
```

O banco fica no diretorio `./data`, que nao e versionado. Para zerar os dados, pare a aplicacao e remova a pasta `data`.

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

Para executar a verificacao completa do build:

```bash
./mvnw verify
```

A suite cobre:

- Regras de dominio do agregado `Beneficiario`, da entidade `Documento` e dos value objects `Telefone` e `Cpf`.
- Casos de uso da camada de aplicacao com Mockito e `Clock` fixo, incluindo CPF duplicado e consulta por CPF.
- Contrato HTTP com `@WebMvcTest`, validacao, serializacao e tratamento de erros, incluindo `409` para CPF duplicado.
- Integracao real com Spring, JPA e H2, incluindo persistencia, atualizacao, remocao em cascata, constraint unica de CPF e consultas.

Os testes de integracao usam o profile `test` com H2 em memoria, isolado do banco de arquivo da aplicacao.

## Decisoes de projeto

- **DDD pragmatico**: o dominio e isolado de frameworks; a persistencia usa entidades JPA separadas e mappers explicitos.
- **MVC na borda**: controllers recebem DTOs HTTP, traduzem para commands e devolvem responses. As regras nao ficam no controller.
- **Agregado imutavel**: operacoes de atualizacao retornam uma nova instancia de `Beneficiario`, preservando id, CPF, data de inclusao e documentos.
- **CPF como chave natural**: value object validado, coluna unica no banco, consulta dedicada e CPF imutavel via `PUT`.
- **UUID como identificador**: evita dependencia de sequencia do banco e facilita a geracao no dominio.
- **Clock injetado**: permite testar datas de inclusao e atualizacao de forma deterministica.
- **H2 em arquivo**: atende ao requisito de banco relacional e mantem os dados entre reinicios. Em testes, o H2 e em memoria.
- **ProblemDetail**: padroniza respostas de erro conforme a especificacao HTTP.
- **Sem autenticacao**: item opcional no desafio; a API foi mantida focada nos requisitos obrigatorios.

## Limitacoes e proximos passos

- Substituir H2 por PostgreSQL/MySQL e adicionar Flyway/Liquibase em um cenario de producao.
- Adicionar autenticacao e autorizacao (Spring Security + JWT ou Basic Auth).
- Adicionar paginacao e filtros na listagem de beneficiarios.
- Criar endpoints para atualizar ou remover documentos individualmente.
- Criar um fluxo especifico para correção de CPF, caso o negócio permita alterar a chave natural.
- Adicionar testes de contrato com Testcontainers para um banco externo.

## CI

O workflow em `.github/workflows/ci.yml` executa `./mvnw -B verify` em cada push e pull request para as branches principais, usando Java 17.
