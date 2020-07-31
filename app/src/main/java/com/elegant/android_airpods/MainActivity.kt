package com.elegant.android_airpods

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.image
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer

private const val Tag="DEBUG"
private const val REQUEST_ACCESS_LOCATION=1001

class MainActivity : AppCompatActivity() {
    var bleScanner:BleScanner?=null
    var timer: Timer?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        checkAuthority()
    }
    private fun checkOk(){
        val ba =
            (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        ba.getProfileProxy(applicationContext, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(i: Int, bluetoothProfile: BluetoothProfile) {
                if (i == BluetoothProfile.HEADSET) {
                    Log.d(Tag, "BT PROXY SERVICE CONNECTED")
                    val h = bluetoothProfile as BluetoothHeadset
                    for (d in h.connectedDevices) {
                        if (checkUUID(d!!)) {
                            Log.d(Tag, "BT PROXY: AIRPODS ALREADY CONNECTED")
                            PodsForegroundService.maybeConnected = true
                            bleScanner= BleScanner(this@MainActivity)
                            bleScanner?.startAirPodsScanner()
                            PodsForegroundService.device=d
                            break
                        }
                    }
                }
            }

            override fun onServiceDisconnected(i: Int) {
                if (i == BluetoothProfile.HEADSET) {
                    Log.d(Tag, "BT PROXY SERVICE DISCONNECTED ")
                    PodsForegroundService.maybeConnected = false
                }
            }
        }, BluetoothProfile.HEADSET)
        timer=timer(period=2000L){
            runOnUiThread {
                getDeviceInfo()
            }
            if(PodsForegroundService.leftStatus ==15&& PodsForegroundService.rightStatus ==15&& PodsForegroundService.caseStatus ==15){
                bleScanner?.stopAirPodsScanner()
                bleScanner?.startAirPodsScanner()
            }
        }
    }
    private fun checkUUID(bluetoothDevice: BluetoothDevice): Boolean {
        val AIRPODS_UUIDS = arrayOf(
            ParcelUuid.fromString("74ec2172-0bad-4d01-8f77-997b2be0722a"),
            ParcelUuid.fromString("2a72e02b-7b99-778f-014d-ad0b7221ec74")
        )
        getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val uuids = bluetoothDevice.uuids ?: return false
        for (u in uuids) {
            for (v in AIRPODS_UUIDS) {
                if (u == v) return true
            }
        }
        return false
    }
    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences("com.elegant.android_airpods", Context.MODE_PRIVATE)
        val app1Pkg=sharedPref.getString("first","")
        val app2Pkg=sharedPref.getString("second","")
        val app3Pkg=sharedPref.getString("third","")
        val app4Pkg=sharedPref.getString("fourth","")
        if(app1Pkg!=""){
            setImage(app1Pkg!!,1)
            val name1=sharedPref.getString("first_name","")
            mainTitle1.text=name1
            mainApp1.setOnClickListener {
                val intent=packageManager.getLaunchIntentForPackage(app1Pkg!!)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                Toast.makeText(this,name1+getString(R.string.app_launch), Toast.LENGTH_LONG).show()
                startActivity(intent)
            }
        }
        if(app2Pkg!=""){
            setImage(app2Pkg!!,2)
            val name2=sharedPref.getString("second_name","")
            mainTitle2.text=name2
            mainApp2.setOnClickListener {
                val intent=packageManager.getLaunchIntentForPackage(app2Pkg!!)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                Toast.makeText(this,name2+getString(R.string.app_launch), Toast.LENGTH_LONG).show()
                startActivity(intent)
            }
        }

        if(app3Pkg!=""){
            setImage(app3Pkg!!,3)
            val name3=sharedPref.getString("third_name","")
            mainTitle3.text=name3
            mainApp3.setOnClickListener {
                val intent=packageManager.getLaunchIntentForPackage(app3Pkg!!)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                Toast.makeText(this,name3+getString(R.string.app_launch), Toast.LENGTH_LONG).show()
                startActivity(intent)
            }
        }
        if(app4Pkg!=""){
            setImage(app4Pkg!!,4)
            val name4=sharedPref.getString("fourth_name","")
            mainTitle4.text=name4
            mainApp4.setOnClickListener {
                val intent=packageManager.getLaunchIntentForPackage(app4Pkg!!)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                Toast.makeText(this,name4+getString(R.string.app_launch), Toast.LENGTH_LONG).show()
                startActivity(intent)
            }
        }
    }
    private fun setImage(pkg:String,num:Int){
        val temp=packageManager.getApplicationIcon(pkg)
        when (num) {
            1 -> mainApp1.setImageDrawable(temp)
            2 -> mainApp2.setImageDrawable(temp)
            3 -> mainApp3.setImageDrawable(temp)
            4 -> mainApp4.setImageDrawable(temp)
        }
    }
    private fun getDeviceInfo(){

        val connectionTextView=arrayOf<TextView>(leftConnection,caseConnection,rightConnection)
        val batteryImage=arrayOf<ImageView>(leftImage,caseImage,rightImage)
        val batteryProgressBar=arrayOf<ProgressBar>(mainleft,maincase,mainright)
        val batteryTextView=arrayOf<TextView>(mainLeftInfoNum,mainCaseInfoNum,mainRightInfoNum)
        val connection=if(PodsForegroundService.maybeConnected){
            arrayOf(PodsForegroundService.leftStatus,PodsForegroundService.caseStatus,PodsForegroundService.rightStatus)
        }else{
            arrayOf(15,15,15)
        }
        var chargeStatus=if(PodsForegroundService.maybeConnected){arrayOf(
            PodsForegroundService.chargeL,
            PodsForegroundService.chargeCase,
            PodsForegroundService.chargeR
        )}else{
            arrayOf(false,false,false)
        }
        deviceName.text= when (PodsForegroundService.model) {
            PodsForegroundService.MODEL_AIRPODS_PRO -> {
                if(PodsForegroundService.maybeConnected){
                    PodsForegroundService.device?.name+"(AirPods Pro)"
                }else{
                    getString(R.string.connection_status_notconnected)
                }
            }
            PodsForegroundService.MODEL_AIRPODS_NORMAL -> {
                if(PodsForegroundService.maybeConnected)
                    PodsForegroundService.device?.name+"(AirPods)"
                else
                    getString(R.string.connection_status_notconnected)
            }
            else -> {
                getString(R.string.connection_status_notconnected)
            }
        }
        for(i in connectionTextView.indices){
            batteryProgressBar[i].progress=0
            when {
                connection[i]==10 -> {
                    connectionTextView[i].text=getString(R.string.connection_status_connected)
                    batteryProgressBar[i].progress=100
                    var text="100%"
                    if(chargeStatus[i])
                        text+="+"
                    batteryTextView[i].text=text
                }
                connection[i]<10 -> {
                    connectionTextView[i].text=getString(R.string.connection_status_connected)
                    val percent=connection[i]*10+5
                    if(percent<30){
                        batteryProgressBar[i].progressDrawable=getDrawable(R.drawable.battery_progress_empty)
                    }else{
                        batteryProgressBar[i].progressDrawable=getDrawable(R.drawable.battery_progress_full)
                    }
                    batteryProgressBar[i].progress=connection[i]*10+5
                    var text=(connection[i]*10+5).toString()+"%"
                    if(chargeStatus[i])
                        text+="+"
                    batteryTextView[i].text=text
                }
                else -> {
                    connectionTextView[i].text=getString(R.string.connection_status_notconnected)
                    batteryProgressBar[i].progress=0
                    batteryTextView[i].text="0%"
                }
            }
        }
        batteryImage[0].image=if(PodsForegroundService.chargeL){
            getDrawable(R.drawable.left_charging)
        }else{
            getDrawable(R.drawable.left_airpod)
        }
        batteryImage[1].image=if(PodsForegroundService.chargeCase){
            getDrawable(R.drawable.case_charging)
        }else{
            getDrawable(R.drawable.case_airpod)
        }
        batteryImage[2].image=if(PodsForegroundService.chargeR){
            getDrawable(R.drawable.right_charging)
        }else{
            getDrawable(R.drawable.right_airpod)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        bleScanner?.stopAirPodsScanner()
    }
    private fun checkAuthority(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 체크
                var intent2 = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
                )
                startActivityForResult(intent2, 1)
            }
        }
        var permissionFine= ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!permissionFine){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Log.d("FroegroundService","should show request permission in access_fine_location")
                alert(getString(R.string.permission_message), getString(R.string.permission_title)) {
                    yesButton {
                        // 권한 요청
                        ActivityCompat.requestPermissions(this@MainActivity,
                            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                            REQUEST_ACCESS_LOCATION)
                    }
                    noButton { finish()}
                }.show()
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
                    , REQUEST_ACCESS_LOCATION)
            }
        }else{
            checkOk()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.item_options,menu)
        return true
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            1001->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    finish()
                }else if(grantResults.isNotEmpty()&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    checkOk()
                }
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.setting->{
                var intent=Intent(this,SettingActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}