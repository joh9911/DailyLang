package com.example.dailylang.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailylang.Adapter.LibraryListAdapter
import com.example.dailylang.SecondActivity
import com.example.dailylang.databinding.FragmentLibraryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryFragment: Fragment() {
    var _binding: FragmentLibraryBinding? = null
    val binding get() = _binding!!
    lateinit var adapter: LibraryListAdapter
    lateinit var activity: SecondActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        adapter = LibraryListAdapter()
        binding.recyclerView.adapter = adapter
        CoroutineScope(Dispatchers.IO).launch {
            val list = activity.projectDao.getDailyExpressionsForProject(activity.uid!!)
            withContext(Dispatchers.Main){
                adapter.submitList(list.toMutableList())
            }

        }
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as SecondActivity
    }
}