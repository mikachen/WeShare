package com.zoe.weshare.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.data.Cards
import com.zoe.weshare.databinding.ItemEventCardsViewBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.toDisplayFormat

// TODO if 融合卡片內容，把贈品 活動取出 標題 圖片 刊登時間 地點名 來顯示recyclerView??

class CardStackAdapter(private val onClickListener: StackViewOnClickListener) : RecyclerView.Adapter<CardStackAdapter.CardsViewHolder>() {

    var list: List<Cards> = emptyList()

    class CardsViewHolder(val binding: ItemEventCardsViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Cards) {

            binding.textTitle.text = data.title
            binding.textPostedLocation.text = data.title
            binding.textDiscontinuedCountdown.text = data.createdTime.toDisplayFormat()

            bindImage(binding.image, data.image)
        }
    }

    class StackViewOnClickListener(val clickListener: (selectedProduct: Cards) -> Unit) {
        fun onClick(selectedCard: Cards) = clickListener(selectedCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardsViewHolder {
        return CardsViewHolder(
            ItemEventCardsViewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CardsViewHolder, position: Int) {
        val data = list[position]
        holder.bind(data)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(data)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun onListUpdate(dataList: List<Cards>) {
        list = dataList
    }
}
