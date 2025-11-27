package com.eguerra.ciudadanodigital.ui.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.eguerra.ciudadanodigital.R
import com.eguerra.ciudadanodigital.data.local.entity.ChatModel

class ChatListAdapter(
    private var dataSet: MutableList<ChatModel>,
    private val operationListener: ChatListener
) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    interface ChatListener {
        fun onItemClicked(chat: ChatModel)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chatTitle: TextView = view as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val textView = TextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(32, 24, 32, 24)
            textSize = 20f
            setTextColor(context.getColor(R.color.white))
            typeface = ResourcesCompat.getFont(context, R.font.urbanist)
        }
        return ViewHolder(textView)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = dataSet[position]
        holder.chatTitle.text = chat.nombre

        holder.chatTitle.setOnClickListener {
            operationListener.onItemClicked(chat)
        }
    }

    override fun getItemCount() = dataSet.size

    fun addChat(chat: ChatModel) {
        dataSet.add(chat)
        notifyItemInserted(dataSet.size - 1)
    }

    fun setChats(chats: List<ChatModel>) {
        dataSet.clear()
        dataSet.addAll(chats)
        notifyDataSetChanged()
    }
}
