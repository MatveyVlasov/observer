package ru.elections.observer.utils

import android.view.View
import android.view.animation.BounceInterpolator
import android.view.animation.TranslateAnimation
import com.hyy.highlightpro.parameter.Constraints
import com.hyy.highlightpro.parameter.HighlightParameter
import com.hyy.highlightpro.parameter.MarginOffset
import com.hyy.highlightpro.shape.RectShape
import com.hyy.highlightpro.util.dp
import ru.elections.observer.utils.Animation.animation

enum class Training {
    NO, FIRST, SECOND
}


const val ID_START_TITLE = 0
const val ID_START_MAIN = 100
const val ID_START_TURNOUT = 200
const val ID_START_PAST = 300
const val GAP = 50

object Animation {
    val animation = TranslateAnimation(-500f,0f,0f,0f).apply {
        duration = 500
        interpolator = BounceInterpolator()
    }
}

fun getHint(view: View, buttonId: Int,
            verticalConstraints: Constraints = Constraints.TopToBottomOfHighlight,
            horizontalConstraints: Constraints = Constraints.StartToStartOfHighlight):
        HighlightParameter {

    return HighlightParameter.Builder()
        .setHighlightViewId(buttonId)
        .setTipsView(view)
        .setHighlightShape(RectShape(4f.dp, 4f.dp, 6f))
        .setHighlightHorizontalPadding(8f.dp)
        .setConstraints(verticalConstraints + horizontalConstraints)
        .setMarginOffset(MarginOffset(4.dp, 4.dp, 4.dp, 4.dp))
        .setTipViewDisplayAnimation(animation)
        .build()
}