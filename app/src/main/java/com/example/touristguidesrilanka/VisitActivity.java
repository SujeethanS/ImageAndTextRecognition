package com.example.touristguidesrilanka;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.touristguidesrilanka.Adapter.PlaceAdapter;
import com.example.touristguidesrilanka.Pojo.Place;

import java.util.ArrayList;
import java.util.List;

public class VisitActivity extends AppCompatActivity {

    List<Place> listPlace;
    ListView listViewPlaces;

    public static String[] placesName = new String[] { "Nallur Kovil" , "Vallipuram Kovil", "Selvasanithy",
            "Nainativu naagapoosani", "Purana vihara","Public Library","Jaffna Fort"};

    public static final String[] placesAddress = new String[] {
            "Nallur, Jaffna",
            "Thunnalai, Pointpedro",
            "Thondaimanaaru, Vadamaradchy",
            "Nainatheevu",
            "Nainatheevu",
            "Near Jaffna Town",
            "Near Jaffna Town" };

   /* public static final Double[] placesLoicationLatitude = new Double[] {
            9.674469, //9.674469, 80.029530
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0};

    public static final Double[] placesLoicationLongitude = new Double[] {
            80.029530,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0};*/

    public static final Integer[] placeImages = {  R.drawable.nallur ,R.drawable.vallipuram,
            R.drawable.sanithy,R.drawable.nainativu,R.drawable.puranavihara , R.drawable.publiclibrary, R.drawable.fort};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit);

        initializeUI();
    }

    private void initializeUI(){

        listPlace = new ArrayList<>();
        for (int i = 0; i < placesName.length; i++) {
            Place place = new Place(placeImages[i], placesName[i], placesAddress[i]);
            listPlace.add(place);
        }

        listViewPlaces = findViewById(R.id.listView_places);
        PlaceAdapter adapter = new PlaceAdapter(this, listPlace);
        listViewPlaces.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        listViewPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(VisitActivity.this,PlaceActivity.class);
                intent.putExtra("PLACE_NAME",listPlace.get(position).getPlaceName());
                intent.putExtra("PLACE_ADDRESS",String.valueOf(listPlace.get(position).getPlaceAddress()));

                intent.putExtra("PLACE_POSITION", String.valueOf(position));

                startActivity(intent);
            }
        });

    }
}
