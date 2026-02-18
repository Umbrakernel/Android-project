package com.example.android_project

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

class SocketsActivity : AppCompatActivity() {
    var log_tag : String = "MY_LOG_TAG"

    lateinit var tvSockets: TextView
    var textString : String = ""
    lateinit var handler: Handler
    lateinit var btnConnectToPC: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sockets)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        tvSockets = findViewById(R.id.tvSockets)
        btnConnectToPC = findViewById(R.id.btnConnectToPC)
        handler = Handler(Looper.getMainLooper())

        tvSockets.text = "Готово.\nСервер: tcp://*:5555"

        btnConnectToPC.setOnClickListener {
            val runnableClient = Runnable{startClient()}
            val threadClient = Thread(runnableClient)
            threadClient.start()
        }
    }

    fun startServer() {
        val context = ZMQ.context(1)
        val socket = ZContext().createSocket(SocketType.REP)
        socket.bind("tcp://*:5555")
        var counter: Int = 0

        while(true){
            counter++
            val requestBytes = socket.recv(0)
            val request = String(requestBytes, ZMQ.CHARSET)
            println("[SERVER] Received request: [$request]")

            handler.postDelayed({
                tvSockets.text = "Получено сообщение от клиента = $counter\n$request"
            }, 0)

            Thread.sleep(1000)

            val response = "Hello from Client!"
            socket.send(response.toByteArray(ZMQ.CHARSET), 0)
            println("[SERVER] Sent reply: [$response]")
        }

        socket.close();
        context.close();
    }

    fun startClient() {
        val context = ZMQ.context(1)
        val socket = ZContext().createSocket(SocketType.REQ)
        socket.connect("tcp://10.163.3.63:5555")

        val request = "Hello from Client!"
        for(i in 0..10){
            socket.send(request.toByteArray(ZMQ.CHARSET), 0)
            Log.d(log_tag, "[CLIENT] Send: $request")

            val reply = socket.recv(0)
            if (reply != null) {
                Log.d(log_tag, "[CLIENT] Received: " + String(reply, ZMQ.CHARSET))

                handler.postDelayed({
                    tvSockets.append("\nОтправлено: $request")
                    tvSockets.append("\nПолучено: " + String(reply, ZMQ.CHARSET) + "\n")
                }, 0)
            } else {
                Log.e(log_tag, "[CLIENT] Timeout or error receiving reply")
                handler.postDelayed({
                    tvSockets.append("\nОшибка получения ответа #$i\n")
                }, 0)
            }

            Thread.sleep(500)
        }
        socket.close()
        context.close()
    }

    override fun onResume() {
        super.onResume()
        val runnableServer = Runnable{startServer()}
        val threadServer = Thread(runnableServer)
        threadServer.start()
    }
}