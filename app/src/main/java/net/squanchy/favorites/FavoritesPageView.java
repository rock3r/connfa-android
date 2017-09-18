package net.squanchy.favorites;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;

import net.squanchy.R;
import net.squanchy.analytics.Analytics;
import net.squanchy.analytics.ContentType;
import net.squanchy.favorites.view.FavoritesListView;
import net.squanchy.home.HomeActivity;
import net.squanchy.navigation.Navigator;
import net.squanchy.schedule.ScheduleService;
import net.squanchy.schedule.domain.view.Event;
import net.squanchy.schedule.domain.view.Schedule;
import net.squanchy.schedule.view.ScheduleViewPagerAdapter;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;

import static net.squanchy.support.ContextUnwrapper.unwrapToActivityContext;

public class FavoritesPageView extends CoordinatorLayout implements LifecycleObserver {

    private View progressBar;
    private Disposable subscription;
    private ScheduleService service;
    private Navigator navigate;
    private Analytics analytics;
    private FavoritesListView favoritesListView;
    private View emptyViewSignedIn;
    private View emptyViewSignedOut;

    public FavoritesPageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FavoritesPageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (!isInEditMode()) {
            AppCompatActivity activity = unwrapToActivityContext(getContext());
            FavoritesComponent component = FavoritesInjector.obtain(activity);
            service = component.service();
            navigate = component.navigator();
            analytics = component.analytics();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        progressBar = findViewById(R.id.progressbar);
        favoritesListView = (FavoritesListView) findViewById(R.id.favorites_list);
        emptyViewSignedIn = findViewById(R.id.empty_view_signed_in);
        emptyViewSignedOut = findViewById(R.id.empty_view_signed_out);

        findViewById(R.id.empty_view_signed_out_button).setOnClickListener(view -> requestSignIn());

        setupToolbar();
    }

    private void requestSignIn() {
        // ⚠️ HACK this is DIRTY and HORRIBLE but it's the only way we can ship this
        // without rewriting the whole data layer. Sorry. I swear, we know it sucks
        // and we want to fix this ASAP.
        HomeActivity activity = (HomeActivity) unwrapToActivityContext(getContext());
        activity.requestSignIn();
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.activity_favorites);
        toolbar.inflateMenu(R.menu.homepage);
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_search:
                    navigate.toSearch();
                    return true;
                case R.id.action_settings:
                    navigate.toSettings();
                    return true;
                default:
                    return false;
            }
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected void startLoading() {
        subscription = Observable.combineLatest(service.schedule(true), service.currentUserIsSignedIn(), toScheduleAndLoggedIn())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(schedule -> updateWith(schedule, this::onEventClicked));
    }

    private BiFunction<Schedule, Boolean, ScheduledAndSignedIn> toScheduleAndLoggedIn() {
        return ScheduledAndSignedIn::new;
    }

    private void onEventClicked(Event event) {
        analytics.trackItemSelected(ContentType.FAVORITES_ITEM, event.getId());
        navigate.toEventDetails(event.getId());
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    protected void stopLoading() {
        subscription.dispose();
    }

    private void updateWith(ScheduledAndSignedIn scheduledAndSignedIn, ScheduleViewPagerAdapter.OnEventClickedListener listener) {
        if (scheduledAndSignedIn.hasFavorites()) {
            updateWith(scheduledAndSignedIn.schedule(), listener);
        } else {
            if (scheduledAndSignedIn.signedIn()) {
                promptToFavorite();
            } else {
                promptToSign();
            }
        }
    }

    private void updateWith(Schedule schedule, ScheduleViewPagerAdapter.OnEventClickedListener listener) {
        if (schedule.isEmpty()) {
            promptToFavorite();
        } else {
            favoritesListView.updateWith(schedule, listener);
            favoritesListView.setVisibility(VISIBLE);
            emptyViewSignedIn.setVisibility(GONE);
        }

        progressBar.setVisibility(GONE);
        emptyViewSignedOut.setVisibility(GONE);
    }

    private void promptToFavorite() {
        favoritesListView.setVisibility(GONE);
        progressBar.setVisibility(GONE);
        emptyViewSignedOut.setVisibility(GONE);
        emptyViewSignedIn.setVisibility(VISIBLE);
    }

    public void promptToSign() {
        favoritesListView.setVisibility(GONE);
        progressBar.setVisibility(GONE);
        emptyViewSignedOut.setVisibility(VISIBLE);
        emptyViewSignedIn.setVisibility(GONE);
    }

    private static final class ScheduledAndSignedIn {

        private final Schedule schedule;
        private final boolean signedIn;

        private ScheduledAndSignedIn(Schedule schedule, boolean signedIn) {
            this.schedule = schedule;
            this.signedIn = signedIn;
        }

        boolean hasFavorites() {
            return !schedule.isEmpty();
        }

        public Schedule schedule() {
            return schedule;
        }

        public boolean signedIn() {
            return signedIn;
        }
    }
}
