# SimpleTaskRegister (Android Nativo)

Aplicativo Android nativo simples para:

- Listar tarefas pendentes por padrão.
- Filtrar para ver tarefas concluídas.
- Cadastrar tarefa por modal com botão flutuante (FAB).
- Selecionar data da tarefa por calendário no modal (DatePicker).
- Marcar/desmarcar conclusão por checkbox.
- Organizar tarefas por data (concluídas por data de conclusão mais recente).
- Navegar por menu lateral (lado direito) entre Início, Relatório e Configurações.
- Configurar horário diário de lembrete de tarefas pendentes.
- Interface com ícones e componentes Material 3 (AppBar, cards e FAB estendido).
- Mostrar relatório simples em tela:
  - Total
  - Concluídas
  - Pendentes
  - Vencidas

## Tecnologias

- Kotlin
- Jetpack Compose
- Material 3

## Como abrir

1. Abra a pasta do projeto no Android Studio.
2. Aguarde o sync do Gradle.
3. Execute no emulador ou dispositivo Android.

## Observações

- A data é escolhida por calendário no modal.
- As tarefas são persistidas localmente no aparelho.
- Em Android 13+, conceda permissão de notificação na tela de Configurações.

## Assinatura de release via keystore.properties

1. Crie um arquivo `keystore.properties` na raiz do projeto (não versionado).
2. Use como base o arquivo `keystore.properties.example`.
3. Preencha com os dados da sua chave de upload:

```properties
storeFile=release-signing/upload-keystore.jks
storePassword=sua_senha
keyAlias=upload
keyPassword=sua_senha
```

4. Gere o bundle para Google Play:

```bash
./gradlew bundleRelease
```

O arquivo final será gerado em:
`app/build/outputs/bundle/release/app-release.aab`
