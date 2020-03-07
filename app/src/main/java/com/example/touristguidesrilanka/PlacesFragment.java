package com.example.touristguidesrilanka;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.touristguidesrilanka.Adapter.PlaceAdapter;
import com.example.touristguidesrilanka.Pojo.Place;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PlacesFragment extends Fragment {
    List<Place> listPlace;
    ListView listViewPlaces;

    private View fragmentView;
    private PlacesFragment fragment;

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

    public static final Double[] placesLoicationLatitude = new Double[] {
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
            0.0};

    public static final Integer[] placeImages = {  R.drawable.nallur ,R.drawable.vallipuram,
            R.drawable.sanithy,R.drawable.nainativu,R.drawable.puranavihara , R.drawable.publiclibrary, R.drawable.fort};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragment = this;
        fragmentView = inflater.inflate(R.layout.fragment_places, container, false);

        initializeUI(fragmentView);

        return fragmentView;
    }


    private void initializeUI(View fragmentView){

        listPlace = new ArrayList<>();
        for (int i = 0; i < placesName.length; i++) {
            Place place = new Place(placeImages[i], placesName[i], placesAddress[i]);
            listPlace.add(place);
        }

        listViewPlaces = fragmentView.findViewById(R.id.listView_places);
        PlaceAdapter adapter = new PlaceAdapter(getActivity(), listPlace);
        listViewPlaces.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        listViewPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               /* Bitmap bmp = BitmapFactory.decodeResource(getResources(),listPlace.get(position).getImgPlace());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();*/

                Intent intent = new Intent(getActivity(),PlaceActivity.class);
                intent.putExtra("PLACE_NAME",listPlace.get(position).getPlaceName());
                intent.putExtra("PLACE_ADDRESS",String.valueOf(listPlace.get(position).getPlaceAddress()));

                //intent.putExtra("PLACE_IMAGE", byteArray);
                intent.putExtra("PLACE_POSITION", String.valueOf(position));

                startActivity(intent);
            }
        });

    }

}
