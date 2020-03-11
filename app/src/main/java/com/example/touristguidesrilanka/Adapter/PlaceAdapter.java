package com.example.touristguidesrilanka.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.touristguidesrilanka.Pojo.Place;
import com.example.touristguidesrilanka.R;

import java.util.List;

public class PlaceAdapter extends BaseAdapter {
    Context context;
    private List<Place> listPlace;

    public PlaceAdapter(Context context, List<Place> items) {
        this.context = context;
        this.listPlace = items;
    }

    /*private view holder class*/
    private class ViewHolder {
        ImageView imageViewPlace;//,nextImage;
        TextView txtName;
        TextView txtAddress;
    }

    @SuppressLint("InflateParams")
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        LayoutInflater mInflater = (LayoutInflater)
                context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.place_single, null);
            holder = new ViewHolder();
            holder.txtName =  convertView.findViewById(R.id.txtView_place_name);
            holder.txtAddress =  convertView.findViewById(R.id.txtView_place_address);
            holder.imageViewPlace =  convertView.findViewById(R.id.imgView_place);
            //holder.nextImage = convertView.findViewById(R.id.setup_next_icon_imageButton);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        Place place = (Place) getItem(position);

        holder.txtName.setText(place.getPlaceName());
        holder.txtAddress.setText(place.getPlaceAddress());
        holder.imageViewPlace.setImageResource(place.getImgPlace());
        //holder.nextImage.setImageResource(rowItem.getNextImageId());

        return convertView;
    }

    @Override
    public int getCount() {
        return listPlace.size();
    }

    @Override
    public Object getItem(int position) {
        return listPlace.get(position);
    }

    @Override
    public long getItemId(int position) {
        return listPlace.indexOf(getItem(position));
    }
}
