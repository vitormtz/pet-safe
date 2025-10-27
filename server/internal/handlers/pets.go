package handlers

import (
	"net/http"
	"time"

	"petsafe/internal/db"
	"petsafe/internal/models"

	"github.com/gin-gonic/gin"
)

type CreatePetInput struct {
	Name        string     `json:"name" binding:"required"`
	Species     string     `json:"species"`
	Breed       string     `json:"breed"`
	MicrochipID string     `json:"microchip_id"`
	DOB         *time.Time `json:"dob"` // ISO date optional
}

func CreatePet(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	ownerID := uidAny.(uint64)
	var in CreatePetInput
	if err := c.ShouldBindJSON(&in); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	pet := models.Pet{
		OwnerID:     ownerID,
		Name:        in.Name,
		Species:     in.Species,
		Breed:       in.Breed,
		MicrochipID: in.MicrochipID,
		DOB:         in.DOB,
	}
	if err := db.DB.Create(&pet).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to create pet"})
		return
	}
	c.JSON(http.StatusCreated, gin.H{"data": pet})
}

func ListPets(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	ownerID := uidAny.(uint64)
	var pets []models.Pet
	db.DB.Where("owner_id = ?", ownerID).Find(&pets)
	c.JSON(http.StatusOK, gin.H{"data": pets})
}

func DetailsPet(c *gin.Context) {
	pet_id := c.Param("id")
	uidAny, _ := c.Get("user_id")
	ownerID := uidAny.(uint64)
	var p models.Pet
	if err := db.DB.Where("owner_id = ?", ownerID).First(&p, pet_id).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "pet not found"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"data": p})
}

type UpdatePetInput struct {
	Name        string     `json:"name"`
	Species     string     `json:"species"`
	Breed       string     `json:"breed"`
	DOB         *time.Time `json:"dob"`
	MicrochipID string     `json:"microchip_id"`
}

func UpdatePet(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	ownerID := uidAny.(uint64)

	// Get model if exist
	var pet models.Pet
	if err := db.DB.Where("id = ?", c.Param("id")).Where("owner_id = ?", ownerID).First(&pet).Error; err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Pet not found!"})
		return
	}

	// Validate input
	var input UpdatePetInput
	if err := c.ShouldBindJSON(&input); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	db.DB.Model(&pet).Updates(input)

	c.JSON(http.StatusOK, gin.H{"data": pet})
}

func DeletePet(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	ownerID := uidAny.(uint64)
	// Get model if exist
	var pet models.Pet
	if err := db.DB.Where("id = ?", c.Param("id")).Where("owner_id = ?", ownerID).First(&pet).Error; err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Pet not found!"})
		return
	}

	db.DB.Delete(&pet)

	c.JSON(http.StatusOK, gin.H{"data": true})
}
