package com.magazines.catalog.di

import com.magazines.catalog.data.repository.AuthRepositoryImpl
import com.magazines.catalog.data.repository.CategoryRepositoryImpl
import com.magazines.catalog.data.repository.MagazineRepositoryImpl
import com.magazines.catalog.domain.repository.AuthRepository
import com.magazines.catalog.domain.repository.CategoryRepository
import com.magazines.catalog.domain.repository.MagazineRepository
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
}
