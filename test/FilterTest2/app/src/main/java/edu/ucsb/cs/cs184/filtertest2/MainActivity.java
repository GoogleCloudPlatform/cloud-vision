package edu.ucsb.cs.cs184.filtertest2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zomato.photofilters.SampleFilters;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.SubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubFilter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("NativeImageProcessor");

    }
    // determines reasonable resolutions for the mipmap in order to maximize fidelty and framerate
    final int MIPMAP_MAX_DIMENSION = 750;
    final int MIPMAP_MIN_DIMENSION = 400;
    final int MIPMAP_STEP = 80;

    private Bitmap originalBitmap;
    private Bitmap mipMap;

    // save sub filters to a map so we don't compound filters when adding the same type of subfilter
    private Map<String,ArrayList<SubFilter>> filterMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filterMap = new HashMap<>();
        originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dsc_0171);

//todo: potentially make these into an array, or something else more elegant


        final ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageResource(R.drawable.dsc_0171);

        final SeekBar brightnessSlider = (SeekBar) findViewById(R.id.seekBar1);
        final TextView brightnessLabel = (TextView) findViewById(R.id.textView1);
        brightnessLabel.setText("Brightness: 0");
        brightnessSlider.setMax(200);
        brightnessSlider.setProgress(100);

        final SeekBar contrastSlider = (SeekBar) findViewById(R.id.seekBar2);
        final TextView contrastLabel = (TextView) findViewById(R.id.textView2);
        contrastLabel.setText("Contrast: 0");
        contrastSlider.setMax(200);
        contrastSlider.setProgress(100);

        final SeekBar saturationSlider = (SeekBar) findViewById(R.id.seekBar3);
        final TextView saturationLabel = (TextView) findViewById(R.id.textView3);
        saturationLabel.setText("Saturation: 0");
        saturationSlider.setMax(200);
        saturationSlider.setProgress(100);

        //cachedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        updateMipMap();
        final Button button = (Button) findViewById(R.id.button);
        final Button button1 = (Button) findViewById(R.id.button1);
        final Button button2 = (Button) findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("bt", "button pressed");

                if(filterMap.containsKey("myfilter"))
                     removeFromFilterMap("myfilter");
                else addToFilterMap("myfilter",(ArrayList) SampleFilters.getNightWhisperFilter().getSubFilters());

                imageView.setImageBitmap(getBitmap());
            }
        });

        button1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    Bitmap currentBitmap = originalBitmap.copy( Bitmap.Config.ARGB_8888,true);
                    imageView.setImageBitmap(getFilter().processFilter(currentBitmap));
                    //imageView.setImageBitmap(getCachedBitmap());
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    imageView.setImageBitmap(originalBitmap);

                }
                return true;
            }


        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                brightnessSlider.setProgress(100);
                contrastSlider.setProgress(100);
                saturationSlider.setProgress(100);
                removeFromFilterMap("myfilter");
                imageView.setImageBitmap(originalBitmap);

            }
        });




        brightnessSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int brightness = i-100;
                Log.d("ad","brightness:" + brightness);
                brightnessLabel.setText("Brightness: " + brightness);
                ArrayList<SubFilter> a = new ArrayList<>();
                a.add(new BrightnessSubFilter(brightness));
                Filter f = new Filter();
                f.addSubFilter(a.get(0));
                addToFilterMap("brightness",a);
                imageView.setImageBitmap(getFilter().processFilter(getMipMap()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateMipMap();
                imageView.setImageBitmap(getBitmap());


            }
        });

        contrastSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Float contrast = (float) (.0f + i * .01f);
                DecimalFormat d = new DecimalFormat("0.00");
                contrastLabel.setText("Contrast: "+d.format(contrast));
                ArrayList<SubFilter> a = new ArrayList<>();
                a.add(new ContrastSubFilter(contrast));
                addToFilterMap("contrast", a);
                Filter f = new Filter();
                f.addSubFilter(a.get(0));
                imageView.setImageBitmap(getFilter().processFilter(getMipMap()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                updateMipMap();
                imageView.setImageBitmap(getBitmap());


            }
        });


        saturationSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                Float saturation = (float)(.0f + i * .01f);
                DecimalFormat d = new DecimalFormat("0.00");
                saturationLabel.setText("Saturation: " + d.format(saturation));
                ArrayList<SubFilter> a = new ArrayList<>();
                a.add(new SaturationSubFilter(saturation));
                addToFilterMap("saturation",a);
                imageView.setImageBitmap(getFilter().processFilter(getMipMap()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateMipMap();
                imageView.setImageBitmap(getBitmap());
            }
        });




    }

    private Bitmap getBitmap(){
        return getFilter().processFilter(originalBitmap.copy(Bitmap.Config.ARGB_8888,true));
    }


    private void updateMipMap(){

        mipMap = scaleBitmapDown(originalBitmap,Math.max( MIPMAP_MAX_DIMENSION-MIPMAP_STEP*getMapSize(),MIPMAP_MIN_DIMENSION));

    }



    private Bitmap getMipMap(){
        return mipMap.copy(Bitmap.Config.ARGB_8888,true);
    }

    private void addToFilterMap(String s, ArrayList<SubFilter> a){
        filterMap.put(s, a);
    }


    private  void removeFromFilterMap(String s)
    {
        filterMap.remove(s);
    }

    private Filter getFilter() {
        Filter filters = new Filter();
        for( ArrayList<SubFilter> a: filterMap.values())
        filters.addSubFilters( a);
        return filters;
    }

    private int getMapSize(){
        int result = 0;
        for(ArrayList<SubFilter> a: filterMap.values()) result += a.size();
        return  result;
    }






    // taken from mainactivity of cloudvision
    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

}
