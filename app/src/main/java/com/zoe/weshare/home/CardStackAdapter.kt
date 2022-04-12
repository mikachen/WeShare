package com.zoe.weshare.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.databinding.ItemEventCardsViewBinding

class CardStackAdapter(private var list: List<String> = emptyList()) : RecyclerView.Adapter<CardStackAdapter.CardsViewHolder>() {

    class CardsViewHolder(val binding: ItemEventCardsViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            binding.text.text = text
        }
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
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setSpots(list: List<String>) {
        this.list = list
    }

    fun getSpots(): List<String> {
        return list
    }
}
