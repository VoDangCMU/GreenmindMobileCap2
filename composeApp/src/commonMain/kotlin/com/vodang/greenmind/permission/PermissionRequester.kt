package com.vodang.greenmind.permission

import kotlinx.coroutines.flow.StateFlow

enum class PermissionGroup { LOCATION, CAMERA }

expect object PermissionRequester {
    fun grantedFlow(group: PermissionGroup): StateFlow<Boolean>
    fun request(group: PermissionGroup)
}
