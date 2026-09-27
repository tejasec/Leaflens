package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smartagriculture.R
import com.example.smartagriculture.compose.LoginScreen

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LoginScreen(
                    onLoginSuccess = {
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    },
                    onRegisterClick = {
                        findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
                    }
                )
            }
        }
    }
}