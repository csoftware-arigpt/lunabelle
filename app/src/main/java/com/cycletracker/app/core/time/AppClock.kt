package com.cycletracker.app.core.time

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** Injectable clock so screens/use-cases and tests share one notion of "today". */
interface AppClock {
    fun today(): LocalDate
}

class SystemClock : AppClock {
    override fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
}
