package ar.com.nestor.bluetoothtester

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_discovery_list.*


class DiscoveryList : Fragment() {

    private val TAG = "DiscoveryList"

    private var listener: FragmentListener? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var foundDevices = mutableListOf<BluetoothDevice>()
    private var deviceAdapter : DeviceAdapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach: Discovery!")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach: Discovery!")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        retainInstance = true
    }

    override fun onStart() {
        super.onStart()
        startReceivers()
    }

    override fun onStop() {
        super.onStop()
        stopReceiver()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discovery_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (bluetoothAdapter == null)
            listener?.onGetAdapter()
        initRecycler()
        discovery_list_button1.setOnClickListener {
            startDiscovery()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ok")
        INSTANCE = null
    }

    private fun initRecycler() {
        deviceAdapter = DeviceAdapter(foundDevices, object : DeviceAdapter.AdapterListener {
            override fun onSelectDevice(device: BluetoothDevice) {
                bluetoothAdapter?.cancelDiscovery()
                listener?.onConnectDevice(device)
            }
        })
        discovery_list_recycler.adapter = deviceAdapter
        discovery_list_recycler.layoutManager = LinearLayoutManager(view?.context)
    }

    private fun updateRecycler() {
        deviceAdapter?.setList(foundDevices)
    }

    private fun startDiscovery() {
        bluetoothAdapter?.let {
            foundDevices.clear()
            updateRecycler()
            if (it.startDiscovery()) {
                showToast("Discovery Starts..")
            } else {
                showToast("Could not start discovery, check for permissions!")
            }
        }
    }

    private fun startReceivers() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        activity?.registerReceiver(receiver, filter)
    }

    private fun stopReceiver() {
        activity?.unregisterReceiver(receiver)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            when(action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    showToast("Discovery Started")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    showToast("Discovery Finished")
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device : BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        foundDevices.add(it)
                        updateRecycler()
                        showToast("Found ${it.name}")
                    }
                }

            }
        }
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) : Boolean {
        activity?.let {
            Toast.makeText(it, message, duration).show()
        }
        return false
    }

    fun setBluetoothAdapter(adapter: BluetoothAdapter) {
        bluetoothAdapter = adapter
    }

    fun setListener(listener: FragmentListener) {
        this.listener = listener
    }

    interface FragmentListener {
        fun onGetAdapter()
        fun onConnectDevice(device: BluetoothDevice)
    }

    companion object {
        var INSTANCE: DiscoveryList? = null

        @JvmStatic
        fun newInstance() =
            DiscoveryList().apply {
                INSTANCE = this
                arguments = Bundle().apply {
                    //
                }
            }
    }
}