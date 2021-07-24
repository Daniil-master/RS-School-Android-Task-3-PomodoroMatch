package com.daniilmaster.pomodoro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes

class CustomViewCircularProgressbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
    // @JvmOverloads - используется антипатерн для возведение в одну конструкцию - конструкторы
    // @AttrRes - ожидается целочисленное возвращаемое значение параметра (например для: android.R.attr.action)
) : View(context, attrs, defStyleAttr) {
    /*
    attrs: AttributeSet? -  это набор свойств, указанных в файле ресурсов xml (Атрибуты по типу app:custom_style="fill")
    defStyleAttr: Int - атрибут для текущей теме, содержащий ссылку на ресурс стиля, который предоставляет значения по умолчанию для атрибутов StyledAttributes. Может быть 0, чтобы не искать значения по умолчанию.

    1) Получаем св-ва, атрибуты и значения
    2) Формируем кисточку
    3) Отрисовываем с расчитанным градусом дугу
     */

    // Хранимые значения
    private var periodMs = 0L // период в мс
    private var currentMs = 0L // текущее время в мс
    private var color = 0 // цвет
    private var style = FILL // стиль FILL или STROKE (с или без закраски)
    private val paint = Paint() // кисть

    init { // при инициализация
        if (attrs != null) { // если атрибов не было
            val styledAttrs = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.CustomView,
                defStyleAttr,
                0
            ) // получаем атрибуты из ресурса (R.styleable.CustomView, атрибуты: custom_color и custom_style)
            color = styledAttrs.getColor(R.styleable.CustomView_custom_color, Color.RED) // подставляем цвет (для св-в по умолчанию)
            style = styledAttrs.getInt(R.styleable.CustomView_custom_style, FILL) // подставляем стиль (для св-в по умолчанию)
            styledAttrs.recycle() // релиз атрибутов, очистка мусора, связанных с соответствующим ресурсом
        }

        // устанавливаем значения для кисточки
        paint.color = color // цвет (установленное св-во)
        paint.style = if (style == FILL) Paint.Style.FILL else Paint.Style.STROKE // стиль (по значению св-ва)
        paint.strokeWidth = 5F // размер кисти (px)
    }

    // Отрисовка
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (periodMs == 0L || currentMs == 0L) return // при 0 периоде или текущем значение остановить отрисовку
        val startAngel = (((currentMs % periodMs).toFloat() / periodMs) * 360) // расчет градуса

        canvas.drawArc( // в холсте (canvas) рисуем дугу
            0f, // влево
            0f, // вверх
            width.toFloat(), // вправо по ширине
            height.toFloat(), // вниз по высоте
            -90f, // начало дуги (сверху по прямой 90)
            startAngel, // до грудуса
            true, // от центра
            paint // кисточкой
        )
    }

    // Установка текущего тайминга в мс
    fun setCurrent(current: Long) {
        currentMs = current
        invalidate() // перерисовка View
    }

    // Установка периода
    fun setPeriod(period: Long) {
        periodMs = period
    }

    private companion object {
        private const val FILL = 0 // по умолчанию значение для FILL
    }
}