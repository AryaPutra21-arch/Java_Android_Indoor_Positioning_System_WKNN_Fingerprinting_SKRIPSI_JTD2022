package com.talentica.wifiindoorpositioning.wifiindoorpositioning.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.MotionEvent;

import com.talentica.wifiindoorpositioning.wifiindoorpositioning.R;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.model.AccessPoint;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.model.IndoorProject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.text.DecimalFormat;

import io.realm.Realm;

/**
 * Created by suyashg on 26/08/17.
 */

public class AddOrEditAccessPointActivity extends AppCompatActivity implements View.OnClickListener {

    private Button addAp, btnScanAP, open_map_ap;
    private EditText etName, etDesc, etX, etY, etMAC;
    private ImageView loc, lay_loc;
    private String projectId, apID;
    private boolean isEdit = false;
    private AccessPoint apToBeEdited;
    private int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 199, gmbr_map;
    private static final int REQ_CODE = 1212;//this is always positive
    private DecimalFormatSymbols symbols;
    private IndoorProject project;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_access_point);

        Realm realm = Realm.getDefaultInstance();
        projectId = getIntent().getStringExtra("projectId");
        project = realm.where(IndoorProject.class).equalTo("id", projectId).findFirst();

        if (project.getName().equals("Lantai 1")) {
            gmbr_map = getResources().getIdentifier("@drawable/ah_lt1", null, getPackageName());
        } else if (project.getName().equals("Lantai 2")) {
            gmbr_map = getResources().getIdentifier("@drawable/ah_lt2", null, getPackageName());
        }else if (project.getName().equals("Lantai 3")) {
            gmbr_map = getResources().getIdentifier("@drawable/ah_lt3", null, getPackageName());}

        if(projectId == null) {
            Toast.makeText(this, "Access point not found", Toast.LENGTH_LONG).show();
            this.finish();
        }

        apID = getIntent().getStringExtra("apID");
        initUI();
        symbols = new DecimalFormatSymbols(Locale.US);
        if (apID.equals("")) {
            isEdit = false;
        } else {
            isEdit = true;
            addAp.setText("Save");
        }

        if (isEdit)
        setUpEditMode();
    }

    private void setUpEditMode() {
        Realm realm = Realm.getDefaultInstance();
        apToBeEdited = realm.where(AccessPoint.class).equalTo("id", apID).findFirst();
        setValuesToFields(apToBeEdited);
    }

    private void setValuesToFields(AccessPoint accessPoint) {
        etName.setText(accessPoint.getSsid());
        etDesc.setText(accessPoint.getDescription());

        etX.setText(String.valueOf(accessPoint.getX()));
        etY.setText(String.valueOf(accessPoint.getY()));
        etMAC.setText(accessPoint.getMac_address());
        String xx = new DecimalFormat("##.##", symbols).format((((float)accessPoint.getX())*50));
        String yy = new DecimalFormat("##.##", symbols).format((((float)accessPoint.getY())*50));
        Log.v("cek", String.valueOf(accessPoint.getX()*50) + String.valueOf(accessPoint.getY()*50));
        lay_loc.setX(Float.parseFloat(xx)-50);
        lay_loc.setY(Float.parseFloat(yy)-500);
    }

    private void initUI() {
        etName = findViewById(R.id.et_ap_name);
        etDesc = findViewById(R.id.et_ap_desc);
        etX = findViewById(R.id.et_ap_x);
        etY = findViewById(R.id.et_ap_y);
        etMAC = findViewById(R.id.et_ap_mac);
        addAp = findViewById(R.id.bn_ap_create);
        addAp.setOnClickListener(this);
        btnScanAP = findViewById(R.id.bn_ap_scan);
        btnScanAP.setOnClickListener(this);
        open_map_ap = findViewById(R.id.open_map_ap);
        open_map_ap.setOnClickListener(this);

        lay_loc = findViewById(R.id.ap_loc);
        lay_loc.setVisibility(View.INVISIBLE);
        loc = findViewById(R.id.map_ap);
        loc.setVisibility(View.GONE);
        loc.setImageResource(gmbr_map);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == addAp.getId()) {
            final String text = etName.getText().toString().trim();
            final String desc = etDesc.getText().toString().trim();
            final String x = etX.getText().toString().trim();
            final String y = etY.getText().toString().trim();
            final String mac = etMAC.getText().toString().trim();
            final boolean isEditMode = isEdit;

            if (text.isEmpty()) {
                Snackbar.make(addAp, "Provide Access Point Name", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                // Obtain a Realm instance
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                IndoorProject project = realm.where(IndoorProject.class).equalTo("id", projectId).findFirst();
                if (isEditMode) {
                    apToBeEdited.setSsid(text);
                    apToBeEdited.setDescription(desc);
                    apToBeEdited.setX(Double.valueOf(x));
                    apToBeEdited.setY(Double.valueOf(y));
                    apToBeEdited.setMac_address(mac);
                } else {
                    AccessPoint accessPoint = realm.createObject(AccessPoint.class, UUID.randomUUID().toString());
                    accessPoint.setBssid(mac);
                    accessPoint.setDescription(desc);
                    accessPoint.setCreatedAt(new Date());
                    accessPoint.setX(Double.valueOf(x));
                    accessPoint.setY(Double.valueOf(y));
                    accessPoint.setSsid(text);
                    accessPoint.setMac_address(mac);
                    project.getAps().add(accessPoint);
                }
                realm.commitTransaction();
                this.finish();
            }
        } else if (view.getId() == btnScanAP.getId()) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
                //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method

            } else{
                startSearchWifiActivity();
            }
        } else if(view.getId() == open_map_ap.getId()){
            if (loc.getVisibility()==View.VISIBLE){
                loc.setVisibility(View.GONE);
                lay_loc.setVisibility(View.INVISIBLE);
                open_map_ap.setText("Open Map");
            }else{
                open_map_ap.setText("Close Map");
                loc.setVisibility(View.VISIBLE);
                lay_loc.setVisibility(View.VISIBLE);
            }
        }
    }

    private void startSearchWifiActivity() {
        Intent intent = new Intent(this, SearchWifiAccessPointActivity.class);
        startActivityForResult(intent, REQ_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSearchWifiActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            AccessPoint accessPoint = (AccessPoint) data.getParcelableExtra("accessPoint");
            setValuesToFields(accessPoint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (loc.getVisibility()==View.VISIBLE){
            float cx = event.getX();
            float cy = event.getY();
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            String xx = new DecimalFormat("##.##", symbols).format((cx/50));
            String yy = new DecimalFormat("##.##", symbols).format((cy/50));
            if(cy/50<39 & cy/50>8){

                lay_loc.setVisibility(View.VISIBLE);
                etX.setText(xx);
                etY.setText(yy);
                lay_loc.setX(cx-50);
                lay_loc.setY(cy-500);
            }
        }

        return false;
    }
}
