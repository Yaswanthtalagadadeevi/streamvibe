package com.example.streamvibe;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LiveActivity extends AppCompatActivity {

    String userID, name, liveID;
    boolean isHost;

    TextView liveIdText;
    ImageView shareBtn;

    FirebaseFirestore firestore; // Firebase Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        liveIdText = findViewById(R.id.live_id_textview);
        shareBtn = findViewById(R.id.share_btn);

        firestore = FirebaseFirestore.getInstance(); // Initialize Firestore

        userID = getIntent().getStringExtra("user_id");
        name = getIntent().getStringExtra("name");
        liveID = getIntent().getStringExtra("live_id");
        isHost = getIntent().getBooleanExtra("host", false);

        liveIdText.setText(liveID);
        addFragment();

        shareBtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "Join my live in StreamVibe app \n Live ID : " + liveID);
            startActivity(Intent.createChooser(intent, "Share via"));
        });

        // Show the BottomSheetDialog when the activity starts, only if the user is the host
        if (isHost) {
            showBottomSheetDialog();
        }
    }

    void addFragment() {
        ZegoUIKitPrebuiltLiveStreamingConfig config;
        if (isHost) {
            config = ZegoUIKitPrebuiltLiveStreamingConfig.host();
        } else {
            config = ZegoUIKitPrebuiltLiveStreamingConfig.audience();
        }

        ZegoUIKitPrebuiltLiveStreamingFragment fragment = ZegoUIKitPrebuiltLiveStreamingFragment.newInstance(
                AppConstants.appID, AppConstants.appSign, userID, name, liveID, config);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow();
    }

    // Function to show the BottomSheetDialog
    void showBottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(LiveActivity.this);
        View dialogView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_live_details, findViewById(R.id.main), false);

        EditText titleInput = dialogView.findViewById(R.id.title_input);
        EditText descriptionInput = dialogView.findViewById(R.id.description_input);
        EditText liveIdInput = dialogView.findViewById(R.id.liveid_input); // New Live ID input
        TextView startDateTime = dialogView.findViewById(R.id.start_date_time);
        TextView endDateTime = dialogView.findViewById(R.id.end_date_time);
        Button submitButton = dialogView.findViewById(R.id.submit_button);

        // Date and Time Picker
        startDateTime.setOnClickListener(v -> showDateTimePicker(startDateTime));
        endDateTime.setOnClickListener(v -> showDateTimePicker(endDateTime));

        submitButton.setOnClickListener(v -> {
            // Get details from the input fields
            String title = titleInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String liveId = liveIdInput.getText().toString().trim(); // Get Live ID
            String start = startDateTime.getText().toString().trim();
            String end = endDateTime.getText().toString().trim();

            // Store details in Firebase
            Map<String, Object> liveDetails = new HashMap<>();
            liveDetails.put("title", title);
            liveDetails.put("description", description);
            liveDetails.put("start", start);
            liveDetails.put("end", end);
            liveDetails.put("liveId", liveId);

            // Save stream details to Firestore
            firestore.collection("liveStreams").document(liveId)
                    .set(liveDetails)
                    .addOnSuccessListener(aVoid -> {
                        // Successfully added to Firebase
                        bottomSheetDialog.dismiss();
                        Toast.makeText(LiveActivity.this, "Stream details saved!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Handle the error
                        e.printStackTrace();
                        Toast.makeText(LiveActivity.this, "Error saving stream details!", Toast.LENGTH_SHORT).show();
                    });
        });

        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();
    }

    // Function to show Date and Time picker
    void showDateTimePicker(final TextView textView) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(LiveActivity.this, (view, year, month, dayOfMonth) -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(LiveActivity.this, (timeView, hourOfDay, minute) -> {
                calendar.set(year, month, dayOfMonth, hourOfDay, minute);
                // Use SimpleDateFormat to format the date and time
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                textView.setText(sdf.format(calendar.getTime())); // Format and set the date and time
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
            timePickerDialog.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}
