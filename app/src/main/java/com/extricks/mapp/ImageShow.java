package com.extricks.mapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.FileNotFoundException;

public class ImageShow extends AppCompatActivity {

    ImageView imshow;
    TessBaseAPI tessBaseAPI;
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_show);

        imshow = findViewById(R.id.imageView);


        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getApplicationContext().openFileInput("myImage"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        imshow.setImageBitmap(bitmap);
        String Num = getText(bitmap);
        Num = Num.replaceAll("[^0-9]", "");
        Toast.makeText(this,Num , Toast.LENGTH_LONG).show();

    }

    private String getText(Bitmap bitmap){
        try{
            tessBaseAPI = new TessBaseAPI();
        }catch (Exception e){
            Log.e("imshow", e.getMessage());
        }
        tessBaseAPI.init(DATA_PATH,"eng");
        tessBaseAPI.setImage(bitmap);
        String retStr = "No result";
        try{
            retStr = tessBaseAPI.getUTF8Text();
        }catch (Exception e){
            Log.e("imshow", e.getMessage());
        }
        tessBaseAPI.end();
        return retStr;
    }
}
