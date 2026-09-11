package br.com.douglaslehmann.controlefrota

import android.app.*
import android.content.*
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("frota", 0) }
    private val navy=Color.rgb(17,45,74); private val blue=Color.rgb(32,112,190); private val green=Color.rgb(24,157,105); private val bg=Color.rgb(244,247,250); private val dark=Color.rgb(35,43,52); private val gray=Color.rgb(105,115,125)
    private lateinit var plate:EditText; private lateinit var dest:EditText; private lateinit var out:EditText; private lateinit var arrival:EditText; private lateinit var km:EditText; private lateinit var list:LinearLayout; private lateinit var total:TextView; private lateinit var pending:TextView
    override fun onCreate(b:Bundle?){super.onCreate(b); if(prefs.getBoolean("login",false)) home() else login()}
    private fun login(){
        val r=LinearLayout(this); r.orientation=LinearLayout.VERTICAL; r.gravity=Gravity.CENTER; r.setPadding(dp(25),dp(25),dp(25),dp(25)); r.setBackgroundColor(bg)
        val head=LinearLayout(this); head.orientation=LinearLayout.VERTICAL; head.gravity=Gravity.CENTER; head.setPadding(dp(20),dp(24),dp(20),dp(24)); head.background=box(navy,24f)
        head.addView(tv("🚗",42f,Color.WHITE,true), lp(-1,58)); head.addView(tv("CONTROLE DE FROTA",24f,Color.WHITE,true),lp(-1,45)); head.addView(tv("Acesso ao sistema",14f,Color.rgb(205,220,235),false),lp(-1,30)); r.addView(head,lp(-1,160))
        val c=LinearLayout(this); c.orientation=LinearLayout.VERTICAL; c.setPadding(dp(20),dp(20),dp(20),dp(20)); c.background=box(Color.WHITE,20f); c.elevation=dp(5).toFloat()
        c.addView(tv("Bem-vindo",22f,dark,true),lp(-1,38)); c.addView(tv("Entre para registrar as saídas",13f,gray,false),lp(-1,28))
        val u=field("Usuário","Digite o usuário"); val p=field("Senha","Digite a senha"); p.inputType=0x81
        c.addView(label("Usuário"),lp(-1,24)); c.addView(u,lp(-1,52)); c.addView(label("Senha"),lp(-1,24)); c.addView(p,lp(-1,52).also{it.setMargins(0,0,0,15)})
        val enter=button("ENTRAR",blue); c.addView(enter,lp(-1,52)); r.addView(c,lp(-1,315).also{it.setMargins(0,dp(20),0,0)})
        enter.setOnClickListener{if(u.text.toString().trim()=="admin" && p.text.toString()=="admin123"){prefs.edit().putBoolean("login",true).apply();home()}else Toast.makeText(this,"Usuário ou senha inválidos",0).show()}; setContentView(r)
    }
    private fun home(){
        val s=ScrollView(this); s.setBackgroundColor(bg); val r=LinearLayout(this); r.orientation=LinearLayout.VERTICAL; r.setPadding(dp(15),dp(13),dp(15),dp(25))
        val h=LinearLayout(this); h.gravity=Gravity.CENTER_VERTICAL; h.setPadding(dp(18),dp(13),dp(12),dp(13)); h.background=box(navy,20f); val ht=LinearLayout(this); ht.orientation=LinearLayout.VERTICAL; ht.addView(tv("Controle de Frota",22f,Color.WHITE,true),lp(-1,32)); ht.addView(tv("Registro de saídas",13f,Color.rgb(205,220,235),false),lp(-1,25)); h.addView(ht,new LinearLayout.LayoutParams(0,60,1f)); val logout=button("SAIR",Color.rgb(60,82,105)); h.addView(logout,lp(62,42)); logout.setOnClickListener{prefs.edit().putBoolean("login",false).apply();login()}; r.addView(h,lp(-1,82))
        val stats=LinearLayout(this); total=stat("VIAGENS",blue); pending=stat("PENDENTES",Color.rgb(230,140,40)); stats.addView(total.parent as View,LinearLayout.LayoutParams(0,86,1f).also{it.setMargins(0,dp(12),dp(5),0)}); stats.addView(pending.parent as View,LinearLayout.LayoutParams(0,86,1f).also{it.setMargins(dp(5),dp(12),0,0)}); r.addView(stats,lp(-1,98))
        r.addView(tv("NOVA SAÍDA",18f,navy,true),lp(-1,40))
        val f=LinearLayout(this); f.orientation=LinearLayout.VERTICAL; f.setPadding(dp(17),dp(16),dp(17),dp(17)); f.background=box(Color.WHITE,20f); f.elevation=dp(2).toFloat()
        plate=field("Placa","Ex.: ABC1D23"); dest=field("Destino","Ex.: Belo Horizonte"); out=field("Saída","Selecione o horário"); arrival=field("Chegada","Selecione o horário"); km=field("KM final","Ex.: 125430"); km.inputType=2
        add(f,"Placa do veículo",plate); add(f,"Destino",dest); add(f,"Hora de saída",out); add(f,"Hora de chegada",arrival); add(f,"KM final",km)
        out.isFocusable=false; arrival.isFocusable=false; out.setOnClickListener{time(out)}; arrival.setOnClickListener{time(arrival)}
        val row=LinearLayout(this); val save=button("SALVAR",blue); val send=button("ENVIAR",green); row.addView(save,LinearLayout.LayoutParams(0,52,1f).also{it.setMargins(0,dp(4),dp(5),0)}); row.addView(send,LinearLayout.LayoutParams(0,52,1f).also{it.setMargins(dp(5),dp(4),0,0)}); f.addView(row,lp(-1,56)); r.addView(f,lp(-1,-2))
        r.addView(tv("HISTÓRICO DE VIAGENS",18f,navy,true),lp(-1,52).also{it.setPadding(0,dp(18),0,0)}); list=LinearLayout(this); list.orientation=LinearLayout.VERTICAL; r.addView(list,lp(-1,-2))
        val clear=button("LIMPAR HISTÓRICO",Color.rgb(185,70,70)); r.addView(clear,lp(-1,50).also{it.setMargins(0,dp(8),0,0)}); clear.setOnClickListener{prefs.edit().remove("items").apply();history()}
        r.addView(tv("Desenvolvido por Douglas Lehmann",11f,Color.GRAY,false).also{it.gravity=Gravity.RIGHT},lp(-1,32).also{it.setMargins(0,dp(10),0,0)})
        save.setOnClickListener{save(false)}; send.setOnClickListener{save(true)}; s.addView(r); setContentView(s); history()
    }
    private fun save(send:Boolean){val a=plate.text.toString().trim().uppercase(); val b=dest.text.toString().trim(); val c=out.text.toString(); val d=arrival.text.toString(); val e=km.text.toString().trim(); if(listOf(a,b,c,d,e).any{it.isEmpty()}){Toast.makeText(this,"Preencha todos os campos",0).show();return}; val j=JSONArray(prefs.getString("items","[]")); j.put(JSONObject().apply{put("placa",a);put("destino",b);put("saida",c);put("chegada",d);put("km",e);put("enviado",send)}); prefs.edit().putString("items",j.toString()).apply(); clearFields(); history(); if(send) shareLast(j.getJSONObject(j.length()-1)) else Toast.makeText(this,"Viagem salva",0).show()}
    private fun shareLast(j:JSONObject){val text="CONTROLE DE SAÍDA\nPlaca: ${j.optString("placa")}\nDestino: ${j.optString("destino")}\nSaída: ${j.optString("saida")}\nChegada: ${j.optString("chegada")}\nKM final: ${j.optString("km")}"; startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,text)},"Enviar viagem"))}
    private fun history(){if(!::list.isInitialized)return; list.removeAllViews(); val j=JSONArray(prefs.getString("items","[]")); total.text=j.length().toString(); var p=0; for(i in 0 until j.length())if(!j.getJSONObject(i).optBoolean("enviado"))p++; pending.text=p.toString(); if(j.length()==0){list.addView(tv("📋  Nenhuma viagem registrada.",15f,gray,false).also{it.setPadding(dp(15),dp(22),dp(15),dp(22));it.background=box(Color.WHITE,18f)},lp(-1,70));return}; for(i in j.length()-1 downTo 0){val x=j.getJSONObject(i); val sent=x.optBoolean("enviado"); val c=LinearLayout(this); c.orientation=LinearLayout.VERTICAL;c.setPadding(dp(15),dp(12),dp(15),dp(12));c.background=box(Color.WHITE,18f);c.elevation=dp(2).toFloat(); val top=LinearLayout(this); top.gravity=Gravity.CENTER_VERTICAL; top.addView(tv(x.optString("placa"),18f,navy,true),LinearLayout.LayoutParams(0,30,1f)); top.addView(tv(if(sent)"✓ ENVIADO" else "● PENDENTE",11f,if(sent)green else Color.rgb(225,140,35),true),lp(90,30));c.addView(top,lp(-1,30));c.addView(tv("📍 ${x.optString("destino")}",15f,dark,false),lp(-1,30));c.addView(tv("🕐 ${x.optString("saida")}  →  ${x.optString("chegada")}   •   KM ${x.optString("km")}",13f,gray,false),lp(-1,28)); if(!sent){val b=TextView(this);b.text="Enviar agora";b.textSize=13f;b.typeface=Typeface.DEFAULT_BOLD;b.setTextColor(blue);b.setOnClickListener{shareLast(x)};c.addView(b,lp(-1,32))};list.addView(c,lp(-1,-2).also{it.setMargins(0,0,0,dp(9))})}}
    private fun add(c:LinearLayout,l:String,e:EditText){c.addView(label(l),lp(-1,23));c.addView(e,lp(-1,50).also{it.setMargins(0,0,0,8)})}
    private fun clearFields(){plate.text.clear();dest.text.clear();out.text.clear();arrival.text.clear();km.text.clear()}
    private fun time(e:EditText){val n=Calendar.getInstance();TimePickerDialog(this,{_,h,m->e.setText(String.format("%02d:%02d",h,m))},n.get(11),n.get(12),true).show()}
    private fun field(label:String,hint:String)=EditText(this).apply{this.hint=hint;this.contentDescription=label;textSize=16f;setSingleLine(true);setPadding(dp(13),0,dp(13),0);setTextColor(dark);setHintTextColor(Color.rgb(150,160,170));background=box(Color.rgb(247,249,251),12f)}
    private fun label(t:String)=tv(t,12f,dark,true)
    private fun button(t:String,c:Int)=Button(this).apply{text=t;textSize=14f;isAllCaps=false;typeface=Typeface.DEFAULT_BOLD;setTextColor(Color.WHITE);background=box(c,13f);stateListAnimator=null}
    private fun stat(t:String,c:Int):TextView{val w=LinearLayout(this);w.orientation=LinearLayout.VERTICAL;w.setPadding(dp(14),dp(8),dp(14),dp(6));w.background=box(Color.WHITE,17f);val l=tv(t,10f,gray,true);w.addView(l,lp(-1,22));val n=tv("0",25f,c,true);w.addView(n,lp(-1,36));n.tag="stat";n.parent?.let{};return n}
    private fun tv(t:String,z:Float,c:Int,b:Boolean)=TextView(this).apply{text=t;textSize=z;setTextColor(c);if(b)typeface=Typeface.DEFAULT_BOLD}
    private fun box(c:Int,r:Float)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat()}
    private fun dp(v:Int)= (v*resources.displayMetrics.density).toInt(); private fun dp(v:Float)= (v*resources.displayMetrics.density).toInt()
    private fun lp(w:Int,h:Int)=LinearLayout.LayoutParams(if(w<0)-1 else dp(w),if(h<0)-1 else dp(h))
}
