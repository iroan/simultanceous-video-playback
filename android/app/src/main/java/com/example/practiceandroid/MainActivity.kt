package com.example.practiceandroid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.practiceandroid.databinding.LoginBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var binding: LoginBinding
    private fun loginFailTips() {
        runOnUiThread {
            Toast.makeText(this, "账户或密码错误，请重试", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginVerify() {
        thread {
            var conn: HttpURLConnection? = null
            try {
                val response = StringBuilder()

                val config =this.assets.open("config.yml")
                val proper = Properties()
                proper.load(config)
                val tmp = "${proper.getProperty("serverUrl")}/login/"
                Log.d("login url", tmp)
                val url = URL(tmp)
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Accept", "application/json")
                val out: OutputStream = conn.outputStream
                val writer = BufferedWriter(OutputStreamWriter(out, "UTF-8"))
                val account = binding.account.text.toString()
                val tmp1 = mapOf("account" to account, "password" to binding.password.text.toString())
                val params = JSONObject(tmp1).toString()
                Log.d("login params", params)
                writer.write(params)
                writer.flush()
                writer.close()
                out.close()

                val input = conn.inputStream
                val reader = BufferedReader(InputStreamReader(input))
                reader.use {
                    reader.forEachLine {
                        response.append(it)
                    }
                }
                val tmp2 = JSONObject(response.toString())
                val token = tmp2.getString("token")
                if (tmp2.getBoolean("ok")) {
                    val intent = Intent(this, VideoManager::class.java)
                    intent.putExtra("account",account)
                    intent.putExtra("token",token)

                    startActivity(intent)
                } else {
                    loginFailTips()
                }
            } catch (e: JSONException) {
                Log.d("JSONException", e.toString())
                loginFailTips()
            } catch (e: Exception) {
                Log.d("Exception", e.toString())
                loginFailTips()
            } finally {
                conn?.disconnect()
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.login.setOnClickListener {
            loginVerify()
        }
    }
}