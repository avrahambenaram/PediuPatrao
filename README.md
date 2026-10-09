# PediuPatrao

Sistema web para gerenciamento de pedidos de uma pizzaria, desenvolvido com Java e Spring Boot.

## Tecnologias utilizadas

- **Java 21** — linguagem e ambiente de execução.
- **Spring Boot 3.4.4** — estrutura principal da aplicação.
- **Spring Web** — controllers e rotas HTTP.
- **Spring Security** — autenticação e autorização.
- **Spring Data MongoDB** e **MongoDB** — persistência dos dados.
- **Thymeleaf** — renderização das páginas HTML no servidor.
- **Thymeleaf Layout Dialect** — composição e reutilização de layouts.
- **Tailwind CSS** e **Bootstrap** — estilização e componentes de interface.
- **Lombok** — redução de código repetitivo.
- **Maven Wrapper** — execução do Maven sem instalação global do Maven.

## Pré-requisitos

- JDK 21.
- MongoDB local ou uma instância acessível do MongoDB Atlas.
- Git, caso vá clonar o repositório.

## Estrutura do projeto

A estrutura principal segue o padrão de projetos Maven e Spring Boot:

```text
PediuPatrao/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/umc/pediupatrao/
│       │       ├── PizzariaPedidosApplication.java
│       │       ├── config/      # Configurações, incluindo segurança
│       │       ├── controller/  # Controllers e rotas da aplicação
│       │       ├── entity/      # Entidades/modelos persistidos
│       │       ├── repository/  # Acesso aos dados com Spring Data
│       │       └── service/     # Regras de negócio
│       └── resources/
│           ├── static/          # CSS, JavaScript, imagens e outros arquivos estáticos
│           ├── templates/       # Páginas e fragmentos Thymeleaf
│           └── application.properties
├── .env.example                 # Modelo das variáveis de ambiente
├── .gitignore
├── mvnw                         # Maven Wrapper para Linux/macOS
├── mvnw.cmd                     # Maven Wrapper para Windows
├── pom.xml                      # Dependências e configuração do Maven
└── README.md
```

Os diretórios `config`, `controller`, `entity`, `repository` e `service` organizam as principais responsabilidades do backend. Os arquivos de interface ficam em `src/main/resources/templates`, enquanto os recursos estáticos ficam em `src/main/resources/static`.

## 1. Clone o repositório

```bash
git clone https://github.com/avrahambenaram/PediuPatrao.git
cd PediuPatrao
```

## 2. Configurar o arquivo `.env`

Copie `.env.example` para `.env`, na raiz do projeto.

### Linux

```bash
cp .env.example .env
```

### Windows — PowerShell

```powershell
Copy-Item .env.example .env
```

### Windows — Prompt de Comando (CMD)

```cmd
copy .env.example .env
```

Edite o `.env`. O arquivo de exemplo contém as variável `MONGODB_URI`. Para uma instância local do MongoDB sem autenticação, por exemplo:

```dotenv
MONGODB_URI=mongodb://localhost:27017/pediupatrao
```

Ajuste a URI conforme a configuração do seu banco.

### MongoDB local com autenticação

Se o MongoDB exigir usuário e senha, use o formato:

```dotenv
MONGODB_URI=mongodb://USUARIO:SENHA@localhost:27017/pediupatrao?authSource=admin
```

Substitua `USUARIO` e `SENHA` pelas credenciais do banco. Se a senha tiver caracteres especiais, codifique-os para uso em uma URI.

### MongoDB Atlas

Copie a URI de conexão fornecida pelo painel do Atlas e ajuste as credenciais e o banco:

```dotenv
MONGODB_URI=mongodb+srv://USUARIO:SENHA@SEU_CLUSTER.mongodb.net/pediupatrao
```

No Atlas, confira também se o endereço IP de onde a aplicação será executada está autorizado nas configurações de rede.

### Usuário padrão

Também é necessário editar no `.env` o usuário admin padrão que será criado para acesso do sistema:
```dotenv
ADMIN_USERNAME=admin
ADMIN_PASSWORD=password
```

## 3. Usuário e senha iniciais da aplicação

Na configuração de desenvolvimento atual, as credenciais iniciais de acesso à aplicação são:

| Campo | Valor |
|---|---|
| Usuário | `user` |
| Senha | `password` |

Essas credenciais são para entrar na aplicação, não para autenticar no MongoDB. Se o inicializador de dados ou a configuração de segurança tiverem sido alterados, os valores podem ser diferentes.

## 4. Executar a aplicação

Execute os comandos na raiz do projeto, onde estão `pom.xml` e o Maven Wrapper.

### Linux

Se necessário, dê permissão de execução ao wrapper:

```bash
chmod +x mvnw
```

Inicie a aplicação:

```bash
./mvnw spring-boot:run
```

### Windows — PowerShell

```powershell
.\mvnw.cmd spring-boot:run
```

### Windows — Prompt de Comando (CMD)

```cmd
mvnw.cmd spring-boot:run
```

Na primeira execução, o wrapper poderá baixar o Maven e as dependências do projeto. É necessária uma conexão com a internet nesse processo.

Quando a aplicação iniciar, acesse:

**http://localhost:8080**

## 5. Gerar e executar o JAR

### Linux

```bash
./mvnw clean package
```

### Windows — PowerShell

```powershell
.\mvnw.cmd clean package
```

### Windows — CMD

```cmd
mvnw.cmd clean package
```

O JAR será gerado em `target/`. Para executá-lo:

```bash
java -jar target/pediupatrao-1.0-SNAPSHOT.jar
```
