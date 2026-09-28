package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddItemActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private EditText ingredientNameInput;
    private EditText quantityInput;
    private EditText expiryDateInput;
    private Spinner unitSpinner;

    // Used when editing an existing pantry item
    private int editingItemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_item);

        databaseHelper = new DatabaseHelper(this);

        ingredientNameInput = findViewById(R.id.ingredientNameInput);
        quantityInput = findViewById(R.id.quantityInput);
        unitSpinner = findViewById(R.id.unitSpinner);
        expiryDateInput = findViewById(R.id.expiryDateInput);

        Button saveItemButton = findViewById(R.id.saveItemButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        setupUnitSpinner();

        // Check whether this screen was opened to edit an existing item
        editingItemId = getIntent().getIntExtra("ITEM_ID", -1);

        if (editingItemId != -1) {
            loadItemForEditing();
        }

        expiryDateInput.setOnClickListener(v -> showDatePicker());

        saveItemButton.setOnClickListener(v -> saveItem());

        cancelButton.setOnClickListener(v -> finish());
    }

    private void setupUnitSpinner() {

        String[] units = {
                "Select Unit",
                "Piece",
                "g",
                "kg",
                "ml",
                "L",
                "Pack",
                "Cup",
                "tbsp",
                "tsp",
                "Slice"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                units
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        unitSpinner.setAdapter(adapter);
    }

    private void loadItemForEditing() {

        PantryItem item = databaseHelper.getPantryItemById(editingItemId);

        if (item == null) {

            Toast.makeText(
                    this,
                    "Item could not be found",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        // Fill in the existing information
        ingredientNameInput.setText(item.getName());

        quantityInput.setText(
                String.valueOf(item.getQuantity())
        );

        if (item.getExpiryDate() != null) {

            expiryDateInput.setText(
                    item.getExpiryDate()
            );
        }

        // Set the existing unit in the spinner
        String databaseUnit = item.getUnit();

        String spinnerUnit = convertUnitForSpinner(databaseUnit);

        ArrayAdapter adapter =
                (ArrayAdapter) unitSpinner.getAdapter();

        int position = adapter.getPosition(spinnerUnit);

        if (position >= 0) {

            unitSpinner.setSelection(position);
        }
    }

    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {

                            String formattedDate =
                                    String.format(
                                            "%04d-%02d-%02d",
                                            selectedYear,
                                            selectedMonth + 1,
                                            selectedDay
                                    );

                            expiryDateInput.setText(formattedDate);
                        },
                        year,
                        month,
                        day
                );

        datePickerDialog.show();
    }

    private void saveItem() {

        String name =
                ingredientNameInput.getText()
                        .toString()
                        .trim();

        String quantityText =
                quantityInput.getText()
                        .toString()
                        .trim();

        String unit =
                unitSpinner.getSelectedItem()
                        .toString();

        String expiryDate =
                expiryDateInput.getText()
                        .toString()
                        .trim();

        // Ingredient name validation
        if (name.isEmpty()) {

            ingredientNameInput.setError(
                    "Please enter an ingredient name"
            );

            ingredientNameInput.requestFocus();
            return;
        }

        // Quantity validation
        if (quantityText.isEmpty()) {

            quantityInput.setError(
                    "Please enter a quantity"
            );

            quantityInput.requestFocus();
            return;
        }

        double quantity;

        try {

            quantity = Double.parseDouble(quantityText);

        } catch (NumberFormatException e) {

            quantityInput.setError(
                    "Please enter a valid quantity"
            );

            quantityInput.requestFocus();
            return;
        }

        if (quantity <= 0) {

            quantityInput.setError(
                    "Quantity must be greater than 0"
            );

            quantityInput.requestFocus();
            return;
        }

        // Unit validation
        if (unit.equals("Select Unit")) {

            Toast.makeText(
                    this,
                    "Please select a unit",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String databaseUnit =
                convertUnitForDatabase(unit);

        long result;

        // EDIT EXISTING ITEM
        if (editingItemId != -1) {

            result = databaseHelper.updatePantryItem(
                    editingItemId,
                    name,
                    quantity,
                    databaseUnit,
                    expiryDate
            );

            if (result != -1) {

                Toast.makeText(
                        this,
                        "Item updated successfully!",
                        Toast.LENGTH_SHORT
                ).show();

                finish();

            } else {

                Toast.makeText(
                        this,
                        "Failed to update item",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } else {

            // ADD NEW ITEM
            result = databaseHelper.addPantryItem(
                    name,
                    quantity,
                    databaseUnit,
                    expiryDate
            );

            if (result != -1) {

                Toast.makeText(
                        this,
                        "Item saved successfully!",
                        Toast.LENGTH_SHORT
                ).show();

                finish();

            } else {

                Toast.makeText(
                        this,
                        "Failed to save item",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private String convertUnitForDatabase(String unit) {

        switch (unit) {

            case "Piece":
                return "piece";

            case "g":
                return "grams";

            case "kg":
                return "kg";

            case "ml":
                return "ml";

            case "L":
                return "L";

            case "Pack":
                return "pack";

            case "Cup":
                return "cup";

            case "tbsp":
                return "tablespoon";

            case "tsp":
                return "teaspoon";

            case "Slice":
                return "slice";

            default:
                return unit.toLowerCase();
        }
    }

    private String convertUnitForSpinner(String unit) {

        switch (unit) {

            case "piece":
            case "pieces":
                return "Piece";

            case "grams":
            case "gram":
                return "g";

            case "kg":
                return "kg";

            case "ml":
                return "ml";

            case "L":
                return "L";

            case "pack":
                return "Pack";

            case "cup":
                return "Cup";

            case "tablespoon":
            case "tablespoons":
                return "tbsp";

            case "teaspoon":
            case "teaspoons":
                return "tsp";

            case "slice":
            case "slices":
                return "Slice";

            default:
                return unit;
        }
    }
}