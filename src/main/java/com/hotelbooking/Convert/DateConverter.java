package com.hotelbooking.Convert;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateConverter {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Date convertStringToDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            LocalDate localDate = LocalDate.parse(dateString.trim(), DEFAULT_FORMATTER);
            return Date.valueOf(localDate);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected: yyyy-MM-dd, but got: " + dateString, e);
        }
    }

    public String convertDateToString(Date date) {
        if (date == null) {
            return null;
        }
        return date.toLocalDate().format(DEFAULT_FORMATTER);
    }

    public Date convertStringToDate(String dateString, String pattern) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            LocalDate localDate = LocalDate.parse(dateString.trim(), formatter);
            return Date.valueOf(localDate);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected: " + pattern + ", but got: " + dateString, e);
        }
    }
}