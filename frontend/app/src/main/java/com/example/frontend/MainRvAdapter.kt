package com.example.frontend

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainRvAdapter(val context: Context, val allergyList : ArrayList<Allergy>):
    RecyclerView.Adapter<MainRvAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.main_allergy, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder?.bind(allergyList[position], context)
    }

    override fun getItemCount(): Int {
        return allergyList.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val medicine = itemView?.findViewById<TextView>(R.id.medicine)
        val material = itemView?.findViewById<TextView>(R.id.material)
        val symptom = itemView?.findViewById<TextView>(R.id.symptom)

        fun bind(allergy: Allergy, context: Context){
            medicine?.text = allergy.medicine
            material?.text = allergy.material
            symptom?.text = allergy.symptom
        }
    }

}