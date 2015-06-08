package com.marcosedo.yambu.app;


import com.marcosedo.yambu.R;
import java.util.ArrayList;
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

public class ListaDispositivosCustomAdapter extends ArrayAdapter<Dispositivo> {
		 
	Context context;
	ViewHolder holder;
	 
	public ListaDispositivosCustomAdapter(Context context, int resourceId, ArrayList<Dispositivo> items) {
		super(context, resourceId, items);
		this.context = context;
	}
	     
	/*private view holder class*/
	public class ViewHolder {
		ImageView img;
		TextView tvDevId;
		TextView tvNick;
		ImageView ivChecked;
	}
	     
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Dispositivo rowItem = getItem(position);
	         
		LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	     
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.linea_dispositivos, null);
			holder = new ViewHolder();
			holder.img = (ImageView) convertView.findViewById(R.id.ivImg);
			holder.tvDevId = (TextView) convertView.findViewById(R.id.tvDevid);
			holder.tvNick = (TextView) convertView.findViewById(R.id.tvNick);
			holder.ivChecked = (ImageView) convertView.findViewById(R.id.ivChecked);
			
			convertView.setTag(holder);
		} else{
			holder = (ViewHolder) convertView.getTag();
		}
	         
		byte[] fotoByteArray = rowItem.getFotoPerfil();
		
		if (fotoByteArray != null){
			Bitmap fotoBitmap = BitmapFactory.decodeByteArray(fotoByteArray, 0, fotoByteArray.length);
			holder.img.setImageBitmap(fotoBitmap);
		}
		else holder.img.setImageBitmap(null);
		 
		holder.tvDevId.setText(rowItem.getDevId());
		holder.tvNick.setText(rowItem.getNick());
				
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