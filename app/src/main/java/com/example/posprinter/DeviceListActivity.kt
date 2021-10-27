package com.example.posprinter

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.posprinter.databinding.ActivityDeviceListBinding

class DeviceListActivity : AppCompatActivity() {

    lateinit var binding            : ActivityDeviceListBinding
    lateinit var mBluetoothAdapter  : BluetoothAdapter
    private var mPairedDevicesArrayAdapter : ArrayAdapter<String>? = null
    private val TAG = "DeviceListActivity"

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setResult(Activity.RESULT_CANCELED)
        mPairedDevicesArrayAdapter = ArrayAdapter(this,R.layout.device_name)

        binding.pairedDevices.adapter               = mPairedDevicesArrayAdapter
        binding.pairedDevices.onItemClickListener   = mDeviceClickListener

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val mPairedDevices = mBluetoothAdapter.bondedDevices
        if(mPairedDevices.size > 0){
            /* List of all paired devices */
            binding.titlePairedDevices.visibility = View.VISIBLE
            for(mDevice in mPairedDevices){
                mPairedDevicesArrayAdapter!!.add(mDevice.name + "\n" + mDevice.address)
            }
        }else{
            /* No paired device */
            mPairedDevicesArrayAdapter!!.add("None Device")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mBluetoothAdapter != null ){
            mBluetoothAdapter.cancelDiscovery()
        }
    }

    private val mDeviceClickListener = OnItemClickListener { mAdapterView, mView, mPosition, mLong ->
        try {
            mBluetoothAdapter.cancelDiscovery()
            val mDeviceInfo = (mView as TextView).text.toString()
            val mDeviceAddress = mDeviceInfo.substring(mDeviceInfo.length - 17)
            Log.v(TAG, "Device_Address: $mDeviceAddress")

            val bundle = Bundle()
            bundle.putString("DeviceAddress" , mDeviceAddress)
            val intent = Intent()
            intent.putExtras(bundle)
            setResult(Activity.RESULT_OK,intent)
            finish()
        }catch (e:Exception){
            Log.v(TAG, "Error: $e")
        }
    }
}