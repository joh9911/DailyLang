package com.example.dailylang.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dailylang.R
import com.example.dailylang.database.Projects
import com.example.dailylang.databinding.MainRecyclerViewItemBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MainActivityListAdapter :
    ListAdapter<Projects, MainActivityListAdapter.MyViewHolder>(DiffCallback) {

    inner class MyViewHolder(val binding: MainRecyclerViewItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    itemClickListener.onClick(it, adapterPosition)
            }
        }
        fun bind(projects: Projects){
            binding.titleTextView.text = projects.title
            binding.dateTextView.text = formatTimestamp(projects.timestamp)
            binding.defaultTextView.text = "${projects.defaultLanguage} to ${projects.learningLanguage}"
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

    companion object DiffCallback : DiffUtil.ItemCallback<Projects>() {
        override fun areItemsTheSame(oldItem: Projects, newItem: Projects): Boolean {
            // 여기서는 문자열 자체를 고유 식별자로 사용하고 있습니다.
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Projects, newItem: Projects): Boolean {
            // 항목의 내용이 같은지 확인합니다.
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // 새 뷰 생성
        val binding = MainRecyclerViewItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)

    }
    // (3) 외부에서 클릭 시 이벤트 설정
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }
    // (4) setItemClickListener로 설정한 함수 실행
    private lateinit var itemClickListener : OnItemClickListener
}
