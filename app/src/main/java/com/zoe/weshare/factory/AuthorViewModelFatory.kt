package com.zoe.weshare.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.detail.askgift.AskForGiftViewModel
import com.zoe.weshare.detail.checkin.CheckInViewModel
import com.zoe.weshare.detail.event.EventDetailViewModel
import com.zoe.weshare.detail.gift.GiftDetailViewModel
import com.zoe.weshare.manage.distribution.DistributeViewModel
import com.zoe.weshare.manage.eventItem.EventManageViewModel
import com.zoe.weshare.manage.giftsItem.GiftManageViewModel
import com.zoe.weshare.map.MapViewModel
import com.zoe.weshare.message.ChatRoomViewModel
import com.zoe.weshare.message.roomlist.RoomListViewModel
import com.zoe.weshare.notification.NotificationViewModel
import com.zoe.weshare.posting.event.PostEventViewModel
import com.zoe.weshare.posting.gift.PostGiftViewModel
import com.zoe.weshare.profile.ProfileViewModel
import com.zoe.weshare.profile.editmode.EditInfoViewModel
import com.zoe.weshare.report.ReportViewModel

/**
 * Factory for all ViewModels which need [UserInfo].
 */
@Suppress("UNCHECKED_CAST")
class AuthorViewModelFactory(
    private val repository: WeShareRepository,
    private val userInfo: UserInfo?,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(PostEventViewModel::class.java)) {
            return PostEventViewModel(repository, userInfo) as T
        }

        if (modelClass.isAssignableFrom(PostGiftViewModel::class.java)) {
            return PostGiftViewModel(repository, userInfo) as T
        }

        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(repository, userInfo) as T
        }

        if (modelClass.isAssignableFrom(AskForGiftViewModel::class.java)) {
            return AskForGiftViewModel(repository, userInfo) as T
        }

        if (modelClass.isAssignableFrom(ChatRoomViewModel::class.java)) {
            return ChatRoomViewModel(repository, userInfo) as T
        }

        if (modelClass.isAssignableFrom(RoomListViewModel::class.java)) {
            return RoomListViewModel(repository, userInfo) as T
        }

        if (modelClass.isAssignableFrom(EventDetailViewModel::class.java)) {
            return EventDetailViewModel(repository, userInfo) as T
        }

        if (modelClass.isAssignableFrom(GiftDetailViewModel::class.java)) {
            return GiftDetailViewModel(repository, userInfo) as T
        }

        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(repository, userInfo) as T
        }

        if (modelClass.isAssignableFrom(GiftManageViewModel::class.java)) {
            return GiftManageViewModel(repository, userInfo) as T
        }

        if (modelClass.isAssignableFrom(EventManageViewModel::class.java)) {
            return EventManageViewModel(repository, userInfo) as T
        }

        if (modelClass.isAssignableFrom(DistributeViewModel::class.java)) {
            return DistributeViewModel(repository, userInfo) as T
        }

        if (modelClass.isAssignableFrom(EditInfoViewModel::class.java)) {
            return EditInfoViewModel(repository, userInfo) as T
        }

        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            return NotificationViewModel(repository, userInfo) as T
        }

        if (modelClass.isAssignableFrom(CheckInViewModel::class.java)) {
            return CheckInViewModel(repository, userInfo) as T
        }

        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            return ReportViewModel(repository, userInfo) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
