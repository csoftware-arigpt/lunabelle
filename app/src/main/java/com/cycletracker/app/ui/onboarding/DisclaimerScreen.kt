package com.cycletracker.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cycletracker.app.R

@Composable
fun DisclaimerScreen(
    onAccept: () -> Unit,
    viewModel: DisclaimerViewModel = hiltViewModel(),
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.disclaimer_title), style = MaterialTheme.typography.headlineSmall)
        DisclaimerBlock(stringResource(R.string.disclaimer_contra_title), stringResource(R.string.disclaimer_contra_body))
        DisclaimerBlock(stringResource(R.string.disclaimer_estimates_title), stringResource(R.string.disclaimer_estimates_body))
        DisclaimerBlock(stringResource(R.string.disclaimer_clinician_title), stringResource(R.string.disclaimer_clinician_body))
        Button(onClick = { viewModel.accept(onAccept) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.onboarding_accept_continue))
        }
    }
}

@Composable
private fun DisclaimerBlock(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(body, style = MaterialTheme.typography.bodyMedium)
    }
}
