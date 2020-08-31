package com.example.timetableparser

import java.util.*

class Date() {

    //Counter means how many days to future or past we need to shift from today date
    fun getDateReq(c: Calendar = Calendar.getInstance(), counter: Int = 0): String {

        c.add(Calendar.DATE, counter)

        val todayDate = c.get(Calendar.DAY_OF_MONTH)
        val date = if (todayDate < 10) {
            "0$todayDate"
        } else {
            "$todayDate"
        }

        val todayMonth = c.get(Calendar.MONTH) + 1
        val month = if (todayMonth < 10) {
            "0$todayMonth"
        } else {
            "$todayMonth"
        }

        val todayYear = c.get(Calendar.YEAR)
        val year = todayYear.toString()

        return "$date.$month.$year"
    }

    fun getDate(datereq: kotlin.String): Triple<Int, Int, Int> {

        val date = getDatePartIndex(datereq.substringBefore("."))
        val month = getDatePartIndex(datereq.substringAfter(".").substringBeforeLast("."))
        val year = getDatePartIndex(datereq.substringAfterLast("."))

        return Triple(date, month, year)
    }

    private fun getDatePartIndex(str: String): Int {
        return if (str[0] == '0') str.substringAfter('0').toInt() else str.toInt()
    }

    fun getDayOfWeekNumber(datereq: String? = null): Int {

        val c = Calendar.getInstance()

        if (datereq == null) return (c.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1

        val dateList = datereq.split(".")

        val day = getDatePartIndex(dateList[0])
        val month = getDatePartIndex(dateList[1])
        val year = getDatePartIndex(dateList[2])

        c.set(year, month - 1, day)

        return (c.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1
    }

    fun getWeekReq(counter: Int = 0): String {

        val c = Calendar.getInstance()

        c.add(Calendar.WEEK_OF_YEAR, counter)

        val todayDate = c.get(Calendar.DAY_OF_MONTH)
        val date = if (todayDate < 10) {
            "0$todayDate"
        } else {
            "$todayDate"
        }

        val todayMonth = c.get(Calendar.MONTH) + 1
        val month = if (todayMonth < 10) {
            "0$todayMonth"
        } else {
            "$todayMonth"
        }

        val todayYear = c.get(Calendar.YEAR)
        val year = todayYear.toString()

        return "${date}.$month.$year"
    }

}