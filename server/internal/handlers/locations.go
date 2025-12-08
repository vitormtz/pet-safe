package handlers

import (
	"math"
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

// calculateDistance calcula a distância em metros entre duas coordenadas usando a fórmula de Haversine
func calculateDistance(lat1, lon1, lat2, lon2 float64) float64 {
	const earthRadius = 6371000 // raio da Terra em metros

	// Converte graus para radianos
	lat1Rad := lat1 * math.Pi / 180
	lat2Rad := lat2 * math.Pi / 180
	deltaLat := (lat2 - lat1) * math.Pi / 180
	deltaLon := (lon2 - lon1) * math.Pi / 180

	// Fórmula de Haversine
	a := math.Sin(deltaLat/2)*math.Sin(deltaLat/2) +
		math.Cos(lat1Rad)*math.Cos(lat2Rad)*
			math.Sin(deltaLon/2)*math.Sin(deltaLon/2)
	c := 2 * math.Atan2(math.Sqrt(a), math.Sqrt(1-a))

	return earthRadius * c
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

	// Verificar geofence - busca o device para pegar o owner_id
	var device models.Device
	if err := db.DB.First(&device, in.DeviceID).Error; err == nil {
		// Busca o geofence ativo do usuário
		var geofence models.Geofence
		if err := db.DB.Where("owner_id = ? AND active = ?", device.OwnerID, true).First(&geofence).Error; err == nil {
			// Verifica se o geofence tem coordenadas válidas
			if geofence.Latitude != nil && geofence.Longitude != nil && geofence.RadiusM != nil {
				// Calcula a distância entre a localização atual e o centro do geofence
				distance := calculateDistance(in.Latitude, in.Longitude, *geofence.Latitude, *geofence.Longitude)

				// Se está fora do raio, cria um alerta
				if distance > float64(*geofence.RadiusM) {
					alert := models.Alert{
						DeviceID:       in.DeviceID,
						GeofenceID:     &geofence.ID,
						AlertType:      "geofence_exit",
						AlertTimestamp: now,
					}
					db.DB.Create(&alert)
				}
			}
		}
	}

	c.JSON(http.StatusCreated, gin.H{"data": loc})
}
