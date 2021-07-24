package com.daniilmaster.pomodoro

data class StopwatchItem(
    val id: Int, // элемент
    var currentMs: Long, // текущие мс
    var periodMs: Long,  //  период мс (от скольки)
    var isStarted: Boolean // статус запущен или на паузе таймер
)