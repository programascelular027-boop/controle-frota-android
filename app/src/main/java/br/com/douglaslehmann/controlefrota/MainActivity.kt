package br.com.douglaslehmann.controlefrota

import android.app.Activity
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("frota", MODE_PRIVATE) }

    private val navy = Color.rgb(17, 45, 74)
    private val blue = Color.rgb(32, 112, 190)
    private val green = Color.rgb(24, 157, 105)
    private val red = Color.rgb(190, 70, 70)
    private val orange = Color.rgb(230, 140, 40)
    private val bg = Color.rgb(244, 247, 250)
    private val dark = Color.rgb(35, 43, 52)
    private val gray = Color.rgb(105, 115, 125)

    private lateinit var plate: EditText
    private lateinit var dest: EditText
    private lateinit var out: EditText
    private lateinit var arrival: EditText
    private lateinit var km: EditText
    private lateinit var list: LinearLayout
    private lateinit var total: TextView
    private lateinit var pending: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (prefs.getBoolean("login", false)) showHome() else showLogin()
    }

    private fun showLogin() {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(28), dp(24), dp(28))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(22), dp(18), dp(22))
            background = box(navy, 24f)
        }
        header.addView(tv("🚗", 42f, Color.WHITE, true), lp(-1, 55))
        header.addView(tv("CONTROLE DE FROTA", 24f, Color.WHITE, true).apply { gravity = Gravity.CENTER }, lp(-1, 42))
        header.addView(tv("Acesso ao sistema", 14f, Color.rgb(205, 220, 235), false).apply { gravity = Gravity.CENTER }, lp(-1, 28))
        root.addView(header, lp(-1, 150))

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            background = box(Color.WHITE, 20f)
            elevation = dp(5).toFloat()
        }
        card.addView(tv("Bem-vindo", 22f, dark, true), lp(-1, 38))
        card.addView(tv("Entre para registrar as saídas", 13f, gray, false), lp(-1, 28))

        val user = field("Usuário", "Digite o usuário")
        val pass = field("Senha", "Digite a senha").apply { inputType = 0x81 }
        card.addView(label("Usuário"), lp(-1, 23))
        card.addView(user, lp(-1, 52))
        card.addView(label("Senha"), lp(-1, 23))
        card.addView(pass, lp(-1, 52).apply { setMargins(0, 0, 0, dp(15)) })

        val enter = button("ENTRAR", blue)
        card.addView(enter, lp(-1, 52))
        root.addView(card, lp(-1, 300).apply { setMargins(0, dp(18), 0, 0) })

        enter.setOnClickListener {
            if (user.text.toString().trim() == "admin" && pass.text.toString() == "admin123") {
                prefs.edit().putBoolean("login", true).apply()
                showHome()
            } else {
                Toast.makeText(this, "Usuário ou senha inválidos", Toast.LENGTH_SHORT).show()
            }
        }

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun showHome() {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(12), dp(15), dp(24))
        }

        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(12), dp(12), dp(12))
            background = box(navy, 20f)
        }
        val headerText = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        headerText.addView(tv("Controle de Frota", 22f, Color.WHITE, true), lp(-1, 32))
        headerText.addView(tv("Registro de saídas", 13f, Color.rgb(205, 220, 235), false), lp(-1, 25))
        header.addView(headerText, LinearLayout.LayoutParams(0, dp(60), 1f))
        val logout = button("SAIR", Color.rgb(60, 82, 105))
        header.addView(logout, lp(62, 42))
        logout.setOnClickListener {
            prefs.edit().putBoolean("login", false).apply()
            showLogin()
        }
        root.addView(header, lp(-1, 82))

        val stats = LinearLayout(this)
        val totalCard = stat("VIAGENS", blue)
        val pendingCard = stat("PENDENTES", orange)
        total = totalCard.second
        pending = pendingCard.second
        stats.addView(totalCard.first, LinearLayout.LayoutParams(0, dp(86), 1f).apply { setMargins(0, dp(12), dp(5), 0) })
        stats.addView(pendingCard.first, LinearLayout.LayoutParams(0, dp(86), 1f).apply { setMargins(dp(5), dp(12), 0, 0) })
        root.addView(stats, lp(-1, 98))

        root.addView(tv("NOVA SAÍDA", 18f, navy, true), lp(-1, 40))

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(17), dp(16), dp(17), dp(17))
            background = box(Color.WHITE, 20f)
            elevation = dp(2).toFloat()
        }

        plate = field("Placa", "Ex.: ABC1D23")
        dest = field("Destino", "Ex.: Belo Horizonte")
        out = field("Saída", "Selecione o horário")
        arrival = field("Chegada", "Selecione o horário")
        km = field("KM final", "Ex.: 125430").apply { inputType = 2 }

        addField(form, "Placa do veículo", plate)
        addField(form, "Destino", dest)
        addField(form, "Hora de saída", out)
        addField(form, "Hora de chegada", arrival)
        addField(form, "KM final", km)

        out.isFocusable = false
        arrival.isFocusable = false
        out.setOnClickListener { chooseTime(out) }
        arrival.setOnClickListener { chooseTime(arrival) }

        val row = LinearLayout(this)
        val saveButton = button("SALVAR", blue)
        val sendButton = button("ENVIAR", green)
        row.addView(saveButton, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(0, dp(4), dp(5), 0) })
        row.addView(sendButton, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(5), dp(4), 0, 0) })
        form.addView(row, lp(-1, 56))
        root.addView(form, lp(-1, -2))

        root.addView(tv("HISTÓRICO DE VIAGENS", 18f, navy, true), lp(-1, 52))
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(list, lp(-1, -2))

        val clearButton = button("LIMPAR HISTÓRICO", red)
        root.addView(clearButton, lp(-1, 50).apply { setMargins(0, dp(8), 0, 0) })
        clearButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Limpar histórico")
                .setMessage("Apagar todos os registros deste aparelho?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Apagar") { _, _ ->
                    prefs.edit().remove("items").apply()
                    history()
                }
                .show()
        }

        root.addView(tv("Desenvolvido por Douglas Lehmann", 11f, Color.GRAY, false).apply { gravity = Gravity.END }, lp(-1, 30))

        saveButton.setOnClickListener { saveTrip(false) }
        sendButton.setOnClickListener { saveTrip(true) }

        scroll.addView(root)
        setContentView(scroll)
        history()
    }

    private fun saveTrip(sendNow: Boolean) {
        val p = plate.text.toString().trim().uppercase()
        val d = dest.text.toString().trim()
        val s = out.text.toString().trim()
        val a = arrival.text.toString().trim()
        val k = km.text.toString().trim()

        if (listOf(p, d, s, a, k).any { it.isEmpty() }) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val items = JSONArray(prefs.getString("items", "[]"))
        val item = JSONObject().apply {
            put("placa", p)
            put("destino", d)
            put("saida", s)
            put("chegada", a)
            put("km", k)
            put("enviado", sendNow)
        }
        items.put(item)
        prefs.edit().putString("items", items.toString()).apply()

        clearFields()
        history()

        if (sendNow) share(item)
        else Toast.makeText(this, "Viagem salva no histórico", Toast.LENGTH_SHORT).show()
    }

    private fun share(item: JSONObject) {
        val text = "CONTROLE DE SAÍDA\n\n" +
            "Placa: ${item.optString("placa")}\n" +
            "Destino: ${item.optString("destino")}\n" +
            "Saída: ${item.optString("saida")}\n" +
            "Chegada: ${item.optString("chegada")}\n" +
            "KM final: ${item.optString("km")}"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Enviar viagem"))
    }

    private fun history() {
        if (!::list.isInitialized) return
        list.removeAllViews()

        val items = JSONArray(prefs.getString("items", "[]"))
        total.text = items.length().toString()

        var pendingCount = 0
        for (i in 0 until items.length()) {
            if (!items.getJSONObject(i).optBoolean("enviado")) pendingCount++
        }
        pending.text = pendingCount.toString()

        if (items.length() == 0) {
            list.addView(
                tv("📋  Nenhuma viagem registrada.", 15f, gray, false).apply {
                    setPadding(dp(15), dp(22), dp(15), dp(22))
                    background = box(Color.WHITE, 18f)
                },
                lp(-1, 70)
            )
            return
        }

        for (i in items.length() - 1 downTo 0) {
            val item = items.getJSONObject(i)
            val sent = item.optBoolean("enviado")

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(15), dp(12), dp(15), dp(12))
                background = box(Color.WHITE, 18f)
                elevation = dp(2).toFloat()
            }

            val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
            top.addView(tv(item.optString("placa"), 18f, navy, true), LinearLayout.LayoutParams(0, dp(30), 1f))
            val statusColor = if (sent) green else orange
            val statusText = if (sent) "✓ ENVIADO" else "● PENDENTE"
            top.addView(tv(statusText, 11f, statusColor, true), lp(90, 30))
            card.addView(top, lp(-1, 30))

            card.addView(tv("📍 ${item.optString("destino")}", 15f, dark, false), lp(-1, 30))
            card.addView(tv("🕐 ${item.optString("saida")}  →  ${item.optString("chegada")}   •   KM ${item.optString("km")}", 13f, gray, false), lp(-1, 28))

            if (!sent) {
                val resend = TextView(this).apply {
                    text = "Enviar agora"
                    textSize = 13f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(blue)
                    setPadding(0, dp(6), 0, 0)
                    setOnClickListener { share(item) }
                }
                card.addView(resend, lp(-1, 30))
            }

            list.addView(card, lp(-1, -2).apply { setMargins(0, 0, 0, dp(9)) })
        }
    }

    private fun addField(container: LinearLayout, label: String, editText: EditText) {
        container.addView(this.label(label), lp(-1, 23))
        container.addView(editText, lp(-1, 50).apply { setMargins(0, 0, 0, dp(8)) })
    }

    private fun clearFields() {
        plate.text.clear()
        dest.text.clear()
        out.text.clear()
        arrival.text.clear()
        km.text.clear()
    }

    private fun chooseTime(editText: EditText) {
        val now = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute -> editText.setText(String.format("%02d:%02d", hour, minute)) },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun field(label: String, hintText: String) = EditText(this).apply {
        hint = hintText
        contentDescription = label
        textSize = 16f
        setSingleLine(true)
        setPadding(dp(13), 0, dp(13), 0)
        setTextColor(dark)
        setHintTextColor(Color.rgb(150, 160, 170))
        background = box(Color.rgb(247, 249, 251), 12f)
    }

    private fun label(text: String) = tv(text, 12f, dark, true)

    private fun button(textValue: String, color: Int) = Button(this).apply {
        text = textValue
        textSize = 14f
        isAllCaps = false
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(Color.WHITE)
        background = box(color, 13f)
        stateListAnimator = null
    }

    private fun stat(title: String, color: Int): Pair<View, TextView> {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(8), dp(14), dp(6))
            background = box(Color.WHITE, 17f)
        }
        wrapper.addView(tv(title, 10f, gray, true), lp(-1, 22))
        val number = tv("0", 25f, color, true)
        wrapper.addView(number, lp(-1, 36))
        return Pair(wrapper, number)
    }

    private fun tv(textValue: String, size: Float, color: Int, bold: Boolean) = TextView(this).apply {
        text = textValue
        textSize = size
        setTextColor(color)
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }

    private fun box(color: Int, radius: Float) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun dp(value: Float) = (value * resources.displayMetrics.density).toInt()

    private fun lp(width: Int, height: Int): LinearLayout.LayoutParams {
        val w = if (width < 0) ViewGroup.LayoutParams.MATCH_PARENT else dp(width)
        val h = if (height < 0) ViewGroup.LayoutParams.WRAP_CONTENT else dp(height)
        return LinearLayout.LayoutParams(w, h)
    }
}
