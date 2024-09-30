package com.example.streamvibe;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class StreamActivity extends AppCompatActivity {

    Button goLiveBtn;
    TextInputEditText liveIdInput, nameInput;

    String liveID, name, userID;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        goLiveBtn = findViewById(R.id.go_live_btn);
        liveIdInput = findViewById(R.id.live_id_input);
        nameInput = findViewById(R.id.name_input);

        // Get the live ID from the Intent extras
        String liveIDFromIntent = getIntent().getStringExtra("live_id");
        if (liveIDFromIntent != null) {
            liveIdInput.setText(liveIDFromIntent); // Set the live ID in the input field
        }

        // Update the button text based on the live ID
        updateGoLiveButtonText();

        liveIdInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                liveID = Objects.requireNonNull(liveIdInput.getText()).toString();
                updateGoLiveButtonText();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        goLiveBtn.setOnClickListener((v) -> {
            name = nameInput.getText().toString();

            if (name.isEmpty()) {
                nameInput.setError("Name is required");
                nameInput.requestFocus();
                return;
            }

            liveID = liveIdInput.getText().toString();

            if (!liveID.isEmpty() && liveID.length() != 5) {
                liveIdInput.setError("Invalid Live ID");
                liveIdInput.requestFocus();
                return;
            }
            startMeeting();
        });
    }

    private void startMeeting() {
        Log.i("StreamActivity", "Starting meeting");

        boolean isHost = true;
        if (liveID.isEmpty()) {
            isHost = true;
            liveID = generateLiveID();
        } else {
            isHost = false; // Joining an existing live stream
        }

        userID = UUID.randomUUID().toString();

        // Starting LiveActivity with required data
        Intent intent = new Intent(StreamActivity.this, LiveActivity.class);
        intent.putExtra("user_id", userID);
        intent.putExtra("name", name);
        intent.putExtra("live_id", liveID);
        intent.putExtra("host", isHost);  // Set if the user is the host or joining
        startActivity(intent);  // This will open LiveActivity
    }

    private String generateLiveID() {
        StringBuilder id = new StringBuilder();
        while (id.length() != 5) {
            int random = new Random().nextInt(10);
            id.append(random);
        }
        return id.toString();
    }

    // Method to update the button text based on live ID
    private void updateGoLiveButtonText() {
        if (liveIdInput.getText() != null && liveIdInput.getText().toString().length() == 5) {
            goLiveBtn.setText("Join a live");
        } else {
            goLiveBtn.setText("Start new live");
        }
    }
}
