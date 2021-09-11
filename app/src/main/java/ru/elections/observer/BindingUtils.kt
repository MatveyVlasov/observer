package ru.elections.observer

import android.annotation.SuppressLint
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import ru.elections.observer.database.*
import java.text.DateFormat.getDateInstance
import java.text.SimpleDateFormat
import java.util.*

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

        @JvmStatic
        @BindingAdapter("itemDate")
        fun TextView.setItemDate(item: Action) {
            text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)
                .format(item.actionDate)
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
            text = String.format(context.getString(R.string.turnout_record), item.actionTotal, turnout)
        }

        @JvmStatic
        @BindingAdapter(value = ["election", "itemOfficialTurnout"], requireAll = true)
        fun TextView.setItemOfficialTurnout(election: Election, item: Action) {
            var turnout = item.officialTotal / election.totalVoters.toDouble() * 100.0
            turnout = maxOf(0.0, turnout)
            text = if (item.officialTotal >= 0)
                String.format(context.getString(R.string.turnout_record), item.officialTotal, turnout) else "-"
        }

        @JvmStatic
        @BindingAdapter("electionDate")
        fun TextView.setElectionDate(election: Election) {
            val str = SimpleDateFormat("dd.MM.yyyy - ", Locale.US)
                .format(election.dateStart) +
                    SimpleDateFormat("dd.MM.yyyy", Locale.US)
                .format(election.dateEnd)
            text = str
        }

        @JvmStatic
        @BindingAdapter("electionPollingStation")
        fun TextView.setElectionPollingStation(election: Election) {
            text = String.format(
                context.getString(R.string.polling_station), election.pollingStation)
        }
    }
}