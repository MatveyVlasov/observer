package ru.elections.observer

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.databinding.BindingAdapter
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
        fun TextView.setItemAction(item: Action) {
            text = item.actionType.toString()
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