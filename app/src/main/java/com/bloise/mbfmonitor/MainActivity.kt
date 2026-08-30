package com.bloise.mbfmonitor

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipe: SwipeRefreshLayout

    // URL da solução de monitoramento (painel de custos + e-mails AWS com IA).
    private val startUrl = "https://sx35x2e9pd.execute-api.us-east-1.amazonaws.com/prod/"
    private val appHost = "sx35x2e9pd.execute-api.us-east-1.amazonaws.com"

    private val notifPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* opcional */ }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPush()

        swipe = findViewById(R.id.swipe)
        webView = findViewById(R.id.webview)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(false)
            mediaPlaybackRequiresUserGesture = false
        }

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                val url = request?.url ?: return false
                // Links do próprio painel abrem no WebView; externos vão pro navegador.
                return if (url.host == appHost) {
                    false
                } else {
                    startActivity(Intent(Intent.ACTION_VIEW, url))
                    true
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                swipe.isRefreshing = false
            }
        }

        swipe.setOnRefreshListener { webView.reload() }

        // Botão voltar navega no histórico do WebView; se não houver, fecha o app.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })

        if (savedInstanceState == null) {
            webView.loadUrl(startUrl)
        }
    }

    /** Pede permissão de notificação (Android 13+) e registra o token FCM no backend. */
    private fun setupPush() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        // Obtém o token atual do FCM e registra no backend (falha suave se FCM indisponível).
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.let { MbfMessagingService.registerToken(it) }
                }
            }
        } catch (_: Exception) {
            // FCM pode não estar configurado ainda (sem google-services.json) — ignora.
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }
}
