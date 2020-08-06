package ar.com.nestor.bluetoothtester

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_bonded_list.*


class BondedList : Fragment() {
    private val TAG = "BondedList"

    private var bondedDevices: Set<BluetoothDevice>? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var deviceAdapter: DeviceAdapter? = null
    private var listener: FragmentListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (listener == null)
            listener = context as? FragmentListener
        Log.d(TAG, "onAttach: Bonded!")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach: Bonded!")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bonded_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (bluetoothAdapter == null)
            listener?.onGetAdapter()
        initRecycler()
        initButtonts()
    }

    private fun initButtonts() {
        bonded_list_button1?.setOnClickListener {
            getBondedList()
        }
    }

    private fun initRecycler() {
        deviceAdapter = DeviceAdapter(bondedDevices?.map { it },
            object : DeviceAdapter.AdapterListener {
                override fun onSelectDevice(device: BluetoothDevice) {
                    bluetoothAdapter?.cancelDiscovery()
                    listener?.onConnectDevice(device)
                }
            })
        bonded_list_recycler.layoutManager = LinearLayoutManager(view?.context)
        bonded_list_recycler.adapter = deviceAdapter
    }

    private fun updateRecycler() {
        deviceAdapter?.setList(bondedDevices?.map { it })
    }

    fun setBluetoothAdapter(adapter: BluetoothAdapter) {
        bluetoothAdapter = adapter
        getBondedList()
    }

    private fun getBondedList() {
        bluetoothAdapter?.let {
            bondedDevices = it.bondedDevices
            updateRecycler()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ok?")
        INSTANCE = null
    }

    fun setListener(listener: FragmentListener) {
        this.listener = listener
    }

    interface FragmentListener {
        fun onGetAdapter()
        fun onConnectDevice(device: BluetoothDevice)
    }

    companion object {
        var INSTANCE : BondedList? = null
        @JvmStatic
        fun newInstance() =
            BondedList().apply { INSTANCE = this }
    }
}