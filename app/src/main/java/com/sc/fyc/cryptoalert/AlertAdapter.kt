package com.sc.fyc.cryptoalert

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AlertAdapter (private val context: Context, private val dataset: List<Alert>): RecyclerView.Adapter<AlertAdapter.ItemViewHolder>() {

    class ItemViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        val symbolView: TextView = view.findViewById(R.id.symbol)
        val priceView: TextView = view.findViewById(R.id.price)
        val triggered_timestamp_row: LinearLayout = view.findViewById(R.id.triggered_time_row)
        val triggered_timestamp: TextView = view.findViewById(R.id.triggered_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.alert, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    override fun getItemCount() = dataset.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset[position]
        holder.symbolView.text = item.symbol
        holder.priceView.text = item.price.toString()

        if(item.triggered) {
            holder.triggered_timestamp_row.visibility = View.VISIBLE
            holder.triggered_timestamp.text = item.triggered_timestamp?.toDate().toString()
        } else {
            holder.triggered_timestamp_row.visibility = View.GONE
        }
    }
}