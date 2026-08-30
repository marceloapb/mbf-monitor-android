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

## Notificações push (FCM)

O app recebe push quando o backend detecta e-mail(s) novo(s) da AWS. Usa Firebase Cloud
Messaging (gratuito). Passo a passo (feito uma vez):

### 1. Criar o projeto no Firebase
1. Acesse https://console.firebase.google.com e crie um projeto (plano **Spark**, grátis).
2. Adicione um app **Android** com o package **`com.bloise.mbfmonitor`**.
3. Baixe o **`google-services.json`**.

### 2. Habilitar o push no build (app)
Converta o arquivo em base64 e cadastre como secret do repositório:

- Secret: **`GOOGLE_SERVICES_JSON_BASE64`** (Settings → Secrets → Actions).
  - Linux/Mac: `base64 -w0 google-services.json`
  - Windows PowerShell: `[Convert]::ToBase64String([IO.File]::ReadAllBytes("google-services.json"))`

O workflow injeta o arquivo automaticamente e ativa o FCM no APK.

### 3. Habilitar o envio no backend (Lambda)
1. No Firebase → Configurações do projeto → **Contas de serviço** → **Gerar nova chave privada**
   (baixa um JSON da service account).
2. Guarde esse JSON no SSM (SecureString) do backend, no parâmetro
   **`/mbf/prod/cost-dashboard/fcm-service-account`**:

```
aws ssm put-parameter --name "/mbf/prod/cost-dashboard/fcm-service-account" \
  --type SecureString --overwrite --value file://service-account.json --region us-east-1
```

Pronto: no próximo scan que achar e-mail novo, o push chega no celular. Sem esses arquivos,
o app funciona normal (só sem push).


## Build local (opcional)

Requer Android SDK + JDK 17:

```
gradle wrapper --gradle-version 8.7
./gradlew :app:assembleRelease
```

O APK sai em `app/build/outputs/apk/release/`.
