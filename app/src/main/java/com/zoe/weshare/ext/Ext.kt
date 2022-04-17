package com.zoe.weshare.ext

import android.icu.text.SimpleDateFormat
import android.widget.ImageView
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.zoe.weshare.R
import java.util.*

fun Long.toDisplayFormat(): String {
    return SimpleDateFormat("yyyy.MM.dd\t\nhh:mm", Locale.TAIWAN).format(this)
}


fun Long.toDisplaySentTime(): String {
    return SimpleDateFormat("hh:mm", Locale.TAIWAN).format(this)
}


fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = it.toUri().buildUpon().scheme("https").build()
        Glide.with(imgView.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.tiger_pngrepo_com)
                    .error(R.drawable.tiger_pngrepo_com)
            )
            .into(imgView)
    }
}