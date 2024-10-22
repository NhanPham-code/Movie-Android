package com.example.ojtbadaassignment14.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class Base64Helper {

    // Chuyển đổi Bitmap thành Base64
    public String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // chuyển đổi Base64 thành Bitmap
    public Bitmap convertBase64ToBitmap(String avatarBase64) {
        if (avatarBase64 != null && !avatarBase64.isEmpty()) {
            byte[] decodedString = Base64.decode(avatarBase64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }
        return null;
    }

}
