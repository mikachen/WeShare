package com.zoe.weshare.data

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PostLocation(
    val locationName: String = "",
    val latitude: String = "",
    val longitude: String = ""
) : Parcelable {

    val getLocation: LatLng
        get() {
            return LatLng(latitude.toDouble(), longitude.toDouble())
        }
}
