package com.vodang.greenmind

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import com.vodang.greenmind.api.auth.RegisterEmailRequest
import com.vodang.greenmind.api.auth.registerWithEmail
import com.vodang.greenmind.api.cities.getCitiesByCountry
import com.vodang.greenmind.api.restcountries.CountryDto
import com.vodang.greenmind.api.restcountries.getAllCountries
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.SettingsStore
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onCancel: () -> Unit) {
    val s = LocalAppStrings.current
    var fullName     by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var confirm      by remember { mutableStateOf("") }
    var road         by remember { mutableStateOf("") }
    var city         by remember { mutableStateOf("") }
    var gender       by remember { mutableStateOf("other") }

    // Country picker state
    var selectedCountry   by remember { mutableStateOf<CountryDto?>(null) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var countrySearch     by remember { mutableStateOf("") }
    var countries         by remember { mutableStateOf<List<CountryDto>>(emptyList()) }
    var countriesLoading  by remember { mutableStateOf(false) }

    // City picker state
    var showCityPicker  by remember { mutableStateOf(false) }
    var citySearch      by remember { mutableStateOf("") }
    var citiesForCountry by remember { mutableStateOf<List<String>>(emptyList()) }
    var citiesLoading   by remember { mutableStateOf(false) }

    var acceptTerms     by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }
    var isLoading    by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentStep  by remember { mutableStateOf(1) }
    val scope = rememberCoroutineScope()

    val green800 = Color(0xFF2E7D32)
    val green500 = Color(0xFF4CAF50)

    val fieldColors = OutlinedTextFieldDefaults.colors(focusedBorderColor = green800, focusedLabelColor = green800, cursorColor = green800)
    val fieldShape = RoundedCornerShape(12.dp)

    val step1Valid = email.isNotBlank()
    val step2Valid = password.isNotEmpty() && password == confirm
    val step3Valid = acceptTerms

    // Load countries when step 3 first opens
    LaunchedEffect(currentStep) {
        if (currentStep == 3 && countries.isEmpty()) {
            countriesLoading = true
            countries = try { getAllCountries() } catch (_: Throwable) { emptyList() }
            countriesLoading = false
        }
    }

    // Load cities whenever the selected country changes
    LaunchedEffect(selectedCountry) {
        val countryName = selectedCountry?.name?.common ?: return@LaunchedEffect
        city = ""
        citiesForCountry = emptyList()
        citiesLoading = true
        citiesForCountry = try { getCitiesByCountry(countryName) } catch (_: Throwable) { emptyList() }
        citiesLoading = false
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFF1F8E9))))) {
        Column(
            modifier = Modifier.fillMaxSize().safeContentPadding().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            Box(modifier = Modifier.size(72.dp).background(green800, shape = CircleShape), contentAlignment = Alignment.Center) {
                Text("🌱", fontSize = 32.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(s.appName, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = green800)
            Text(s.createAccount, fontSize = 13.sp, color = Color(0xFF66BB6A))

            Spacer(Modifier.height(20.dp))

            // Step indicator
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                val stepLabels = listOf(s.stepInfo, s.stepPassword, s.stepAddress)
                stepLabels.forEachIndexed { index, label ->
                    val step = index + 1
                    val isActive   = step == currentStep
                    val isComplete = step < currentStep
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(32.dp).background(
                                color = when { isActive -> green800; isComplete -> green500; else -> Color(0xFFBDBDBD) },
                                shape = CircleShape
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (isComplete) "✓" else "$step", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(label, fontSize = 11.sp, color = if (isActive) green800 else Color(0xFF9E9E9E), fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal)
                    }
                    if (index < stepLabels.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.width(40.dp).padding(bottom = 18.dp),
                            color = if (step < currentStep) green500 else Color(0xFFBDBDBD),
                            thickness = 2.dp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    val stepTitle = when (currentStep) { 1 -> s.yourInformation; 2 -> s.setPassword; else -> s.addressAndTerms }
                    Text(stepTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))

                    when (currentStep) {
                        1 -> {
                            OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text(s.fullName) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = fieldShape, colors = fieldColors, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
                            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(s.emailAddress) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = fieldShape, colors = fieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done))
                            Text(s.gender, fontSize = 13.sp, color = Color(0xFF757575))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val options = listOf("male" to s.male, "female" to s.female, "other" to s.other)
                                options.forEach { (value, label) ->
                                    val selected = gender == value
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selected) Color(0xFFE8F5E9) else Color.White,
                                        border = if (selected) null else BorderStroke(1.dp, Color(0xFFDDDDDD)),
                                        modifier = Modifier.clickable { gender = value }
                                    ) {
                                        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = if (selected) green800 else Color(0xFF424242))
                                    }
                                }
                            }
                            Button(onClick = { if (step1Valid) currentStep = 2 }, enabled = step1Valid, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = green800)) {
                                Text(s.next, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                        }
                        2 -> {
                            OutlinedTextField(
                                value = password, onValueChange = { password = it }, label = { Text(s.password) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = fieldShape, colors = fieldColors,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                                trailingIcon = { TextButton(onClick = { passwordVisible = !passwordVisible }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text(if (passwordVisible) s.hide else s.show, fontSize = 12.sp, color = green500) } }
                            )
                            OutlinedTextField(
                                value = confirm, onValueChange = { confirm = it }, label = { Text(s.confirmPassword) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = fieldShape,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = if (confirm.isNotEmpty() && confirm != password) Color.Red else green800, focusedLabelColor = green800, cursorColor = green800),
                                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                trailingIcon = { TextButton(onClick = { confirmVisible = !confirmVisible }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text(if (confirmVisible) s.hide else s.show, fontSize = 12.sp, color = green500) } },
                                supportingText = if (confirm.isNotEmpty() && confirm != password) { { Text(s.passwordMismatch, color = Color.Red, fontSize = 11.sp) } } else null
                            )
                            // Fix: no letterSpacing so "Tiếp theo" fits in the half-width button
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { currentStep = 1 }, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, green800), colors = ButtonDefaults.outlinedButtonColors(contentColor = green800)) {
                                    Text(s.back, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Button(onClick = { if (step2Valid) currentStep = 3 }, enabled = step2Valid, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = green800)) {
                                    Text(s.next, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        3 -> {
                            Text(s.address, fontSize = 13.sp, color = Color(0xFF757575))
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {

                                // Road
                                OutlinedTextField(
                                    value = road,
                                    onValueChange = { road = it },
                                    label = { Text(s.road) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = fieldShape,
                                    colors = fieldColors,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                )

                                // Country selector
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = selectedCountry?.let { "${it.flag}  ${it.name.common}" } ?: "",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text(s.country) },
                                        placeholder = { Text(s.selectCountry, color = Color.Gray) },
                                        trailingIcon = {
                                            if (countriesLoading) {
                                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = green800)
                                            } else {
                                                Text("▼", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(end = 4.dp))
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = fieldShape,
                                        colors = fieldColors,
                                    )
                                    Spacer(modifier = Modifier.matchParentSize().clickable(enabled = !countriesLoading) { showCountryPicker = true })
                                }

                                // City selector
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = city,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text(s.city) },
                                        placeholder = { Text(s.selectCity, color = Color.Gray) },
                                        trailingIcon = {
                                            if (citiesLoading) {
                                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = green800)
                                            } else {
                                                Text("▼", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(end = 4.dp))
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = fieldShape,
                                        colors = fieldColors,
                                    )
                                    Spacer(
                                        modifier = Modifier.matchParentSize().clickable(
                                            enabled = selectedCountry != null && !citiesLoading
                                        ) { showCityPicker = true }
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { acceptTerms = !acceptTerms }.padding(vertical = 4.dp)) {
                                Checkbox(checked = acceptTerms, onCheckedChange = { acceptTerms = it }, modifier = Modifier.size(20.dp), colors = CheckboxDefaults.colors(checkedColor = green800, checkmarkColor = Color.White, uncheckedColor = Color(0xFF9E9E9E)))
                                Spacer(Modifier.width(8.dp))
                                Text(s.acceptTerms, fontSize = 13.sp, color = Color(0xFF424242))
                            }
                            if (errorMessage != null) Text(errorMessage!!, color = Color.Red, fontSize = 12.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { currentStep = 2 }, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, green800), colors = ButtonDefaults.outlinedButtonColors(contentColor = green800)) {
                                    Text(s.back, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Button(
                                    onClick = {
                                        errorMessage = null
                                        if (!step3Valid) return@Button
                                        scope.launch {
                                            isLoading = true
                                            try {
                                                val countryName = selectedCountry?.name?.common
                                                val req = RegisterEmailRequest(
                                                    email = email,
                                                    password = password,
                                                    confirmPassword = confirm,
                                                    fullName = fullName.ifBlank { "User" },
                                                    dateOfBirth = "2000-01-01",
                                                    location = listOfNotNull(road.ifBlank { null }, city.ifBlank { null }, countryName).joinToString(", "),
                                                    gender = gender,
                                                    region = countryName ?: city.ifBlank { "unknown" },
                                                )
                                                val resp = registerWithEmail(req)
                                                SettingsStore.setAccessToken(resp.accessToken)
                                                SettingsStore.setRefreshToken(resp.refreshToken)
                                                SettingsStore.setUser(resp.user)
                                                onRegisterSuccess()
                                            } catch (t: Throwable) {
                                                errorMessage = t.message ?: "Unknown error"
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    },
                                    enabled = step3Valid && !isLoading,
                                    modifier = Modifier.weight(1f).height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = green800)
                                ) {
                                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    else Text(s.register, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFBDBDBD))
                Text(s.or, fontSize = 13.sp, color = Color(0xFF9E9E9E))
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFBDBDBD))
            }
            Spacer(Modifier.height(14.dp))
            OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFDBDBDB)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3C4043))) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text("G", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
                    Spacer(Modifier.width(12.dp))
                    Text(s.continueWithGoogle, fontSize = 15.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(s.alreadyHaveAccount, fontSize = 14.sp, color = Color(0xFF757575))
                TextButton(onClick = onCancel) { Text(s.signIn, fontSize = 14.sp, color = green800, fontWeight = FontWeight.SemiBold) }
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    // ── Country picker bottom sheet ───────────────────────────────────────────
    if (showCountryPicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCountryPicker = false; countrySearch = "" },
            sheetState = sheetState,
            containerColor = Color.White,
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(s.selectCountry, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), modifier = Modifier.padding(bottom = 12.dp))
                OutlinedTextField(
                    value = countrySearch,
                    onValueChange = { countrySearch = it },
                    placeholder = { Text(s.searchCountry, color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32), focusedLabelColor = Color(0xFF2E7D32), cursorColor = Color(0xFF2E7D32)),
                    leadingIcon = { Text("🔍", fontSize = 16.sp) },
                )
                Spacer(Modifier.height(8.dp))
            }
            val filteredCountries = countries.filter { countrySearch.isBlank() || it.name.common.contains(countrySearch, ignoreCase = true) }
            LazyColumn(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            ) {
                items(filteredCountries, key = { it.cca2 }) { country ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable {
                                selectedCountry = country
                                showCountryPicker = false
                                countrySearch = ""
                            }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(country.flag.ifBlank { "🏳" }, fontSize = 24.sp)
                        Text(country.name.common, fontSize = 15.sp, color = Color(0xFF212121), modifier = Modifier.weight(1f))
                        if (selectedCountry?.cca2 == country.cca2) {
                            Text("✓", fontSize = 16.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                }
            }
        }
    }

    // ── City picker bottom sheet ──────────────────────────────────────────────
    if (showCityPicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCityPicker = false; citySearch = "" },
            sheetState = sheetState,
            containerColor = Color.White,
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(s.selectCity, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), modifier = Modifier.padding(bottom = 12.dp))
                OutlinedTextField(
                    value = citySearch,
                    onValueChange = { citySearch = it },
                    placeholder = { Text(s.searchCity, color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32), focusedLabelColor = Color(0xFF2E7D32), cursorColor = Color(0xFF2E7D32)),
                    leadingIcon = { Text("🔍", fontSize = 16.sp) },
                )
                Spacer(Modifier.height(8.dp))
            }
            val filteredCities = citiesForCountry.filter { citySearch.isBlank() || it.contains(citySearch, ignoreCase = true) }
            if (filteredCities.isEmpty() && !citiesLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text(citySearch.ifBlank { s.selectCountry }, fontSize = 14.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    items(filteredCities, key = { it }) { cityName ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable {
                                    city = cityName
                                    showCityPicker = false
                                    citySearch = ""
                                }
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text("📍", fontSize = 18.sp)
                            Text(cityName, fontSize = 15.sp, color = Color(0xFF212121), modifier = Modifier.weight(1f))
                            if (city == cityName) {
                                Text("✓", fontSize = 16.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}
