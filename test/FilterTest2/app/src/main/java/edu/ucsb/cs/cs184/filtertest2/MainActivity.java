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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("NativeImageProcessor");

    }

    //save sub filters to a map so we don't compound filters when adding the same type of subfilter
    public Map<String,ArrayList<SubFilter>> filterMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filterMap = new HashMap<>();

//todo: potentially make these into an array, or something else more elegant


        final ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageResource(R.drawable.dsc_0171);
        SeekBar brightnessSlider = (SeekBar) findViewById(R.id.seekBar1);
        final TextView brightnessLabel = (TextView) findViewById(R.id.textView1);
        brightnessLabel.setText("Brightness: 0");
        brightnessSlider.setMax(100);
        brightnessSlider.setProgress(50);

        SeekBar contrastSlider = (SeekBar) findViewById(R.id.seekBar2);
        final TextView contrastLabel= (TextView) findViewById(R.id.textView2);
        contrastLabel.setText("Contrast: 0");
        contrastSlider.setMax(100);
        contrastSlider.setProgress(50);

        SeekBar saturationSlider = (SeekBar) findViewById(R.id.seekBar3);
        final TextView saturationLabel = (TextView) findViewById(R.id.textView3);
        saturationLabel.setText("Saturation: 0");
        saturationSlider.setMax(100);
        saturationSlider.setProgress(50);


        final Bitmap mBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.dsc_0171);

        final Button button = (Button) findViewById(R.id.button);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("bt","button pressed");

                if(filterMap.containsKey("night"))
                     removeFromFilterMap("night");
                else addToFilterMap("night",(ArrayList) SampleFilters.getNightWhisperFilter().getSubFilters());

                Bitmap currentBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888,true);
                imageView.setImageBitmap(getFilter().processFilter(currentBitmap));
            }
        });

        final Button button1 = (Button) findViewById(R.id.button1);

        button1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction()==MotionEvent.ACTION_UP)
                {
                    Bitmap currentBitmap = mBitmap.copy( Bitmap.Config.ARGB_8888,true);
                    imageView.setImageBitmap(getFilter().processFilter(currentBitmap));
                }
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN)
                {
                    imageView.setImageBitmap(mBitmap);

                }
                return true;
            }


        });


        brightnessSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //todo: scale down image
                int brightness = i-50;
                Log.d("ad","brightness:" + brightness);
                brightnessLabel.setText("Brightness: " + brightness);
                ArrayList<SubFilter> a = new ArrayList<>();
                a.add(new BrightnessSubFilter(brightness));

                addToFilterMap("brightness",a);

                Bitmap currentBitmap =  mBitmap.copy(Bitmap.Config.ARGB_8888,true);
                imageView.setImageBitmap(getFilter().processFilter( currentBitmap));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //todo: full res image into imageview

            }
        });

        contrastSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //todo: scale down image


                Float contrast = .75f*((float)i)/100f + .75f;
                Log.d("ad","Contrast:" + (i-50));
                contrastLabel.setText("Contrast: " + contrast);
                ArrayList<SubFilter> a = new ArrayList<>();
                a.add(new ContrastSubFilter(contrast));

                addToFilterMap("contrast",a);

                Bitmap currentBitmap =  mBitmap.copy(Bitmap.Config.ARGB_8888,true);
                imageView.setImageBitmap(getFilter().processFilter( currentBitmap));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                //todo: full res image into imageview

            }
        });


        saturationSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //todo: scale down image

                Float saturation = .75f*((float)i)/100f + .75f;

                Log.d("ad","Contrast:" + (i-50));
                saturationLabel.setText("Saturation: " + saturation);
                ArrayList<SubFilter> a = new ArrayList<>();
                a.add(new SaturationSubFilter(saturation));

                addToFilterMap("saturation",a);

                Bitmap currentBitmap =  mBitmap.copy(Bitmap.Config.ARGB_8888,true);
                imageView.setImageBitmap(getFilter().processFilter( currentBitmap));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //todo: full res image into imageview

            }
        });



    }

    private  void removeFromFilterMap(String s)
    {
        filterMap.remove(s);
    }

    private void addToFilterMap(String s, ArrayList<SubFilter> a){
        filterMap.put(s, a);
    }

    private Filter getFilter() {
        Filter filters = new Filter();
        for( ArrayList<SubFilter> a: filterMap.values())
        filters.addSubFilters( a);
        return filters;
    }
}
