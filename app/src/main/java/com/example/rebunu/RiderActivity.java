package com.example.rebunu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.common.collect.MapMaker;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Rider screen
 * @author Zijian Xi, Zihao Huang
 */
// Since we are clear that I am using correct down-casting, we will suppress type "unchecked" warnings.
@SuppressWarnings("unchecked")
public class RiderActivity extends AppCompatActivity implements OnMapReadyCallback{
    // Reference: https://www.zoftino.com/android-mapview-tutorial posted on November 14, 2017
    private MapView mapView;
    private GoogleMap gmap;
    private static final int TAG_CODE_PERMISSION_LOCATION = 1;
//    private String floatingButtonStatus = "VISIBLE";
    Boolean cancel_clicked = false;
    private LocationManager locationManager;
    private Criteria criteria;
    Integer flag = 0;
    Request myRequest = null;
    String driverId = null;
    String riderId = null;
    Marker pickMarker;
    Marker dropMarker;
    Boolean alreadyRating = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);

        riderId = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("profileId")).toString();

        //All the layout
        ConstraintLayout postRequest_layout;
        LinearLayout postRequest_estimated_rate_layout;
        ConstraintLayout wait_responding_layout;
        ConstraintLayout rider_layout_request_confirmed;
        ConstraintLayout rider_layout_request_accepted;
        ConstraintLayout rider_layout_information;
        ConstraintLayout rider_layout_qrcode;
        ConstraintLayout rider_layout_rating;
        DrawerLayout drawerLayout;

        // All the toolbar
        Toolbar toolbar;

        // All the navigation view
        NavigationView navigationView;

        // All the action bar toogle
        ActionBarDrawerToggle toggle;


        //All the buttons
        Button button_postRequest;
        Button button_postRequest_floating;
        Button button_hide;
        Button button_tips;
        Button rider_button_tips_request_confirmed;
        Button rider_button_cancel_post_request;
        Button rider_button_hide_request_confirmed;
        Button rider_button_accept_request_confirmed;
        Button rider_button_decline_request_confirmed;
        Button rider_button_hide_request_accepted;
        Button rider_button_decline_request_accepted;
        Button rider_button_tips_request_accepted;
        Button rider_button_contact_request_accepted;
        Button rider_button_hide_information;
        Button rider_button_hide_qrcode;
        Button rider_button_payYourTrip_rating;
        Button rider_button_hide_rating;
        Button rider_button_end;
        ImageButton rider_button_like_rating;
        ImageButton rider_button_dislike_rating;

        // All the TextView
        TextView postRequest_textview_estimatedRateNumeric;
        TextView rider_textview_estimatedRateNumeric_request_confirmed;
        TextView rider_textview_estimatedRateNumeric_request_accepted;
        TextView rider_textview_fromWhere_request_accepted;
        TextView rider_textview_to_request_accepted;
        TextView rider_textview_name_request_accepted;
        TextView rider_textview_like_request_accepted;
        TextView rider_textview_dislike_request_accepted;
        TextView rider_textview_name_information;
        TextView rider_textview_like_information;
        TextView rider_textview_dislike_information;
        TextView drawer_header_textview_username;
//        TextView rider_textview_phone;
//        TextView rider_textview_email;
        ImageView rider_imageview_qrcode;

        //All the editText
        EditText postRequest_edittext_from;
        EditText postRequest_edittext_to;
        EditText rider_edittext_from_request_confirmed;
        EditText rider_edittext_to_request_confirmed;

        postRequest_layout = findViewById(R.id.postRequest_layout);
        postRequest_estimated_rate_layout = findViewById(R.id.postRequest_estimated_rate_layout);
        wait_responding_layout = findViewById(R.id.wait_responding_layout);
        rider_layout_request_confirmed = findViewById(R.id.rider_layout_request_confirmed);
        rider_layout_request_accepted = findViewById(R.id.rider_layout_request_accepted);
        rider_layout_information = findViewById(R.id.rider_layout_information);
        rider_layout_qrcode = findViewById(R.id.rider_layout_qrcode);

        rider_layout_rating = findViewById(R.id.rider_layout_rating);

        mapView = findViewById(R.id.postRequest_mapView);
        button_postRequest = findViewById(R.id.postRequest_button_postRequest);
        button_postRequest_floating = findViewById(R.id.postRequest_button_postRequest_floating);
        button_hide = findViewById(R.id.postRequest_button_hide);
        button_tips = findViewById(R.id.postRequest_button_tips);
        rider_button_tips_request_confirmed = findViewById(R.id.rider_button_tips_request_confirmed);
        rider_button_like_rating = findViewById(R.id.rider_button_like_rating);
        rider_button_dislike_rating = findViewById(R.id.rider_button_dislike_rating);
        rider_button_end = findViewById(R.id.rider_button_end);

        rider_button_cancel_post_request = findViewById(R.id.rider_button_cancel_post_request);
        rider_button_hide_request_confirmed = findViewById(R.id.rider_button_hide_request_confirmed);
        rider_button_accept_request_confirmed = findViewById(R.id.rider_button_accept_request_confirmed);
        rider_button_decline_request_confirmed = findViewById(R.id.rider_button_decline_request_confirmed);
        rider_button_hide_request_accepted = findViewById(R.id.rider_button_hide_request_accepted);
        rider_button_decline_request_accepted = findViewById(R.id.rider_button_decline_request_accepted);
        rider_button_tips_request_accepted = findViewById(R.id.rider_button_tips_request_accepted);
        rider_button_contact_request_accepted = findViewById(R.id.rider_button_contact_request_accepted);
        rider_button_hide_information = findViewById(R.id.rider_button_hide_information);
        rider_button_hide_qrcode = findViewById(R.id.rider_button_hide_qrcode);
        rider_button_payYourTrip_rating = findViewById(R.id.rider_button_payYourTrip_rating);
        rider_button_hide_rating = findViewById(R.id.rider_button_hide_rating);

        postRequest_textview_estimatedRateNumeric = findViewById(R.id.postRequest_textview_estimatedRateNumeric);
        rider_textview_estimatedRateNumeric_request_confirmed = findViewById(R.id.rider_textview_estimatedRateNumeric_request_confirmed);
        rider_textview_estimatedRateNumeric_request_accepted = findViewById(R.id.rider_textview_estimatedRateNumeric_request_accepted);
        rider_textview_fromWhere_request_accepted = findViewById(R.id.rider_textview_fromWhere_request_accepted);
        rider_textview_to_request_accepted = findViewById(R.id.rider_textview_to_request_accepted);
        rider_textview_name_request_accepted = findViewById(R.id.rider_textview_name_request_accepted);
        rider_textview_like_request_accepted = findViewById(R.id.rider_textview_like_request_accepted);
        rider_textview_dislike_request_accepted = findViewById(R.id.rider_textview_dislike_request_accepted);
        rider_textview_name_information = findViewById(R.id.rider_textview_name_information);
        rider_textview_like_information = findViewById(R.id.rider_textview_like_information);
        rider_textview_dislike_information = findViewById(R.id.rider_textview_dislike_information);
//        drawer_header_textview_username = findViewById(R.id.drawer_header_textview_username);

        rider_imageview_qrcode = findViewById(R.id.rider_imageview_qrcode);

        postRequest_edittext_from = findViewById(R.id.postRequest_edittext_from);
        postRequest_edittext_to = findViewById(R.id.postRequest_edittext_to);
        rider_edittext_from_request_confirmed = findViewById(R.id.rider_edittext_from_request_confirmed);
        rider_edittext_to_request_confirmed = findViewById(R.id.rider_edittext_to_request_confirmed);

        // drawer
        drawerLayout = findViewById(R.id.drawer);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigationView);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawerOpen,R.string.drawerClose) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                button_postRequest_floating.setVisibility(View.GONE);

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                button_postRequest_floating.setVisibility(View.VISIBLE);
            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        //get header
        View harderLayout = navigationView.getHeaderView(0);
        //get TextView
        drawer_header_textview_username = harderLayout.findViewById(R.id.drawer_header_textview_username);

        // get username
        Database dbPro = new Database();

        dbPro.profiles.document(riderId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()){
                    drawer_header_textview_username.setText((String) documentSnapshot.get("name"));
                }
            }
        });


//        dbpro.profiles.document(riderId).get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                DocumentSnapshot document = Objects.requireNonNull(task.getResult());
//                if (document.exists()) {
////                    Toast.makeText(getApplicationContext(),(String) document.get("name"),Toast.LENGTH_SHORT).show();
//                    drawer_header_textview_username.setText((String) document.get("name"));
//                    Log.d("", " Success");
//                } else {
//                    Toast.makeText(getApplicationContext(),"Not found!", Toast.LENGTH_SHORT).show();
//                    Log.d("", "No such document");
//                    return;
//                }
//            } else {
//                Log.d("", "get failed with ", task.getException());
//                Toast.makeText(getApplicationContext(), "Oops, little problem occured, please try again...", Toast.LENGTH_SHORT).show();
//                return;
//            }
//        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch(item.getTitle().toString()){
                    case "Profile":
//                        Toast.makeText(getApplicationContext(), item.getTitle().toString(), Toast.LENGTH_SHORT).show();
                        Intent userInformationIntent = new Intent(RiderActivity.this, UserInformationActivity.class);
                        userInformationIntent.putExtra("userId", riderId);
                        startActivity(userInformationIntent);
                        break;

                    case "Order":

                        Intent orderListActivity = new Intent(RiderActivity.this, OrderListActivity.class);
                        orderListActivity.putExtra("userId",riderId);
                        orderListActivity.putExtra("role", false);
                        startActivity(orderListActivity);

                        break;

                    case "Logout":
//                        Intent mainActivity = new Intent(RiderActivity.this, MainActivity.class);
//                        startActivity(mainActivity);
                        finish();
                }
                return false;
            }
        });


        // Reference: https://developer.android.com/training/permissions/requesting.html Posted on 2019-12-27.
        if (!(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, TAG_CODE_PERMISSION_LOCATION);
        }

        postRequest_layout.setVisibility(ConstraintLayout.GONE);
        postRequest_estimated_rate_layout.setVisibility(LinearLayout.VISIBLE);
        wait_responding_layout.setVisibility(ConstraintLayout.GONE);
        rider_layout_request_confirmed.setVisibility(ConstraintLayout.GONE);
        rider_layout_request_accepted.setVisibility(ConstraintLayout.GONE);
        rider_layout_information.setVisibility(ConstraintLayout.GONE);
        rider_layout_rating.setVisibility(ConstraintLayout.GONE);
        rider_layout_qrcode.setVisibility(ConstraintLayout.GONE);

        button_postRequest_floating.setVisibility(Button.VISIBLE);

//        ///////
//        rider_layout_rating.setVisibility(ConstraintLayout.VISIBLE);
//        button_postRequest_floating.setVisibility(Button.GONE);

        mapView.onCreate(null);
        mapView.getMapAsync(this);

        button_postRequest_floating.setOnClickListener(v -> {
            if (flag == 0) {
            Geocoder geocoder = new Geocoder(RiderActivity.this, Locale.getDefault());
            String addressPick = String.format("%.5f", pickMarker.getPosition().latitude) + ", " + String.format("%.5f", pickMarker.getPosition().longitude);
            String addressDrop = String.format("%.5f", dropMarker.getPosition().latitude) + ", " + String.format("%.5f", dropMarker.getPosition().longitude);
            try {
                List<Address> addresses = geocoder.getFromLocation(pickMarker.getPosition().latitude, pickMarker.getPosition().longitude, 1);
                String address = addresses.get(0).getAddressLine(0);
                String[] splitedAddress = Objects.requireNonNull(address.split(","));
                addressPick = splitedAddress[0] + ", " + splitedAddress[1];
                addresses = geocoder.getFromLocation(dropMarker.getPosition().latitude, dropMarker.getPosition().longitude, 1);
                address = addresses.get(0).getAddressLine(0);
                splitedAddress = Objects.requireNonNull(address.split(","));
                addressDrop = splitedAddress[0] + ", " + splitedAddress[1];
            } catch (Exception ignored) {}
            postRequest_edittext_from.setText(addressPick);
            postRequest_edittext_to.setText(addressDrop);
            Location startPos = Utility.latLngToLocation(pickMarker.getPosition());
            Location endPos = Utility.latLngToLocation(dropMarker.getPosition());

            postRequest_textview_estimatedRateNumeric.setText(Utility.getEstimatePrice(startPos, endPos, null).toString());
                wait_responding_layout.setVisibility(ConstraintLayout.GONE);
                postRequest_layout.setVisibility(ConstraintLayout.VISIBLE);
                button_postRequest_floating.setVisibility(Button.GONE);
                button_postRequest.setVisibility(Button.VISIBLE);
                flag = 1;
                return;
            }
            if(flag == 1){
                postRequest_layout.setVisibility(ConstraintLayout.VISIBLE);
                button_postRequest_floating.setVisibility(Button.GONE);
                return;
            }
            if(flag == 2){
                rider_layout_request_confirmed.setVisibility(ConstraintLayout.VISIBLE);
                button_postRequest_floating.setVisibility(Button.GONE);
                return;
            }
            if(flag == 3){
                rider_layout_request_accepted.setVisibility(ConstraintLayout.VISIBLE);
                button_postRequest_floating.setVisibility(Button.GONE);
                return;
            }
            if(flag == 4){
                button_postRequest_floating.setVisibility(Button.GONE);
                rider_layout_rating.setVisibility(ConstraintLayout.VISIBLE);
                return;

            }
            if(flag == 5){
                button_postRequest_floating.setVisibility(Button.GONE);
                rider_layout_qrcode.setVisibility(ConstraintLayout.VISIBLE);
                return;
            }
        });

//        postRequest_edittext_from.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {}
//            @SuppressLint("SetTextI18n")
//            @Override
//            public void afterTextChanged(Editable s) {
//                try {
//                    if(!postRequest_edittext_from.getText().toString().isEmpty() && !postRequest_edittext_to.getText().toString().isEmpty()) {
//                        String rawStart = postRequest_edittext_from.getText().toString();
//                        String rawEnd = postRequest_edittext_to.getText().toString();
//                        String[] splitedStart = rawStart.split(",");
//                        String[] splitedEnd = rawEnd.split(",");
//                        Location startPos = Utility.latLngToLocation(new LatLng((Double.valueOf(splitedStart[0])),(Double.valueOf(splitedStart[1]))));
//                        Location endPos = Utility.latLngToLocation(new LatLng((Double.valueOf(splitedEnd[0])),(Double.valueOf(splitedEnd[1]))));
//                        postRequest_textview_estimatedRateNumeric.setText(Utility.getEstimatePrice(startPos, endPos, null).toString());
//                    }
//                }catch (Exception ignored){}
//            }
//        });
//
//        postRequest_edittext_to.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {}
//            @SuppressLint("SetTextI18n")
//            @Override
//            public void afterTextChanged(Editable s) {
//                try {
//                    if(!postRequest_edittext_from.getText().toString().isEmpty() && !postRequest_edittext_to.getText().toString().isEmpty()) {
//                        String rawStart = postRequest_edittext_from.getText().toString();
//                        String rawEnd = postRequest_edittext_to.getText().toString();
//                        String[] splitedStart = rawStart.split(",");
//                        String[] splitedEnd = rawEnd.split(",");
//                        Location startPos = Utility.latLngToLocation(new LatLng((Double.valueOf(splitedStart[0])),(Double.valueOf(splitedStart[1]))));
//                        Location endPos = Utility.latLngToLocation(new LatLng((Double.valueOf(splitedEnd[0])),(Double.valueOf(splitedEnd[1]))));
//
//                        postRequest_estimated_rate_layout.setVisibility(LinearLayout.VISIBLE);
//                        postRequest_textview_estimatedRateNumeric.setText(Utility.getEstimatePrice(startPos, endPos, null).toString());
//                    }
//                }catch (Exception ignored){}
//            }
//        });

        button_tips.setOnClickListener(v -> {
            try{
                postRequest_textview_estimatedRateNumeric.setText(Integer.toString(Integer.parseInt(postRequest_textview_estimatedRateNumeric.getText().toString()) + 1));
            }catch (Exception e){
                postRequest_textview_estimatedRateNumeric.setText("6");
            }
        });

        rider_button_tips_request_confirmed.setOnClickListener(v -> {
            try{
                rider_textview_estimatedRateNumeric_request_confirmed.setText(Integer.toString(Integer.parseInt(rider_textview_estimatedRateNumeric_request_confirmed.getText().toString()) + 1));
            }catch (Exception e){
                rider_textview_estimatedRateNumeric_request_confirmed.setText("6");
            }
        });

        rider_button_tips_request_accepted.setOnClickListener(v -> {
            try{
                rider_textview_estimatedRateNumeric_request_accepted.setText(Integer.toString(Integer.parseInt(rider_textview_estimatedRateNumeric_request_confirmed.getText().toString()) + 1));
            }catch (Exception e){
                rider_textview_estimatedRateNumeric_request_accepted.setText("6");
            }
        });


        button_postRequest.setOnClickListener(v -> {
            cancel_clicked = false;

            try {
                Location startPos = Utility.latLngToLocation(pickMarker.getPosition());
                Location endPos = Utility.latLngToLocation(dropMarker.getPosition());

                myRequest = new Request(startPos,endPos,Integer.parseInt(postRequest_textview_estimatedRateNumeric.getText().toString()),riderId);
                Database dbr = new Database();
                String id = dbr.add(myRequest);
                myRequest.setId(id);

//                        Toast.makeText(getApplicationContext(),myRequest.getId(),Toast.LENGTH_SHORT).show();
                Database db = new Database();
                final DocumentReference reqRef = db.requests.document(myRequest.getId());

                reqRef.addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w("", "Listen failed.", e);
                        return;
                    }

                    if (!(snapshot != null && snapshot.exists())) {
                        if(!cancel_clicked){
                            postRequest_layout.setVisibility(ConstraintLayout.GONE);
                            rider_layout_request_confirmed.setVisibility(ConstraintLayout.VISIBLE);

                            rider_edittext_from_request_confirmed.setText(postRequest_edittext_from.getText().toString());
                            rider_edittext_to_request_confirmed.setText(postRequest_edittext_to.getText().toString());
                            rider_textview_estimatedRateNumeric_request_confirmed.setText(postRequest_textview_estimatedRateNumeric.getText().toString());

                            Database dbo = new Database();
                            DocumentReference ordRef = dbo.orders.document(myRequest.getId());
                            ordRef.get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = Objects.requireNonNull(task.getResult());
                                    if (document.exists()) {
                                        driverId = (String) document.get("driverId");
                                        Database dbp = new Database();
                                        DocumentReference proRef = dbp.profiles.document(driverId);
                                        proRef.get().addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                DocumentSnapshot document1 = Objects.requireNonNull(task1.getResult());
                                                if (document1.exists()) {
                                                    TextView rider_textview_name_request_confirmed = findViewById(R.id.rider_textview_name_request_confirmed);
                                                    TextView rider_textview_like_request_confirmed = findViewById(R.id.rider_textview_like_request_confirmed);
                                                    TextView rider_textview_dislike_request_confirmed = findViewById(R.id.rider_textview_dislike_request_confirmed);

                                                    ArrayList<Long> rating;
                                                    rating = (ArrayList<Long>) document1.get("rating");

                                                    rider_textview_name_request_confirmed.setText((String) document1.get("name"));
                                                    rider_textview_like_request_confirmed.setText(Objects.requireNonNull(rating).get(0).toString());
                                                    rider_textview_dislike_request_confirmed.setText(rating.get(1).toString());


//                                                                    Toast.makeText(getApplicationContext(),(String)document.get("name"), Toast.LENGTH_SHORT).show();
//                                                                    Toast.makeText(getApplicationContext(),((ArrayList<Integer>)document.get("rating")).get(0).toString(), Toast.LENGTH_SHORT).show();

                                                } else {
                                                    Toast.makeText(getApplicationContext(),"Not found!", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Oops, little problem occured, please try again...", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        });

                                    } else {
                                        Toast.makeText(getApplicationContext(),"Not found!", Toast.LENGTH_SHORT).show();
                                        Log.d("", "No such document");
                                        return;
                                    }
                                } else {
                                    Log.d("", "get failed with ", task.getException());
                                    Toast.makeText(getApplicationContext(), "Oops, little problem occured, please try again...", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            });

                            // Successfully Done!!
                            ordRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                    if(e != null){
                                        return;
                                    }
                                    if (documentSnapshot != null && documentSnapshot.exists()){
                                        if((Long)documentSnapshot.get("status") == 5){
                                            rider_layout_request_accepted.setVisibility(ConstraintLayout.GONE);
                                            rider_layout_information.setVisibility(ConstraintLayout.GONE);
                                            rider_layout_rating.setVisibility(ConstraintLayout.VISIBLE);


                                            driverId = (String) documentSnapshot.get("driverId");
                                            Database dbp = new Database();
                                            DocumentReference proRef = dbp.profiles.document(driverId);
                                            proRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()){
                                                        DocumentSnapshot document = Objects.requireNonNull(task.getResult());
                                                        if (document.exists()){
                                                            TextView rider_textview_name_rating = findViewById(R.id.rider_textview_name_rating);
                                                            TextView rider_textview_like_rating = findViewById(R.id.rider_textview_like_rating);
                                                            TextView rider_textview_dislike_rating = findViewById(R.id.rider_textview_dislike_rating);
                                                            TextView rider_button_needToImprove_rating = findViewById(R.id.rider_button_needToImprove_rating);
                                                            TextView rider_textview_awesome_rating = findViewById(R.id.rider_textview_awesome_rating);
                                                            rider_textview_name_rating.setText((String)document.get("name"));
                                                            ArrayList<Long> rating = new ArrayList<>();
                                                            rating = (ArrayList<Long>) document.get("rating");
                                                            rider_textview_like_rating.setText(rating.get(0).toString());
                                                            rider_textview_dislike_rating.setText(rating.get(1).toString());

                                                            //rating
                                                            rider_button_like_rating.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    if(alreadyRating){
                                                                        return;
                                                                    }
                                                                    alreadyRating = true;
                                                                    rider_button_dislike_rating.setVisibility(Button.GONE);
                                                                    rider_button_needToImprove_rating.setVisibility(View.GONE);

                                                                    ArrayList<Integer> rating = new ArrayList<>();
                                                                    Integer thumbsUp = Integer.parseInt(rider_textview_like_rating.getText().toString());
                                                                    Integer thumbsDown = Integer.parseInt(rider_textview_dislike_rating.getText().toString());
                                                                    rating.add(thumbsUp + 1);
                                                                    rating.add(thumbsDown);

                                                                    document.getReference()
                                                                            .update("rating", rating)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    Log.d("", "DocumentSnapshot successfully updated!");
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Log.w("", "Error updating document", e);
                                                                                }
                                                                            });

                                                                }
                                                            });

                                                            rider_button_dislike_rating.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    if(alreadyRating){
                                                                        return;
                                                                    }
                                                                    alreadyRating = true;
                                                                    rider_button_like_rating.setVisibility(Button.GONE);
                                                                    rider_textview_awesome_rating.setVisibility(View.GONE);

                                                                    ArrayList<Integer> rating = new ArrayList<>();
                                                                    Integer thumbsUp = Integer.parseInt(rider_textview_like_rating.getText().toString());
                                                                    Integer thumbsDown = Integer.parseInt(rider_textview_dislike_rating.getText().toString());
                                                                    rating.add(thumbsUp);
                                                                    rating.add(thumbsDown+1);

                                                                    document.getReference()
                                                                            .update("rating", rating)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    Log.d("", "DocumentSnapshot successfully updated!");
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Log.w("", "Error updating document", e);
                                                                                }
                                                                            });
                                                                }
                                                            });

                                                        }
                                                    }

                                                }
                                            });

                                            //pay your trip!!
                                            rider_button_payYourTrip_rating.setOnClickListener(v -> {
                                                rider_layout_rating.setVisibility(ConstraintLayout.GONE);
                                                rider_layout_qrcode.setVisibility(ConstraintLayout.VISIBLE);

                                                QRCode qrCode = new QRCode(driverId,riderId,Integer.parseInt(postRequest_textview_estimatedRateNumeric.getText().toString()));
                                                try {

                                                    rider_imageview_qrcode.setImageBitmap(qrCode.getBitmap());
                                                }catch (Exception ignored){}

                                            });



                                        }
                                    }
                                }
                            });
                        }
                    }
                });
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }


            button_postRequest.setVisibility(Button.GONE);
            wait_responding_layout.setVisibility(ConstraintLayout.VISIBLE);
        });

        rider_button_cancel_post_request.setOnClickListener(v -> {
            cancel_clicked = true;
            Database db = new Database();
            db.delete(myRequest);
            db.modifyOrderStatus(myRequest.getId(), 1);

            postRequest_edittext_from.setText("");
            postRequest_edittext_to.setText("");
            postRequest_textview_estimatedRateNumeric.setText("");
            postRequest_layout.setVisibility(ConstraintLayout.GONE);
            button_postRequest_floating.setVisibility(Button.VISIBLE);
            button_postRequest.setVisibility(Button.GONE);

            flag = 0;
        });


        rider_button_accept_request_confirmed.setOnClickListener(v -> {
            Database db = new Database();
            db.modifyOrderStatus(myRequest.getId(), 3);
            rider_layout_request_confirmed.setVisibility(ConstraintLayout.GONE);
            rider_layout_request_accepted.setVisibility(ConstraintLayout.VISIBLE);

            TextView rider_textview_name_request_confirmed = findViewById(R.id.rider_textview_name_request_confirmed);
            TextView rider_textview_like_request_confirmed = findViewById(R.id.rider_textview_like_request_confirmed);
            TextView rider_textview_dislike_request_confirmed = findViewById(R.id.rider_textview_dislike_request_confirmed);

            rider_textview_fromWhere_request_accepted.setText(rider_edittext_from_request_confirmed.getText().toString());
            rider_textview_to_request_accepted.setText(rider_edittext_to_request_confirmed.getText().toString());
            rider_textview_estimatedRateNumeric_request_accepted.setText(rider_textview_estimatedRateNumeric_request_confirmed.getText().toString());

            rider_textview_name_request_accepted.setText(rider_textview_name_request_confirmed.getText().toString());
            rider_textview_like_request_accepted.setText(rider_textview_like_request_confirmed.getText().toString());
            rider_textview_dislike_request_accepted.setText(rider_textview_dislike_request_confirmed.getText().toString());




        });

        rider_button_decline_request_confirmed.setOnClickListener(v -> {
            cancel_clicked = true;
            Database db = new Database();
            db.modifyOrderStatus(myRequest.getId(), 2);

            postRequest_edittext_from.setText("");
            postRequest_edittext_to.setText("");
            postRequest_textview_estimatedRateNumeric.setText("");
            postRequest_layout.setVisibility(ConstraintLayout.GONE);
            button_postRequest_floating.setVisibility(Button.VISIBLE);
            button_postRequest.setVisibility(Button.GONE);
            rider_layout_request_confirmed.setVisibility(ConstraintLayout.GONE);


            flag = 0;

        });

        rider_button_decline_request_accepted.setOnClickListener(v -> {
            cancel_clicked = true;
            Database db = new Database();
            db.modifyOrderStatus(myRequest.getId(), 4);

            postRequest_edittext_from.setText("");
            postRequest_edittext_to.setText("");
            postRequest_textview_estimatedRateNumeric.setText("");

            postRequest_layout.setVisibility(ConstraintLayout.GONE);
            button_postRequest_floating.setVisibility(Button.VISIBLE);
            button_postRequest.setVisibility(Button.GONE);
            rider_layout_request_confirmed.setVisibility(ConstraintLayout.GONE);
            rider_layout_request_accepted.setVisibility(ConstraintLayout.GONE);

            flag = 0;

        });


        rider_button_contact_request_accepted.setOnClickListener(v -> {
            rider_layout_information.setVisibility(ConstraintLayout.VISIBLE);
            rider_layout_request_accepted.setVisibility(ConstraintLayout.GONE);

            rider_textview_name_information.setText(rider_textview_name_request_accepted.getText().toString());
            rider_textview_like_information.setText(rider_textview_like_request_accepted.getText().toString());
            rider_textview_dislike_information.setText(rider_textview_dislike_request_accepted.getText().toString());

            Database dbp = new Database();
            dbp.profiles.document(driverId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = Objects.requireNonNull(task.getResult());
                    if (document.exists()) {

                        TextView rider_textview_phone = findViewById(R.id.rider_textview_phone);
                        TextView rider_textview_email = findViewById(R.id.rider_textview_email);
                        rider_textview_phone.setText((String) document.get("phone"));
                        rider_textview_email.setText((String) document.get("email"));

                        //click on phone
                        rider_textview_phone.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + rider_textview_phone.getText().toString()));
                                startActivity(phoneIntent);
                            }
                        });

                        //click on email
                        rider_textview_email.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                                emailIntent.setData(Uri.parse("mailto:" + rider_textview_email.getText().toString()));
                                startActivity(Intent.createChooser(emailIntent, "Send to Driver"));
                            }
                        });

                    } else return;
                } else return;
            });

        });


        button_hide.setOnClickListener(v -> {
            postRequest_layout.setVisibility(ConstraintLayout.GONE);
            button_postRequest_floating.setVisibility(Button.VISIBLE);
            flag = 1;

        });

        rider_button_hide_request_confirmed.setOnClickListener(v -> {
            rider_layout_request_confirmed.setVisibility(ConstraintLayout.GONE);
            button_postRequest_floating.setVisibility(Button.VISIBLE);
            flag = 2;
        });

        rider_button_hide_request_accepted.setOnClickListener(v -> {
            rider_layout_request_accepted.setVisibility(ConstraintLayout.GONE);
            button_postRequest_floating.setVisibility(Button.VISIBLE);
            flag = 3;
        });

        rider_button_hide_information.setOnClickListener(v -> {
            rider_layout_information.setVisibility(ConstraintLayout.GONE);
            rider_layout_request_accepted.setVisibility(ConstraintLayout.VISIBLE);

        });

        rider_button_hide_rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rider_layout_rating.setVisibility(ConstraintLayout.GONE);
                button_postRequest_floating.setVisibility(Button.VISIBLE);
                flag = 4;
            }
        });



        rider_button_hide_qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rider_layout_qrcode.setVisibility(ConstraintLayout.GONE);
                button_postRequest_floating.setVisibility(Button.VISIBLE);
                flag = 5;
            }
        });

        rider_button_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rider_layout_qrcode.setVisibility(ConstraintLayout.GONE);
                button_postRequest_floating.setVisibility(Button.VISIBLE);
                flag = 0;
            }
        });

        // not sure working yet..
        mapView.setOnClickListener(v -> postRequest_layout.setVisibility(ConstraintLayout.VISIBLE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        gmap.setMinZoomPreference(10);
        gmap.setMyLocationEnabled(true);
        UiSettings uiSettings = gmap.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setZoomControlsEnabled(true);

        locationManager = Objects.requireNonNull((LocationManager)getSystemService(Context.LOCATION_SERVICE));
        criteria = new Criteria();

        if (!(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, TAG_CODE_PERMISSION_LOCATION);
        }
        // get current location
        // Reference: https://stackoverflow.com/questions/36878087/get-current-location-lat-long-in-android-google-map-when-app-start Posted on Apr 27 '16 at 21:04 by Dijkstra
        Location currentLocation;
        try {
            currentLocation = Objects.requireNonNull(locationManager.getLastKnownLocation(Objects.requireNonNull(locationManager.getBestProvider(criteria, false))));
        } catch (Exception ignored) {
            currentLocation = Utility.currentLocation;
        }
        if(currentLocation != null) {
            double lat = currentLocation.getLatitude();
            double lon = currentLocation.getLongitude();
            LatLng cur = new LatLng(lat, lon);
            gmap.moveCamera(CameraUpdateFactory.newLatLng(cur));
        }

        pickMarker = gmap.addMarker(new MarkerOptions()
        .position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
        .draggable(true)
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pick)));
        pickMarker.setTag("pickMarker");

        dropMarker = gmap.addMarker(new MarkerOptions()
        .position(new LatLng(currentLocation.getLatitude()+0.001, currentLocation.getLongitude()+0.001))
        .draggable(true)
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.drop)));
        dropMarker.setTag("dropMarker");

        EditText postRequest_edittext_from = findViewById(R.id.postRequest_edittext_from);
        EditText postRequest_edittext_to = findViewById(R.id.postRequest_edittext_to);
        TextView postRequest_textview_estimatedRateNumeric = findViewById(R.id.postRequest_textview_estimatedRateNumeric);

//        Geocoder geocoder = new Geocoder(RiderActivity.this, Locale.getDefault());
//        String addressPick = String.format("%.5f", pickMarker.getPosition().latitude) + ", " + String.format("%.5f", pickMarker.getPosition().longitude);
//        String addressDrop = String.format("%.5f", dropMarker.getPosition().latitude) + ", " + String.format("%.5f", dropMarker.getPosition().longitude);
//        try {
//            List<Address> addresses = geocoder.getFromLocation(pickMarker.getPosition().latitude, pickMarker.getPosition().longitude, 1);
//            String address = addresses.get(0).getAddressLine(0);
//            String[] splitedAddress = Objects.requireNonNull(address.split(","));
//            addressPick = splitedAddress[0] + ", " + splitedAddress[1];
//            addresses = geocoder.getFromLocation(dropMarker.getPosition().latitude, dropMarker.getPosition().longitude, 1);
//            address = addresses.get(0).getAddressLine(0);
//            splitedAddress = Objects.requireNonNull(address.split(","));
//            addressDrop = splitedAddress[0] + ", " + splitedAddress[1];
//        } catch (Exception ignored) {}
//        postRequest_edittext_from.setText(addressPick);
//        postRequest_edittext_to.setText(addressDrop);
//        Location startPos = Utility.latLngToLocation(pickMarker.getPosition());
//        Location endPos = Utility.latLngToLocation(dropMarker.getPosition());
//        postRequest_textview_estimatedRateNumeric.setText(Utility.getEstimatePrice(startPos, endPos, null).toString());

        gmap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {}
            @Override
            public void onMarkerDrag(Marker marker) {}
            @Override
            public void onMarkerDragEnd(Marker marker) {
                Geocoder geocoder = new Geocoder(RiderActivity.this, Locale.getDefault());
                String whichOne = (String) marker.getTag();
                String addressToUpdate = String.format("%.5f", marker.getPosition().latitude) + ", " + String.format("%.5f", marker.getPosition().longitude);
                try {
                    List<Address> addresses = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
                    String address = addresses.get(0).getAddressLine(0);
                    String[] splitedAddress = Objects.requireNonNull(address.split(","));
                    addressToUpdate = splitedAddress[0] + ", " + splitedAddress[1];
                } catch (Exception ignored) {}
                if(whichOne.equals("pickMarker")) {
                    postRequest_edittext_from.setText(addressToUpdate);
                } else if (whichOne.equals("dropMarker")) {
                    postRequest_edittext_to.setText(addressToUpdate);
                }
                Location startPos = Utility.latLngToLocation(pickMarker.getPosition());
                Location endPos = Utility.latLngToLocation(dropMarker.getPosition());
                postRequest_textview_estimatedRateNumeric.setText(Utility.getEstimatePrice(startPos, endPos, null).toString());
            }
        });
    }
}
