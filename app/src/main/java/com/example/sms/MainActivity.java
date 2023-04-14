package com.example.sms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;

    private TextView messageTextView;
    private EditText phoneNumberEditText, messageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, 1);
        }
Button buttonSensorSection = findViewById(R.id.buttonSensorSection);
        buttonSensorSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent screen = new Intent(getApplicationContext(), Sensory.class);
                    startActivity(screen);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        Button buttonSendWithAzimuth = findViewById(R.id.buttonSendWithAzimuth);

        buttonSendWithAzimuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = phoneNumberEditText.getText().toString();
                String message = messageEditText.getText().toString() + Sensory.azymutPublic;
                if (phoneNumber.isEmpty() || message.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Proszę uzupełnić numer i wiadomość", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.SEND_SMS},
                            MY_PERMISSIONS_REQUEST_SEND_SMS);
                } else {
                    sendSMS(phoneNumber, message);
                }
            }
        });
        Button showLastSmsButton = findViewById(R.id.showLastSmsButton);
        showLastSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String lastSms = getLastSms();
                    if (lastSms != null) {
                        messageTextView.setText(lastSms);
                    } else {
                        messageTextView.setText("No SMS found");
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        messageTextView = findViewById(R.id.messageTextView);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        messageEditText = findViewById(R.id.messageEditText);
        messageTextView.setText("Test onCreate");
        // Ustawiamy listenera dla przycisku "Wyślij"
        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = phoneNumberEditText.getText().toString();
                String message = messageEditText.getText().toString();
                if (phoneNumber.isEmpty() || message.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Proszę uzupełnić numer i wiadomość", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.SEND_SMS},
                            MY_PERMISSIONS_REQUEST_SEND_SMS);
                } else {
                    sendSMS(phoneNumber, message);
                }
            }
        });
    }

    // Metoda do wysyłania SMS
    private void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Toast.makeText(this, "SMS wysłany", Toast.LENGTH_SHORT).show();
    }

    // Metoda wywoływana po uzyskaniu uprawnienia do wysyłania SMS
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String phoneNumber = phoneNumberEditText.getText().toString();
                    String message = messageEditText.getText().toString();
                    sendSMS(phoneNumber, message);
                } else {
                    Toast.makeText(MainActivity.this,
                            "Nie można wysłać wiadomości. Brak uprawnień",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }
                    final String messageText = messages[0].getMessageBody();

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messageTextView.setText(messageText);
                        }
                    });
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        registerReceiver(smsReceiver, filter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(smsReceiver);
    }
    @SuppressLint("Range")
    private String getLastSms() {
        String lastSms = null;
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        if (cursor.moveToFirst()) {
            lastSms = cursor.getString(cursor.getColumnIndex("body"));
        }
        cursor.close();
        return lastSms;
    }


}