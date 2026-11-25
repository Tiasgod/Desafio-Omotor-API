# Roda Livre — API REST para Oficina de Bicicletas

API REST em Java 21 e Spring Boot 3 para cadastro de clientes, catálogo de serviços e ordens de serviço, com persistência em PostgreSQL.

## Tecnologias

- Java 21 e Spring Boot 3.5.16
- Spring Web, Spring Data JPA, Bean Validation, Hibernate, Lombok
- PostgreSQL 15; Docker Compose para o banco local
- Maven Wrapper para execução

## Repositório

https://github.com/Tiasgod/Desafio-Omotor-API.git

Para clonar:

```bash
git clone https://github.com/Tiasgod/Desafio-Omotor-API.git
cd Desafio-Omotor-API
```

## Pré-requisitos

Java 21 e Docker Desktop com Docker Compose, ou uma instância PostgreSQL acessível. No Windows, execute os comandos no PowerShell na pasta do projeto.

## Configuração e execução

1. Crie um arquivo `.env` na raiz do projeto copiando `.env.example`:

   ```powershell
   Copy-Item .env.example .env
   ```

2. Para usar o PostgreSQL local do `docker-compose.yml`, mantenha os valores de exemplo. A configuração do Compose inicia o banco `postgres`, usuário `postgres`, senha `postgres`, na porta `5432`. **Essas credenciais são apenas de desenvolvimento**. Para um servidor diferente, edite `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_SSLMODE` e `DB_CHANNEL_BINDING` no `.env` conforme o seu banco. Não publique o `.env`.

3. Inicie o banco (se utilizar Docker):

   ```powershell
   docker compose up -d
   docker compose ps
   ```

4. Inicie o Spring Boot:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   No Linux/macOS, utilize `./mvnw spring-boot:run`.

5. A API estará em `http://localhost:8080`. Teste `GET http://localhost:8080/clientes` no Postman. **Não é preciso criar o banco manualmente com o Docker Compose acima:** o contêiner cria `postgres` na primeira inicialização. Se usar um PostgreSQL instalado separadamente, crie o banco indicado em `DB_NAME` antes de iniciar a API, por exemplo, com `CREATE DATABASE roda_livre;`.

A configuração `spring.jpa.hibernate.ddl-auto=update` cria/atualiza as três tabelas após conectar ao banco. O banco do Compose usa um volume persistente: `docker compose down` interrompe os contêineres sem excluir o volume. Não use `docker compose down -v` caso queira preservar os registros.

## Entidades e relacionamentos

- `clientes`: `id`, `nome`, `email` (único), `telefone`, `cidade`, `data_cadastro`, `ativo`.
- `servicos`: `id`, `codigo` (único), `nome`, `categoria`, `preco_sugerido`, `ativo`.
- `ordens_servico`: `id`, `cliente_id` (FK), `servico_id` (FK), `data_entrada`, `modelo_bicicleta`, `descricao`, `valor`, `status`.

Uma ordem pertence a um cliente e a um serviço. Não há tabela de bicicleta; o modelo é uma coluna textual na ordem. Valores monetários são armazenados em colunas `NUMERIC(12,2)` via `BigDecimal`. Datas usam `LocalDateTime`/timestamp sem fuso. Categorias aceitas: `REVISAO`, `FREIOS`, `PNEUS`, `TRANSMISSAO`. Status aceitos: `ABERTA`, `CONCLUIDA`, `CANCELADA`.

### Regras de negócio

- E-mail de cliente e código de serviço únicos (duplicidade: HTTP 409).
- Cliente e serviço inativos permanecem cadastrados, mas não podem ser usados em uma nova ordem (409).
- Um cliente não pode ter duas ordens `ABERTA` ao mesmo tempo (409). Para proteção adicional no PostgreSQL, consulte `sql/indice_ordem_aberta.sql`.
- Toda ordem nasce `ABERTA`. Somente `ABERTA -> CONCLUIDA` ou `ABERTA -> CANCELADA` é permitido; estados finais não mudam (409).
- `valor` e `precoSugerido` devem ser maiores que zero e ter no máximo duas casas decimais (400). Eles podem ser diferentes.
- Cliente ou serviço inexistente ao abrir uma ordem retorna 404.
- As demais restrições de campos e formatos são verificadas com Bean Validation.

## Endpoints (12 previstos no desafio)

| Método | Endpoint | Objetivo |
|---|---|---|
| POST | `/clientes` | Criar cliente (201) |
| GET | `/clientes` | Listar e filtrar clientes (200) |
| GET | `/clientes/{id}` | Buscar cliente (200/404) |
| PUT | `/clientes/{id}` | Atualizar cliente e atividade (200) |
| POST | `/servicos` | Criar serviço (201) |
| GET | `/servicos` | Listar e filtrar serviços (200) |
| GET | `/servicos/{id}` | Buscar serviço (200/404) |
| PUT | `/servicos/{id}` | Atualizar serviço e atividade (200) |
| POST | `/ordens-servico` | Criar ordem ABERTA (201) |
| GET | `/ordens-servico` | Listar e filtrar ordens (200) |
| GET | `/ordens-servico/{id}` | Buscar ordem (200/404) |
| PATCH | `/ordens-servico/{id}/status` | Concluir/cancelar ordem (200/409) |

Todos os pedidos e respostas usam JSON com nomes em **camelCase**. Os nomes das colunas do banco usam **snake_case**. Datas e horas são strings JSON no formato ISO-8601 **sem fuso**, `yyyy-MM-dd'T'HH:mm:ss`. Exemplo: `2026-03-10T09:00:00`. O objeto Java continua sendo `LocalDateTime`; ele não é enviado como objeto JSON com campos `year`, `month`, etc.

### Exemplos de requisições

`POST /clientes`:

```json
{"nome":"Pedro Almeida","email":"pedro@email.com","telefone":"31987654321","cidade":"Belo Horizonte"}
```

Retorna 201 com `id`, os campos enviados, `dataCadastro` (gerada pela API) e `ativo: true`.

`PUT /clientes/1`:

```json
{"nome":"Pedro Almeida","email":"pedro@email.com","telefone":"31987654321","cidade":"Belo Horizonte","ativo":true}
```

`POST /servicos`:

```json
{"codigo":"FRE-01","nome":"Troca de pastilha","categoria":"FREIOS","precoSugerido":70.00}
```

Retorna 201 com `id` e `ativo: true`.

`PUT /servicos/1`:

```json
{"codigo":"FRE-01","nome":"Troca de pastilha","categoria":"FREIOS","precoSugerido":70.00,"ativo":true}
```

`POST /ordens-servico` (**a data é obrigatória e enviada pelo cliente**):

```json
{
  "clienteId": 1,
  "servicoId": 1,
  "dataEntrada": "2026-03-10T09:00:00",
  "modeloBicicleta": "Mountain bike aro 29",
  "descricao": "Freio dianteiro chiando na descida",
  "valor": 80.00
}
```

Retorna 201 com `id`, `clienteId`, `clienteNome`, `servicoId`, `servicoNome`, `dataEntrada`, `modeloBicicleta`, `descricao`, `valor` e `status: "ABERTA"`. O `POST` não aceita `status`; o servidor define esse campo automaticamente.

`PATCH /ordens-servico/1/status`:

```json
{"status":"CONCLUIDA"}
```

### Consultas, filtros e paginação

- `GET /clientes?nome=pedro&cidade=Belo Horizonte&page=0&size=20`
- `GET /servicos?nome=freio&categoria=FREIOS&page=0&size=20`
- `GET /ordens-servico?status=ABERTA&clienteId=1&servicoId=1&page=0&size=20`
- `GET /ordens-servico?dataInicio=2026-03-01T00:00:00&dataFim=2026-03-31T23:59:59`

`page` tem padrão `0`; `size` tem padrão `20`, mínimo `1` e máximo `100`. Listagens usam a paginação padrão Spring (`content`, `totalElements`, `totalPages`, `number`, `size`). O filtro de datas é inclusivo e aceita só uma das extremidades.

### Respostas de erro

O projeto devolve JSON com `timestamp`, `status`, `error`, `message`. Status previstos: 400 para pedido inválido, 404 para registro inexistente e 409 para conflito de negócio. As mensagens identificam o campo ou a regra violada.

## Dados de exemplo (`dados.sql`)

O arquivo contém **12 clientes**, **6 serviços** e **30 ordens**, abrangendo pelo menos três cidades, quatro categorias, três status e datas distribuídas por mais de 60 dias. Há um serviço inativo e no máximo uma ordem aberta por cliente. Os nomes são fictícios.

**Importante:** o script foi feito para um **banco vazio, com as tabelas já criadas pelo Spring Boot**. Ele não limpa tabelas pré-existentes nem deve ser executado sobre os dados que você já cadastrou no Postman; se isso acontecer, as verificações de quantidade podem falhar ou pode haver conflito de e-mails/códigos.

Após iniciar o aplicativo uma vez, execute o script na pasta raiz do projeto:

```powershell
psql -h localhost -p 5432 -U postgres -d postgres -f dados.sql
```

Esse comando presume as credenciais locais de exemplo; adapte `-U` e `-d` se você configurou outro banco. Execute preferencialmente em uma base de demonstração limpa. O script inclui verificações de volume e integridade, com `BEGIN`/`COMMIT`.

## Testes recomendados

Valide no Postman: `201` no cadastro; `200` nas consultas e atualizações; `400` para datas ausentes ou mal formatadas, valores inválidos e campos obrigatórios; `404` para IDs inexistentes; `409` para e-mail/código duplicado, inativos, segunda ordem aberta e transições de status inválidas. Reinicie a aplicação para verificar a persistência no PostgreSQL. Em `src/test` existe o teste inicial de contexto do Spring.
