package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizedStrings
import com.example.core.preferences.ThemeMode
import com.example.data.AuthRepository
import com.example.data.AuthResult
import com.example.model.UserRole
import com.example.model.UserSession
import com.example.ui.components.EducoreHeaderBar
import com.example.ui.components.EducoreLogoView
import com.example.ui.components.EducorePrimaryButton
import com.example.ui.components.EducoreTextField
import com.example.ui.theme.EducoreError
import com.example.ui.theme.EducoreSuccess
import kotlinx.coroutines.launch

@Composable
fun StudentLoginScreen(
    language: AppLanguage,
    themeMode: ThemeMode,
    authRepository: AuthRepository,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: (UserSession) -> Unit,
    onNeedsEmailVerification: (String) -> Unit
) {
    val strings = LocalizedStrings.get(language)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var email by remember { mutableStateOf("ahmed.student@educore.edu") }
    var password by remember { mutableStateOf("123456") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EducoreHeaderBar(
            language = language,
            themeMode = themeMode,
            onToggleLanguage = onToggleLanguage,
            onToggleTheme = onToggleTheme,
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EducoreLogoView(
                size = 72,
                showTitle = true,
                showSlogan = false,
                language = language
            )

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.loginAsStudent,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            EducoreTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = strings.email,
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                testTag = "student_email_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            EducoreTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = strings.password,
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                testTag = "student_password_input"
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = EducoreError,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text(
                        text = strings.forgotPassword,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                EducorePrimaryButton(
                    text = strings.login,
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = strings.errorEmptyFields
                            return@EducorePrimaryButton
                        }
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val result = authRepository.login(email, password, UserRole.STUDENT)
                            isLoading = false
                            when (result) {
                                is AuthResult.Success -> onLoginSuccess(result.session)
                                is AuthResult.NeedsEmailVerification -> onNeedsEmailVerification(result.email)
                                is AuthResult.Error -> {
                                    errorMessage = if (language == AppLanguage.ARABIC) result.messageAr else result.messageEn
                                }
                            }
                        }
                    },
                    testTag = "student_login_button"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = strings.dontHaveAccount,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun StudentRegisterScreen(
    language: AppLanguage,
    themeMode: ThemeMode,
    authRepository: AuthRepository,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (UserSession) -> Unit
) {
    val strings = LocalizedStrings.get(language)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf("") }
    
    // Dynamic teacher codes list
    val teacherCodes = remember { mutableStateListOf("mahmoud123") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EducoreHeaderBar(
            language = language,
            themeMode = themeMode,
            onToggleLanguage = onToggleLanguage,
            onToggleTheme = onToggleTheme,
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = strings.registerAsStudent,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            EducoreTextField(
                value = fullName,
                onValueChange = { fullName = it; errorMessage = null },
                label = strings.fullName,
                leadingIcon = Icons.Default.Person,
                testTag = "student_name_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            EducoreTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = strings.email,
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                testTag = "student_reg_email_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            EducoreTextField(
                value = phone,
                onValueChange = { phone = it; errorMessage = null },
                label = strings.phoneNumber,
                leadingIcon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                testTag = "student_phone_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            EducoreTextField(
                value = parentPhone,
                onValueChange = { parentPhone = it; errorMessage = null },
                label = strings.parentPhoneNumber,
                leadingIcon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                testTag = "student_parent_phone_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            EducoreTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = strings.password,
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                testTag = "student_reg_pass_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            EducoreTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = null },
                label = strings.confirmPassword,
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                testTag = "student_reg_confirm_pass_input"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Teacher Codes Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = strings.teacherCodesTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = strings.teacherCodeExample,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    teacherCodes.forEachIndexed { index, code ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EducoreTextField(
                                value = code,
                                onValueChange = { teacherCodes[index] = it },
                                label = "${strings.teacherCode} ${index + 1}",
                                leadingIcon = Icons.Default.School,
                                modifier = Modifier.weight(1f),
                                testTag = "teacher_code_input_$index"
                            )

                            if (teacherCodes.size > 1) {
                                IconButton(
                                    onClick = { teacherCodes.removeAt(index) },
                                    modifier = Modifier.padding(top = 24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = strings.removeTeacherCode,
                                        tint = EducoreError
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(
                        onClick = { teacherCodes.add("") },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = strings.addTeacherCode,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = EducoreError,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                EducorePrimaryButton(
                    text = strings.register,
                    onClick = {
                        if (fullName.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
                            errorMessage = strings.errorEmptyFields
                            return@EducorePrimaryButton
                        }
                        if (password.length < 6) {
                            errorMessage = strings.errorShortPassword
                            return@EducorePrimaryButton
                        }
                        if (password != confirmPassword) {
                            errorMessage = strings.errorPasswordMismatch
                            return@EducorePrimaryButton
                        }
                        if (teacherCodes.none { it.isNotBlank() }) {
                            errorMessage = strings.errorTeacherCodeRequired
                            return@EducorePrimaryButton
                        }

                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val result = authRepository.registerStudent(
                                fullName = fullName,
                                email = email,
                                password = password,
                                phone = phone,
                                parentPhone = parentPhone,
                                teacherCodes = teacherCodes.toList()
                            )
                            isLoading = false
                            when (result) {
                                is AuthResult.Success -> onRegisterSuccess(result.session)
                                is AuthResult.NeedsEmailVerification -> onRegisterSuccess(UserSession(
                                    id = "temp",
                                    fullName = fullName,
                                    email = email,
                                    phoneNumber = phone,
                                    role = UserRole.STUDENT,
                                    isEmailVerified = false
                                ))
                                is AuthResult.Error -> {
                                    errorMessage = if (language == AppLanguage.ARABIC) result.messageAr else result.messageEn
                                }
                            }
                        }
                    },
                    testTag = "student_submit_register_button"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = strings.alreadyHaveAccount,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
