package com.zoe.weshare.ext

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.zoe.weshare.R
import java.util.*


fun Long.toDisplayFormat(): String {
    return SimpleDateFormat("yyyy.MM.dd hh:mm", Locale.TAIWAN).format(this)
}

fun Long.toDisplaySentTime(): String {
    return SimpleDateFormat("hh:mm", Locale.TAIWAN).format(this)
}

fun bindImage(imgView: ImageView, imgUrl: String?) {

    val drawable = CircularProgressDrawable(imgView.context)
    drawable.setColorSchemeColors(R.color.app_work_orange1,
        R.color.app_work_orange2,
        R.color.app_work_orange3)
    drawable.centerRadius = 50f
    drawable.strokeWidth = 5f
    drawable.start()

    imgUrl?.let {
        val imgUri = it.toUri().buildUpon().scheme("https").build()
        Glide.with(imgView.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                    .placeholder(drawable)
                    .error(drawable)
            )
            .into(imgView)
    }
}


fun generateSmallIcon(context: Context, icon: Int): Bitmap {
    val height = 120
    val width = 120
    val bitmap = BitmapFactory.decodeResource(context.resources, icon)
    return Bitmap.createScaledBitmap(bitmap, width, height, false)
}
