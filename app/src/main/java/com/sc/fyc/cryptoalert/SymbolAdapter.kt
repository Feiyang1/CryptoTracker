package com.sc.fyc.cryptoalert


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SymbolAdapter (private val context: Context, private val dataset: List<SymbolSearchResult>): RecyclerView.Adapter<SymbolAdapter.ItemViewHolder>() {

    class ItemViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        val symbolView: TextView = view.findViewById(R.id.symbol_search_name)
        val priceView: TextView = view.findViewById(R.id.symbol_search_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.symbol, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    override fun getItemCount() = dataset.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset[position]
        holder.symbolView.text = item.symbol
        holder.priceView.text = item.price.toString()
    }
}