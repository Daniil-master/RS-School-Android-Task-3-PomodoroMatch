package com.daniilmaster.pomodoro

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.daniilmaster.pomodoro.databinding.StopwatchItemBinding

class StopwatchAdapter(stopwatchListener: StopwatchListener, resources: Resources) : ListAdapter<StopwatchItem, StopwatchViewHolder>(itemComparator) {

    private var stopwatchListener: StopwatchListener = stopwatchListener
    private var resources = resources

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopwatchViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context) // получаем inflater
        val binding =
            StopwatchItemBinding.inflate(layoutInflater, parent, false)  // биндинг по inflater'у

        return StopwatchViewHolder(binding, stopwatchListener, resources) // передаем биндинг
    }

    override fun onBindViewHolder(holder: StopwatchViewHolder, position: Int) {
        holder.bind(getItem(position)) // выстраиваем холдер по позиции (постоянно выстраиваем)
    }

    private companion object {

        private val itemComparator =
            object : DiffUtil.ItemCallback<StopwatchItem>() { // сравниваем списки
                // чтобы правильно проиграть анимацию и показать результат пользователю
                override fun areItemsTheSame(
                    oldItem: StopwatchItem,
                    newItem: StopwatchItem
                ): Boolean {
                    return oldItem.id == newItem.id // возращаем проверенные старый и новый пункт
                }

                // Проверка на равество только те параметры модели, которые влияют на её визуальное представление на экране
                override fun areContentsTheSame(
                    oldItem: StopwatchItem,
                    newItem: StopwatchItem
                ): Boolean {
                    return oldItem.currentMs == newItem.currentMs &&
                            oldItem.isStarted == newItem.isStarted // проверка состояния старого и нового (время мс и запуск)
                }

                override fun getChangePayload(oldItem: StopwatchItem, newItem: StopwatchItem) = Any()
            }
    }
}