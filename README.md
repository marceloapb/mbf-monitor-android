# MBF Monitor — App Android

App Android que traz a solução de monitoramento MBF (painel de custos AWS + leitor de
e-mails com IA) para o celular, num WebView em tela cheia apontando para o painel.

- **URL da solução:** https://sx35x2e9pd.execute-api.us-east-1.amazonaws.com/prod/
- **applicationId:** `com.bloise.mbfmonitor`
- Navegação com botão voltar, "puxar para atualizar", links externos abrem no navegador.

## Como obter o APK

O APK é compilado automaticamente pelo **GitHub Actions** a cada push na `main`
(ou manualmente em Actions → build-apk → Run workflow).

1. Acesse a aba **Actions** do repositório.
2. Abra a execução mais recente do workflow **build-apk**.
3. Baixe o artifact **mbf-monitor-apk** (contém `mbf-monitor.apk`).

## Como instalar no Android

1. Copie o `mbf-monitor.apk` para o celular.
2. Abra o arquivo; o Android vai pedir para permitir **instalar de fontes desconhecidas**
   (Configurações → Apps → Instalar apps desconhecidos → permitir para o app usado).
3. Instale e abra. O app carrega o painel e pede login normalmente.

## Assinatura

O APK é assinado no CI com uma keystore gerada na hora (uso pessoal). Para publicar na
Play Store ou ter atualizações estáveis, gere uma keystore fixa e guarde como secret.

## Build local (opcional)

Requer Android SDK + JDK 17:

```
gradle wrapper --gradle-version 8.7
./gradlew :app:assembleRelease
```

O APK sai em `app/build/outputs/apk/release/`.
