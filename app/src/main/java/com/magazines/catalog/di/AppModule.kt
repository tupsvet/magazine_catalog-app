package com.magazines.catalog.di

import com.magazines.catalog.domain.repository.AuthRepository
import com.magazines.catalog.domain.usecase.auth.LoginUseCase
import com.magazines.catalog.domain.usecase.auth.LogoutUseCase
import com.magazines.catalog.domain.usecase.auth.RegisterUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideLoginUseCase(authRepository: AuthRepository): LoginUseCase =
        LoginUseCase(authRepository)

    @Provides
    fun provideRegisterUseCase(authRepository: AuthRepository): RegisterUseCase =
        RegisterUseCase(authRepository)

    @Provides
    fun provideLogoutUseCase(authRepository: AuthRepository): LogoutUseCase =
        LogoutUseCase(authRepository)
}
