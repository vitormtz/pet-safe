package handlers

import (
	"net/http"

	"petsafe/internal/db"
	"petsafe/internal/models"

	"github.com/gin-gonic/gin"
)

type CreateDeviceInput struct {
	SerialNumber string `json:"serial_number" binding:"required"`
	IMEI         string `json:"imei"`
	Model        string `json:"model"`
	Firmware     string `json:"firmware"`
}

func CreateDevice(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	ownerID := uidAny.(uint64)
	var in CreateDeviceInput
	if err := c.ShouldBindJSON(&in); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	d := models.Device{
		SerialNumber: in.SerialNumber,
		IMEI:         in.IMEI,
		Model:        in.Model,
		Firmware:     in.Firmware,
		OwnerID:      ownerID,
		Active:       true,
	}
	if err := db.DB.Create(&d).Error; err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "failed to create device (duplicate serial?)"})
		return
	}
	c.JSON(http.StatusCreated, gin.H{"data": d})
}

func DeviceStatus(c *gin.Context) {
	id := c.Param("id")
	var d models.Device
	if err := db.DB.First(&d, id).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "device not found"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"data": d})
}
