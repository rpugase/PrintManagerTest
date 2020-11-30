package com.example.printermanagertest

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.dantsu.escposprinter.EscPosCharsetEncoding
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.tcp.TcpConnection
import java.net.NetworkInterface
import java.net.Socket
import java.nio.charset.Charset
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    var ip = "172.21.10.164"
    val port = 9100

    var escPos: EscPosPrinter? = null
    val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        findViewById<Button>(R.id.btnPrint).setOnClickListener {
//            val text = findViewById<EditText>(R.id.etText).text.toString()
            print(text2)
        }
    }

    override fun onStart() {
        super.onStart()
        executor.submit {
            escPos = EscPosPrinter(TcpConnection(ip, port) { Log.i("logger", "OnPaperEnd") },
                    300, 48f, 32, EscPosCharsetEncoding("csPC862LatinHebrew", 0x0a))
        }
    }

    override fun onStop() {
        executor.submit { escPos?.disconnectPrinter() }
        super.onStop()
    }

    fun print(text: String) {
        executor.submit {
//            escPos = EscPosPrinter(TcpConnection(ip, port), 203, 48f, 32)
            escPos?.printFormattedTextAndCut(text)
//            escPos?.disconnectPrinter()
        }
    }

    val text = "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
            "[L]\n" +
            "[C]================================\n" +
            "[L]\n" +
            "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99e\n" +
            "[L]  + Size : S\n" +
            "[L]\n" +
            "[L]<b>AWESOME HAT</b>[R]24.99e\n" +
            "[L]  + Size : 57/58\n" +
            "[L]\n" +
            "[C]--------------------------------\n" +
            "[R]TOTAL PRICE :[R]34.98e\n" +
            "[R]TAX :[R]4.23e\n" +
            "[L]\n" +
            "[C]================================\n" +
            "[L]\n" +
            "[L]<font size='tall'>Customer :</font>\n" +
            "[L]Raymond DUPONT\n" +
            "[L]5 rue des girafes\n" +
            "[L]31547 PERPETES\n" +
            "[L]Tel : +33801201456\n" +
            "[L]\n" +
            "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
            "[C]<qrcode size='20'>http://www.developpeur-web.dantsu.com/</qrcode>"

    val text2 = "Hello! 12345 מנהל"
}
