package com.example.dailylang.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.dailylang.SecondActivity
import com.example.dailylang.data.ChatModel
import com.example.dailylang.data.ChatResponseModel
import com.example.dailylang.database.DailyExpressions
import com.example.dailylang.databinding.FragmentRecordingBinding
import com.example.dailylang.viewmodel.SecondViewModel
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class RecordingFragment: Fragment() {
    var _binding: FragmentRecordingBinding? = null
    val binding get() = _binding!!

    lateinit var activity: SecondActivity
    lateinit var viewModel: SecondViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecordingBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[SecondViewModel::class.java]

        binding.btnStartRecording.setOnClickListener {
            activity.startCapturing()
        }

        binding.btnStopRecording.setOnClickListener {
            activity.stopCapturing()
            activity.convertSpeechToText()
        }

        binding.textView.setOnClickListener {
            showDialog()
        }


        return binding.root
    }

    fun showDialog(){
        val builder = AlertDialog.Builder(activity)

        builder.setTitle("Save Confirm")
        builder.setMessage("Do you want to save it?")

// 예 버튼을 눌렀을 때의 동작
        builder.setPositiveButton("Yes") { dialog, which ->
            sendMessage(binding.textView.text.toString())
        }

// 아니오 버튼을 눌렀을 때의 동작
        builder.setNegativeButton("No") { dialog, which ->
            dialog.cancel()
        }

        builder.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.isMediaProjectionSuccess.observe(viewLifecycleOwner){
            setButtonsEnabled(it)
        }
        viewModel.speechToTextValue.observe(viewLifecycleOwner){
            binding.textView.text = it
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as SecondActivity
    }
    fun setButtonsEnabled(isCapturingAudio: Boolean) {
        binding.btnStartRecording.isEnabled = !isCapturingAudio
        binding.btnStopRecording.isEnabled = isCapturingAudio

    }

    fun sendMessage(chat: String) {

        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

        val json = """
{
    "model": "gpt-3.5-turbo",
    "messages": [
        {
            "role": "user",
            "content": "${chat}. Translate that sentence in ${activity.defaultLanguage}"
        }
    ]
}
""".trimIndent()

        val body = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer ")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val messageText = extractContent(responseBody) // API 응답으로부터 메시지 내용을 추출하는 로직 구현 필요
                activity.projectDao.insertDailyExpression(DailyExpressions(0,chat,messageText, activity.uid!!))
                 withContext(Dispatchers.Main) {
                    Log.d("이 앱","$messageText")
                }
            } else {
                Log.d("ChatApp", "API Request Failed: ${response.body?.string()}")
            }
        }
    }
    fun extractContent(json: String?): String {
        val gson = Gson()
        val chatResponse = gson.fromJson(json, ChatResponseModel::class.java)
        return chatResponse.choices.first().message.content
    }
}