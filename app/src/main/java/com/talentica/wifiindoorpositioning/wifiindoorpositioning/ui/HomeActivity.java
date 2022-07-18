package com.talentica.wifiindoorpositioning.wifiindoorpositioning.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.talentica.wifiindoorpositioning.wifiindoorpositioning.R;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.adapter.ProjectsListAdapter;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.model.IndoorProject;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.model.RoomData;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.utils.RecyclerItemClickListener;

import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, RecyclerItemClickListener.OnItemClickListener {
    public String abc= "aaaa";
    private Realm realm;
    private RealmResults<IndoorProject> projects;
    private RecyclerView mRecyclerView;
    private ProjectsListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button data, nav;
    private int PERM_REQ_CODE_RP_ACCESS_COARSE_LOCATION = 198;
    private int PERM_REQ_CODE_LM_ACCESS_COARSE_LOCATION = 197;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        // Create a new empty instance of Realm

        // Clear the realm from last time
//        Realm.deleteRealm(realmConfiguration);

        realm = Realm.getInstance(realmConfiguration);

        projects = realm.where(IndoorProject.class).findAll();

        if (projects.isEmpty()) {

            create_project();
        }
        mAdapter = new ProjectsListAdapter(projects);
        mRecyclerView.setAdapter(mAdapter);
        // specify an adapter
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.settings:
                Intent intent = new Intent(this, UnifiedNavigationActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initUI() {
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //fab = findViewById(R.id.fab);
        data = findViewById(R.id.btn_data);
        nav = findViewById(R.id.btn_nav);
        //fab.setOnClickListener(this);
        data.setOnClickListener(this);
        nav.setOnClickListener(this);
        mRecyclerView = findViewById(R.id.projects_recycler_view);
        mRecyclerView.setVisibility(View.INVISIBLE);
        //mRecyclerView.setVisibility(View.INVISIBLE);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,mRecyclerView, this));
    }
    private void create_project(){

        String[] text = {"Lantai 1", "Lantai 2", "Lantai 3"};
        String[] desc = {"Ged.AH Lt.1", "Ged.AH Lt.2", "Ged.AH Lt.3"};
        for (int i=0;i<text.length; i++){
            int index = i;
            IndoorProject indoorProject = new IndoorProject(new Date(), text[i], desc[i]);
            // Obtain a Realm instance
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    IndoorProject indoorProject = bgRealm.createObject(IndoorProject.class, UUID.randomUUID().toString());
                    indoorProject.setName(text[index]);
                    indoorProject.setDesc(desc[index]);
                    indoorProject.setCreatedAt(new Date());
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    // Transaction was a success.
                    System.out.print("project created");
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    // Transaction failed and was automatically canceled.
                    System.out.print(error.getMessage());
                }
            });
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == data.getId()){
            Intent intent = new Intent(this, ProjectDetailActivity.class);
            IndoorProject project = projects.get(2);
            String[] id_lt = {projects.get(0).getId(), projects.get(1).getId(), projects.get(2).getId()};
            intent.putExtra("id", project.getId());
            intent.putExtra("list_id", id_lt);
            startActivity(intent);
        }
        else if (view.getId() == nav.getId()){
            Intent intent = new Intent(this, LocateMeActivity.class);
            IndoorProject project = projects.get(1);
            String[] id_lt = {projects.get(0).getId(), projects.get(1).getId(), projects.get(2).getId()};
            intent.putExtra("projectId", project.getId());
            intent.putExtra("list_id", id_lt);
            startActivity(intent);
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!realm.isClosed()) {
            realm.close();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        String pos = Integer.toString(position);
        Intent intent = new Intent(this, ProjectDetailActivity.class);
        IndoorProject project = projects.get(position);
        Toast.makeText(this, pos, Toast.LENGTH_SHORT).show();
//        intent.putExtra("id", project.getId());
//        startActivity(intent);
    }

    @Override
    public void onLongClick(View view, int position) {

    }
}
