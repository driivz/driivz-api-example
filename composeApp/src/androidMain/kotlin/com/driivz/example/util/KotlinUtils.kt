package com.driivz.example.util

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue


fun Int.dpToPx(context: Context): Float {
    val r: Resources = context.resources
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        r.displayMetrics
    )
}
