package com.example.kroko

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    private val wordList = listOf(
        "Barrier", "Procrastination", "Astronaut", "Neural Network", "Giraffe",
        "Interpolation", "Espresso", "Helicopter", "Cryptography", "Parachute",
        "Sushi Bar", "Meteorite", "Shark", "Magnet", "Smartphone", "Algorithm"
    ).shuffled()

    private var usedWordIndices = mutableSetOf<Int>()

    fun setupGame(playerNames: List<String>, targetScore: Int) {
        val shuffledNames = playerNames.filter { it.isNotBlank() }.shuffled()
        if (shuffledNames.isEmpty()) return

        val players = shuffledNames.map { Player(name = it) }
        val teams = listOf(
            Team(name = "Team 1", members = mutableListOf()),
            Team(name = "Team 2", members = mutableListOf())
        )

        players.forEachIndexed { index, player ->
            val teamIndex = index % 2
            (teams[teamIndex].members as MutableList).add(player)
        }

        _uiState.update {
            GameUiState(
                teams = teams,
                currentTeamIndex = 0,
                targetScore = targetScore,
                gameOver = false,
                winnerTeam = null
            )
        }
        prepareNextTurn()
    }

    private fun prepareNextTurn() {
        val currentState = _uiState.value
        if (currentState.teams.isEmpty()) return

        val currentTeam = currentState.teams[currentState.currentTeamIndex]
        val randomPlayer = currentTeam.members.randomOrNull()

        var randomIndex = (wordList.indices).random()
        if (usedWordIndices.size < wordList.size) {
            while (usedWordIndices.contains(randomIndex)) {
                randomIndex = (wordList.indices).random()
            }
            usedWordIndices.add(randomIndex)
        } else {
            usedWordIndices.clear()
        }

        _uiState.update {
            it.copy(
                currentExplainingPlayer = randomPlayer,
                currentWord = wordList[randomIndex],
                timeRemaining = 60,
                isTimerRunning = false,
                isRoundFinished = false
            )
        }
    }

    fun startTimer() {
        if (_uiState.value.isTimerRunning) return

        _uiState.update { it.copy(isTimerRunning = true) }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemaining > 0 && _uiState.value.isTimerRunning) {
                delay(1000L)
                _uiState.update { it.copy(timeRemaining = it.timeRemaining - 1) }
            }
            if (_uiState.value.timeRemaining == 0) {
                onTimeExpired()
            }
        }
    }

    fun onWordGuessed() {
        timerJob?.cancel()
        val currentState = _uiState.value
        val updatedTeams = currentState.teams.mapIndexed { index, team ->
            if (index == currentState.currentTeamIndex) {
                team.copy(score = team.score + 1)
            } else team
        }

        val winningTeam = updatedTeams.find { it.score >= currentState.targetScore }

        _uiState.update {
            it.copy(
                teams = updatedTeams,
                isTimerRunning = false,
                isRoundFinished = true,
                gameOver = winningTeam != null,
                winnerTeam = winningTeam
            )
        }
    }

    fun onTimeExpired() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                isTimerRunning = false,
                isRoundFinished = true
            )
        }
    }

    fun nextTurn() {
        if (_uiState.value.gameOver) return
        val nextTeamIndex = (_uiState.value.currentTeamIndex + 1) % 2
        _uiState.update { it.copy(currentTeamIndex = nextTeamIndex) }
        prepareNextTurn()
    }

    fun resetGame() {
        timerJob?.cancel()
        _uiState.value = GameUiState()
    }
}