package com.zoe.weshare.herorank

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.R
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.databinding.ItemHeroRankBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.util.Util.getStringWithIntParm

class HeroRankAdapter(private val onClickListener: HeroOnClickListener) :
    ListAdapter<UserProfile, HeroRankAdapter.RankViewHolder>(DiffCallback) {

    private var rankNumber = 4

    class RankViewHolder(var binding: ItemHeroRankBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserProfile) {
            binding.apply {
                bindImage(imageUserImage, user.image)
                textUserName.text = user.name
                textContribution.text =
                    getStringWithIntParm(R.string.hero_contribution,
                        user.contribution.totalContribution)
            }
        }

        companion object {
            fun from(parent: ViewGroup): RankViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                return RankViewHolder(
                    ItemHeroRankBinding
                        .inflate(layoutInflater, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RankViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)

        holder.binding.textRankNumber.text = rankNumber.toString()
        rankNumber += 1

        holder.itemView.setOnClickListener { onClickListener.onClick(user) }

        //hide last divider view
        if (position == itemCount - 1) {
            holder.binding.divider.visibility = View.INVISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankViewHolder {
        return RankViewHolder.from(parent)
    }

    class HeroOnClickListener(val clicked: (user: UserProfile) -> Unit) {
        fun onClick(selectedUser: UserProfile) = clicked(selectedUser)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<UserProfile>() {
        override fun areItemsTheSame(oldItem: UserProfile, newItem: UserProfile): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: UserProfile, newItem: UserProfile): Boolean {
            return oldItem == newItem
        }
    }

    fun modifyList(users: List<UserProfile>) {

        val removedTopThree = users.slice(3 until users.size)

        submitList(removedTopThree)
    }
}
