package com.si.myfoodordering.di

import com.si.myfoodordering.data.SupabaseConfig
import com.si.myfoodordering.data.repository.AuthRepository
import com.si.myfoodordering.data.repository.AvisRepository
import com.si.myfoodordering.data.repository.FavoriRepository
import com.si.myfoodordering.data.repository.OrderRepository
import com.si.myfoodordering.data.repository.PlatRepository
import com.si.myfoodordering.data.repository.PushTokenRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = SupabaseConfig.client

    @Provides
    @Singleton
    fun provideAuthRepository(supabase: SupabaseClient): AuthRepository = AuthRepository(supabase)

    @Provides
    @Singleton
    fun providePlatRepository(supabase: SupabaseClient): PlatRepository = PlatRepository(supabase)

    @Provides
    @Singleton
    fun provideOrderRepository(supabase: SupabaseClient): OrderRepository = OrderRepository(supabase)

    @Provides
    @Singleton
    fun providePushTokenRepository(supabase: SupabaseClient): PushTokenRepository =
        PushTokenRepository(supabase)

    @Provides
    @Singleton
    fun provideAvisRepository(supabase: SupabaseClient): AvisRepository = AvisRepository(supabase)

    @Provides
    @Singleton
    fun provideFavoriRepository(supabase: SupabaseClient): FavoriRepository = FavoriRepository(supabase)
}
