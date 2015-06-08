package com.marcosedo.yambu.app;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.marcosedo.yambu.R;

import java.util.ArrayList;

public class ListaCommentsCustomAdapter extends ArrayAdapter<Comment> {

    Context context;
    ViewHolder holder;

    public ListaCommentsCustomAdapter(Context context, int resourceId, ArrayList<Comment> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    /*private view holder class*/
    public class ViewHolder {
        ImageView img;
        TextView tvUsername;
        TextView msg;
        TextView timestamp;
        Button btn_rispond;
        }

    public View getView(int position, View convertView, ViewGroup parent) {

        Comment rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.linea_comments, null);
            holder = new ViewHolder();
            holder.img = (ImageView) convertView.findViewById(R.id.ivImg);
            holder.tvUsername = (TextView) convertView.findViewById(R.id.tvUsername);
            holder.msg = (TextView) convertView.findViewById(R.id.tvMessage);
            holder.btn_rispond = (Button) convertView.findViewById(R.id.btn_rispond);
            holder.timestamp = (TextView) convertView.findViewById((R.id.tvTimestamp));

            convertView.setTag(holder);
        } else{
            holder = (ViewHolder) convertView.getTag();
        }

        byte[] imageByteArray = rowItem.getImage();

        if (imageByteArray != null){
            Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            holder.img.setImageBitmap(imageBitmap);
        }
        else holder.img.setImageBitmap(null);

        holder.tvUsername.setText(rowItem.getUsername());
        holder.msg.setText(rowItem.getTxt());
        holder.timestamp.setText(rowItem.getTimestamp());

        return convertView;
    }
}