package com.ryim.actin.data.workouts

import android.content.Context
import com.ryim.actin.domain.workouts.WorkoutRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkoutDataModule {

    @Provides
    @Singleton
    fun provideWorkoutRepository(
        @ApplicationContext context: Context
    ): WorkoutRepository {
        return WorkoutRepositoryImpl(context)
    }
}
