package com.gramayatri.app.data.remote

import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import com.gramayatri.app.R
import com.gramayatri.app.data.model.Route
import com.gramayatri.app.data.model.Stop
import com.gramayatri.app.util.Constants
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class FirebaseSeeder(
    private val context: Context,
    private val database: FirebaseDatabase
) {
    suspend fun seedRoutesIfEmpty(): Result<Boolean> = runCatching {
        val routesRef = database.reference.child(Constants.PATH_ROUTES)
        val existingRoutes = routesRef.get().await()
        if (existingRoutes.exists() && existingRoutes.childrenCount > 0L) {
            return@runCatching false
        }

        val routes = readSeedRoutes()
        routesRef.setValue(routes).await()
        true
    }

    private fun readSeedRoutes(): Map<String, Route> {
        val json = context.resources.openRawResource(R.raw.routes_seed)
            .bufferedReader()
            .use { it.readText() }
        val routesJson = JSONObject(json).getJSONObject(Constants.PATH_ROUTES)
        val routes = linkedMapOf<String, Route>()

        routesJson.keys().forEach { routeId ->
            val routeJson = routesJson.getJSONObject(routeId)
            val stopsJson = routeJson.getJSONObject("stops")
            val stops = linkedMapOf<String, Stop>()

            stopsJson.keys().forEach { stopId ->
                val stopJson = stopsJson.getJSONObject(stopId)
                stops[stopId] = Stop(
                    id = stopJson.getString("id"),
                    name = stopJson.getString("name"),
                    order = stopJson.getInt("order"),
                    attToNextMin = stopJson.getInt("attToNextMin")
                )
            }

            routes[routeId] = Route(
                id = routeJson.getString("id"),
                name = routeJson.getString("name"),
                totalStops = routeJson.getInt("totalStops"),
                stops = stops
            )
        }

        return routes
    }
}
