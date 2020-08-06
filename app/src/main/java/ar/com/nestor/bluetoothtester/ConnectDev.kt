package ar.com.nestor.bluetoothtester

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.view.isEmpty
import kotlinx.android.synthetic.main.fragment_connect_dev.*
import java.io.IOException


class ConnectDev : Fragment() {
    private val TAG = "ConnectDev"

    private var listener: FragmentListener? = null
    private var adapter: BluetoothAdapter? = null
    private var device: BluetoothDevice? = null

    private var connectionWrapper: BluetoothConnectionWrapper? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (listener == null)
            listener = context as? FragmentListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (device == null)
                device = it.getParcelable(ARG_DEVICE) as? BluetoothDevice
        }
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_connect_dev, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (adapter == null)
            listener?.onGetAdapter()
        updateDeviceInfo()
        initButtons()
    }

    override fun onStart() {
        super.onStart()
        startReceivers()
    }

    override fun onStop() {
        super.onStop()
        stopReceiver()
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: what?")
        cancel()
        INSTANCE = null
    }

    private fun initButtons() {
        connect_dev_button_connect?.setOnClickListener {
            connectDevice()
        }
        connect_dev_button_send?.setOnClickListener {
            sendText()
        }
        connect_dev_button_sdp?.setOnClickListener {
            device?.fetchUuidsWithSdp()
        }
    }

    private fun initUuids() {
        val dev = device ?: return
        val rg = connect_dev_uuids ?: return
        rg.removeAllViews()
        val uuids = dev.uuids
        if (uuids == null || uuids.isEmpty()) {
            val rb = RadioButton(rg.context)
            rb.tag = -1
            rb.text = "None"
            rg.addView(rb)
            return
        }
        for ((i,u) in uuids.withIndex()) {
            val rb = RadioButton(view?.context)
            rb.id = View.generateViewId()
            rb.tag = i
            rb.text = u.toString()
            rg.addView(rb)
        }
    }

    private fun connectDevice() {
        val adapter = adapter
        if (adapter == null) {
            showToast("No Bluetooth Adapter!")
            return
        }
        device?.let {
            if (connect_dev_uuids == null)
                return
            if (connect_dev_uuids.isEmpty()) {
                showToast("No Uuid Found, please Search SDP...")
                initUuids()
                return
            }
            val selectedId = connect_dev_uuids.checkedRadioButtonId
            if (selectedId < 0) {
                showToast("Please select UUID")
                return
            }
            val radiobutton = connect_dev_uuids.findViewById(selectedId) as? RadioButton
            if (radiobutton == null) {
                showToast("Button Option not found ?")
                return
            }
            val uuidIndex : Int = radiobutton.tag as? Int ?: return

            if (uuidIndex < 0) {
                showToast("Please Select UUID!!")
                return
            }

            val uuid = it.uuids?.get(uuidIndex)?.toString()
            if (uuid != null) {
                connectionWrapper?.close()
                connectionWrapper = BluetoothConnectionWrapper(adapter, it, uuid, wrapperListener)
                showToast("Connecting to ($uuidIndex) \n $uuid")
            } else {
                showToast("Uuid not found!? Try SDP again...")
            }
        }
    }

    private fun updateDeviceInfo() {
        device?.let {
            val text =
                "Name: " + it.name + "\n" +
                "Address: " + it.address + "\n" +
                "Class: " + bluetoothClassMajorToString(it.bluetoothClass.majorDeviceClass)
            Log.d(TAG, "updateDeviceInfo: " + text)
            connect_dev_info?.text = text
        }
        initUuids()
    }

    fun setListener(listener: FragmentListener) {
        this.listener = listener
    }

    fun setDevice(device: BluetoothDevice) {
        this.device = device
        updateDeviceInfo()
        if (device.uuids == null || device.uuids?.size == 0) {
            Log.d(TAG, "setDevice: fetchUuids")
            device.fetchUuidsWithSdp()
        }
    }

    fun setBluetoothAdapter(adapter: BluetoothAdapter) {
        this.adapter = adapter
    }

    interface FragmentListener {
        fun onGetAdapter()
    }


    private fun sendText() {
        val txt = connect_dev_sendtext.text.toString() +
                if(connect_dev_newline.isChecked) "\n" else ""
        connectionWrapper?.write(txt)
    }

    private var wrapperListener = object : BluetoothConnectionWrapper.WrapperListener {

        override fun onRead(buffer: ByteArray): Boolean {
            activity ?: return false
            Toast.makeText(activity, "Read Message !", Toast.LENGTH_SHORT).show()
            val s = String(buffer)
            Log.d(TAG, "handleMessage: Read! ${s.length} = " + s)
            connect_device_recevied?.let { it ->
                it.text = it.text.toString() + s
            }
            connect_dev_scroll_recv?.fullScroll(View.FOCUS_DOWN)
            return true
        }

        override fun onError(message: String) {
            activity?.let {
                Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
            }
        }

        override fun onConnect() {
            activity ?: return
            Toast.makeText(activity, "Connected!", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "handleMessage: Connected!")
        }

        override fun onDisconnect() {
            Log.d(TAG, "handleMessage: Disconnected!")
        }
    }



    private fun startReceivers() {
        val filter = IntentFilter(BluetoothDevice.ACTION_UUID)
        activity?.registerReceiver(broadcastRecv, filter)
    }

    private fun stopReceiver() {
        activity?.unregisterReceiver(broadcastRecv)
    }

    private val broadcastRecv = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            when(action) {
                BluetoothDevice.ACTION_UUID -> {
                    val device : BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val uuids : Array<Parcelable>? = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)
                    if (uuids == null)
                        return
                    for((i, u) in uuids.withIndex()) {
                        (u as? ParcelUuid)?.let {
                            Log.d(TAG, "onReceive: [$i] uuid" + it.uuid.toString())
                        }
                    }
                    initUuids()
                }

            }
        }
    }

    private fun cancel() {
        try {
            connectionWrapper?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the client socket", e)
        }
    }

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) : Boolean {
        Toast.makeText(activity, message, duration).show()
        return false
    }


    companion object {

        const val ARG_DEVICE = "ar.com.nestor.bluetoothtester.connect.device"
        var INSTANCE : ConnectDev? = null

        @JvmStatic
        fun newInstance(device: BluetoothDevice?) =
            ConnectDev().apply {
                INSTANCE = this
                arguments = Bundle().apply {
                    putParcelable(ARG_DEVICE, device)
                }
            }
    }
}