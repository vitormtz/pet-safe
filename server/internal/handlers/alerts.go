package handlers

import (
	"net/http"
	"time"

	"petsafe/internal/db"
	"petsafe/internal/models"

	"github.com/gin-gonic/gin"
)

// ListAlerts retorna todos os alertas do usuário
func ListAlerts(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	userID := uidAny.(uint64)

	var alerts []models.Alert
	// Busca alertas de dispositivos do usuário, ordenados por mais recentes
	db.DB.Joins("JOIN devices ON devices.id = alerts.device_id").
		Where("devices.owner_id = ?", userID).
		Order("alerts.alert_timestamp DESC").
		Limit(50).
		Find(&alerts)

	c.JSON(http.StatusOK, gin.H{"data": alerts})
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

	// Atualiza todos os alertas não lidos do usuário
	db.DB.Model(&models.Alert{}).
		Joins("JOIN devices ON devices.id = alerts.device_id").
		Where("devices.owner_id = ? AND alerts.acknowledged_at IS NULL", userID).
		Updates(map[string]interface{}{
			"acknowledged_by": userID,
			"acknowledged_at": now,
		})

	c.JSON(http.StatusOK, gin.H{"message": "All alerts marked as read"})
}
