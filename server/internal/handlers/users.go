package handlers

import (
	"net/http"

	"petsafe/internal/db"
	"petsafe/internal/models"

	"github.com/gin-gonic/gin"
	"golang.org/x/crypto/bcrypt"
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

type UpdatePasswordInput struct {
	CurrentPassword string `json:"current_password" binding:"required"`
	NewPassword     string `json:"new_password" binding:"required,min=8"`
}

func UpdatePassword(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	userID := uidAny.(uint64)

	var in UpdatePasswordInput
	if err := c.ShouldBindJSON(&in); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// Buscar usuário
	var user models.User
	if err := db.DB.First(&user, userID).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "user not found"})
		return
	}

	// Verificar se a senha atual está correta
	if err := bcrypt.CompareHashAndPassword([]byte(user.PasswordHash), []byte(in.CurrentPassword)); err != nil {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "current password is incorrect"})
		return
	}

	// Verificar se a nova senha é diferente da atual
	if err := bcrypt.CompareHashAndPassword([]byte(user.PasswordHash), []byte(in.NewPassword)); err == nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "new password must be different from current password"})
		return
	}

	// Hash da nova senha
	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(in.NewPassword), bcrypt.DefaultCost)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to hash password"})
		return
	}

	// Atualizar senha
	user.PasswordHash = string(hashedPassword)
	if err := db.DB.Save(&user).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to update password"})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "password updated successfully"})
}

// Admin / read by id (example)
// func GetUserByID(c *gin.Context) {
// 	idStr := c.Param("id")
// 	id, _ := strconv.ParseUint(idStr, 10, 64)
// 	var user models.User
// 	if err := db.DB.First(&user, id).Error; err != nil {
// 		c.JSON(http.StatusNotFound, gin.H{"error": "user not found"})
// 		return
// 	}
// 	user.PasswordHash = ""
// 	c.JSON(http.StatusOK, gin.H{"data": user})
// }
