package com.zoe.weshare.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.databinding.ItemHeaderTvBinding
import com.zoe.weshare.ext.bindImage

class HeaderAdapter(private val onClickListener: HeaderOnClickListener) :
    RecyclerView.Adapter<HeaderAdapter.HeaderViewHolder>() {

    private var list: List<EventPost>? = null

    class HeaderViewHolder(val binding: ItemHeaderTvBinding) :
        RecyclerView.ViewHolder(binding.root)

    class HeaderOnClickListener(val doNothing: (selected: EventPost) -> Unit) {
        fun onClick(selectedEvent: EventPost) = doNothing(selectedEvent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        return HeaderViewHolder(
            ItemHeaderTvBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        val data = list?.get(getRealPosition(position))

        data?.let {
            bindImage(holder.binding.headerImageTv, data.image)

            holder.itemView.setOnClickListener { onClickListener.onClick(data) }
        }
    }

    override fun getItemCount(): Int {
        return list?.let { Int.MAX_VALUE } ?: 0
    }

    private fun getRealPosition(position: Int): Int = list?.let {
        position % it.size
    } ?: 0

    fun submitEvents(dataList: List<EventPost>) {
        this.list = dataList
        notifyDataSetChanged()
    }
}
