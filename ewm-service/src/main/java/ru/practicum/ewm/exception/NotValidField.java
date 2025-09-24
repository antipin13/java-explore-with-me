package ru.practicum.ewm.exception;

public class NotValidField extends RuntimeException {
    public NotValidField(String message) {
        super(message);
    }
}
