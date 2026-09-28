package com.example.smartpantrymanager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchPantryActivity extends AppCompatActivity {

    private EditText searchEditText;
    private RecyclerView recyclerViewSearch;
    private TextView emptySearchText;

    private DatabaseHelper databaseHelper;
    private PantryAdapter pantryAdapter;

    private List<PantryItem> allItems;
    private List<PantryItem> filteredItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_pantry);

        searchEditText = findViewById(R.id.searchEditText);
        recyclerViewSearch = findViewById(R.id.recyclerViewSearch);

        databaseHelper = new DatabaseHelper(this);

        recyclerViewSearch.setLayoutManager(
                new LinearLayoutManager(this)
        );

        allItems = databaseHelper.getAllPantryItems();

        filteredItems = new ArrayList<>(allItems);

        pantryAdapter = new PantryAdapter(
                filteredItems,
                databaseHelper
        );

        recyclerViewSearch.setAdapter(pantryAdapter);

        // Back button
        ImageButton backButton =
                findViewById(R.id.backToPantryButton);

        backButton.setOnClickListener(v -> finish());

        // Empty search message
        emptySearchText = new TextView(this);
        emptySearchText.setText(
                "No matching ingredients found.\nTry searching for something else."
        );
        emptySearchText.setTextSize(14);
        emptySearchText.setTextColor(
                android.graphics.Color.rgb(125, 116, 111)
        );
        emptySearchText.setGravity(android.view.Gravity.CENTER);
        emptySearchText.setPadding(30, 30, 30, 30);

        addContentView(
                emptySearchText,
                new android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        emptySearchText.setVisibility(View.GONE);

        searchEditText.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        filterPantry(s.toString());
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );
    }

    private void filterPantry(String searchText) {

        filteredItems.clear();

        String search =
                searchText.toLowerCase().trim();

        if (search.isEmpty()) {

            filteredItems.addAll(allItems);

        } else {

            for (PantryItem item : allItems) {

                if (item.getName()
                        .toLowerCase()
                        .contains(search)) {

                    filteredItems.add(item);
                }
            }
        }

        pantryAdapter.notifyDataSetChanged();

        if (!search.isEmpty() && filteredItems.isEmpty()) {

            emptySearchText.setText(
                    "No matching ingredients found.\n" +
                            "Try searching for something else."
            );

            emptySearchText.setVisibility(View.VISIBLE);

        } else {

            emptySearchText.setVisibility(View.GONE);
        }
    }
}

