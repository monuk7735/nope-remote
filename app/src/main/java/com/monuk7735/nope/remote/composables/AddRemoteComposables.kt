package com.monuk7735.nope.remote.composables

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import java.util.*

@ExperimentalMaterial3Api
@Composable
fun ListTypes(
    allTypes: List<DeviceTypesRetrofitModel>?,
    onOneClicked: (type: String) -> Unit,
) {
    Scaffold(
        topBar = {
            AppBar(
                title = if (allTypes == null) "Loading..." else "Types"
            )
        },
        content = {
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

                items(allTypes.sortedBy {
                    it.type[0]
                }) { type ->
                    DeviceTypeComposable(
                        name = type.type,
                        icon = type.getIcon(),
                        onClick = {
                            onOneClicked(type.type)
                        }
                    )
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
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppBar(
                title = allBrands?.get(0)?.type ?: "Loading...",
            )
        },
        content = {
            if (allBrands == null) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
                return@Scaffold
            }

            val groupedSortedList = allBrands
                .sortedBy {
                    it.brand
                }
                .groupBy {
                    it.brand[0]
                }
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                for (key in groupedSortedList.keys) {
                    stickyHeader {
                        Surface() {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Text(text = key.toString())
                            }
                        }
                    }
                    items(groupedSortedList[key] ?: listOf()) { item ->
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
    )
}

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
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
        content = {
            if (allCodes == null) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
                return@Scaffold
            }
            var layoutLimits by remember {
                mutableStateOf(Rect(Offset.Zero, 0f))
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
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
//                        .background(
//                            if (leftEnabled)
//                                MaterialTheme.colors.primary
//                            else
//                                Color.Gray
//                        )
                        .clickable(
                            enabled = leftEnabled
                        ) {
                            selected--
                        }
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
                    Text(text = "${selected + 1}/${allCodes?.size}")
                }
                Icon(
                    modifier = Modifier
                        .weight(1 / 3f)
                        .fillMaxHeight()
//                        .background(
//                            if (rightEnabled)
//                                MaterialTheme.colors.primary
//                            else
//                                Color.Gray
//                        )
                        .clickable(
                            enabled = rightEnabled,
                            onClickLabel = "Next"
                        ) {
                            selected++
                        }
                        .padding(15.dp),
                    imageVector = Icons.Outlined.KeyboardArrowRight,
                    contentDescription = "Next"
                )
            }
//            Row(
//                modifier = Modifier
//                    .height(60.dp)
//                    .background(MaterialTheme.colorScheme.primary),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceEvenly
//            ) {
//
//            }
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
            .clickable {
                onClick()
            }
            .fillMaxWidth()
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
            .clickable {
                onClick()
            }
            .fillMaxWidth()
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