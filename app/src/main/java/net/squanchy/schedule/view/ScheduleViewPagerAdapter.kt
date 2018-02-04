package net.squanchy.schedule.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.novoda.viewpageradapter.ViewPagerAdapter
import net.squanchy.R
import net.squanchy.schedule.domain.view.Event
import net.squanchy.schedule.domain.view.SchedulePage
import java.util.Locale

class ScheduleViewPagerAdapter(private val context: Context) : ViewPagerAdapter<ScheduleDayPageView>() {

    private lateinit var listener: (Event) -> Unit

    private var pages = emptyList<SchedulePage>()
    private var initialEventForPage = emptyList<Event?>()

    fun updateWith(pages: List<SchedulePage>, initialEventForPage: List<Event?>, listener: (Event) -> Unit) {
        this.pages = pages
        this.initialEventForPage = initialEventForPage
        this.listener = listener
        notifyDataSetChanged()
    }

    override fun getCount() = pages.size

    override fun createView(container: ViewGroup, position: Int): ScheduleDayPageView {
        return LayoutInflater.from(context)
            .inflate(R.layout.view_page_schedule_day, container, false) as ScheduleDayPageView
    }

    override fun bindView(view: ScheduleDayPageView, position: Int) {
        val events = pages[position].events
        val initialEvent = initialEventForPage[position]
        view.updateWith(events, initialEvent, listener)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val date = pages[position].date
        return date.toString(TITLE_FORMAT_TEMPLATE).toUpperCase(Locale.getDefault())
    }

    fun getPageDayId(position: Int) = pages[position].dayId

    override fun isViewFromObject(view: View, `object`: Any) = view === `object`

    interface OnEventClickedListener {
        fun onEventClicked(event: Event)
    }

    companion object {
        private const val TITLE_FORMAT_TEMPLATE = "EEE d"
    }
}
