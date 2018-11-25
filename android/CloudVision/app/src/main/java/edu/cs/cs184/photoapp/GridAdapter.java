package edu.cs.cs184.photoapp;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;


public class GridAdapter extends BaseAdapter {
    private Context mContext;
    ImageRetriever mImageRetriever;
    ArrayList<Uri> localPics = new ArrayList<>();
    final static int MAX_DIMENSION = 1200;
    final static int NUM_STOCK_PICS = 30;

    public GridAdapter(Context c){
        mContext = c;
    }

    public int getCount() {
        return localPics.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(400, 400));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setVisibility(View.INVISIBLE);
        if(position < localPics.size()) {
            Log.d("Picture Displaying: ", localPics.get(position).toString());

            Picasso.get().load(localPics.get(position)).centerCrop().resize(200, 200).into(imageView);

            imageView.setVisibility(View.VISIBLE);
        }
        return imageView;
    }

    public void addImage(Uri uri){
        localPics.add(0, uri);
        notifyDataSetInvalidated();
    }


    // gets list of pics, adds those with (height<=1024) to db, then sorts by rating of 0 (returns all pics)
    public void populate(){
        mImageRetriever = ImageRetriever.getInstance(mContext );
        mImageRetriever.listImagesRequest(new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                try {
                    for(int i=0; i<NUM_STOCK_PICS; i++){
                        int randIndex = new Random().nextInt(response.length());
                        JSONObject item = response.getJSONObject(randIndex);

                        String src = "https://picsum.photos/"+MAX_DIMENSION+"/"+MAX_DIMENSION+"?image="+item.getInt("id");
                        Log.d("Stockpix", "Adding: "+src);
                        Uri uri = Uri.parse(src);
                        localPics.add(uri);
                        notifyDataSetInvalidated();

                    }
                } catch (Exception e) {
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", error.toString());
            }
        });


    }

    public Uri getEntry(int pos){
        return localPics.get(pos);
    }

    public void restore(int size){

    }



}
