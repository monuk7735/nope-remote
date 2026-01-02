package com.monuk7735.nope.remote.composables

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.infrared.patterns.IRPatternDecoder
import com.monuk7735.nope.remote.models.database.RemoteButtonDBModel
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.models.retrofit.DeviceBrandsRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceCodesRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceTypesRetrofitModel
import java.util.Date

@ExperimentalMaterial3Api
@Composable
fun ListTypes(
        allTypes: List<DeviceTypesRetrofitModel>?,
        isRepoInstalled: Boolean,
        availableRepos: List<String>,
        selectedRepoIndex: Int,
        onRepoSelected: (Int) -> Unit,
        onOneClicked: (type: String) -> Unit,
        onSearch: (String) -> Unit,
        onBack: () -> Unit,
        onGoToSettings: () -> Unit
) {
        var showDialog by remember { mutableStateOf(false) }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Select Repository") },
                text = {
                    Column {
                        availableRepos.forEachIndexed { index, name ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onRepoSelected(index)
                                        showDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (index == selectedRepoIndex),
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = name)
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        Scaffold(
                topBar = {
                        AppBar(
                                title =
                                        if (allTypes == null && isRepoInstalled) "Loading..."
                                        else "Select Device",
                                onBack = onBack,
                                actions = {
                                        if (availableRepos.size > 1) {
                                                IconButton(onClick = { showDialog = true }) {
                                                        Icon(
                                                                imageVector = Icons.AutoMirrored.Outlined.List,
                                                                contentDescription = "Select Repository"
                                                        )
                                                }
                                        }
                                }
                        )
                },
                content = { paddingValues ->
                        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                                SearchBar(onSearch = onSearch, hint = "Search device types...")

                                if (!isRepoInstalled) {
                                        Column(
                                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                        ) {
                                                Icon(
                                                        imageVector = Icons.Outlined.CloudOff,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(64.dp),
                                                        tint = MaterialTheme.colorScheme.error
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                        text = "No Repository Found",
                                                        style = MaterialTheme.typography.titleLarge,
                                                        fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                        text =
                                                                "You need to download an IR database to use this feature without internet.",
                                                        textAlign = TextAlign.Center,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.height(24.dp))
                                                Button(onClick = onGoToSettings) {
                                                        Text("Go to Settings")
                                                }
                                        }
                                } else if (allTypes == null) {
                                        Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                        ) { CircularProgressIndicator() }
                                } else if (allTypes.isEmpty()) {
                                        EmptyState(
                                                text = "No types found",
                                                secondaryText = "Try a different search term",
                                                icon = Icons.Outlined.SearchOff
                                        )
                                } else {
                                        LazyVerticalGrid(
                                                columns = GridCells.Fixed(2),
                                                modifier = Modifier.fillMaxSize(),
                                                contentPadding = PaddingValues(16.dp),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                                items(
                                                        items = allTypes.sortedBy { it.type },
                                                        key = { it.type }
                                                ) { typeModel ->
                                                        DeviceTypeComposable(
                                                                name = typeModel.type,
                                                                icon = typeModel.getIcon(),
                                                                onClick = {
                                                                        onOneClicked(typeModel.type)
                                                                }
                                                        )
                                                }
                                        }
                                }
                        }
                }
        )
}

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Composable
fun ListBrands(
        allBrands: List<DeviceBrandsRetrofitModel>?,
        onOneClicked: (type: String, brand: String) -> Unit,
        onSearch: (String) -> Unit,
        onBack: () -> Unit
) {
        Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                        AppBar(
                                title = allBrands?.firstOrNull()?.type ?: "Select Brand",
                                onBack = onBack
                        )
                },
                content = { paddingValues ->
                        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                                SearchBar(onSearch = onSearch, hint = "Search brands...")

                                if (allBrands == null) {
                                        Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                        ) { CircularProgressIndicator() }
                                } else if (allBrands.isEmpty()) {
                                        EmptyState(
                                                text = "No brands found",
                                                secondaryText = "Try a different search term",
                                                icon = Icons.Outlined.SearchOff
                                        )
                                } else {
                                        val groupedSortedList =
                                                allBrands.sortedBy { it.brand }.groupBy {
                                                    val firstChar = it.brand.firstOrNull()?.uppercaseChar() ?: '#'
                                                    if (firstChar in 'A'..'Z') firstChar else '#'
                                                }

                                        LazyColumn(
                                                modifier = Modifier.fillMaxSize(),
                                                contentPadding = PaddingValues(vertical = 16.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                groupedSortedList.forEach { (initial, brands) ->
                                                        stickyHeader {
                                                                Surface(
                                                                        modifier =
                                                                            Modifier.fillMaxWidth(),
                                                                        color =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .surface,
                                                                        tonalElevation = 2.dp
                                                                ) {
                                                                        Text(
                                                                                text =
                                                                                        initial.toString(),
                                                                                modifier =
                                                                                        Modifier.padding(
                                                                                                vertical =
                                                                                                        8.dp,
                                                                                                horizontal =
                                                                                                        24.dp
                                                                                        ),
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .titleMedium,
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold,
                                                                                color =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .primary
                                                                        )
                                                                }
                                                        }

                                                        items(items = brands, key = { it.brand }) {
                                                                item ->
                                                                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                                                        DeviceBrandComposable(
                                                                                name = item.brand,
                                                                                onClick = {
                                                                                        onOneClicked(
                                                                                                item.type,
                                                                                                item.brand
                                                                                        )
                                                                                }
                                                                        )
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
        )
}

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Composable
fun ListCodes(
        allCodes: List<DeviceCodesRetrofitModel>?,
        loadingProgress: Pair<Int, Int>?,
        onSave: (remoteDataDBModel: RemoteDataDBModel) -> Unit,
        onBack: () -> Unit
) {
        var selected by remember { mutableStateOf(0) }

        val hasCodes = !allCodes.isNullOrEmpty()
        val rightEnabled = hasCodes && (selected + 1 < allCodes.size)
        val leftEnabled = hasCodes && (selected > 0)

        val allRemoteDataDBModels =
                remember(allCodes) { allCodes?.map { it.toDBModel() } ?: emptyList() }

        Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                        AppBar(
                                title = if (!hasCodes) "Loading..." else allCodes[selected].brand,
                                onBack = onBack
                        )
                },
                bottomBar = {
                        if (hasCodes) {
                                Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
                                        Row(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Row(
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        IconButton(
                                                                onClick = { selected-- },
                                                                enabled = leftEnabled
                                                        ) {
                                                                Icon(
                                                                        Icons.AutoMirrored.Outlined
                                                                                .ArrowBackIos,
                                                                        contentDescription =
                                                                                "Previous"
                                                                )
                                                        }
                                                        Text(
                                                                text =
                                                                        "${selected + 1} / ${allCodes.size}",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .titleMedium,
                                                                modifier =
                                                                        Modifier.padding(
                                                                                horizontal = 8.dp
                                                                        )
                                                        )
                                                        IconButton(
                                                                onClick = { selected++ },
                                                                enabled = rightEnabled
                                                        ) {
                                                                Icon(
                                                                        Icons.AutoMirrored.Outlined
                                                                                .ArrowForwardIos,
                                                                        contentDescription = "Next"
                                                                )
                                                        }
                                                }

                                                Button(
                                                        onClick = {
                                                                onSave(
                                                                        allRemoteDataDBModels[
                                                                                selected]
                                                                )
                                                        },
                                                        shape = RoundedCornerShape(12.dp)
                                                ) {
                                                        Icon(
                                                                Icons.Outlined.Save,
                                                                contentDescription = null
                                                        )
                                                        Spacer(Modifier.width(8.dp))
                                                        Text("Save Remote")
                                                }
                                        }
                                }
                        }
                }
        ) { paddingValues ->
                if (allCodes == null) {
                        Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                        ) {
                                if (loadingProgress != null) {
                                        val (current, total) = loadingProgress
                                        val progress =
                                                if (total > 0) current.toFloat() / total.toFloat()
                                                else 0f

                                        Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                                CircularProgressIndicator(progress = { progress })
                                                Text(
                                                        text = "Loading $current of $total...",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                                )
                                        }
                                } else {
                                        CircularProgressIndicator()
                                }
                        }
                        return@Scaffold
                } else if (!hasCodes) {
                        EmptyState(
                                text = "No codes found",
                                secondaryText = "Try a different search term or device",
                                icon = Icons.Outlined.SearchOff
                        )
                        return@Scaffold
                }

                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(paddingValues)
                                        .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        Text(
                                text = "Test the buttons below",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                                text = "Ensure your device responds",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 16.dp)
                        )

                        TestRemoteGrid(remoteDataDBModel = allRemoteDataDBModels[selected])
                }
        }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TestRemoteGrid(remoteDataDBModel: RemoteDataDBModel) {
        val context = LocalContext.current
        val irController = remember {
                IRController(
                        context.getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager
                )
        }
        val vibrator = remember {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager =
                                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as
                                        VibratorManager
                        vibratorManager.defaultVibrator
                } else {
                        context.getSystemService(Vibrator::class.java)!!
                }
        }

        val allButtons =
                remember(remoteDataDBModel) {
                        remoteDataDBModel.onScreenRemoteButtonDBS +
                                remoteDataDBModel.offScreenRemoteButtonDBS
                }

        LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 80.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
        ) {
                items(allButtons.size) { index ->
                        val button = allButtons[index]
                        RemoteButtonSingle(
                                name = button.name,
                                icon = button.getIcon(),
                                textIcon = button.getTextIcon(),
                                offsetX = 0f,
                                offsetY = 0f,
                                onClick = { button.transmit(irController, vibrator) }
                        )
                }
        }
}

private fun DeviceCodesRetrofitModel.toDBModel(): RemoteDataDBModel {
        val listOfRemoteButtons = mutableListOf<RemoteButtonDBModel>()
        val powerKey =
                codes.keys.find { it.contains("Power", ignoreCase = true) }
                        ?: codes.keys.firstOrNull() ?: "POWER TOGGLE"
        val powerCode = codes[powerKey] ?: ""

        codes.forEach { (key, value) ->
                if (key != powerKey) {
                        listOfRemoteButtons.add(
                                RemoteButtonDBModel(
                                        offsetX = 0f,
                                        offsetY = 0f,
                                        name = key,
                                        irPattern = IRPatternDecoder(value).irPattern
                                )
                        )
                }
        }
        return RemoteDataDBModel(
                id = 0,
                name = brand,
                type = type,
                brand = brand,
                added = java.util.Date(),
                offScreenRemoteButtonDBS = listOfRemoteButtons,
                onScreenRemoteButtonDBS =
                        if (powerCode.isNotEmpty())
                                listOf(
                                        RemoteButtonDBModel(
                                                offsetX = 0f,
                                                offsetY = 0f,
                                                name = powerKey,
                                                irPattern = IRPatternDecoder(powerCode).irPattern
                                        )
                                )
                        else emptyList()
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceTypeComposable(
        name: String,
        icon: ImageVector,
        onClick: () -> Unit,
) {
        Card(
                onClick = onClick,
                modifier = Modifier.aspectRatio(1f).fillMaxWidth().clip(RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors =
                        CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
                Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                                modifier =
                                        Modifier.align(Alignment.TopEnd)
                                                .size(60.dp)
                                                .background(
                                                        androidx.compose.ui.graphics.Brush
                                                                .radialGradient(
                                                                        colors =
                                                                                listOf(
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .primary
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.05f
                                                                                                ),
                                                                                        Color.Transparent
                                                                                )
                                                                )
                                                )
                        )

                        Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                                Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color =
                                                MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = 0.8f
                                                ),
                                        modifier = Modifier.size(56.dp)
                                ) {
                                        Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                        imageVector = icon,
                                                        contentDescription = null,
                                                        tint =
                                                                MaterialTheme.colorScheme
                                                                        .onPrimaryContainer,
                                                        modifier = Modifier.size(28.dp)
                                                )
                                        }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                        text = name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface
                                )
                        }
                }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceBrandComposable(
        name: String,
        onClick: () -> Unit,
) {
        Card(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                shape = RoundedCornerShape(20.dp),
                colors =
                        CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
                Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color =
                                        MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = 0.3f
                                        )
                        ) {
                                Box(contentAlignment = Alignment.Center) {
                                        Text(
                                                text = name.take(1).uppercase(),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                        )
                                }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                        )
                }
        }
}

@Composable
fun SearchBar(
        modifier: Modifier = Modifier,
        hint: String = "Search...",
        onSearch: (String) -> Unit
) {
        var text by remember { mutableStateOf("") }

        Surface(
                modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                tonalElevation = 2.dp
        ) {
                TextField(
                        value = text,
                        onValueChange = {
                                text = it
                                onSearch(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                                Text(
                                        text = hint,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        },
                        leadingIcon = {
                                Icon(
                                        Icons.Outlined.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                )
                        },
                        trailingIcon = {
                                if (text.isNotEmpty()) {
                                        IconButton(
                                                onClick = {
                                                        text = ""
                                                        onSearch("")
                                                }
                                        ) {
                                                Icon(
                                                        Icons.Outlined.Close,
                                                        contentDescription = "Clear",
                                                        tint =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                                )
                                        }
                                }
                        },
                        singleLine = true,
                        colors =
                                TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                ),
                        textStyle = MaterialTheme.typography.bodyLarge
                )
        }
}
