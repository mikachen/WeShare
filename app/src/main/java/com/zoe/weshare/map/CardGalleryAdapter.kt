package com.zoe.weshare.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.R
import com.zoe.weshare.data.Cards
import com.zoe.weshare.databinding.ItemCardGalleryViewBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.toDisplayFormat
import com.zoe.weshare.util.Util.getString
import com.zoe.weshare.util.Util.getStringWithStrParm

class CardGalleryAdapter(private val onClickListener: CardOnClickListener) :
    RecyclerView.Adapter<CardGalleryAdapter.CardsViewHolder>() {

    private var list: List<Cards>? = null

    class CardsViewHolder(val binding: ItemCardGalleryViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Cards) {

            binding.apply {

                textTitle.text = data.title
                textPostedLocation.text = data.postLocation?.locationName
                textPostedTime.text = data.createdTime.toDisplayFormat()
                bindImage(image, data.image)

                if(data.postType == EVENT_CARD){
                    titlePostedTime.text = getString(R.string.preview_event_time_title)
                    textPostedTime.text = data.eventTime
                }
            }
        }
    }

    class CardOnClickListener(val clickListener: (selectedProduct: Cards) -> Unit) {
        fun onClick(selectedCard: Cards) = clickListener(selectedCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardsViewHolder {
        return CardsViewHolder(
            ItemCardGalleryViewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CardsViewHolder, position: Int) {
        val data = list?.get(getRealPosition(position))

        data?.let {
            holder.bind(data)
            holder.binding.clickableView.setOnClickListener {
                onClickListener.onClick(data)
            }
        }
    }

    override fun getItemCount(): Int {
        return list?.let { Int.MAX_VALUE } ?: 0
    }

    private fun getRealPosition(position: Int): Int = list?.let {
        position % it.size
    } ?: 0

    fun submitCards(dataList: List<Cards>) {
        this.list = dataList
        notifyDataSetChanged()
    }
}
