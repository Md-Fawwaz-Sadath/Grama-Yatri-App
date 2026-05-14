package com.gramayatri.app.data.model

data class StopEta(
    val stop: Stop,
    val etaMinutes: Int?,
    val status: EtaStatus,
    val reporterName: String?,
    val pingTimestamp: Long?
)
