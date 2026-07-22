package com.focusstreak.app.viewmodel

import android.app.Activity
import android.app.Application
import android.media.RingtoneManager
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusstreak.app.FocusStreakApplication
import com.focusstreak.app.R
import com.focusstreak.app.ads.InterstitialAdManager
import com.focusstreak.app.ads.RewardedAdManager
import com.focusstreak.app.data.UserPreferences
import com.focusstreak.app.data.UserPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TimerState {
    object Idle : TimerState()
    object Running : TimerState()
    object Paused : TimerState()
    object AdShowing : TimerState() // New state for Ad
    object Completed : TimerState()
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository: UserPreferencesRepository = (application as FocusStreakApplication).userPreferencesRepository
    private val interstitialAdManager: InterstitialAdManager = (application as FocusStreakApplication).interstitialAdManager
    private val rewardedAdManager: RewardedAdManager = (application as FocusStreakApplication).rewardedAdManager

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState

    private val _timeInMillis = MutableStateFlow(UserPreferences.DEFAULT.focusDuration * 60 * 1000L)
    val timeInMillis: StateFlow<Long> = _timeInMillis

    private val _userPreferences = MutableStateFlow(UserPreferences.DEFAULT)
    val userPreferences: StateFlow<UserPreferences> = _userPreferences

    // Used for coroutine-based countdown. When the user pauses, we cancel
    // the job; when they resume, we start a new one with the remaining time.
    private var tickerJob: Job? = null

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect {
                _userPreferences.value = it
                if (_timerState.value == TimerState.Idle) {
                    val baseMinutes = it.focusDuration + it.bonusMinutes
                    _timeInMillis.value = baseMinutes * 60 * 1000L
                }
            }
        }
        // Note: we deliberately do NOT pre-load ads here. Ad loads happen
        // after the UMP consent flow completes (FocusStreakApplication
        // exposes a loadAllAds() hook that MainActivity calls in the
        // ConsentManager onComplete). This avoids the "first session in
        // EEA/UK = NO_FILL" failure mode where the load request beats
        // the consent gather.
    }

    private fun playCompletionSound() {
        if (_userPreferences.value.soundEffectsEnabled) {
            try {
                val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                RingtoneManager.getRingtone(getApplication(), notificationUri).play()
            } catch (e: Exception) {
                android.util.Log.w("HomeViewModel", "Failed to play completion sound", e)
            }
        }
    }

    fun startTimer() {
        // Consume any pending bonus minutes once at the start of the session.
        viewModelScope.launch {
            val bonus = userPreferencesRepository.consumeBonusMinutes()
            if (bonus > 0) {
                _userPreferences.value = _userPreferences.value.copy(bonusMinutes = 0)
            }
            val totalMinutes = _userPreferences.value.focusDuration + bonus
            _timeInMillis.value = totalMinutes * 60 * 1000L
            beginTicker()
        }
    }

    private fun beginTicker() {
        tickerJob?.cancel()
        _timerState.value = TimerState.Running
        tickerJob = viewModelScope.launch {
            while (_timeInMillis.value > 0 && _timerState.value == TimerState.Running) {
                delay(1000)
                if (_timerState.value != TimerState.Running) break
                _timeInMillis.value = (_timeInMillis.value - 1000).coerceAtLeast(0)
            }
            if (_timerState.value == TimerState.Running && _timeInMillis.value <= 0) {
                onTimerFinished()
            }
        }
    }

    private suspend fun onTimerFinished() {
        viewModelScope.launch {
            userPreferencesRepository.updateOnSessionCompleted(
                _userPreferences.value.focusDuration,
                _userPreferences.value.focusCategory
            )
        }
        playCompletionSound()
        // Gate the first-time ad: only show if the user has launched the
        // app more than once. Otherwise skip straight to the completion dialog.
        if (_userPreferences.value.appLaunchCount > 1) {
            _timerState.value = TimerState.AdShowing
        } else {
            _timerState.value = TimerState.Completed
        }
    }

    fun pauseTimer() {
        tickerJob?.cancel()
        _timerState.value = TimerState.Paused
    }

    fun resumeTimer() {
        beginTicker()
    }

    fun selectCategory(category: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateCategory(category)
        }
    }

    fun endTimer() {
        tickerJob?.cancel()
        _timeInMillis.value = _userPreferences.value.focusDuration * 60 * 1000L
        _timerState.value = TimerState.Idle
    }

    // Called when UI observes AdShowing state.
    fun showInterstitialAd(activity: Activity) {
        interstitialAdManager.showAd(
            activity = activity,
            onAdDismissed = {
                // After ad (or if it failed to show), move to Completed so
                // the session-complete dialog appears.
                _timerState.value = TimerState.Completed
            }
        )
    }

    /**
     * The rewarded ad grants the user a small bonus (5 minutes) added to
     * their next focus session. We persist the bonus via DataStore (so it
     * survives process death) and notify the user.
     */
    fun showRewardedAd(activity: Activity) {
        rewardedAdManager.showAd(
            activity = activity,
            onAdNotReady = {
                Toast.makeText(
                    getApplication(),
                    getApplication<Application>().getString(R.string.ad_loading),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onRewardEarned = {
                viewModelScope.launch {
                    userPreferencesRepository.setBonusMinutes(5)
                }
                Toast.makeText(
                    getApplication(),
                    getApplication<Application>().getString(R.string.bonus_minutes_added),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }
}
