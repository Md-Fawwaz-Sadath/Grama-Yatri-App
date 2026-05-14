package com.gramayatri.app.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.gramayatri.app.data.remote.FirebaseDataSource
import com.gramayatri.app.data.remote.FirebaseSeeder
import com.gramayatri.app.data.repository.FirebaseRepository
import com.gramayatri.app.data.repository.UserPrefsRepository
import com.gramayatri.app.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase =
        FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_URL)

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDataSource(
        database: FirebaseDatabase
    ): FirebaseDataSource =
        FirebaseDataSource(database)

    @Provides
    @Singleton
    fun provideFirebaseRepository(
        dataSource: FirebaseDataSource,
        auth: FirebaseAuth
    ): FirebaseRepository =
        FirebaseRepository(dataSource, auth)

    @Provides
    @Singleton
    fun provideUserPrefsRepository(
        @ApplicationContext context: Context
    ): UserPrefsRepository =
        UserPrefsRepository(context)

    @Provides
    @Singleton
    fun provideFirebaseSeeder(
        @ApplicationContext context: Context,
        database: FirebaseDatabase
    ): FirebaseSeeder =
        FirebaseSeeder(context, database)
}
