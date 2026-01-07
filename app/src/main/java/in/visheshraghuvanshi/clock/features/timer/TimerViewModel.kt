package `in`.visheshraghuvanshi.clock.features.timer

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.clock.features.timer.logic.TimerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerViewModel(app: Application) : AndroidViewModel(app) {

    private val _inputString = MutableStateFlow("")
    val inputString = _inputString.asStateFlow()

    private val _totalTime = MutableStateFlow(0L)
    val totalTime = _totalTime.asStateFlow()

    private val _remainingTime = MutableStateFlow(0L)
    val remainingTime = _remainingTime.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused = _isPaused.asStateFlow()

    private var timerEndTime = 0L
    private var tickerJob: Job? = null
    private var timeRemainingOnPause = 0L

    fun onNumberClick(num: String) {
        if (_inputString.value.length < 6) _inputString.value += num
    }

    fun onBackspace() {
        if (_inputString.value.isNotEmpty()) _inputString.value = _inputString.value.dropLast(1)
    }

    fun startTimer() {
        val seconds = parseInputToSeconds(_inputString.value)
        if (seconds > 0) {
            _totalTime.value = seconds
            startService(seconds)
            startTicker(seconds)
        }
    }

    fun stopTimer() {
        _isRunning.value = false
        _isPaused.value = false
        _inputString.value = ""
        tickerJob?.cancel()
        stopService()
    }

    fun togglePause() {
        if (_isPaused.value) {
            _isPaused.value = false
            startService(timeRemainingOnPause / 1000)
            startTicker(timeRemainingOnPause / 1000)
        } else {
            _isPaused.value = true
            timeRemainingOnPause = timerEndTime - System.currentTimeMillis()
            tickerJob?.cancel()
            stopService()
        }
    }

    fun addMinute() {
        if (_isRunning.value) {
            timerEndTime += 60_000
            _totalTime.value += 60
        }
    }

    private fun startService(duration: Long) {
        val intent = Intent(getApplication(), TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_DURATION, duration)
        }
        getApplication<Application>().startForegroundService(intent)
    }

    private fun stopService() {
        val intent = Intent(getApplication(), TimerService::class.java).apply {
            action = TimerService.ACTION_STOP
        }
        getApplication<Application>().startService(intent)
    }

    private fun startTicker(duration: Long) {
        _isRunning.value = true
        timerEndTime = System.currentTimeMillis() + (duration * 1000)

        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (_isRunning.value) {
                val left = timerEndTime - System.currentTimeMillis()
                if (left <= 0) {
                    _remainingTime.value = 0
                    stopTimer()
                    break
                }
                _remainingTime.value = left / 1000
                delay(100)
            }
        }
    }

    private fun parseInputToSeconds(input: String): Long {
        if (input.isEmpty()) return 0
        val padded = input.padStart(6, '0')
        val h = padded.take(2).toLong()
        val m = padded.substring(2, 4).toLong()
        val s = padded.substring(4, 6).toLong()
        return (h * 3600) + (m * 60) + s
    }
}