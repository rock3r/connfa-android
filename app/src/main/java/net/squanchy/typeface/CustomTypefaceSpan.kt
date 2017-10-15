package net.squanchy.typeface

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class CustomTypefaceSpan(typeface: Typeface) : MetricAffectingSpan() {

    private val delegate = TypefaceDelegate(typeface)

    override fun updateMeasureState(p: TextPaint) {
        delegate.applyStyleTo(p)
    }

    override fun updateDrawState(tp: TextPaint) {
        delegate.applyStyleTo(tp)
    }

}