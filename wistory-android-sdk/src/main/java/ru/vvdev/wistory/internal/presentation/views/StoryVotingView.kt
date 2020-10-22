package ru.vvdev.wistory.internal.presentation.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFlexible
import ru.vvdev.wistory.R
import ru.vvdev.wistory.UiConfig
import ru.vvdev.wistory.internal.data.models.OptionModel
import ru.vvdev.wistory.internal.data.models.StoryVotingAttr
import ru.vvdev.wistory.internal.presentation.callback.ItemSelected
import ru.vvdev.wistory.internal.presentation.items.VoteItem

internal class StoryVotingView(context: Context, attrs: AttributeSet) :
    RelativeLayout(context, attrs),
    ItemSelected {

    private var adapter = FlexibleAdapter<IFlexible<*>>(mutableListOf())
    private var list = mutableListOf<OptionModel>()
    private var replay = false
    private var votedId: String? = null

    private lateinit var textTitle: TextView
    private lateinit var rv: RecyclerView
    private var callback: ItemSelected? = null
    private lateinit var storyVotingAttr: StoryVotingAttr

    init {
        initAttr(attrs)
    }

    private fun initAttr(attrs: AttributeSet) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate(R.layout.wistory_voting_view, this)
        rv = v.findViewById(R.id.rvVotes)
        textTitle = v.findViewById(R.id.tvTitle)

        rv.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rv.setHasFixedSize(true)
        rv.adapter = adapter
        adapter.setNotifyChangeOfUnfilteredItems(true)
        rv.itemAnimator = null
    }

    fun setVotingViewTheme(storyVotingAttr: StoryVotingAttr) {
        this.storyVotingAttr = storyVotingAttr

        setTitle(storyVotingAttr)
    }

    private fun setTitle(storyVotingAttr: StoryVotingAttr) {
        if (storyVotingAttr.text.isNullOrEmpty()) {
            textTitle.visibility = GONE
            return
        }

        textTitle.text = storyVotingAttr.text
        if (storyVotingAttr.theme == UiConfig.Theme.LIGHT) {
            textTitle.setTextColor(Color.WHITE)
        } else {
            textTitle.setTextColor(Color.BLACK)
        }
    }

    fun setVotingViewList(
        voteModels: List<OptionModel>,
        votedId: String,
        storyId: String,
        snap: Int,
        replay: Boolean
    ) {
        this.votedId = votedId
        adapter.clear()
        list = voteModels as MutableList<OptionModel>
        list.forEach {
            adapter.addItem(
                VoteItem(
                    it,
                    context,
                    storyVotingAttr.theme,
                    this,
                    this.votedId!!,
                    storyId,
                    sumVotes(list),
                    snap,
                    replay
                )
            )
        }
        adapter.notifyDataSetChanged()

        this.replay = replay
    }

    fun setCallback(callback: ItemSelected) {
        this.callback = callback
    }

    private fun sumVotes(list: List<OptionModel>): Int {
        var totalSumVotes = 0
        list.forEach { item -> totalSumVotes += item.votes }
        return totalSumVotes
    }

    private fun updateSumVotes(newpoll: String, oldpoll: String) {
        list.forEach { item ->
            if (item.optionId == newpoll) {
                item.votes += 1
                this.votedId = newpoll
            } else if (item.optionId == oldpoll)
                item.votes -= 1
        }
    }

    override fun itemSelected(storyId: String, newpoll: String, oldpoll: String, sheet: Int) {
        callback?.itemSelected(storyId, newpoll, oldpoll, sheet)
        updateSumVotes(newpoll, oldpoll)
        setVotingViewList(list, newpoll, storyId, sheet, replay)
    }
}
