package com.vodang.greenmind

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onCancel: () -> Unit) {
    var fullName     by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var confirm      by remember { mutableStateOf("") }
    var road         by remember { mutableStateOf("") }
    var city         by remember { mutableStateOf("") }
    
    var acceptTerms  by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }

    val green800 = Color(0xFF2E7D32)
    val green500 = Color(0xFF4CAF50)

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = green800,
        focusedLabelColor  = green800,
        cursorColor        = green800
    )
    val fieldShape = RoundedCornerShape(12.dp)

    val isValid = acceptTerms
            && email.isNotBlank()
            && password.isNotEmpty()
            && password == confirm

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFF1F8E9))))
    ) {
            Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // ── Logo ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(green800, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🌱", fontSize = 32.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "GreenMind",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = green800
            )
            Text(
                text = "Create your account",
                fontSize = 13.sp,
                color = Color(0xFF66BB6A)
            )

            Spacer(Modifier.height(28.dp))

            // ── Form card ────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Sign Up",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )

                    // Full name
                    OutlinedTextField(
                        value = fullName, onValueChange = { fullName = it },
                        label = { Text("Full name") },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape, colors = fieldColors,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    // Email
                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("Email Address") },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape, colors = fieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                    )

                    // Password
                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape, colors = fieldColors,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        trailingIcon = {
                            TextButton(onClick = { passwordVisible = !passwordVisible }, contentPadding = PaddingValues(horizontal = 8.dp)) {
                                Text(if (passwordVisible) "Hide" else "Show", fontSize = 12.sp, color = green500)
                            }
                        }
                    )

                    // Confirm password
                    OutlinedTextField(
                        value = confirm, onValueChange = { confirm = it },
                        label = { Text("Confirm password") },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (confirm.isNotEmpty() && confirm != password) Color.Red else green800,
                            focusedLabelColor  = green800,
                            cursorColor        = green800
                        ),
                        visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        trailingIcon = {
                            TextButton(onClick = { confirmVisible = !confirmVisible }, contentPadding = PaddingValues(horizontal = 8.dp)) {
                                Text(if (confirmVisible) "Hide" else "Show", fontSize = 12.sp, color = green500)
                            }
                        },
                        supportingText = if (confirm.isNotEmpty() && confirm != password) {
                            { Text("Passwords do not match", color = Color.Red, fontSize = 11.sp) }
                        } else null
                    )

                    // Address
                    Text("Address", fontSize = 13.sp, color = Color(0xFF757575))
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = road, onValueChange = { road = it },
                            label = { Text("Road") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth(), shape = fieldShape, colors = fieldColors
                        )
                        OutlinedTextField(
                            value = city, onValueChange = { city = it },
                            label = { Text("City") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth(), shape = fieldShape, colors = fieldColors
                        )
                    }

                    

                    // ── Accept terms ──────────────────────────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { acceptTerms = !acceptTerms }.padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = acceptTerms,
                            onCheckedChange = { acceptTerms = it },
                            modifier = Modifier.size(20.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = green800,
                                checkmarkColor = Color.White,
                                uncheckedColor = Color(0xFF9E9E9E)
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("I accept the terms and conditions", fontSize = 13.sp, color = Color(0xFF424242))
                    }

                    // ── Register button ───────────────────────────
                    Button(
                        onClick = onRegisterSuccess,
                        enabled = isValid,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = green800)
                    ) {
                        Text("REGISTER", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── OR divider ─────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFBDBDBD))
                Text("  OR  ", fontSize = 13.sp, color = Color(0xFF9E9E9E))
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFBDBDBD))
            }

            Spacer(Modifier.height(14.dp))

            // ── Google sign-up ─────────────────────────────────────
            OutlinedButton(
                onClick = { /* TODO: Google sign-up */ },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFDBDBDB)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3C4043))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text("G", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
                    Spacer(Modifier.width(12.dp))
                    Text("Continue with Google", fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Already have account ───────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text("Already have an account? ", fontSize = 14.sp, color = Color(0xFF757575))
                TextButton(onClick = onCancel) {
                    Text("Sign in", fontSize = 14.sp, color = green800, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
