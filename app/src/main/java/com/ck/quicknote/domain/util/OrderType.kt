package com.ck.quicknote.domain.util

sealed class OrderType {
    object Ascending: OrderType()
    object Descending: OrderType()
}