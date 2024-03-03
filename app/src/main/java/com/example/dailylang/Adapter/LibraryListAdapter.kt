package com.example.dailylang.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dailylang.database.DailyExpressions
import com.example.dailylang.database.Projects
import com.example.dailylang.databinding.ItemLibraryBinding
import com.example.dailylang.databinding.MainRecyclerViewItemBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class LibraryListAdapter:
    ListAdapter<DailyExpressions, LibraryListAdapter.MyViewHolder>(DiffCallback) {

    inner class MyViewHolder(val binding: ItemLibraryBinding) : RecyclerView.ViewHolder(binding.root) {
        init {

        }
        fun bind(dailyExpressions: DailyExpressions){
            binding.learningTextView.text = dailyExpressions.expression
            binding.defaultTextView.text = dailyExpressions.translation
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

    companion object DiffCallback : DiffUtil.ItemCallback<DailyExpressions>() {
        override fun areItemsTheSame(oldItem: DailyExpressions, newItem: DailyExpressions): Boolean {
            // 여기서는 문자열 자체를 고유 식별자로 사용하고 있습니다.
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DailyExpressions, newItem: DailyExpressions): Boolean {
            // 항목의 내용이 같은지 확인합니다.
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // 새 뷰 생성
        val binding = ItemLibraryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(currentList[position])
    }


}