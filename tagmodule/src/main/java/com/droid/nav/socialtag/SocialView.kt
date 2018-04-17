package com.droid.nav.socialtag

import android.content.res.ColorStateList
import android.content.res.ColorStateList.valueOf
import android.support.annotation.ColorInt
import android.support.v4.util.PatternsCompat.WEB_URL
import android.util.AttributeSet
import android.widget.TextView
import kotlin.text.RegexOption.IGNORE_CASE

/**
 * Base methods of all socialview's widgets.
 * The implementation, however, are kept in [SocialViewImpl].
 *
 * @see SocialViewImpl
 */
interface SocialView<T : TextView> {

    /**
     * Internal function to initialize [TextView] with [SocialView].
     * To be called immediately upon view creation.
     *
     * @param view target [TextView].
     * @param attrs from `View`'s constructor.
     */
    fun initialize(view: T, attrs: AttributeSet?)

    /** Determine whether this view should find and span hashtags. */
    var isHashtagEnabled: Boolean

    /** Determine whether this view should find and span mentions. */
    var isMentionEnabled: Boolean

    /** Determine whether this view should find and span hyperlinks. */
    var isHyperlinkEnabled: Boolean

    /** Color of hashtag spans. Default is color accent of current app theme. */
    var hashtagColor: ColorStateList

    /** Color of mention spans. Default is color accent of current app theme. */
    var mentionColor: ColorStateList

    /** Color of hyperlink spans. Default is color accent of current app theme. */
    var hyperlinkColor: ColorStateList

    var query: String


    /** Set hashtag color from color integer. */
    fun setHashtagColor(@ColorInt color: Int) {
        hashtagColor = valueOf(color)
    }

//    fun setQuery(query:String)
//    {
//        this.query= query;
//    }


    /** Set mention color from color integer. */
    fun setMentionColor(@ColorInt color: Int) {
        mentionColor = valueOf(color)
    }

    /** Set hyperlink color from color integer. */
    fun setHyperlinkColor(@ColorInt color: Int) {
        hyperlinkColor = valueOf(color)
    }

    /** Register a callback to be invoked when hashtag is clicked. */
    fun setOnHashtagClickListener(listener: ((view: T, String) -> Unit)?)

    /** Register a callback to be invoked when mention is clicked. */
    fun setOnMentionClickListener(listener: ((view: T, String) -> Unit)?)

    /** Register a callback to be invoked when hyperlink is clicked. */
    fun setOnHyperlinkClickListener(listener: ((view: T, String) -> Unit)?)

    /** Register a text watcher to be invoked when hashtag is modified. */
    fun setHashtagTextChangedListener(watcher: ((view: T, String) -> Unit)?)

    /** Register a text watcher to be invoked when mention is modified. */
    fun setMentionTextChangedListener(watcher: ((view: T, String) -> Unit)?)

    /** Obtain all hashtags in current text. */
    val hashtags: List<String>

    /** Obtain all mentions in current text. */
    val mentions: List<String>

    /** Obtain all hyperlinks in current text. */
    val hyperlinks: List<String>

    /** Internal function to notify `SocialView` components that flags has changed. */
    fun onFlagsChanged() {
    }

    companion object {
        internal var REGEX_HASHTAG: Regex = "#(\\w+[^A-Za-z0-9\\s]*|[^A-Za-z0-9\\s]*)*".toRegex()
        internal var REGEX_MENTION: Regex = "@(\\w+)".toRegex()
        internal var REGEX_HYPERLINK: Regex = WEB_URL.toRegex()

        /** Change current hashtag regex. */
        fun setHashtagRegex(regex: String) {
            REGEX_HASHTAG = regex.toRegex(IGNORE_CASE)
        }

        /** Change current mention regex. */
        fun setMentionRegex(regex: String) {
            REGEX_MENTION = regex.toRegex(IGNORE_CASE)
        }

        /** Change current hyperlink regex. */
        fun setHyperlinkRegex(regex: String) {
            REGEX_HYPERLINK = regex.toRegex(IGNORE_CASE)
        }
    }

}