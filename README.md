# Execlauncher

Aplicativo em Java (Swing) para gerenciar executáveis Java (JAR) e scripts PNPM através de uma interface gráfica simples, com integração com a bandeja do sistema (system tray).

## Funcionalidades

- Criar executáveis do tipo **Java** (`java -jar`) e **PNPM** (`pnpm run <script>`)
- Definir variáveis de ambiente (manualmente ou via arquivo `.env`)
- Definir opções de linha de comando, comandos de pré-start e pós-stop
- Atraso opcional antes da inicialização do processo
- Janela de logs por executável (com salvar em arquivo)
- Importar/exportar executáveis como JSON
- Iniciar/parar todos os executáveis de uma vez
- Controle pela bandeja do sistema
- Persistência em banco local (H2 MVStore) em `%APPDATA%\Execlauncher\storage.db`

## Requisitos

- JDK 21+
- Maven (ou use o wrapper)

## Compilação

```bash
mvn clean package -Dmaven.test.skip
```

O JAR com todas as dependências será gerado em `target/execlauncher.jar`.

## Testes

```bash
mvn test
```

## Instaladores (jpackage)

Use o script `build.sh` (Linux/macOS) para gerar instaladores:

```bash
./build.sh
```

Selecione o tipo (`exe`, `app-image`, `msi`, `rpm`, `deb`, `pkg`, `dmg`) e informe a versão.

## Estrutura

```
src/main/java/balbucio/execlauncher/
├── Main.java                  # Ponto de entrada, caminho de instalação
├── Storage.java               # Persistência (H2 MVStore + Gson)
├── Executor.java              # Gerencia a execução de processos
├── Tray.java                  # Ícone da bandeja do sistema
├── action/                    # Diálogos de criação/edição
├── components/                # Cards da interface
├── model/                     # Entidades (Executable, CmdOptions)
├── ui/                        # Janelas (MainFrame, LogsFrame)
└── utils/                     # Utilitários (CommandLine, File, Java, Map)
```

## Licença

MIT — veja o arquivo [LICENSE](LICENSE).