package com.example.dailylang

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailylang.database.AppDatabase
import com.example.dailylang.database.ProjectDao
import com.example.dailylang.databinding.ActivityMainBinding

class MainActivity: FragmentActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    lateinit var database: AppDatabase
    lateinit var projectDao: ProjectDao

    var defaultLanguage: String? = null
    var learningLanguage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        initDb()
        setContentView(view)

    }

    fun initDb(){
        database = AppDatabase.getDatabase(this)
        projectDao = database.projectDao()
    }
}