package com.elegant.android_airpods

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_setting.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.share
import org.jetbrains.anko.textColor

class SettingActivity : AppCompatActivity() {
    companion object{
        lateinit var progress: ProgressDialog
        var progressShowing=false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        val adRequest = AdRequest.Builder().build()
        settingAdView.loadAd(adRequest)
        add1.setOnClickListener {
            getAppList(1)
        }
        add2.setOnClickListener {
            getAppList(2)
        }
        add3.setOnClickListener {
            getAppList(3)
        }
        add4.setOnClickListener {
            getAppList(4)
        }
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
            settingTitle1.text=name1
        }
        if(app2Pkg!=""){
            setImage(app2Pkg!!,2)
            val name2=sharedPref.getString("second_name","")
            settingTitle2.text=name2
        }
        if(app3Pkg!=""){
            setImage(app3Pkg!!,3)
            val name3=sharedPref.getString("third_name","")
            settingTitle3.text=name3
        }
        if(app4Pkg!=""){
            setImage(app4Pkg!!,4)
            val name4=sharedPref.getString("fourth_name","")
            settingTitle4.text=name4
        }

    }
    private fun setImage(pkg:String,num:Int){
        val temp=packageManager.getApplicationIcon(pkg)
        if(num==1)
            add1.setImageDrawable(temp)
        else if(num==2)
            add2.setImageDrawable(temp)
        else if(num==3)
            add3.setImageDrawable(temp)
        else if(num==4)
            add4.setImageDrawable(temp)
    }
    private fun getAppList(index:Int){
        progressShowing =true
        progress =ProgressDialog(this)
        progress.setMessage("Loading")
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progress.setCancelable(false)
        progress.show()
        val intent= Intent(this,ApplistActivity::class.java)
        intent.putExtra("index",index)
        startActivity(intent)
    }
}