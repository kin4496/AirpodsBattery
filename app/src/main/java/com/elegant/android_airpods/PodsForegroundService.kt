package com.elegant.android_airpods

import android.app.*
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.core.app.NotificationCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import org.jetbrains.anko.*
import java.util.*
import kotlin.concurrent.timer
import kotlin.properties.Delegates

private const val Tag="DEBUG"
class PodsForegroundService : Service() {

    private val notificationManager:NotificationManager by lazy{getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager}
    private val builder:NotificationCompat.Builder by lazy{ NotificationCompat.Builder(this,Tag)}
    val inflate:LayoutInflater by lazy{LayoutInflater.from(this)}
    val mView: View by lazy{inflate.inflate(R.layout.battery_popup,null)}
    val wm:WindowManager by lazy{windowManager}
    val adRequest: AdRequest by lazy{ AdRequest.Builder().build()}
    val bleScanner: BleScanner by lazy{
        BleScanner(this)
    }
    var isWindowShowing=true
    companion object{
        val RECENT_BEACONS_MAX_T_NS=10000000000L
        var btScanner: BluetoothLeScanner?=null
        var recentBeacons=mutableListOf<ScanResult>()
        var chargeL=false
        var chargeR=false
        var chargeCase=false
        var leftStatus=15
        var rightStatus=15
        var caseStatus=15
        var maybeConnected=false
        var lastSeenConnected=0L
        val MODEL_AIRPODS_NORMAL="airpods12"
        val MODEL_AIRPODS_PRO="airpodspro"
        var model= MODEL_AIRPODS_NORMAL
        var timer:Timer?=null
        var device:BluetoothDevice?=null
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationInit()
        bleScanner.startAirPodsScanner()
        viewGenerate()

        return START_NOT_STICKY
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(Tag,"OnDestroy")
        removeBatteryPopupView()
        timer?.cancel()
    }
    private fun notifyNotification(){
        if(model == MODEL_AIRPODS_NORMAL){
            builder.setContentTitle("Airpods")
        }else if(model == MODEL_AIRPODS_PRO){
            builder.setContentTitle("AirpodsPro")
        }
        var str="Left :"+ (if (leftStatus == 10) "100%" else if (leftStatus < 10) (leftStatus * 10 + 5).toString() + "%" else "")
        if(chargeL)
            str+="+"
        str+="  "
        str+= "Right :"+(if (rightStatus == 10) "100%" else if (rightStatus < 10) (rightStatus * 10 + 5).toString() + "%" else "")
        if(chargeR)
            str+="+"
        str+="  "
        str+= "Case :"+(if (caseStatus == 10) "100%" else if (caseStatus < 10) (caseStatus * 10 + 5).toString() + "%" else "")
        if(chargeCase)
            str+="+"
        builder.setContentText(str)
        notificationManager.notify(1,builder.build())
    }
    private fun viewGenerate(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {        // 체크
                Log.e(Tag,"Overlay Permission Not Granted")
                return
            }
        }
        var params= WindowManager.LayoutParams( /*ViewGroup.LayoutParams.MATCH_PARENT*/
            wrapContent,
            wrapContent,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSPARENT
        )
        params.gravity = Gravity.CENTER
        val exitBt: ImageButton =mView.findViewById(R.id.exitBt)
        exitBt.setOnClickListener {
            isWindowShowing=false
            removeBatteryPopupView()
        }
        val settingBt: ImageButton =mView.findViewById(R.id.settingBt)
        settingBt.setOnClickListener {
            val intent= Intent(this,SettingActivity::class.java)
            intent.flags= Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        try{
            timer?.cancel()
        }catch (e:Exception){Log.e(Tag,"There's no timerTask")}
        timer= timer(period=1000L){
            if(isWindowShowing)
                drawProgressBar()
            if(leftStatus==15&& rightStatus==15&& caseStatus==15){
                bleScanner.stopAirPodsScanner()
                bleScanner.startAirPodsScanner()
            }
            notifyNotification()
        }
        val sharedPref = getSharedPreferences("com.elegant.android_airpods",
            Context.MODE_PRIVATE)
        val app1Pkg=sharedPref.getString("first","")
        val app2Pkg=sharedPref.getString("second","")
        val app3Pkg=sharedPref.getString("third","")
        val app4Pkg=sharedPref.getString("fourth","")
        if(app1Pkg!=""){
            val app1Bt: ImageView =mView.findViewById(R.id.app1)
            val app1title: TextView =mView.findViewById(R.id.title1)
            try{
                app1Bt.image=packageManager.getApplicationIcon(app1Pkg!!)
                val name1=sharedPref.getString("first_name","")
                app1title.text=name1
                app1Bt.setOnClickListener {
                    val intent=packageManager.getLaunchIntentForPackage(app1Pkg!!)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    Toast.makeText(this,name1+getString(R.string.app_launch), Toast.LENGTH_LONG).show()
                    startActivity(intent)
                    wm.removeView(mView)
                }
            }catch(e:Exception){
                app1Bt.image=getDrawable(R.drawable.ic_baseline_error_24)
                app1title.text="Error"
            }
        }
        if(app2Pkg!=""){
            val app2Bt: ImageView =mView.findViewById(R.id.app2)
            val app2title: TextView =mView.findViewById(R.id.title2)
            try{
                app2Bt.image=packageManager.getApplicationIcon(app2Pkg!!)
                val name2=sharedPref.getString("second_name","")
                app2title.text=name2
                app2Bt.setOnClickListener {
                    val intent=packageManager.getLaunchIntentForPackage(app2Pkg!!)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    Toast.makeText(this,name2+getString(R.string.app_launch), Toast.LENGTH_LONG).show()
                    startActivity(intent)
                    wm.removeView(mView)
                }
            }catch(e:Exception) {
                app2Bt.image=getDrawable(R.drawable.ic_baseline_error_24)
                app2title.text="Error"
            }
        }
        if(app3Pkg!=""){
            val app3Bt: ImageView =mView.findViewById(R.id.app3)
            val app3title: TextView =mView.findViewById(R.id.title3)
            try{
                app3Bt.image=packageManager.getApplicationIcon(app3Pkg!!)

                val name3=sharedPref.getString("third_name","")
                app3title.text=name3
                app3Bt.setOnClickListener {
                    val intent=packageManager.getLaunchIntentForPackage(app3Pkg!!)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    Toast.makeText(this,name3+getString(R.string.app_launch), Toast.LENGTH_LONG).show()
                    startActivity(intent)
                    wm.removeView(mView)
                }
            }catch(e:Exception){
                app3Bt.image=getDrawable(R.drawable.ic_baseline_error_24)
                app3title.text="Error"
            }

        }
        if(app4Pkg!=""){
            val app4Bt: ImageView =mView.findViewById(R.id.app4)
            val app4title: TextView =mView.findViewById(R.id.title4)
            try{
                app4Bt.setImageDrawable(packageManager.getApplicationIcon(app4Pkg!!))
                val name4=sharedPref.getString("fourth_name","")
                app4title.text=name4
                app4Bt.setOnClickListener {
                    val intent=packageManager.getLaunchIntentForPackage(app4Pkg!!)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    Toast.makeText(this,name4+getString(R.string.app_launch), Toast.LENGTH_LONG).show()
                    startActivity(intent)
                    wm.removeView(mView)
                }
            }catch(e:Exception){
                app4Bt.image=getDrawable(R.drawable.ic_baseline_error_24)
                app4title.text="Error"
            }
        }
        try{
            wm.removeView(mView)
        }catch(e:Exception){ Log.e(Tag,"view not found")}
        wm.addView(mView,params)
        isWindowShowing=true
        val adView: AdView =mView.findViewById(R.id.podsServiceAdView)
        adView.loadAd(adRequest)
    }
    private fun removeBatteryPopupView(){
        isWindowShowing=false
        try{
            wm.removeView(mView)
        }catch (e:java.lang.Exception){Log.e(Tag,"view not found")}
    }
    private fun drawProgressBar(){
        val progressBar=arrayOf<ProgressBar>(mView.findViewById(R.id.leftinfo),mView.findViewById(R.id.caseinfo),mView.findViewById(R.id.rightinfo))
        val textViews=arrayOf<TextView>(mView.findViewById(R.id.leftinfonum),mView.findViewById(R.id.caseinfonum),mView.findViewById(R.id.rightinfonum))
        val batteryImage=arrayOf<ImageView>(mView.findViewById(R.id.popupLeftImage),mView.findViewById(R.id.popupCaseImage),mView.findViewById(R.id.popupRightImage))
        var left:Int=if(leftStatus==10)100 else if(leftStatus<10) 10*leftStatus+5 else 0
        var case:Int=if(caseStatus==10)100 else if(caseStatus<10) 10*caseStatus+5 else 0
        var right:Int=if(rightStatus==10)100 else if(rightStatus<10) 10*rightStatus+5 else 0
        val percents=arrayOf(left,case,right)
        val modelTextView: TextView =mView.findViewById(R.id.modelTextView)
        var chargeStatus=arrayOf(chargeL, chargeCase, chargeR)
        runOnUiThread {
            if(model==MODEL_AIRPODS_PRO){
                modelTextView.text=device?.name+"(AirPods Pro)"
            }else{
                modelTextView.text=device?.name+"(AirPods)"
            }
            for(i in progressBar.indices){
                val percent=percents[i]
                when {
                    percent<=30 -> {
                        progressBar[i].progressDrawable=getDrawable(R.drawable.battery_progress_empty)
                    }
                    else -> {
                        progressBar[i].progressDrawable=getDrawable(R.drawable.battery_progress_full)
                    }
                }
                textViews[i].text=if(percent!=0&&chargeStatus[i]) "$percent%+" else if(percent!=0) "$percent%" else ""
                progressBar[i].progress=0
                progressBar[i].progress=percent
            }
            batteryImage[0].image=if(chargeL){
                getDrawable(R.drawable.left_charging)
            }else{
                getDrawable(R.drawable.left_airpod)
            }
            batteryImage[1].image=if(chargeCase){
                getDrawable(R.drawable.case_charging)
            }else{
                getDrawable(R.drawable.case_airpod)
            }
            batteryImage[2].image=if(chargeR){
                getDrawable(R.drawable.right_charging)
            }else{
                getDrawable(R.drawable.right_airpod)
            }
        }
    }
    private fun notificationInit(){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            val channel= NotificationChannel(Tag,Tag, NotificationManager.IMPORTANCE_LOW)
            channel.setSound(null,null)
            channel.enableLights(false)
            channel.enableVibration(false)
            channel.setShowBadge(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)
        }
        var notiIntent=Intent(this@PodsForegroundService,MainActivity::class.java)
        notiIntent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP
                or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        var pending= PendingIntent.getActivities(this@PodsForegroundService,0, arrayOf(notiIntent),0)

        builder.setAutoCancel(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentTitle("Airpods Battery")
            .setContentText("")
            .setContentInfo("Info")
            .setContentIntent(pending)
        startForeground(1,builder.build())
    }
}
