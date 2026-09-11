package br.com.douglaslehmann.controlefrota

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private val prefs by lazy { getSharedPreferences("frota", Context.MODE_PRIVATE) }
    private var serverUrl: String
        get() = prefs.getString("server_url", "http://192.168.1.10:5000") ?: "http://192.168.1.10:5000"
        set(value) { prefs.edit().putString("server_url", value).apply() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        setContentView(webView)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }
        webView.webViewClient = WebViewClient()
        webView.loadUrl(serverUrl.trimEnd('/'))
        webView.setOnLongClickListener {
            showServerDialog()
            true
        }
    }

    private fun showServerDialog() {
        val input = EditText(this)
        input.setText(serverUrl)
        input.setSingleLine(true)
        AlertDialog.Builder(this)
            .setTitle("Endereço do servidor")
            .setMessage("Informe o endereço do Controle de Frota.")
            .setView(input)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar") { _, _ ->
                var value = input.text.toString().trim()
                if (!value.startsWith("http://") && !value.startsWith("https://")) value = "http://$value"
                serverUrl = value
                webView.loadUrl(value.trimEnd('/'))
            }
            .show()
    }

    @Deprecated("Deprecated in Android SDK")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}