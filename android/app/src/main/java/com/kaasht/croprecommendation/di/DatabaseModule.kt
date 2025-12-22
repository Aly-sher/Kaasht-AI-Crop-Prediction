package com.kaasht.croprecommendation.di

import android.content.Context
import androidx.room.Room
import com.kaasht.croprecommendation.data.local.AppDatabase
import com.kaasht.croprecommendation.data.local.PredictionDao
import com.kaasht.croprecommendation.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun providePredictionDao(database: AppDatabase): PredictionDao {
        return database.predictionDao()
    }
}
