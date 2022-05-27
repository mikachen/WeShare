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
) : RecyclerView.Adapter<CardGalleryAdapter.CardsViewHolder>() {

    private var list: List<Cards>? = null
    private lateinit var likeAnimation: ScaleAnimation

    init {
        setupLikeAnimation()
    }

    class CardsViewHolder(val binding: ItemCardGalleryViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(card: Cards) {

            binding.apply {

                textTitle.text = card.title
                textPostedLocation.text = card.postLocation.locationName
                textPostedTime.text = card.createdTime.toDisplayFormat()
                textDescriptionShort.text = card.description
                bindImage(image, card.image)

                if (card.postType == EVENT_CARD) {
                    textPostedTime.text = card.eventTime
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

            val hasUserLiked: Boolean = hasUserLikedBefore(card.whoLiked)

            holder.binding.clickableView.setOnClickListener {
                onClickListener.onClick(card)
            }
            holder.binding.buttonLike.setOnClickListener {

                it.startAnimation(likeAnimation)
                viewModel.onPostLikePressed(card, hasUserLiked)
            }

            holder.binding.buttonLike.isChecked = hasUserLiked
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

    private fun setupLikeAnimation() {
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

    private fun hasUserLikedBefore(list: List<String>): Boolean {
        return list.contains(UserManager.weShareUser.uid)
    }
}
