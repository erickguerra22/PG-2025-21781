package com.eguerra.ciudadanodigital.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.eguerra.ciudadanodigital.R
import com.eguerra.ciudadanodigital.databinding.ActivityUnloggedBinding
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class UnloggedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUnloggedBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUnloggedBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        configureNavigation()
    }


    private fun configureNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.unloggedActivity_fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController
    }
}