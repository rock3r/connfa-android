package net.squanchy.typeface

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned

class TypefaceController {

    fun applyTypeface(text: CharSequence, typeface: Typeface) =
            if (text !is Spannable) {
                addSpanTo(SpannableString(text), typeface)
            } else {
                addSpanTo(text, typeface)
            }


    private fun addSpanTo(text: Spannable, typeface: Typeface) =
            text.apply {
                text.setSpan(CustomTypefaceSpan(typeface), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }


    fun hasSpan(text: Spanned) =
            text.getSpans(0, text.length, CustomTypefaceSpan::class.java).isNotEmpty()
}