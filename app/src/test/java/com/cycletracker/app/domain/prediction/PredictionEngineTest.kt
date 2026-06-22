package com.cycletracker.app.domain.prediction

import com.cycletracker.app.domain.model.CervicalMucus
import com.cycletracker.app.domain.model.Confidence
import com.cycletracker.app.domain.model.CyclePhase
import com.cycletracker.app.domain.model.DailyLog
import com.cycletracker.app.domain.model.LhTest
import com.cycletracker.app.domain.model.Period
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PredictionEngineTest {

    private val engine = DefaultPredictionEngine()
    private val base = LocalDate(2026, 1, 1)
    private fun d(n: Int): LocalDate = base.plus(n, DateTimeUnit.DAY)

    private fun periodsFromLengths(start: LocalDate = base, lengths: List<Int>, periodLen: Int = 5): List<Period> {
        val list = mutableListOf<Period>()
        var cur = start
        list += Period("0", cur, cur.plus(periodLen - 1, DateTimeUnit.DAY))
        lengths.forEachIndexed { i, len ->
            cur = cur.plus(len, DateTimeUnit.DAY)
            list += Period("${i + 1}", cur, cur.plus(periodLen - 1, DateTimeUnit.DAY))
        }
        return list
    }

    @Test fun emptyHistory_hasNoPrediction() {
        assertNull(engine.predictNextPeriod(emptyList()))
        assertNull(engine.estimateOvulation(emptyList()))
        assertEquals(0, engine.stats(emptyList()).completedCycleCount)
        assertEquals(CyclePhase.UNKNOWN, engine.currentPhase(emptyList(), base).phase)
    }

    @Test fun singlePeriod_usesDefaultWithNoConfidence() {
        val periods = listOf(Period("0", base, d(4)))
        val pred = engine.predictNextPeriod(periods)!!
        assertEquals(Confidence.NONE, pred.confidence)
        assertEquals(0, pred.basedOnCycles)
        assertEquals(d(28), pred.nextStart)
    }

    @Test fun oneCycle_lowConfidenceShrunkTowardPrior() {
        val periods = periodsFromLengths(lengths = listOf(30))
        val pred = engine.predictNextPeriod(periods)!!
        assertEquals(Confidence.LOW, pred.confidence)
        assertEquals(1, pred.basedOnCycles)
        // 30 shrinks toward the 29 prior, so predicted length is in 28..30.
        val predictedLen = periods.last().start.daysUntil(pred.nextStart)
        assertTrue("predicted=$predictedLen", predictedLen in 28..30)
    }

    @Test fun threeRegularCycles_predictHighConfidenceTightBand() {
        val periods = periodsFromLengths(lengths = listOf(28, 28, 28))
        val pred = engine.predictNextPeriod(periods)!!
        assertEquals(Confidence.HIGH, pred.confidence)
        assertEquals(periods.last().start.plus(28, DateTimeUnit.DAY), pred.nextStart)
        assertEquals(pred.nextStart.plus(-1, DateTimeUnit.DAY), pred.rangeStart)
        assertEquals(pred.nextStart.plus(1, DateTimeUnit.DAY), pred.rangeEnd)
    }

    @Test fun ovulationIsThirteenDaysBeforeNextPeriod() {
        val periods = periodsFromLengths(lengths = listOf(28, 28, 28, 28, 28, 28))
        val pred = engine.predictNextPeriod(periods)!!
        val ov = engine.estimateOvulation(periods)!!
        assertEquals(pred.nextStart.plus(-13, DateTimeUnit.DAY), ov.day)
        assertTrue(ov.confidence <= Confidence.MEDIUM)
    }

    @Test fun fertileWindowIsSixDaysEndingOnOvulation() {
        val periods = periodsFromLengths(lengths = listOf(28, 28, 28, 28, 28, 28))
        val ov = engine.estimateOvulation(periods)!!
        val fw = engine.fertileWindow(periods)!!
        assertEquals(ov.day, fw.end)
        assertEquals(ov.day.plus(-5, DateTimeUnit.DAY), fw.start)
        assertEquals(5, fw.start.daysUntil(fw.end))
    }

    @Test fun gapOver90Days_isExcludedFromAveraging() {
        val periods = periodsFromLengths(lengths = listOf(28, 28, 200, 28, 28))
        assertEquals(listOf(28, 28, 28, 28), engine.cycleLengths(periods))
    }

    @Test fun doubledCycle_isSkipImputedIntoTwoCycles() {
        val periods = periodsFromLengths(lengths = listOf(28, 28, 56, 28, 28))
        val cleaned = engine.cycleLengths(periods)
        assertTrue("expected all ~28: $cleaned", cleaned.all { it == 28 })
        assertEquals(6, cleaned.size) // 56 split into two 28s
    }

    @Test fun tripleCycle_isSkipImputedIntoThree() {
        val periods = periodsFromLengths(lengths = listOf(28, 28, 84, 28))
        assertTrue(engine.cycleLengths(periods).all { it == 28 })
    }

    @Test fun shrinkagePullsSingleOutlierTowardPrior() {
        val pred = engine.predictNextPeriod(periodsFromLengths(lengths = listOf(40)))!!
        val len = base.daysUntil(pred.nextStart) - 0 // from last start? use last start
        val predictedLen = periodsFromLengths(lengths = listOf(40)).last().start.daysUntil(pred.nextStart)
        assertTrue("predicted=$predictedLen should be shrunk below 40", predictedLen in 30..39)
    }

    @Test fun recencyWeightsRecentCyclesMore() {
        // Mean is 29, but recent cycles are longer -> weighted prediction skews up.
        val pred = engine.predictNextPeriod(periodsFromLengths(lengths = listOf(25, 25, 25, 33, 33, 33)))!!
        val predictedLen = periodsFromLengths(lengths = listOf(25, 25, 25, 33, 33, 33)).last().start.daysUntil(pred.nextStart)
        assertTrue("predicted=$predictedLen should exceed flat mean 29", predictedLen >= 30)
    }

    @Test fun irregularCycles_giveLowConfidenceWideBand() {
        val periods = periodsFromLengths(lengths = listOf(20, 40, 20, 40, 20, 40))
        val pred = engine.predictNextPeriod(periods)!!
        assertEquals(Confidence.LOW, pred.confidence)
        assertTrue(pred.rangeStart.daysUntil(pred.rangeEnd) >= 10)
        assertTrue(engine.stats(periods).isIrregular)
    }

    @Test fun currentPhase_classifiesAcrossTheCycle() {
        val periods = periodsFromLengths(lengths = listOf(28, 28, 28, 28, 28, 28))
        val lastStart = periods.last().start
        assertEquals(CyclePhase.MENSTRUAL, engine.currentPhase(periods, lastStart.plus(2, DateTimeUnit.DAY)).phase)
        assertEquals(CyclePhase.FOLLICULAR, engine.currentPhase(periods, lastStart.plus(9, DateTimeUnit.DAY)).phase)
        assertEquals(CyclePhase.OVULATORY, engine.currentPhase(periods, lastStart.plus(15, DateTimeUnit.DAY)).phase)
        assertEquals(CyclePhase.LUTEAL, engine.currentPhase(periods, lastStart.plus(22, DateTimeUnit.DAY)).phase)
    }

    @Test fun symptothermal_lhPositiveSetsOvulationNextDayHighConfidence() {
        val periods = listOf(Period("0", base, d(4)))
        val logs = listOf(DailyLog(date = d(9), lhTest = LhTest.POSITIVE))
        val ov = engine.refineOvulation(periods, logs, today = d(12))!!
        assertEquals(d(10), ov.day)
        assertEquals(Confidence.HIGH, ov.confidence)
    }

    @Test fun symptothermal_eggWhiteMucusSetsOvulation() {
        val periods = listOf(Period("0", base, d(4)))
        val logs = listOf(DailyLog(date = d(11), cervicalMucus = CervicalMucus.EGG_WHITE))
        val ov = engine.refineOvulation(periods, logs, today = d(13))!!
        assertEquals(d(11), ov.day)
    }

    @Test fun symptothermal_bbtSustainedRiseSetsOvulation() {
        val periods = listOf(Period("0", base, d(4)))
        val logs = (1..12).map { DailyLog(date = d(it), bbtCelsius = if (it <= 9) 36.4 else 36.7) }
        val ov = engine.refineOvulation(periods, logs, today = d(12))!!
        assertEquals(d(9), ov.day)
    }

    @Test fun refineFertileWindowEndsOnRefinedOvulation() {
        val periods = listOf(Period("0", base, d(4)))
        val logs = listOf(DailyLog(date = d(9), lhTest = LhTest.POSITIVE))
        val fw = engine.refineFertileWindow(periods, logs, today = d(12))!!
        assertEquals(d(10), fw.end)
        assertEquals(d(5), fw.start)
    }

    @Test fun nextPhaseStart_menstrualIsNextPredictedStart() {
        val periods = periodsFromLengths(lengths = listOf(28, 28, 28, 28, 28, 28))
        val lastStart = periods.last().start
        val today = lastStart.plus(5, DateTimeUnit.DAY)
        val menstrual = engine.nextPhaseStart(CyclePhase.MENSTRUAL, periods, today)!!
        assertEquals(lastStart.plus(28, DateTimeUnit.DAY), menstrual)
        val ovulatory = engine.nextPhaseStart(CyclePhase.OVULATORY, periods, today)!!
        assertEquals(lastStart.plus(28 - 13, DateTimeUnit.DAY), ovulatory)
    }

    @Test fun rhythmBand_needsSixCyclesAndCountsFromDayOne() {
        assertNull(engine.rhythmFertileBand(periodsFromLengths(lengths = listOf(28, 28, 28))))
        val band = engine.rhythmFertileBand(periodsFromLengths(lengths = listOf(27, 28, 29, 30, 31, 32)))!!
        assertEquals(27 - 18, band.first)
        assertEquals(32 - 11, band.last)
    }
}
