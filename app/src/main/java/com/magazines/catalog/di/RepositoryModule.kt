package com.magazines.catalog.di

import com.magazines.catalog.data.repository.AdminRepositoryImpl
import com.magazines.catalog.data.repository.AuthRepositoryImpl
import com.magazines.catalog.data.repository.CategoryRepositoryImpl
import com.magazines.catalog.data.repository.FavoriteRepositoryImpl
import com.magazines.catalog.data.repository.IssueRepositoryImpl
import com.magazines.catalog.data.repository.MagazineRepositoryImpl
import com.magazines.catalog.data.repository.ReviewRepositoryImpl
import com.magazines.catalog.domain.repository.AdminRepository
import com.magazines.catalog.domain.repository.AuthRepository
import com.magazines.catalog.domain.repository.CategoryRepository
import com.magazines.catalog.domain.repository.FavoriteRepository
import com.magazines.catalog.domain.repository.IssueRepository
import com.magazines.catalog.domain.repository.MagazineRepository
import com.magazines.catalog.domain.repository.ReviewRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMagazineRepository(impl: MagazineRepositoryImpl): MagazineRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindIssueRepository(impl: IssueRepositoryImpl): IssueRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindAdminRepository(impl: AdminRepositoryImpl): AdminRepository
}
