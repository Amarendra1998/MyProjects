package com.example.helmet40;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class PhoneCallActivity extends AppCompatActivity {
     EditText etNumber;
     ImageButton btCall;
     Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_call);

        etNumber = (EditText)findViewById(R.id.et_number);
        btCall = (ImageButton) findViewById(R.id.bt_call);


        btCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = etNumber.getText().toString();
                if (phone.isEmpty()){
                    Toast.makeText(getApplicationContext(),"wPlease enter your number",Toast.LENGTH_LONG).show();
                }else {
                    String s = "tel:" + phone;
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse(s));
                    startActivity(intent);
                    getContactName(phone, context);
                    Toast.makeText(getApplicationContext(),"number and name is:"+ phone + getContactName(phone,context),Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public String getContactName(String number, Context context) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }
}

