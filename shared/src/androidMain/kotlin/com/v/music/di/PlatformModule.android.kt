package com.v.music.di

import androidx.room3.RoomDatabase
import com.v.music.data.local.AndroidMediaManager
import com.v.music.data.local.MusicDatabase
import com.v.music.data.local.PlatformMediaManager
import com.v.music.data.local.getDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun platformModule() = module {

    /// inject database builder
    single<RoomDatabase.Builder<MusicDatabase>> {
        getDatabaseBuilder(androidContext())
    }

    // inject media manager
    single<PlatformMediaManager> {
        AndroidMediaManager(androidContext())
    }
}
