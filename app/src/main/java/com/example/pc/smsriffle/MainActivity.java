package com.example.pc.smsriffle;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    EditText nrEdit;
    EditText tekstEdit;
    EditText ilosc;
    EditText opoznienie;
    int iloscwiadomosci;
    String wiadomosc;
    String nr;
    int delay;

    private SensorManager sensorManager;
    private Sensor mSensor;

    private static final String TAG = Activity.class.getSimpleName();
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private Uri uriContact;
    private String contactID;
    private static final String SCREEN_BRIGHTNESS_MODE = "screen_brightness_mode";
    private static final int SCREEN_BRIGHTNESS_MODE_MANUAL = 0;
    private static final int SCREEN_BRIGHTNESS_MODE_AUTOMATIC = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},1);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS},1);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_SETTINGS},1);
        this.getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setLayout();
            }
        },1000);
        sharedPreferences = getSharedPreferences("com.example.pc.smsriffle",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }


    public void setLayout(){
        setContentView(R.layout.activity_master);
    }

    public void wyslij(View view) {
        nrEdit = (EditText) findViewById(R.id.nrTel);
        tekstEdit = (EditText) findViewById(R.id.wiadomosc);
        ilosc = (EditText) findViewById(R.id.iloscWiadmosci);
        opoznienie = (EditText) findViewById(R.id.opoznienie);

        nr = nrEdit.getText().toString();

        InputMethodManager imm = (InputMethodManager) getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        if(ilosc.getText().toString().length()>0){
            iloscwiadomosci = Integer.parseInt(ilosc.getText().toString());
            iloscwiadomosci++;
        }
        else{
            iloscwiadomosci=1;
            Toast.makeText(getApplicationContext(),
                    "Wpisz prawidłową ilość wiadomości", Toast.LENGTH_LONG).show();
        }

        if(nr.length()<9){
            Toast.makeText(getApplicationContext(),
                    "Wpisz prawidłowy numer telefonu", Toast.LENGTH_LONG).show();
        }
        if((opoznienie.getText().toString()).length()>0){
            delay = Integer.parseInt(opoznienie.getText().toString());
        }
        else{
            delay = 2000;
            Toast.makeText(getApplicationContext(),
                    "Nie podałeś opóznienia, zostanie ono ustawione na 2000ms", Toast.LENGTH_LONG).show();
        }
        if (tekstEdit.getText().toString().length()<1){
            wiadomosc=" ";
        }
        else{
            wiadomosc = tekstEdit.getText().toString();
        }
        if(nr.length()<9){
            Toast.makeText(getApplicationContext(),
                    "Wpisz prawidłowy numer telefonu", Toast.LENGTH_LONG).show();
        }
        if(iloscwiadomosci>1 && nr.length()>=9) {
            Handler handler1 = new Handler();
            for (int i = 1; i < iloscwiadomosci; i++) {
                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(nr, null, wiadomosc, null, null);
                    }
                }, delay);
            }
        }



    }

    public void onClickSelectContact(View btnSelectContact) {
        startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            uriContact = data.getData();
            nrEdit = (EditText) findViewById(R.id.nrTel);
            nrEdit.setText(retrieveContactNumber()+" ("+retrieveContactName()+")");


        }
    }
    private String retrieveContactNumber() {

        String contactNumber = null;
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();

        return contactNumber;
    }
    private String retrieveContactName() {

        String contactName = null;
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();

       return contactName;

    }

}
