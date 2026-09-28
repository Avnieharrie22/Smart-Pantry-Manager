package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 8;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Pantry table
        db.execSQL(
                "CREATE TABLE pantry_items (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT, " +
                        "quantity REAL, " +
                        "unit TEXT, " +
                        "expiry_date TEXT)"
        );

        // Recipes table
        db.execSQL(
                "CREATE TABLE recipes (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT, " +
                        "instructions TEXT)"
        );

        // Recipe ingredients table
        db.execSQL(
                "CREATE TABLE recipe_ingredients (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "recipe_id INTEGER, " +
                        "ingredient_name TEXT, " +
                        "required_quantity REAL, " +
                        "unit TEXT)"
        );

        insertSampleRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Update recipe data without deleting pantry items.
        if (oldVersion < 8) {

            db.delete(
                    "recipe_ingredients",
                    null,
                    null
            );

            db.delete(
                    "recipes",
                    null,
                    null
            );

            insertSampleRecipes(db);
        }
    }

    // ============================================================
    // PANTRY METHODS
    // ============================================================

    public long addPantryItem(
            String name,
            double quantity,
            String unit,
            String expiryDate
    ) {

        SQLiteDatabase db = this.getWritableDatabase();

        // Check whether the same ingredient with the same unit
        // already exists in the pantry.
        Cursor cursor = db.rawQuery(
                "SELECT id, quantity, expiry_date FROM pantry_items " +
                        "WHERE LOWER(name) = LOWER(?) " +
                        "AND LOWER(unit) = LOWER(?) " +
                        "LIMIT 1",
                new String[]{
                        name.trim(),
                        unit.trim()
                }
        );

        if (cursor.moveToFirst()) {

            int existingId = cursor.getInt(
                    cursor.getColumnIndexOrThrow("id")
            );

            double existingQuantity = cursor.getDouble(
                    cursor.getColumnIndexOrThrow("quantity")
            );

            String existingExpiryDate = cursor.getString(
                    cursor.getColumnIndexOrThrow("expiry_date")
            );

            cursor.close();

            // Combine the existing quantity with the new quantity.
            double newQuantity =
                    existingQuantity + quantity;

            ContentValues values = new ContentValues();

            values.put(
                    "quantity",
                    newQuantity
            );

            // If a new expiry date was entered, use it.
            // Otherwise keep the existing expiry date.
            if (expiryDate != null && !expiryDate.isEmpty()) {

                values.put(
                        "expiry_date",
                        expiryDate
                );

            } else if (existingExpiryDate != null
                    && !existingExpiryDate.isEmpty()) {

                values.put(
                        "expiry_date",
                        existingExpiryDate
                );
            }

            return db.update(
                    "pantry_items",
                    values,
                    "id = ?",
                    new String[]{
                            String.valueOf(existingId)
                    }
            );

        } else {

            cursor.close();

            // No matching ingredient exists,
            // so create a new pantry item.
            ContentValues values = new ContentValues();

            values.put(
                    "name",
                    name
            );

            values.put(
                    "quantity",
                    quantity
            );

            values.put(
                    "unit",
                    unit
            );

            if (expiryDate == null || expiryDate.isEmpty()) {

                values.putNull(
                        "expiry_date"
                );

            } else {

                values.put(
                        "expiry_date",
                        expiryDate
                );
            }

            return db.insert(
                    "pantry_items",
                    null,
                    values
            );
        }
    }

    public List<PantryItem> getAllPantryItems() {

        List<PantryItem> pantryItems = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM pantry_items ORDER BY name ASC",
                null
        );

        if (cursor.moveToFirst()) {

            do {

                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id")
                );

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                );

                double quantity = cursor.getDouble(
                        cursor.getColumnIndexOrThrow("quantity")
                );

                String unit = cursor.getString(
                        cursor.getColumnIndexOrThrow("unit")
                );

                String expiryDate = cursor.getString(
                        cursor.getColumnIndexOrThrow("expiry_date")
                );

                PantryItem item = new PantryItem(
                        id,
                        name,
                        quantity,
                        unit,
                        expiryDate
                );

                pantryItems.add(item);

            } while (cursor.moveToNext());
        }

        cursor.close();

        return pantryItems;
    }

    public List<PantryItem> getExpiringSoonItems() {

        List<PantryItem> pantryItems = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM pantry_items " +
                        "WHERE expiry_date IS NOT NULL " +
                        "AND expiry_date != '' " +
                        "ORDER BY expiry_date ASC",
                null
        );

        if (cursor.moveToFirst()) {

            do {

                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id")
                );

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                );

                double quantity = cursor.getDouble(
                        cursor.getColumnIndexOrThrow("quantity")
                );

                String unit = cursor.getString(
                        cursor.getColumnIndexOrThrow("unit")
                );

                String expiryDate = cursor.getString(
                        cursor.getColumnIndexOrThrow("expiry_date")
                );

                PantryItem item = new PantryItem(
                        id,
                        name,
                        quantity,
                        unit,
                        expiryDate
                );

                pantryItems.add(item);

            } while (cursor.moveToNext());
        }

        cursor.close();

        return pantryItems;
    }

    // ============================================================
    // GET ONE PANTRY ITEM FOR EDITING
    // ============================================================

    public PantryItem getPantryItemById(int id) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM pantry_items WHERE id = ?",
                new String[]{
                        String.valueOf(id)
                }
        );

        PantryItem item = null;

        if (cursor.moveToFirst()) {

            int itemId = cursor.getInt(
                    cursor.getColumnIndexOrThrow("id")
            );

            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow("name")
            );

            double quantity = cursor.getDouble(
                    cursor.getColumnIndexOrThrow("quantity")
            );

            String unit = cursor.getString(
                    cursor.getColumnIndexOrThrow("unit")
            );

            String expiryDate = cursor.getString(
                    cursor.getColumnIndexOrThrow("expiry_date")
            );

            item = new PantryItem(
                    itemId,
                    name,
                    quantity,
                    unit,
                    expiryDate
            );
        }

        cursor.close();

        return item;
    }

    // ============================================================
    // UPDATE PANTRY ITEM
    // ============================================================

    public long updatePantryItem(
            int id,
            String name,
            double quantity,
            String unit,
            String expiryDate
    ) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                "name",
                name
        );

        values.put(
                "quantity",
                quantity
        );

        values.put(
                "unit",
                unit
        );

        if (expiryDate == null || expiryDate.isEmpty()) {

            values.putNull(
                    "expiry_date"
            );

        } else {

            values.put(
                    "expiry_date",
                    expiryDate
            );
        }

        return db.update(
                "pantry_items",
                values,
                "id = ?",
                new String[]{
                        String.valueOf(id)
                }
        );
    }

    // ============================================================
    // DELETE PANTRY ITEM
    // ============================================================

    public void deletePantryItem(int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(
                "pantry_items",
                "id = ?",
                new String[]{
                        String.valueOf(id)
                }
        );
    }

    // ============================================================
    // RECIPE INSERT METHODS
    // ============================================================

    private long insertRecipe(
            SQLiteDatabase db,
            String name,
            String instructions
    ) {

        ContentValues values = new ContentValues();

        values.put(
                "name",
                name
        );

        values.put(
                "instructions",
                instructions
        );

        return db.insert(
                "recipes",
                null,
                values
        );
    }

    private void insertIngredient(
            SQLiteDatabase db,
            long recipeId,
            String ingredientName,
            double quantity,
            String unit
    ) {

        ContentValues values = new ContentValues();

        values.put(
                "recipe_id",
                recipeId
        );

        values.put(
                "ingredient_name",
                ingredientName
        );

        values.put(
                "required_quantity",
                quantity
        );

        values.put(
                "unit",
                unit
        );

        db.insert(
                "recipe_ingredients",
                null,
                values
        );
    }

    // ============================================================
    // 15 SAMPLE RECIPES
    // ============================================================

    private void insertSampleRecipes(SQLiteDatabase db) {

        // 1. Vegetable Omelette
        long recipe1 = insertRecipe(
                db,
                "Vegetable Omelette",
                "1. Beat the eggs in a bowl.\n" +
                        "2. Chop the tomato and onion.\n" +
                        "3. Add the vegetables to the eggs.\n" +
                        "4. Heat a little oil in a pan.\n" +
                        "5. Pour the mixture into the pan.\n" +
                        "6. Cook both sides until fully cooked."
        );

        insertIngredient(db, recipe1, "eggs", 2, "pieces");
        insertIngredient(db, recipe1, "tomato", 1, "piece");
        insertIngredient(db, recipe1, "onion", 1, "piece");


        // 2. Creamy Tomato Pasta
        long recipe2 = insertRecipe(
                db,
                "Creamy Tomato Pasta",
                "1. Cook the pasta until tender.\n" +
                        "2. Chop the tomatoes and onion.\n" +
                        "3. Cook the onion and tomato in a pan.\n" +
                        "4. Add the cheese and stir until creamy.\n" +
                        "5. Add the cooked pasta and mix well."
        );

        insertIngredient(db, recipe2, "pasta", 200, "grams");
        insertIngredient(db, recipe2, "tomato", 2, "pieces");
        insertIngredient(db, recipe2, "cheese", 2, "slices");
        insertIngredient(db, recipe2, "onion", 1, "piece");


        // 3. Potato & Tomato Curry
        long recipe3 = insertRecipe(
                db,
                "Potato & Tomato Curry",
                "1. Peel and cut the potatoes into small pieces.\n" +
                        "2. Chop the tomato and onion.\n" +
                        "3. Heat oil in a pan and cook the onion.\n" +
                        "4. Add the tomato and potatoes.\n" +
                        "5. Add seasoning and cook until the potatoes are tender."
        );

        insertIngredient(db, recipe3, "potato", 3, "pieces");
        insertIngredient(db, recipe3, "tomato", 1, "piece");
        insertIngredient(db, recipe3, "onion", 1, "piece");


        // 4. Vegetable Fried Rice
        long recipe4 = insertRecipe(
                db,
                "Vegetable Fried Rice",
                "1. Cook the rice and allow it to cool slightly.\n" +
                        "2. Chop the carrot and onion.\n" +
                        "3. Cook the onion and carrot in a pan.\n" +
                        "4. Add the peas and cooked rice.\n" +
                        "5. Stir-fry until everything is heated through."
        );

        insertIngredient(db, recipe4, "rice", 200, "grams");
        insertIngredient(db, recipe4, "carrot", 1, "piece");
        insertIngredient(db, recipe4, "peas", 100, "grams");
        insertIngredient(db, recipe4, "onion", 1, "piece");


        // 5. Cheese & Tomato Sandwich
        long recipe5 = insertRecipe(
                db,
                "Cheese & Tomato Sandwich",
                "1. Slice the tomato.\n" +
                        "2. Place cheese and tomato between two slices of bread.\n" +
                        "3. Toast the sandwich until the bread is golden and the cheese is warm."
        );

        insertIngredient(db, recipe5, "bread", 2, "slices");
        insertIngredient(db, recipe5, "cheese", 2, "slices");
        insertIngredient(db, recipe5, "tomato", 1, "piece");


        // 6. Banana Smoothie
        long recipe6 = insertRecipe(
                db,
                "Banana Smoothie",
                "1. Peel the banana.\n" +
                        "2. Add the banana and milk to a blender.\n" +
                        "3. Blend until smooth.\n" +
                        "4. Serve chilled."
        );

        insertIngredient(db, recipe6, "banana", 1, "piece");
        insertIngredient(db, recipe6, "milk", 250, "ml");


        // 7. Egg Fried Rice
        long recipe7 = insertRecipe(
                db,
                "Egg Fried Rice",
                "1. Cook the rice.\n" +
                        "2. Beat the eggs.\n" +
                        "3. Cook the onion and peas in a pan.\n" +
                        "4. Add the eggs and scramble them.\n" +
                        "5. Add the rice and stir everything together."
        );

        insertIngredient(db, recipe7, "rice", 200, "grams");
        insertIngredient(db, recipe7, "eggs", 2, "pieces");
        insertIngredient(db, recipe7, "peas", 100, "grams");
        insertIngredient(db, recipe7, "onion", 1, "piece");


        // 8. Creamy Tomato Soup
        long recipe8 = insertRecipe(
                db,
                "Creamy Tomato Soup",
                "1. Chop the tomatoes, carrot and onion.\n" +
                        "2. Cook the onion and vegetables until soft.\n" +
                        "3. Add the milk and simmer gently.\n" +
                        "4. Blend until smooth.\n" +
                        "5. Serve warm."
        );

        insertIngredient(db, recipe8, "tomato", 2, "pieces");
        insertIngredient(db, recipe8, "carrot", 1, "piece");
        insertIngredient(db, recipe8, "onion", 1, "piece");
        insertIngredient(db, recipe8, "milk", 100, "ml");


        // 9. Chickpea & Tomato Curry
        long recipe9 = insertRecipe(
                db,
                "Chickpea & Tomato Curry",
                "1. Chop the tomato and onion.\n" +
                        "2. Heat oil in a pan and cook the onion.\n" +
                        "3. Add the tomato and cook until soft.\n" +
                        "4. Add the chickpeas and seasoning.\n" +
                        "5. Cook until the curry is heated through."
        );

        insertIngredient(db, recipe9, "chickpeas", 200, "grams");
        insertIngredient(db, recipe9, "tomato", 1, "piece");
        insertIngredient(db, recipe9, "onion", 1, "piece");


        // 10. Vegetable Stir-Fry
        long recipe10 = insertRecipe(
                db,
                "Vegetable Stir-Fry",
                "1. Slice the carrot and onion.\n" +
                        "2. Cut the cabbage into thin strips.\n" +
                        "3. Heat oil in a pan.\n" +
                        "4. Add the vegetables and stir-fry until tender.\n" +
                        "5. Add seasoning and serve."
        );

        insertIngredient(db, recipe10, "carrot", 1, "piece");
        insertIngredient(db, recipe10, "cabbage", 100, "grams");
        insertIngredient(db, recipe10, "onion", 1, "piece");


        // 11. Cheese & Vegetable Toast
        long recipe11 = insertRecipe(
                db,
                "Cheese & Vegetable Toast",
                "1. Slice the tomato and onion.\n" +
                        "2. Place the vegetables and cheese on the bread.\n" +
                        "3. Toast until the bread is crisp and the cheese has melted."
        );

        insertIngredient(db, recipe11, "bread", 2, "slices");
        insertIngredient(db, recipe11, "cheese", 2, "slices");
        insertIngredient(db, recipe11, "tomato", 1, "piece");
        insertIngredient(db, recipe11, "onion", 1, "piece");


        // 12. Banana Pancakes
        long recipe12 = insertRecipe(
                db,
                "Banana Pancakes",
                "1. Mash the banana in a bowl.\n" +
                        "2. Add the eggs, flour and milk.\n" +
                        "3. Mix until a smooth batter forms.\n" +
                        "4. Heat a pan and cook small pancakes on both sides.\n" +
                        "5. Serve warm."
        );

        insertIngredient(db, recipe12, "banana", 1, "piece");
        insertIngredient(db, recipe12, "eggs", 2, "pieces");
        insertIngredient(db, recipe12, "flour", 100, "grams");
        insertIngredient(db, recipe12, "milk", 100, "ml");


        // 13. Tomato & Vegetable Rice
        long recipe13 = insertRecipe(
                db,
                "Tomato & Vegetable Rice",
                "1. Cook the rice.\n" +
                        "2. Chop the tomatoes and carrot.\n" +
                        "3. Cook the vegetables until soft.\n" +
                        "4. Add the peas and cooked rice.\n" +
                        "5. Mix well and serve warm."
        );

        insertIngredient(db, recipe13, "rice", 200, "grams");
        insertIngredient(db, recipe13, "tomato", 2, "pieces");
        insertIngredient(db, recipe13, "carrot", 1, "piece");
        insertIngredient(db, recipe13, "peas", 100, "grams");


        // 14. Crispy Potato Wedges
        long recipe14 = insertRecipe(
                db,
                "Crispy Potato Wedges",
                "1. Wash and cut the potatoes into wedges.\n" +
                        "2. Coat the wedges with a little oil and seasoning.\n" +
                        "3. Bake or fry until golden and crispy.\n" +
                        "4. Serve warm."
        );

        insertIngredient(db, recipe14, "potato", 3, "pieces");


        // 15. Creamy Vegetable Pasta
        long recipe15 = insertRecipe(
                db,
                "Creamy Vegetable Pasta",
                "1. Cook the pasta until tender.\n" +
                        "2. Chop the carrot.\n" +
                        "3. Cook the carrot and peas until tender.\n" +
                        "4. Add the cheese and stir until creamy.\n" +
                        "5. Add the cooked pasta and mix well."
        );

        insertIngredient(db, recipe15, "pasta", 200, "grams");
        insertIngredient(db, recipe15, "carrot", 1, "piece");
        insertIngredient(db, recipe15, "peas", 100, "grams");
        insertIngredient(db, recipe15, "cheese", 2, "slices");
    }
}