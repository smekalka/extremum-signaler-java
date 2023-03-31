package io.extremum.signaler

import com.fasterxml.jackson.databind.ObjectMapper
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import io.extremum.sharedmodels.constant.HttpStatus
import io.extremum.sharedmodels.dto.Response
import io.extremum.sharedmodels.signal.Signal
import io.extremum.sharedmodels.signal.SignalKind
import io.extremum.sharedmodels.signal.SubscriptionRequest

class SignalerTemplate(
    private val objectMapper: ObjectMapper,
    private val signalerProperties: SignalerPublisherProperties
) : SignalerOperations {

    private companion object {
        val httpClient = OkHttpClient()
    }

    override suspend fun send(data: Any, exchange: String, source: String) {
        send(data, exchange, source, signalerProperties.headers)
    }

    override suspend fun send(data: Any, exchange: String, source: String, headers: Map<String, String>?) {
        val signal = Signal(SignalKind.REGULAR, exchange, source)
            .apply {
                this.data = data
            }

        val request = Request.Builder()
            .url(signalerProperties.endpoint)
            .post(
                RequestBody.create(
                    MediaType.parse("application/json"),
                    objectMapper.writeValueAsString(signal)
                )
            )
            .apply {
                headers?.forEach { (header, value) ->
                    this.header(header, value)
                }
            }
            .build()

        httpClient.newCall(request).execute().code().let {
            if (it != HttpStatus.OK.value()) {
                throw SignalerException("Unable to send signal to $exchange")
            }
        }
    }

    override suspend fun subscribe(
        exchange: String,
        destination: String,
        function: String,
        headers: Map<String, String>?
    ): String {
        val subscriptionRequest = SubscriptionRequest(destination, function)
        val request = Request.Builder()
            .url("${signalerProperties.endpoint}/exchanges/${exchange}/sub")
            .post(
                RequestBody.create(
                    MediaType.parse("application/json"),
                    objectMapper.writeValueAsString(subscriptionRequest)
                )
            )
            .apply {
                headers?.forEach { (header, value) ->
                    this.header(header, value)
                }
            }
            .build()

        return httpClient.newCall(request).execute().let {
            if (it.code() != HttpStatus.OK.value()) {
                throw SignalerException("Unable to subscribe function $function to $exchange")
            }

            it.body().use { body ->
                val readValue = objectMapper.readValue(body.string(), Response::class.java)
                val result = readValue.result
                if (result is String) {
                    result
                } else {
                    throw SignalerException("Wrong response body")
                }
            }
        }
    }

    override suspend fun poll(subscriptionId: String): Response {
        TODO("Not yet implemented")
    }
}