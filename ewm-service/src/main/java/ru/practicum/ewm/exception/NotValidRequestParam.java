package ru.practicum.ewm.exception;

public class NotValidRequestParam extends RuntimeException {
    public NotValidRequestParam(String message) {
        super(message);
    }
}
