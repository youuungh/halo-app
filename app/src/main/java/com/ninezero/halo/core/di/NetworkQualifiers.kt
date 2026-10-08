package com.ninezero.halo.core.di

import javax.inject.Qualifier

/** refresh 전용 네트워크 qualifier */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshAuth

/** 스토리지 업로드 전용 네트워크 qualifier */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DirectUpload  // 로그인 토큰 없음
