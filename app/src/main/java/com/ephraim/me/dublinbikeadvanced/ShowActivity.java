package com.ephraim.me.dublinbikeadvanced;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ephraim.me.dublinbikeadvanced.mSpacecraft.Spacecraft;

import java.util.ArrayList;
import java.util.List;

public class ShowActivity extends AppCompatActivity {

    ListView listView;

    public static int child;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);



        listView = (ListView) findViewById(R.id.listView);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, (List<String>) MainActivity.routeList);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ShowActivity.this, FavoritesActivity.class);
                child  = position;
                startActivity(intent);

            }
        });

    }
}
