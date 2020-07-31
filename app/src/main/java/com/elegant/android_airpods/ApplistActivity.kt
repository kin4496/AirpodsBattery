package com.elegant.android_airpods

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
private const val Tag="DEBUG"
class ApplistActivity : AppCompatActivity() {
    var mAppList= mutableListOf<AppData>()
    var copy= mutableListOf<AppData>()
    var index=1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applist)
        index=intent.getIntExtra("index",1)
        val packageManager=packageManager//getPackageManager
        var mPkgName= mutableListOf<String>()//Initiate pKgName List
        var intent= Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        var appInfo=packageManager.queryIntentActivities(intent,0)
        for(i in appInfo.indices){
            var temp=appInfo[i].activityInfo
            mPkgName.add(temp.packageName)
            mAppList.add(AppData(temp.loadLabel(packageManager).toString(),temp.loadIcon(packageManager),temp.packageName))
            copy.add(AppData(temp.loadLabel(packageManager).toString(),temp.loadIcon(packageManager),temp.packageName))
        }

        val listview: ListView =findViewById(R.id.applist)
        listview.requestFocusFromTouch()
        val applistadapter= ApplistAdapter(mAppList)
        listview.adapter=applistadapter

        listview.setOnItemClickListener { parent, view, position, id ->
            clickApp(mAppList[id.toInt()])
        }
        val textView: TextView =findViewById(R.id.editTextView)


        textView.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                var text=textView.text
                search(text)
                applistadapter.notifyDataSetChanged()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {


            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        if(SettingActivity.progressShowing){
            SettingActivity.progress.dismiss()
            SettingActivity.progressShowing=false
        }
    }
    private fun search(text:CharSequence){

        mAppList.clear()
        if(text.isEmpty()){
            mAppList.addAll(copy)
        } else{
            for(i in copy){
                if(i.name.contains(text,true)){
                    mAppList.add(i)
                }
            }
        }

    }
    private fun clickApp(app:AppData){
        val sharedPref = getSharedPreferences("com.elegant.android_airpods",Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            when(index){
                1->{
                    putString("first",app.pkgName)
                    putString("first_name",app.name)
                    commit()
                }
                2->{
                    putString("second",app.pkgName)
                    putString("second_name",app.name)
                    commit()
                }
                3->{
                    putString("third",app.pkgName)
                    putString("third_name",app.name)
                    commit()
                }
                4->{
                    putString("fourth",app.pkgName)
                    putString("fourth_name",app.name)
                    commit()
                }
                else -> {
                    Toast.makeText(this@ApplistActivity,getString(R.string.error),Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
        Toast.makeText(this,"${app.name}"+getString(R.string.app_selected),Toast.LENGTH_LONG).show()
        finish()
    }
}