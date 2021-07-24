package com.daniilmaster.pomodoro

interface StopwatchListener {
    fun start(id: Int) // запуск таймера

    fun stop(id: Int, currentMs: Long) // остановка таймера

    fun reset(id: Int, currentMs: Long) // очистка таймера

    fun delete(id: Int) // удаление таймера
}