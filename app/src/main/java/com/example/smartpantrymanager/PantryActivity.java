package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class PantryActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private TextView summaryTitle;
    private TextView expiringCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_pantry);

        View mainView = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(
                mainView,
                (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        databaseHelper = new DatabaseHelper(this);

        // Dashboard summary
        summaryTitle = findViewById(R.id.summaryTitle);
        expiringCountText = findViewById(R.id.expiringCountText);

        updateDashboardSummary();


        // =========================
        // PROFILE
        // =========================

        TextView profileButton =
                findViewById(R.id.profileButton);

        profileButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    PantryActivity.this,
                    SettingsActivity.class
            );

            startActivity(intent);
        });


        // =========================
        // ADD ITEM
        // =========================

        View addItemButton =
                findViewById(R.id.addItemButton);

        addItemButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    PantryActivity.this,
                    AddItemActivity.class
            );

            startActivity(intent);
        });


        // =========================
        // MY PANTRY
        // =========================

        View myPantryButton =
                findViewById(R.id.myPantryButton);

        myPantryButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    PantryActivity.this,
                    PantryListActivity.class
            );

            startActivity(intent);
        });


        // =========================
        // EXPIRING SOON
        // =========================

        View expiringSoonButton =
                findViewById(R.id.expiringSoonButton);

        expiringSoonButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    PantryActivity.this,
                    ExpiringSoonActivity.class
            );

            startActivity(intent);
        });


        // =========================
        // SEARCH PANTRY
        // =========================

        View searchPantryButton =
                findViewById(R.id.searchPantryButton);

        searchPantryButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    PantryActivity.this,
                    SearchPantryActivity.class
            );

            startActivity(intent);
        });


        // =========================
        // SUGGESTED RECIPES
        // =========================

        View suggestedRecipesButton =
                findViewById(R.id.suggestedRecipesButton);

        suggestedRecipesButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    PantryActivity.this,
                    SuggestedRecipesActivity.class
            );

            startActivity(intent);
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null) {
            updateDashboardSummary();
        }
    }


    private void updateDashboardSummary() {

        List<PantryItem> pantryItems =
                databaseHelper.getAllPantryItems();

        // Total pantry items
        int totalItems = pantryItems.size();

        summaryTitle.setText(
                String.valueOf(totalItems)
        );


        // Expiring within the next 7 days
        int expiringSoon = 0;

        java.text.SimpleDateFormat dateFormat =
                new java.text.SimpleDateFormat(
                        "yyyy-MM-dd",
                        java.util.Locale.getDefault()
                );

        java.util.Date today =
                new java.util.Date();


        for (PantryItem item : pantryItems) {

            if (item.getExpiryDate() != null
                    && !item.getExpiryDate().isEmpty()) {

                try {

                    java.util.Date expiryDate =
                            dateFormat.parse(
                                    item.getExpiryDate()
                            );

                    long difference =
                            expiryDate.getTime()
                                    - today.getTime();

                    long days =
                            java.util.concurrent.TimeUnit.MILLISECONDS
                                    .toDays(difference);

                    if (days >= 0 && days <= 7) {
                        expiringSoon++;
                    }

                } catch (Exception e) {
                    // Ignore invalid expiry dates
                }
            }
        }


        expiringCountText.setText(
                String.valueOf(expiringSoon)
        );
    }
}