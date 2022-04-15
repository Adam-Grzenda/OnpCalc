package pl.put.onpcalc

import java.math.BigDecimal

interface ViewUpdateObserver {
    fun updateView(newValue: List<BigDecimal>)
}