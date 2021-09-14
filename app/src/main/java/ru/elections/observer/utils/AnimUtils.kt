package ru.elections.observer.utils

import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation

/**
 * Created by heCunCun on 2021/4/12
 */
object AnimUtils {
    fun getScaleAnimation(): Animation {
        val scaleAnimator = ScaleAnimation(
            0.5f,
            1f,
            0.5f,
            1f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        scaleAnimator.duration = 500
        scaleAnimator.interpolator = BounceInterpolator()
        return scaleAnimator
    }

}