package com.ephraim.me.dublinbikeadvanced;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ephraim.me.dublinbikeadvanced.AccountActivity.LoginActivity;
import com.ephraim.me.dublinbikeadvanced.Camera.CaptureActivity;
import com.ephraim.me.dublinbikeadvanced.Helper.FirebaseHelper;
import com.ephraim.me.dublinbikeadvanced.mSpacecraft.Spacecraft;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    DatabaseReference db;
    FirebaseHelper helper;

    EditText routeName,date,comment;
    Button save, show;
    ImageButton camera;

    private Button signOut;


    private ProgressBar progressBar;
    private FirebaseAuth auth;

    private TextView textView;
    private LocationListener locationListener;
    private LocationManager locationManager;

    public static ArrayList<String> poS = new ArrayList<>();
    public static ArrayList<String> arrayList = new ArrayList<>();
    public static ArrayList<String> routeList = new ArrayList<>();



    Button btnLoc, btnStopLoc;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        show = (Button) findViewById(R.id.show);
        camera = (ImageButton) findViewById(R.id.camera);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivity(intent);
            }
        });

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (MainActivity.this, ShowActivity.class);
                startActivity(intent);
            }
        });

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//INITIALIZE FIREBASE DB
        db= FirebaseDatabase.getInstance().getReference();
        helper=new FirebaseHelper(db);


        db.child("Spacecraft").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    Spacecraft spacecraft = dataSnapshot1.getValue(Spacecraft.class);
                    String comment = spacecraft.getComment().toString();
                    String route = spacecraft.getRoute().toString();
                    String date = spacecraft.getDate().toString();
                    String addresS = spacecraft.getAddS();
                    String addresE = spacecraft.getAddE();

                    routeList.add("Route Name: " + route);
                    arrayList.add("Started at: " + addresS + "\n" + "Ended at: " + addresE + "\n" + "Route Name: " + route + "\n" + "Date: " + date + "\n" + "Comments: " + comment);

                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        routeName = (EditText) findViewById(R.id.routeName);
        date = (EditText) findViewById(R.id.date);
        comment = (EditText) findViewById(R.id.comment);
        //SAVE
        save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //GET DATA
                String name=routeName.getText().toString();
                String propellant=date.getText().toString();
                String desc=comment.getText().toString();
                String addS=poS.get(0);
                String addE=poS.get(1);


                //SET DATA
                Spacecraft s=new Spacecraft();
                s.setRoute(name);
                s.setDate(propellant);
                s.setComment(desc);
                s.setAddS(addS);
                s.setAddE(addE);
                //s.setAddE(addE.toString());
                //SIMPLE VALIDATION
                if(name != null && name.length()>0)
                {
                    //THEN SAVE
                    if(helper.save(s))
                    {
                        //IF SAVED CLEAR EDITXT
                        routeName.setText("");
                        date.setText("");
                        comment.setText("");
                        poS.clear();
                        textView.clearComposingText();

                        // adapter=new CustomAdapter(MainActivity.this,helper.retrieve());
                        // listView.setAdapter(adapter);

                    }
                }else
                {
                    // Toast.makeText(MainActivity.this, "Name Must Not Be Empty", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //d.show();





        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };



        signOut = (Button) findViewById(R.id.sign_out);




        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }




        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        btnLoc = (Button) findViewById(R.id.btnGetLoc);
        btnStopLoc = (Button) findViewById(R.id.btnStopLoc);
        btnStopLoc.setVisibility(btnStopLoc.INVISIBLE);




        textView = (TextView) findViewById(R.id.textView);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                textView.append("\n " + location.getLatitude() +" " + location.getLongitude());
                double lat = location.getLatitude();
                double lng = location.getLongitude();



                Geocoder geocoder;

                List<Address> addresses;
                geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                String geoAdd = "";


                try {
                    addresses = geocoder.getFromLocation(lat, lng, 1);

                    if (addresses != null && addresses.size() > 0) {
                        String address = addresses.get(0).getAddressLine(0);
                        String city = addresses.get(0).getLocality();

                        String country = addresses.get(0).getCountryName();
                        geoAdd = address + ", " + city + ", " + country;
                        System.out.println(address + " " + city + " " + country);


                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                poS.add(lat + " " + lng + "\n" + geoAdd);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) { requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 10);
            return;
        } else {
            start();
            stop();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    start();
                stop();
                return;
        }
    }

    private void start() {
        btnLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //locationManager.requestLocationUpdates("gps", 1, 1, locationListener);
                Toast.makeText(MainActivity.this, "Getting GPS coordinates", Toast.LENGTH_LONG).show();
                locationManager.requestSingleUpdate("gps", locationListener, Looper.getMainLooper());
                btnLoc.setVisibility(btnLoc.INVISIBLE);
                btnStopLoc.setVisibility(btnStopLoc.VISIBLE);
            }
        });



    }

    public void stop(){
        btnStopLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager.requestSingleUpdate("gps", locationListener, Looper.getMainLooper());
                Toast.makeText(MainActivity.this, "Getting GPS coordinates", Toast.LENGTH_LONG).show();
                btnStopLoc.setVisibility(btnStopLoc.INVISIBLE);
                btnLoc.setVisibility(btnLoc.VISIBLE);
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void setDataToView(FirebaseUser user) {

        //email.setText("User Email: " + user.getEmail());


    }

    // this listener will be called when there is change in firebase user session
    FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // user auth state is changed - user is null
                // launch login activity
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            } else {
                //setDataToView(user);

            }
        }


    };

    //sign out method
    public void signOut() {
        auth.signOut();


// this listener will be called when there is change in firebase user session
        FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
    }



    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }


}