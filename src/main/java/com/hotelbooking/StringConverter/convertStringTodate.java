package com.hotelbooking.StringConverter;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
@Slf4j
@ToString
public class convertStringTodate {

    public String convertDateToString(Date dt, String pattern) {
        DateFormat df = new SimpleDateFormat(pattern);
        String dateToString = df.format(dt);
        return dateToString;
    }
}
