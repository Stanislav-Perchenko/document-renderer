package com.webssa.library.utils;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;

/**
 * Created by stanislav.perchenko on 4/17/2019
 */
public class DigestOutputStream extends OutputStream {

    private final MessageDigest digest;
    private final OutputStream os;

    public DigestOutputStream(@NonNull OutputStream os, @Nullable MessageDigest digest) {
        this.os = os;
        this.digest = digest;
    }

    public byte[] getDigestBytes() {
        return (digest == null) ? null : digest.digest();
    }

    public String getDigestBase64() {
        return (digest == null) ? null : Base64.encodeToString(getDigestBytes(), Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
    }

    @Override
    public void write(int b) throws IOException {
        if (digest != null) digest.update((byte) b);
        os.write(b);
    }

    @Override
    public void write(@NonNull byte[] b) throws IOException {
        if (digest != null) digest.update(b, 0, b.length);
        os.write(b);
    }

    @Override
    public void write(@NonNull byte[] b, int off, int len) throws IOException {
        if (digest != null) digest.update(b, off, len);
        os.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        os.flush();
    }

    @Override
    public void close() throws IOException {
        os.close();
    }



    @Override
    public int hashCode() {
        return os.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return os.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @NonNull
    @Override
    public String toString() {
        String hash = (digest == null) ? "(null)" : Base64.encodeToString(digest.digest(), Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
        return String.format("{digest=%s, super=%s}", hash, super.toString());
    }
}
