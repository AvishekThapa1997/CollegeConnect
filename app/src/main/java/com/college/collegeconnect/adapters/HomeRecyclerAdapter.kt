package com.college.collegeconnect.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.college.collegeconnect.R
import com.college.collegeconnect.timetable.NewTimeTable
import com.college.collegeconnect.timetable.TimeTable
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class HomeRecyclerAdapter(val context: Context) : RecyclerView.Adapter<HomeRecyclerAdapter.ViewHolder>() {

    private val arrayList = arrayListOf("Time Table")
    private val arrayListImage = arrayListOf(R.drawable.ic_time_table)
    private lateinit var mFirebaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var intent: Intent
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_card_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val firebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(10)
                .build()
        mFirebaseRemoteConfig.setConfigSettingsAsync(firebaseRemoteConfigSettings)
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener {
            if (it.isSuccessful) {
                intent = if (mFirebaseRemoteConfig.getBoolean("timetable_activity"))
                    Intent(context, NewTimeTable::class.java)
                else
                    Intent(context, TimeTable::class.java)
            }
        }

        holder.textView.text = arrayList[position]
        holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, arrayListImage[position]))
        holder.itemView.setOnClickListener {
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = arrayList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.cardName)
        val imageView: ImageView = itemView.findViewById(R.id.cardImageHome)
    }
}