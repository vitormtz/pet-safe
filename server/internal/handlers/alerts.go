package handlers

import (
	"net/http"
	"time"

	"petsafe/internal/db"
	"petsafe/internal/models"

	"github.com/gin-gonic/gin"
)

// AlertWithPet estrutura para retornar alertas com informações do pet
type AlertWithPet struct {
	ID             uint64     `json:"id"`
	DeviceID       uint64     `json:"device_id"`
	GeofenceID     *uint64    `json:"geofence_id"`
	EventID        *uint64    `json:"event_id"`
	AlertType      string     `json:"alert_type"`
	AlertTimestamp time.Time  `json:"alert_timestamp"`
	AcknowledgedBy *uint64    `json:"acknowledged_by"`
	AcknowledgedAt *time.Time `json:"acknowledged_at"`
	PetName        *string    `json:"pet_name"`
}

// ListAlerts retorna todos os alertas do usuário
func ListAlerts(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	userID := uidAny.(uint64)

	var alertsWithPet []AlertWithPet
	// Busca alertas de dispositivos do usuário com LEFT JOIN em pets
	// Para incluir o nome do pet vinculado ao dispositivo (se houver)
	err := db.DB.Table("alerts").
		Select("alerts.id, alerts.device_id, alerts.geofence_id, alerts.event_id, alerts.alert_type, alerts.alert_timestamp, alerts.acknowledged_by, alerts.acknowledged_at, pets.name as pet_name").
		Joins("JOIN devices ON devices.id = alerts.device_id").
		Joins("LEFT JOIN pets ON pets.owner_id = devices.owner_id AND pets.microchip_id = CAST(devices.id AS VARCHAR)").
		Where("devices.owner_id = ?", userID).
		Order("alerts.alert_timestamp DESC").
		Limit(50).
		Scan(&alertsWithPet).Error

	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"data": alertsWithPet})
}

// GetUnreadAlertsCount retorna a quantidade de alertas não lidos
func GetUnreadAlertsCount(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	userID := uidAny.(uint64)

	var count int64
	db.DB.Model(&models.Alert{}).
		Joins("JOIN devices ON devices.id = alerts.device_id").
		Where("devices.owner_id = ? AND alerts.acknowledged_at IS NULL", userID).
		Count(&count)

	c.JSON(http.StatusOK, gin.H{"count": count})
}

// MarkAlertAsRead marca um alerta como lido
func MarkAlertAsRead(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	userID := uidAny.(uint64)

	alertID := c.Param("id")

	// Verifica se o alerta pertence ao usuário
	var alert models.Alert
	if err := db.DB.Joins("JOIN devices ON devices.id = alerts.device_id").
		Where("alerts.id = ? AND devices.owner_id = ?", alertID, userID).
		First(&alert).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "Alert not found"})
		return
	}

	// Marca como lido
	now := time.Now()
	db.DB.Model(&alert).Updates(map[string]interface{}{
		"acknowledged_by": userID,
		"acknowledged_at": now,
	})

	c.JSON(http.StatusOK, gin.H{"data": alert})
}

// MarkAllAlertsAsRead marca todos os alertas do usuário como lidos
func MarkAllAlertsAsRead(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	userID := uidAny.(uint64)

	now := time.Now()

	// Primeiro, busca os IDs dos alertas não lidos do usuário
	var alertIDs []uint64
	db.DB.Model(&models.Alert{}).
		Select("alerts.id").
		Joins("JOIN devices ON devices.id = alerts.device_id").
		Where("devices.owner_id = ? AND alerts.acknowledged_at IS NULL", userID).
		Pluck("alerts.id", &alertIDs)

	// Se não houver alertas, retorna sucesso
	if len(alertIDs) == 0 {
		c.JSON(http.StatusOK, gin.H{"message": "No unread alerts"})
		return
	}

	// Atualiza os alertas usando os IDs encontrados
	db.DB.Model(&models.Alert{}).
		Where("id IN ?", alertIDs).
		Updates(map[string]interface{}{
			"acknowledged_by": userID,
			"acknowledged_at": now,
		})

	c.JSON(http.StatusOK, gin.H{"message": "All alerts marked as read"})
}
