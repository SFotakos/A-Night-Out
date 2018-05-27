package sfotakos.anightout.map;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import sfotakos.anightout.R;
import sfotakos.anightout.databinding.FragmentMapBinding;
import sfotakos.anightout.databinding.MapFilterBinding;

/**
 * A simple {@link Fragment} subclass.
 */
// TODO finish filter popup layout
// TODO get coarse location and zoom in on that, we don't need fine location for this app
// TODO get GPS permission and get UserLocation
// TODO only query location and move the camera when the fragment is visible
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.CancelableCallback {

    private static int ANIMATION_DURATION = 800;
    private static int DEFAULT_ZOOM_LEVEL = 15;

    private FragmentMapBinding mBinding;

    private GoogleMap mGoogleMap;
    private Marker mCenterMarker;
    private Circle mSearchCircle;
    private LatLng mClickedLatLng;

    private int mZoomLevel = DEFAULT_ZOOM_LEVEL;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        // TODO find out why dataBinding is ignoring <fragment tags>
        SupportMapFragment supportMapFragment =
                ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        supportMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                cleanMap();

                mClickedLatLng = latLng;

                mGoogleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(latLng, mZoomLevel),
                        ANIMATION_DURATION, MapFragment.this);
            }
        });
    }

    @Override
    public void onFinish() {
        mCenterMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(mClickedLatLng)
                .draggable(false));

        Projection projection = mGoogleMap.getProjection();
        Point markerScreenPosition = projection.toScreenLocation(mCenterMarker.getPosition());

        showFilterPopup(markerScreenPosition.x, markerScreenPosition.y);
    }

    @Override
    public void onCancel() {

    }

    private void showFilterPopup(int xPos, int yPos) {
        MapFilterBinding filterBinding =
                DataBindingUtil.inflate(getLayoutInflater(),
                        R.layout.map_filter, null, false);

        PopupWindow filterPopup =
                new PopupWindow(filterBinding.getRoot(),
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, true);
        filterPopup.setOutsideTouchable(false);

        filterPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                cleanMap();
            }
        });

        filterPopup.showAsDropDown(mBinding.getRoot(), xPos, yPos, Gravity.NO_GRAVITY);

        filterBinding.filterOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "OK", Toast.LENGTH_SHORT).show();
            }
        });

        filterBinding.filterCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "CANCEL", Toast.LENGTH_SHORT).show();
            }
        });

        filterBinding.filterDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                safeRemoveCircle();
                mSearchCircle = mGoogleMap.addCircle(new CircleOptions()
                        .center(mCenterMarker.getPosition())
                        .radius(progress)
                        .strokeColor(Color.DKGRAY)
                        .fillColor(Color.TRANSPARENT));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void safeRemoveMarker() {
        if (mCenterMarker != null) {
            mCenterMarker.remove();
        }
    }

    private void safeRemoveCircle() {
        if (mSearchCircle != null) {
            mSearchCircle.remove();
        }
    }

    private void cleanMap() {
        safeRemoveMarker();
        safeRemoveCircle();
    }

}
