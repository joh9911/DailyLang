package com.example.dailylang.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailylang.Adapter.MainActivityListAdapter
import com.example.dailylang.MainActivity
import com.example.dailylang.R
import com.example.dailylang.SecondActivity
import com.example.dailylang.database.ProjectDao
import com.example.dailylang.databinding.FragmentMainPageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainFragment: Fragment() {
    private var _binding: FragmentMainPageBinding? = null
    private val binding get() = _binding!!

    lateinit var activity: MainActivity
    lateinit var adapter: MainActivityListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainPageBinding.inflate(inflater, container, false)


        initRecyclerView()
        initView()
        return binding.root
    }


    fun initView(){
        binding.mainFloatingButton.setOnClickListener {
            findNavController().navigate(R.id.defaultLanguageSelectFragment)
        }
        binding.arrangeLinearLayout.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                activity.projectDao.deleteAll()
                val values = activity.projectDao.getAllProjects()
                withContext(Dispatchers.Main){
                    adapter.submitList(values.toMutableList())
                }
            }

        }
    }

    fun initRecyclerView(){
        val layoutManager = LinearLayoutManager(requireActivity())
        binding.mainRecyclerView.layoutManager = layoutManager


        adapter = MainActivityListAdapter()
        binding.mainRecyclerView.adapter = adapter

        adapter.setItemClickListener(object: MainActivityListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(activity, SecondActivity::class.java).apply {
                    // "projectId"라는 키로 프로젝트 ID를 Intent에 추가
                    putExtra("projectId", adapter.currentList[position].uid)
                    putExtra("defaultLanguage", adapter.currentList[position].defaultLanguage)
                    putExtra("learningLanguage", adapter.currentList[position].learningLanguage)
                }
                startActivity(intent)

            }

        })
        CoroutineScope(Dispatchers.IO).launch {
            val values = activity.projectDao.getAllProjects()
            withContext(Dispatchers.Main){
                adapter.submitList(values.toMutableList())
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as MainActivity
    }
}