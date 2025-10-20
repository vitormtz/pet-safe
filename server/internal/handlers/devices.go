package handlers

import (
	"net/http"
	"strconv"

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

func ListDevices(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	ownerID := uidAny.(uint64)
	var devices []models.Device
	db.DB.Where("owner_id = ?", ownerID).Find(&devices)
	c.JSON(http.StatusOK, gin.H{"data": devices})
}

type UpdateDeviceInput struct {
	SerialNumber string  `json:"serial_number"`
	IMEI         string  `json:"imei"`
	Model        string  `json:"model"`
	Firmware     string  `json:"firmware"`
	PetID        *uint64 `json:"pet_id"`
	Connectivity string  `json:"connectivity"`
	Active       bool    `json:"active"`
}

func UpdateDevice(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	ownerID := uidAny.(uint64)

	// Get model if exist
	var device models.Device
	if err := db.DB.Where("id = ?", c.Param("id")).Where("owner_id = ?", ownerID).First(&device).Error; err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Device not found!"})
		return
	}

	// Validate input
	var input UpdateDeviceInput
	if err := c.ShouldBindJSON(&input); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	db.DB.Model(&device).Updates(input)

	c.JSON(http.StatusOK, gin.H{"data": device})
}

func DeleteDevice(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	ownerID := uidAny.(uint64)
	// Get model if exist
	var device models.Device
	if err := db.DB.Where("id = ?", c.Param("id")).Where("owner_id = ?", ownerID).First(&device).Error; err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Device not found!"})
		return
	}

	db.DB.Delete(&device)

	c.JSON(http.StatusOK, gin.H{"data": true})
}

func ListDeviceLocations(c *gin.Context) {

	limitParam := c.Param("limit")
	limit, err := strconv.Atoi(limitParam)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid limit"})
		return
	}

	var locations []models.Location
	db.DB.Where("device_id = ?", c.Param("id")).Order("id DESC").Limit(limit).Find(&locations)
	c.JSON(http.StatusOK, gin.H{"data": locations})
}
