package org.flexlabs.widgets.dualbattery.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedbackValidatorTest {
    @Test
    fun `accepts descriptive feedback`() {
        assertTrue(FeedbackValidator.isValid("The widget works well on my tablet"))
    }

    @Test
    fun `accepts feedback separated by whitespace characters`() {
        assertTrue(FeedbackValidator.isValid("The\twidget\nworks\twell\non\tmy tablet"))
    }

    @Test
    fun `rejects null short and low-detail feedback`() {
        assertFalse(FeedbackValidator.isValid(null))
        assertFalse(FeedbackValidator.isValid("A few words here"))
        assertFalse(FeedbackValidator.isValid("This-feedback-is-long-but-has-no-spaces"))
    }
}
