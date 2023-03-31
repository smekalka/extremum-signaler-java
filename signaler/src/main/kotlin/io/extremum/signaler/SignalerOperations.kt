package io.extremum.signaler

import io.extremum.sharedmodels.dto.Response

interface SignalerOperations {

    suspend fun poll(subscriptionId: String): Response

    suspend fun send(data: Any, exchange: String, source: String)

    suspend fun send(data: Any, exchange: String, source: String, headers: Map<String, String>?)

    suspend fun subscribe(
        exchange: String,
        destination: String,
        function: String,
        headers: Map<String, String>?
    ): String
}