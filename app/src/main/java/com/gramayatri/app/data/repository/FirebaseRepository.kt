package com.gramayatri.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.gramayatri.app.data.model.BusAlert
import com.gramayatri.app.data.model.Ping
import com.gramayatri.app.data.model.Route
import com.gramayatri.app.data.remote.FirebaseDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class FirebaseRepository(
    private val dataSource: FirebaseDataSource,
    private val auth: FirebaseAuth
) {
    fun getRoutes(): Flow<List<Route>> =
        dataSource.getRoutes()

    fun getLatestPing(routeId: String): Flow<Ping?> =
        dataSource.getLatestPing(routeId)

    fun getTodayPings(routeId: String): Flow<List<Ping>> =
        dataSource.getTodayPings(routeId)

    suspend fun submitPing(routeId: String, ping: Ping): Result<Unit> =
        dataSource.writePing(routeId, ping)

    fun getAlerts(routeId: String): Flow<List<BusAlert>> =
        dataSource.getAlerts(routeId)

    suspend fun submitAlert(routeId: String, alert: BusAlert): Result<Unit> =
        dataSource.writeAlert(routeId, alert)

    fun getCurrentUid(): String? =
        auth.currentUser?.uid

    suspend fun signInAnonymously(): Result<String> = runCatching {
        val result = auth.signInAnonymously().await()
        result.user?.uid ?: error("Anonymous sign-in returned no user.")
    }
}
