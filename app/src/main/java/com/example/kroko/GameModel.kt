package com.example.kroko

data class Player(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String
)

data class Team(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val members: List<Player>,
    var score: Int = 0
)

data class GameUiState(
    val teams: List<Team> = emptyList(),
    val currentTeamIndex: Int = 0,
    val currentExplainingPlayer: Player? = null,
    val currentWord: String = "",
    val timeRemaining: Int = 60,
    val isTimerRunning: Boolean = false,
    val isRoundFinished: Boolean = false,
    val targetScore: Int = 3,
    val winnerTeam: Team? = null,
    val gameOver: Boolean = false
)