package com.example.smartpantrymanager;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SimpleRecipeAdapter
        extends RecyclerView.Adapter<SimpleRecipeAdapter.RecipeViewHolder> {

    private final List<String> recipeNames;
    private final Context context;

    public SimpleRecipeAdapter(
            Context context,
            List<String> recipeNames) {

        this.context = context;
        this.recipeNames = recipeNames;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_recipe,
                        parent,
                        false
                );

        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecipeViewHolder holder,
            int position) {

        String recipeName = recipeNames.get(position);

        holder.recipeNameText.setText(recipeName);

        holder.recipeSubtitleText.setText(
                "Matches your pantry"
        );

        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(
                    context,
                    RecipeDetailActivity.class
            );

            intent.putExtra(
                    "RECIPE_NAME",
                    recipeName
            );

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recipeNames.size();
    }

    public static class RecipeViewHolder
            extends RecyclerView.ViewHolder {

        TextView recipeNameText;
        TextView recipeSubtitleText;

        public RecipeViewHolder(
                @NonNull View itemView) {

            super(itemView);

            recipeNameText =
                    itemView.findViewById(
                            R.id.textRecipeName
                    );

            recipeSubtitleText =
                    itemView.findViewById(
                            R.id.textRecipeSubtitle
                    );
        }
    }
}

