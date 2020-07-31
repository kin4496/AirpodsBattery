package com.elegant.android_airpods

import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.SystemClock
import android.util.Log
import java.util.*
private val hexCharset = arrayOf('0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F')
private const val Tag="DEBUG"
class BleScanner(context: Context) {
    private val context=context
    private val scanCallback=object:ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            if (results != null) {
                for (result in results) onScanResult(1,result)
            }
        }
        override fun onScanResult(callbackType: Int, result: ScanResult?) {

            var data: ByteArray? =result!!.scanRecord!!.getManufacturerSpecificData(76)
            if(data==null || data.size!=27){
                return
            }

            PodsForegroundService.recentBeacons.add(result)
            var strongestBeacon: ScanResult?=null
            var i=0
            while(i< PodsForegroundService.recentBeacons.size){
                if(i<0)
                    break
                if(SystemClock.elapsedRealtimeNanos()- PodsForegroundService.recentBeacons.get(i).timestampNanos> PodsForegroundService.RECENT_BEACONS_MAX_T_NS){
                    PodsForegroundService.recentBeacons.removeAt(i--)
                    continue
                }
                if(strongestBeacon!=null&&strongestBeacon.device.address.equals(result.device.address))
                    strongestBeacon=result
                i++
            }
            if(result.rssi<-60){
                return
            }

            var a= result.scanRecord!!.getManufacturerSpecificData(76)?.let { decodeHex(it) }
            var str=""
            var str2=""
            if(a?.let { isFlipped(it) }!!){
                str=""+a[12]
                str2=""+a[13]
            }else{
                str=""+a[13]
                str2=""+a[12]
            }
            var str3=""+a[15]
            var str4=""+a[14]
            Log.d(Tag,"str=$str str2=$str2 str3=$str3")
            PodsForegroundService.leftStatus =str.toInt(16)
            PodsForegroundService.rightStatus =str2.toInt(16)
            PodsForegroundService.caseStatus =str3.toInt(16)
            var chargeStatus=str4.toInt(16)
            PodsForegroundService.chargeL =(chargeStatus and 0b0000001) != 0
            PodsForegroundService.chargeR =(chargeStatus and 0b0000010) != 0
            PodsForegroundService.chargeCase =(chargeStatus and 0b000100) != 0
            PodsForegroundService.model = if(a[7]=='E') PodsForegroundService.MODEL_AIRPODS_PRO
            else
                PodsForegroundService.MODEL_AIRPODS_NORMAL
            PodsForegroundService.lastSeenConnected =System.currentTimeMillis()

        }
    }

    fun startAirPodsScanner(){
        Log.d(Tag,"startScanner")
        try{
            val btManager=context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val btadAdapter=btManager.adapter
            PodsForegroundService.btScanner = btadAdapter.bluetoothLeScanner
            if (btadAdapter == null) throw Exception("No BT")
            if (!btadAdapter.isEnabled) throw Exception("BT Off")

            var scanfilters=getScanFilters()
            val settings= ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(2).build()
            PodsForegroundService.btScanner!!.startScan(scanfilters,settings,scanCallback)
        }catch(t:Throwable){
            Log.d("Scan Error", "" + t)
        }

    }
    private fun getScanFilters():MutableList<ScanFilter>{
        var manufacturerData = ByteArray(27);
        var manufacturerDataMask = ByteArray(27);

        manufacturerData[0] = 7;
        manufacturerData[1] = 25;

        manufacturerDataMask[0] = -1;
        manufacturerDataMask[1] = -1;

        val builder = ScanFilter.Builder();
        builder.setManufacturerData(76, manufacturerData, manufacturerDataMask);
        return Collections.singletonList(builder.build())
    }
    fun stopAirPodsScanner(){
        try{
            if(PodsForegroundService.btScanner !=null){
                Log.d(Tag,"Stop Scanner")
                PodsForegroundService.btScanner?.flushPendingScanResults(scanCallback)
                PodsForegroundService.btScanner?.stopScan(scanCallback)
            }

        }catch(t:Throwable){

        }
    }
    private fun isFlipped(str: String): Boolean {
        return Integer.toString(("" + str[10]).toInt(16) + 0x10, 2)[3] == '0'
    }
    private fun decodeHex(bArr:ByteArray):String{
        var ret=CharArray(bArr.size*2)
        for (i in bArr.indices) {
            val b = bArr[i].toInt() and 0xFF//bArr[i] and 255 1byte = 8bits

            ret[i * 2] = hexCharset[b.ushr(4)]
            ret[i * 2 + 1] = hexCharset[b and 0x0F]
        }

        return String(ret)
    }
}