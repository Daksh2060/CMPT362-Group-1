package com.example.cmpt362group1.utils

import android.util.Log

sealed class SummaryResult {
    data class Success(val summary: String) : SummaryResult()
    data class Error(val message: String) : SummaryResult()
}

class Summarizer() {
    fun summarize(text: String): SummaryResult {
        Log.d("INFO SUMMARY", text)

        return try {
            val summary = extractKeyInfo(text)
            if (summary.isNotBlank()) {
                SummaryResult.Success(summary)
            } else {
                SummaryResult.Error("Could not generate summary")
            }
        } catch (e: Exception) {
            SummaryResult.Error("Error: ${e.message}")
        }
    }

    private fun extractKeyInfo(text: String): String {
        val parts = mutableListOf<String>()

        val lines = text.split(". ").map { it.trim() }

        lines.find { it.startsWith("Event:") }?.let {
            val title = it.removePrefix("Event:").trim()
            parts.add(title)
        }

        lines.find { it.startsWith("Location:") }?.let {
            val location = it.removePrefix("Location:").trim()
            if (location.isNotBlank()) {
                parts.add("at $location")
            }
        }

        lines.find { it.startsWith("Date:") }?.let { dateLine ->
            val dateTimeStr = dateLine.removePrefix("Date:").trim()

            if (dateTimeStr.contains(" at ")) {
                parts.add("on $dateTimeStr")
            } else {
                parts.add("on $dateTimeStr")
            }
        }

        val descriptionStart = text.indexOf(". ", text.indexOf("Date:"))
        if (descriptionStart != -1) {
            val remainingText = text.substring(descriptionStart + 2).trim()
            if (remainingText.isNotBlank()) {
                val firstSentence = remainingText.split(". ").firstOrNull()?.trim()
                    ?: remainingText.take(100).trim()

                if (firstSentence.isNotBlank()) {
                    val shortened = if (firstSentence.length > 100) {
                        firstSentence.take(97) + "..."
                    } else {
                        firstSentence
                    }
                    parts.add(shortened)
                }
            }
        }

        // Build final summary
        return if (parts.isNotEmpty()) {
            parts.joinToString(" ")
                .replace("  ", " ")
                .trim()
                .let { summary ->
                    when {
                        summary.endsWith(".") || summary.endsWith("...") -> summary
                        else -> "$summary."
                    }
                }
        } else {
            "Event summary unavailable."
        }
    }
}