package com.gramayatri.app.ui.tracking

import com.gramayatri.app.data.model.StopEta

data class StopTimelineItem(
    val stopEta: StopEta,
    val isSavedStop: Boolean
)
