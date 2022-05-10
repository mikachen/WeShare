package com.zoe.weshare.manage.eventItem

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.databinding.ItemEventManageBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.toDisplayDateFormat
import com.zoe.weshare.util.EventStatusType
import com.zoe.weshare.util.GiftStatusType
import com.zoe.weshare.util.Logger

class EventManageAdapter(
    val viewModel: EventManageViewModel,
    private val onClickListener: OnClickListener,
) : ListAdapter<EventPost, EventManageAdapter.EventItemViewHolder>(DiffCallback) {

    val viewBinderHelper = ViewBinderHelper()
    private var unfilteredList = listOf<EventPost>()

    class EventItemViewHolder(var binding: ItemEventManageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(event: EventPost) {

            binding.apply {
                bindImage(imageEvent, event.image)
                textTitle.text = event.title
                textPostedLocation.text = event.location!!.locationName

                textEventPeriodTime.text = WeShareApplication.instance.getString(
                    R.string.preview_event_time,
                    event.startTime.toDisplayDateFormat(),
                    event.endTime.toDisplayDateFormat()
                )

                textAttendeeCount.text = event.whoAttended.size.toString()
                textVolunteerNeed.text = event.volunteerNeeds.toString()
                textVolunteerCount.text = event.whoVolunteer.size.toString()
                textCheckinCount.text = event.whoCheckedIn.size.toString()
            }

            when (event.status) {
                EventStatusType.WAITING.code -> {
                    binding.textStatus.text = EventStatusType.WAITING.tag
                    binding.textStatus.setBackgroundResource(R.color.event_awaiting_tag)
                }
                EventStatusType.ONGOING.code -> {
                    binding.textStatus.text = EventStatusType.ONGOING.tag
                    binding.textStatus.setBackgroundResource(R.color.app_work_orange3)
                }
                EventStatusType.ENDED.code -> {
                    binding.textStatus.text = EventStatusType.ENDED.tag
                    binding.textStatus.setBackgroundResource(R.color.app_work_dark_grey)
                }

                else -> Logger.d("unKnow status")
            }
        }

        companion object {
            fun from(parent: ViewGroup): EventItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                return EventItemViewHolder(
                    ItemEventManageBinding
                        .inflate(layoutInflater, parent, false)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventItemViewHolder {
        return EventItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: EventItemViewHolder, position: Int) {

        val event = getItem(position)

        viewBinderHelper.setOpenOnlyOne(true)

        if (event.status != EventStatusType.ENDED.code) {
            viewBinderHelper.bind(holder.binding.swipeLayout, event.id)
            holder.binding.swipeLayout.setLockDrag(false)
        } else {
            holder.binding.swipeLayout.setLockDrag(true)
        }

        event?.let {
            holder.bind(event)

            holder.itemView.setOnClickListener {
                onClickListener.onClick(event)
            }

//            holder.binding.buttonAbandon.setOnClickListener {
//                viewModel.userClickAbandon(event)
//            }
//
            holder.binding.btnGetQrcode.setOnClickListener {
                viewModel.generateQrcode(event.id)
            }
        }
    }

    class OnClickListener(val clickListener: (event: EventPost) -> Unit) {
        fun onClick(selectedEvent: EventPost) = clickListener(selectedEvent)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<EventPost>() {
        override fun areItemsTheSame(oldItem: EventPost, newItem: EventPost): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: EventPost, newItem: EventPost): Boolean {
            return oldItem.id == newItem.id
        }
    }

    fun modifyList(list: List<EventPost>, position: Int) {
        unfilteredList = list

        filter(position)
    }

    fun filter(position: Int) {
        val list = mutableListOf<EventPost>()

        when (position) {

            0 -> {
                list.addAll(unfilteredList.filter { it.status == EventStatusType.ONGOING.code })
            }

            1 -> {
                list.addAll(unfilteredList.filter { it.status == EventStatusType.WAITING.code })
            }

            2 -> {
                list.addAll(unfilteredList.filter { it.status == EventStatusType.ENDED.code })
            }

            3 -> {
                list.addAll(unfilteredList)
            }
        }

        viewModel.onFilterEmpty.value = list.isEmpty()
        submitList(list)
    }
}
