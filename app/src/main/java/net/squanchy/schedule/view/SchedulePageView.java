package net.squanchy.schedule.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.util.List;

import net.squanchy.schedule.domain.view.Event;

public class SchedulePageView extends RecyclerView {

    private EventsAdapter adapter;

    public SchedulePageView(Context context) {
        super(context);
    }

    public SchedulePageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SchedulePageView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        setLayoutManager(layoutManager);
        adapter = new EventsAdapter(getContext());
        setAdapter(adapter);
    }

    void updateWith(List<Event> newData, ScheduleViewPagerAdapter.OnEventClickedListener listener) {
        DiffUtil.Callback callback = new EventsDiffCallback(adapter.events(), newData);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(callback, true);    // TODO move off the UI thread
        adapter.updateWith(newData, listener);
        diffResult.dispatchUpdatesTo(adapter);
    }

    private static class EventsDiffCallback extends DiffUtil.Callback {

        private final List<Event> oldEvents;
        private final List<Event> newEvents;

        private EventsDiffCallback(List<Event> oldEvents, List<Event> newEvents) {
            this.oldEvents = oldEvents;
            this.newEvents = newEvents;
        }

        @Override
        public int getOldListSize() {
            return oldEvents.size();
        }

        @Override
        public int getNewListSize() {
            return newEvents.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            Event oldEvent = oldEvents.get(oldItemPosition);
            Event newEvent = newEvents.get(newItemPosition);
            return oldEvent.id() == newEvent.id();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Event oldEvent = oldEvents.get(oldItemPosition);
            Event newEvent = newEvents.get(newItemPosition);
            return oldEvent.equals(newEvent);
        }
    }
}
