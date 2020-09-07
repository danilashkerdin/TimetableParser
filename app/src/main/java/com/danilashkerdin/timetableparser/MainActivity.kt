package com.danilashkerdin.timetableparser

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Html.fromHtml
import android.text.Spanned
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileNotFoundException
import java.util.*


class MainActivity : AppCompatActivity() {

    private var authInfo = arrayOf("", "")

    //files
    val LOGIN_FILE = "login.txt"
    val PASSWORD_FILE = "password.txt"

    private var isOnline: Boolean = false
    var runningTasksCounter: Int = 0
    private var globalCalendar: Calendar = Calendar.getInstance()

    private val date = Date()

    //Reads login and password from loginFileName and passwordFileName
    private fun getAuthInfo(
        context: Context = this,
        loginFileName: String = LOGIN_FILE,
        passwordFileName: String = PASSWORD_FILE
    ) {
        try {
            context.openFileInput(loginFileName).use {
                authInfo[0] = it.readBytes().toString(Charsets.UTF_8)
            }

            context.openFileInput(passwordFileName).use {
                authInfo[1] = it.readBytes().toString(Charsets.UTF_8)
            }
        } catch (e: Exception) {
            Log.e("ReadingException", "$e. Reading auth error")
        }
    }

    //Data loading AsyncTask class
    @SuppressLint("StaticFieldLeak")
    inner class Loader(login: String, password: String) : DataLoader(
        login,
        password,
        this
    ) {

        @SuppressLint("SetTextI18n")
        override fun onPreExecute() {
            //textView.text = "\u2B73  " + getString(R.string.loading) + "\n" + "⌛  " + getString(R.string.wait)
            runningTasksCounter++
            //setDate(counter)
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg datereqs: String?): MutableList<List<String>>? {
            var data: MutableList<List<String>>? = null

            for (datereq in datereqs) {
                Log.e("doInBackground", "req: $datereq")
                if (datereq != null) {
                    try {
                        saveWeekDataFromInternet(datereq)
                        data = getDayFromFile("schedule_$datereq.html")
                    } catch (e: Exception) {
                        Log.e("doInBack_Main", e.toString())
                    }
                }
            }
            Log.e("doInBack", data.toString())
            return data
        }

        override fun onPostExecute(result: MutableList<List<String>>?) {
            super.onPostExecute(result)
            /*
            if (fileExists) {

                /*if (isOnline) {
                    Toast.makeText(applicationContext, getString(R.string.online_downloaded), Toast.LENGTH_LONG).show()
                    //statusTextView.text = getString(R.string.online_status)

                } else {
                    Toast.makeText(applicationContext, getString(R.string.offline_downloaded)+getString(R.string.warning), Toast.LENGTH_LONG).show()
                    //statusTextView.text = getString(R.string.offline_status)
                }*/

            } else {
                //statusTextView.text = getString(R.string.offline_status)
                //Toast.makeText(applicationContext, getString(R.string.downloading_failure), Toast.LENGTH_LONG).show()
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle(getString(R.string.schedule_not_found))

                builder.setMessage(getString(R.string.will_download))

                builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.offline_downloaded),
                        Toast.LENGTH_LONG
                    ).show()

                }

                builder.setNegativeButton(getString(R.string.no)) { _, _ -> }
                builder.create().show()
            }
            */

            runningTasksCounter--
            if (runningTasksCounter == 0) {

                if (result != null) {
                    if (isOnline) {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.online_downloaded),
                            Toast.LENGTH_LONG
                        ).show()
                        showTextContent(result)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.offline_downloaded),
                            Toast.LENGTH_LONG
                        ).show()
                        showTextContent(result)
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.downloading_failure) + " " +
                                getString(R.string.try_again),
                        Toast.LENGTH_LONG
                    ).show()
                    //textView.text = "❌  " + getString(R.string.error_status) + "  ❌"
                }

                progressBar.visibility = View.INVISIBLE
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarView.date = globalCalendar.timeInMillis
        //textView.movementMethod = ScrollingMovementMethod()
        isOnline = intent.getBooleanExtra("status", false)
    }

    override fun onResume() {
        super.onResume()

        getAuthInfo()

        globalCalendar = Calendar.getInstance()

        //Downloading today schedule
        downloadDailySchedule(globalCalendar)

        //Starts dateChangeListener
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            onDateChangeListener(
                year,
                month,
                dayOfMonth
            )
        }

        //calendarView.setOnLongClickListener { letUpdate() }

    }

    /*@SuppressLint("SetTextI18n")
    private fun setDate(counter: Int){
        val req = date.getDateReq(counter = counter)
        val weekDays = resources.getStringArray(R.array.weekdays)
        val dayOfWeek = weekDays[date.getDayOfWeekNumber(req)-1]
        headTextView.text = "$req, $dayOfWeek"
    }*/

    /*private fun buttonClickExecutor(counter: Int) {
        val loader = Loader(authInfo[0], authInfo[1])
        loader.execute(counter)
    }*/

    private fun showTextContent(lessons: List<List<String>>?) {
        Log.e("showContent_Main", lessons.toString())

        if (!lessons.isNullOrEmpty()) {

            /*textView.text =
                "" /* + getString(R.string.tab) + getString(R.string.num_simbol) +
                        getString(R.string.tab) +getString(R.string.time)+
                        getString(R.string.tab) +getString(R.string.classroom) +
                        getString(R.string.tab) + "\n" + "\n"
                   */
             */

            val listString = mutableListOf<Spanned>()

            lessons.forEach {

                val resultString =
                    "<br>" +
                            coloredString(
                                "\uD83D\uDCCC" + getString(R.string.tab) + "<b>" + it[3] + "</b>"
                                        + getString(R.string.tab) + " " + "<br>",
                                resources.getColor(R.color.colorPrimary)
                            ) +

                            "⏰  " + /*it[0]+"." +getString(R.string.tab)+*/ it[1] + getString(R.string.tab) +

                            "\uD83D\uDEAA  " + it[2] + getString(R.string.tab) + "<br>" +

                            "📕  " + "<b>" + it[4] + "</b>" + "<br>" +

                            "\uD83D\uDCBC  " + it[5] +/*"\n"+it[6] + */ "<br>"

                listString.add(fromHtml(resultString))
            }


            val adapter = ArrayAdapter<Spanned>(this, R.layout.list_tem, listString)

            listView.adapter = adapter

            //textView.setText(fromHtml(resultString), TextView.BufferType.SPANNABLE)
        } else {
            val resultString = "<br> 🙈  " + getString(R.string.no_classes) + "  🙉 <br>"
            val listString = mutableListOf<Spanned>(fromHtml(resultString))
            val adapter = ArrayAdapter<Spanned>(this, R.layout.list_tem, listString)

            listView.adapter = adapter

        }
    }

    private fun coloredString(str: String, color: Int): String {
        return "<font color=$color>$str</font>"
    }

    private fun onDateChangeListener(year: Int, month: Int, dayOfMonth: Int) {

        if (globalCalendar.get(Calendar.DAY_OF_MONTH) == dayOfMonth &&
            globalCalendar.get(Calendar.MONTH) == month &&
            globalCalendar.get(Calendar.YEAR) == year
        ) {
            letUpdate()
        } else {
            globalCalendar.set(year, month, dayOfMonth)
            downloadDailySchedule(globalCalendar)
        }


    }

    private fun downloadDailySchedule(calendar: Calendar = globalCalendar) {

        val loader = Loader(authInfo[0], authInfo[1])
        //calendarView.date = calendar.timeInMillis
        val localDateReq = date.getDateReq(calendar)

        try {
            val lessons = loader.getDayFromFile("schedule_$localDateReq.html")
            showTextContent(lessons)
        } catch (e: FileNotFoundException) {
            //Toast.makeText(applicationContext, getString(R.string.downloading_failure), Toast.LENGTH_LONG).show()
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle(getString(R.string.schedule_not_found) + "  \uD83D\uDD0E")
            builder.setMessage(getString(R.string.will_download))
            builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                //Toast.makeText( applicationContext, getString(R.string.offline_downloaded), Toast.LENGTH_LONG).show()
                loader.execute(localDateReq)
            }

            builder.setNegativeButton(getString(R.string.no)) { _, _ -> }
            builder.create().show()

        }
    }

    private fun letUpdate() {
        try {
            Log.e("onCalendarLongClick", "INSIDE")
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle(getString(R.string.update_question))
            builder.setMessage(getString(R.string.will_be_updated))
            builder.setNegativeButton(getString(R.string.no)) { _, _ -> }
            builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                val loader = Loader(authInfo[0], authInfo[1])
                //calendarView.date = calendar.timeInMillis
                val localDateReq = date.getDateReq(globalCalendar)
                loader.execute(localDateReq)
            }
            builder.create().show()
        } catch (e: Exception) {
            Log.e("onCalendarLongClick", e.toString())
        }
    }

}