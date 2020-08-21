package com.example.ubertutorandroidstudentapp.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ubertutorandroidstudentapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    SupportMapFragment mapFragment;

    private HomeViewModel homeViewModel;
    private GoogleMap mMap;
    //Location
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

//    //Online System
//    DatabaseReference onlineRef, currentUserRef, tutorsLocationRef;
//    GeoFire geoFire;
//    ValueEventListener onlineValueEventListener = new ValueEventListener( ) {
//        @Override
//        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//            if(dataSnapshot.exists())
//                currentUserRef.onDisconnect().removeValue();
//        }
//
//        @Override
//        public void onCancelled(@NonNull DatabaseError databaseError) {
//            Snackbar.make( mapFragment.getView(), databaseError.getMessage(), Snackbar.LENGTH_LONG )
//                    .show();
//        }
//    };

    @Override
    public void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates( locationCallback );
//        geoFire.removeLocation( FirebaseAuth.getInstance().getCurrentUser().getUid() );
//        onlineRef.removeEventListener( onlineValueEventListener );
        super.onDestroy( );
    }

    @Override
    public void onResume() {
        super.onResume( );
        //registerOnlineSystem();
    }

//    private void registerOnlineSystem() {
//        onlineRef.addValueEventListener( onlineValueEventListener );
//    }

    public View onCreateView(@NonNull LayoutInflater inflater ,
                             ViewGroup container , Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider( this ).get( HomeViewModel.class );
        View root = inflater.inflate( R.layout.fragment_home , container , false );

        init( );

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getChildFragmentManager( )
                .findFragmentById( R.id.map );
        mapFragment.getMapAsync( this );

        return root;
    }

    private void init() {

//        onlineRef = FirebaseDatabase.getInstance( ).getReference( ).child( ".info/connected" );
//        studentLocationRef = FirebaseDatabase.getInstance( ).getReference( Common.TUTORS_LOCATION_REFERENCES );
//        currentUserRef = FirebaseDatabase.getInstance( ).getReference( Common.TUTORS_LOCATION_REFERENCES )
//                .child( FirebaseAuth.getInstance( ).getCurrentUser( ).getUid( ) );
//        geoFire = new GeoFire( tutorsLocationRef );
//
//        registerOnlineSystem( );

        locationRequest = new LocationRequest( );
        locationRequest.setSmallestDisplacement( 10f );
        locationRequest.setInterval( 5000 );
        locationRequest.setFastestInterval( 3000 );
        locationRequest.setPriority( LocationRequest.PRIORITY_HIGH_ACCURACY );

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult( locationResult );
                LatLng newPosition = new LatLng( locationResult.getLastLocation( ).getLatitude( ) ,
                        locationResult.getLastLocation( ).getLongitude( ) );
                mMap.moveCamera( CameraUpdateFactory.newLatLngZoom( newPosition , 18f ) );

            }
        };

////                //Update Location
////                geoFire.setLocation( FirebaseAuth.getInstance( ).getCurrentUser( ).getUid( ) ,
////                        new GeoLocation( locationResult.getLastLocation( ).getLatitude( ) ,
////                                locationResult.getLastLocation( ).getLongitude( ) ) ,
////                        (key , error) -> {
////                            if ( error != null )
////                                Snackbar.make( mapFragment.getView( ) , error.getMessage( ) , Snackbar.LENGTH_LONG )
////                                        .show( );
////                            else
////                                Snackbar.make( mapFragment.getView( ) , "You're online" , Snackbar.LENGTH_LONG )
////                                        .show( );
////                        } );
////            }
////        };
//
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient( getContext( ) );
        if ( ActivityCompat.checkSelfPermission( getContext( ) , Manifest.permission.ACCESS_FINE_LOCATION )
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission( getContext( ) , Manifest.permission.ACCESS_COARSE_LOCATION )
                != PackageManager.PERMISSION_GRANTED ) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates( locationRequest , locationCallback , Looper.myLooper( ) );

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Request permission to add current location
        Dexter.withContext( getContext( ) )
                .withPermission( Manifest.permission.ACCESS_FINE_LOCATION )
                .withListener( new PermissionListener( ) {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if ( ActivityCompat.checkSelfPermission( getContext() , Manifest.permission.ACCESS_FINE_LOCATION )
                                != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission( getContext() , Manifest.permission.ACCESS_COARSE_LOCATION )
                                != PackageManager.PERMISSION_GRANTED ) {
                            return;
                        }
                        mMap.setMyLocationEnabled( true );
                        mMap.getUiSettings().setMyLocationButtonEnabled( true );
                        mMap.setOnMyLocationButtonClickListener( () -> {
                            fusedLocationProviderClient.getLastLocation()
                                    .addOnFailureListener( e -> Snackbar.make( getView(), e.getMessage(),
                                            Snackbar.LENGTH_SHORT).show() )
                                    .addOnSuccessListener( location -> {
                                        LatLng userLatLng = new LatLng( location.getLatitude(),
                                                location.getLongitude());
                                        mMap.animateCamera( CameraUpdateFactory.newLatLngZoom( userLatLng, 18f ) );
                                    } );
                            return true;
                        } );

                        //Set layout button
                        View locationButton = ((View)mapFragment.getView().findViewById( Integer.parseInt( "1" ) )
                                .getParent())
                                .findViewById( Integer.parseInt( "2" ) );
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton
                                .getLayoutParams();

                        //Right bottom
                        params.addRule( RelativeLayout.ALIGN_PARENT_TOP, 0 );
                        params.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE );
                        params.setMargins( 0, 0, 0, 50 );


                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Snackbar.make( getView(), permissionDeniedResponse.getPermissionName() +
                                "needs to be enabled.", Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest , PermissionToken permissionToken) {

                    }
                } )
                .check(); //Don't forget to check method

        try {
            boolean success = googleMap.setMapStyle( MapStyleOptions.loadRawResourceStyle(
                    getContext(), R.raw.uber_maps_style ) );
            if(!success)
                Snackbar.make( getView(), "Loading map style failed",
                        Snackbar.LENGTH_SHORT).show();
        }catch (Exception e){
            Snackbar.make( getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
        }


    }
}