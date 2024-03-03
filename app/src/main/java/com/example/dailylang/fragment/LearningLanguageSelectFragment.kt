package com.example.dailylang.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailylang.Adapter.LanguageListAdapter
import com.example.dailylang.MainActivity
import com.example.dailylang.R
import com.example.dailylang.SecondActivity
import com.example.dailylang.data.LanguageModel
import com.example.dailylang.database.Projects
import com.example.dailylang.databinding.FragmentLearningLanguageSelectPageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LearningLanguageSelectFragment: Fragment() {
    private var _binding: FragmentLearningLanguageSelectPageBinding? = null
    private val binding get() = _binding!!

    lateinit var activity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLearningLanguageSelectPageBinding.inflate(inflater, container, false)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = LanguageListAdapter()
        val dataSet = listOf(LanguageModel("Korean", R.mipmap.korean), LanguageModel("English",
            R.mipmap.american
        ), LanguageModel("Japanese", R.mipmap.japan), LanguageModel("Spanish", R.mipmap.spain)
        )

        binding.recyclerView.adapter = adapter
        adapter.submitList(dataSet.toMutableList())
        adapter.setItemClickListener(object: LanguageListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                activity.learningLanguage = adapter.currentList[position].countryName
                CoroutineScope(Dispatchers.IO).launch {
                    activity.projectDao.insertProject(Projects(0, activity.defaultLanguage, activity.learningLanguage,  activity.learningLanguage, System.currentTimeMillis()))
                }
                findNavController().navigate(R.id.mainFragment)
            }

        })
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as MainActivity
    }
}