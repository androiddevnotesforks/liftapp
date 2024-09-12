package com.patrykandpatryk.liftapp.feature.onerepmax

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import com.patrykandpatryk.liftapp.core.extension.smartToFloatOrNull
import com.patrykandpatryk.liftapp.domain.format.Formatter
import com.patrykandpatryk.liftapp.domain.unit.GetPreferredMassUnitUseCase
import com.patrykandpatryk.liftapp.domain.unit.MassUnit
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
class OneRepMaxState(
    getMassUnit: () -> Flow<MassUnit>,
    private val formatWeight: (Float, MassUnit) -> String,
    private val coroutineScope: CoroutineScope,
) {
    private val _mass: MutableState<String> = mutableStateOf("")

    private val _reps: MutableState<String> = mutableStateOf("")

    private val _history: MutableStateFlow<ImmutableList<HistoryEntryModel>> = MutableStateFlow(persistentListOf())

    private val massValue = MutableStateFlow(0f)

    private val repsValue = MutableStateFlow(0)

    private val oneRepMaxValue = combine(massValue, repsValue, ::calculateOneRepMax)
        .stateIn(coroutineScope, SharingStarted.Eagerly, 0f)

    private var historyUpdateJob: Job? = null

    val mass: State<String> = _mass

    val reps: State<String> = _reps

    val massUnit: StateFlow<MassUnit> = getMassUnit()
        .stateIn(coroutineScope, SharingStarted.Lazily, MassUnit.Kilograms)

    val oneRepMax: StateFlow<String> = combine(oneRepMaxValue, massUnit, formatWeight)
        .stateIn(coroutineScope, SharingStarted.Eagerly, "")

    val history: StateFlow<ImmutableList<HistoryEntryModel>> = _history

    @AssistedInject constructor(
        @Assisted coroutineScope: CoroutineScope,
        getPreferredMassUnitUseCase: GetPreferredMassUnitUseCase,
        formatter: Formatter,
    ) : this (
        getMassUnit = getPreferredMassUnitUseCase::invoke,
        formatWeight = formatter::formatWeight,
        coroutineScope = coroutineScope,
    )

    fun updateReps(reps: String) {
        val trimmedReps = reps.take(3)
        repsValue.value = when {
            trimmedReps.isEmpty() -> 0
            trimmedReps.isDigitsOnly() -> trimmedReps.toIntOrNull()
            else -> null
        } ?: return

        _reps.value = trimmedReps
        addToHistory()
    }

    fun updateMass(mass: String) {
        massValue.value = when {
            mass.isEmpty() -> 0f
            else -> mass.smartToFloatOrNull() ?: return
        }

        _mass.value = mass
        addToHistory()
    }

    private fun addToHistory() {
        historyUpdateJob?.cancel()
        if (oneRepMaxValue.value == 0f) return

        historyUpdateJob = coroutineScope.launch {
            delay(HISTORY_UPDATE_DELAY)
            val historyEntry = HistoryEntryModel(
                reps = repsValue.value,
                mass = massValue.value,
                oneRepMax = oneRepMaxValue.value,
            )
            if (history.value.firstOrNull() == historyEntry) return@launch
            _history.update { history -> (history + historyEntry).toImmutableList() }
        }
    }

    private fun calculateOneRepMax(
        mass: Float,
        reps: Int,
    ) = when {
        reps == 0 || mass == 0f -> 0f
        reps == 1 -> mass
        else -> mass * (1f + reps.toFloat() / EPLEY_REP_COUNT_DIVISOR)
    }

    @AssistedFactory
    interface Factory {
        fun create(coroutineScope: CoroutineScope): OneRepMaxState
    }

    private companion object {
        private const val EPLEY_REP_COUNT_DIVISOR = 30f
        private const val ONE_REP_MAX_UI_STATE_KEY = "one_rep_max_ui_state"
        private const val HISTORY_UPDATE_DELAY = 3000L
    }
}