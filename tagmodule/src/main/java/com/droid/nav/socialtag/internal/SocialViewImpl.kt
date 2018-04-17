package com.droid.nav.socialtag.internal;

import android.content.res.ColorStateList
import android.graphics.Paint
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.method.LinkMovementMethod.getInstance
import android.text.style.*
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import android.widget.TextView.BufferType.SPANNABLE
import com.droid.nav.socialtag.CustomTypefaceSpan
import com.droid.nav.socialtag.SocialView
import com.droid.nav.socialtag.SocialView.Companion.REGEX_HASHTAG
import com.droid.nav.socialtag.SocialView.Companion.REGEX_HYPERLINK
import com.droid.nav.socialtag.SocialView.Companion.REGEX_MENTION
import com.droid.nav.socialtag.R
import java.lang.Character.isLetterOrDigit

/**
 * Implementation of [SocialView] that is delegated to all socialview's widgets.
 *
 * @see SocialView
 */
class SocialViewImpl<T : TextView> : SocialView<T> {


    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            if (count > 0 && start > 0) when (s[start - 1]) {
                '#' -> {
                    hashtagEditing = true
                    mentionEditing = false
                }
                '@' -> {
                    hashtagEditing = false
                    mentionEditing = true
                }
                else -> when {
                    !isLetterOrDigit(s[start - 1]) -> {
                        hashtagEditing = false
                        mentionEditing = false
                    }
                    hashtagWatcher != null && hashtagEditing ->
                        hashtagWatcher!!(
                                view,
                                s.substring(indexOfPreviousNonLetterDigit(s, 0, start - 1) + 1, start))
                    mentionWatcher != null && mentionEditing ->
                        mentionWatcher!!(
                                view,
                                s.substring(indexOfPreviousNonLetterDigit(s, 0, start - 1) + 1, start))
                }
            }
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            // triggered when text is added
            if (s.isEmpty()) return
            colorize()
            if (start < s.length) {
                if (start + count - 1 < 0) return
                when (s[start + count - 1]) {
                    '#' -> {
                        hashtagEditing = true
                        mentionEditing = false
                    }
                    '@' -> {
                        hashtagEditing = false
                        mentionEditing = true
                    }
                    else -> when {
                        !isLetterOrDigit(s[start]) -> {
                            hashtagEditing = false
                            mentionEditing = false
                        }
                        hashtagWatcher != null && hashtagEditing ->
                            hashtagWatcher!!(
                                    view,
                                    s.substring(
                                            indexOfPreviousNonLetterDigit(s, 0, start) + 1, start + count))
                        mentionWatcher != null && mentionEditing ->
                            mentionWatcher!!(
                                    view,
                                    s.substring(
                                            indexOfPreviousNonLetterDigit(s, 0, start) + 1, start + count))
                    }
                }
            }
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    private lateinit var view: T
    private var flags = 0
    private var abc = "";
    private lateinit var _hashtagColor: ColorStateList
    private lateinit var _mentionColor: ColorStateList
    private lateinit var _hyperlinkColor: ColorStateList
    private var hashtagListener: ((T, String) -> Unit)? = null
    private var mentionListener: ((T, String) -> Unit)? = null
    private var hyperlinkListener: ((T, String) -> Unit)? = null
    private var hashtagWatcher: ((T, String) -> Unit)? = null
    private var mentionWatcher: ((T, String) -> Unit)? = null
    private var hashtagEditing: Boolean = false
    private var mentionEditing: Boolean = false

    override fun initialize(view: T, attrs: AttributeSet?) {
        this.view = view
        this.view.addTextChangedListener(textWatcher)
        this.view.setText(this.view.text, SPANNABLE)
        val a = this.view.context.obtainStyledAttributes(attrs, R.styleable.SocialView,
                R.attr.socialViewStyle, R.style.Widget_SocialView)
        flags = a.getInteger(R.styleable.SocialView_socialFlags,
                FLAG_HASHTAG or FLAG_MENTION or FLAG_HYPERLINK)
        _hashtagColor = a.getColorStateList(R.styleable.SocialView_hashtagColor)
        _mentionColor = a.getColorStateList(R.styleable.SocialView_mentionColor)
        _hyperlinkColor = a.getColorStateList(R.styleable.SocialView_hyperlinkColor)
        a.recycle()
        colorize()
    }

    override var isHashtagEnabled: Boolean
        get() = flags or FLAG_HASHTAG == flags
        set(value) {
            if (value != isHashtagEnabled) {
                flags = when {
                    value -> flags or FLAG_HASHTAG
                    else -> flags and FLAG_HASHTAG.inv()
                }
                colorize()
                onFlagsChanged()
            }
        }


    override var query: String
        get() = abc
        set(value) {

            if (!value.equals(abc)) {
                abc = value
                colorize()
                onFlagsChanged()
            }
        }



    override var isMentionEnabled: Boolean
        get() = flags or FLAG_MENTION == flags
        set(value) {
            if (value != isMentionEnabled) {
                flags = when {
                    value -> flags or FLAG_MENTION
                    else -> flags and FLAG_MENTION.inv()
                }
                colorize()
                onFlagsChanged()
            }
        }


    override var isHyperlinkEnabled: Boolean
        get() = flags or FLAG_HYPERLINK == flags
        set(value) {

            if (value != isHyperlinkEnabled) {
                flags = when {
                    value -> flags or FLAG_HYPERLINK
                    else -> flags and FLAG_HYPERLINK.inv()
                }
                colorize()
                onFlagsChanged()
            }
        }

    override var hashtagColor: ColorStateList
        get() = _hashtagColor
        set(colorStateList) {
            _hashtagColor = colorStateList
            colorize()
        }

    override var mentionColor: ColorStateList
        get() = _mentionColor
        set(colorStateList) {
            _mentionColor = colorStateList
            colorize()
        }

    override var hyperlinkColor: ColorStateList
        get() = _hyperlinkColor
        set(colorStateList) {
            _hyperlinkColor = colorStateList
            colorize()
        }

    override fun setOnHashtagClickListener(listener: ((view: T, String) -> Unit)?) {
        view.setLinkMovementMethodIfNotAlready()
        hashtagListener = listener
        colorize()
    }

    override fun setOnMentionClickListener(listener: ((view: T, String) -> Unit)?) {
        view.setLinkMovementMethodIfNotAlready()
        mentionListener = listener
        colorize()
    }

    override fun setOnHyperlinkClickListener(listener: ((view: T, String) -> Unit)?) {
        view.setLinkMovementMethodIfNotAlready()
        hyperlinkListener = listener
        colorize()
    }

    override fun setHashtagTextChangedListener(watcher: ((view: T, String) -> Unit)?) {
        hashtagWatcher = watcher
    }

    override fun setMentionTextChangedListener(watcher: ((view: T, String) -> Unit)?) {
        mentionWatcher = watcher
    }

    override val hashtags: List<String>
        get() = if (!isHashtagEnabled) emptyList() else REGEX_HASHTAG.extract()

    override val mentions: List<String>
        get() = if (!isMentionEnabled) emptyList() else REGEX_MENTION.extract()

    override val hyperlinks: List<String>
        get() = if (!isHyperlinkEnabled) emptyList() else REGEX_HYPERLINK.extract()

    /** Internal function to span text based on current configuration. */
    private fun colorize() {
        val spannable = view.text
        check(spannable is Spannable, {
            "Attached text is not a Spannable," +
                    "add TextView.BufferType.SPANNABLE when setting text to this TextView."
        })
        spannable as Spannable
        spannable.getSpans(0, spannable.length, CharacterStyle::class.java).forEach {
            spannable.removeSpan(it)
        }

        val fontBold = Typeface.createFromAsset(view.context.assets, view.context.getString(R.string.eina_01_bold))

        if (abc.isNotEmpty()) {
            var index = TextUtils.indexOf(view.text, abc)
            while (index >= 0) {

                spannable.setSpan(BackgroundColorSpan(ContextCompat.getColor(view.context, R.color.text_highlight)), index, index + abc.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                index = TextUtils.indexOf(view.text, abc, index + abc.length)
            }
        }

        if (isHashtagEnabled) spannable.span(REGEX_HASHTAG, { s ->
            hashtagListener?.newClickableSpan(s, _hashtagColor)
                    ?: CustomTypefaceSpan("", fontBold)
        })



        if (isMentionEnabled)
            spannable.span(REGEX_MENTION, { s ->
                mentionListener?.newClickableSpan(s, _mentionColor)
                        ?: CustomTypefaceSpan("", fontBold);

            })


        if (isHyperlinkEnabled) spannable.span(REGEX_HYPERLINK, { s ->
            hyperlinkListener?.newClickableSpan(s, _hyperlinkColor, true) ?: object : URLSpan(s) {
                override fun updateDrawState(ds: TextPaint) {
                    ds.color = _hyperlinkColor.defaultColor
                    ds.isUnderlineText = true
                }
            }
        })
    }

    private fun indexOfNextNonLetterDigit(
            text: CharSequence,
            start: Int
    ): Int = (start + 1 until text.length).firstOrNull { !isLetterOrDigit(text[it]) }
            ?: text.length

    private fun indexOfPreviousNonLetterDigit(
            text: CharSequence,
            start: Int,
            end: Int
    ): Int = (end downTo start + 1).firstOrNull { !isLetterOrDigit(text[it]) } ?: start

    private fun TextView.setLinkMovementMethodIfNotAlready() {
        if (movementMethod == null || movementMethod !is LinkMovementMethod)
            movementMethod = getInstance()
    }

    private fun ((T, String) -> Unit).newClickableSpan(
            s: String,
            color: ColorStateList,
            underline: Boolean = false
    ): CharacterStyle = object : ClickableSpan() {


        override fun onClick(widget: View) = invoke(view, when {
            this@newClickableSpan !== hyperlinkListener -> s.substring(1)
            else -> s
        })


        override fun updateDrawState(ds: TextPaint) {
//            ds.color = color.defaultColor
//            ds.isUnderlineText = underline
            val fontBold = Typeface.createFromAsset(view.context.assets, view.context.getString(R.string.eina_01_bold))

            applyCustomTypeFace(ds, fontBold)
        }

        private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
            val oldStyle: Int
            val old = paint.typeface
            if (old == null) {
                oldStyle = 0
            } else {
                oldStyle = old.style
            }

            val fake = oldStyle and tf.style.inv()
            if (fake and Typeface.BOLD != 0) {
                paint.isFakeBoldText = true
            }

            if (fake and Typeface.ITALIC != 0) {
                paint.textSkewX = -0.25f
            }

            paint.typeface = tf
        }
    }

    private fun Regex.extract(): List<String> {
        val list = mutableListOf<String>()
        val matcher = toPattern().matcher(view.text)
        while (matcher.find()) list += matcher.group(when {
            this !== REGEX_HYPERLINK -> 1 /* remove hashtag and mention symbol */
            else -> 0
        })
        return list
    }

    private companion object {
        const val FLAG_HASHTAG = 1
        const val FLAG_MENTION = 2
        const val FLAG_HYPERLINK = 4

        fun Spannable.span(
                regex: Regex,
                vararg spans: (String) -> Any,
                flags: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        ) {
            val matcher = regex.toPattern().matcher(this)
            while (matcher.find()) {


                val start = matcher.start()
                val end = matcher.end()
                for (span in spans) setSpan(span(substring(start, end)), start, end, flags)
            }
        }

//        fun Spannable.spanMention(
//                regex: Regex,
//                userTag: ArrayList<User>,
//                vararg spans: (String) -> Any
//
//        ) {
//            val flags: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//            val matcher = regex.toPattern().matcher(this)
//            while (matcher.find()) {
//
//                var isTag=false
//                for (user in userTag) {
//                    if (user.userName.toLowerCase()==(matcher.group().replace("@",""))) {
//                        isTag = true
//                    }
//                }
//
//                if(isTag) {
//
//                    val start = matcher.start()
//                    val end = matcher.end()
//                    for (span in spans) setSpan(span(substring(start, end)), start, end, flags)
//                }
//            }
//        }
    }
}