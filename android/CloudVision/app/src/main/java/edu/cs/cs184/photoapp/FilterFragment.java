package edu.cs.cs184.photoapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.SubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubFilter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static edu.cs.cs184.photoapp.MainActivity.scaleBitmapDown;


// Filter fragment:

public class FilterFragment extends Fragment {
    // TODO: fix slider values for saturation. Detailed instructions included below.
    // TODO: add save functionality
    // TODO: create some interactable to view which feature(s) corresponded and what their percentages were.

    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    public static final String ARG_PARAM3 = "param3";



    private byte[] inBitmap;
    private int inIndex;
    private String inFilterName;


    // determines reasonable resolutions for the mipmap in order to maximize fidelty and framerate
    final int MIPMAP_MAX_DIMENSION = 1000;
    final int MIPMAP_MIN_DIMENSION = 400;
    final int MIPMAP_STEP = 95;

    private Bitmap originalBitmap;
    private Bitmap mipMap;
    private Bitmap tempBitmap;


    int currBrightness;
    int currContrast;
    int currSaturation;

    // save sub filters to a map so we don't compound filters when adding the same type of subfilter
    private Map<String,ArrayList<SubFilter>> filterMap;


    private ImageView imageView;

    // Required empty public constructor
    public FilterFragment() {
    }

    public static FilterFragment newInstance(byte[] param1, int param2, String param3) {
        FilterFragment fragment = new FilterFragment();
        Bundle args = new Bundle();
        args.putByteArray(ARG_PARAM1, param1);
        args.putInt(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);



        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        setRetainInstance(true);
        super.onCreate(savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_filter, container, false);


        return view;





    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){


        super.onViewCreated(view,savedInstanceState);

        filterMap = new HashMap<>();
        imageView = (ImageView) getView().findViewById(R.id.imageView);
        originalBitmap = Bitmap.createBitmap(10,10,Bitmap.Config.ARGB_8888);


        if (getArguments() != null) {
            Log.e("as", "arguments aren't null");
            inBitmap = getArguments().getByteArray(ARG_PARAM1);
            inIndex = getArguments().getInt(ARG_PARAM2);
            inFilterName = getArguments().getString(ARG_PARAM3);


            // try to decode the bitmap passed.
            // If it doesn't get passed successfully restart the app, because without the bitmap there is nothing to do.
            try {
                originalBitmap = BitmapFactory.decodeByteArray(inBitmap,0, inBitmap.length);
                imageView.setImageBitmap(originalBitmap);


            } catch(Exception e){Toast.makeText(getActivity().getApplicationContext(), "Error fetching image. Restarting...",Toast.LENGTH_LONG).show();
                Intent i = getActivity().getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage( getActivity(). getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }


            // TODO: make a dialog with the features detected on info clicked




        }

        // TODO: potentially make these into an array, or something else more elegant (only if someone wants to put in the effort)


        // TODO: Don't actually do anything for brightness (making sure there isn't confusion)
        final SeekBar brightnessSlider = (SeekBar) getView().findViewById(R.id.seekBar1);
        final TextView brightnessLabel = (TextView) getView().findViewById(R.id.textView1);
        brightnessLabel.setText("Brightness: 0");
        brightnessSlider.setMax(200);
        brightnessSlider.setProgress(100);


        // TODO: set on a nonlinear scale, but display on a linear scale. We don't want the user to be able to set the entire image gray.
        // TODO: 50 should be the lowest, 200 should be the highest, and 100 should be in the middle (where it starts). The display should still progress linearly.
        // TODO: See the slider listener code and how it handles the change and text, and change it

        final SeekBar contrastSlider = (SeekBar) getView().findViewById(R.id.seekBar2);
        final TextView contrastLabel = (TextView) getView().findViewById(R.id.textView2);
        contrastLabel.setText("Contrast: 0");
        contrastSlider.setMax(200);
        contrastSlider.setProgress(100);


        final SeekBar saturationSlider = (SeekBar) getView().findViewById(R.id.seekBar3);
        final TextView saturationLabel = (TextView) getView().findViewById(R.id.textView3);
        saturationLabel.setText("Saturation: 0");
        saturationSlider.setMax(200);
        saturationSlider.setProgress(100);

        final Button infoButton = (Button) getView().findViewById(R.id.button);
        final Button button1 = (Button) getView().findViewById(R.id.button1);
        final Button ResetButton = (Button) getView().findViewById(R.id.button2);

        final Filter mFilter = CustomFilters.getFilter(inFilterName,FilterSelectorActivity.getContext());



        addToFilterMap("myfilter",(ArrayList) mFilter.getSubFilters());
        imageView.setImageBitmap(getBitmap());

        updateMipMap();


        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(FilterSelectorActivity.getContext(),"Implement something that pops up with information on the features that matched.", Toast.LENGTH_LONG).show();
            }
        });


        button1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    //Bitmap currentBitmap = originalBitmap.copy( Bitmap.Config.ARGB_8888,true);
                    //imageView.setImageBitmap(getFilter().processFilter(currentBitmap));
                    imageView.setImageBitmap(tempBitmap);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    tempBitmap =  ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                    imageView.setImageBitmap(originalBitmap);
                    //tempBitmap = getFilter().processFilter(getBitmap());

                }
                return true;
            }


        });

        ResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                brightnessSlider.setProgress(100);
                contrastSlider.setProgress(100);
                saturationSlider.setProgress(100);
                imageView.setImageBitmap(getBitmap());

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
                currBrightness = seekBar.getProgress();


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
                currContrast = seekBar.getProgress();


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
                currSaturation = seekBar.getProgress();
            }
        });

        // todo: make sure it always saves the variables (or make sure the demo video doesn't show them resetting ;) )
        if(savedInstanceState!=null){
            brightnessSlider.setProgress( savedInstanceState.getInt("br"));
            contrastSlider.setProgress( savedInstanceState.getInt("con"));
            saturationSlider.setProgress( savedInstanceState.getInt("sat"));
        }

    }

    // todo: make sure it always saves the variables (or make sure the demo video doesn't show them resetting ;) )
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt("br",currBrightness);
        state.putInt("con",currContrast);
        state.putInt("sat",currSaturation);
        state.putBundle("main",state);

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

}
