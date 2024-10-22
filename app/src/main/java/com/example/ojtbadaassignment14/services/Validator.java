package com.example.ojtbadaassignment14.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Validator {

    public boolean isValidDate(String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        simpleDateFormat.setLenient(false);
        try {
            simpleDateFormat.parse(date);
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }


    public boolean isValidEmail(String email) {
        String emailPattern = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$";
        return email.matches(emailPattern);
    }
}
