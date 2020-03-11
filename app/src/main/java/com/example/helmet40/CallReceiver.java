package com.example.helmet40;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.android.internal.telephony.ITelephony;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.SEND_SMS;
import static androidx.core.app.ActivityCompat.requestPermissions;
import static androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale;
import static androidx.core.provider.FontsContractCompat.FontRequestCallback.RESULT_OK;
import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class CallReceiver extends BroadcastReceiver {
    DatabaseReference databaseReference;
    Context context;
    String contactName;
    boolean isIncomingPicked;
    boolean isOutgoingStarted;
    private static String lastState = TelephonyManager.EXTRA_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;
    TelephonyManager telephony;
    ITelephony telephonyService;
    Bundle bundle;
    TelephonyManager tm;
    String phonenumber;
    static String number;
    private static final int REQUEST_PHONE_CALL = 1;
    private BroadcastReceiver sentStatusReceiver, deliveredStatusReceiver;
    private static final int REQUEST_SMS = 0;
    private static final int REQ_PICK_CONTACT = 2 ;
    //because the passed incoming is only valid in ringing

    @SuppressLint({"ServiceCast", "MissingPermission"})
    @Override
    public void onReceive(Context context, Intent intent) {
        databaseReference = FirebaseDatabase.getInstance().getReference("MySmartHelmet");

        try {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
             number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            savedNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
          /*
            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {

                tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                try {
                    Method m = tm.getClass().getDeclaredMethod("getITelephony");

                    m.setAccessible(true);
                    telephonyService = (ITelephony) m.invoke(tm);

                    if ((number != null)) {
                        telephonyService.endCall();
                        //contactName=getContactName(number,context);
                        Map<String, Object> profiled = new HashMap<>();
                        profiled.put("Caller", getContactName(number, context));
                        profiled.put("InComingFlag", "0");
                        profiled.put("State", "C");
                        profiled.put("TempTrigger", "0");
                        databaseReference.child("InComingCall").setValue(profiled);
                        Toast.makeText(context, "Ending the call from: " + number + getContactName(number, context), Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Toast.makeText(context, "Ring " + number, Toast.LENGTH_SHORT).show();

            }
            */
            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
                Map<String, Object> profiled = new HashMap<>();
                profiled.put("Caller", getContactName(number, context));
                profiled.put("Number", number);
                profiled.put("InComingFlag", "2");
                profiled.put("State", "R");
                profiled.put("TempTrigger", "2");
                databaseReference.child("IncomingCall").setValue(profiled);
                //
                //
                Toast.makeText(context, "InComing call started " + number + getContactName(number, context), Toast.LENGTH_SHORT).show();

                if (!contactExists(context,number)){
                    TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                    try {
                        Class c = Class.forName(tm.getClass().getName());
                        Method m = c.getDeclaredMethod("getITelephony");
                        m.setAccessible(true);
                        telephonyService = (ITelephony) m.invoke(tm);
                        String phoneNumber = bundle.getString("incoming_number");
                        Log.d("INCOMING", phoneNumber);
                        if ((number != null)) {
                            telephonyService.endCall();
                            Log.d("HANG UP", number);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                        if (telecomManager != null) {
                            telecomManager.endCall();
                            Map<String, Object> profile = new HashMap<>();
                            profile.put("Caller", getContactName(number, context));
                            profile.put("Number", number);
                            profile.put("InComingFlag", "0");
                            profile.put("State", "C");
                            profile.put("TempTrigger", "0");
                            databaseReference.child("IncomingCall").setValue(profile);
                            Toast.makeText(context, "InComing call ended " + number + getContactName(number, context), Toast.LENGTH_SHORT).show();
                        }
                    }
                }else {
                    Toast.makeText(context, "accept InComing call " + number + getContactName(number, context), Toast.LENGTH_SHORT).show();
                }

   /*
                databaseReference.child("IncomingCall").addValueEventListener(new ValueEventListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String acceptordecline = dataSnapshot.child("AcceptOrDecline").getValue().toString();
                        if (acceptordecline.equals("A")) {
                            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            try {
                                Class c = Class.forName(tm.getClass().getName());
                                Method m = c.getDeclaredMethod("getITelephony");
                                m.setAccessible(true);
                                telephonyService = (ITelephony) m.invoke(tm);
                                //String phoneNumber = bundle.getString("incoming_number");
                                //Log.d("INCOMING", phoneNumber);
                                if ((number != null)) {
                                    telephonyService.answerRingingCall();
                                    Log.d("HANG UP", number);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                                if (telecomManager != null) {
                                    telecomManager.acceptRingingCall();
                                }
                            }
                            //onIncomingCallAnswered(context, savedNumber, callStartTime);

                        } else if (acceptordecline.equals("D")) {
                            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            try {
                                Class c = Class.forName(tm.getClass().getName());
                                Method m = c.getDeclaredMethod("getITelephony");
                                m.setAccessible(true);
                                telephonyService = (ITelephony) m.invoke(tm);
                                String phoneNumber = bundle.getString("incoming_number");
                                Log.d("INCOMING", phoneNumber);
                                if ((number != null)) {
                                    telephonyService.endCall();
                                    //
                                   sendMySMS();
                                    Log.d("HANG UP", number);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                                if (telecomManager != null) {
                                    telecomManager.endCall();
                                     sendMySMS();
                                }
                            }
                           // onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                            // }
                        } else if (acceptordecline.equals("NR")) {
                            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            try {
                                Class c = Class.forName(tm.getClass().getName());
                                Method m = c.getDeclaredMethod("getITelephony");
                                m.setAccessible(true);
                                telephonyService = (ITelephony) m.invoke(tm);
                                if ((number != null)) {
                                    telephonyService.silenceRinger();
                                    Log.d("HANG UP", number);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                                if (telecomManager != null) {
                                    telecomManager.silenceRinger();
                                }
                            }
                            //onMissedCall(context, savedNumber, new Date());
                            Toast.makeText(getApplicationContext(), "Call is on silent mode", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                    }); */

            }
            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {

                if (lastState != TelephonyManager.EXTRA_STATE_RINGING) {
                    savedNumber = number;
                    isIncoming = false;
                    isIncomingPicked = false;
                    isOutgoingStarted = true;
                    Map<String, Object> profiled = new HashMap<>();
                    profiled.put("CallTo", getContactName(savedNumber, context));
                    profiled.put("Number", savedNumber);
                    profiled.put("OutGoingFlag", "1");
                    profiled.put("State", "A");
                    profiled.put("TempTrigger", "1");
                    databaseReference.child("OutGoingCall").setValue(profiled);
                    Toast.makeText(context, "OutGoingCall Started " + savedNumber + getContactName(savedNumber, context), Toast.LENGTH_SHORT).show();
                }else {
                   // savedNumber = number;
                    Toast.makeText(getApplicationContext(), "InComingCall Answered" + getContactName(number, context) + "" + number, Toast.LENGTH_SHORT).show();
                    isIncoming = true;
                    isIncomingPicked = true;
                    isOutgoingStarted = false;
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("Caller", getContactName(number, context));
                    profile.put("Number", number);
                    profile.put("InComingFlag", "1");
                    profile.put("State", "A");
                    profile.put("TempTrigger", "1");
                    databaseReference.child("InComingCall").setValue(profile);
                }
            }
            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {

                if (lastState == TelephonyManager.EXTRA_STATE_RINGING) {
                   //
                    //Ring but no pickup-  a miss
                    Toast.makeText(getApplicationContext(), "InComingCall ended" + getContactName(savedNumber, context) + "" + savedNumber, Toast.LENGTH_SHORT).show();
                    String callflag = "0";
                    String decline = "NR";
                    Map<String, Object> profilemapp = new HashMap<>();
                    profilemapp.put("Caller", getContactName(savedNumber, context));
                    profilemapp.put("Number", savedNumber);
                    profilemapp.put("TempTrigger", "0");
                    profilemapp.put("State", "C");
                    profilemapp.put("InComingFlag", callflag);
                    //profilemapp.put("AcceptOrDecline", decline);
                    databaseReference.child("InComingCall").setValue(profilemapp);
                } else if (isIncoming) {
                    Toast.makeText(getApplicationContext(), "InComingCall ended" + getContactName(savedNumber, context) + "" + savedNumber, Toast.LENGTH_SHORT).show();
                    String acceptordeclie = "D";
                    String callflag = "0";
                    Map<String, Object> profiles = new HashMap<>();
                    profiles.put("Caller", getContactName(savedNumber,context));
                    profiles.put("Number", savedNumber);
                    profiles.put("TempTrigger", "0");
                    profiles.put("State", "C");
                    profiles.put("InComingFlag", callflag);
                    //profiles.put("AcceptOrDecline", acceptordeclie);
                    databaseReference.child("InComingCall").setValue(profiles);
                } else {
                    savedNumber = number;
                    Toast.makeText(getApplicationContext(), "OutGoingCall ended" + getContactName(savedNumber, context) + "" + savedNumber, Toast.LENGTH_SHORT).show();
                    Map<String, Object> profiled = new HashMap<>();
                    profiled.put("CallTo", getContactName(savedNumber, context));
                    profiled.put("OutGoingFlag", "0");
                    profiled.put("State", "C");
                    profiled.put("TempTrigger", "0");
                    profiled.put("Number", savedNumber);
                    databaseReference.child("OutGoingCall").setValue(profiled);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendSMS() {
        String message = "Dont call me back";
        //Check if the phoneNumber is empty
        if (number.isEmpty()) {
            Toast.makeText(getApplicationContext(), "empty Phone Number", Toast.LENGTH_SHORT).show();
        } else {
            SmsManager sms = SmsManager.getDefault();
            // if message length is too long messages are divided
            List<String> messages = sms.divideMessage(message);
            for (String msg : messages) {
                PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, new Intent("SMS_SENT"), 0);
                PendingIntent deliveredIntent = PendingIntent.getBroadcast(context, 0, new Intent("SMS_DELIVERED"), 0);
                sms.sendTextMessage(number, null, msg, sentIntent, deliveredIntent);

            }
        }
    }
/*
        databaseReference = FirebaseDatabase.getInstance().getReference("MySmartHelmet");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && intent!=null && intent.getExtras() !=null) {

            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
               // savedNumber= intent.getStringExtra(TelephonyManager);
            } else {
                TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

               // String mPhoneNumber = tMgr.getLine1Number();
                String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                // String number= intent.getStringExtra("incoming_number");
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                bundle = intent.getExtras();
                int state = 0;
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }
                onCallStateChanged(context, state, number);
            }
        }
    }
*/
/*
    public void onCallStateChanged(Context context, int state, String number) {
        //super.onCallStateChanged(context,state,number);
        if (lastState == state) {
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallReceived(context, number, callStartTime);
                databaseReference.child("IncomingCall").addValueEventListener(new ValueEventListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String acceptordecline = dataSnapshot.child("AcceptOrDecline").getValue().toString();
                        if (acceptordecline.equals("A")) {
                            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            try {
                                Class c = Class.forName(tm.getClass().getName());
                                Method m = c.getDeclaredMethod("getITelephony");
                                m.setAccessible(true);
                                telephonyService = (ITelephony) m.invoke(tm);
                                //String phoneNumber = bundle.getString("incoming_number");
                                //Log.d("INCOMING", phoneNumber);
                                if ((number != null)) {
                                    telephonyService.answerRingingCall();
                                    Log.d("HANG UP", number);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                                if (telecomManager != null) {
                                    telecomManager.acceptRingingCall();
                                }
                            }
                            onIncomingCallAnswered(context, savedNumber, callStartTime);

                        } else if (acceptordecline.equals("D")) {
                            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            try {
                                Class c = Class.forName(tm.getClass().getName());
                                Method m = c.getDeclaredMethod("getITelephony");
                                m.setAccessible(true);
                                telephonyService = (ITelephony) m.invoke(tm);
                                String phoneNumber = bundle.getString("incoming_number");
                                Log.d("INCOMING", phoneNumber);
                                if ((number != null)) {
                                    telephonyService.endCall();
                                    Log.d("HANG UP", number);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                                if (telecomManager != null) {
                                    telecomManager.endCall();
                                }
                            }
                            onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                            // }
                        } else if (acceptordecline.equals("NR")) {
                            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            try {
                                Class c = Class.forName(tm.getClass().getName());
                                Method m = c.getDeclaredMethod("getITelephony");
                                m.setAccessible(true);
                                telephonyService = (ITelephony) m.invoke(tm);
                                if ((number != null)) {
                                    telephonyService.silenceRinger();
                                    Log.d("HANG UP", number);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                                if (telecomManager != null) {
                                    telecomManager.silenceRinger();
                                }
                            }
                            onMissedCall(context, savedNumber, new Date());
                            Toast.makeText(getApplicationContext(), "Call is on silent mode", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, savedNumber, callStartTime);
                } else {
                    isIncoming = true;
                    callStartTime = new Date();
                    onIncomingCallAnswered(context, savedNumber, callStartTime);

                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                savedNumber = number;
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime);
                } else if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                } else {
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                break;
        }
        lastState = state;
    }
*/
/*
    private void onOutgoingCallEnded(Context context, String savedNumber, Date callStartTime, Date date) {
        // String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        contactName = getContactName(savedNumber, context);
        // Toast.makeText(getApplicationContext(), "OutGoingCall ended", Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), "OutGoingCall ended" + getContactName(savedNumber, context) + "" + savedNumber, Toast.LENGTH_SHORT).show();
        Map<String, Object> profiled = new HashMap<>();
        profiled.put("CallTo", getContactName(savedNumber, context));
        profiled.put("OutGoingFlag", "0");
        profiled.put("State", "C");
        profiled.put("TempTrigger", "0");
        databaseReference.child("OutgoingCall").setValue(profiled);
    }


    private void onIncomingCallEnded(Context context, String savedNumber, Date callStartTime, Date date) {
        contactName = getContactName(savedNumber, context);
        // Toast.makeText(getApplicationContext(), "InComingCall ended", Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), "InComingCall ended" + getContactName(savedNumber, context) + "" + savedNumber, Toast.LENGTH_SHORT).show();
        String acceptordeclie = "D";
        String callflag = "0";
        Map<String, Object> profiles = new HashMap<>();
        profiles.put("CallerNumber", savedNumber);
        profiles.put("CallerName", getContactName(savedNumber, context));
        profiles.put("IncomingCall_Flag", callflag);
        profiles.put("AcceptOrDecline", acceptordeclie);
        databaseReference.child("IncomingCall").setValue(profiles);
    }

    private void onMissedCall(Context context, String savedNumber, Date callStartTime) {
        contactName = getContactName(savedNumber, context);
        //Toast.makeText(getApplicationContext(), "InComingCall ended", Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), "InComingCall ended" + getContactName(savedNumber, context) + "" + savedNumber, Toast.LENGTH_SHORT).show();

        String callflag = "0";
        String decline = "NR";
        Map<String, Object> profilemapp = new HashMap<>();
        profilemapp.put("CallerNumber", savedNumber);
        profilemapp.put("CallerName", getContactName(savedNumber, context));
        profilemapp.put("IncomingCall_Flag", callflag);
        profilemapp.put("AcceptOrDecline", decline);
        databaseReference.child("IncomingCall").setValue(profilemapp);
    }

    private void onIncomingCallAnswered(Context context, String savedNumber, Date callStartTime) {
        contactName = getContactName(savedNumber, context);
        // callPickedUp(number);
        isIncoming = true;
        isIncomingPicked = true;
        isOutgoingStarted = false;
        String accept = "A";
        String callflag = "1";
        Map<String, Object> profilemaps = new HashMap<>();
        profilemaps.put("CallerNumber", savedNumber);
        profilemaps.put("CallerName", getContactName(savedNumber, context));
        profilemaps.put("AcceptOrDecline", accept);
        profilemaps.put("IncomingCall_Flag", callflag);
        databaseReference.child("IncomingCall").setValue(profilemaps);
        //Toast.makeText(getApplicationContext(), "IncomingCall Started", Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), "InComingCall Started" + getContactName(savedNumber, context) + "" + savedNumber, Toast.LENGTH_SHORT).show();
    }

    private void onOutgoingCallStarted(String savedNumber,Context context) {
        //contactName = getContactName(savedNumber, context);
        // Toast.makeText(getApplicationContext(), "OutGoingCall Started", Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), "OutGoingCall Started" + getContactName(savedNumber, context) + "" + savedNumber, Toast.LENGTH_SHORT).show();
        Map<String, Object> profile = new HashMap<>();
        profile.put("CallTo", getContactName(savedNumber, context));
        profile.put("OutGoingFlag", "1");
        profile.put("State", "S");
        profile.put("TempTrigger", "1");
        databaseReference.child("OutgoingCall").setValue(profile);
    }

    private void onIncomingCallReceived(Context context, String number, Date callStartTime) {
       // contactName = getContactName(number, context);
        // callPickedUp(number);
        String accept = "I";
        String callflag = "1";
        Map<String, Object> profilemaps = new HashMap<>();
        profilemaps.put("CallerNumber", number);
        profilemaps.put("CallerName", getContactName(savedNumber, context));
        profilemaps.put("AcceptOrDecline", accept);
        profilemaps.put("IncomingCall_Flag", callflag);
        databaseReference.child("IncomingCall").setValue(profilemaps);
        //Toast.makeText(getApplicationContext(), "IncomingCall Started", Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), "InComingCall Started" + getContactName(savedNumber, context) + "" + number, Toast.LENGTH_SHORT).show();
    }

*/
public void sendMySMS() {

    //String phone = phoneEditText.getText().toString();
    String message = "Call me later,busy now";

    //Check if the phoneNumber is empty
    if (number.isEmpty()) {
        Toast.makeText(getApplicationContext(), "Please Enter a Valid Phone Number", Toast.LENGTH_SHORT).show();
    } else {

        SmsManager sms = SmsManager.getDefault();
        // if message length is too long messages are divided
        List<String> messages = sms.divideMessage(message);
        for (String msg : messages) {

            PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, new Intent("SMS_SENT"), 0);
            PendingIntent deliveredIntent = PendingIntent.getBroadcast(context, 0, new Intent("SMS_DELIVERED"), 0);
            sms.sendTextMessage(number, null, msg, sentIntent, deliveredIntent);

        }
    }
}

    public boolean contactExists(Context context, String number) {
        /// number is the phone number
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME };
        Cursor cur = context.getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
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

