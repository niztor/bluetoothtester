package ar.com.nestor.bluetoothtester

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_bonded.view.*

class DeviceAdapter(
    private var list: List<BluetoothDevice>? ,
    private var listener : AdapterListener? = null
)
    : RecyclerView.Adapter<DeviceAdapter.ViewHolder>()  {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(device: BluetoothDevice) {
            itemView.apply {
                var s = ""
                if (device.uuids != null)
                    for (u in device.uuids)
                        s += u.uuid.toString() + "\n"

                bonded_item_uuids.text = if (s.isNotBlank()) "Uuids: \n" + s else ""
                bonded_item_address.text = "Address: " + device.address
                bonded_item_name.text = "Name: " +device.name
                bonded_item_class.text = "Class: " + bluetoothClassMajorToString(device.bluetoothClass.majorDeviceClass)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bonded, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        list?.let {
            val dev = it[position]
            holder.bind(it[position])
            holder.itemView.setOnLongClickListener {
                listener?.onSelectDevice(dev)
                true
            }
            holder.itemView.setOnClickListener {
                clickCount++
                if (clickCount < 3)
                    Toast.makeText(holder.itemView.context,
                        "Press and Hold to Select a Item Device", Toast.LENGTH_LONG).show()
            }
        }
    }


    fun setList(list: List<BluetoothDevice>?) {
        this.list = list
        notifyDataSetChanged()
    }

    fun setListener(listener: AdapterListener) {
        this.listener = listener
    }

    interface AdapterListener {
        fun onSelectDevice(device: BluetoothDevice)
    }

    companion object {
        private var clickCount: Int = 0
    }
}