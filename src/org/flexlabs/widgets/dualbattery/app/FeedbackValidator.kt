package org.flexlabs.widgets.dualbattery.app

object FeedbackValidator {
    @JvmStatic
    fun isValid(source: CharSequence?): Boolean {
        if (source == null || source.length < 15) return false
        var whitespace = 0
        for (character in source) {
            if (character.isWhitespace()) whitespace++
            if (whitespace > 3) return true
        }
        return false
    }
}
