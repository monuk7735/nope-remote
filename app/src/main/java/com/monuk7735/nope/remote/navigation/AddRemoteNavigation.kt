package com.monuk7735.nope.remote.navigation

sealed class AddRemoteNavigation(val route: String) {

    object ListTypes : AddRemoteNavigation("list_types")

    object ListBrands : AddRemoteNavigation("list_brands")

    object ListCodes : AddRemoteNavigation("list_codes")

}
