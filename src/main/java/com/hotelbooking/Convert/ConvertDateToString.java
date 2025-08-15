package com.hotelbooking.Convert;

import java.security.PublicKey;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ConvertDateToString {

    public Date ConvertDate(String dt){
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
             return new Date(formatter.parse(dt).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
