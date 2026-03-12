package com.vodang.greenmind

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import com.vodang.greenmind.accounts.Account
import com.vodang.greenmind.accounts.AccountsRepository
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onNavigateToRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val green800 = Color(0xFF2E7D32)
    val green500 = Color(0xFF4CAF50)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFF1F8E9)))
            )
            .safeContentPadding()
    ) {
        val maxH = maxHeight
        val smallScreen = maxH < 640.dp
        val logoSize = if (smallScreen) 72.dp else 88.dp

        val scope = rememberCoroutineScope()

        var showAccountsDialog by remember { mutableStateOf(false) }
        var showAddDialog by remember { mutableStateOf(false) }
        var addEmail by rememberSaveable { mutableStateOf("") }
        var addPassword by rememberSaveable { mutableStateOf("") }

        val accounts by AccountsRepository.accountsFlow.collectAsState()

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top: logo + title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(logoSize)
                        .background(green800, shape = CircleShape)
                        .clickable { showAccountsDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌱", fontSize = (if (smallScreen) 32.sp else 40.sp))
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "GreenMind",
                    fontSize = if (smallScreen) 22.sp else 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = green800
                )
                Text(
                    text = "Smart Environmental System",
                    fontSize = 12.sp,
                    color = Color(0xFF66BB6A)
                )
            }

            // Middle: input card (constrained height so it never expands beyond screen)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .heightIn(max = maxH * 0.58f),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Sign In",
                        fontSize = if (smallScreen) 18.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("you@example.com") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = green800,
                            focusedLabelColor = green800,
                            cursorColor = green800
                        )
                    )

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        trailingIcon = {
                            TextButton(
                                onClick = { passwordVisible = !passwordVisible },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = if (passwordVisible) "Hide" else "Show",
                                    fontSize = 12.sp,
                                    color = green500
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = green800,
                            focusedLabelColor = green800,
                            cursorColor = green800
                        )
                    )

                    // Remember me (compact) and Forgot password (centered)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { rememberMe = !rememberMe }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                modifier = Modifier.size(20.dp),
                                colors = CheckboxDefaults.colors(
                                    checkedColor = green800,
                                    checkmarkColor = Color.White,
                                    uncheckedColor = Color(0xFF9E9E9E)
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Remember me",
                                fontSize = 14.sp,
                                color = Color(0xFF424242),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(onClick = { /* TODO: forgot password */ }) {
                                Text(
                                    "Forgot password?",
                                    fontSize = 13.sp,
                                    color = green500,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Login button
                    Button(
                        onClick = onLoginSuccess,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = green800)
                    ) {
                        Text(
                            text = "LOGIN",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Bottom: social sign-in + sign up
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFBDBDBD))
                    Text("  OR  ", fontSize = 13.sp, color = Color(0xFF9E9E9E))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFBDBDBD))
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { /* TODO: Google sign-in integration */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFDBDBDB)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3C4043))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("G", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
                        Spacer(Modifier.width(12.dp))
                        Text("Continue with Google", fontSize = 15.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Don't have an account? ",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                    TextButton(onClick = { onNavigateToRegister() }) {
                        Text(
                            text = "Sign up",
                            fontSize = 14.sp,
                            color = green800,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        if (showAccountsDialog) {
            AlertDialog(
                onDismissRequest = { showAccountsDialog = false },
                title = { Text("Choose account") },
                text = {
                    Column {
                        if (accounts.isEmpty()) Text("No saved accounts")
                        accounts.forEach { a ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(a.email, modifier = Modifier.weight(1f))
                                TextButton(onClick = {
                                    email = a.email
                                    password = a.password
                                    showAccountsDialog = false
                                }) { Text("Select") }
                                TextButton(onClick = {
                                    scope.launch { AccountsRepository.removeAccount(a) }
                                }) { Text("Delete") }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            TextButton(onClick = { showAddDialog = true }) { Text("Add account") }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAccountsDialog = false }) { Text("Close") }
                }
            )
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add account") },
                text = {
                    Column {
                        OutlinedTextField(value = addEmail, onValueChange = { addEmail = it }, label = { Text("Email") })
                        OutlinedTextField(value = addPassword, onValueChange = { addPassword = it }, label = { Text("Password") })
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val a = Account(addEmail.trim(), addPassword)
                        scope.launch {
                            AccountsRepository.addAccount(a)
                        }
                        // fill fields and close
                        email = a.email
                        password = a.password
                        addEmail = ""
                        addPassword = ""
                        showAddDialog = false
                        showAccountsDialog = false
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
