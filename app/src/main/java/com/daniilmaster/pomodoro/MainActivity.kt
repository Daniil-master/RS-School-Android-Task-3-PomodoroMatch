package com.daniilmaster.pomodoro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import com.daniilmaster.pomodoro.databinding.ActivityMainBinding
import com.daniilmaster.pomodoro.foreground.*

class MainActivity : AppCompatActivity(), StopwatchListener {
    /*
    • Интерфейс StopwatchListener - для управления в Activity таймером, передается в StopwatchAdapter через конструкторв,
    далее в StopwatchViewHolder также через конструктор
    • RecyclerView - состоит из StopwatchAdapter и StopwatchViewHolder
    • StopwatchItem - посредник данных таймера используется в stopwatchesList, при кнопки "Add"

     */

    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding // view binding
    private lateinit var stopwatchAdapter: StopwatchAdapter // адпатер
    private val stopwatchesList = mutableListOf<StopwatchItem>() // список StopwatchItem
    private var nextId = 0 // счетчик id
    private var startTime = 0L
    private var isStarted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater) // биндим по текущему inflater'у
        setContentView(binding.root) // устанавливаем layout из биндинга
        stopwatchAdapter =
            StopwatchAdapter(this, resources) // передаем StopwatchListener и доступ к ресурсам

        binding.recycler.apply { // из RecyclerView обращаемся
            layoutManager = LinearLayoutManager(context) // создаем layoutManager
            adapter = stopwatchAdapter // устанавливаем адаптер
        }

        binding.addNewStopwatchButton.setOnClickListener { // для кнопки "Add" делаем слушатель
            if (binding.etPeriodMinute.text.toString().isNotEmpty()) {
                val periodMinute = binding.etPeriodMinute.text.toString().toLong()
                if (periodMinute > 0L) {
                    val periodMs = periodMinute * 1000 * 60
                    stopwatchesList.add(
                        StopwatchItem(
                            nextId++, // ид
                            periodMs, // текущее время (точка старта)
                            periodMs, // период
                            false // первоначальный статус (запуск)
                        )
                    ) // добавляем в список Stopwatch с повышенным id, временм 0 мс и без старта
                    stopwatchAdapter.submitList(stopwatchesList.toList()) // отправляем в адаптер измененный список (с новым таймером, классом Stopwatch)
                } else
                    Toast.makeText(this, "Введите период больше 0 минуты", Toast.LENGTH_SHORT)
                        .show()
            } else
                Toast.makeText(this, "Введите период", Toast.LENGTH_SHORT).show()

        }
    }

    // Запуск таймера
    override fun start(id: Int) {
        if (!isStarted) {
            changeStopwatch(
                id,
                null,
                true
            ) // передаем таймера id, никакого времени мс и статус запуск
            isStarted = true
        } else
            Toast.makeText(this, "Уже запущен таймер", Toast.LENGTH_SHORT).show()

//        lifecycleScope.launch(Dispatchers.Main) {
//            while (true) {
//                binding.timerView.text = (System.currentTimeMillis() - startTime).displayTime()
//                delay(StopwatchViewHolder.UNIT_TEN_MS)
//            }
//        }
    }

    // Остановка таймера
    override fun stop(id: Int, currentMs: Long) {
        if (isStarted) {
            changeStopwatch(
                id,
                currentMs,
                false
            ) // передаем таймера id, текущее время мс и статус остановки
            isStarted = false
        }


    }

    // Очистка таймера
    override fun reset(id: Int, currentMs: Long) {
        changeStopwatch(
            id,
            currentMs
        ) // передаем таймера id, время и статус остановки
    }

    // Удаление таймера
    override fun delete(id: Int) {
        stopwatchesList.remove(stopwatchesList.find { it.id == id }) // удаляем из текущего списка с заданным id
        stopwatchAdapter.submitList(stopwatchesList.toList()) // отправляем в адаптер новый список
    }

    // Принятие
    private fun changeStopwatch(
        id: Int,
        currentMs: Long?,
        isStarted: Boolean = false
    ) {
        val newTimers = mutableListOf<StopwatchItem>() // создаем список Stopwatch
        stopwatchesList.forEach { // проходя по списку имеющегося Stopwatch
            if (it.id == id) { // при равных входимого id и со списка id
                // добавляем в новый таймер со текущим ид, входным временем и состоянием старта (объект Stopwatch)

                onStartForeground()
                newTimers.add(
                    StopwatchItem(
                        it.id,
                        currentMs ?: it.currentMs,
                        it.periodMs,
                        isStarted
                    )
                )
                Log.d(
                    TAG,
                    "ID: ${it.id}, CurrentMS: ${it.currentMs}, PeriodMS: ${it.periodMs}, IsStarted: ${it.isStarted} "
                )
            } else {
                newTimers.add(it) // иначе при не равных id добавляем все остальные
            }
        }
        stopwatchAdapter.submitList(newTimers) // отправляем в адаптер новый список Stopwatch
        stopwatchesList.clear() // чистим старый (текущий) список
        stopwatchesList.addAll(newTimers) // добавляем все эллементы от нового списка в старый (текущий) список
    }

    private fun onStartForeground() {
        startTime = System.currentTimeMillis()

//        lifecycleScope.launch(Dispatchers.Main) {
//            while (true) {
//                binding.timerView.text = (System.currentTimeMillis() - startTime).displayTime()
//                delay(INTERVAL)
//            }
//        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startIntent.putExtra(STARTED_TIMER_TIME_MS, startTime)
        startService(startIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    private companion object {

        private const val INTERVAL = 10L
    }
}