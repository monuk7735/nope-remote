package com.monuk7735.nope.remote.composables


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.monuk7735.nope.remote.infrared.patterns.IRPatternDecoder
import com.monuk7735.nope.remote.models.database.RemoteButtonDBModel
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.models.retrofit.DeviceBrandsRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceCodesRetrofitModel
import com.monuk7735.nope.remote.models.retrofit.DeviceTypesRetrofitModel
import java.util.Date
import androidx.compose.material3.TextField
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.OutlinedTextField

@ExperimentalMaterial3Api
@Composable
fun ListTypes(
    allTypes: List<DeviceTypesRetrofitModel>?,
    onOneClicked: (type: String) -> Unit,
    onSearch: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            AppBar(
                title = if (allTypes == null) "Loading..." else "Types",
                onBack = {
                    onBack()
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                SearchBar(onSearch = onSearch)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                if (allTypes == null) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    return@LazyColumn
                }

                items(
                    items = allTypes.sortedBy { it.type[0] },
                    key = { it.type }
                ) {
                    DeviceTypeComposable(
                        name = it.type,
                        icon = it.getIcon(),
                        onClick = {
                            onOneClicked(it.type)
                        }
                    )
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
                title = allBrands?.get(0)?.type ?: "Loading...",
                onBack = {
                    onBack()
                }
            )
        },
        content = { paddingValues ->
            if (allBrands == null) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues)

                )
                return@Scaffold
            }

            val groupedSortedList = allBrands
                .sortedBy { it.brand }
                .groupBy { it.brand[0] }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                SearchBar(onSearch = onSearch, hint = "Search Brands...")
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                for (key in groupedSortedList.keys) {
                    stickyHeader {
                        Surface {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Text(text = key.toString())
                            }
                        }
                    }
                    items(
                        items = groupedSortedList[key] ?: listOf(),
                        key = { it.brand }
                    ) { item ->
                        DeviceBrandComposable(
                            name = item.brand,
                            onClick = {
                                onOneClicked(item.type, item.brand)
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
fun ListCodes(
    allCodes: List<DeviceCodesRetrofitModel>?,
    onSave: (remoteDataDBModel: RemoteDataDBModel) -> Unit,
    onBack: () -> Unit
) {
    var selected by remember {
        mutableStateOf(0)
    }

    val rightEnabled = selected + 1 < (allCodes?.size ?: 0)
    val leftEnabled = selected > 0

    val allRemoteDataDBModel = mutableListOf<RemoteDataDBModel>()

    allCodes?.forEach {
        val listOfRemoteButtons = mutableListOf<RemoteButtonDBModel>()
        
        // Find Power button safely
        val powerKey = it.codes.keys.find { key -> 
            key.contains("Power", ignoreCase = true) 
        } ?: it.codes.keys.firstOrNull() ?: "POWER TOGGLE"
        
        val powerCode = it.codes[powerKey] ?: ""

        it.codes.forEach { (key, value) ->
            if (key != powerKey)
                listOfRemoteButtons.add(
                    RemoteButtonDBModel(
                        offsetX = 0f,
                        offsetY = 0f,
                        name = key,
                        irPattern = IRPatternDecoder(value).irPattern
                    )
                )
        }
        allRemoteDataDBModel.add(
            RemoteDataDBModel(
                id = 0,
                name = it.brand,
                type = it.type,
                brand = it.brand,
                added = Date(),
                offScreenRemoteButtonDBS = listOfRemoteButtons,
                onScreenRemoteButtonDBS = if(powerCode.isNotEmpty()) listOf(
                    RemoteButtonDBModel(
                        offsetX = 0f,
                        offsetY = 0f,
                        name = powerKey,
                        irPattern = IRPatternDecoder(powerCode).irPattern
                    )
                ) else emptyList()
            )
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppBar(
                title = if (allCodes.isNullOrEmpty()) "Loading..." else "${allCodes[selected].type} - ${allCodes[selected].brand}",
                onBack = {
                    onBack()
                }
            )
        },
        content = { paddingValues ->
            if (allCodes.isNullOrEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
                return@Scaffold
            }
            var layoutLimits by remember {
                mutableStateOf(Rect.Zero)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp), // Add margin
                    contentAlignment = Alignment.Center
            ) {
                 Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { coordinates ->
                            layoutLimits = coordinates.boundsInWindow()
                        }
                ) {
                    UniversalRemote(
                        remoteDataDBModel = allRemoteDataDBModel[selected],
                        layoutLimits = layoutLimits
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar {
                Icon(
                    modifier = Modifier
                        .weight(1 / 3f)
                        .clickable(enabled = leftEnabled) { selected-- }
                        .padding(15.dp),
                    imageVector = Icons.Outlined.KeyboardArrowLeft,
                    contentDescription = "Prev"
                )
                Column(
                    modifier = Modifier
                        .weight(1 / 3f)
                        .clickable {
                            if (allCodes.isNullOrEmpty()) {
                                return@clickable
                            }
                            onSave(allRemoteDataDBModel[selected])
                        }
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly,

                    ) {
                    Icon(
                        imageVector = Icons.Outlined.Done,
                        contentDescription = "Done"
                    )
                    if (!allCodes.isNullOrEmpty()) {
                        Text(text = "${selected + 1}/${allCodes.size}")
                    }
                }
                Icon(
                    modifier = Modifier
                        .weight(1 / 3f)
                        .clickable(enabled = rightEnabled) { selected++ }
                        .padding(15.dp),
                    imageVector = Icons.Outlined.KeyboardArrowRight,
                    contentDescription = "Next"
                )
            }
        }
    )
}

@Composable
fun DeviceTypeComposable(
    name: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(5.dp)
            .clickable { onClick() }
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.padding(end = 10.dp, start = 10.dp),
                imageVector = icon,
                contentDescription = name
            )
            Text(text = name)
        }
    }
}

@Composable
fun DeviceBrandComposable(
    name: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(5.dp)
            .clickable { onClick() }
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name)
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
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onSearch(it)
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        placeholder = { Text(text = hint) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        singleLine = true
    )
}
