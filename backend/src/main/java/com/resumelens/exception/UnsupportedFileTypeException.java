package com.resumelens.exception;

public class UnsupportedFileTypeException extends RuntimeException {
    public UnsupportedFileTypeException(String extension) {
        super("Unsupported file type: ." + extension + ". Only PDF and DOCX are accepted.");
    }
}