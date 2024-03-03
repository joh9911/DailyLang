package com.example.dailylang.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailylang.Adapter.LanguageListAdapter
import com.example.dailylang.MainActivity
import com.example.dailylang.R
import com.example.dailylang.data.LanguageModel
import com.example.dailylang.databinding.FragmentDefaultLanguageSelectPageBinding

class DefaultLanguageSelectFragment: Fragment() {
    private var _binding: FragmentDefaultLanguageSelectPageBinding? = null
    private val binding get() = _binding!!
    lateinit var activity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDefaultLanguageSelectPageBinding.inflate(inflater, container, false)


        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = LanguageListAdapter()
        val dataSet = listOf(LanguageModel("Korean", R.mipmap.korean), LanguageModel("English",
            R.mipmap.american
        ))

        binding.recyclerView.adapter = adapter
        adapter.submitList(dataSet.toMutableList())
        adapter.setItemClickListener(object: LanguageListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                activity.defaultLanguage = adapter.currentList[position].countryName
                findNavController().navigate(R.id.learningLanguageSelectFragment)
            }

        })
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as MainActivity
    }
}