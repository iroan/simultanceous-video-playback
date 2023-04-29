package com.example.practiceandroid

import android.util.Log
import org.json.JSONObject
import java.io.*
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class ServerConn(urlstr: String) {
    private val url = URL(urlstr)

    public fun send(params: Map<String, String>): JSONObject {
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "application/json")
        val out: OutputStream? = conn.outputStream
        val writer = BufferedWriter(OutputStreamWriter(out, "UTF-8"))
        val reqData = JSONObject(params).toString()
        Log.d("ServerConn.reqData", reqData)
        writer.write(reqData)
        writer.flush()
        writer.close()
        out?.close()

        val input = conn.inputStream
        val reader = BufferedReader(InputStreamReader(input))
        val response = StringBuilder()
        reader.use {
            reader.forEachLine {
                response.append(it)
            }
        }
        val res = JSONObject(response.toString())
        Log.d("ServerConn.resData", res.toString())
        return res
    }

     fun get(urlstr: String): JSONObject {
         val url = URL(urlstr)
         val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000

        val input = conn.inputStream
        val reader = BufferedReader(InputStreamReader(input))
        val response = StringBuilder()
        reader.use {
            reader.forEachLine {
                response.append(it)
            }
        }
        val res = JSONObject(response.toString())
        Log.d("ServerConn.get", res.toString())
        return res
    }
}