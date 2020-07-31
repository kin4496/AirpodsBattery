package com.elegant.android_airpods

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class ApplistAdapter(val applist:MutableList<AppData>): BaseAdapter(){
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ApplistAdapter.ViewHolder
        if(convertView==null){
            view= LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_app,parent,false)
            holder= ApplistAdapter.ViewHolder(view)
            view.tag=holder
        }else{
            view=convertView
            holder=view.tag as ApplistAdapter.ViewHolder
        }

        var app=applist[position]
        holder.imageView.setImageDrawable(app.appIcon)
        holder.textview.setText(app.name)
        //Toast.makeText(context,holder.text.text, Toast.LENGTH_LONG).show()
        return view
    }

    override fun getItem(position: Int): Any {
        return applist[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return applist.size
    }
    private class ViewHolder(view: View){
        var textview: TextView =view.findViewById(R.id.apptitle)
        var imageView: ImageView =view.findViewById(R.id.appicon)
    }
}