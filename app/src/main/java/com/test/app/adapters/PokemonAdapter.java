package com.test.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.test.app.R;
import com.test.app.activities.MainActivity;
import com.test.app.callbacks.OnItemClickListener;
import com.test.app.db.dao.PokemonDao;
import com.test.app.db.entities.PokemonEntity;
import com.test.app.globals.Globals;
import com.test.app.models.Pokemon;
import com.test.app.web.PKResponseResult;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class PokemonAdapter extends RecyclerView.Adapter<PokemonViewHolder> {

    public PokemonAdapter()
    {
        this.pokemonArray = new Pokemon[Globals.getPageLimit()];
    }

    @NonNull
    @Override
    public PokemonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_view_pokemon, parent, false);
        return new PokemonViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PokemonViewHolder holder, int position) {
        Pokemon pokemon = this.pokemonArray[position];

        holder.itemView.setBackgroundColor(selectedPosition == position ? Color.GRAY : Color.WHITE);

        holder.itemView.setOnClickListener(v -> {
            if(onClickListener != null)
                onClickListener.onItemClick(holder.getAdapterPosition(), pokemonArray[holder.getAdapterPosition()]);

            if(selectedPosition > -1)
                notifyItemChanged(selectedPosition);

            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(selectedPosition);

        });

        if(pokemon != null)
        {
            setViewHolder(holder, pokemon);
            return;
        }

        loadPokemonAt(position, holder);
    }

    @Override
    public int getItemCount() {
        return pokemonArray.length;
    }

    public void setOnClickListener(OnItemClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    private void setViewHolder(@NonNull PokemonViewHolder holder, Pokemon pokemon) {
        holder.setPokemonName(pokemon.getName());
        holder.setPokemonFrontSprite(pokemon.getSprites().getFrontDefault());
        Button favouriteBtn = holder.FavouriteButton;
        PokemonDao pokemonDao = Globals.AppDatabase.pokemonDao();
        List<PokemonEntity> existingPokemon = pokemonDao.findPokemonWithId(pokemon.getId());
        boolean isFavourite = existingPokemon.size() > 0;
        favouriteBtn.setText(isFavourite ? "Unfavourite" : "Favourite");

        favouriteBtn.setOnClickListener(v -> {
            if(isFavourite)
                unfavouritePokemon(pokemonArray[holder.getAdapterPosition()], pokemonDao);
            else
                favouritePokemon(pokemonArray[holder.getAdapterPosition()], pokemonDao);

            notifyItemChanged(holder.getAdapterPosition());
        });
    }

    private void loadPokemonAt(int position, PokemonViewHolder holder) {
        PKResponseResult pkResponseResult = MainActivity.getPokemonManager().getPkResult(position);
        if(pkResponseResult == null)
            return;

        String[] pathParts = pkResponseResult.getUrl().split("/");
        MainActivity.getPokemonManager().getPokemon(Integer.parseInt(pathParts[pathParts.length - 1]), pokemon -> {
            if(holder.getAdapterPosition() < 0)
                return;
            pokemonArray[holder.getAdapterPosition()] = pokemon;
            setViewHolder(holder, pokemon);
        });
    }

    private void favouritePokemon(Pokemon pokemon, PokemonDao pokemonDao) {
        PokemonEntity pokemonEntity = new PokemonEntity(
                                            pokemon.getId(),
                                            pokemon.getName(),
                                            pokemon.getSprites().getFrontDefault());

        pokemonDao.insertPokemon(pokemonEntity);
    }

    private void unfavouritePokemon(Pokemon pokemon, PokemonDao pokemonDao) {
        pokemonDao.deletePokemon(pokemon.getId());
    }

    private final Pokemon[] pokemonArray;
    private OnItemClickListener onClickListener;
    private int selectedPosition = -1;
}
