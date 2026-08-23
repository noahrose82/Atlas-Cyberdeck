package com.noahrose.pocketlab.feature.system.capability

data class FeatureGateResult(

    val feature: AtlasFeature,

    val available: Boolean,

    val reason: String? = null
)