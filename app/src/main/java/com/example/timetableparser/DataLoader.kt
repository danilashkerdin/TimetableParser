package com.example.timetableparser

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.util.*

//Data loading AsyncTask class
abstract class DataLoader(
    private val login: String,
    private val password: String,
    private val context: Context
) :
    AsyncTask<String, Int, MutableList<List<String>>?>() {

    //Networking
    var isOnline: Boolean = false

    val date = Date()

    fun saveMonthDataFromInternet(dateReq: String? = null, counter: Int = 0) {

        val c = Calendar.getInstance()
        var datereq = dateReq
        if (datereq == null) datereq = date.getDateReq(c)
        val currentDate = date.getDate(datereq)
        c.set(currentDate.third, currentDate.second - 1, currentDate.first)
        c.add(Calendar.DATE, counter)

        //Log.e("saveMonthData_curDate:", currentDate.toString())

        val month = c.get(Calendar.MONTH)
        val dayOfWeek = date.getDayOfWeekNumber(datereq)
        c.add(Calendar.DATE, -(dayOfWeek - 1))
        //c.set(Calendar.DAY_OF_MONTH, 1)
        while (c.get(Calendar.MONTH) == month) {

            datereq = date.getDateReq(c)
            saveWeekDataFromInternet(datereq)
            c.add(Calendar.DATE, 7)

            Log.e("saveMonthData", datereq)
        }
        isOnline = true
    }

    fun saveWeekDataFromInternet(dateReq: String? = null) {

        val c = Calendar.getInstance()
        var datereq = dateReq
        if (datereq == null) datereq = date.getDateReq(c)


        val dateTriple = date.getDate(datereq)
        c.set(dateTriple.third, dateTriple.second - 1, dateTriple.first)
        val dayOfWeek = date.getDayOfWeekNumber(datereq)
        c.add(Calendar.DATE, -(dayOfWeek - 1))

        Log.e("saveWeek", "dateTriple: $dateTriple, dayOfWeek: $dayOfWeek")

        val doc = Jsoup.connect("https://msal.me/personal/index.php")
            .data("AUTH_FORM", "Y")
            .data("TYPE", "AUTH")
            //.data("backurl", "/personal/index.php")
            .data("USER_LOGIN", login).data("USER_PASSWORD", password)
            //.data("Login", "Войти")
            .data("datereq", datereq).timeout(0).post()

        val tableElements = doc.getElementsByClass("schedule__table").select("tbody")

        //Log.e("table_elements_weekData", tableElements.toString())
        if (tableElements.isNullOrEmpty()) {
            isOnline = false
        } else {
            isOnline = true
            saveStringToFile(doc.toString(), "document.html")
            //Log.e("doc_weekData", doc.toString())

            for (dayOfWeekNumber in 1..7) {
                val privateDatereq = date.getDateReq(c)
                val lessons = dataPreparing(tableElements, dayOfWeekNumber)
                saveDayToFile(lessons, "schedule_$privateDatereq.html")
                c.add(Calendar.DATE, 1)
            }
        }

    }

    public fun saveDayToFile(lessons: MutableList<List<String>>, fileName: String) {
        try {
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write("<html><head></head><body>\n".toByteArray())
                for (lesson in lessons) {
                    it.write("<lesson>\n".toByteArray())
                    for ((propCounter, lessonProperty) in lesson.withIndex()) {
                        it.write("<property$propCounter>".toByteArray())
                        it.write(lessonProperty.toByteArray())
                        it.write("</property$propCounter>\n".toByteArray())
                    }
                    it.write("</lesson>\n".toByteArray())
                }
                it.write("</body>\n</html>\n".toByteArray())
                it.close()
            }
        } catch (e: Exception) {
            Log.e("exception", "$e. Writing auth error")
        }
    }

    fun getDayFromFile(fileName: String): MutableList<List<String>> {

        val schedule =
            context.openFileInput(fileName).use { it.readBytes().toString(Charsets.UTF_8) }

        val elements = Jsoup.parse(schedule).getElementsByTag("lesson")
        val lessons = mutableListOf<List<String>>()
        for (elem in elements) {
            //val les = elements?.select("lesson")?.get(index)
            Log.e("getDay_lesson", elem.toString())
            var properties = mutableListOf<String>()

            if (elem.hasText()) {
                for (prop in elem.children()) {
                    properties.add(prop.ownText())
                    Log.e("getDay_properties", "prop:${prop.ownText()},  properties:$properties")
                }
                lessons.add(properties)
            }
        }

        Log.e("getDay_lessons", lessons.toString())
        return lessons
    }

    private fun saveDataFromInternet(
        dateCounter: Int = 0
    ): Elements? {
        val datereq = date.getDateReq(counter = dateCounter)
        return try {
            isOnline = true

            val doc = Jsoup.connect("https://msal.me/personal/index.php").data("AUTH_FORM", "Y")
                .data("TYPE", "AUTH")
                .data("USER_LOGIN", "s0120111").data("USER_PASSWORD", "5415397973")
                .data("datereq", datereq).post()


            val tableElements = doc.getElementsByClass("schedule__table").select("tbody")

            saveStringToFile(tableElements.html(), "schedule_$datereq.html")

            tableElements
        } catch (e: Exception) {
            isOnline = false
            Log.e("exception", e.toString())
            null
        }
    }

    fun getDataFromInternet(
        counter: Int = 0
    ): MutableList<List<String>>? {
        val elem = saveDataFromInternet(counter)

        return if (elem != null) {
            val req = date.getDateReq(counter = counter)
            val dayOfWeekNumber = date.getDayOfWeekNumber(req)
            val lessons = dataPreparing(elem, dayOfWeekNumber)
            Log.e("getDataFromInternet", "lessons: $lessons")
            lessons
        } else null
    }

    internal fun getDataFromStorage(
        counter: Int = 0
    ): MutableList<List<String>>? {

        val datereq = date.getDateReq(counter = counter)

        isOnline = false
        val fileName = "schedule_${datereq}.html"
        return try {

            var schedule = context.openFileInput(fileName).use {
                it.readBytes().toString(Charsets.UTF_8)
            }

            schedule =
                "<html><body><table><thead></thead><tbody>$schedule</tbody></table></body></html>"

            val element = Jsoup.parse(schedule).select("tbody")
            val lessons = dataPreparing(element, date.getDayOfWeekNumber(datereq))

            lessons
        } catch (e: Exception) {
            Log.e("exception", "$e. Getting data from storage error")
            null
        }
    }

    private fun dataPreparing(table: Elements, weekDayNumber: Int): MutableList<List<String>> {
        val lessons: MutableList<List<String>> = mutableListOf()

        //Preparing daily data
        for (rowNum in 0..7) {

            var lessonNum: String
            var lessonTime = ""
            var lessonPlace = ""
            var lessonType = ""
            var lessonName = ""
            var lessonLector = ""
            var link = ""
            var lessonData: List<String>? = null

            if (table.isNotEmpty()) {
                val row = table.select("tr")[rowNum]

                if (row.select("td")[0].hasText() && row.select("td")[1].hasText()) {

                    lessonNum = row.select("td")[0].text()
                    lessonTime = row.select("td")[1].text()

                    Log.e("weekDay", weekDayNumber.toString())

                    if (row.select("td")[weekDayNumber + 2 - 1].hasText()) {

                        val lesson = row.select("td")[weekDayNumber + 2 - 1]

                        lessonType =
                            lesson.toString().substringAfter("<td>").substringBefore("<br>")
                        lessonPlace = lesson.ownText().replace(lessonType, "")

                        Log.e("lessonType", lessonType)
                        Log.e("lessonPlace", lessonPlace)

                        try {

                            if (lesson.select("p")[0].hasText()) {
                                val lessonNameElem = lesson.select("p")[0]
                                lessonName = lessonNameElem.ownText()
                            }
                            Log.e("lessonName", lessonName)
                        } catch (e: Exception) {
                            Log.e("PreparingException", e.toString())
                        }

                        try {
                            if (lesson.select("p")[1].hasText()) {
                                val lessonLectorElem = lesson.select("p")[1]
                                lessonLector = lessonLectorElem.ownText()
                            }
                            Log.e("lessonLector", lessonLector)
                        } catch (e: Exception) {
                            Log.e("PreparingException", e.toString())
                        }

                        try {
                            if (lesson.select("a")[0].hasText()) {
                                val linkElem = lesson.select("p")[0]
                                link = linkElem.attr("href")
                                Log.e("link", link)
                            }
                        } catch (e: Exception) {
                            Log.e("PreparingException", e.toString())
                        }

                        lessonData = listOf(
                            lessonNum,
                            lessonTime,
                            lessonPlace,
                            lessonType,
                            lessonName,
                            lessonLector,
                            link
                        )

                    } else continue
                } else continue
            } else continue

            lessons.add(lessonData)

        }

        return lessons
    }

    private fun saveStringToFile(str: String, fileName: String) {
        if (fileName in context.fileList()) context.deleteFile(fileName)
        try {
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(str.toByteArray())
                it.close()
            }
        } catch (e: Exception) {
            Log.e("exception", "$e. Writing auth error")
        }
    }

    override fun doInBackground(vararg datereqs: String?): MutableList<List<String>>? {

        var data: MutableList<List<String>>? = null

        for (datereq in datereqs) {
            Log.e("doInBackground", "c: $datereq")

            saveMonthDataFromInternet(datereq)

            data = getDayFromFile("schedule_$datereq.html")
        }

        Log.e("doInBack", data.toString())
        return data
    }
}
