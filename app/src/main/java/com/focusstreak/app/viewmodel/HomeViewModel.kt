package com.focusstreak.app.viewmodel

import android.app.Activity
import android.app.Application
import android.os.CountDownTimer
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusstreak.app.FocusStreakApplication
import com.focusstreak.app.R
import com.focusstreak.app.ads.InterstitialAdManager
import com.focusstreak.app.ads.RewardedAdManager
import com.focusstreak.app.data.UserPreferences
import com.focusstreak.app.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
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
    private val interstitialAdManager = InterstitialAdManager(application)
    private val rewardedAdManager = RewardedAdManager(application)

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState

    private val _timeInMillis = MutableStateFlow(25 * 60 * 1000L)
    val timeInMillis: StateFlow<Long> = _timeInMillis

    private val _userPreferences = MutableStateFlow(UserPreferences(emptySet(), 0, 0, 0, 25, "System", 9, 0, false, false, true))
    val userPreferences: StateFlow<UserPreferences> = _userPreferences

    private var countDownTimer: CountDownTimer? = null

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect {
                _userPreferences.value = it
                if (_timerState.value == TimerState.Idle) {
                    _timeInMillis.value = it.focusDuration * 60 * 1000L
                }
            }
        }
        interstitialAdManager.loadAd()
        rewardedAdManager.loadAd()
    }

    fun startTimer() {
        _timeInMillis.value = _userPreferences.value.focusDuration * 60 * 1000L
        _timerState.value = TimerState.Running
        countDownTimer = object : CountDownTimer(_timeInMillis.value, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeInMillis.value = millisUntilFinished
            }

            override fun onFinish() {
                viewModelScope.launch {
                    userPreferencesRepository.updateOnSessionCompleted(_userPreferences.value.focusDuration)
                }
                // Transition to AdShowing instead of Completed
                _timerState.value = TimerState.AdShowing
            }
        }.start()
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        _timerState.value = TimerState.Paused
    }

    fun resumeTimer() {
        _timerState.value = TimerState.Running
        countDownTimer = object : CountDownTimer(_timeInMillis.value, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeInMillis.value = millisUntilFinished
            }

            override fun onFinish() {
                viewModelScope.launch {
                    userPreferencesRepository.updateOnSessionCompleted(_userPreferences.value.focusDuration)
                }
                _timerState.value = TimerState.AdShowing
            }
        }.start()
    }

    fun endTimer() {
        countDownTimer?.cancel()
        _timeInMillis.value = _userPreferences.value.focusDuration * 60 * 1000L
        _timerState.value = TimerState.Idle
    }

    // Called when UI observes AdShowing state
    fun showInterstitialAd(activity: Activity) {
        interstitialAdManager.showAd(activity) {
            // After ad (or if failed), move to Completed to show Dialog
            _timerState.value = TimerState.Completed
        }
    }

    fun showRewardedAd(activity: Activity) {
        rewardedAdManager.showAd(activity) {
            _timeInMillis.value += 5 * 60 * 1000L
            Toast.makeText(getApplication(), getApplication<Application>().getString(R.string.bonus_minutes_added), Toast.LENGTH_SHORT).show()
        }
    }
}
