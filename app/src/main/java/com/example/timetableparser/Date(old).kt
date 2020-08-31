package com.example.timetableparser

import java.util.*

class DateOld {

    private var fullDate: Array<String> = arrayOf("", "", "", "")

    private val calendar = Calendar.getInstance()

    private var day = calendar.get(Calendar.DAY_OF_MONTH)
    private var month = calendar.get(Calendar.MONTH)
    private var year = calendar.get(Calendar.YEAR)

    private val weekDays: Array<String> = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    private var dayInWeekNumber = calendar.get(Calendar.DAY_OF_WEEK)
    private var dayOfWeek = weekDays[dayInWeekNumber - 1]

    fun setCurrentDate() {
        //setting current date

        fullDate = arrayOf(dayOfWeek, day.toString(), month.toString(), year.toString())
    }

    fun setNextDate() {
        //setting next day date

        val months = arrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        if (year % 4 == 0) months[1] = 29

        day++

        //For month changing
        if (day > months[month - 1]) {
            month++

            //For year
            if (month > 12) {
                month = 1; year++
            }

            day = 1
        }

        //For week
        dayInWeekNumber++
        if (dayInWeekNumber == 8) dayInWeekNumber = 1
        dayOfWeek = weekDays[dayInWeekNumber - 1]

        fullDate = arrayOf(dayOfWeek, day.toString(), month.toString(), year.toString())
    }

    fun setPreviousDate() {
        //setting previous day date

        val months = arrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        if (year % 4 == 0) months[1] = 29

        day--

        //For month changing
        if (day < 1) {
            month--

            //For year changing
            if (month < 1) {
                month = 12; year--
            }

            day = months[month - 1]
        }

        //For week
        dayInWeekNumber--
        if (dayInWeekNumber == 0) dayInWeekNumber = 7
        dayOfWeek = weekDays[dayInWeekNumber - 1]

        fullDate = arrayOf(dayOfWeek, day.toString(), month.toString(), year.toString())
    }

}