package com.zoe.weshare.ext

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.util.DisplayMetrics
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.zoe.weshare.R
import java.util.*
import java.util.concurrent.TimeUnit

fun Long.toDisplayFormat(): String {
    return SimpleDateFormat("yyyy.MM.dd hh:mm", Locale.TAIWAN).format(this)
}

fun Long.toDisplaySentTime(): String {
    return SimpleDateFormat("hh:mm", Locale.TAIWAN).format(this)
}

fun Long.toDisplayDateFormat(): String {
    return SimpleDateFormat("yyyy.MM.dd", Locale.TAIWAN).format(this)
}

fun bindImage(imgView: ImageView, imgUrl: String?) {

    val drawable = CircularProgressDrawable(imgView.context)
    drawable.setColorSchemeColors(
        R.color.app_work_orange1,
        R.color.app_work_orange2,
        R.color.app_work_orange3
    )
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


/**
 * resize Map marker icon
 * */
fun generateSmallIcon(context: Context, icon: Int): Bitmap {
    val height = 120
    val width = 120
    val bitmap = BitmapFactory.decodeResource(context.resources, icon)
    return Bitmap.createScaledBitmap(bitmap, width, height, false)
}

/**
 * set scrollDuration for Home page Log ticker when scrolling
 * */
fun RecyclerView.smoothSnapToPosition(position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_START) {
    val scrollDuration = 8000f;
    val smoothScroller = object : LinearSmoothScroller(this.context) {
        override fun getVerticalSnapPreference(): Int = snapMode
        override fun getHorizontalSnapPreference(): Int = snapMode
        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
            return scrollDuration / computeVerticalScrollRange();
        }
    }
    smoothScroller.targetPosition = position
    layoutManager?.startSmoothScroll(smoothScroller)
}

fun Long.getTimeAgoString(): String {

    val calendar = Calendar.getInstance()
    calendar.time = Date(this)

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val currentCalendar = Calendar.getInstance()

    val currentYear = currentCalendar.get(Calendar.YEAR)
    val currentMonth = currentCalendar.get(Calendar.MONTH)
    val currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH)
    val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = currentCalendar.get(Calendar.MINUTE)

    return if (year < currentYear) {
        val interval = currentYear - year
        if (interval == 1) "$interval year ago" else "$interval years ago"
    } else if (month < currentMonth) {
        val interval = currentMonth - month
        if (interval == 1) "$interval month ago" else "$interval months ago"
    } else if (day < currentDay) {
        val interval = currentDay - day
        if (interval == 1) "$interval day ago" else "$interval days ago"
    } else if (hour < currentHour) {
        val interval = currentHour - hour
        if (interval == 1) "$interval hour ago" else "$interval hours ago"
    } else if (minute < currentMinute) {
        val interval = currentMinute - minute
        if (interval == 1) "$interval minute ago" else "$interval minutes ago"
    } else {
        "just now"
    }
}

// Method to get days hours minutes seconds from milliseconds
fun getCountDownTimeString(millisUntilFinished: Long): String {
    var millisTillEnd: Long = millisUntilFinished

    val days = TimeUnit.MILLISECONDS.toDays(millisTillEnd)
    millisTillEnd -= TimeUnit.DAYS.toMillis(days)

    val hours = TimeUnit.MILLISECONDS.toHours(millisTillEnd)
    millisTillEnd -= TimeUnit.HOURS.toMillis(hours)

    val minutes = TimeUnit.MILLISECONDS.toMinutes(millisTillEnd)
    millisTillEnd -= TimeUnit.MINUTES.toMillis(minutes)

    val seconds = TimeUnit.MILLISECONDS.toSeconds(millisTillEnd)

    return if (days != 0L) {
        String.format(
            Locale.getDefault(),
            "%02d day %02d hour %02d min %02d sec left",
            days, hours, minutes, seconds
        )
    } else if (hours != 0L) {
        String.format(
            Locale.getDefault(),
            "%02d hour %02d min %02d sec left",
            hours, minutes, seconds
        )
    } else if (minutes != 0L) {
        String.format(
            Locale.getDefault(),
            "%02d min %02d sec left",
            minutes, seconds
        )
    } else {
        String.format(
            Locale.getDefault(),
            "%02d sec left",
            seconds
        )
    }
}
