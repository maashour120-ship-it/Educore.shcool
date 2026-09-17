package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MarkEmailRead
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
import com.example.ui.components.EducoreHeaderBar
import com.example.ui.components.EducorePrimaryButton
import com.example.ui.components.EducoreSecondaryButton
import com.example.ui.components.EducoreTextField
import com.example.ui.theme.EducoreSuccess
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    language: AppLanguage,
    themeMode: ThemeMode,
    authRepository: AuthRepository,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalizedStrings.get(language)
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = strings.resetPassword,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isSubmitted) strings.verificationSentSuccess else "أدخل بريدك الإلكتروني لتلقي رابط إعادة تعيين كلمة المرور",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSubmitted) EducoreSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!isSubmitted) {
                EducoreTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = strings.email,
                    leadingIcon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    testTag = "forgot_email_input"
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    EducorePrimaryButton(
                        text = strings.sendResetLink,
                        onClick = {
                            if (email.isNotBlank()) {
                                isLoading = true
                                scope.launch {
                                    authRepository.sendPasswordReset(email)
                                    isLoading = false
                                    isSubmitted = true
                                }
                            }
                        },
                        testTag = "submit_reset_button"
                    )
                }
            } else {
                EducorePrimaryButton(
                    text = strings.backToLogin,
                    onClick = onBack,
                    testTag = "back_login_button"
                )
            }
        }
    }
}

@Composable
fun EmailVerificationScreen(
    email: String,
    language: AppLanguage,
    themeMode: ThemeMode,
    authRepository: AuthRepository,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalizedStrings.get(language)
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    var resentSuccess by remember { mutableStateOf(false) }

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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = strings.emailVerificationTitle,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = email,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = strings.emailVerificationDesc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            EducorePrimaryButton(
                text = strings.checkVerificationStatus,
                onClick = onVerified,
                testTag = "check_verification_button"
            )

            Spacer(modifier = Modifier.height(12.dp))

            EducoreSecondaryButton(
                text = strings.resendVerificationEmail,
                onClick = {
                    scope.launch {
                        authRepository.resendVerificationEmail(email)
                        resentSuccess = true
                    }
                },
                testTag = "resend_verification_button"
            )

            if (resentSuccess) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = strings.verificationSentSuccess,
                    style = MaterialTheme.typography.bodySmall,
                    color = EducoreSuccess
                )
            }
        }
    }
}
