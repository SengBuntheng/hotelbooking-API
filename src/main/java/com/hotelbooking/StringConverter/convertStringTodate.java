package com.hotelbooking.StringConverter;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.sql.Date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
@Slf4j
@ToString
public class convertStringTodate {

   public Date convertStringTodate(String date) {

       SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
       try {
           java.util.Date parsed = format.parse(date);
           return new Date(parsed.getTime());
       } catch (ParseException e) {
           log.error("Failed to parse date string: {}", date, e);
           return null;
       }
   }
}
