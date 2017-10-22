package net.squanchy.tweets

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.view_page_tweets.view.*
import net.squanchy.R
import net.squanchy.home.Loadable
import net.squanchy.navigation.Navigator
import net.squanchy.support.ContextUnwrapper.*
import net.squanchy.tweets.domain.TweetLinkInfo
import net.squanchy.tweets.domain.view.TweetViewModel
import net.squanchy.tweets.service.TwitterService
import net.squanchy.tweets.view.TweetsAdapter
import timber.log.Timber

class TweetsPageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr), Loadable {

    private val twitterService: TwitterService
    private val navigator: Navigator

    private val tweetClickListener: ((TweetLinkInfo) -> Unit)

    private val tweetsAdapter = TweetsAdapter(context)

    private lateinit var query: String
    private lateinit var subscription: Disposable
    private var refreshingData: Boolean = false

    init {
        val activity = unwrapToActivityContext(getContext())
        val component = TwitterInjector.obtain(activity)
        twitterService = component.service()
        navigator = component.navigator()
        tweetClickListener = { navigator.toTweet(it) }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        setupToolbar()

        tweetFeed.adapter = tweetsAdapter

        swipeRefreshContainer.setOnRefreshListener {
            if (refreshingData.not()) {
                refreshTimeline()
            }
        }
    }

    private fun setupToolbar() {
        toolbar.inflateMenu(R.menu.homepage)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_search -> {
                    navigator.toSearch()
                    true
                }
                R.id.action_settings -> {
                    navigator.toSettings()
                    true
                }
                else -> false
            }
        }
    }

    override fun startLoading() {
        query = context.getString(R.string.social_query)
        tweetEmptyView.text = context.getString(R.string.no_tweets_for_query, query)
        refreshTimeline()
    }

    override fun stopLoading() {
        subscription.dispose()
    }

    private fun refreshTimeline() {
        swipeRefreshContainer.isRefreshing = true
        refreshingData = true
        subscription = twitterService.refresh(query)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onSuccess, this::onError)
    }

    private fun onSuccess(tweet: List<TweetViewModel>) {
        tweetsAdapter.updateWith(tweet, tweetClickListener)
        onRefreshCompleted()
    }

    private fun onError(e: Throwable) {
        Timber.e(e, "Error refreshing the Twitter timeline")
        onRefreshCompleted()
    }

    private fun onRefreshCompleted() {
        refreshingData = false
        swipeRefreshContainer.isRefreshing = false

        if (tweetsAdapter.isEmpty) {
            tweetEmptyView.visibility = View.VISIBLE
            tweetFeed.visibility = View.GONE
        } else {
            tweetEmptyView.visibility = View.GONE
            tweetFeed.visibility = View.VISIBLE
        }
    }
}
