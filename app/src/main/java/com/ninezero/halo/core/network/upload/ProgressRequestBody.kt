package com.ninezero.halo.core.network.upload

import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer

/** 전송 바이트 콜백 RequestBody */
class ProgressRequestBody(
    private val delegate: RequestBody,
    private val onBytesWritten: (delta: Long) -> Unit
) : RequestBody() {
    override fun contentType() = delegate.contentType()
    override fun contentLength() = delegate.contentLength()
    override fun isOneShot() = true  // 중복 카운트 방지
    override fun writeTo(sink: BufferedSink) {
        val counting = object : ForwardingSink(sink) {
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                onBytesWritten(byteCount)
            }
        }
        val buffered = counting.buffer()
        delegate.writeTo(buffered)
        buffered.flush()
    }
}
