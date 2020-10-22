package ru.vvdev.wistory.internal.presentation.items

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import kotlin.math.roundToInt
import kotlinx.android.synthetic.main.wistory_item_voting.view.*
import ru.vvdev.wistory.R
import ru.vvdev.wistory.UiConfig
import ru.vvdev.wistory.internal.data.models.OptionModel
import ru.vvdev.wistory.internal.presentation.callback.ItemSelected

internal class VoteItem(
    val voteModel: OptionModel,
    private val context: Context,
    private val theme: UiConfig.Theme,
    private val callback: ItemSelected?,
    private val votedId: String,
    val storyId: String,
    val sumVotes: Int,
    val snap: Int,
    val replay: Boolean
) : AbstractFlexibleItem<VoteItem.ViewHolder>() {

    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?,
        holder: ViewHolder?,
        position: Int,
        payloads: MutableList<Any>?
    ) {
        holder?.bind(voteModel, context, theme, callback, votedId, storyId, sumVotes, snap, replay)
    }

    override fun equals(other: Any?): Boolean {
        return if (other is VoteItem) {
            other.voteModel == this.voteModel
        } else {
            false
        }
    }

    override fun createViewHolder(
        view: View?,
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?
    ): ViewHolder {
        return ViewHolder(
            view,
            adapter
        )
    }

    override fun getLayoutRes(): Int {
        return R.layout.wistory_item_voting
    }

    override fun hashCode(): Int {
        var result = voteModel.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }

    class ViewHolder(
        view: View?,
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?
    ) : FlexibleViewHolder(view, adapter) {
        private val seekBar: SeekBar = itemView.progress as SeekBar
        private val textVote: TextView = itemView.tvVote as TextView
        private val tvPercent: TextView = itemView.tvPercent as TextView
        private val ivSelect: ImageView = itemView.ivSelect as ImageView

        private lateinit var context: Context

        fun bind(
            model: OptionModel,
            context: Context,
            theme: UiConfig.Theme,
            callback: ItemSelected?,
            votedId: String,
            storyId: String,
            sumVotes: Int,
            snap: Int,
            replay: Boolean
        ) {

            this.context = context

            textVote.setOnClickListener {
                if ((replay || votedId == "-1") && votedId != model.optionId)
                    callback?.itemSelected(storyId, model.optionId, votedId, snap)
            }

            with(model) {
                textVote.text = text
                if (votedId == "-1") {
                    tvPercent.visibility = View.GONE
                    ivSelect.visibility = View.GONE
                } else {
                    vote(votedId, model, sumVotes)
                }
            }

            setColor(theme, context)
        }

        private fun vote(votedId: String, option: OptionModel, sumVotes: Int) {
            seekBar.isClickable = false
            seekBar.isFocusable = false
            seekBar.isEnabled = false
            tvPercent.animateShow(true)
            if (votedId == option.optionId) {
                ivSelect.animateShow(true)
            } else {
                ivSelect.visibility = View.GONE
            }
            if (option.votes > 0) {
                val percent = (option.votes.toFloat() / sumVotes.toFloat() * 100).roundToInt()
                val percentText =
                    "$percent%"
                tvPercent.text = percentText
                seekBar.progress = percent
            } else {
                tvPercent.text = "0%"
                seekBar.progress = 0
            }
        }

        private fun setColor(theme: UiConfig.Theme, context: Context) {
            if (theme == UiConfig.Theme.LIGHT) {
                seekBar.progressDrawable = context.getDrawable(R.drawable.wistory_vote_item_light)
                textVote.setTextColor(getColorFromContext(R.color.wistory_black))
                tvPercent.setTextColor(getColorFromContext(R.color.wistory_tv_dark_gray))
                ivSelect.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.wistory_tv_dark_gray
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                seekBar.progressDrawable = context.getDrawable(R.drawable.wistory_vote_item_dark)
                textVote.setTextColor(Color.WHITE)
                tvPercent.setTextColor(Color.WHITE)
                ivSelect.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.wistory_ss_progress_centerColor_white
                    )
                )
            }
        }

        private fun getColorFromContext(colorAdress: Int): Int {
            return context.resources.getColor(colorAdress)
        }

        private fun View.animateShow(userSelect: Boolean) {
            if (userSelect) {
                this.alpha = 0f
                this.animate().alpha(1f).withStartAction { this.visibility = View.VISIBLE }
                    .setDuration(200L).start()
            } else {
                this.visibility = View.GONE
            }
        }
    }
}
