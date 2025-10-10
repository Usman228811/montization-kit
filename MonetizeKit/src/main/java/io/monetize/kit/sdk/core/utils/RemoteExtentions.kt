package io.monetize.kit.sdk.core.utils

import io.monetize.kit.sdk.core.utils.init.AdKit


fun firebaseBoolean(key: String, default: Boolean) =
    AdKit.firebaseHelper.getBoolean(key, default)


fun firebaseLong(key: String, default: Long) =
    AdKit.firebaseHelper.getLong(key, default)


fun firebaseString(key: String, default: String) =
    AdKit.firebaseHelper.getString(key, default)