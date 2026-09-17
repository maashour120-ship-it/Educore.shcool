package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.launch

@Composable
fun TeacherLoginScreen(
    language: AppLanguage,
    themeMode: ThemeMode,
    authRepository: AuthRepository,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: (UserSession) -> Unit
) {
    val strings = LocalizedStrings.get(language)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var selectedRole by remember { mutableStateOf(UserRole.TEACHER) }
    var email by remember { mutableStateOf("ahmed.teacher@educore.edu") }
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

            Spacer(modifier = Modifier.height(16.dp))

            // Role Toggle (Teacher / Supervisor)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                // Teacher Option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedRole == UserRole.TEACHER) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedRole = UserRole.TEACHER; email = "ahmed.teacher@educore.edu" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.teacher,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (selectedRole == UserRole.TEACHER) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Supervisor Option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedRole == UserRole.SUPERVISOR) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedRole = UserRole.SUPERVISOR; email = "mostafa.super@educore.edu" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.supervisor,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (selectedRole == UserRole.SUPERVISOR) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            EducoreTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = strings.email,
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                testTag = "teacher_email_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            EducoreTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = strings.password,
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                testTag = "teacher_password_input"
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
                            val result = authRepository.login(email, password, selectedRole)
                            isLoading = false
                            when (result) {
                                is AuthResult.Success -> onLoginSuccess(result.session)
                                is AuthResult.NeedsEmailVerification -> {
                                    errorMessage = strings.errorEmailNotVerified
                                }
                                is AuthResult.Error -> {
                                    errorMessage = if (language == AppLanguage.ARABIC) result.messageAr else result.messageEn
                                }
                            }
                        }
                    },
                    testTag = "teacher_login_button"
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
fun TeacherRegisterScreen(
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

    var selectedRole by remember { mutableStateOf(UserRole.TEACHER) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var customCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
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
                text = if (selectedRole == UserRole.TEACHER) strings.registerAsTeacher else strings.registerAsSupervisor,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Role Picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedRole == UserRole.TEACHER) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedRole = UserRole.TEACHER }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.teacher,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (selectedRole == UserRole.TEACHER) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedRole == UserRole.SUPERVISOR) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedRole = UserRole.SUPERVISOR }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.supervisor,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (selectedRole == UserRole.SUPERVISOR) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            EducoreTextField(
                value = fullName,
                onValueChange = { fullName = it; errorMessage = null },
                label = strings.fullName,
                leadingIcon = Icons.Default.Person,
                testTag = "teacher_name_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            EducoreTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = strings.email,
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                testTag = "teacher_reg_email_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            EducoreTextField(
                value = phone,
                onValueChange = { phone = it; errorMessage = null },
                label = strings.phoneNumber,
                leadingIcon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                testTag = "teacher_phone_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Custom Unique Code
            EducoreTextField(
                value = customCode,
                onValueChange = { customCode = it; errorMessage = null },
                label = strings.myCustomCode,
                leadingIcon = Icons.Default.Key,
                testTag = "teacher_custom_code_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            EducoreTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = strings.password,
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                testTag = "teacher_reg_pass_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            EducoreTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = null },
                label = strings.confirmPassword,
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                testTag = "teacher_reg_confirm_pass_input"
            )

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
                        if (fullName.isBlank() || email.isBlank() || phone.isBlank() || customCode.isBlank() || password.isBlank()) {
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

                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val result = authRepository.registerStaff(
                                fullName = fullName,
                                email = email,
                                phone = phone,
                                password = password,
                                customCode = customCode,
                                role = selectedRole
                            )
                            isLoading = false
                            when (result) {
                                is AuthResult.Success -> onRegisterSuccess(result.session)
                                is AuthResult.NeedsEmailVerification -> onRegisterSuccess(UserSession(
                                    id = "temp-staff",
                                    fullName = fullName,
                                    email = email,
                                    phoneNumber = phone,
                                    role = selectedRole,
                                    isEmailVerified = false,
                                    customCode = customCode
                                ))
                                is AuthResult.Error -> {
                                    errorMessage = if (language == AppLanguage.ARABIC) result.messageAr else result.messageEn
                                }
                            }
                        }
                    },
                    testTag = "teacher_submit_register_button"
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
