package com.example.dailylang.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dailylang.R
import com.example.dailylang.data.LanguageModel
import com.example.dailylang.databinding.ItemLanguageBinding

class LanguageListAdapter :
    ListAdapter<LanguageModel, LanguageListAdapter.MyViewHolder>(DiffCallback) {

    inner class MyViewHolder(val binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    itemClickListener.onClick(it, adapterPosition)
            }

        }
        fun bind(languageModel: LanguageModel){
            binding.countryImage.setImageResource(languageModel.imageResId)
            binding.textView.text = languageModel.countryName
        }

    }

    companion object DiffCallback : DiffUtil.ItemCallback<LanguageModel>() {
        override fun areItemsTheSame(oldItem: LanguageModel, newItem: LanguageModel): Boolean {
            // 여기서는 문자열 자체를 고유 식별자로 사용하고 있습니다.
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: LanguageModel, newItem: LanguageModel): Boolean {
            // 항목의 내용이 같은지 확인합니다.
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // 새 뷰 생성
        val binding = ItemLanguageBinding.inflate(
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
