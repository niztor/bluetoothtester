package ar.com.nestor.bluetoothtester

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private var bondedFragment : BondedList? = BondedList.INSTANCE
    private var discoveryFragment: DiscoveryList? = DiscoveryList.INSTANCE
    private var connectFragment: ConnectDev? = ConnectDev.INSTANCE
    private var bluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            initialize()
        } else
            configure()

        if (bluetoothAdapter == null)
            initBluetooth()
    }

    fun initialize() {
        connectFragment = ConnectDev.newInstance(null)
        bondedFragment = BondedList.newInstance()
        discoveryFragment = DiscoveryList.newInstance()
        configure()
        discoveryFragment?.apply {
            launchFragment(this)
        }
        checkPermission()
    }

    fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION ),
                REQUEST_PERMISSION_FINE_LOCATION)
        }
    }

    private fun configure() {
        connectFragment?.apply {
            setListener(object : ConnectDev.FragmentListener {
                override fun onGetAdapter() {
                    initBluetooth()
                }
            })
        }
        bondedFragment?.apply {
            setListener(object : BondedList.FragmentListener {
                override fun onGetAdapter() {
                    initBluetooth()
                }

                override fun onConnectDevice(device: BluetoothDevice) {
                    connectDevice(device)
                }
            })
        }
        discoveryFragment?.apply {
            setListener(object : DiscoveryList.FragmentListener {
                override fun onGetAdapter() {
                    initBluetooth()
                }

                override fun onConnectDevice(device: BluetoothDevice) {
                    connectDevice(device)
                }
            })
        }
    }

    private fun launchFragment(fragment: Fragment, tag: String?= null, back: Boolean=false) {
        val fm = supportFragmentManager
        val current = fm.findFragmentById(R.id.main_frame_container)
        if (current == fragment)
            return

        if (current == null) {
            Log.d(TAG, "launchFragment: replace")
            fm.beginTransaction()
                .replace(R.id.main_frame_container, fragment, tag)
                .commit()
        } else {
            if (fragment.isDetached) {
                Log.d(TAG, "launchFragment: reattach")
                fm.beginTransaction()
                    .detach(current)
                    .attach(fragment)
                    .commit()
            } else {
                Log.d(TAG, "launchFragment: add")
                fm.beginTransaction()
                    .detach(current)
                    .add(R.id.main_frame_container, fragment, tag)
                    .also { if(back) it.addToBackStack(null) }
                    .commit()
            }
        }

    }


    private fun initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            showToast("El dispositivo no tiene Soporte para Bluetooth!")
            return
        }
        bluetoothAdapter?.let {
            if (!it.isEnabled) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, REQUEST_ENABLE_BT)
            }
            discoveryFragment?.setBluetoothAdapter(it)
            bondedFragment?.setBluetoothAdapter(it)
            connectFragment?.setBluetoothAdapter(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK)
                showToast("Bluetooth Enabled!")
        }
    }

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) : Boolean {
        Toast.makeText(this, message, duration).show()
        return false
    }

    fun connectDevice(device: BluetoothDevice) {
        connectFragment?.let {
            it.setDevice(device)
            launchFragment(it,back=true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.mm_item_boundedList -> {
                bondedFragment?.let {
                    launchFragment(it)
                }
                true
            }
            R.id.mm_item_discovery -> {
                discoveryFragment?.let {
                    launchFragment(it)
                }
                true
            }
            else -> false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionsResult starts")
        when (requestCode) {
            REQUEST_PERMISSION_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestsPermissionResult: permission granted")
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: permission refused")
                    showPersmissionSnakBar()
                }
            }
        }
        Log.d(TAG, "onRequestPermissionsResult ends")
    }

    private fun showPersmissionSnakBar() {
        Snackbar.make(main_frame_container, "Permission to FINE_LOCATION denied!", Snackbar.LENGTH_LONG)
            .setAction("GRANT ACCESS") {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Log.d(TAG, "Snackbar.onClick: calling request permission")
                    ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSION_FINE_LOCATION)
                } else {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", this.packageName, null)
                    this.startActivity(intent)
                }
                Log.d(TAG, "Snackbar.onClick: ends")
            }.show()
    }

    companion object {
        const val REQUEST_ENABLE_BT = 1000
        const val REQUEST_PERMISSION_FINE_LOCATION  = 2000

        const val FRAGMENT_TAG_DISCOVERY = "ar.com.nestor.bluetoothtest.tag.discovery"
        const val FRAGMENT_TAG_BONDED = "ar.com.nestor.bluetoothtest.tag.bonded"

    }

}