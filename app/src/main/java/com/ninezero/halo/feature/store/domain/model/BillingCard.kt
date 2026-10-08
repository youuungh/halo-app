package com.ninezero.halo.feature.store.domain.model

data class BillingCard(
    val registered: Boolean,
    val cardCompany: String? = null,
    val cardNumberMasked: String? = null,
    val cardType: String? = null
)
