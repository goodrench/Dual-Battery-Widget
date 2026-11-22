package org.flexlabs.widgets.dualbattery.app

object FeedbackValidator {
    @JvmStatic
    fun isValid(source: CharSequence?): Boolean {
        if (source == null || source.length < 15) return false
        var spaces = 0
        for (character in source) {
            if (character == ' ') spaces++
            if (spaces > 3) return true
        }
        return false
    }
}
