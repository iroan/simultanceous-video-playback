package com.example.practiceandroid

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.practiceandroid.databinding.VideoBinding
import okio.ByteString.Companion.toByteString
import org.json.JSONException
import org.json.JSONObject
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.properties.Delegates
import kotlin.text.StringBuilder as StringBuilder1

class VideoManager : AppCompatActivity() {
    lateinit var vb: VideoBinding
    private var isMaster = false
    private lateinit var serverConn: ServerConn
    private var videoProgress: Int = 0
    lateinit var SERVER_URL: String
    private var isSelectedVideo = false

    fun onRadioButtonClicked(v: View) {
        if (v is RadioButton) {
            when (v.getId()) {
                vb.radioMaster.id -> {
                    isMaster = true
                }
                vb.radioSlave.id -> {
                    isMaster = false
                }
            }
        }
    }

    private fun loginFailTips() {
        runOnUiThread {
            Toast.makeText(this, "账户或密码错误，请重试", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = VideoBinding.inflate(layoutInflater)
        setContentView(vb.root)
        // intent实际上调用了 parent类的getIntent方法
        val token = intent.getStringExtra("token")
        val account = intent.getStringExtra("account")
        val config = this.assets.open("config.yml")
        val proper = Properties()
        proper.load(config)
        SERVER_URL = "${proper.getProperty("serverUrl")}"
        serverConn = ServerConn("${SERVER_URL}/status/")
        val videoSelectLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val fileUri = result.data?.data
                val sha1 = getVideoSha1(fileUri)
                thread {
                    if (isMaster) {
                        newSession(account, sha1, token)
                    } else {
                        joinSession(account, sha1, token)
                    }
                }
                vb.videoView.setVideoURI(result.data?.data)
                isSelectedVideo = true;
            }
        }

        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (isMaster && vb.videoView.isPlaying) {
                    videoProgress = vb.videoView.currentPosition
                    vb.videoProgress.progress = videoProgress
                    updateProgress(account, String.format("%d", videoProgress), token)
                    Log.d("videoProgress", String.format("%d", videoProgress))
                }
                if (!isMaster && isSelectedVideo) {
                    val progress = getProgress(account, token);
                    if (progress != 0 && abs(vb.videoView.currentPosition - progress) > 10) {
                        vb.videoView.seekTo(progress+5);
                    }
                }
            }
        }, 2000, 2000)

        vb.videoSelect.setOnClickListener {
            Log.d("log", "you click select")
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "video/*"
            }

            if (intent.resolveActivity(packageManager) != null) {
                videoSelectLauncher.launch(intent)
            }
        }

        vb.videoStart.setOnClickListener {
            Log.d("log", "you click start")
            if (!vb.videoView.isPlaying) {
                vb.videoProgress.max = vb.videoView.duration
                vb.videoView.start()
            }
        }

        vb.videoPause.setOnClickListener {
            Log.d("log", "you click pause")
            if (vb.videoView.isPlaying) {
                vb.videoView.pause()
            }
        }

        vb.videoResume.setOnClickListener {
            Log.d("log", "you click resume")
            if (vb.videoView.isPlaying) {
                vb.videoView.resume()
            }
        }

        vb.preStep.setOnClickListener {
            if (vb.videoView.isPlaying) {
                val curr = vb.videoView.currentPosition
                vb.videoView.seekTo(curr - 10000)
            }
        }

        vb.nextStep.setOnClickListener {
            if (vb.videoView.isPlaying) {
                val curr = vb.videoView.currentPosition
                vb.videoView.seekTo(curr + 10000)
            }
        }

    }

    private fun newSession(account: String?, sha1: String, token: String?) {
        try {
            val tmp1 = mapOf(
                "account" to account, "action" to "newSession", "videoSHA1" to sha1, "token" to token
            )
            val res = serverConn.send(tmp1 as Map<String, String>)
            Log.d("VideoManager.res", res.toString())
        } catch (e: JSONException) {
            Log.d("JSONException", e.toString())
            loginFailTips()
        } catch (e: Exception) {
            Log.d("Exception", e.toString())
            loginFailTips()
        }
    }

    private fun joinSession(account: String?, sha1: String, token: String?) {
        try {
            val tmp1 = mapOf(
                // TODO master
                "account" to account, "master" to "wangkaixuan", "action" to "joinSession", "videoSHA1" to sha1, "token" to token
            )
            val res = serverConn.send(tmp1 as Map<String, String>)
            Log.d("VideoManager.res", res.toString())
        } catch (e: JSONException) {
            Log.d("JSONException", e.toString())
            loginFailTips()
        } catch (e: Exception) {
            Log.d("Exception", e.toString())
            loginFailTips()
        }
    }

    private fun updateProgress(account: String?, progress: String, token: String?) {
        try {
            val tmp1 = mapOf(
                "account" to account, "action" to "updateProgress", "progress" to progress, "token" to token
            )
            val res = serverConn.send(tmp1 as Map<String, String>)
            Log.d("VideoManager.res", res.toString())
        } catch (e: JSONException) {
            Log.d("JSONException", e.toString())
        } catch (e: Exception) {
            Log.d("Exception", e.toString())
        }
    }

    private fun getProgress(account: String, token: String?): Int {
        try {
            val queryString = "action=sessionProgress&account=${account}&token=${token}&master=wangkaixuan"
            val url = "${SERVER_URL}/status/?${queryString}"
//            Log.d("getProgress",url)
            val res = serverConn.get(url)
            return res.getInt("progress")
        } catch (e: JSONException) {
            Log.d("JSONException", e.toString())
        } catch (e: Exception) {
            Log.d("Exception", e.toString())
        }

        return 0
    }

    private fun getVideoSha1(fileUri: Uri?): String {
        val resolver = applicationContext.contentResolver
        var hex = ""
        fileUri?.let {
            resolver.openInputStream(it).use { stream ->
                val sha1 = MessageDigest.getInstance("sha1")
                val dis = DigestInputStream(stream, sha1)
                val buffer = ByteArray(1024 * 8)
                while (dis.read(buffer) != -1) {

                }
                dis.close()

                val raw: ByteArray = dis.messageDigest.digest();
                for (b in raw) {
                    hex += String.format("%02x", b)
                }
            }
        }
        Log.d("sha1", hex)
        return hex
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}