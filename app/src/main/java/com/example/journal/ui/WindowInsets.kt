package com.example.journal.ui

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

fun View.applyStatusBarInsets() {
    val initialPaddingTop = paddingTop
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        view.updatePadding(
            top = initialPaddingTop + windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        )
        windowInsets
    }
}
