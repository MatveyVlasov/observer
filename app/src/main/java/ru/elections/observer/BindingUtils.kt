package ru.elections.observer

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.elections.observer.database.*
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
                ACTIONS.ADD -> R.drawable.ic_baseline_add_box_20
                else -> R.drawable.ic_baseline_access_time_20
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

        @JvmStatic
        @BindingAdapter(value = ["election", "itemTurnout"], requireAll = true)
        fun TextView.setItemTurnout(election: Election, item: Action) {
            var turnout = item.actionTotal / election.totalVoters.toDouble() * 100.0
            turnout = maxOf(0.0, turnout)
            text = String.format("%d (%2.2f %%)", item.actionTotal, turnout)
        }

//        @JvmStatic
//        @BindingAdapter("itemOfficialTotal")
//        fun TextView.setItemOfficialTotal(item: Action) {
//            text = if (item.officialTotal >= 0) item.officialTotal.toString() else "-"
//        }

        @JvmStatic
        @BindingAdapter(value = ["election", "itemOfficialTurnout"], requireAll = true)
        fun TextView.setItemOfficialTurnout(election: Election, item: Action) {
            val turnout = item.officialTotal / election.totalVoters.toDouble() * 100.0
            text = if (item.officialTotal >= 0)
                String.format("%d (%2.2f %%)", item.officialTotal, turnout) else "-"
        }
    }
}