package com.talentica.wifiindoorpositioning.wifiindoorpositioning.ui;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.talentica.wifiindoorpositioning.wifiindoorpositioning.R;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.adapter.NearbyReadingsAdapter;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.core.Algorithms;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.core.WifiService;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.model.IndoorProject;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.model.LocDistance;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.model.LocationWithNearbyPlaces;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.model.WifiData;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.utils.AppContants;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.utils.Utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;

import io.realm.Realm;

/**
 * Created by suyashg on 10/09/17.
 */

public class LocateMeActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener{

    private WifiData mWifiData;
    private Algorithms algorithms = new Algorithms();
    private String projectId, defaultAlgo, item_select;
    private IndoorProject project;
    private MainActivityReceiver mReceiver = new MainActivityReceiver();
    private Intent wifiServiceIntent;
    private TextView tvLocation, tvNearestLocation, tvDistance;
    private ImageView locat, rp1, map_lt2,compass;
    private RecyclerView rvPoints;
    private LinearLayoutManager layoutManager;
    private NearbyReadingsAdapter readingsAdapter = new NearbyReadingsAdapter();
    private Button loc_me, get_dest;
    private Boolean destroy = false;
    private Spinner drop_dest;
    private Paint paint = new Paint();
    private Bitmap bmp;
    private Float tempX, tempY;
    private int poss, gmbr_map;
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;
    private String[] items,list_id;
    private float[] floatGravity = new float[3];
    private float[] floatGeoMagnetic = new float[3];

    private float[] floatOrientation = new float[3];
    private float[] floatRotationMatrix = new float[9];

    private float[][] room;
    private int PERM_REQ_CODE_LM_ACCESS_COARSE_LOCATION = 197;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_locate_me);

        list_id = getIntent().getStringArrayExtra("list_id");
        Intent intent = getIntent();
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.lantai_1:
                        intent.putExtra("projectId", list_id[0]);
                        finish();
                        startActivity(intent);
                        break;
                    case R.id.lantai_2:
                        intent.putExtra("projectId", list_id[1]);
                        finish();
                        startActivity(intent);
                        break;
                    case R.id.lantai_3:
                        intent.putExtra("projectId", list_id[2]);
                        finish();
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });


        defaultAlgo = Utils.getDefaultAlgo(this);
        projectId = getIntent().getStringExtra("projectId");
        if (projectId == null) {
            Toast.makeText(getApplicationContext(), "Project Not Found", Toast.LENGTH_LONG).show();
            this.finish();
        }
        Realm realm = Realm.getDefaultInstance();
        project = realm.where(IndoorProject.class).equalTo("id", projectId).findFirst();
        Log.v("LocateMeActivity", "onCreate");
        Log.i("LocateMeActivity", "projectId: " + project.getName());

        if (project.getName().equals("Lantai 1")) {
            gmbr_map = getResources().getIdentifier("@drawable/ah_lt1_rev", null, getPackageName());
            items = new String[]{"Pintu Keluar lt_1", "Lobi", "Toilet", "R.AH.1.1", "R.AH.1.2B","R.AH.1.2A", "R.AH.1.3","R.AH.1.5A","R.AH.1.5B","LAB AH 8","R.AH.1.8", "LAB AH 9","R.AH.1.10","R.AH.1.12A","R.AH.1.12B","R.AH.1.13A","R.AH.1.13B","R.AH.1.14A","R.AH.1.14B"};
            room = new float[][]{
                    {200f, 650f},{500f, 650f},{260f, 0f},{670f, 100f},
                    {670f, 200f},{670f, 300f},{670f, 450f},{670f, 820f},
                    {670f, 920},{670f, 1020f},{260f, 1170f},{260f, 1050f},
                    {260f, 870f},{260f, 440f},{260f, 370f},{260f, 290f},
                    {260f, 200f},{260f, 140f},{260f, 70f}};
        }else if (project.getName().equals("Lantai 2")){
            gmbr_map = getResources().getIdentifier("@drawable/ah_lt2_revv", null, getPackageName());
            items = new String[]{"Tanga lt_2", "Lobi", "R. Dosen Telkom", "R. Baca","Auditorium 1", "Auditorium AH 2", "Admin Telkom", "Kemahasiswaan","Admin Jurusan", "Admin Elektronika dan Listrik", "R. Kajur", "R. OB"};
            room = new float[][]{
                    {260f, 670f},{500f, 670f},{670f, 350f},{670f,50f},
                    {270f, 250f},{670f, 900f},{670f, 1100f},{670f, 1300f},
                    {270f, 850f},{270f, 950f},{270f, 1100f},{260f, 1300f}
            };
        }else if (project.getName().equals("Lantai 3")){
            gmbr_map = getResources().getIdentifier("@drawable/ah_lt3_revv", null, getPackageName());
            items = new String[]{"Tangga lt_3", "Lobi", "R. Dosen", "Lab. ELektronika Dasar", "Lab. Rangkaian Listrik","Kelas Internasional", "Toilet", "R.AH.3.22","R.AH.3.23","R.AH.3.24","R.AH.3.26","R.AH.3.28","R.AH.3.33"};
            room = new float[][]{
                    {207f, 650f},{500f, 650f},{500f, 0f},{285f, 850f},
                    {285f, 1000f},{285f, 1170f},{285f, 1300f},{285f, 430f},
                    {285f, 280f},{285f, 130f},{670f,130f},{670f, 430f},
                    {670f, 280f}
            };
        }

        initUI();

        //route_direct(100,100,200,200);
        compass = findViewById(R.id.compass);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        SensorEventListener sensorEventListenerAccelrometer = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                floatGravity = event.values;

                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

                compass.setRotation((float) (-floatOrientation[0]*180/3.14159));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        SensorEventListener sensorEventListenerMagneticField = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                floatGeoMagnetic = event.values;

                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

                compass.setRotation((float) (-floatOrientation[0]*180/3.14159));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(sensorEventListenerAccelrometer, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListenerMagneticField, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }


    private void initUI() {
        layoutManager = new LinearLayoutManager(this);

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(1f);

        get_dest =findViewById(R.id.btn_direct);
        get_dest.setOnClickListener(this);

        loc_me = findViewById(R.id.btn_loc_me);
        loc_me.setOnClickListener(this);

        map_lt2 = findViewById(R.id.map_lt2_bit);
        map_lt2.setImageResource(gmbr_map);

        rp1 = findViewById(R.id.rp1);
        rp1.setVisibility(View.INVISIBLE);

        locat = findViewById(R.id.loc);
        locat.setVisibility(View.INVISIBLE);

        tvLocation = findViewById(R.id.tv_location);
        tvNearestLocation = findViewById(R.id.tv_nearest_location);

        String name = project.getName();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(name);
        }
        drop_dest = findViewById(R.id.drop_dest);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        drop_dest.setAdapter(adapter);
        drop_dest.setOnItemSelectedListener(this);

        poss = 0;

    }

    private void route_direct(float x, float y, float xend, float yend) {

        bmp = Bitmap.createBitmap(map_lt2.getWidth(), map_lt2.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        map_lt2.draw(c);
        float midX = 500;

        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setStrokeWidth(10);
        Log.v("cek", String.valueOf(y)+String.valueOf(yend));
        if ((y>=650 && y<=850)&&(yend>=650 && yend<=850)){
                c.drawLine(x, y, xend, yend, p);
        }
        else{
            c.drawLine(x, y, midX, y, p);
            c.drawLine(midX, y, midX, yend, p);
            c.drawLine(midX, yend, xend, yend, p);
        }
        map_lt2.setImageBitmap(bmp);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        item_select = parent.getItemAtPosition(position).toString();
        poss = position;

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public void onClick(View view) {
        if (view.getId()==loc_me.getId()){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERM_REQ_CODE_LM_ACCESS_COARSE_LOCATION);
                //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            }
            destroy = true;
            mWifiData = null;

            // set receiver gae njupuk service android nyecann
            LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(AppContants.INTENT_FILTER));

            // launch WiFi service
            wifiServiceIntent = new Intent(this, WifiService.class);
            startService(wifiServiceIntent);

            // recover retained object== artine mengambil data yang sudah tersimpan
            mWifiData = (WifiData) getLastNonConfigurationInstance(); //iki gak se suk
            tvNearestLocation.setVisibility(View.VISIBLE);
            tvLocation.setVisibility(View.VISIBLE);
        }
        else if (view.getId() == get_dest.getId()){
            if (locat.getVisibility()==View.VISIBLE){
                map_lt2.setImageResource(gmbr_map);
                rp1.setVisibility(View.VISIBLE);

                rp1.setX(room[poss][0]);
                rp1.setY(room[poss][1]);
                float rpX = rp1.getX();
                float rpY = rp1.getY();

                route_direct(tempX*50-30,tempY*50-450,rpX+50,rpY+100);
            } else{
                Toast.makeText(this, String.valueOf(poss), Toast.LENGTH_SHORT).show();
                rp1.setVisibility(View.VISIBLE);
                rp1.setX(room[poss][0]);
                rp1.setY(room[poss][1]);
            }

        }

    }



    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mWifiData;
    }

    public class MainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("LocateMeActivity", "MainActivityReceiver");
            mWifiData = (WifiData) intent.getParcelableExtra(AppContants.WIFI_DATA); //memgambil data yang tersimpan di wifidata

            if (mWifiData != null) {
                LocationWithNearbyPlaces loc = Algorithms.processingAlgorithms(mWifiData.getNetworks(), project, Integer.parseInt(defaultAlgo));
                Log.w("LocateMeActivity", "lantai: "+ project.getName()+" loc:" + loc + mWifiData.getNetworks()+Integer.parseInt(defaultAlgo));
                if (loc == null) {
                    tvLocation.setText("Location: Not Found");
                } else {
                    //ubah locate me sesuai korrdinat referensi point terdekat
                    LocDistance theNearestPoint = Utils.getTheNearestPoint(loc);
                    String lokasi = loc.getLocation();
                    tvLocation.setText("Location: " + lokasi);
                    Log.w("nearest", lokasi);
                    String[] coordinate = lokasi.split(" ");
                    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                    tempX = Float.parseFloat(coordinate[0]);
                    tempY = Float.parseFloat((coordinate[1]));

                    tvLocation.setText("Location: " + coordinate[0]+","+coordinate[1]);
                    locat.setX(tempX*50-100);
                    locat.setY(tempY*50-550);
                    locat.setVisibility(View.VISIBLE);
                    Log.w("loc",String.valueOf(tempX)+String.valueOf(tempY));
                    if (theNearestPoint != null) {
                        tvNearestLocation.setText("You are near to: " + theNearestPoint.getName());
                    }
                    readingsAdapter.setReadings(loc.getPlaces());
                    readingsAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (destroy){
            Log.i("LocateMeActivity", "onDestroy");
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
            stopService(wifiServiceIntent);
        }
    }
}
