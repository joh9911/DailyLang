package com.example.dailylang.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dailylang.data.ChatModel
import com.example.dailylang.data.LanguageModel
import com.example.dailylang.databinding.ItemChatReceiveBinding
import com.example.dailylang.databinding.ItemChatSendBinding
import com.example.dailylang.databinding.ItemLanguageBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ChatListAdapter:
    ListAdapter<ChatModel, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }


    inner class SentMessageViewHolder(val binding: ItemChatSendBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatModel: ChatModel){
            binding.textViewMessage.text = chatModel.text
            binding.textViewTime.text = formatTimestamp(chatModel.timestamp)
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        // SimpleDateFormat 인스턴스를 생성하고 원하는 시간 형식을 지정합니다.
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        // 사용자의 현재 시간대를 설정합니다.
        formatter.timeZone = TimeZone.getDefault()

        // timestamp를 Date 객체로 변환한 다음, 지정한 형식의 문자열로 변환합니다.
        return formatter.format(timestamp)
    }

    inner class ReceivedMessageViewHolder(val binding: ItemChatReceiveBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    itemClickListener.onClick(adapterPosition)
            }
        }
        fun bind(chatModel: ChatModel){
            binding.textViewMessage.text = chatModel.text
            binding.textViewTime.text = formatTimestamp(chatModel.timestamp)
        }
    }
    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.isSent) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    object DiffCallback : DiffUtil.ItemCallback<ChatModel>() {
        override fun areItemsTheSame(oldItem: ChatModel, newItem: ChatModel): Boolean {
            // 여기서는 문자열 자체를 고유 식별자로 사용하고 있습니다.
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ChatModel, newItem: ChatModel): Boolean {
            // 항목의 내용이 같은지 확인합니다.
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // 새 뷰 생성
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val binding = ItemChatSendBinding.inflate(LayoutInflater.from(parent.context),
                    parent,
                    false)
                SentMessageViewHolder(binding)
            }
            VIEW_TYPE_RECEIVED -> {

                val binding = ItemChatReceiveBinding.inflate(LayoutInflater.from(parent.context),
                    parent,
                    false)
                ReceivedMessageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder.itemViewType) {
            VIEW_TYPE_SENT -> (holder as SentMessageViewHolder).bind(message)
            VIEW_TYPE_RECEIVED -> (holder as ReceivedMessageViewHolder).bind(message)
        }
    }

    interface OnItemClickListener {
        fun onClick(position: Int)

    }
    // (3) 외부에서 클릭 시 이벤트 설정
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }
    // (4) setItemClickListener로 설정한 함수 실행
    private lateinit var itemClickListener : OnItemClickListener
}