package com.danilashkerdin.timetableparser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_note.*

class NoteActivity : AppCompatActivity() {

    private var dateReq: String? = null
    private var position: Int = 0
    private var fileName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        dateReq = intent.getStringExtra("dateReq")
        position = intent.getIntExtra("position", 0)
        fileName = "note" + "_" + dateReq + "_" + "$position" + ".txt"

        val str = openStringFromFile(fileName)
        if ((str != null) and (str != "")) editor.setText(str)

    }

    override fun onResume() {
        super.onResume()

        buttonSave.setOnClickListener {
            buttonClickListener()
        }

    }

    private fun buttonClickListener() {
        val str = editor.text.toString()

        var result = false
        if (str != "") result = saveStringToFile(str, fileName)
        val res = if (result) 1 else 0

        val intent = Intent()
        setResult(res, intent)
        finish()
    }

    private fun openStringFromFile(fileName: String): String? {
        var str: String? = null
        if (fileName in this.fileList()) {
            try {
                this.openFileInput(fileName).use {
                    str = it.readBytes().toString(Charsets.UTF_8)
                    it.close()
                }
                Log.e("openStringFromFile", str)
            } catch (e: Exception) {
                Log.e("exception", "$e. Reading auth error")
            }
        }
        return str
    }

    private fun saveStringToFile(str: String, fileName: String): Boolean {
        var result = false
        if (fileName in this.fileList()) this.deleteFile(fileName)
        try {
            this.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(str.toByteArray())
                it.close()
            }
            result = true
        } catch (e: Exception) {
            Log.e("exception", "$e. Writing auth error")
        }
        return result
    }

}