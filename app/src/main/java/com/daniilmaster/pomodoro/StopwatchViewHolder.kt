package com.daniilmaster.pomodoro

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.daniilmaster.pomodoro.databinding.StopwatchItemBinding

class StopwatchViewHolder(
    private val binding: StopwatchItemBinding, // биндинг
    private val listener: StopwatchListener, // интерфейс реализованный в MainActivity
    private val resources: Resources // обращение к ресурсам Android
) : RecyclerView.ViewHolder(binding.root) {

    private val TAG = "StopwatchViewHolder"
    private var timer: CountDownTimer? = null // таймер обратного отчета (встроенный Android класс)
    private var startTime = 0L

    fun bind(stopwatchItem: StopwatchItem) { // при строительстве данных
        binding.stopwatchTimer.text =
            stopwatchItem.currentMs.displayTime() // постоянно отображаем текущее время

        startTime = System.currentTimeMillis()
        binding.circularProgressbar.setCurrent(stopwatchItem.periodMs - stopwatchItem.currentMs)

        if (stopwatchItem.isStarted) { // если состояние запущен
            startTimer(stopwatchItem) // запускаем таймер
        } else {
            stopTimer() // если не запущен останавливаем таймер
        }

        initButtonsListeners(stopwatchItem) // реализовываем слушатели
    }

    // Реализация слушателей для кнопок
    private fun initButtonsListeners(stopwatchItem: StopwatchItem) {
        binding.startPauseButton.setOnClickListener {
            if (stopwatchItem.isStarted)  // пауза при запущенном иначе запуск
                listener.stop(stopwatchItem.id, stopwatchItem.currentMs)
            else {
                if (stopwatchItem.currentMs > 10L) // проверка при только больше 10 мс будет идти запуск (т.к. небольшая разница отчете)
                    listener.start(stopwatchItem.id)
            }

        }
        binding.circularProgressbar.setPeriod(stopwatchItem.periodMs)

        binding.restartButton.setOnClickListener {  // перезапуск таймера
            binding.circularProgressbar.setCurrent(0L)

            listener.reset(
                stopwatchItem.id,
                stopwatchItem.periodMs
            )

        }

        binding.deleteButton.setOnClickListener { listener.delete(stopwatchItem.id) } // удаление таймера
    }

    // Запуск таймера
    private fun startTimer(stopwatchItem: StopwatchItem) {
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_pause_24, null)
        binding.startPauseButton.setImageDrawable(drawable) // установка иконки паузы

        timer?.cancel() // останавливаем таймер обратного отчета (CountDownTimer)
        timer = getCountDownTimer(stopwatchItem) // получаем таймер CountDownTimer из StopwatchItem
        timer?.start() // запускаем таймер

        binding.blinkingIndicator.isInvisible = false // делаем видимым индикатор
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start() // запускаем анимацию индиктора
    }

    // Стоп таймера
    private fun stopTimer() {
        val drawable =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_play_arrow_24, null)
        binding.startPauseButton.setImageDrawable(drawable) // установка иконки плей

        timer?.cancel() // останавливаем таймер

        binding.blinkingIndicator.isInvisible = true // делаем не видимым индикатор
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop() // останавливаем анимацию индиктора
    }

    // Возращаем CountDownTimer из StopwatchItem
    private fun getCountDownTimer(stopwatchItem: StopwatchItem): CountDownTimer {
        return object : CountDownTimer(
            stopwatchItem.currentMs,
            UNIT_TEN_MS
        ) { // создаем Таймер с периодом (1 день) и интервалом (10мс) по константам

            override fun onTick(millisUntilFinished: Long) { // время тикает
                stopwatchItem.currentMs = millisUntilFinished // к текущему времени + интервал
                binding.circularProgressbar.setCurrent(stopwatchItem.periodMs - stopwatchItem.currentMs)
                binding.stopwatchTimer.text =
                    stopwatchItem.currentMs.displayTime() // в текст отображаем обновляемые данные
            }

            override fun onFinish() { // при окончании
                stopTimer() // остановка таймера (обновление ui)
                binding.stopwatchTimer.text =
                    stopwatchItem.currentMs.displayTime()  //  отображаем оставшееся время
            }
        }
    }

    // Расширенная функция для Long по форатированию времени в String
    private fun Long.displayTime(): String {
        if (this <= 0L) { // при 0 значении ворматируем по константе (0:0...)
            return START_TIME
        }
        // преводим в часы, минуты, секунды и миллисекунды
        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60
//        val ms = this % 1000 / 10

        // возращаем начиная от часов формат (h:m:s:ms) через возращаемый слот для 0 (пример: 09 а не 9)
        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}" // : ${displaySlot(ms)}
    }

    // Добавляет 0 при меньше 10 в формате 09 (при 9), при больше 29 в нормальном
    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {
        private const val START_TIME = "00:00:00" // начало с 0
        private const val UNIT_TEN_MS = 10L // интервал 10 мс = 1 с

    }
}