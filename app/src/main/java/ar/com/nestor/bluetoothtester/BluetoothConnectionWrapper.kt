package ar.com.nestor.bluetoothtester

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import java.io.IOException
import java.util.*

class BluetoothConnectionWrapper(
    private val adapter: BluetoothAdapter,
    private val device: BluetoothDevice,
    private val uuid: String,
    private var listener : WrapperListener? = null
) {
    private val TAG = "BluetoothConnectionWrap"

    @Volatile private var socket : BluetoothSocket? = null

    val connected: Boolean
        get()  = socket?.isConnected ?: false

    private var connect: ConnectThread? = null

    init {
        connect = ConnectThread().apply { start() }
    }

    fun write(message: String) {
        sender?.handler?.let {
            val txt = message
            val msg = it.obtainMessage()
            msg.what = MSG_WRITE
            msg.obj = txt.toByteArray()
            it.sendMessage(msg)
        }
    }

    fun close() {

        try {
            socket?.close()
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("HandlerLeak")
    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            this@BluetoothConnectionWrapper.handleMessage(msg)
        }
    }

    private var receiver : ReceiverThread? = null
    private var sender : SenderThread? = null

    private fun handleMessage(msg: Message) {
        when (msg.what) {
            MSG_ERROR -> {
                val errorMessage = msg.obj as? String ?: return
                listener?.onError(errorMessage)
            }
            MSG_CONNECTED -> {
                listener?.onConnect()
                Log.d(TAG, "handleMessage: Connected!")
                receiver = ReceiverThread().apply { start() }
                sender = SenderThread().apply { start() }
            }
            MSG_READ -> {
                val buffer = msg.obj as? ByteArray ?: return
                listener?.onRead(buffer)
                val s = String(buffer)
                Log.d(TAG, "handleMessage: Read! ${msg.arg1} = " + s)
            }
        }
    }

    private inner class SenderThread : HandlerThread("SendThread") {

        var handler : Handler? = null
            private set

        override fun onLooperPrepared() {
            init()
        }

        private fun init() {
            handler = object : Handler(looper) {
                override fun handleMessage(msg: Message) {
                    if (msg.what == MSG_WRITE) {
                        write(msg.obj as? ByteArray)
                    }
                }
            }
        }

        private fun write(byteArray: ByteArray?) {
            if (byteArray == null)
                return
            if (socket == null || !(socket?.isConnected ?: true))
                return
            val outputStream = socket?.outputStream ?: return
            try {
                outputStream.write(byteArray)
            } catch(e: IOException) {
                e.printStackTrace()
                Log.d(TAG, "write: fail ${e.message}")
                if (connected)
                    postErrorMessage("Error writing outpuStream")
                return
            }
            Log.d(TAG, "write: success!")
        }
    }

    private inner class ReceiverThread() : Thread() {

        override fun run() {
            read()
        }

        private fun read() {
            if (socket == null || !(socket?.isConnected ?: true))
                return
            val inputStream = socket?.inputStream ?: return
            val byteArray = ByteArray(1024)
            var readBytes: Int
            while(true) {
                try {
                    readBytes = inputStream.read(byteArray)
                } catch (e: IOException) {
                    e.printStackTrace()
                    if (connected)
                        postErrorMessage("Error Reading InputStream...")
                    return
                }
                val b = byteArray.copyOfRange(0, readBytes)
                val s = byteArrayToString(byteArray, readBytes) 
                Log.d(TAG, "ReceiveThread.run: Read bytes $readBytes $s")
                val msg = handler.obtainMessage(MSG_READ, readBytes, -1, b)
                handler.sendMessage(msg)
            }
        }

    }

    private inner class ConnectThread() : Thread() {

        override fun run() {
            adapter.cancelDiscovery()

            try {
                socket?.close()
            } catch(e: Exception) {
                e.printStackTrace()
            }

            socket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid))

            socket?.let {
                try {
                    it.connect()
                } catch (e: IOException) {
                    Log.d(TAG, "run: Error on connect ${e.message}")
                    e.printStackTrace()
                    postErrorMessage("Error Connecting Socket...")
                    return
                }

                if (it.isConnected)
                    handler.sendEmptyMessage(MSG_CONNECTED)

            }
        }

    }


    private fun postErrorMessage(message: String) {
        handler.let {
            val msg = it.obtainMessage()
            msg.what = MSG_ERROR
            msg.obj = message
            it.sendMessage(msg)
        }
    }

    fun setListener(listener: WrapperListener) {
        this.listener = listener
    }

    interface WrapperListener {
        fun onRead(buffer: ByteArray): Boolean
        fun onError(message: String)
        fun onConnect()
        fun onDisconnect()
    }

    companion object {

        const val MSG_CONNECTED = 100
        const val MSG_READ      = 200
        const val MSG_WRITE     = 300

        const val MSG_ERROR     = 900
    }
}