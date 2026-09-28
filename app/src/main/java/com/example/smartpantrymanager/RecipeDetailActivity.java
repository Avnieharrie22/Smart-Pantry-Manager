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

public class RecipeDetailActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private TextView recipeTitleText;
    private TextView ingredientsText;
    private TextView instructionsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_recipe_detail);

        ImageButton backButton = findViewById(R.id.backButton);

        recipeTitleText = findViewById(R.id.recipeTitleText);
        ingredientsText = findViewById(R.id.ingredientsText);
        instructionsText = findViewById(R.id.instructionsText);

        backButton.setOnClickListener(v -> finish());

        databaseHelper = new DatabaseHelper(this);

        String recipeName = getIntent().getStringExtra("RECIPE_NAME");

        if (recipeName != null) {
            loadRecipeDetails(recipeName);
        }

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
    }

    private void loadRecipeDetails(String recipeName) {

        Cursor recipeCursor =
                databaseHelper.getReadableDatabase().rawQuery(
                        "SELECT id, name, instructions " +
                                "FROM recipes " +
                                "WHERE LOWER(TRIM(name)) = LOWER(TRIM(?))",
                        new String[]{recipeName}
                );

        if (recipeCursor.moveToFirst()) {

            int recipeId =
                    recipeCursor.getInt(
                            recipeCursor.getColumnIndexOrThrow("id")
                    );

            String name =
                    recipeCursor.getString(
                            recipeCursor.getColumnIndexOrThrow("name")
                    );

            String instructions =
                    recipeCursor.getString(
                            recipeCursor.getColumnIndexOrThrow("instructions")
                    );

            recipeTitleText.setText(name);
            instructionsText.setText(instructions);

            loadIngredients(recipeId);

        } else {

            recipeTitleText.setText("Recipe Details");
            ingredientsText.setText("Recipe information not found.");
            instructionsText.setText("");
        }

        recipeCursor.close();
    }

    private void loadIngredients(int recipeId) {

        Cursor ingredientCursor =
                databaseHelper.getReadableDatabase().rawQuery(
                        "SELECT ingredient_name, required_quantity, unit " +
                                "FROM recipe_ingredients " +
                                "WHERE recipe_id = ?",
                        new String[]{String.valueOf(recipeId)}
                );

        StringBuilder ingredients = new StringBuilder();

        if (ingredientCursor.moveToFirst()) {

            do {

                String ingredientName =
                        ingredientCursor.getString(
                                ingredientCursor.getColumnIndexOrThrow(
                                        "ingredient_name"
                                )
                        );

                double quantity =
                        ingredientCursor.getDouble(
                                ingredientCursor.getColumnIndexOrThrow(
                                        "required_quantity"
                                )
                        );

                String unit =
                        ingredientCursor.getString(
                                ingredientCursor.getColumnIndexOrThrow(
                                        "unit"
                                )
                        );

                ingredients.append("• ")
                        .append(formatIngredient(quantity, unit, ingredientName))
                        .append("\n");

            } while (ingredientCursor.moveToNext());

        } else {

            ingredients.append("No ingredients found.");
        }

        ingredientCursor.close();

        ingredientsText.setText(ingredients.toString());
    }

    private String formatIngredient(
            double quantity,
            String unit,
            String ingredientName
    ) {

        String formattedQuantity = formatQuantity(quantity);

        String cleanUnit = unit.toLowerCase().trim();
        String cleanName = ingredientName.trim();

        // Pieces / units
        if (cleanUnit.equals("piece")
                || cleanUnit.equals("pieces")
                || cleanUnit.equals("unit")
                || cleanUnit.equals("units")) {

            return formattedQuantity + " "
                    + makePluralIfNeeded(cleanName, quantity);
        }

        // Grams
        if (cleanUnit.equals("gram")
                || cleanUnit.equals("grams")
                || cleanUnit.equals("g")) {

            return formattedQuantity + " g " + cleanName;
        }

        // Kilograms
        if (cleanUnit.equals("kilogram")
                || cleanUnit.equals("kilograms")
                || cleanUnit.equals("kg")) {

            return formattedQuantity + " kg " + cleanName;
        }

        // Millilitres
        if (cleanUnit.equals("millilitre")
                || cleanUnit.equals("millilitres")
                || cleanUnit.equals("milliliter")
                || cleanUnit.equals("milliliters")
                || cleanUnit.equals("ml")) {

            return formattedQuantity + " ml " + cleanName;
        }

        // Litres
        if (cleanUnit.equals("litre")
                || cleanUnit.equals("litres")
                || cleanUnit.equals("liter")
                || cleanUnit.equals("liters")
                || cleanUnit.equals("l")) {

            return formattedQuantity + " L " + cleanName;
        }

        // Tablespoons
        if (cleanUnit.equals("tablespoon")
                || cleanUnit.equals("tablespoons")
                || cleanUnit.equals("tbsp")) {

            return formattedQuantity + " tbsp " + cleanName;
        }

        // Teaspoons
        if (cleanUnit.equals("teaspoon")
                || cleanUnit.equals("teaspoons")
                || cleanUnit.equals("tsp")) {

            return formattedQuantity + " tsp " + cleanName;
        }

        // Cups
        if (cleanUnit.equals("cup")
                || cleanUnit.equals("cups")) {

            return formattedQuantity + " cup"
                    + (quantity == 1 ? "" : "s")
                    + " " + cleanName;
        }

        // Slices
        if (cleanUnit.equals("slice")
                || cleanUnit.equals("slices")) {

            return formattedQuantity + " slice"
                    + (quantity == 1 ? "" : "s")
                    + " " + cleanName;
        }

        // Packs
        if (cleanUnit.equals("pack")
                || cleanUnit.equals("packs")) {

            return formattedQuantity + " pack"
                    + (quantity == 1 ? "" : "s")
                    + " " + cleanName;
        }

        // Fallback for any future units
        return formattedQuantity + " "
                + cleanUnit + " "
                + cleanName;
    }

    private String makePluralIfNeeded(
            String ingredientName,
            double quantity
    ) {

        if (quantity == 1) {
            return makeSingular(ingredientName);
        }

        String name = makeSingular(ingredientName);

        // Common irregular plurals
        if (name.equals("egg")) {
            return "eggs";
        }

        if (name.equals("tomato")) {
            return "tomatoes";
        }

        if (name.equals("potato")) {
            return "potatoes";
        }

        if (name.equals("onion")) {
            return "onions";
        }

        if (name.equals("carrot")) {
            return "carrots";
        }

        if (name.equals("banana")) {
            return "bananas";
        }

        if (name.equals("apple")) {
            return "apples";
        }

        if (name.equals("pepper")) {
            return "peppers";
        }

        if (name.endsWith("y")) {
            return name.substring(0, name.length() - 1) + "ies";
        }

        if (name.endsWith("s")) {
            return name;
        }

        return name + "s";
    }

    private String makeSingular(String ingredientName) {

        String name = ingredientName.toLowerCase().trim();

        if (name.equals("eggs")) {
            return "egg";
        }

        if (name.equals("tomatoes")) {
            return "tomato";
        }

        if (name.equals("potatoes")) {
            return "potato";
        }

        if (name.equals("onions")) {
            return "onion";
        }

        if (name.equals("carrots")) {
            return "carrot";
        }

        if (name.equals("bananas")) {
            return "banana";
        }

        if (name.equals("apples")) {
            return "apple";
        }

        if (name.equals("peppers")) {
            return "pepper";
        }

        if (name.endsWith("ies")) {
            return name.substring(0, name.length() - 3) + "y";
        }

        if (name.endsWith("s") && !name.endsWith("ss")) {
            return name.substring(0, name.length() - 1);
        }

        return name;
    }

    private String formatQuantity(double quantity) {

        if (quantity == (long) quantity) {
            return String.valueOf((long) quantity);
        }

        return String.valueOf(quantity);
    }
}