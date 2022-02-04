package com.example.weather.di

import android.content.Context
import androidx.room.Room
import com.example.weather.api.OpenWeatherMapApi
import com.example.weather.database.LocationDao
import com.example.weather.database.LocationDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(OpenWeatherMapApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): OpenWeatherMapApi =
        retrofit.create(OpenWeatherMapApi::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): LocationDatabase =
        Room.databaseBuilder(
            appContext,
            LocationDatabase::class.java,
            "location_database"
        ).build()

    @Provides
    @Singleton
    fun provideDao(locationDatabase: LocationDatabase): LocationDao =
        locationDatabase.getLocationDao()
}