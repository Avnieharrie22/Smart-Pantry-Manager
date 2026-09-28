package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PantryListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPantry;
    private DatabaseHelper databaseHelper;
    private PantryAdapter pantryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_pantry_list);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
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

        recyclerViewPantry = findViewById(R.id.recyclerViewPantry);

        recyclerViewPantry.setLayoutManager(
                new LinearLayoutManager(this)
        );

        databaseHelper = new DatabaseHelper(this);

        List<PantryItem> pantryItems = databaseHelper.getAllPantryItems();

        pantryAdapter = new PantryAdapter(pantryItems, databaseHelper);

        recyclerViewPantry.setAdapter(pantryAdapter);

        // Back button
        ImageButton backToPantryButton =
                findViewById(R.id.backToPantryButton);

        backToPantryButton.setOnClickListener(v -> {
            finish();
        });
    }
}

