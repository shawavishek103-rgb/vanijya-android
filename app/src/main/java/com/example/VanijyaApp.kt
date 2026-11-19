package com.example

import android.app.Application
import com.example.core.database.CoreRepository
import com.example.core.database.DatabaseModule

class VanijyaApp : Application() {

    val coreRepository: CoreRepository by lazy {
        val db = DatabaseModule.provideVanijyaDatabase(applicationContext)
        val coreDao = DatabaseModule.provideCoreDao(db)
        val step2Dao = DatabaseModule.provideStep2Dao(db)
        val step3ADao = DatabaseModule.provideStep3ADao(db)
        CoreRepository(coreDao, applicationContext, step2Dao, step3ADao)
    }
}
