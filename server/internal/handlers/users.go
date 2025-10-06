package handlers

import (
	"net/http"
	"strconv"

	"petsafe/internal/db"
	"petsafe/internal/models"

	"github.com/gin-gonic/gin"
)

func GetMe(c *gin.Context) {
	uidAny, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "not authenticated"})
		return
	}
	userID := uidAny.(uint64)
	var user models.User
	if err := db.DB.Preload("Pets").Preload("Devices").First(&user, userID).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "user not found"})
		return
	}
	user.PasswordHash = ""
	c.JSON(http.StatusOK, gin.H{"data": user})
}

type UpdateUserInput struct {
	FullName *string `json:"full_name"`
	Phone    *string `json:"phone"`
}

func UpdateUser(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	userID := uidAny.(uint64)
	var in UpdateUserInput
	if err := c.ShouldBindJSON(&in); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	var user models.User
	if err := db.DB.First(&user, userID).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "user not found"})
		return
	}
	if in.FullName != nil {
		user.FullName = *in.FullName
	}
	if in.Phone != nil {
		user.Phone = *in.Phone
	}
	db.DB.Save(&user)
	user.PasswordHash = ""
	c.JSON(http.StatusOK, gin.H{"data": user})
}

// Admin / read by id (example)
func GetUserByID(c *gin.Context) {
	idStr := c.Param("id")
	id, _ := strconv.ParseUint(idStr, 10, 64)
	var user models.User
	if err := db.DB.First(&user, id).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "user not found"})
		return
	}
	user.PasswordHash = ""
	c.JSON(http.StatusOK, gin.H{"data": user})
}
