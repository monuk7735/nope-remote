package com.monuk7735.nope.remote.composables

import android.util.Log
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
    onSearch: (String) -> Unit
) {
    Scaffold(
        topBar = {
            AppBar(
                title = if (allTypes == null) "Loading..." else "Types"
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
    onSearch: (String) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppBar(
                title = allBrands?.get(0)?.type ?: "Loading...",
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
) {
    var selected by remember {
        mutableStateOf(0)
    }

    val rightEnabled = selected + 1 < allCodes?.size ?: 0
    val leftEnabled = selected > 0

    val allRemoteDataDBModel = mutableListOf<RemoteDataDBModel>()

    allCodes?.forEach {
        Log.d("monumonu", "ListCodes: Converting ${it.brand}")
        val listOfRemoteButtons = mutableListOf<RemoteButtonDBModel>()
        it.codes.forEach { button ->
            if (button.key != "POWER TOGGLE")
                listOfRemoteButtons.add(
                    RemoteButtonDBModel(
                        offsetX = 0f,
                        offsetY = 0f,
                        name = button.key,
                        irPattern = IRPatternDecoder(button.value).irPattern
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
                onScreenRemoteButtonDBS = listOf(
                    RemoteButtonDBModel(
                        offsetX = 0f,
                        offsetY = 0f,
                        name = "POWER TOGGLE",
                        irPattern = IRPatternDecoder(it.codes["POWER TOGGLE"]!!).irPattern
                    )

                )
            )
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppBar(
                title = if (allCodes == null) "Loading..." else "${allCodes[selected].type} - ${allCodes[selected].brand}"
            )
        },
        content = { paddingValues ->
            if (allCodes == null) {
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
                    .onGloballyPositioned { coordinates ->
                        layoutLimits = coordinates.boundsInWindow()
                    }
            ) {
                UniversalRemote(
                    remoteDataDBModel = allRemoteDataDBModel[selected],
                    layoutLimits = layoutLimits
                )
            }
        },
        bottomBar = {
            NavigationBar {
                Icon(
                    modifier = Modifier
                        .weight(1 / 3f)
                        .fillMaxHeight()
                        .clickable(enabled = leftEnabled) { selected-- }
                        .padding(15.dp),
                    imageVector = Icons.Outlined.KeyboardArrowLeft,
                    contentDescription = "Prev"
                )
                Column(
                    modifier = Modifier
                        .weight(1 / 3f)
                        .fillMaxHeight()
                        .clickable {
                            if (allCodes == null) {
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
                    allCodes?.let { Text(text = "${selected + 1}/${it.size}") }
                }
                Icon(
                    modifier = Modifier
                        .weight(1 / 3f)
                        .fillMaxHeight()
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
