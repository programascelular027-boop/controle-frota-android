package br.com.douglaslehmann.controlefrota

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("viagens", Context.MODE_PRIVATE) }
    private lateinit var placa: EditText
    private lateinit var saida: EditText
    private lateinit var chegada: EditText
    private lateinit var kmFinal: EditText
    private lateinit var historico: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        montarTela()
        atualizarHistorico()
    }

    private fun montarTela() {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 28, 32, 32)
            setBackgroundColor(Color.rgb(248, 249, 250))
        }

        val titulo = TextView(this).apply {
            text = "CONTROLE DE SAÍDA"
            textSize = 25f
            setTextColor(Color.rgb(25, 55, 90))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 10)
        }
        root.addView(titulo, lp())

        val subtitulo = TextView(this).apply {
            text = "Informe os dados da viagem"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 0, 24)
        }
        root.addView(subtitulo, lp())

        placa = campo("Placa do veículo", "ABC1D23")
        root.addView(placa, lp())

        saida = campo("Hora de saída", "00:00")
        saida.isFocusable = false
        saida.setOnClickListener { escolherHora(saida) }
        root.addView(saida, lp())

        chegada = campo("Hora de chegada", "00:00")
        chegada.isFocusable = false
        chegada.setOnClickListener { escolherHora(chegada) }
        root.addView(chegada, lp())

        kmFinal = campo("KM final", "0")
        kmFinal.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        root.addView(kmFinal, lp())

        val salvar = Button(this).apply {
            text = "SALVAR VIAGEM"
            textSize = 16f
            setOnClickListener { salvarRegistro() }
        }
        root.addView(salvar, LinearLayout.LayoutParams(-1, 58).apply { setMargins(0, 12, 0, 24) })

        val h = TextView(this).apply {
            text = "REGISTROS SALVOS"
            textSize = 19f
            setTextColor(Color.rgb(25, 55, 90))
            setPadding(0, 8, 0, 10)
        }
        root.addView(h, lp())

        historico = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(8, 8, 8, 8)
        }
        root.addView(historico, lp())

        val limpar = Button(this).apply {
            text = "LIMPAR REGISTROS"
            setOnClickListener {
                prefs.edit().remove("registros").apply()
                atualizarHistorico()
            }
        }
        root.addView(limpar, lp())

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun campo(label: String, hint: String): EditText = EditText(this).apply {
        this.hint = hint
        textSize = 18f
        setSingleLine(true)
        setPadding(16, 8, 16, 8)
        accessibilityDelegate = null
        contentDescription = label
        setHintTextColor(Color.GRAY)
    }

    private fun escolherHora(campo: EditText) {
        val agora = Calendar.getInstance()
        TimePickerDialog(this, { _, h, m ->
            campo.setText(String.format("%02d:%02d", h, m))
        }, agora.get(Calendar.HOUR_OF_DAY), agora.get(Calendar.MINUTE), true).show()
    }

    private fun salvarRegistro() {
        val p = placa.text.toString().trim().uppercase()
        val s = saida.text.toString().trim()
        val c = chegada.text.toString().trim()
        val km = kmFinal.text.toString().trim()

        if (p.isEmpty() || s.isEmpty() || c.isEmpty() || km.isEmpty()) {
            android.widget.Toast.makeText(this, "Preencha todos os campos.", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val registros = JSONArray(prefs.getString("registros", "[]"))
        registros.put(JSONObject().apply {
            put("placa", p)
            put("saida", s)
            put("chegada", c)
            put("km", km)
        })
        prefs.edit().putString("registros", registros.toString()).apply()

        placa.text.clear()
        saida.text.clear()
        chegada.text.clear()
        kmFinal.text.clear()
        atualizarHistorico()
        android.widget.Toast.makeText(this, "Viagem salva.", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun atualizarHistorico() {
        if (!::historico.isInitialized) return
        val registros = JSONArray(prefs.getString("registros", "[]"))
        if (registros.length() == 0) {
            historico.text = "Nenhuma viagem registrada."
            return
        }
        val texto = StringBuilder()
        for (i in registros.length() - 1 downTo 0) {
            val r = registros.getJSONObject(i)
            texto.append("Placa: ").append(r.optString("placa"))
                .append("\nSaída: ").append(r.optString("saida"))
                .append("    Chegada: ").append(r.optString("chegada"))
                .append("\nKM final: ").append(r.optString("km"))
                .append("\n-------------------------\n")
        }
        historico.text = texto.toString()
    }

    private fun lp() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        setMargins(0, 0, 0, 12)
    }
}
