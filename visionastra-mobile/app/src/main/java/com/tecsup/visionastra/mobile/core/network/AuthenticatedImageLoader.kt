package com.tecsup.visionastra.mobile.core.network

import android.content.Context
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AuthenticatedImageLoaderEntryPoint {
    @AuthenticatedClient
    fun okHttpClient(): OkHttpClient
}

fun authenticatedImageLoader(context: Context): ImageLoader {
    val entryPoint = EntryPointAccessors.fromApplication(
        context.applicationContext,
        AuthenticatedImageLoaderEntryPoint::class.java
    )
    val okHttpClient = entryPoint.okHttpClient()
    return ImageLoader.Builder(context)
        .components {
            add(OkHttpNetworkFetcherFactory(callFactory = okHttpClient))
        }
        .build()
}
