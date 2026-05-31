package com.example.myapplication.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text.trim()
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val digitsOnly = originalText.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formatted = try {
            val parsed = digitsOnly.toLong()
            val format = NumberFormat.getNumberInstance(Locale("id", "ID"))
            format.format(parsed)
        } catch (e: Exception) {
            digitsOnly
        }

        return TransformedText(
            AnnotatedString(formatted),
            CurrencyOffsetMapping(originalText, formatted)
        )
    }

    private class CurrencyOffsetMapping(val originalText: String, val formattedText: String) : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset <= 0) return 0
            if (offset >= originalText.length) return formattedText.length
            
            val originalSub = originalText.substring(0, offset)
            val digitsCount = originalSub.count { it.isDigit() }
            
            var transformedOffset = 0
            var matchedDigits = 0
            for (i in formattedText.indices) {
                if (matchedDigits == digitsCount) break
                if (formattedText[i].isDigit()) {
                    matchedDigits++
                }
                transformedOffset++
            }
            return transformedOffset
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (offset <= 0) return 0
            if (offset >= formattedText.length) return originalText.length
            
            val formattedSub = formattedText.substring(0, offset)
            val digitsCount = formattedSub.count { it.isDigit() }
            
            var originalOffset = 0
            var matchedDigits = 0
            for (i in originalText.indices) {
                if (matchedDigits == digitsCount) break
                if (originalText[i].isDigit()) {
                    matchedDigits++
                }
                originalOffset++
            }
            return originalOffset
        }
    }
}
