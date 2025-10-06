package handlers

import (
	"net/http"
	"time"

	"petsafe/internal/db"
	"petsafe/internal/models"

	"github.com/gin-gonic/gin"
)

type LocationInput struct {
	DeviceID  uint64   `json:"device_id" binding:"required"`
	Latitude  float64  `json:"latitude" binding:"required"`
	Longitude float64  `json:"longitude" binding:"required"`
	Accuracy  *float32 `json:"accuracy"`
	Speed     *float32 `json:"speed"`
	Heading   *float32 `json:"heading"`
	UpdatedAt int64    `json:"updated_at" binding:"required"`
}

func PostLocation(c *gin.Context) {
	var in LocationInput
	if err := c.ShouldBindJSON(&in); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	// Save location
	loc := models.Location{
		DeviceID:   in.DeviceID,
		Latitude:   in.Latitude,
		Longitude:  in.Longitude,
		Accuracy:   in.Accuracy,
		Speed:      in.Speed,
		Heading:    in.Heading,
		UpdatedAt:  in.UpdatedAt,
		ReceivedAt: time.Now(),
	}
	if err := db.DB.Create(&loc).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to save location"})
		return
	}
	// update device last coords and last_comm
	now := time.Now()
	db.DB.Model(&models.Device{}).Where("id = ?", in.DeviceID).Updates(map[string]interface{}{
		"last_latitude":  in.Latitude,
		"last_longitude": in.Longitude,
		"last_comm":      now,
	})
	c.JSON(http.StatusCreated, gin.H{"data": loc})
}
