package com.example.nplapp2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;



public class MainActivity extends AppCompatActivity {

    private TextView mobileTimeTextView;
    private TextView ntpTimeTextView;
    private TextView timeDifferenceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mobileTimeTextView = findViewById(R.id.mobileTimeTextView);
        ntpTimeTextView = findViewById(R.id.ntpTimeTextView);
        timeDifferenceTextView = findViewById(R.id.timeDifferenceTextView);

        Button syncButton = findViewById(R.id.syncButton);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get mobile time
                // Note: You need to implement the logic to get the mobile time as per your requirements
                String mobileTime = getMobileTime();
                mobileTimeTextView.setText("Mobile Time: " + mobileTime);

                // Fetch NTP time
                new FetchNTPTime().execute();
            }
        });
    }

    // AsyncTask to fetch NTP time
    private class FetchNTPTime extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // URL of the NPL time server
                URL url = new URL("http://time.nplindia.org");

                // Open a connection to the URL
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    // Read the response from the server
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    // Read the first line of the response (which contains the time)
                    String ntpTime = reader.readLine();

                    // Close the reader
                    reader.close();

                    return ntpTime;
                } finally {
                    // Disconnect the URL connection
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String ntpTime) {
            if (ntpTime != null) {
                ntpTimeTextView.setText("NTP Time: " + ntpTime);

                // Calculate time difference
                String mobileTime = mobileTimeTextView.getText().toString().replace("Mobile Time: ", "");
                String[] mobileTimeParts = mobileTime.split(":");
                String[] ntpTimeParts = ntpTime.split(":");

                long mobileMillis = calculateMilliseconds(mobileTimeParts);
                long ntpMillis = calculateMilliseconds(ntpTimeParts);
                long timeDifference = Math.abs(mobileMillis - ntpMillis);

                // Format time difference
                String formattedTimeDifference = String.format(
                        Locale.getDefault(),
                        "%02d:%02d:%02d:%03d",
                        timeDifference / 3600000,
                        (timeDifference % 3600000) / 60000,
                        (timeDifference % 60000) / 1000,
                        timeDifference % 1000
                );

                timeDifferenceTextView.setText("Time Difference: " + formattedTimeDifference);
            } else {
                // Handle the case where the NTP time couldn't be retrieved
                ntpTimeTextView.setText("NTP Time: Unable to fetch");
                timeDifferenceTextView.setText("Time Difference: N/A");
            }
        }

        private long calculateMilliseconds(String[] timeParts) {
            int hours = Integer.parseInt(timeParts[0]);
            int minutes = Integer.parseInt(timeParts[1]);
            int seconds = Integer.parseInt(timeParts[2]);
            int milliseconds = Integer.parseInt(timeParts[3]);

            return hours * 3600000L + minutes * 60000L + seconds * 1000L + milliseconds;
        }
    }

    // Implement the logic to get mobile time
    private String getMobileTime() {
            // Get the current time in milliseconds
            long currentTimeMillis = System.currentTimeMillis();

            // Create a SimpleDateFormat object to format the time
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault());

            // Format the current time
            return dateFormat.format(new Date(currentTimeMillis));
        }
    }

