# Lanx Backend 🚀

Backend do sistema ERP Lanx, desenvolvido com as melhores práticas de engenharia de software para garantir escalabilidade, segurança e fácil manutenção.

## 🏗 Arquitetura
O projeto utiliza um modelo híbrido. A escolha é feita **por feature**, pela complexidade dela:

| Use | Quando | Estrutura | Exemplos |
|-----|--------|-----------|----------|
| **Hexagonal (Ports & Adapters)** | Regra de negócio robusta (cálculos, políticas) ou integração com sistema externo (ERP, planilhas) | `domain/` (puro, sem Spring/JPA) · `application/` (casos de uso e portas) · `adapter/in` (web) · `adapter/out` (persistência, clients) | `compras.relatorios.analisepedidos`, `compras.relatorios.iqf`, `cadastros.unidademedida` |
| **Camadas (Layered)** | CRUD simples, sem regra além de validação e unicidade | `XController` → `XService` → `XRepository`, com `dtos/` e `exceptions/` | `cadastros.fornecedores`, `cadastros.departamentos`, `cadastros.operadores`, `auth` |

Regras comuns às duas, garantidas pelo `ArchitectureTest`:
- Controller nunca acessa repository; service nunca conhece controller.
- Um cadastro não conhece quem o usa: para vetar a exclusão de um registro em uso, o módulo dependente implementa um `UsageCheck` (ex.: `DepartamentoUsageCheck`).
- Exceções de negócio estendem `DomainException` e seguem o padrão `XNotFoundException`, `XAlreadyExistsException`, `XInUseException`.
- Controllers devolvem só o `ApiResponse` (`ok`, `created`...): o `ApiResponseStatusAdvice` aplica o `status` do corpo como status HTTP.

Uma feature em camadas que ganhar regra complexa ou integração externa migra para hexagonal.

## 🛠 Tecnologias Principais
- **Java 21+** (Records, Pattern Matching)
- **Spring Boot 3.x**
- **Spring Security** (Autenticação JWT & RBAC Granular)
- **Spring Data JPA & Hibernate**
- **PostgreSQL** (Banco de dados relacional)
- **Flyway** (Migrations)
- **Docker & Docker Compose** (Infraestrutura local)

## 🔒 Segurança (RBAC Granular)
O sistema possui um controle de permissões altamente sofisticado:
- **Catálogo de Funcionalidades:** Módulos declaram suas funcionalidades (ex: `cadastros.unidade-medida`) via `FeatureCatalogContributor`.
- **Bitmask CRUD:** Ações (Ler, Criar, Atualizar, Deletar) são salvas em um único número inteiro (bitwise) para performance extrema.
- **Grants & Denies:** Além dos perfis (Roles) genéricos, é possível dar permissões individuais ou bloquear permissões específicas por usuário (O "Deny" sempre vence).

---

## 💻 Como rodar o projeto localmente

### 1. Pré-requisitos
- JDK 21 ou superior instalado.
- Docker e Docker Compose instalados.
- Maven (Opcional, pois o projeto possui o `mvnw` embutido).

### 2. Subindo o Banco de Dados
Na raiz do projeto, existe um arquivo `compose.yaml`. Para subir o PostgreSQL, execute:
```bash
docker compose up -d
```
*O banco subirá na porta 5432. As migrations do Flyway (`V1__init.sql`) rodarão automaticamente assim que a aplicação Spring iniciar, criando todas as tabelas necessárias.*

### 3. Configurando as Variáveis de Ambiente
Copie o arquivo de exemplo e crie o seu `.env`:
```bash
cp .env.example .env
```
*(Certifique-se de preencher as variáveis do `.env` com a senha do banco, JWT Secret e credenciais de administrador).*

### 4. Iniciando a Aplicação
Execute o projeto via Maven Wrapper:
```bash
# Windows
.\mvnw spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```
*A aplicação estará disponível em `http://localhost:8080`.*

---

## 🐶 Testando as APIs com o Bruno

Este projeto utiliza a abordagem **Docs as Code**. A coleção de requisições REST não fica num Postman externo, mas sim versionada junto com o código usando o **[Bruno](https://www.usebruno.com/)**.

### Como importar a Coleção
1. Baixe e instale o aplicativo **Bruno**.
2. Abra o Bruno, clique em **"Open Collection"**.
3. Navegue até a pasta do projeto e selecione a pasta `bruno/LanxBackend`.

### Como configurar o Ambiente (Environment)
Para segurança, as senhas não são subidas para o GitHub. Siga este passo a passo para configurar seu ambiente local:
1. No canto superior direito do Bruno, selecione o Environment **"Dev"**.
2. Clique no ícone de engrenagem (⚙️) ao lado do nome "Dev" para configurar o ambiente.
3. Vá até a aba **"Secrets"**.
4. Procure a variável `adminPassword` e preencha com a senha padrão do administrador configurada no seu `.env`.
5. Clique em **Save**. *(O Bruno vai gerar um `.env` interno que já está no nosso `.gitignore`).*

### Fluxo de Teste Inicial
1. Rode a requisição **Auth > Authentication > Login**.
2. O Bruno está configurado com scripts pós-requisição que capturam o Token JWT retornado e salvam na variável de ambiente `token`.
3. A partir daí, todas as outras requisições protegidas (como as da pasta `Cadastros`) usarão o Bearer Token automaticamente!
