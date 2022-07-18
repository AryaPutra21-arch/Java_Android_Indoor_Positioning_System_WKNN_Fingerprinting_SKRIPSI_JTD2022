package com.talentica.wifiindoorpositioning.wifiindoorpositioning.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.talentica.wifiindoorpositioning.wifiindoorpositioning.R;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.adapter.WifiResultsAdapter;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.adapter.holder.AccessPointSection;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.adapter.holder.ReferencePointSection;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.model.AccessPoint;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.model.IndoorProject;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.model.ReferencePoint;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.utils.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.realm.Realm;

/**
 * Created by suyashg on 25/08/17.
 */

public class ProjectDetailActivity extends AppCompatActivity implements View.OnClickListener, RecyclerItemClickListener.OnItemClickListener {

    private RecyclerView pointRV;
    private Button btnAddAp, btnAddRp, btndbmap;
    private IndoorProject project;
    private WifiManager mainWifi;
    private List<ScanResult> results = new ArrayList<>();
    private SectionedRecyclerViewAdapter sectionAdapter = new SectionedRecyclerViewAdapter();
    private ReferencePointSection rpSec;
    private AccessPointSection apSec;
    private LinearLayoutManager layoutManager;
    private String projectId;
    private WifiResultsAdapter wifiResultsAdapter = new WifiResultsAdapter();
    private int PERM_REQ_CODE_RP_ACCESS_COARSE_LOCATION = 198;
    private int PERM_REQ_CODE_LM_ACCESS_COARSE_LOCATION = 197;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        String[] list_id = getIntent().getStringArrayExtra("list_id");
        projectId = getIntent().getStringExtra("id");

        if (projectId == null) {
            Toast.makeText(getApplicationContext(), "Project Not Found", Toast.LENGTH_LONG).show();
            this.finish();
        }
        Log.i("ProjectDetailActivity", "id>"+projectId);

        Realm realm = Realm.getDefaultInstance();

        project = realm.where(IndoorProject.class).equalTo("id", projectId).findFirst();
        Log.i("ProjectDetailActivity", "name>"+project.getName());


        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        results = mainWifi.getScanResults();
        final int N = results.size();
        initUI();

        for(int i=0; i < N; ++i) {
            Log.v("wifi ", "  BSSID       =" + results.get(i).BSSID);
            Log.v("wifi ", "  Level       =" + results.get(i).level);
            Log.v("wifi ", "---------------");
        }

        Intent intent = getIntent();
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.lantai_1:
                        intent.putExtra("id", list_id[0]);
                        finish();
                        startActivity(intent);
                        break;
                    case R.id.lantai_2:
                        intent.putExtra("id", list_id[1]);
                        finish();
                        startActivity(intent);
                        break;
                    case R.id.lantai_3:
                        intent.putExtra("id", list_id[2]);
                        finish();
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
    }


    private void initUI() {
        pointRV = findViewById(R.id.rv_points);

        btnAddAp = findViewById(R.id.btn_add_ap);
        btnAddAp.setOnClickListener(this);
        btnAddRp = findViewById(R.id.btn_add_rp);
        btnAddRp.setOnClickListener(this);
        btndbmap = findViewById(R.id.view_db_map);
        btndbmap.setOnClickListener(this);

        setCounts();

        SectionParameters sp = new SectionParameters.Builder(R.layout.item_point_details)
                .headerResourceId(R.layout.item_section_details)
                .build();
        apSec = new AccessPointSection(sp);

        rpSec = new ReferencePointSection(sp);
        apSec.setAccessPoints(project.getAps());
        rpSec.setReferencePoints(project.getRps());

        sectionAdapter.addSection(apSec);
        sectionAdapter.addSection(rpSec);
        layoutManager = new LinearLayoutManager(this);
        pointRV.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        pointRV.setLayoutManager(layoutManager);
        pointRV.setAdapter(sectionAdapter);
        pointRV.addOnItemTouchListener(new RecyclerItemClickListener(this,pointRV, this));
    }

    private void setCounts() {
        String name = project.getName();
        int apCount = project.getAps().size();
        int rpCount = project.getRps().size();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(name);
        }
        if (apCount > 0) {
            ((TextView)findViewById(R.id.tv_ap_count)).setText("Access Points:"+String.valueOf(apCount));
        }
        if (rpCount > 0) {
            ((TextView)findViewById(R.id.tv_rp_count)).setText("Reference Points:"+String.valueOf(rpCount));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sectionAdapter.notifyDataSetChanged();
        setCounts();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == btnAddAp.getId()) {
            startAddAPActivity("");
        } else if (view.getId() == btnAddRp.getId()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERM_REQ_CODE_RP_ACCESS_COARSE_LOCATION);
                //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            } else {
                startAddRPActivity(null);
            }
        } else if (view.getId() == btndbmap.getId()) {
            startAddDBMAPActivity();
        }
    }


    private void startAddAPActivity(String apId) {
        Intent intent = new Intent(this, AddOrEditAccessPointActivity.class);
        intent.putExtra("projectId", projectId);
        intent.putExtra("apID", apId);
        startActivity(intent);
    }


    private void startAddRPActivity(String rpId) {
        Intent intent = new Intent(this, AddOrEditReferencePointActivity.class);
        intent.putExtra("projectId", projectId);
        intent.putExtra("rpId", rpId);
        startActivity(intent);
    }

    private void  startAddDBMAPActivity() {
        Intent intent = new Intent(this, ViewDBMapActivity.class);
        intent.putExtra("projectId", projectId);
        startActivity(intent);
    }


    @Override
    public void onItemClick(View view, int position) {
//        Toast.makeText(this,"onItemClick pos>"+position, Toast.LENGTH_SHORT).show();
        int apsCount = 0;
        if (project.getAps() != null) {
            apsCount = project.getAps().size();
        }
        if (position <= apsCount && position != 0) {//AP section event
            AccessPoint accessPoint = project.getAps().get(position - 1);
            startAddAPActivity(accessPoint.getId());
        } else if (position > (apsCount+1)) {//RP section event
            ReferencePoint referencePoint = project.getRps().get(position - apsCount - 1 - 1);
            startAddRPActivity(referencePoint.getId());
        }
    }

    @Override
    public void onLongClick(View view, int position) {
//        Toast.makeText(this,"onLongClick pos>"+position, Toast.LENGTH_SHORT).show();
        int apsCount = 0;
        if (project.getAps() != null) {
            apsCount = project.getAps().size();
        }
        if (position <= apsCount && position != 0) {//AP section event
            AccessPoint accessPoint = project.getAps().get(position - 1);
            showDeleteDialog(accessPoint, null);
        } else if (position > (apsCount+1)) {//RP section event
            ReferencePoint referencePoint = project.getRps().get(position - apsCount - 1 - 1);
            showDeleteDialog(null, referencePoint);
        }
    }

    private void showDeleteDialog(final AccessPoint accessPoint,final ReferencePoint referencePoint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog);
        if (accessPoint != null) {
            builder.setTitle("Delete this Access Point");
            builder.setMessage("Delete "+ accessPoint.getSsid());
        } else {
            builder.setTitle("Delete this Reference Point");
            builder.setMessage("Delete "+ referencePoint.getName());
        }

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Realm realm = Realm.getDefaultInstance();
                if (accessPoint != null) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            accessPoint.deleteFromRealm();
                            refreshList();
                        }
                    });
                } else {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            referencePoint.deleteFromRealm();
//                            project.getRps().deleteAllFromRealm();
                            refreshList();
                        }
                    });
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void refreshList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sectionAdapter.notifyDataSetChanged();
            }
        });
    }


}