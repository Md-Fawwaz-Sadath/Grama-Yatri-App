package com.gramayatri.app.data.remote

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gramayatri.app.data.model.BusAlert
import com.gramayatri.app.data.model.Ping
import com.gramayatri.app.data.model.Route
import com.gramayatri.app.util.Constants
import com.gramayatri.app.util.TimeUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseDataSource(
    private val database: FirebaseDatabase
) {
    fun getRoutes(): Flow<List<Route>> = callbackFlow {
        val ref = database.reference.child(Constants.PATH_ROUTES)
        Log.d(TAG, "Listening for routes at ${ref.toString()}")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val routes = snapshot.children
                    .mapNotNull { it.getValue(Route::class.java) }
                    .sortedBy { it.id }
                Log.d(
                    TAG,
                    "Loaded ${routes.size} routes from /${Constants.PATH_ROUTES}. " +
                        "exists=${snapshot.exists()}, children=${snapshot.childrenCount}"
                )
                trySend(routes)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Route listener cancelled: ${error.message}", error.toException())
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getLatestPing(routeId: String): Flow<Ping?> = callbackFlow {
        if (routeId.isBlank()) {
            Log.d(TAG, "Skipping latest ping listener because routeId is blank.")
            trySend(null)
            close()
            return@callbackFlow
        }
        val ref = database.reference
            .child(Constants.PATH_PINGS)
            .child(routeId)
            .orderByChild("timestamp")
            .limitToLast(20)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latestPing = snapshot.children
                    .mapNotNull { it.getValue(Ping::class.java) }
                    .filter { it.isActive }
                    .filter { it.belongsToTodayService() }
                    .maxByOrNull { it.timestamp }
                trySend(latestPing)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Ping listener cancelled for routeId=$routeId: ${error.message}", error.toException())
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getTodayPings(routeId: String): Flow<List<Ping>> = callbackFlow {
        if (routeId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val ref = database.reference
            .child(Constants.PATH_PINGS)
            .child(routeId)
            .orderByChild("timestamp")
            .limitToLast(100)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pings = snapshot.children
                    .mapNotNull { it.getValue(Ping::class.java) }
                    .filter { it.belongsToTodayService() }
                    .sortedByDescending { it.timestamp }
                trySend(pings)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Ping list listener cancelled for routeId=$routeId: ${error.message}", error.toException())
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun writePing(routeId: String, ping: Ping): Result<Unit> = runCatching {
        val routePingsRef = database.reference
            .child(Constants.PATH_PINGS)
            .child(routeId)
        val existingPings = routePingsRef.get().await()
        val updates = mutableMapOf<String, Any?>()

        existingPings.children.forEach { child ->
            val existingPing = child.getValue(Ping::class.java)
            if (existingPing?.isActive == true &&
                existingPing.belongsToTodayService() &&
                child.key != null
            ) {
                updates["${child.key}/isActive"] = false
            }
        }

        val newPingKey = routePingsRef.push().key ?: error("Unable to create ping key.")
        updates[newPingKey] = ping
        routePingsRef.updateChildren(updates).await()
    }

    fun getAlerts(routeId: String): Flow<List<BusAlert>> = callbackFlow {
        if (routeId.isBlank()) {
            Log.d(TAG, "Skipping alert listener because routeId is blank.")
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val ref = database.reference
            .child(Constants.PATH_ALERTS)
            .child(routeId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alerts = snapshot.children
                    .mapNotNull { child ->
                        child.getValue(BusAlert::class.java)?.copy(
                            alertId = child.key.orEmpty()
                        )
                    }
                    .filter { it.belongsToTodayService() }
                    .sortedByDescending { it.timestamp }
                trySend(alerts)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Alert listener cancelled for routeId=$routeId: ${error.message}", error.toException())
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun writeAlert(routeId: String, alert: BusAlert): Result<Unit> = runCatching {
        val ref = database.reference
            .child(Constants.PATH_ALERTS)
            .child(routeId)
            .push()
        ref.setValue(alert).await()
    }

    private companion object {
        const val TAG = "FirebaseDataSource"
    }
}

private fun Ping.belongsToTodayService(): Boolean =
    when {
        serviceDateKey.isNotBlank() -> serviceDateKey == TimeUtils.getTodayServiceDateKey()
        else -> TimeUtils.isTimestampToday(timestamp)
    }

private fun BusAlert.belongsToTodayService(): Boolean =
    when {
        serviceDateKey.isNotBlank() -> serviceDateKey == TimeUtils.getTodayServiceDateKey()
        else -> TimeUtils.isTimestampToday(timestamp)
    }
