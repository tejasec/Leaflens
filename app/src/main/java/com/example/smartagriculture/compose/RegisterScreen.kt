package com.example.smartagriculture.compose

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartagriculture.compose.components.*
import com.example.smartagriculture.repository.UserRepository
import com.example.smartagriculture.utils.SecurityUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var hintQuestion by remember { mutableStateOf("") }
    var hintAnswer by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val userRepo = remember { UserRepository.getInstance(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthHeader(
            title = "Create Account",
            subtitle = "Sign up to get started",
            imageRes = com.example.smartagriculture.R.drawable.app_logo
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = name,
            onValueChange = { name = it },
            label = "Full Name",
            icon = Icons.Default.Person
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            icon = Icons.Default.Email
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password (min 8 chars)",
            icon = Icons.Default.Lock,
            isPassword = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AuthTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm Password",
            icon = Icons.Default.Lock,
            isPassword = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = hintQuestion,
            onValueChange = { hintQuestion = it },
            label = "Security Hint Question (e.g. Favorite Crop)",
            icon = Icons.Default.Info
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = hintAnswer,
            onValueChange = { hintAnswer = it },
            label = "Security Hint Answer",
            icon = Icons.Default.Lock,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        AuthButton(
            text = "Create Account",
            isLoading = isLoading,
            onClick = {
                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                    hintQuestion.isEmpty() || hintAnswer.isEmpty()
                ) {
                    Toast.makeText(context, "Please fill all fields including security hint", Toast.LENGTH_SHORT).show()
                    return@AuthButton
                }

                if (password != confirmPassword) {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@AuthButton
                }

                val (isValidPass, passErr) = SecurityUtils.validatePasswordPolicy(password)
                if (!isValidPass) {
                    Toast.makeText(context, passErr ?: "Password policy violated", Toast.LENGTH_SHORT).show()
                    return@AuthButton
                }

                if (!SecurityUtils.validateHintDoesNotExposePassword(hintQuestion, password) ||
                    !SecurityUtils.validateHintDoesNotExposePassword(hintAnswer, password)
                ) {
                    Toast.makeText(context, "Hint question or answer cannot contain the password", Toast.LENGTH_SHORT).show()
                    return@AuthButton
                }

                isLoading = true
                coroutineScope.launch {
                    val regResult = userRepo.registerUser(
                        name = name.trim(),
                        email = email.trim(),
                        password = password,
                        hintQuestion = hintQuestion.trim(),
                        hintAnswer = hintAnswer.trim()
                    )

                    if (regResult.isFailure) {
                        isLoading = false
                        Toast.makeText(context, regResult.exceptionOrNull()?.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // Save session details
                    val prefs = context.getSharedPreferences("smart_agri_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("user_name", name.trim())
                        .putString("user_email", email.trim())
                        .apply()

                    // Optional Firebase Auth sync
                    try {
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.trim(), password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                                onRegisterSuccess()
                            }
                    } catch (t: Throwable) {
                        isLoading = false
                        Toast.makeText(context, "Registration successful locally!", Toast.LENGTH_SHORT).show()
                        onRegisterSuccess()
                    }
                }
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account? ", color = Color.DarkGray, fontSize = 14.sp)
            Text(
                text = "Login",
                color = PurpleEnd,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onLoginClick() }
            )
        }
    }
}
