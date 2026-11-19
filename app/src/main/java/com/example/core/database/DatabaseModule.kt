package com.example.core.database

import android.content.Context
import androidx.room.Room
import com.example.core.security.KeystoreSqlCipherManager
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
    fun provideVanijyaDatabase(
        @ApplicationContext context: Context
    ): VanijyaDatabase {
        val builder = Room.databaseBuilder(
            context,
            VanijyaDatabase::class.java,
            VanijyaDatabase.DATABASE_NAME
        )
        try {
            val factory = KeystoreSqlCipherManager.createEncryptedOpenHelperFactory(context)
            builder.openHelperFactory(factory)
        } catch (_: UnsatisfiedLinkError) {
            // In local JVM unit tests where native libsqlcipher.so is not loaded, fall back to standard SQLite
        }
        return builder
            .addMigrations(VanijyaDatabase.MIGRATION_1_2, VanijyaDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigrationOnDowngrade(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideCoreDao(database: VanijyaDatabase): CoreDao = database.coreDao()

    @Provides
    @Singleton
    fun provideStep2Dao(database: VanijyaDatabase): Step2Dao = database.step2Dao()

    @Provides
    @Singleton
    fun provideStep3ADao(database: VanijyaDatabase): Step3ADao = database.step3ADao()
}
