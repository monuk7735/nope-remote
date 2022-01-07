package com.monuk7735.nope.remote.models.custom.flows

import com.monuk7735.nope.remote.infrared.patterns.IRPattern

class FlowTransmit(
    var name: String,
    val sourceRemoteId:Int,
    val sourceButtonName: String,
    val irPattern: IRPattern
)