package com.example.alphaacemobile.adapter

import android.widget.TextView
import com.example.alphaacemobile.R
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class FeedbackAdapter(
    val feedbackUsername: String,
    val feedbackMessage: String
): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val tvUsername = viewHolder.itemView.findViewById<TextView>(R.id.tvFeedbackUsername)
        val tvMessage = viewHolder.itemView.findViewById<TextView>(R.id.tvFeedbackMessage)
        tvUsername.text = feedbackUsername
        tvMessage.text = feedbackMessage
    }

    override fun getLayout(): Int {
        return  R.layout.view_feedback_item
    }

}