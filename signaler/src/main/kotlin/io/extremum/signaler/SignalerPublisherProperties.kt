package io.extremum.signaler

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "extremum.signaler.publisher")
@ConstructorBinding
data class SignalerPublisherProperties(
    val endpoint: String,
    val headers: Map<String, String>?
)