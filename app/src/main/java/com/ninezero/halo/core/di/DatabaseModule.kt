package com.ninezero.halo.core.di

import android.content.Context
import androidx.room.Room
import com.ninezero.halo.core.database.HaloDatabase
import com.ninezero.halo.core.database.dao.CachedChatRoomDao
import com.ninezero.halo.core.database.dao.CachedMessageDao
import com.ninezero.halo.core.database.dao.PendingChatMessageDao
import com.ninezero.halo.core.database.dao.PendingCommentDao
import com.ninezero.halo.core.database.dao.PendingPostDao
import com.ninezero.halo.core.database.dao.PostDraftDao
import com.ninezero.halo.core.util.Constants
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): HaloDatabase {
        return Room.databaseBuilder(
            context,
            HaloDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun providePendingCommentDao(database: HaloDatabase): PendingCommentDao {
        return database.pendingCommentDao()
    }

    @Provides
    @Singleton
    fun providePendingPostDao(database: HaloDatabase): PendingPostDao {
        return database.pendingPostDao()
    }

    @Provides
    @Singleton
    fun providePostDraftDao(database: HaloDatabase): PostDraftDao {
        return database.postDraftDao()
    }

    @Provides
    @Singleton
    fun providePendingChatMessageDao(database: HaloDatabase): PendingChatMessageDao {
        return database.pendingChatMessageDao()
    }

    @Provides
    @Singleton
    fun provideCachedMessageDao(database: HaloDatabase): CachedMessageDao {
        return database.cachedMessageDao()
    }

    @Provides
    @Singleton
    fun provideCachedChatRoomDao(database: HaloDatabase): CachedChatRoomDao {
        return database.cachedChatRoomDao()
    }
}
