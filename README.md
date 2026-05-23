# SimpleTaskRegister (Android Nativo)

Aplicativo Android nativo simples para:

- Cadastrar tarefa com data limite.
- Listar tarefas cadastradas.
- Marcar/desmarcar conclusão por checkbox.
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

- Formato de data no cadastro: `yyyy-MM-dd`.
- A lista fica em memória (não persiste após fechar o app).

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
