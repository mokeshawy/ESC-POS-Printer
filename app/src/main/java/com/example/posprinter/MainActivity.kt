package com.example.posprinter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.example.posprinter.databinding.ActivityMainBinding
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Exception

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    private val TAG = "MainActivity"
    private val REQUEST_CONNECT_DEVICE  = 1;
    private val REQUEST_ENABLE_BT       = 2;
    private var mBluetoothAdapter   : BluetoothAdapter? = null

    lateinit var mBluetoothConnectProgressDialog : ProgressDialog
    private val applicationUUID     : UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    lateinit var mBluetoothSocket   : BluetoothSocket
    private var mBluetoothDevice    : BluetoothDevice? = null
    var printstat = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        binding.Scan.setOnClickListener {
            if(binding.Scan.text.equals("Connect")){
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                if(mBluetoothAdapter == null ){
                    Toast.makeText(this, "Message1", Toast.LENGTH_SHORT).show()
                }else{
                    if(!mBluetoothAdapter!!.isEnabled){
                        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(intent,REQUEST_ENABLE_BT)
                    }else{
                        listPairedDevices()
                        val connectIntent = Intent(this,DeviceListActivity::class.java)
                        startActivityForResult(connectIntent , REQUEST_CONNECT_DEVICE)
                    }
                }
            }else if(binding.Scan.text.equals("Disconnect")){
                if(mBluetoothAdapter != null ){
                    mBluetoothAdapter!!.disable()
                    binding.bpstatus.text = ""
                    binding.bpstatus.text = "Disconnected"
                    binding.bpstatus.setTextColor(Color.rgb(199, 59, 59))
                    binding.mPrint.isEnabled = false
                    binding.Scan.isEnabled = true
                    binding.Scan.text = "Connect"
                }
            }
        }

        binding.mPrint.setOnClickListener {
            printOne()
        }
    }

            /*======================================================================
              ============ Handle Connect device printer with bluetooth ============
              ======================================================================*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_CONNECT_DEVICE ->{
                if(resultCode == Activity.RESULT_OK){
                    val bundle = data!!.extras
                    val mDeviceAddress = bundle!!.getString("DeviceAddress")
                    Log.v(TAG, "Coming incoming address $mDeviceAddress")
                    mBluetoothDevice = mBluetoothAdapter!!.getRemoteDevice(mDeviceAddress)
                    mBluetoothConnectProgressDialog = ProgressDialog.show(this,"Connecting...",
                        mBluetoothDevice!!.name + " : " + mBluetoothDevice!!.address ,
                        true , false)
                    val mBluetoothConnectThread = Thread()
                    mBluetoothConnectThread.start()
                }
            }
            REQUEST_ENABLE_BT ->{
                if(resultCode == Activity.RESULT_OK){
                    listPairedDevices()
                    val connectIntent = Intent(this , DeviceListActivity::class.java)
                    startActivityForResult(connectIntent,REQUEST_CONNECT_DEVICE)
                }else{
                    Toast.makeText(this, "Not connected to any device", Toast.LENGTH_SHORT).show()
                }
            }
        }

        /* handle finish connect and disconnect with BT */
        runBlocking {
            try{
                mBluetoothSocket = mBluetoothDevice!!.createRfcommSocketToServiceRecord(applicationUUID)
                mBluetoothAdapter!!.cancelDiscovery()
                mBluetoothSocket!!.connect()
                mHandler.sendEmptyMessage(0)
            }catch (e: IOException){
                Log.d(TAG, "CouldNotConnectToSocket", e)
                closeSocket(mBluetoothSocket!!)
            }
        }
    }

    /* ----- list of print device ----- */
    private fun listPairedDevices(){
        val mPairedDevices = mBluetoothAdapter!!.bondedDevices
        if(mPairedDevices.size > 0 ){
            for ( mDevice in mPairedDevices){
                Log.v(TAG,"Devices: " + mDevice.name + " " + mDevice.address)
            }
        }
    }

    /* ----- handle message of connect and disconnect ---- */
    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        @SuppressLint("SetTextI18n")
        override fun handleMessage(msg: Message) {
            mBluetoothConnectProgressDialog.dismiss()
            binding.bpstatus.text = ""
            binding.bpstatus.text = "Connected"
            binding.bpstatus.setTextColor(Color.rgb(97, 170, 74))
            binding.mPrint.isEnabled = true
            binding.Scan.text = "Disconnect"
        }
    }

    private fun closeSocket(nOpenSocket : BluetoothSocket){
        try {
            nOpenSocket.close()
            Log.d(TAG, "SocketClosed")
        }catch (e:IOException){
            Log.d(TAG, "CouldNotCloseSocket")
        }
    }

            /*======================================================================
             ======================= Handle print message ==========================
             ======================================================================*/

    @SuppressLint("SimpleDateFormat")
    fun printOne(){
        runBlocking {
            val outPutStream = mBluetoothSocket.outputStream

            /* first zone */
            var blank           = ""
            var titleOfCompany  = ""

            /* product zone */
            var productOfGasoline   = ""
            var priceGasoline92     = ""
            var priceGasoline95     = ""

            var productOfDiesel     = ""
            var priceDiesel         = ""

            /* ---- free space from top ----*/
            blank = "\n\n\n"

            /* write company name */
            titleOfCompany = "    Solarus Smart Solution\n\n"
            titleOfCompany += "===============================\n\n"

            productOfGasoline = "GASOLINE\n"
            productOfGasoline += "---------\n"

            priceGasoline92   = "Gasoline [92]   Price : 800 EG\n\n"

            priceGasoline95   = "Gasoline [95]   Price : 0,0 EG\n\n"
            priceGasoline95 += "===============================\n"

            productOfDiesel = "DIESEL\n"
            productOfDiesel += "---------\n"

            priceDiesel = "Diesel           Price : 1000 EG\n\n"
            priceDiesel += "===============================\n"

            outPutStream.write(blank.toByteArray())
            outPutStream.write(titleOfCompany.toByteArray())
            outPutStream.write(productOfGasoline.toByteArray())
            outPutStream.write(priceGasoline92.toByteArray())
            outPutStream.write(priceGasoline95.toByteArray())
            outPutStream.write(productOfDiesel.toByteArray())
            outPutStream.write(priceDiesel.toByteArray())

            // Setting height


            //This is printer specific code you can comment ==== > Start

            // Setting height
            val gs = 29
            outPutStream.write(intToByteArray(gs).toInt())
            val h = 150
            outPutStream.write(intToByteArray(h).toInt())
            val n = 170
            outPutStream.write(intToByteArray(n).toInt())

            // Setting Width

            // Setting Width
            val gs_width = 29
            outPutStream.write(intToByteArray(gs_width).toInt())
            val w = 119
            outPutStream.write(intToByteArray(w).toInt())
            val n_width = 2
            outPutStream.write(intToByteArray(n_width).toInt())

        }
    }

    fun intToByteArray(value: Int): Byte {
        val b = ByteBuffer.allocate(4).putInt(value).array()
        for (k in b.indices) {
            println( "Selva  [" + k + "] = " + "0x" + UnicodeFormatter.byteToHex(b[k]))
        }
        return b[3]
    }
}