package com.example.touristguidesrilanka;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class PlaceActivity extends AppCompatActivity {

    private TextView placeName,placeDetails,placeAddress;
    private ImageView imageViewPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        placeName = findViewById(R.id.place_name);
        placeDetails = findViewById(R.id.place_details);
        placeAddress = findViewById(R.id.address);

        imageViewPlace = findViewById(R.id.img_place);

    }

    @Override
    protected void onStart(){
        super.onStart();

        String place_name = getIntent().getStringExtra("PLACE_NAME");
        String place_address = getIntent().getStringExtra("PLACE_ADDRESS");
        String position = getIntent().getStringExtra("PLACE_POSITION");

        //Bundle extras = getIntent().getExtras();
        //byte[] byteArray = extras.getByteArray("PLACE_IMAGE");

        //Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        placeName.setText(place_name);
        placeAddress.setText(place_address);
        //imageViewPlace.setImageBitmap(bmp);
        imageViewPlace.setImageResource(VisitActivity.placeImages[Integer.parseInt(position)]);

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        finish();
    }
}
