package com.example.posprinter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.example.posprinter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    lateinit var selectDevice : BluetoothConnection
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelectPrintDevice.setOnClickListener {
            browseBluetoothDevice()
        }
    }

    /* ------------ select printer device ------- */
    private fun browseBluetoothDevice() {
        val bluetoothDevicesList = BluetoothPrintersConnections().list
        if(bluetoothDevicesList != null ){
            val items = arrayOfNulls<String>(bluetoothDevicesList.size + 1)
            items[0] = "Default printer"
            var i = 0
            for (device in bluetoothDevicesList) {
                items[++i] = device.device.name
            }

            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Bluetooth printer selection")
            dialog.setItems(items){dialog, which ->
                val index = which - 1
                if(index == -1){
                    selectDevice
                }else{
                    selectDevice = bluetoothDevicesList[index]
                }
                binding.btnSelectPrintDevice.setText(items[which])
            }
            dialog.create()
            dialog.setCancelable(false)
            dialog.show()
        }
    }
}