package com.danilashkerdin.timetableparser

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_auth.*
import java.io.FileNotFoundException

class AuthActivity : AppCompatActivity() {

    //Authentication info
    var authInfo = arrayOf("", "")

    //files
    val LOGIN_FILE = "login.txt"
    val PASSWORD_FILE = "password.txt"


    //Reads login and password from loginFileName and passwordFileName
    fun getAuthInfo(context: Context = this, loginFileName: String, passwordFileName: String) {
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

    private fun tryPasteAuthInfo() {
        if (LOGIN_FILE in this.fileList() && PASSWORD_FILE in this.fileList() && authInfo[0] == "" && authInfo[1] == "") getAuthInfo(
            this,
            LOGIN_FILE,
            PASSWORD_FILE
        )
        loginTextView.setText(authInfo[0])
        passwordTextView.setText(authInfo[1])
        Toast.makeText(applicationContext, getString(R.string.found_data), Toast.LENGTH_LONG).show()
    }

    private fun saveStringToFile(str: String, fileName: String) {
        try {
            this.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(str.toByteArray())
                it.close()
            }
        } catch (e: Exception) {
            Log.e("exception", "$e. Writing auth error")
        }
    }

    //Data loading AsyncTask class
    /*@SuppressLint("StaticFieldLeak")
    inner class Loader(login: String, password: String) : DataLoader(login, password, this) {

        override fun onPreExecute() {
            progressBar.visibility = View.VISIBLE
            logo.visibility = View.GONE
        }
        override fun doInBackground(vararg counters: Int?): MutableList<List<String>>? {

            var data:MutableList<List<String>>?  = null

            for (c in counters)
            {
                Log.e("doInBackground", "c: $c")

                data = if (c==null) getDataFromInternet() else getDataFromInternet(c)

                if (data == null) data = if (c==null) getDataFromStorage() else getDataFromStorage(c)
            }

            return data
        }
        override fun onPostExecute(result: MutableList<List<String>>?) {
            super.onPostExecute(result)

            if (result != null) {

                if (isOnline) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.sign_in_successfully) + getString(R.string.online_downloaded),
                        Toast.LENGTH_LONG
                    ).show()

                    //Saving login, password and data
                    if (LOGIN_FILE in fileList()) this@AuthActivity.deleteFile(LOGIN_FILE)
                    if (PASSWORD_FILE in fileList()) this@AuthActivity.deleteFile(PASSWORD_FILE)
                    saveStringToFile(authInfo[0], LOGIN_FILE)
                    saveStringToFile(authInfo[1], PASSWORD_FILE)

                    //Starting new activity
                    val intent = Intent(this@AuthActivity, MainActivity::class.java)
                    intent.putExtra("status", isOnline)
                    startActivity(intent)

                } else {
                    val builder = AlertDialog.Builder(this@AuthActivity)
                    builder.setTitle(getString(R.string.sign_in_failure))

                    builder.setMessage(
                        getString(R.string.saved_loading_question)
                                + getString(R.string.warning)
                    )

                    builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.offline_downloaded),
                            Toast.LENGTH_LONG
                        ).show()
                        //Starting new activity
                        val intent = Intent(this@AuthActivity, MainActivity::class.java)
                        intent.putExtra("status", isOnline)
                        startActivity(intent)
                    }

                    builder.setNegativeButton(getString(R.string.no)) { _, _ -> }
                    builder.create().show()
                }

            } else {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.sign_in_failure) + getString(R.string.try_again),
                    Toast.LENGTH_LONG
                ).show()
            }

            progressBar.visibility = View.GONE
            logo.visibility = View.VISIBLE

        }
    }
    */

    //Data loading AsyncTask class
    @SuppressLint("StaticFieldLeak")
    inner class Loader(login: String, password: String) : DataLoader(login, password, this) {

        fun startMainActivity() {
            //Starting new activity
            val intent = Intent(this@AuthActivity, MainActivity::class.java)
            intent.putExtra("status", isOnline)
            startActivity(intent)
        }

        override fun onPreExecute() {
            progressBar.visibility = View.VISIBLE
            logo.visibility = View.GONE
        }

        override fun doInBackground(vararg datereqs: String?): MutableList<List<String>>? {

            var data: MutableList<List<String>>? = null

            for (datereq in datereqs) {
                Log.e("doInBackground", "c: $datereq")

                try {
                    saveWeekDataFromInternet(datereq)
                    data = getDayFromFile("schedule_$datereq.html")
                } catch (e: Exception) {
                    Log.e("exception_auth_doinback", e.toString())
                    try {
                        data = getDayFromFile("schedule_$datereq.html")
                    } catch (e: FileNotFoundException) {
                        Log.e("doInBack_Auth", e.toString())
                    }
                }

            }

            Log.e("doInBack", data.toString())
            return data
        }

        override fun onPostExecute(result: MutableList<List<String>>?) {
            super.onPostExecute(result)

            if (result != null) {

                if (isOnline) {

                    Toast.makeText(
                        applicationContext,
                        getString(R.string.sign_in_successfully) + " " + getString(R.string.online_downloaded),
                        Toast.LENGTH_LONG
                    ).show()

                    //Saving login, password and data
                    if (LOGIN_FILE in fileList()) this@AuthActivity.deleteFile(LOGIN_FILE)
                    if (PASSWORD_FILE in fileList()) this@AuthActivity.deleteFile(PASSWORD_FILE)
                    saveStringToFile(authInfo[0], LOGIN_FILE)
                    saveStringToFile(authInfo[1], PASSWORD_FILE)

                    startMainActivity()

                } else {
                    val builder = AlertDialog.Builder(this@AuthActivity)
                    builder.setTitle(
                        getString(R.string.downloading_failure) + "\n" +
                                getString(R.string.check) + " " +
                                getString(R.string.internet_connection) + " " + getString(R.string.and) + " " +
                                getString(R.string.auth_data) + ". "
                    )

                    builder.setMessage(
                        getString(R.string.saved_loading_question) + " "
                                + getString(R.string.warning)
                    )

                    builder.setNegativeButton(getString(R.string.no)) { _, _ -> }

                    builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.offline_downloaded),
                            Toast.LENGTH_LONG
                        ).show()

                        startMainActivity()
                    }

                    builder.create().show()
                }

            } else {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.sign_in_failure) + " " + getString(R.string.try_again) + "\n" + getString(
                        R.string.check
                    ) + " " +
                            getString(R.string.internet_connection) + " " + getString(R.string.and) + " " +
                            getString(R.string.auth_data) + ". ",
                    Toast.LENGTH_LONG
                ).show()
            }

            Log.e("AuthActivity_loader", result.toString())
            progressBar.visibility = View.GONE
            logo.visibility = View.VISIBLE

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        //Checking internet permission
        val status = ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET)
        if (status != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.INTERNET),
                1
            )
        }

        tryPasteAuthInfo()
        loginButtonClickListener()

        button_signIn.setOnClickListener { loginButtonClickListener() }

    }

    private fun loginButtonClickListener() {
        authInfo[0] = loginTextView.text.toString()
        authInfo[1] = passwordTextView.text.toString()

        if ((authInfo[0] != "") && (authInfo[1] != "")) {
            val loader = Loader(authInfo[0], authInfo[1])
            val req = loader.date.getDateReq()
            loader.execute(req)
        }
    }
}


