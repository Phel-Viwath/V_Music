package com.v.music.di

import androidx.room3.RoomDatabase
import com.v.music.data.local.IosMediaManager
import com.v.music.data.local.MusicDatabase
import com.v.music.data.local.PlatformMediaManager
import com.v.music.data.local.getDatabaseBuilder
import org.koin.dsl.module

actual fun platformModule() = module {

    // inject database builder on iOS
    single<RoomDatabase.Builder<MusicDatabase>> {
        getDatabaseBuilder()
    }

    // inject media manager on iOS
    single <PlatformMediaManager> { IosMediaManager() }
}