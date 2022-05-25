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
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: EventPost) {
            binding.apply {

                bindImage(headerImageTv, event.image)
                textTvTitle.text = event.title
            }
        }
    }

    class HeaderOnClickListener(val clicked: (selected: EventPost) -> Unit) {
        fun onClick(selectedEvent: EventPost) = clicked(selectedEvent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        return HeaderViewHolder(
            ItemHeaderTvBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        val event = list?.get(getRealPosition(position))

        event?.let {
            holder.bind(it)
            holder.itemView.setOnClickListener { onClickListener.onClick(event) }
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
