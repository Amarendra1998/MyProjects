package com.example.helmet40;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {
    EditText search_from_place, search_to_place;
    Button editBtn;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        search_from_place = (EditText) findViewById(R.id.search_from_place);
        search_to_place = (EditText) findViewById(R.id.search_to_place);
        editBtn = (Button) findViewById(R.id.edtbtn);
        databaseReference = FirebaseDatabase.getInstance().getReference("MySmartHelmet");

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchfrom = search_from_place.getText().toString();
                String searchTo = search_to_place.getText().toString();
                if (searchfrom != null && searchTo != null) {
                    Map<String, Object> profiled = new HashMap<>();
                    profiled.put("FromAddress", searchfrom);
                    profiled.put("ToAddress", searchTo);
                    profiled.put("NavigationFlag", "1");
                    profiled.put("MapStyle", "N");
                    databaseReference.child("Navigation1").setValue(profiled);
                    Toast.makeText(getApplicationContext(), "Data Uploaded Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Data empty", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
