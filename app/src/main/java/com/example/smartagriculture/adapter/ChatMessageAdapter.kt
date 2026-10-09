package com.example.smartagriculture.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagriculture.R
import com.example.smartagriculture.model.ChatMessage

class ChatMessageAdapter(
    private val messages: MutableList<ChatMessage> = mutableListOf()
) : RecyclerView.Adapter<ChatMessageAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layoutDoctor: LinearLayout = view.findViewById(R.id.layoutDoctorMessage)
        val tvDoctor: TextView = view.findViewById(R.id.tvDoctorMessage)
        val layoutUser: LinearLayout = view.findViewById(R.id.layoutUserMessage)
        val tvUser: TextView = view.findViewById(R.id.tvUserMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msg = messages[position]
        if (msg.isUser) {
            holder.layoutUser.visibility = View.VISIBLE
            holder.layoutDoctor.visibility = View.GONE
            holder.tvUser.text = msg.message
        } else {
            holder.layoutDoctor.visibility = View.VISIBLE
            holder.layoutUser.visibility = View.GONE
            holder.tvDoctor.text = msg.message
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun setMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    fun getMessages(): List<ChatMessage> = messages.toList()
}
