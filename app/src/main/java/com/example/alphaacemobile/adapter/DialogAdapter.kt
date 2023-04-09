package com.example.alphaacemobile.adapter

import android.graphics.Color
import android.util.Log
import android.widget.TextView
import com.example.alphaacemobile.R
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import java.text.DecimalFormat

class DialogAdapter(
    private val tkName: String,
    private var tkStatus: String
): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        var tkConvert = tkStatus.toDouble()
        val df = DecimalFormat("#.##")
        Log.d("CALCULATOR_MARK", "SETUP DIALOG RV")
        val name = viewHolder.itemView.findViewById<TextView>(R.id.tvTokenName)
        val status = viewHolder.itemView.findViewById<TextView>(R.id.tvTokenStatus)

        var total = df.format(tkConvert)

        if (total <= 0.toString()) {
            status.setTextColor(Color.parseColor("#FF0000"))
        } else {
            status.setTextColor(Color.parseColor("#00FF00"))
        }

        name.text = tkName
        status.text = "$total%"
    }

    override fun getLayout(): Int {
        Log.d("CALCULATOR_MARK", "RETURN DIALOG ITEMS")
        return  R.layout.dialog_items
    }
}