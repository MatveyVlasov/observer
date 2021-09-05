package ru.elections.observer

import android.annotation.SuppressLint
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import ru.elections.observer.database.ACTIONS
import ru.elections.observer.database.Action
import java.text.SimpleDateFormat

class BindingUtils {
    companion object {
        @JvmStatic
        @BindingAdapter("itemId")
        fun TextView.setItemId(item: Action) {
            text = item.actionId.toString()
        }

        @JvmStatic
        @BindingAdapter("itemAction")
        fun ImageView.setItemAction(item: Action) {
            setImageResource(when (item.actionType) {
                ACTIONS.COUNT -> R.drawable.ic_baseline_add_20
                ACTIONS.REMOVE -> R.drawable.ic_baseline_horizontal_rule_20
                ACTIONS.SET -> R.drawable.ic_baseline_edit_20
                else -> R.drawable.ic_baseline_add_box_20
            })
        }

        @SuppressLint("SimpleDateFormat")
        @JvmStatic
        @BindingAdapter("itemDate")
        fun TextView.setItemDate(item: Action) {
            text = SimpleDateFormat("dd.MM.yyyy HH:mm")
                .format(item.actionDate).toString()
        }

        @JvmStatic
        @BindingAdapter("itemTotal")
        fun TextView.setItemTotal(item: Action) {
            text = item.actionTotal.toString()
        }
    }
}