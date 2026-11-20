package com.ninezero.halo.core.util


private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

fun String.isValidEmail(): Boolean = this.matches(EMAIL_REGEX)

fun String.stripPasswordWhitespace(): String = filterNot { it.isWhitespace() }

fun String.isValidPassword(): Boolean {  // 8~20자, 영문·숫자·특수문자
    return this.length in Constants.Auth.MIN_PASSWORD_LENGTH..Constants.Auth.MAX_PASSWORD_LENGTH &&
            this.none { it.isWhitespace() } &&
            this.any { it.isDigit() } &&
            this.any { it.isLetter() } &&
            this.any { !it.isLetterOrDigit() }
}
