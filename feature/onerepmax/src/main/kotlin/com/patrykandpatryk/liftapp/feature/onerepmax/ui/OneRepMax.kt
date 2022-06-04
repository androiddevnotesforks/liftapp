package com.patrykandpatryk.liftapp.feature.onerepmax.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.patrykandpatryk.liftapp.core.R
import com.patrykandpatryk.liftapp.core.extension.formatValue
import com.patrykandpatryk.liftapp.core.extension.stringResourceId
import com.patrykandpatryk.liftapp.core.ui.Info
import com.patrykandpatryk.liftapp.core.ui.TopAppBar
import com.patrykandpatryk.liftapp.core.ui.topAppBarScrollBehavior
import com.patrykandpatryk.liftapp.feature.onerepmax.viewmodel.OneRepMaxViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun OneRepMax(
    parentNavController: NavController,
    modifier: Modifier = Modifier,
) {
    val topAppBarScrollBehavior = topAppBarScrollBehavior()
    val viewModel = hiltViewModel<OneRepMaxViewModel>()
    val uiState = viewModel.oneRepMaxUiState
    val massUnit by viewModel.massUnit.collectAsState(initial = null)
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = stringResource(id = R.string.route_one_rep_max),
                scrollBehavior = topAppBarScrollBehavior,
                onBackClick = parentNavController::popBackStack,
            )
        },
    ) { paddingValues ->

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(paddingValues = paddingValues),
        ) {

            Text(
                text = massUnit?.formatValue(
                    context = context,
                    value = uiState.oneRepMax,
                    decimalPlaces = OneRepMaxDecimalPlaces,
                ).orEmpty(),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 16.dp),
            )

            Text(
                text = stringResource(id = R.string.one_rep_max),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 32.dp,
                    end = 16.dp,
                ),
            ) {

                TextField(
                    value = uiState.massInput,
                    onValueChange = viewModel::updateMassInput,
                    isError = uiState.massInputValid.not(),
                    keyboardType = KeyboardType.Decimal,
                    label = massUnit?.let {
                        stringResource(
                            id = R.string.mass_with_unit,
                            stringResource(id = it.stringResourceId),
                        )
                    } ?: stringResource(id = R.string.mass),
                )

                TextField(
                    value = uiState.repsInput,
                    keyboardType = KeyboardType.Number,
                    onValueChange = viewModel::updateRepsInput,
                    isError = uiState.repsInputValid.not(),
                    label = stringResource(id = R.string.reps),
                )
            }

            Info(
                text = stringResource(id = R.string.one_rep_max_description),
                modifier = Modifier.padding(top = 32.dp),
            )
        }
    }
}

@Composable
private fun RowScope.TextField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    label: String,
    keyboardType: KeyboardType,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        isError = isError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        label = { Text(text = label) },
        modifier = Modifier.weight(weight = 1f),
    )
}

private const val OneRepMaxDecimalPlaces = 1
