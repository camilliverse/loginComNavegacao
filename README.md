# Controle de Doação de Sangue

Aplicativo Android para agendamento e controle de doações de sangue.

## Descrição

Aplicativo desenvolvido em Java para Android Studio que permite criar
agendamentos, registrar coletas a partir deles e acompanhar o histórico. Os
dados ficam em um **backend REST** (Spring Boot + PostgreSQL, o repositório
`ppi-backend`): o app consome a API via Retrofit e guarda localmente apenas a
sessão e um cache do doador logado, para o topo da Home abrir na hora.

## Funcionalidades

- Login de doadores cadastrados
- Cadastro de doadores com CPF e tipo sanguíneo
- Perfil em formato de cartazinho, com edição de nome, email e senha
- Agendamento de doações por hemocentro, data e hora
- Lista de agendamentos, com cancelamento quando pendente
- Registro de coleta a partir de agendamento elegível
- Histórico de lotes registrados, com código, tipo, volume, local, coleta e validade
- Interface simples e intuitiva

## Tecnologias

- **Linguagem**: Java
- **IDE**: Android Studio
- **Rede**: Retrofit 2 + OkHttp (logging) + Gson
- **Backend**: API REST Spring Boot (`ppi-backend`), persiste em PostgreSQL
- **Sessão**: SharedPreferences (apenas CPF logado e cache do doador)
- **Lista**: RecyclerView
- **UI**: Material 3 (tokens de cor, tema claro/escuro automático)

## Tema e Auto-contraste

A identidade continua no vermelho (`#AD2C2C` / logo `#C02040`), mas a cor deixou de
ser escrita direto no layout. Tudo passa por **tokens do Material 3**, o que faz o
app alternar entre tema claro e escuro sozinho e mantém o texto sempre legível.

### Como funciona

| Arquivo | Papel |
|---------|-------|
| `res/values/colors.xml` | Paleta do tema **claro** |
| `res/values-night/colors.xml` | Mesmos nomes, tonalidades do tema **escuro** |
| `res/values/themes.xml` | **Único** tema: mapeia os papéis do Material 3 |
| `res/values/bools.xml` + `res/values-night/bools.xml` | Cor dos ícones das barras do sistema |
| `res/values/styles.xml` | Estilos de componente (botão, campo, cartão, pílula) |
| `res/values/dimens.xml` + `res/values-sw600dp/dimens.xml` | Espaçamentos e tipografia responsivos |
| `res/color/text_field_stroke.xml` | Cor da borda dos campos (seletor de estado) |

Como as paletas clara e escura usam **os mesmos nomes de recurso**, existe só uma
definição de tema: o Android escolhe a tonalidade certa conforme o modo do sistema.

### Regra de auto-contraste

Cada papel do Material 3 é usado junto do seu par `onX` (`colorOnSurface` sobre
`colorSurface`, `colorOnPrimary` sobre `colorPrimary`, etc.). Todos esses pares
passam de WCAG AA, então nenhuma combinação de tela fica ilegível — no claro nem
no escuro. As pílulas de status seguem a mesma lógica com os tokens
`status_*` / `status_*_container`.

**Ao criar uma tela nova, nunca use cor literal** (`#FF0000`, `0xFFFF0000`).
Use `?attr/colorOnSurface`, `?attr/colorSurfaceContainerLow`,
`?attr/colorOutlineVariant` etc.

### Layout responsivo

- `BaseActivity` ativa o *edge-to-edge* e reserva as margens da status bar /
  barra de navegação no container `@id/rootConteudo` de cada layout.
- `AutoFitGridLayoutManager` calcula o número de colunas da RecyclerView pela
  largura da tela: uma coluna no celular, duas ou mais em tablets — sem layout
  alternativo.
- `content_max_width` + `values-sw600dp` centralizam o conteúdo em telas largas.

### Identidade visual preservada

- Cabeçalhos com o gradiente vermelho (`drawable/bg_hero`) em todas as telas.
- Logo dentro de um círculo claro (`drawable/bg_logo_badge`), para o logo vermelho
  não sumir sobre o fundo vermelho.
- Fontes Bungee (títulos) e Montserrat (corpo) mantidas.

## Estrutura do Projeto

```
app/src/main/java/br/edu/unifaj/cc/mobile/logincomnavegacao/
├── api/                   # Camada REST
│   ├── ApiService.java            # Endpoints Retrofit
│   ├── ApiClient.java             # Fábrica do Retrofit (base URL)
│   ├── *Response.java             # DTOs de resposta
│   └── *Request.java              # DTOs de requisição
├── data/                  # Repositório
│   ├── ApiRepository.java         # Fachada assíncrona da API + mapeamento
│   ├── RepositoryCallback.java    # Callback (onSuccess/onError)
│   └── Perfil.java                # Doador + nível
├── model/
│   ├── entity/            # Hemocentro, Endereco, Agendamento, BolsaSangue
│   ├── enums/             # TipoSanguineo, FatorRh, StatusAgendamento, StatusBolsa
│   ├── user/              # User, Doador
│   └── NivelDoador.java   # Nível do cartazinho (teto de 3 registros)
├── util/
│   ├── PrefsManager.java         # Sessão e cache do doador logado
│   ├── AutoFitGridLayoutManager.java # Grade com colunas calculadas pela largura
│   ├── InsetsUtils.java          # Margens das barras do sistema (edge-to-edge)
│   ├── DateUtils.java            # Datas (BR) + conversão ISO do backend
│   └── ValidacaoUtils.java
├── adapter/
│   ├── MenuHomeAdapter.java                # Cards do menu principal
│   ├── AgendamentoAdapter.java
│   ├── AgendamentoParaRegistroAdapter.java
│   └── BolsaSangueAdapter.java
└── activity/
    ├── BaseActivity.java      # Base: edge-to-edge + insets
    ├── LoginActivity.java
    ├── CadastroActivity.java
    ├── HomeActivity.java
    ├── DoacaoActivity.java
    ├── RegistrarDoacaoActivity.java
    ├── AgendamentoActivity.java
    ├── HistoricoActivity.java
    ├── ListaAgendamentosActivity.java
    ├── PerfilActivity.java
    └── EditarPerfilActivity.java
```

Os testes unitários estão em:

```text
app/src/test/java/br/edu/unifaj/cc/mobile/logincomnavegacao/
├── model/
│   ├── NivelDoadorTest.java
│   └── enums/
│       └── TipoSanguineoTest.java
└── util/
    ├── DateUtilsTest.java
    └── ValidacaoUtilsTest.java
```

## Fluxo das Telas

1. **Login** → Doador acessa com email e senha
2. **Cadastro** → Novo doador se registra
3. **Home** → Menu principal após login
4. **Perfil** → Cartazinho do doador, aberto pelo topo da Home
5. **Editar Perfil** → Altera nome, email e senha
6. **Agendar doação** → Cria agendamento por hemocentro, data e hora
7. **Meus agendamentos** → Lista agendamentos e permite cancelar pendentes
8. **Registrar coleta** → Escolhe um agendamento elegível
9. **Confirmar coleta** → Registra volume e quantidade a partir do agendamento
10. **Histórico** → Lista os lotes registrados

## Como Executar

O app depende do backend REST. Suba-o primeiro (ver `ppi-backend/README.md`):

```bash
cd ppi-backend
sudo docker compose up -d
./gradlew bootRun        # backend em http://localhost:8080
```

Depois:

1. Clone este repositório
2. Abra no Android Studio
3. Aguarde a sincronização do Gradle
4. Execute no emulador ou dispositivo

### Base URL da API

Definida em `api/ApiClient.java` (`BASE_URL`):

- **Emulador**: `http://10.0.2.2:8080/` (10.0.2.2 é o `localhost` da máquina host)
- **Aparelho físico**: troque pelo IP da máquina na mesma rede (ex.: `http://192.168.1.10:8080/`)

O manifesto já declara `INTERNET` e `usesCleartextTraffic` (HTTP em
desenvolvimento).

### Gerar APK

1. No menu superior, vá em: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Aguarde a compilação
3. O APK será gerado em: `app/build/outputs/apk/debug/app-debug.apk`

### Via Linha de Comando

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew lintDebug
```

## Dados Armazenados

Os dados de negócio (doador, agendamentos, bolsas) ficam no **backend**
(PostgreSQL). No aparelho, o `PrefDoacaoSangue` (SharedPreferences) guarda apenas
a sessão e um cache do doador logado:

| Chave | Conteúdo |
|-------|----------|
| `doador_cache` | Cópia do doador logado, para a Home abrir sem esperar a rede |
| `logged_cpf` | CPF da sessão atual (marcador de login) |

A **senha nunca é gravada** no aparelho: o login envia e-mail e senha para a API
e o backend devolve só os dados públicos do doador (`DoadorResponse`, sem senha).

### Mapeamento REST → modelo

| Tela / ação | Endpoint |
|-------------|----------|
| Login | `POST /api/doadores/login` |
| Cadastro | `POST /api/doadores` |
| Home / Perfil | `GET /api/doadores/{cpf}` |
| Editar perfil | `PUT /api/doadores/{cpf}` |
| Agendar | `GET /api/hemocentros` + `POST /api/agendamentos?cpf=` |
| Meus agendamentos | `GET /api/agendamentos?cpf=` + `POST /api/agendamentos/{id}/cancelar?cpf=` |
| Registrar coleta (lista) | `GET /api/agendamentos/para-registro?cpf=` |
| Registrar coleta (salvar) | `POST /api/bolsas-sangue/agendamentos/{id}/coletas?cpf=` |
| Histórico | `GET /api/bolsas-sangue?cpf=` |

As classes de `api/*Response` espelham os DTOs do backend; `data/ApiRepository`
converte essas respostas nos modelos do app (`Agendamento`, `BolsaSangue`, ...)
e formata datas ISO (`2025-12-30`) para o padrão da tela (`30/12/2025`).

## Regras de Negócio

As regras abaixo são aplicadas em duas camadas: validação rápida no app e
validação definitiva no backend (que é a fonte da verdade).

- Campos vazios não são permitidos em nenhuma tela.
- O email precisa ter formato válido; a senha precisa ter ao menos 6 caracteres.
- O CPF precisa ter 11 dígitos, com ou sem máscara.
- Apenas doadores logados podem agendar, registrar coleta, ver agendamentos e ver o histórico.
- A data do agendamento começa em hoje; a coleta só pode ser registrada quando a data é hoje ou já passou.
- Cada agendamento gera no máximo um lote: agendamento cancelado, realizado, futuro ou já registrado não é elegível.
- Cada coleta usa volume de 200 a 470 ml e quantidade de 1 a 6 bolsas.
- A validade do lote é a data da coleta mais 42 dias.
- O nível do doador conta registros de coleta e trava em 3; o número de bolsas da sessão não muda o nível.
- Trocar a senha exige a senha atual, conferida pelo backend.

## Descrição das Classes

### Model

- **User.java**: Classe abstrata com nome, email e senha
- **Doador.java**: Estende `User` e adiciona CPF, tipo sanguíneo e fator Rh
- **DateUtils.java**: Datas estritas em `dd/MM/yyyy` + conversão de/para o ISO do backend

### Rede / Dados

- **api/ApiService.java**: Contrato Retrofit com os endpoints do backend
- **api/ApiClient.java**: Fábrica do Retrofit (base URL e logging)
- **api/*Response.java / *Request.java**: DTOs que espelham o backend
- **data/ApiRepository.java**: Fachada assíncrona da API; converte DTOs nos modelos do app
- **data/Perfil.java**: Doador + nível (registros, limite, percentual)

### Util

- **PrefsManager.java**: Sessão (CPF logado) e cache do doador; não guarda dados de negócio
- **ValidacaoUtils.java**: Valida campos obrigatórios, email, CPF, senha, volume e quantidade

### Adapter

- **MenuHomeAdapter.java**: Cards do menu principal (ícone, título e descrição)
- **AgendamentoAdapter.java**: Lista de agendamentos, com pílula de status e
  contador de bolsas registradas
- **AgendamentoParaRegistroAdapter.java**: Agendamentos que o agente pode registrar
- **BolsaSangueAdapter.java**: Histórico de lotes doados, com local e quantidade

### Activities

Todas estendem `BaseActivity`, que ativa o edge-to-edge e aplica as margens das
barras do sistema no container `@id/rootConteudo` de cada layout.

- **BaseActivity.java**: Base das telas (edge-to-edge e tratamento de insets)
- **LoginActivity.java**: Tela inicial com validação de email e senha
- **CadastroActivity.java**: Tela para cadastrar novos usuários
- **HomeActivity.java**: Tela principal com menu de opções em cards
- **DoacaoActivity.java**: Agente escolhe qual agendamento registrar (lista somente-leitura)
- **RegistrarDoacaoActivity.java**: Confirma a coleta; data e local travados
- **AgendamentoActivity.java**: Doador cria o agendamento
- **HistoricoActivity.java**: Lista dos lotes doados
- **ListaAgendamentosActivity.java**: Lista de agendamentos do doador
- **PerfilActivity.java**: Cartazinho do doador, somente-leitura
- **EditarPerfilActivity.java**: Altera nome, email e senha

### Modelo

- **BolsaSangue.java**: `agendamentoId` liga a bolsa ao agendamento (garante uma
  bolsa por agendamento) e `quantidade` guarda quantas bolsas saíram na sessão
- **BolsaSangue.fromAgendamento()** herda data, local e validade do agendamento, para
  que o registro nunca divirja do que foi agendado
- **NivelDoador.java**: regra do nível do cartazinho - a barra enche com os
  registros de coleta e trava em `LIMITE` (3). Classe pura, sem Android, para
  ficar testável sem emulador

## Manual de Usuário

> O app segue o tema claro/escuro do sistema. Para ver o outro modo, altere
> **Configurações → Tela → Modo escuro**.

### Primeiro Acesso

1. Na tela de login, clique em "Ainda não tenho cadastro"
2. Preencha todos os campos (nome, email, senha, CPF, tipo sanguíneo)
3. Clique em "Cadastrar"
4. Você será redirecionado para a tela de login
5. Faça login com os dados cadastrados

### Registrar Coleta

> A coleta é registrada pelo **agente de saúde**, e sempre a partir de um
> agendamento que o doador já criou. Por isso data e local não são digitados:
> eles vêm do agendamento e aparecem travados na tela.

1. Na tela principal, toque em "Registrar coleta"
2. Escolha o agendamento na lista (só aparecem os de data de hoje ou anterior,
   que ainda não têm coleta registrada)
3. Confira a data e o hemocentro mostrados no topo
4. Informe o **volume por bolsa** e a **quantidade de bolsas**
5. Toque em "Registrar coleta"
6. A bolsa entra no histórico e o agendamento passa a "Realizado"

### Agendar Doação

1. Na tela principal (Home), toque em "Agendar doação"
2. Escolha o hemocentro, a data e a hora
3. Clique em "Confirmar agendamento"
4. O agendamento aparece em "Meus agendamentos", onde também pode ser cancelado

### Ver Histórico

1. Na tela principal (Home), toque em "Ver histórico"
2. Veja a lista de todos os lotes doados
3. Cada item mostra: código, tipo, quantidade, volume, local, coleta e validade

### Ver o Perfil

1. Na tela principal (Home), toque no cartazinho do topo
2. O cartão mostra nome, email, CPF e tipo sanguíneo
3. Veja a barra **Nível de doador** e a contagem de registros
4. Toque em "Editar perfil" para alterar nome, email ou senha

O cartazinho substitui a antiga saudação "Olá, usuário". Ele é a única entrada
para a tela de perfil, então não existe um card extra no menu.

#### Nível de doador

A barra do cartazinho enche com base no **número de registros de coleta já
existentes** e chega ao máximo em **3 registros**:

| Registros | Barra | Texto |
|-----------|-------|-------|
| 0 | vazia | "0 de 3 registros" |
| 1 | 33% | "1 de 3 registro" |
| 2 | 67% | "2 de 3 registros" |
| 3 ou mais | 100% | "Nível máximo alcançado" |

Um registro é um agendamento registrado pela agente. A quantidade de bolsas da
sessão não altera o nível: 1 ou 6 bolsas na mesma sessão contam como 1 registro
só. A regra mora em `NivelDoador.java`, que é uma classe pura de Java, sem
dependência de Android, justamente para o teto ser testável sem emulador.

O valor é recalculado a cada vez que a tela de perfil aparece, então a barra
sempre reflete a lista de registros gravada. Na prática ela só cresce, porque
hoje nenhum fluxo apaga um registro de coleta.

### Editar Perfil

1. Na tela de perfil, toque em "Editar perfil"
2. Altere o nome e/ou o email
3. Para trocar a senha, preencha **senha atual** e **nova senha**; deixar os
   dois campos em branco mantém a senha atual
4. Toque em "Salvar alterações" e volte ao cartazinho, que já mostra os dados novos

CPF e tipo sanguíneo aparecem na tela mas **não podem ser editados**:

- O **CPF** é a chave que associa o doador aos agendamentos. Se fosse editável,
  os agendamentos antigos deixariam de aparecer, por isso a tela avisa que ele é fixo.
- O **tipo sanguíneo** vem do cadastro e é exibido como referência.

### Sair

1. Na tela principal (Home), toque em "Sair da conta"
2. Você será redirecionado para a tela de login

## Autores

- Rodolfo Rodrigues Pinheiro - RA: 12530689
- Rodrigo Pereira Junior - RA: 12529249
- Camilli dos Santos - RA: 12529495
- Otávio Siqueira Gonçalves - RA: 12529937
- Gabriel Rodrigues de Oliveira - RA: 12529520

## Diagramas de Classe

Os diagramas de classes do projeto estão disponíveis na pasta `diagramas/`:

### Arquivos Disponíveis

| Arquivo | Descrição |
|--------|-----------|
| `diagramas/classes-dominio.plantuml` | Diagrama do modelo de domínio do app (PlantUML) |
| `diagramas/diagrama-doacaoSangue.png` | Diagrama visual em PNG |

![Diagrama](diagramas/diagrama-doacaoSangue.png)

> O **diagrama completo do domínio** (com mais de 10 classes de negócio, herança
> `Pessoa` → `Doador`, estoque, triagem e notificações) está no backend, em
> `ppi-backend/diagramas/classes-dominio.puml` (e `.png`).

### Estrutura do Modelo de Domínio (app)

```
model/
├── enums/
│   ├── TipoSanguineo (A, B, AB, O)
│   ├── FatorRh (POSITIVO, NEGATIVO)
│   ├── StatusAgendamento (PENDENTE, CONFIRMADO, CANCELADO, REALIZADO)
│   └── StatusBolsa (DISPONIVEL, RESERVADA, UTILIZADA, VENCIDA)
├── entity/
│   ├── Hemocentro
│   ├── Endereco
│   ├── Agendamento
│   └── BolsaSangue
└── user/
    ├── User (abstrata)
    └── Doador
```

## Licença

Nenhum arquivo de licença está incluído neste repositório.