package com.example.mymenudemo

import android.content.Context
import android.util.TypedValue


class DensityUtil private constructor() {
    companion object {
        /**
         * dp转px
         */
        fun dp2px(context: Context, dpVal: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.resources.displayMetrics
            ).toInt()
        }
    }

}