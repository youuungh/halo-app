package com.ninezero.halo.feature.feed.di

import com.ninezero.halo.feature.feed.data.cache.CommentStateStore
import com.ninezero.halo.feature.feed.data.cache.CommentStateStoreImpl
import com.ninezero.halo.feature.feed.data.cache.PostStateStore
import com.ninezero.halo.feature.feed.data.cache.PostStateStoreImpl
import com.ninezero.halo.feature.feed.data.remote.api.BlockApi
import com.ninezero.halo.feature.feed.data.remote.api.BookmarkApi
import com.ninezero.halo.feature.feed.data.remote.api.CommentApi
import com.ninezero.halo.feature.feed.data.remote.api.FeedApi
import com.ninezero.halo.feature.feed.data.remote.api.HiddenPostApi
import com.ninezero.halo.feature.feed.data.remote.api.LikeApi
import com.ninezero.halo.feature.feed.data.remote.api.MediaDraftApi
import com.ninezero.halo.feature.feed.data.remote.api.PostApi
import com.ninezero.halo.feature.feed.data.remote.api.ReportApi
import com.ninezero.halo.feature.feed.data.repository.BlockRepositoryImpl
import com.ninezero.halo.feature.feed.data.repository.BookmarkRepositoryImpl
import com.ninezero.halo.feature.feed.data.repository.CommentRepositoryImpl
import com.ninezero.halo.feature.feed.data.repository.FeedRepositoryImpl
import com.ninezero.halo.feature.feed.data.repository.HiddenPostRepositoryImpl
import com.ninezero.halo.feature.feed.data.repository.LikeRepositoryImpl
import com.ninezero.halo.feature.feed.data.repository.MediaDraftRepositoryImpl
import com.ninezero.halo.feature.feed.data.repository.PendingCommentRepositoryImpl
import com.ninezero.halo.feature.feed.data.repository.PendingPostRepositoryImpl
import com.ninezero.halo.feature.feed.data.repository.PostDraftRepositoryImpl
import com.ninezero.halo.feature.feed.data.repository.PostRepositoryImpl
import com.ninezero.halo.feature.feed.data.repository.ReportRepositoryImpl
import com.ninezero.halo.feature.feed.data.worker.CommentUploadResultManager
import com.ninezero.halo.feature.feed.data.worker.PostUploadResultManager
import com.ninezero.halo.feature.feed.domain.repository.BlockRepository
import com.ninezero.halo.feature.feed.domain.repository.BookmarkRepository
import com.ninezero.halo.feature.feed.domain.repository.CommentRepository
import com.ninezero.halo.feature.feed.domain.repository.CommentUploadEventRepository
import com.ninezero.halo.feature.feed.domain.repository.FeedRepository
import com.ninezero.halo.feature.feed.domain.repository.HiddenPostRepository
import com.ninezero.halo.feature.feed.domain.repository.LikeRepository
import com.ninezero.halo.feature.feed.domain.repository.MediaDraftRepository
import com.ninezero.halo.feature.feed.domain.repository.PendingCommentRepository
import com.ninezero.halo.feature.feed.domain.repository.PendingPostRepository
import com.ninezero.halo.feature.feed.domain.repository.PostDraftRepository
import com.ninezero.halo.feature.feed.domain.repository.PostRepository
import com.ninezero.halo.feature.feed.domain.repository.PostUploadEventRepository
import com.ninezero.halo.feature.feed.domain.repository.ReportRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FeedModule {

    @Binds
    @Singleton
    abstract fun bindFeedRepository(
        feedRepositoryImpl: FeedRepositoryImpl
    ): FeedRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        postRepositoryImpl: PostRepositoryImpl
    ): PostRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(
        commentRepositoryImpl: CommentRepositoryImpl
    ): CommentRepository

    @Binds
    @Singleton
    abstract fun bindLikeRepository(
        likeRepositoryImpl: LikeRepositoryImpl
    ): LikeRepository

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(
        bookmarkRepositoryImpl: BookmarkRepositoryImpl
    ): BookmarkRepository

    @Binds
    @Singleton
    abstract fun bindBlockRepository(
        blockRepositoryImpl: BlockRepositoryImpl
    ): BlockRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(
        reportRepositoryImpl: ReportRepositoryImpl
    ): ReportRepository

    @Binds
    @Singleton
    abstract fun bindHiddenPostRepository(
        hiddenPostRepositoryImpl: HiddenPostRepositoryImpl
    ): HiddenPostRepository

    @Binds
    @Singleton
    abstract fun bindPendingCommentRepository(
        pendingCommentRepositoryImpl: PendingCommentRepositoryImpl
    ): PendingCommentRepository

    @Binds
    @Singleton
    abstract fun bindPendingPostRepository(
        pendingPostRepositoryImpl: PendingPostRepositoryImpl
    ): PendingPostRepository

    @Binds
    @Singleton
    abstract fun bindPostDraftRepository(
        postDraftRepositoryImpl: PostDraftRepositoryImpl
    ): PostDraftRepository

    @Binds
    @Singleton
    abstract fun bindMediaDraftRepository(
        mediaDraftRepositoryImpl: MediaDraftRepositoryImpl
    ): MediaDraftRepository

    @Binds
    @Singleton
    abstract fun bindPostUploadEventRepository(
        postUploadResultManager: PostUploadResultManager
    ): PostUploadEventRepository

    @Binds
    @Singleton
    abstract fun bindCommentUploadEventRepository(
        commentUploadResultManager: CommentUploadResultManager
    ): CommentUploadEventRepository

    @Binds
    @Singleton
    abstract fun bindPostStateStore(
        postStateStoreImpl: PostStateStoreImpl
    ): PostStateStore

    @Binds
    @Singleton
    abstract fun bindCommentStateStore(
        commentStateStoreImpl: CommentStateStoreImpl
    ): CommentStateStore

    companion object {
        @Provides
        @Singleton
        fun provideFeedApi(retrofit: Retrofit): FeedApi {
            return retrofit.create(FeedApi::class.java)
        }

        @Provides
        @Singleton
        fun providePostApi(retrofit: Retrofit): PostApi {
            return retrofit.create(PostApi::class.java)
        }

        @Provides
        @Singleton
        fun provideCommentApi(retrofit: Retrofit): CommentApi {
            return retrofit.create(CommentApi::class.java)
        }

        @Provides
        @Singleton
        fun provideMediaDraftApi(retrofit: Retrofit): MediaDraftApi {
            return retrofit.create(MediaDraftApi::class.java)
        }

        @Provides
        @Singleton
        fun provideLikeApi(retrofit: Retrofit): LikeApi {
            return retrofit.create(LikeApi::class.java)
        }

        @Provides
        @Singleton
        fun provideBookmarkApi(retrofit: Retrofit): BookmarkApi {
            return retrofit.create(BookmarkApi::class.java)
        }

        @Provides
        @Singleton
        fun provideBlockApi(retrofit: Retrofit): BlockApi {
            return retrofit.create(BlockApi::class.java)
        }

        @Provides
        @Singleton
        fun provideHiddenPostApi(retrofit: Retrofit): HiddenPostApi {
            return retrofit.create(HiddenPostApi::class.java)
        }

        @Provides
        @Singleton
        fun provideReportApi(retrofit: Retrofit): ReportApi {
            return retrofit.create(ReportApi::class.java)
        }
    }
}
