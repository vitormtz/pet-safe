package com.example.petsafe.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petsafe.R;
import com.example.petsafe.models.Pet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PetsAdapter extends RecyclerView.Adapter<PetsAdapter.PetViewHolder> {

    private List<Pet> petList;
    private OnPetClickListener listener;
    private Map<String, String> deviceDisplayMap; // Maps device ID to display name

    public interface OnPetClickListener {
        void onEditClick(Pet pet);
        void onDeleteClick(Pet pet);
        void onPetClick(Pet pet);
    }

    public PetsAdapter(OnPetClickListener listener) {
        this.petList = new ArrayList<>();
        this.listener = listener;
        this.deviceDisplayMap = new HashMap<>();
    }

    public void setDeviceDisplayMap(Map<String, String> deviceDisplayMap) {
        this.deviceDisplayMap = deviceDisplayMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pet_card, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = petList.get(position);
        holder.bind(pet, listener, deviceDisplayMap);
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    public void setPets(List<Pet> pets) {
        this.petList = pets;
        notifyDataSetChanged();
    }

    public void addPet(Pet pet) {
        this.petList.add(pet);
        notifyItemInserted(petList.size() - 1);
    }

    public void updatePet(Pet updatedPet) {
        for (int i = 0; i < petList.size(); i++) {
            if (petList.get(i).getId().equals(updatedPet.getId())) {
                petList.set(i, updatedPet);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removePet(Pet pet) {
        int position = petList.indexOf(pet);
        if (position != -1) {
            petList.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class PetViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPetName;
        private final TextView tvSpecies;
        private final TextView tvBreed;
        private final TextView tvDeviceId;
        private final TextView tvDob;
        private final ImageButton btnMenu;
        private final ImageView ivPetIcon;
        private final LinearLayout llBreed;
        private final LinearLayout llDevice;
        private final LinearLayout llDob;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPetName = itemView.findViewById(R.id.tvPetName);
            tvSpecies = itemView.findViewById(R.id.tvSpecies);
            tvBreed = itemView.findViewById(R.id.tvBreed);
            tvDeviceId = itemView.findViewById(R.id.tvDeviceId);
            tvDob = itemView.findViewById(R.id.tvDob);
            btnMenu = itemView.findViewById(R.id.btnMenu);
            ivPetIcon = itemView.findViewById(R.id.ivPetIcon);
            llBreed = itemView.findViewById(R.id.llBreed);
            llDevice = itemView.findViewById(R.id.llDevice);
            llDob = itemView.findViewById(R.id.llDob);
        }

        public void bind(Pet pet, OnPetClickListener listener, Map<String, String> deviceDisplayMap) {
            tvPetName.setText(pet.getName());
            tvSpecies.setText(pet.getSpecies());

            // Breed (optional field)
            if (pet.getBreed() != null && !pet.getBreed().isEmpty()) {
                tvBreed.setText(pet.getBreed());
                llBreed.setVisibility(View.VISIBLE);
            } else {
                llBreed.setVisibility(View.GONE);
            }

            // Device ID (optional field)
            if (pet.getMicrochipId() != null && !pet.getMicrochipId().isEmpty()) {
                // Try to get display name from map, otherwise use ID
                String displayText = deviceDisplayMap.get(pet.getMicrochipId());
                if (displayText == null || displayText.isEmpty()) {
                    displayText = pet.getMicrochipId();
                }
                tvDeviceId.setText(displayText);
                llDevice.setVisibility(View.VISIBLE);
            } else {
                llDevice.setVisibility(View.GONE);
            }

            // Date of Birth (optional field)
            if (pet.getDob() != null && !pet.getDob().isEmpty()) {
                String formattedDate = formatDate(pet.getDob());
                tvDob.setText(formattedDate);
                llDob.setVisibility(View.VISIBLE);
            } else {
                llDob.setVisibility(View.GONE);
            }

            // Menu button
            btnMenu.setOnClickListener(v -> showPopupMenu(v, pet, listener));

            // Card click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPetClick(pet);
                }
            });
        }

        private void showPopupMenu(View view, Pet pet, OnPetClickListener listener) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.pet_item_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit) {
                    if (listener != null) {
                        listener.onEditClick(pet);
                    }
                    return true;
                } else if (itemId == R.id.action_delete) {
                    if (listener != null) {
                        listener.onDeleteClick(pet);
                    }
                    return true;
                }
                return false;
            });

            popupMenu.show();
        }

        private String formatDate(String dateStr) {
            try {
                // Input format: ISO 8601 (e.g., "2020-01-01T00:00:00Z")
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                Date date = inputFormat.parse(dateStr);
                if (date != null) {
                    return outputFormat.format(date);
                }
            } catch (ParseException e) {
                // If parsing fails, try simpler format
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                    Date date = inputFormat.parse(dateStr);
                    if (date != null) {
                        return outputFormat.format(date);
                    }
                } catch (ParseException ex) {
                    // Return original string if all parsing attempts fail
                    return dateStr;
                }
            }
            return dateStr;
        }
    }
}
