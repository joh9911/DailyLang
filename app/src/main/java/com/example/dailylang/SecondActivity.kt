package com.example.dailylang

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.dailylang.data.ResponseData
import com.example.dailylang.database.AppDatabase
import com.example.dailylang.database.ProjectDao
import com.example.dailylang.databinding.ActivitySecondBinding
import com.example.dailylang.fragment.RecordingFragment
import com.example.dailylang.viewmodel.SecondViewModel
import com.example.dailylang.viewmodel.SecondViewModelModelFactory
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds


class SecondActivity: FragmentActivity() {
    private var _binding: ActivitySecondBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: SecondViewModel

    lateinit var database: AppDatabase
    lateinit var projectDao: ProjectDao

    private lateinit var mediaProjectionManager: MediaProjectionManager

    var uid: Int? = null
    var defaultLanguage: String? = null
    var learningLanguage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySecondBinding.inflate(layoutInflater)
        val view = binding.root
        initView()
        initViewModel()
        initDb()

        val projectId = intent.getIntExtra("projectId", -1) // 기본값으로 -1을 사용
        val defaultLanguage = intent.getStringExtra("defaultLanguage")
        val learningLanguage = intent.getStringExtra("learningLanguage")

        if (projectId != -1) {
            uid = projectId
            this.defaultLanguage = defaultLanguage
            this.learningLanguage = learningLanguage
            Log.d("이 앱", "${defaultLanguage}")
        } else {
            // 에러 처리: 프로젝트 ID가 제대로 전달되지 않았을 경우
        }

        setContentView(view)
    }
    fun initDb(){
        database = AppDatabase.getDatabase(this)
        projectDao = database.projectDao()
    }
    fun initView(){
        val navHostFragment =
            supportFragmentManager.findFragmentById(binding.secondNavHost.id) as NavHostFragment
        binding.bottomNavigationView.setupWithNavController(navHostFragment.navController)
    }
    fun initViewModel(){
        val viewModelFactory = SecondViewModelModelFactory()
        viewModel = ViewModelProvider(this, viewModelFactory)[SecondViewModel::class.java]
    }

    fun startCapturing() {
        if (!isRecordAudioPermissionGranted()) {
            requestRecordAudioPermission()
        } else {
            startMediaProjectionRequest()
        }
    }

    fun convertSpeechToText() {
        val audioCapturesDir = File(getExternalFilesDir(null), "/AudioCaptures")
        val audioFiles = audioCapturesDir.listFiles()

        val path = audioFiles?.last()?.path!!
        val file = File(path)
        val audioBytes = file.readBytes()
        Log.d("이 앱","convertSpeechToText")
        val audioBase64 = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
        val apiKey = ""
        val jsonRequestBody = """
        {
          "config": {
            "encoding":"LINEAR16",
            "sampleRateHertz":44100,
            "languageCode":"en-US",
            "audioChannelCount": 2,
            "enableSeparateRecognitionPerChannel": false
          },
          "audio": {
            "content":"$audioBase64"
          }
        }
    """.trimIndent()

        val requestBody = jsonRequestBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("https://speech.googleapis.com/v1/speech:recognize?key=$apiKey")
            .post(requestBody)
            .build()

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .build()
        CoroutineScope(Dispatchers.IO).launch {
            val response = client.newCall(request).execute()
            if (response.isSuccessful){

                val responseBody = response.body?.string()
                val gson = Gson()
                val responseData = gson.fromJson(responseBody, ResponseData::class.java)
                val results = responseData.results
                withContext(Dispatchers.Main){
                    viewModel.setSpeechToTextValue(results.first().alternatives.first().transcript)

                }
                Log.d("이 앱","success $responseBody")
            }
            else{
                Log.d("이 앱","onFailure ${response.body?.string()}")
            }

        }


    }

    fun stopCapturing() {

        viewModel.setIsSuccess(false)
        startService(Intent(this, AudioCaptureService::class.java).apply {
            action = AudioCaptureService.ACTION_STOP
        })
        val audioCapturesDir = File(getExternalFilesDir(null), "/AudioCaptures")
        val audioFiles = audioCapturesDir.listFiles()

        val path = audioFiles.last().path
        playPCMFile(path)
        Log.d("이 앱","${audioFiles.first().path}")
    }


    fun playPCMFile(filePath: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val sampleRate = 44100 // 예를 들어 44.1kHz
            val channelConfig = AudioFormat.CHANNEL_OUT_STEREO // 녹음 시 사용된 채널 설정
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT // 녹음 시 사용된 오디오 포맷
            val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)


            val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBufferSize,
                AudioTrack.MODE_STREAM
            )
            audioTrack.setVolume(1.0f)

            val data = ByteArray(minBufferSize)
            try {
                val inputStream = FileInputStream(filePath)
                var read = 0

                audioTrack.play()

                while (inputStream.read(data).also { read = it } != -1) {
                    audioTrack.write(data, 0, read)
                }
            } catch (e: Exception) {
                // 오류 처리
                Log.d("이 앱","$e")

                e.printStackTrace()
            } finally {
                audioTrack.stop()
                audioTrack.release()
            }
        }

    }


    private fun isRecordAudioPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestRecordAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_PERMISSION_REQUEST_CODE
        )

    }

    private fun requestReadExternalStorage(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            EXTERNAL_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this,
                    "Permissions to capture audio granted. Click the button once again.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this, "Permissions to capture audio denied.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        if (requestCode == 12) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 부여됨
                Log.d("이 앱", "필요한 권한이 부여되었습니다.")
            } else {
                // 권한이 거부됨
                Log.d("이 앱", "필요한 권한이 거부되었습니다.")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * Before a capture session can be started, the capturing app must
     * call MediaProjectionManager.createScreenCaptureIntent().
     * This will display a dialog to the user, who must tap "Start now" in order for a
     * capturing session to be started. This will allow both video and audio to be captured.
     */
    private fun startMediaProjectionRequest() {
        // use applicationContext to avoid memory leak on Android 10.
        // see: https://partnerissuetracker.corp.google.com/issues/139732252
        mediaProjectionManager =
            applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(),
            MEDIA_PROJECTION_REQUEST_CODE
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MEDIA_PROJECTION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(
                    this,
                    "MediaProjection permission obtained. Foreground service will be started to capture audio.",
                    Toast.LENGTH_SHORT
                ).show()

                val audioCaptureIntent = Intent(this, AudioCaptureService::class.java).apply {
                    action = AudioCaptureService.ACTION_START
                    putExtra(AudioCaptureService.EXTRA_RESULT_DATA, data!!)
                }
                startForegroundService(audioCaptureIntent)
                viewModel.setIsSuccess(true)

            } else {
                Toast.makeText(
                    this, "Request to obtain MediaProjection denied.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 42
        private const val MEDIA_PROJECTION_REQUEST_CODE = 13
        private const val EXTERNAL_CODE = 12
    }
}