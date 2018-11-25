package edu.cs.cs184.photoapp;

/**
 * Created by Yi Ding 11/01.
 *
 * Much of this is based on the Volley documentation -- Android's REST api library.
 * https://developer.android.com/training/volley/
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.support.design.widget.FloatingActionButton;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

public class ImageRetriever {
    public static final String REQ_LIST_IMG_METADATA = "https://picsum.photos/list";

    private static ImageRetriever mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mContext;

    private ImageRetriever(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });


    }

    public static synchronized ImageRetriever getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ImageRetriever(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }


    /**
     * This function will list all available images from Lorem Picsum and calls
     * successListener with the json object if successful
     */
    public void listImagesRequest(Response.Listener<JSONArray> successListener,
                                  Response.ErrorListener errorListener) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                REQ_LIST_IMG_METADATA,
                null,
                successListener,
                errorListener);


        mInstance.addToRequestQueue(jsonArrayRequest);
    }

}
