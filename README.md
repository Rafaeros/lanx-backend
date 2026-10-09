# Lanx Backend 🚀

Backend do sistema ERP Lanx, desenvolvido com as melhores práticas de engenharia de software para garantir escalabilidade, segurança e fácil manutenção.

## 🏗 Arquitetura
O projeto utiliza um modelo híbrido estratégico:
- **Arquitetura Hexagonal (Ports & Adapters) + DDD (Domain-Driven Design) + CQRS:** Utilizados nos módulos mais complexos e com regras de negócio críticas (como o módulo `Auth` e o futuro módulo de cálculo `IQF`). Garante que o Domínio seja puro e isolado da infraestrutura.
- **Arquitetura em Camadas (Layered Architecture):** Utilizada em módulos de CRUDs mais simples (como alguns `Cadastros`) para evitar overengineering, mantendo a produtividade sem perder a organização.

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
