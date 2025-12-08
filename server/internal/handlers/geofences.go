package handlers

import (
	"net/http"
	"strconv"

	"petsafe/internal/db"
	"petsafe/internal/models"

	"github.com/gin-gonic/gin"
)

type CreateGeofenceInput struct {
	Name      string  `json:"name" binding:"required"`
	Latitude  float64 `json:"latitude" binding:"required"`
	Longitude float64 `json:"longitude" binding:"required"`
	RadiusM   int     `json:"radius_m" binding:"required"`
}

// CreateGeofence - Cria um geofence (apenas 1 por usuário)
func CreateGeofence(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	ownerID := uidAny.(uint64)

	// Verifica se o usuário já tem um geofence
	var count int64
	db.DB.Model(&models.Geofence{}).Where("owner_id = ?", ownerID).Count(&count)
	if count > 0 {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Você já possui um geofence cadastrado. Delete o existente para criar um novo."})
		return
	}

	var in CreateGeofenceInput
	if err := c.ShouldBindJSON(&in); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	gf := models.Geofence{
		OwnerID:   ownerID,
		Name:      in.Name,
		Latitude:  &in.Latitude,
		Longitude: &in.Longitude,
		RadiusM:   &in.RadiusM,
		Active:    true,
	}

	if err := db.DB.Create(&gf).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to create geofence"})
		return
	}

	// Auto-associa a TODOS os devices do usuário
	var devices []models.Device
	db.DB.Where("owner_id = ? AND active = ?", ownerID, true).Find(&devices)
	for _, device := range devices {
		gfd := models.GeofenceDevice{
			GeofenceID: gf.ID,
			DeviceID:   device.ID,
		}
		db.DB.Create(&gfd)
	}

	c.JSON(http.StatusCreated, gin.H{"data": gf})
}

// GetGeofence - Retorna o geofence do usuário
func GetGeofence(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	ownerID := uidAny.(uint64)

	var gf models.Geofence
	if err := db.DB.Where("owner_id = ?", ownerID).First(&gf).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "geofence not found"})
		return
	}

	c.JSON(http.StatusOK, gin.H{"data": gf})
}

type UpdateGeofenceInput struct {
	Name      string  `json:"name"`
	Latitude  float64 `json:"latitude"`
	Longitude float64 `json:"longitude"`
	RadiusM   int     `json:"radius_m"`
	Active    *bool   `json:"active"`
}

// UpdateGeofence - Atualiza o geofence do usuário
func UpdateGeofence(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	ownerID := uidAny.(uint64)

	var gf models.Geofence
	if err := db.DB.Where("owner_id = ?", ownerID).First(&gf).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "geofence not found"})
		return
	}

	var in UpdateGeofenceInput
	if err := c.ShouldBindJSON(&in); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// Atualiza campos se fornecidos
	if in.Name != "" {
		gf.Name = in.Name
	}
	if in.Active != nil {
		gf.Active = *in.Active
	}
	if in.Latitude != 0 {
		gf.Latitude = &in.Latitude
	}
	if in.Longitude != 0 {
		gf.Longitude = &in.Longitude
	}
	if in.RadiusM != 0 {
		gf.RadiusM = &in.RadiusM
	}

	db.DB.Save(&gf)

	c.JSON(http.StatusOK, gin.H{"data": gf})
}

// DeleteGeofence - Remove o geofence do usuário
func DeleteGeofence(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	ownerID := uidAny.(uint64)

	var gf models.Geofence
	if err := db.DB.Where("owner_id = ?", ownerID).First(&gf).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "geofence not found"})
		return
	}

	// Deleta associações primeiro (por causa da FK)
	db.DB.Where("geofence_id = ?", gf.ID).Delete(&models.GeofenceDevice{})

	// Deleta o geofence
	db.DB.Delete(&gf)

	c.JSON(http.StatusOK, gin.H{"data": true})
}

// Funções auxiliares para formatar valores
func formatFloat(f float64) string {
	return strconv.FormatFloat(f, 'f', 6, 64)
}

func formatInt(i int) string {
	return strconv.Itoa(i)
}
