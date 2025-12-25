package com.app.notes.core.common

// यह एक Generic Wrapper है जो Data loading states को handle करेगा।
// यह Data Layer से UI Layer तक Data carry करने के लिए use होगा।
sealed class Result<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Result<T>(data)
    class Error<T>(message: String, data: T? = null) : Result<T>(data, message)
    class Loading<T>(data: T? = null) : Result<T>(data)
}