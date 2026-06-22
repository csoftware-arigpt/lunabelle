package com.cycletracker.app.domain.model

/** Bleeding intensity for a day. Ordinal order is meaningful (NONE < ... < HEAVY). */
enum class FlowLevel { NONE, SPOTTING, LIGHT, MEDIUM, HEAVY }

/** Cervical mucus observation; EGG_WHITE indicates peak fertility. */
enum class CervicalMucus { NONE_DRY, STICKY, CREAMY, WATERY, EGG_WHITE }

enum class LhTest { NOT_TAKEN, NEGATIVE, POSITIVE }

enum class SexualActivity { NONE, PROTECTED, UNPROTECTED }

enum class Intensity { NONE, MILD, MODERATE, SEVERE }

enum class CyclePhase { MENSTRUAL, FOLLICULAR, OVULATORY, LUTEAL, UNKNOWN }

/** Prediction confidence. Comparable by ordinal: NONE < LOW < MEDIUM < HIGH. */
enum class Confidence { NONE, LOW, MEDIUM, HIGH }

enum class GoalMode { TRACKING_ONLY, TRYING_TO_CONCEIVE, AVOID_PREGNANCY_INFO }
