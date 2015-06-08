package com.marcosedo.yambu.app;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.marcosedo.yambu.R;

import java.util.ArrayList;

public class ListaUsuariosCustomAdapter extends ArrayAdapter<Usuario> {

	Context context;
	ViewHolder holder;

	public ListaUsuariosCustomAdapter(Context context, int resourceId, ArrayList<Usuario> items) {
		super(context, resourceId, items);
		this.context = context;
	}
	     
	/*private view holder class*/
	public class ViewHolder {
		ImageView img;
		TextView tvUsername;
		TextView tvCompleteName;
        TextView tvEmail;
		ImageView ivChecked;
	}
	     
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Usuario rowItem = getItem(position);
	         
		LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	     
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.linea_lista_usuarios, null);
			holder = new ViewHolder();
			holder.img = (ImageView) convertView.findViewById(R.id.ivImg);
			holder.tvUsername = (TextView) convertView.findViewById(R.id.tvUsername);
			holder.tvCompleteName = (TextView) convertView.findViewById(R.id.tvCompleteName);
            holder.tvEmail = (TextView) convertView.findViewById(R.id.tvEmail);
           	holder.ivChecked = (ImageView) convertView.findViewById(R.id.ivChecked);
			
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
		holder.tvCompleteName.setText(rowItem.getFirstname()+" "+rowItem.getLastName());

        holder.tvEmail.setText(rowItem.getEmail());
				
		if (rowItem.isChecked()){
			holder.ivChecked.setImageResource(R.drawable.btn_check_on);
		}else{
			holder.ivChecked.setImageResource(R.drawable.btn_check_on_disable);
		}
			
			
		//asignamos el onclicklistener a la vista
		  //holder.checkbox.setOnLongClickListener(listener);
		
		return convertView;
	}
}