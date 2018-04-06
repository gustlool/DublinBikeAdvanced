package com.ephraim.me.dublinbikeadvanced;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Collections;

class FavoritesActivity extends AppCompatActivity {

    ListView list_item;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);


        list_item = (ListView) findViewById(R.id.list_item);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Collections.singletonList(MainActivity.arrayList.get(ShowActivity.child)));
        list_item.setAdapter(arrayAdapter);
    }
}
