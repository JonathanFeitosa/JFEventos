package com.example.jfeventos.model

import java.math.BigDecimal

data class Event(
    val id: Long? = -1,
    val image: String? = "",
    val title: String? = "",
    val date: Long? = -1,
    val price: BigDecimal? = BigDecimal.ZERO
)