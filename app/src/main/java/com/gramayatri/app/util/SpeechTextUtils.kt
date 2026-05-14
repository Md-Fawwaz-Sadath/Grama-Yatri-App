package com.gramayatri.app.util

import android.content.Context
import com.gramayatri.app.R

object SpeechTextUtils {
    fun routeNameForSpeech(context: Context, routeName: String): String {
        val cleaned = routeName
            .replace("→", "->")
            .replace("â†’", "->")
            .trim()
        val parts = cleaned
            .split("->")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        return if (parts.size >= 2) {
            context.getString(R.string.route_name_to_format, parts.first(), parts.last())
        } else {
            cleaned.ifBlank { context.getString(R.string.route_unknown) }
        }
    }
}
