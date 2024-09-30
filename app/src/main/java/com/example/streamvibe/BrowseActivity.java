package com.example.streamvibe;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class BrowseActivity extends AppCompatActivity {

    LinearLayout mainLayout;
    FirebaseFirestore firestore; // Firebase Firestore instance

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        mainLayout = findViewById(R.id.mainLayout);
        firestore = FirebaseFirestore.getInstance(); // Initialize Firestore

        addDetailsToLayout();
    }

    private void addDetailsToLayout() {
        firestore.collection("liveStreams")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<StreamDetails> streamList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Get details from Firestore
                            String title = document.getString("title");
                            String description = document.getString("description");
                            String start = document.getString("start");
                            String end = document.getString("end");
                            String liveId = document.getString("liveId");

                            // Create a StreamDetails object and add it to the list
                            streamList.add(new StreamDetails(title, description, start, end, liveId));
                        }

                        // Sort streams based on status
                        streamList.sort(Comparator.comparing(this::getStreamStatusPriority));

                        // Create and add CardViews to the layout
                        for (StreamDetails stream : streamList) {
                            // Determine the status of the stream
                            String status = determineStreamStatus(stream.start, stream.end);

                            // Create and configure a new CardView for the details
                            CardView cardView = createCardView();

                            // Create a LinearLayout to hold the details
                            LinearLayout detailsLayout = createDetailsLayout(stream.title, stream.description, stream.start, stream.end, stream.liveId, status);

                            // Add detailsLayout to the CardView
                            cardView.addView(detailsLayout);

                            // Add CardView to the main layout
                            mainLayout.addView(cardView);
                        }
                    } else {
                        // Handle the error
                        showError(task.getException());
                    }
                });
    }

    private CardView createCardView() {
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 16); // Bottom margin for spacing
        cardView.setLayoutParams(cardParams);
        cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));
        cardView.setCardElevation(8f);
        cardView.setRadius(12f);
        return cardView;
    }

    private LinearLayout createDetailsLayout(String title, String description, String start, String end, String liveId, String status) {
        LinearLayout detailsLayout = new LinearLayout(this);
        detailsLayout.setOrientation(LinearLayout.VERTICAL);
        detailsLayout.setPadding(16, 16, 16, 16);

        // Create and add TextViews for each detail
        detailsLayout.addView(createTextView("Title: " + title, 20, android.R.color.black));
        detailsLayout.addView(createTextView("Description: " + description, 18, android.R.color.black));
        detailsLayout.addView(createTextView("Start: " + start, 18, android.R.color.black));
        detailsLayout.addView(createTextView("End: " + end, 18, android.R.color.black));
        detailsLayout.addView(createTextView("Live ID: " + liveId, 18, android.R.color.holo_blue_dark));

        // Add status TextView at the bottom
        TextView statusView = createTextView(status, 18, getStatusColor(status));
        detailsLayout.addView(statusView);

        // Create and add a button if the status is Streaming
        if ("Streaming".equals(status)) {
            Button streamButton = new Button(this);
            streamButton.setText("Join Stream");
            streamButton.setOnClickListener(v -> {
                Intent intent = new Intent(BrowseActivity.this, StreamActivity.class);
                intent.putExtra("live_id", liveId); // Pass the live ID
                startActivity(intent); // Start StreamActivity
            });
            detailsLayout.addView(streamButton); // Add the button to the details layout
        }

        return detailsLayout;
    }

    private TextView createTextView(String text, int textSize, int textColor) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(textSize);
        textView.setTextColor(getResources().getColor(textColor));
        return textView;
    }

    private String determineStreamStatus(String start, String end) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date startDate = dateFormat.parse(start);
            Date endDate = dateFormat.parse(end);
            Date currentDate = new Date();

            if (currentDate.before(startDate)) {
                return "Upcoming"; // Stream is in the future
            } else if (currentDate.after(endDate)) {
                return "Completed"; // Stream has ended
            } else {
                return "Streaming"; // Stream is currently live
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "Status Unknown"; // Fallback status
        }
    }

    // Method to get the priority of stream status for sorting
    private int getStreamStatusPriority(StreamDetails stream) {
        String status = determineStreamStatus(stream.start, stream.end);
        switch (status) {
            case "Streaming":
                return 1; // Highest priority
            case "Upcoming":
                return 2; // Medium priority
            case "Completed":
                return 3; // Lowest priority
            default:
                return 4; // Unknown status
        }
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "Streaming":
                return android.R.color.holo_green_dark; // Green for streaming
            case "Completed":
                return android.R.color.holo_red_dark; // Red for completed
            case "Upcoming":
                return android.R.color.holo_orange_dark; // Orange for upcoming
            default:
                return android.R.color.black; // Default color for unknown status
        }
    }

    private void showError(Exception e) {
        e.printStackTrace(); // Log the error
        TextView errorText = new TextView(this);
        errorText.setText("Error loading streams.");
        errorText.setTextColor(Color.RED);
        errorText.setTextSize(18);
        mainLayout.addView(errorText); // Show error message
    }

    // Inner class to hold stream details
    private static class StreamDetails {
        String title;
        String description;
        String start;
        String end;
        String liveId;

        StreamDetails(String title, String description, String start, String end, String liveId) {
            this.title = title;
            this.description = description;
            this.start = start;
            this.end = end;
            this.liveId = liveId;
        }
    }
}
