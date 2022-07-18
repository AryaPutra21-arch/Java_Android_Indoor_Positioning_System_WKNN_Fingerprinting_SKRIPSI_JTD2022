package com.talentica.wifiindoorpositioning.wifiindoorpositioning.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.talentica.wifiindoorpositioning.wifiindoorpositioning.R;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.adapter.ReferenceReadingsAdapter;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.adapter.holder.AccessPointSection;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.adapter.holder.ReferencePointSection;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.model.AccessPoint;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.model.IndoorProject;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.model.ReferencePoint;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.utils.AppContants;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.utils.Utils;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.realm.Realm;


public class ViewDBMapActivity extends AppCompatActivity implements View.OnClickListener {
    private IndoorProject project;
    private ReferencePointSection rpSec;
    private AccessPointSection apSec;
    private SectionParameters sp;

    private Button view_ap, view_rp;
    private ImageView map;
//    private Bitmap bmp;
    private RelativeLayout rl_ap,rl_rp;

    private String projectId;
    private int gmbr_map,apSec_len,rpSec_len ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_db_map_activity);
        projectId = getIntent().getStringExtra("projectId");

        Realm realm = Realm.getDefaultInstance();
        projectId = getIntent().getStringExtra("projectId");
        project = realm.where(IndoorProject.class).equalTo("id", projectId).findFirst();

        Log.v("DBMap", project.getName());
        String name = "Database Map "+project.getName();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(name);
        }
        //Toast.makeText(this, project.getName(), Toast.LENGTH_SHORT).show();
        if (project.getName().equals("Lantai 1")) {
            gmbr_map = getResources().getIdentifier("@drawable/ah_lt1", null, getPackageName());
        } else if (project.getName().equals("Lantai 2")) {
            gmbr_map = getResources().getIdentifier("@drawable/ah_lt2", null, getPackageName());
        }else if (project.getName().equals("Lantai 3")) {
            gmbr_map = getResources().getIdentifier("@drawable/ah_lt3", null, getPackageName());}

        Log.v("DBMap", "gmbr_map = "+gmbr_map+project.getName());
        InitUI();

        sp = new SectionParameters.Builder(R.layout.item_point_details)
                .headerResourceId(R.layout.item_section_details)
                .build();

    }

    private void InitUI(){
        map = (ImageView) findViewById(R.id.db_map);

        map.setImageResource(gmbr_map);
        rl_ap = (RelativeLayout) findViewById(R.id.view_ap_point);
        rl_ap.setVisibility(View.INVISIBLE);
        rl_rp = (RelativeLayout) findViewById(R.id.view_rp_point);
        rl_rp.setVisibility(View.INVISIBLE);
        view_ap = (Button) findViewById(R.id.view_ap);
        view_ap.setOnClickListener(this);
        view_rp = (Button) findViewById(R.id.view_rp);
        view_rp.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_ap:
                //startActivity(new Intent(this, ViewDBMapActivity.class));
                //Toast.makeText(this, "view ap", Toast.LENGTH_SHORT).show();
                if (rl_ap.getVisibility()==View.INVISIBLE){
                    rl_ap.setVisibility(View.VISIBLE);
                    view_ap.setText("Hide AP");
                }else{
                    view_ap.setText("View AP");
                    rl_ap.setVisibility(View.INVISIBLE);
                }

                if (apSec_len == 0){
                    apSec = new AccessPointSection(sp);
                    apSec.setAccessPoints(project.getAps());
                    apSec_len = apSec.getAccessPoints().size();
                    generateAp();
                }

                break;
            case R.id.view_rp:
                //startActivity(new Intent(this, ViewDBMapActivity.class));
                //Toast.makeText(this, "view rp", Toast.LENGTH_SHORT).show();
                if (rl_rp.getVisibility()==View.INVISIBLE){
                    rl_rp.setVisibility(View.VISIBLE);
                    view_rp.setText("Hide RP");
                }else{
                    rl_rp.setVisibility(View.INVISIBLE);
                    view_rp.setText("Show RP");
                }
                if (rpSec_len == 0){
                    rpSec = new ReferencePointSection(sp);
                    rpSec.setReferencePoints(project.getRps());
                    rpSec_len = rpSec.getReferencePoints().size();
                    generateRp();

                }

                break;
        }
    }

    private void generateAp(){
        for (int i = 0; i < apSec_len; i++){
            Log.v("apSecX", apSec.getAccessPoints().get(i).getX() + "");
            Log.v("apSecY", apSec.getAccessPoints().get(i).getY() + "");
            float x = (float) apSec.getAccessPoints().get(i).getX();
            float y = (float) apSec.getAccessPoints().get(i).getY();
            String name = apSec.getAccessPoints().get(i).getSsid();
            ImageView img = new ImageView(this);
            TextView txt = new TextView(this);
            txt.setText(name);
            txt.setTextSize(9);
            img.setImageResource(R.drawable.access_point);
            img.setScaleType(ImageView.ScaleType.FIT_XY);

            rl_ap.addView(img);
            rl_ap.addView(txt);
            img.setX(x*50-50);
            img.setY(y*50-500);
            txt.setX(x*50-50);
            txt.setY(y*50-450);
            int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
            img.getLayoutParams().height = dimensionInDp;
            img.getLayoutParams().width = dimensionInDp;
            img.requestLayout();
        }
    }


    private void generateRp(){
        for (int i = 0; i < rpSec_len; i++){
            Log.v("apSecX", rpSec.getReferencePoints().get(i).getX() + "");
            Log.v("apSecY", rpSec.getReferencePoints().get(i).getY() + "");
            float x = (float) rpSec.getReferencePoints().get(i).getX();
            float y = (float) rpSec.getReferencePoints().get(i).getY();
            String name = rpSec.getReferencePoints().get(i).getName();
            ImageView img = new ImageView(this);
            TextView txt = new TextView(this);
            txt.setText(name);
            txt.setTextSize(9);
            img.setImageResource(R.drawable.reference_point);
            img.setScaleType(ImageView.ScaleType.FIT_XY);

            rl_rp.addView(img);
            rl_rp.addView(txt);
            img.setX(x*50-50);
            img.setY(y*50-500);
            txt.setX(x*50-50);
            txt.setY(y*50-435);
            int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics());
            img.getLayoutParams().height = dimensionInDp;
            img.getLayoutParams().width = dimensionInDp;
            img.requestLayout();
        }
    }

}

