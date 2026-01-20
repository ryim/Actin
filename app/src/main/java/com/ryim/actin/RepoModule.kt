package com.ryim.actin

import android.content.Context
import com.ryim.actin.data.ExerciseRepositoryImpl
import com.ryim.actin.domain.ExerciseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {

    @Provides
    fun provideExerciseRepository(
        @ApplicationContext context: Context
    ): ExerciseRepository = ExerciseRepositoryImpl(context)
}