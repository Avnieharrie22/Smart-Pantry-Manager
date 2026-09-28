package com.example.smartpantrymanager;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerViewRecipes;
    private TextView noRecipesText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_suggested_recipes);

        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        recyclerViewRecipes = findViewById(R.id.recyclerViewRecipes);
        noRecipesText = findViewById(R.id.noRecipesText);

        recyclerViewRecipes.setLayoutManager(
                new LinearLayoutManager(this)
        );

        databaseHelper = new DatabaseHelper(this);

        loadSuggestedRecipes();

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
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
    }

    private void loadSuggestedRecipes() {

        List<String> matchingRecipes = new ArrayList<>();

        // =====================================================
        // LOAD ALL RECIPES
        // =====================================================

        Cursor recipeCursor =
                databaseHelper.getReadableDatabase().rawQuery(
                        "SELECT id, name FROM recipes",
                        null
                );

        if (recipeCursor.moveToFirst()) {

            do {

                int recipeId =
                        recipeCursor.getInt(
                                recipeCursor.getColumnIndexOrThrow("id")
                        );

                String recipeName =
                        recipeCursor.getString(
                                recipeCursor.getColumnIndexOrThrow("name")
                        );

                // Check whether the entire recipe can be made
                // using the current pantry.

                if (recipeMatchesPantry(recipeId)) {

                    matchingRecipes.add(recipeName);
                }

            } while (recipeCursor.moveToNext());
        }

        recipeCursor.close();

        // =====================================================
        // DISPLAY RESULTS
        // =====================================================

        if (!matchingRecipes.isEmpty()) {

            recyclerViewRecipes.setVisibility(View.VISIBLE);
            noRecipesText.setVisibility(View.GONE);

            SimpleRecipeAdapter adapter =
                    new SimpleRecipeAdapter(
                            this,
                            matchingRecipes
                    );

            recyclerViewRecipes.setAdapter(adapter);

        } else {

            recyclerViewRecipes.setVisibility(View.GONE);
            noRecipesText.setVisibility(View.VISIBLE);

            noRecipesText.setText(
                    "No recipes available yet.\n\n" +
                            "Add more ingredients to your pantry " +
                            "to unlock recipes."
            );
        }
    }

    // =========================================================
    // STRICT RECIPE MATCHING
    // =========================================================

    private boolean recipeMatchesPantry(int recipeId) {

        // -----------------------------------------------------
        // Get the ingredients required by this recipe
        // -----------------------------------------------------

        Cursor recipeIngredientsCursor =
                databaseHelper.getReadableDatabase().rawQuery(
                        "SELECT ingredient_name, required_quantity, unit " +
                                "FROM recipe_ingredients " +
                                "WHERE recipe_id = ?",
                        new String[]{
                                String.valueOf(recipeId)
                        }
                );

        // A recipe without ingredients cannot be suggested.

        if (!recipeIngredientsCursor.moveToFirst()) {

            recipeIngredientsCursor.close();

            return false;
        }

        boolean recipeCanBeMade = true;

        // -----------------------------------------------------
        // Check EVERY required ingredient
        // -----------------------------------------------------

        do {

            String requiredIngredient =
                    recipeIngredientsCursor.getString(
                            recipeIngredientsCursor
                                    .getColumnIndexOrThrow(
                                            "ingredient_name"
                                    )
                    );

            double requiredQuantity =
                    recipeIngredientsCursor.getDouble(
                            recipeIngredientsCursor
                                    .getColumnIndexOrThrow(
                                            "required_quantity"
                                    )
                    );

            String requiredUnit =
                    recipeIngredientsCursor.getString(
                            recipeIngredientsCursor
                                    .getColumnIndexOrThrow(
                                            "unit"
                                    )
                    );

            boolean ingredientAvailable =
                    isIngredientAvailable(
                            requiredIngredient,
                            requiredQuantity,
                            requiredUnit
                    );

            // If even ONE ingredient is unavailable,
            // the entire recipe is rejected.

            if (!ingredientAvailable) {

                recipeCanBeMade = false;

                break;
            }

        } while (recipeIngredientsCursor.moveToNext());

        recipeIngredientsCursor.close();

        return recipeCanBeMade;
    }

    // =========================================================
    // CHECK ONE INGREDIENT
    // =========================================================

    private boolean isIngredientAvailable(
            String requiredIngredient,
            double requiredQuantity,
            String requiredUnit) {

        String normalisedRequiredIngredient =
                normaliseIngredientName(
                        requiredIngredient
                );

        String normalisedRequiredUnit =
                normaliseUnit(
                        requiredUnit
                );

        // -----------------------------------------------------
        // Look through every pantry item
        // -----------------------------------------------------

        Cursor pantryCursor =
                databaseHelper.getReadableDatabase().rawQuery(
                        "SELECT name, quantity, unit " +
                                "FROM pantry_items",
                        null
                );

        if (pantryCursor.moveToFirst()) {

            do {

                String pantryName =
                        pantryCursor.getString(
                                pantryCursor.getColumnIndexOrThrow(
                                        "name"
                                )
                        );

                double pantryQuantity =
                        pantryCursor.getDouble(
                                pantryCursor.getColumnIndexOrThrow(
                                        "quantity"
                                )
                        );

                String pantryUnit =
                        pantryCursor.getString(
                                pantryCursor.getColumnIndexOrThrow(
                                        "unit"
                                )
                        );

                String normalisedPantryIngredient =
                        normaliseIngredientName(
                                pantryName
                        );

                String normalisedPantryUnit =
                        normaliseUnit(
                                pantryUnit
                        );

                // -------------------------------------------------
                // STRICT MATCH
                // -------------------------------------------------
                //
                // Ingredient must match
                // AND
                // Quantity must be enough
                // AND
                // Unit must match
                //
                // Example:
                //
                // Required:
                // 2 eggs
                //
                // Pantry:
                // 2 eggs
                //
                // = MATCH
                //
                // Required:
                // 2 eggs
                //
                // Pantry:
                // 1 egg
                //
                // = NO MATCH
                //
                // -------------------------------------------------

                if (normalisedRequiredIngredient.equals(
                        normalisedPantryIngredient
                )
                        && pantryQuantity >= requiredQuantity
                        && normalisedRequiredUnit.equals(
                        normalisedPantryUnit
                )) {

                    pantryCursor.close();

                    return true;
                }

            } while (pantryCursor.moveToNext());
        }

        pantryCursor.close();

        return false;
    }

    // =========================================================
    // NORMALISE INGREDIENT NAMES
    // =========================================================

    private String normaliseIngredientName(
            String ingredientName) {

        if (ingredientName == null) {
            return "";
        }

        String name =
                ingredientName
                        .trim()
                        .toLowerCase();

        // Eggs

        if (name.equals("egg")
                || name.equals("eggs")) {

            return "egg";
        }

        // Tomatoes

        if (name.equals("tomato")
                || name.equals("tomatoes")) {

            return "tomato";
        }

        // Onions

        if (name.equals("onion")
                || name.equals("onions")) {

            return "onion";
        }

        // Potatoes

        if (name.equals("potato")
                || name.equals("potatoes")) {

            return "potato";
        }

        // Bananas

        if (name.equals("banana")
                || name.equals("bananas")) {

            return "banana";
        }

        // Carrots

        if (name.equals("carrot")
                || name.equals("carrots")) {

            return "carrot";
        }

        // Peas

        if (name.equals("pea")
                || name.equals("peas")) {

            return "pea";
        }

        // Cheese

        if (name.equals("cheese")) {

            return "cheese";
        }

        // Bread

        if (name.equals("bread")) {

            return "bread";
        }

        // Rice

        if (name.equals("rice")) {

            return "rice";
        }

        // Pasta

        if (name.equals("pasta")) {

            return "pasta";
        }

        // Milk

        if (name.equals("milk")) {

            return "milk";
        }

        // Oil

        if (name.equals("oil")) {

            return "oil";
        }

        // General plural handling

        if (name.endsWith("s")
                && name.length() > 1) {

            return name.substring(
                    0,
                    name.length() - 1
            );
        }

        return name;
    }

    // =========================================================
    // NORMALISE UNITS
    // =========================================================

    private String normaliseUnit(String unit) {

        if (unit == null) {
            return "";
        }

        String cleanedUnit =
                unit.trim().toLowerCase();

        // -----------------------------------------------------
        // Piece / Unit
        // -----------------------------------------------------

        if (cleanedUnit.equals("unit")
                || cleanedUnit.equals("units")
                || cleanedUnit.equals("piece")
                || cleanedUnit.equals("pieces")
                || cleanedUnit.equals("pc")
                || cleanedUnit.equals("pcs")) {

            return "piece";
        }

        // -----------------------------------------------------
        // Grams
        // -----------------------------------------------------

        if (cleanedUnit.equals("g")
                || cleanedUnit.equals("gram")
                || cleanedUnit.equals("grams")) {

            return "gram";
        }

        // -----------------------------------------------------
        // Kilograms
        // -----------------------------------------------------

        if (cleanedUnit.equals("kg")
                || cleanedUnit.equals("kilogram")
                || cleanedUnit.equals("kilograms")) {

            return "kg";
        }

        // -----------------------------------------------------
        // Millilitres
        // -----------------------------------------------------

        if (cleanedUnit.equals("ml")
                || cleanedUnit.equals("millilitre")
                || cleanedUnit.equals("millilitres")
                || cleanedUnit.equals("milliliter")
                || cleanedUnit.equals("milliliters")) {

            return "ml";
        }

        // -----------------------------------------------------
        // Litres
        // -----------------------------------------------------

        if (cleanedUnit.equals("l")
                || cleanedUnit.equals("litre")
                || cleanedUnit.equals("litres")
                || cleanedUnit.equals("liter")
                || cleanedUnit.equals("liters")) {

            return "litre";
        }

        // -----------------------------------------------------
        // Tablespoons
        // -----------------------------------------------------

        if (cleanedUnit.equals("tbsp")
                || cleanedUnit.equals("tablespoon")
                || cleanedUnit.equals("tablespoons")) {

            return "tablespoon";
        }

        // -----------------------------------------------------
        // Teaspoons
        // -----------------------------------------------------

        if (cleanedUnit.equals("tsp")
                || cleanedUnit.equals("teaspoon")
                || cleanedUnit.equals("teaspoons")) {

            return "teaspoon";
        }

        // -----------------------------------------------------
        // Slices
        // -----------------------------------------------------

        if (cleanedUnit.equals("slice")
                || cleanedUnit.equals("slices")) {

            return "slice";
        }

        // -----------------------------------------------------
        // Packs
        // -----------------------------------------------------

        if (cleanedUnit.equals("pack")
                || cleanedUnit.equals("packs")) {

            return "pack";
        }

        // -----------------------------------------------------
        // Cups
        // -----------------------------------------------------

        if (cleanedUnit.equals("cup")
                || cleanedUnit.equals("cups")) {

            return "cup";
        }

        return cleanedUnit;
    }

    // =========================================================
    // RELOAD WHEN RETURNING TO SCREEN
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (databaseHelper != null) {

            loadSuggestedRecipes();
        }
    }
}