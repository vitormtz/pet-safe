package handlers

import (
	"net/http"

	"petsafe/internal/db"
	"petsafe/internal/models"

	"github.com/gin-gonic/gin"
)

type CreatePetInput struct {
	Name        string  `json:"name" binding:"required"`
	Species     string  `json:"species"`
	Breed       string  `json:"breed"`
	MicrochipID string  `json:"microchip_id"`
	DOB         *string `json:"dob"` // ISO date optional
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
