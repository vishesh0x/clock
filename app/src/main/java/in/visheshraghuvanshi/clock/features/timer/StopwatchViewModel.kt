package `in`.visheshraghuvanshi.clock.features.timer

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.clock.features.timer.logic.StopwatchService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StopwatchViewModel(app: Application) : AndroidViewModel(app) {

    private val _elapsedMillis = MutableStateFlow(0L)
    val elapsedMillis = _elapsedMillis.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private val _laps = MutableStateFlow<List<Long>>(emptyList())
    val laps = _laps.asStateFlow()

    private var startTime = 0L
    private var accumulatedTime = 0L
    private var tickerJob: Job? = null

    fun toggleStartPause() {
        if (_isRunning.value) {
            pause()
        } else {
            start()
        }
    }

    private fun start() {
        _isRunning.value = true
        startTime = System.currentTimeMillis()

        val intent = Intent(getApplication(), StopwatchService::class.java).apply {
            action = StopwatchService.ACTION_START
            putExtra(StopwatchService.EXTRA_START_TIME, startTime)
            putExtra(StopwatchService.EXTRA_ACCUMULATED, accumulatedTime)
        }
        getApplication<Application>().startForegroundService(intent)

        startTicker()
    }

    private fun pause() {
        _isRunning.value = false
        tickerJob?.cancel()

        val now = System.currentTimeMillis()
        accumulatedTime += (now - startTime)

        _elapsedMillis.value = accumulatedTime

        val intent = Intent(getApplication(), StopwatchService::class.java).apply {
            action = StopwatchService.ACTION_PAUSE
            putExtra(StopwatchService.EXTRA_ACCUMULATED, accumulatedTime)
        }
        getApplication<Application>().startService(intent)
    }

    fun lapOrReset() {
        if (_isRunning.value) {
            val currentLapTime = _elapsedMillis.value
            _laps.value = listOf(currentLapTime) + _laps.value
        } else {
            _isRunning.value = false
            _elapsedMillis.value = 0L
            accumulatedTime = 0L
            startTime = 0L
            _laps.value = emptyList()

            val intent = Intent(getApplication(), StopwatchService::class.java).apply {
                action = StopwatchService.ACTION_STOP
            }
            getApplication<Application>().startService(intent)
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (_isRunning.value) {
                val now = System.currentTimeMillis()
                _elapsedMillis.value = (now - startTime) + accumulatedTime
                delay(16)
            }
        }
    }
}