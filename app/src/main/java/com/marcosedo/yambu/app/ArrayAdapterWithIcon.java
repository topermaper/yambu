package com.marcosedo.yambu.app;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ArrayAdapterWithIcon extends ArrayAdapter<String> {

private List<Integer> images;
OnItemClickListener listener;

public ArrayAdapterWithIcon(Context context, List<String> items, List<Integer> images,OnItemClickListener listener) {
    super(context, android.R.layout.select_dialog_item, items);
    this.images = images;
    this.listener = listener;
}

public ArrayAdapterWithIcon(Context context, String[] items, Integer[] images,OnItemClickListener listener) {
    super(context, android.R.layout.select_dialog_item, items);
    this.images = Arrays.asList(images);
    this.listener = listener;
}

@Override
public View getView(int position, View convertView, ViewGroup parent) {
    View view = super.getView(position, convertView, parent);
    
    if (listener != null){
    	ListView listView = (ListView) parent.findViewById(android.R.id.list);
    	listView.setOnItemClickListener(listener);
	}
	
	TextView textView = (TextView) view.findViewById(android.R.id.text1);
    textView.setCompoundDrawablesWithIntrinsicBounds(images.get(position), 0, 0, 0);
    textView.setCompoundDrawablePadding(
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics()));
    return view;
}

}