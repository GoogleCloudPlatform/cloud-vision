package edu.cs.cs184.photoapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FilterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FilterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilterFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_PARAM1 = "param1";
    public  static final String ARG_PARAM2 = "param2";
    public static final String ARG_PARAM3 = "param3";

    private TextView filterText;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private double mParam2;
    private byte[] mParam3;

    ImageView imageView;
    Bitmap currentBitmap;

    private OnFragmentInteractionListener mListener;

    public FilterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FilterFragment.
     */

    // TODO: receive the args (or send them?)

    public static FilterFragment newInstance(String param1, Double param2, byte[] param3) {
        FilterFragment fragment = new FilterFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putDouble(ARG_PARAM2, param2);
        args.putByteArray(ARG_PARAM3, param3);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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


        imageView = (ImageView) getView().findViewById(R.id.imageView);
        filterText = (TextView)getView().findViewById(R.id.filter_text);
        currentBitmap = Bitmap.createBitmap(10,10,Bitmap.Config.ARGB_8888);
        if (getArguments() != null) {
            Log.e("as","arguments aren't null");
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getDouble(ARG_PARAM2);
            mParam3 = getArguments().getByteArray(ARG_PARAM3);

            try {
                String filterMessage = getResources().getString(R.string.feature_message) + mParam1 + getResources().getString(R.string.feature_message2) + mParam2;
                filterText.setText(filterMessage);
                currentBitmap = BitmapFactory.decodeByteArray(mParam3,0,mParam3.length);
                imageView.setImageBitmap(currentBitmap);
            } catch (Exception e) {

                filterText.setText(getResources().getString(R.string.feature_error));
            }
        }
        else{
            filterText.setText(getResources().getString(R.string.feature_error));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
