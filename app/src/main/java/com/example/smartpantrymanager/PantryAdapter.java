package com.example.smartpantrymanager;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PantryAdapter
        extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    private final List<PantryItem> pantryItems;
    private final DatabaseHelper databaseHelper;

    public PantryAdapter(
            List<PantryItem> pantryItems,
            DatabaseHelper databaseHelper) {

        this.pantryItems = pantryItems;
        this.databaseHelper = databaseHelper;
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_pantry,
                        parent,
                        false
                );

        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PantryViewHolder holder,
            int position) {

        PantryItem item = pantryItems.get(position);

        // Ingredient name
        holder.nameTextView.setText(
                formatIngredientName(item.getName())
        );

        // Natural quantity display
        holder.quantityTextView.setText(
                formatQuantity(
                        item.getName(),
                        item.getQuantity(),
                        item.getUnit()
                )
        );

        // Expiry date
        String expiryDate = item.getExpiryDate();

        if (expiryDate == null || expiryDate.trim().isEmpty()) {

            holder.expiryTextView.setText(
                    "No expiry date"
            );

        } else {

            holder.expiryTextView.setText(
                    "Expires " + expiryDate
            );
        }

        // =====================================================
        // EDIT
        // =====================================================

        holder.editButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    v.getContext(),
                    AddItemActivity.class
            );

            // This is the important change
            intent.putExtra(
                    "ITEM_ID",
                    item.getId()
            );

            v.getContext().startActivity(intent);
        });

        // =====================================================
        // DELETE
        // =====================================================

        holder.deleteButton.setOnClickListener(v -> {

            int currentPosition =
                    holder.getAdapterPosition();

            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }

            PantryItem itemToDelete =
                    pantryItems.get(currentPosition);

            databaseHelper.deletePantryItem(
                    itemToDelete.getId()
            );

            pantryItems.remove(currentPosition);

            notifyItemRemoved(currentPosition);
        });
    }


    // =========================================================
    // FORMAT INGREDIENT NAME
    // =========================================================

    private String formatIngredientName(String name) {

        if (name == null || name.trim().isEmpty()) {
            return "";
        }

        String cleanedName =
                name.trim().toLowerCase();

        switch (cleanedName) {

            case "egg":
            case "eggs":
                return "Eggs";

            case "tomato":
            case "tomatoes":
                return "Tomatoes";

            case "onion":
            case "onions":
                return "Onions";

            case "potato":
            case "potatoes":
                return "Potatoes";

            case "banana":
            case "bananas":
                return "Bananas";

            case "carrot":
            case "carrots":
                return "Carrots";

            case "peas":
            case "pea":
                return "Peas";

            case "cheese":
                return "Cheese";

            case "bread":
                return "Bread";

            case "rice":
                return "Rice";

            case "pasta":
                return "Pasta";

            case "milk":
                return "Milk";

            case "oil":
                return "Oil";

            default:
                return capitalizeFirstLetter(cleanedName);
        }
    }


    // =========================================================
    // FORMAT QUANTITY NATURALLY
    // =========================================================

    private String formatQuantity(
            String ingredientName,
            double quantity,
            String unit) {

        String quantityText;

        if (quantity == Math.floor(quantity)) {

            quantityText =
                    String.valueOf((int) quantity);

        } else {

            quantityText =
                    String.valueOf(quantity);
        }

        String ingredient =
                ingredientName == null
                        ? ""
                        : ingredientName.trim().toLowerCase();

        String databaseUnit =
                unit == null
                        ? ""
                        : unit.trim().toLowerCase();


        // Eggs
        if (ingredient.equals("egg")
                || ingredient.equals("eggs")) {

            return quantityText
                    + (quantity == 1 ? " egg" : " eggs");
        }


        // Tomatoes
        if (ingredient.equals("tomato")
                || ingredient.equals("tomatoes")) {

            return quantityText
                    + (quantity == 1 ? " tomato" : " tomatoes");
        }


        // Onions
        if (ingredient.equals("onion")
                || ingredient.equals("onions")) {

            return quantityText
                    + (quantity == 1 ? " onion" : " onions");
        }


        // Potatoes
        if (ingredient.equals("potato")
                || ingredient.equals("potatoes")) {

            return quantityText
                    + (quantity == 1 ? " potato" : " potatoes");
        }


        // Bananas
        if (ingredient.equals("banana")
                || ingredient.equals("bananas")) {

            return quantityText
                    + (quantity == 1 ? " banana" : " bananas");
        }


        // Carrots
        if (ingredient.equals("carrot")
                || ingredient.equals("carrots")) {

            return quantityText
                    + (quantity == 1 ? " carrot" : " carrots");
        }


        // Peas
        if (ingredient.equals("pea")
                || ingredient.equals("peas")) {

            if (databaseUnit.equals("grams")) {
                return quantityText + " g";
            }

            return quantityText
                    + (quantity == 1 ? " pea" : " peas");
        }


        // Slices
        if (databaseUnit.equals("slice")
                || databaseUnit.equals("slices")) {

            return quantityText
                    + (quantity == 1 ? " slice" : " slices");
        }


        // Grams
        if (databaseUnit.equals("gram")
                || databaseUnit.equals("grams")
                || databaseUnit.equals("g")) {

            return quantityText + " g";
        }


        // Kilograms
        if (databaseUnit.equals("kilogram")
                || databaseUnit.equals("kilograms")
                || databaseUnit.equals("kg")) {

            return quantityText + " kg";
        }


        // Millilitres
        if (databaseUnit.equals("ml")
                || databaseUnit.equals("millilitre")
                || databaseUnit.equals("millilitres")) {

            return quantityText + " ml";
        }


        // Litres
        if (databaseUnit.equals("l")
                || databaseUnit.equals("litre")
                || databaseUnit.equals("litres")) {

            return quantityText + " L";
        }


        // Packs
        if (databaseUnit.equals("pack")
                || databaseUnit.equals("packs")) {

            return quantityText
                    + (quantity == 1 ? " pack" : " packs");
        }


        // Cups
        if (databaseUnit.equals("cup")
                || databaseUnit.equals("cups")) {

            return quantityText
                    + (quantity == 1 ? " cup" : " cups");
        }


        // Tablespoons
        if (databaseUnit.equals("tablespoon")
                || databaseUnit.equals("tablespoons")
                || databaseUnit.equals("tbsp")) {

            return quantityText + " tbsp";
        }


        // Teaspoons
        if (databaseUnit.equals("teaspoon")
                || databaseUnit.equals("teaspoons")
                || databaseUnit.equals("tsp")) {

            return quantityText + " tsp";
        }


        // Generic fallback
        return quantityText + " " + databaseUnit;
    }


    // =========================================================
    // CAPITALIZE
    // =========================================================

    private String capitalizeFirstLetter(String text) {

        if (text == null || text.isEmpty()) {
            return "";
        }

        return text.substring(0, 1).toUpperCase()
                + text.substring(1);
    }


    @Override
    public int getItemCount() {
        return pantryItems.size();
    }


    // =========================================================
    // VIEW HOLDER
    // =========================================================

    public static class PantryViewHolder
            extends RecyclerView.ViewHolder {

        TextView nameTextView;
        TextView quantityTextView;
        TextView expiryTextView;

        View editButton;
        View deleteButton;

        public PantryViewHolder(
                @NonNull View itemView) {

            super(itemView);

            nameTextView =
                    itemView.findViewById(
                            R.id.textPantryName
                    );

            quantityTextView =
                    itemView.findViewById(
                            R.id.textPantryQuantity
                    );

            expiryTextView =
                    itemView.findViewById(
                            R.id.textPantryExpiry
                    );

            editButton =
                    itemView.findViewById(
                            R.id.editPantryButton
                    );

            deleteButton =
                    itemView.findViewById(
                            R.id.deletePantryButton
                    );
        }
    }
}