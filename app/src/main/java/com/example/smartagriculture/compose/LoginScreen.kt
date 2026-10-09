package com.example.smartagriculture.compose

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.smartagriculture.compose.components.*
import com.example.smartagriculture.repository.UserRepository
import com.example.smartagriculture.utils.SecurityUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userRepo = remember { UserRepository.getInstance(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var showForgotDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White, CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = com.example.smartagriculture.R.drawable.login_image),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Welcome Back!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            
            Text(
                text = "Login to continue to your account",
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 8.dp)
            )
        
            Spacer(modifier = Modifier.height(32.dp))

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
                label = "Password",
                icon = Icons.Default.Lock,
                isPassword = true
            )
            
            Text(
                text = "Forgot Password?",
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 24.dp, top = 8.dp, bottom = 32.dp)
                    .clickable { showForgotDialog = true }
            )

            AuthButton(
                text = "Login",
                isLoading = isLoading,
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@AuthButton
                    }

                    val (isLocked, remainingSec) = userRepo.isLockedOut()
                    if (isLocked) {
                        Toast.makeText(context, "Too many failed attempts. Try again in $remainingSec seconds.", Toast.LENGTH_SHORT).show()
                        return@AuthButton
                    }

                    isLoading = true
                    coroutineScope.launch {
                        // 1. Try local salted verification first
                        val localResult = userRepo.login(email.trim(), password)
                        if (localResult.isSuccess) {
                            isLoading = false
                            val prefs = context.getSharedPreferences("smart_agri_prefs", android.content.Context.MODE_PRIVATE)
                            prefs.edit()
                                .putString("user_email", email.trim())
                                .putString("user_name", localResult.getOrNull()?.name ?: "Farmer")
                                .putBoolean("is_logged_in", true)
                                .apply()

                            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                            return@launch
                        }

                        // 2. Try Firebase Auth (if registered remotely)
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email.trim(), password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    userRepo.resetFailedAttempts()
                                    val prefs = context.getSharedPreferences("smart_agri_prefs", android.content.Context.MODE_PRIVATE)
                                    prefs.edit()
                                        .putString("user_email", email.trim())
                                        .putBoolean("is_logged_in", true)
                                        .apply()

                                    Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                } else {
                                    userRepo.recordFailedAttempt()
                                    Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier.padding(vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Don't have an account? ", 
                    color = Color.DarkGray, 
                    fontSize = 15.sp
                )
                Text(
                    text = "Sign Up",
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.clickable { onRegisterClick() }
                )
            }
        }

        // Hint-based Forgot Password Dialog
        if (showForgotDialog) {
            HintBasedForgotPasswordDialog(
                userRepo = userRepo,
                onDismiss = { showForgotDialog = false }
            )
        }
    }
}

/**
 * Hint-based password reset dialog.
 * Drives password recovery solely through the saved security hint and answer (NO email, SMS, or OTP).
 */
@Composable
fun HintBasedForgotPasswordDialog(
    userRepo: UserRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var resetEmail by remember { mutableStateOf("") }
    var hintQuestion by remember { mutableStateOf<String?>(null) }
    var hintAnswer by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var isCheckingHint by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Reset Password",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recover access using your saved security hint.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(
                    value = resetEmail,
                    onValueChange = { resetEmail = it },
                    label = "Account Email",
                    icon = Icons.Default.Email
                )

                if (hintQuestion == null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (resetEmail.isBlank()) {
                                Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isCheckingHint = true
                            statusMessage = null
                            coroutineScope.launch {
                                val question = userRepo.getHintQuestion(resetEmail.trim())
                                isCheckingHint = false
                                if (question != null) {
                                    hintQuestion = question
                                } else {
                                    // Prevent account enumeration by showing a neutral response
                                    statusMessage = "If this account is registered with a security hint, it will appear here."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isCheckingHint) "Checking..." else "Find Security Hint")
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Security Question:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = hintQuestion ?: "",
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    AuthTextField(
                        value = hintAnswer,
                        onValueChange = { hintAnswer = it },
                        label = "Security Answer",
                        icon = Icons.Default.Info
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    AuthTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = "New Password (min 8 chars)",
                        icon = Icons.Default.Lock,
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    AuthTextField(
                        value = confirmNewPassword,
                        onValueChange = { confirmNewPassword = it },
                        label = "Confirm New Password",
                        icon = Icons.Default.Lock,
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (hintAnswer.isBlank() || newPassword.isBlank()) {
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (newPassword != confirmNewPassword) {
                                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val (isValid, err) = SecurityUtils.validatePasswordPolicy(newPassword)
                            if (!isValid) {
                                Toast.makeText(context, err ?: "Password policy error", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            coroutineScope.launch {
                                val resetRes = userRepo.resetPasswordWithHint(
                                    email = resetEmail.trim(),
                                    hintAnswer = hintAnswer.trim(),
                                    newPassword = newPassword
                                )

                                if (resetRes.isSuccess) {
                                    Toast.makeText(context, "Password reset successfully! Please log in.", Toast.LENGTH_LONG).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, resetRes.exceptionOrNull()?.message ?: "Reset failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update Password")
                    }
                }

                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = statusMessage ?: "",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        }
    }
}
