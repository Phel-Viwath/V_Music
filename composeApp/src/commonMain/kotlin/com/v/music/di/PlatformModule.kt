package com.v.music.di

import com.v.music.data.local.getFavoriteDao
import com.v.music.data.local.getMusicDatabase
import com.v.music.data.local.getPlaylistDao
import com.v.music.data.repository.MusicRepositoryImp
import com.v.music.domain.repository.MusicRepository
import com.v.music.domain.use_case.music_use_case.DeleteMusicsUseCase
import com.v.music.domain.use_case.music_use_case.GetMusicUseCase
import com.v.music.domain.use_case.music_use_case.MusicUseCase
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module

expect fun platformModule() : Module

fun initKoin(config: KoinAppDeclaration? = null): KoinApplication {
    return startKoin {
        config?.invoke(this)
        modules(
            platformModule(),
            provideDatabaseModule,
            provideMusicRepositoryModule,
            provideMusicUseCaseModule
        )
    }
}

val provideMusicRepositoryModule = module {
    singleOf(::MusicRepositoryImp).bind(MusicRepository::class)
}

val provideDatabaseModule = module {
    single { getMusicDatabase(get()) }
    single { getFavoriteDao(get()) }
    single { getPlaylistDao(get()) }
}

val provideMusicUseCaseModule = module {
    factory { GetMusicUseCase(get()) }
    factory { DeleteMusicsUseCase(get()) }
    factory {
       MusicUseCase(get(), get())
    }
}