package com.example.dailylang.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailylang.Adapter.ChatListAdapter
import com.example.dailylang.SecondActivity
import com.example.dailylang.data.ChatModel
import com.example.dailylang.data.ChatResponseModel
import com.example.dailylang.database.DailyExpressions
import com.example.dailylang.databinding.FragmentChatBinding
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

class ChatFragment: Fragment() {
    var _binding: FragmentChatBinding? = null
    val binding get() = _binding!!

    lateinit var adapter: ChatListAdapter
    lateinit var activity: SecondActivity
    private lateinit var viewModel: SecondViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        initViewModel()
        initView()
        initMessage()
        return binding.root
    }


    private fun initViewModel(){

        viewModel = ViewModelProvider(requireActivity())[SecondViewModel::class.java]
        viewModel.setInitPrompt(ChatModel(
                text = "You're a great language teacher. You have the ability to teach more practical languages. My default language is ${activity.defaultLanguage}. Your default language is ${activity.learningLanguage}. From now on, you have to translate what I say into really casual, realistic expressions, If I give you something in ${activity.defaultLanguage}, you have to translate it into ${activity.learningLanguage} for me, And if I give you something in ${activity.learningLanguage}, you have to translate it into ${activity.defaultLanguage} for me. But, you can't use incorrect or awkward expressions. You really have to provide practical and realistic expressions that could actually be used. No need to explain, just give the translated sentence. If you understand, say \"send me the expression that you want\" politely in ${activity.defaultLanguage} only in first time.",
        timestamp = System.currentTimeMillis(),
        isSent = true // false로 설정하여, 이 메시지가 사용자로부터 온 것이 아니라 시스템에 의해 설정된 것임을 나타냅니다.
        ))
        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        adapter = ChatListAdapter()
        binding.recyclerView.adapter = adapter
        adapter.setItemClickListener(object: ChatListAdapter.OnItemClickListener{
            override fun onClick(position: Int) {
                if(position !=0)
                    showDialog(position)
            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { view, windowInsets ->

            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.ime()) // IME (Input Method Editor) 즉, 키보드에 대한 insets을 가져옵니다.
            view.updatePadding(bottom = insets.bottom) // 리사이클러뷰의 바텀 패딩을 키보드 높이만큼 조정합니다.
            windowInsets // WindowInsetsCompat 객체를 반환하여 다른 뷰들도 insets을 적용할 수 있도록 합니다.

        }
        initObserver()
    }

    fun initObserver(){
        viewModel.chatMessages.observe(viewLifecycleOwner){ messages->
            val filteredMessages = messages.filterNot { it.text == viewModel.initialPrompt?.text }
            adapter.submitList(filteredMessages.toMutableList())
            Log.d("이 앱","추가")
            val lastPosition = binding.recyclerView.adapter?.itemCount
            if (lastPosition != null && lastPosition >= 0) {
                binding.recyclerView.smoothScrollToPosition(lastPosition) // 부드럽게 스크롤을 마지막 위치로 이동
            }
        }

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as SecondActivity

    }

    fun initView(){
        binding.chatSendButton.setOnClickListener {
            val chat = binding.chatInputEdittext.text.toString()
            sendMessage(chat)
        }
        binding.chatInputEdittext.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                // 키보드가 나타나고 나서 리사이클러뷰를 마지막 아이템으로 스크롤합니다.
                binding.recyclerView.post {
                    val itemCount = binding.recyclerView.adapter?.itemCount ?: 0
                    if (itemCount > 0) {
                        binding.recyclerView.scrollToPosition(itemCount - 1)
                    }
                }
            }
        }

    }
    fun sendMessage(chat: String) {
        viewModel.addMessage(ChatModel(chat, System.currentTimeMillis(), true))
        binding.chatInputEdittext.setText("")
        binding.chatInputEdittext.clearFocus()

        val chatHistoryJson = viewModel.createChatHistoryJson()
        Log.d("이 앱","${chatHistoryJson}")
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer ")
            .post(chatHistoryJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val messageText = extractContent(responseBody) // API 응답으로부터 메시지 내용을 추출하는 로직 구현 필요
                withContext(Dispatchers.Main) {
                    viewModel.addMessage(ChatModel(messageText, System.currentTimeMillis(), false))
                }
            } else {
                Log.d("ChatApp", "API Request Failed: ${response.body?.string()}")
            }
        }
    }

    fun showDialog(position:Int){
        val builder = AlertDialog.Builder(activity)

        builder.setTitle("Save Confirm")
        builder.setMessage("Do you want to save it?")

// 예 버튼을 눌렀을 때의 동작
        builder.setPositiveButton("Yes") { dialog, which ->
            val a = adapter.currentList[position].text!!
            val b = adapter.currentList[position-1].text!!
            Log.d("이 왜","${adapter.currentList[position].text}  ${adapter.currentList[position-1].text}")
            CoroutineScope(Dispatchers.IO).launch {
                activity.projectDao.insertDailyExpression(DailyExpressions(0,a,b, activity.uid!!))
            }
        }

// 아니오 버튼을 눌렀을 때의 동작
        builder.setNegativeButton("No") { dialog, which ->
            dialog.cancel()
        }

        builder.show()
    }


    fun extractContent(json: String?): String {
        val gson = Gson()
        val chatResponse = gson.fromJson(json, ChatResponseModel::class.java)
        return chatResponse.choices.first().message.content
    }

    fun initMessage(){
        if (viewModel.isInitialized) return
        viewModel.isInitialized = true

        val chatModel = ChatModel(
            text = "You're a great language teacher. You have the ability to teach more practical languages. My default language is ${activity.defaultLanguage}. Your default language is ${activity.learningLanguage}. From now on, you have to translate what I say into really casual, realistic expressions, If I give you something in ${activity.defaultLanguage}, you have to translate it into ${activity.learningLanguage} for me, And if I give you something in ${activity.learningLanguage}, you have to translate it into ${activity.defaultLanguage} for me. But, you can't use incorrect or awkward expressions. You really have to provide practical and realistic expressions that could actually be used. No need to explain, just give the translated sentence. If you understand, say \"send me the expression that you want\" politely in ${activity.defaultLanguage} only in first time.",
            timestamp = System.currentTimeMillis(),
            isSent = true // false로 설정하여, 이 메시지가 사용자로부터 온 것이 아니라 시스템에 의해 설정된 것임을 나타냅니다.
        )
        viewModel.addMessage(chatModel)
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

        val json = """
{
    "model": "gpt-3.5-turbo",
    "messages": [
        {
            "role": "user",
            "content": "You're a great language teacher. You have the ability to teach more practical languages. My default language is ${activity.defaultLanguage}. Your default language is ${activity.learningLanguage}. From now on, you have to translate what I say into really casual, realistic expressions, If I give you something in ${activity.defaultLanguage}, you have to translate it into ${activity.learningLanguage} for me, And if I give you something in ${activity.learningLanguage}, you have to translate it into ${activity.defaultLanguage} for me. But, you can't use incorrect or awkward expressions. You really have to provide practical and realistic expressions that could actually be used. No need to explain, just give the translated sentence. If you understand, say \"send me the expression that you want\" politely in ${activity.defaultLanguage} only in first time."
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
            client.newCall(request).execute().use {
                if (it.isSuccessful){
                    val responseBody = it.body?.string()
                    val data = extractContent(responseBody)
                    withContext(Dispatchers.Main){
                        Log.d("이 앱","$responseBody")
                        viewModel.addMessage(
                            ChatModel(data, System.currentTimeMillis(), false)
                        )
                    }
                }
                else{
                    Log.d("이 앱","${it.body?.string()}")
                }
            }

        }


    }
}