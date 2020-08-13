package com.elegant.android_airpods

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableContainer
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
        var imageViewList=arrayOf(add1,add2,add3,add4)
        val sharedPref = getSharedPreferences("com.elegant.android_airpods", Context.MODE_PRIVATE)
        val app1Pkg=sharedPref.getString("first","")
        val app2Pkg=sharedPref.getString("second","")
        val app3Pkg=sharedPref.getString("third","")
        val app4Pkg=sharedPref.getString("fourth","")
        if(app1Pkg!=""){
            lateinit var temp:Drawable
            try {
                temp= packageManager.getApplicationIcon(app1Pkg!!)
                val name1=sharedPref.getString("first_name","")
                settingTitle1.text=name1
            }catch (e:Exception){
                temp=getDrawable(R.drawable.ic_baseline_error_24)!!
                settingTitle1.text="Error"
            }
            imageViewList[0].setImageDrawable(temp)
        }
        if(app2Pkg!=""){
            lateinit var temp:Drawable
            try {
                temp= packageManager.getApplicationIcon(app2Pkg!!)
                val name2=sharedPref.getString("second_name","")
                settingTitle2.text=name2
            }catch (e:Exception){
                temp=getDrawable(R.drawable.ic_baseline_error_24)!!
                settingTitle2.text="Error"
            }
            imageViewList[1].setImageDrawable(temp)
        }
        if(app3Pkg!=""){
            lateinit var temp:Drawable
            try {
                temp= packageManager.getApplicationIcon(app3Pkg!!)
                val name3=sharedPref.getString("third_name","")
                settingTitle3.text=name3
            }catch (e:Exception){
                temp=getDrawable(R.drawable.ic_baseline_error_24)!!
                settingTitle3.text="Error"
            }
            imageViewList[2].setImageDrawable(temp)
        }
        if(app4Pkg!=""){
            lateinit var temp:Drawable
            try {
                temp= packageManager.getApplicationIcon(app4Pkg!!)
                val name4=sharedPref.getString("fourth_name","")
                settingTitle4.text=name4
            }catch (e:Exception){
                temp=getDrawable(R.drawable.ic_baseline_error_24)!!
                settingTitle4.text="Error"
            }
            imageViewList[3].setImageDrawable(temp)
        }

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