package com.example.petsafe.models;

import com.google.gson.annotations.SerializedName;

public class PetRequest {
    private String name;
    private String species;
    private String breed;

    @SerializedName("microchip_id")
    private String microchipId;

    private String dob;

    // Constructor
    public PetRequest() {
    }

    public PetRequest(String name, String species, String breed, String microchipId, String dob) {
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.microchipId = microchipId;
        this.dob = dob;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getMicrochipId() {
        return microchipId;
    }

    public void setMicrochipId(String microchipId) {
        this.microchipId = microchipId;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }
}
