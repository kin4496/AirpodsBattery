package com.elegant.android_airpods

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.EXTRA_STATE
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.ParcelUuid
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.*
import com.google.android.gms.ads.AdView
import org.jetbrains.anko.image
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.wrapContent
import java.util.*
import kotlin.concurrent.timer

private const val Tag="DEBUG"
class BluetoothReceiver:BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val bluetoothDevice: BluetoothDevice? =
            intent.getParcelableExtra<Parcelable>("android.bluetooth.device.extra.DEVICE") as BluetoothDevice?
        val action:String? = intent.action
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            Log.d(Tag,"state change")
            val state =
                intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF) { //bluetooth turned off, stop scanner and remove notification
                Log.d(Tag, "BT OFF")
                val intent=Intent(context,PodsForegroundService::class.java)
                context.stopService(intent)
                PodsForegroundService.leftStatus =15
                PodsForegroundService.rightStatus =15
                PodsForegroundService.caseStatus =15
                PodsForegroundService.chargeCase =false
                PodsForegroundService.chargeL =false
                PodsForegroundService.chargeR =false
                PodsForegroundService.maybeConnected=false
                PodsForegroundService.recentBeacons.clear()
            }
            if (state == BluetoothAdapter.STATE_ON) { //bluetooth turned on, start/restart scanner
                Log.d(Tag, "BT ON")
            }
        }
        if (bluetoothDevice != null && action != null && action.isNotEmpty() && checkUUID(bluetoothDevice)) { //airpods filter
            if (action == BluetoothDevice.ACTION_ACL_CONNECTED) { //airpods connected, show notification
                Log.d(Tag, "ACL CONNECTED")
                PodsForegroundService.maybeConnected=true
                PodsForegroundService.device=bluetoothDevice
                val intent=Intent(context,PodsForegroundService::class.java)
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                    context.startForegroundService(intent)
                }else{
                    context.startService(intent)
                }
            }
        }
        if (action == BluetoothDevice.ACTION_ACL_DISCONNECTED || action == BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED) { //airpods disconnected, remove notification but leave the scanner going
            Log.d(Tag, "ACL DISCONNECTED")
            val intent=Intent(context,PodsForegroundService::class.java)
            context.stopService(intent)
            PodsForegroundService.leftStatus =15
            PodsForegroundService.rightStatus =15
            PodsForegroundService.caseStatus =15
            PodsForegroundService.chargeCase =false
            PodsForegroundService.chargeL =false
            PodsForegroundService.chargeR =false
            PodsForegroundService.maybeConnected=false
            PodsForegroundService.recentBeacons.clear()
        }
    }
    private fun checkUUID(bluetoothDevice: BluetoothDevice): Boolean {
        val AIRPODS_UUIDS = arrayOf(
            ParcelUuid.fromString("74ec2172-0bad-4d01-8f77-997b2be0722a"),
            ParcelUuid.fromString("2a72e02b-7b99-778f-014d-ad0b7221ec74")
        )
        val uuids = bluetoothDevice.uuids ?: return false
        for (u in uuids) {
            for (v in AIRPODS_UUIDS) {
                if (u == v) return true
            }
        }
        return false
    }
}