package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpiringSoonActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerView;
    private TextView emptyText;

    private List<PantryItem> expiringItems;
    private PantryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_expiring_soon);

        // Back button
        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        // Connect views
        recyclerView = findViewById(R.id.recyclerViewExpiring);
        emptyText = findViewById(R.id.emptyText);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        // Open database
        databaseHelper = new DatabaseHelper(this);

        // Load expiring items
        loadExpiringItems();
    }

    private void loadExpiringItems() {

        // DatabaseHelper now returns a List<PantryItem>
        expiringItems = databaseHelper.getExpiringSoonItems();

        if (expiringItems == null || expiringItems.isEmpty()) {

            recyclerView.setVisibility(
                    RecyclerView.GONE
            );

            emptyText.setVisibility(
                    TextView.VISIBLE
            );

        } else {

            recyclerView.setVisibility(
                    RecyclerView.VISIBLE
            );

            emptyText.setVisibility(
                    TextView.GONE
            );

            adapter = new PantryAdapter(
                    expiringItems,
                    databaseHelper
            );

            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh the list when returning to this screen
        if (databaseHelper != null) {
            loadExpiringItems();
        }
    }
}