package com.zoe.weshare.map

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.data.Cards
import com.zoe.weshare.databinding.ItemCardGalleryViewBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.toDisplayFormat
import com.zoe.weshare.util.UserManager

class CardGalleryAdapter(
    val viewModel: MapViewModel,
    private val onClickListener: CardOnClickListener,
) :
    RecyclerView.Adapter<CardGalleryAdapter.CardsViewHolder>() {

    private var list: List<Cards>? = null
    private lateinit var likeAnimation: ScaleAnimation

    init {
        setupLikeBtn()
    }

    class CardsViewHolder(val binding: ItemCardGalleryViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Cards) {

            binding.apply {

                textTitle.text = data.title
                textPostedLocation.text = data.postLocation?.locationName
                textPostedTime.text = data.createdTime.toDisplayFormat()
                textDescriptionShort.text = data.description
                bindImage(image, data.image)

                if (data.postType == EVENT_CARD) {
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
        val card = list?.get(getRealPosition(position))

        card?.let {
            holder.bind(card)

            val isUserLiked = card.whoLiked.contains(UserManager.weShareUser!!.uid)
            holder.binding.clickableView.setOnClickListener {
                onClickListener.onClick(card)
            }
            holder.binding.buttonLike.setOnClickListener {

                it.startAnimation(likeAnimation)
                viewModel.onPostLikePressed(card, isUserLiked)
            }

            holder.binding.buttonLike.isChecked = isUserLiked
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

    private fun setupLikeBtn() {
        likeAnimation = ScaleAnimation(
            0.7f,
            1.0f,
            0.7f,
            1.0f,
            Animation.RELATIVE_TO_SELF,
            0.7f,
            Animation.RELATIVE_TO_SELF,
            0.7f
        )
        likeAnimation.duration = 500
        likeAnimation.interpolator = BounceInterpolator()
    }

}
