package io.extremum.signaler

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "extremum.signaler.subscriber")
@ConstructorBinding
data class SignalerSubscriberProperties(
    val endpoint: String,
    val headers: Map<String, String>?
)