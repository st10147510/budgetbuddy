package com.budgetbuddy.ui.debt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.budgetbuddy.databinding.ItemScheduleDebtBinding
import com.budgetbuddy.databinding.ItemScheduleHeaderBinding
import com.budgetbuddy.util.CurrencyFormatter

class ScheduleAdapter : ListAdapter<ScheduleRow, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_DEBT = 1

        private val DIFF = object : DiffUtil.ItemCallback<ScheduleRow>() {
            override fun areItemsTheSame(old: ScheduleRow, new: ScheduleRow): Boolean =
                stableId(old) == stableId(new)

            override fun areContentsTheSame(old: ScheduleRow, new: ScheduleRow): Boolean =
                old == new

            private fun stableId(row: ScheduleRow) = when (row) {
                is ScheduleRow.Header  -> "h_${row.month}"
                is ScheduleRow.DebtRow -> "d_${row.debtName}"
            }
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is ScheduleRow.Header  -> VIEW_TYPE_HEADER
        is ScheduleRow.DebtRow -> VIEW_TYPE_DEBT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_HEADER) {
            HeaderVH(ItemScheduleHeaderBinding.inflate(inflater, parent, false))
        } else {
            DebtVH(ItemScheduleDebtBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = getItem(position)) {
            is ScheduleRow.Header  -> (holder as HeaderVH).bind(row)
            is ScheduleRow.DebtRow -> (holder as DebtVH).bind(row)
        }
    }

    inner class HeaderVH(private val b: ItemScheduleHeaderBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(row: ScheduleRow.Header) {
            b.tvMonthHeader.text = b.root.context.getString(
                com.budgetbuddy.R.string.month_header, row.month
            )
        }
    }

    inner class DebtVH(private val b: ItemScheduleDebtBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(row: ScheduleRow.DebtRow) {
            b.tvDebtName.text = row.debtName
            val ctx = b.root.context
            b.tvPayment.text = CurrencyFormatter.format(ctx, row.payment)
            b.tvBalance.text = ctx.getString(
                com.budgetbuddy.R.string.schedule_balance, CurrencyFormatter.format(ctx, row.remainingBalance)
            )
        }
    }
}
