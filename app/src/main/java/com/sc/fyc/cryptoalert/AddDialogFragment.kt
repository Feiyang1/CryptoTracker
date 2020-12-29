package com.sc.fyc.cryptoalert

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONObject

class AddDialogFragment : DialogFragment() {
    internal lateinit var listener: AddDialogListener
    var symbolSearchResult: MutableList<SymbolSearchResult> = mutableListOf()
    lateinit var selectedSymbolId: String

    interface AddDialogListener {
        fun onDialogPositiveClick(alert: Alert)
        fun onDialogNegativeClick()
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.add_dialog, null)
        val symbol = view.findViewById<TextView>(R.id.symbol)
        val currentPrice = view.findViewById<TextView>(R.id.current_price)
        val priceInput = view.findViewById<EditText>(R.id.price)
        val typeInput = view.findViewById<RadioGroup>(R.id.below_above)
        val search = view.findViewById<SearchView>(R.id.symbol_searchbox)
        val symbol_result = view.findViewById<RecyclerView>(R.id.symbol_search_result)
        val allAlertContent = view.findViewById<LinearLayout>(R.id.alert_content)

        builder.setView(view)
            .setPositiveButton(R.string.add,
                DialogInterface.OnClickListener { dialog, id ->

                    val selectedSymbol = symbol.text.toString()
                    val selectedTypeId = typeInput.checkedRadioButtonId
                    val selectedPriceString = priceInput.text.toString()

                    // make sure all required inputs are set
                    if (selectedPriceString != "" && selectedTypeId != -1 && selectedSymbol != "") {
                        val selectedPrice = selectedPriceString.toDouble()
                        val selectedType = view.findViewById<RadioButton>(selectedTypeId).text.toString()
                        listener.onDialogPositiveClick(Alert(selectedSymbol, selectedSymbolId, selectedPrice, selectedType, false))
                    }
                })
            .setNegativeButton(R.string.cancel,
                DialogInterface.OnClickListener { dialog, id ->
                    listener.onDialogNegativeClick()
                    dialog.cancel()
                })

        val symbolAdapter = activity?.let { SymbolAdapter(it, symbolSearchResult) { selected ->
                symbol.text = selected.symbol
                currentPrice.text = selected.price.toString()
                selectedSymbolId = selected.id

                // hide search view and show alert creation view
                symbol_result.visibility = View.GONE
                allAlertContent.visibility = View.VISIBLE
            }
        }
        symbol_result.adapter = symbolAdapter
        symbol_result.setHasFixedSize(true)

        // hide search result until search view is activated
        symbol_result.visibility = View.GONE

        search.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                println("onQueryTextSubmit - $query")
                AndroidNetworking.get("https://api.coincap.io/v2/assets").addQueryParameter("search", query).build().getAsJSONObject(object: JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if(response != null) {
                            val searchResults = response.optJSONArray("data")?.let {0.until(it.length()).map{i -> it.optJSONObject(i)}}?.map{ SymbolSearchResultRemote(it.toString())}?.map{SymbolSearchResult(it)}
                            symbolSearchResult.clear()

                            if (searchResults != null) {
                                symbolSearchResult.addAll(searchResults)
                            }
                        }
                        symbolAdapter?.notifyDataSetChanged()
                        println("response is $response")
                    }

                    override fun onError(anError: ANError?) {
                        println("Error is $anError")
                    }
                })
                symbol_result.visibility = View.VISIBLE
                // hide alert creation inputs when searching
                allAlertContent.visibility = View.GONE

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                println("onQueryTextChange")
                return false
            }
        })
        // Create the AlertDialog object and return it
        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as AddDialogFragment.AddDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }
}