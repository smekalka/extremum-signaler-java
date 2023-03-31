package io.extremum.signaler

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SignalerPublisherProperties::class, SignalerSubscriberProperties::class)
open class SignalerConfiguration