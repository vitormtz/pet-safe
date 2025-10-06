package handlers

import (
	"crypto/sha256"
	"encoding/hex"
	"net/http"
	"os"
	"strconv"
	"time"

	"petsafe/internal/db"
	"petsafe/internal/models"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"golang.org/x/crypto/bcrypt"
)

// helper: hash password
func hashPassword(p string) (string, error) {
	b, err := bcrypt.GenerateFromPassword([]byte(p), bcrypt.DefaultCost)
	return string(b), err
}

func checkPasswordHash(pw, hash string) bool {
	return bcrypt.CompareHashAndPassword([]byte(hash), []byte(pw)) == nil
}

func generateAccessToken(userID uint64) (string, error) {
	secret := []byte(os.Getenv("JWT_SECRET"))
	expStr := os.Getenv("ACCESS_TOKEN_EXP") // e.g. "15m"
	expDur, err := time.ParseDuration(expStr)
	if err != nil {
		expDur = 15 * time.Minute
	}
	claims := jwt.MapClaims{
		"user_id": userID,
		"exp":     time.Now().Add(expDur).Unix(),
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString(secret)
}

func generateAndStoreRefreshToken(userID uint64) (string, error) {
	// generate random token value (here simple timestamp+user for demo — replace por crypto rand)
	raw := strconv.FormatInt(time.Now().UnixNano(), 10) + ":" + strconv.FormatUint(userID, 10)
	// store a hash in DB
	sum := sha256.Sum256([]byte(raw))
	hash := hex.EncodeToString(sum[:])
	expStr := os.Getenv("REFRESH_TOKEN_EXP")
	expDur, err := time.ParseDuration(expStr)
	if err != nil {
		expDur = 168 * time.Hour // 7 dias
	}
	rt := models.RefreshToken{
		UserID:    userID,
		TokenHash: hash,
		Revoked:   false,
		ExpiresAt: time.Now().Add(expDur),
	}
	if err := db.DB.Create(&rt).Error; err != nil {
		return "", err
	}
	return raw, nil // raw é devolvido ao cliente
}

type RegisterInput struct {
	Email    string `json:"email" binding:"required,email"`
	Password string `json:"password" binding:"required,min=8"`
	FullName string `json:"full_name"`
	Phone    string `json:"phone"`
}

func Register(c *gin.Context) {
	var in RegisterInput
	if err := c.ShouldBindJSON(&in); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	pwdHash, err := hashPassword(in.Password)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to hash password"})
		return
	}
	u := models.User{
		Email:        in.Email,
		PasswordHash: pwdHash,
		FullName:     in.FullName,
		Phone:        in.Phone,
	}
	if err := db.DB.Create(&u).Error; err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "email já registrado ou dados inválidos"})
		return
	}
	c.JSON(http.StatusCreated, gin.H{"success": true, "data": gin.H{"id": u.ID, "email": u.Email}})
}

type LoginInput struct {
	Email    string `json:"email" binding:"required,email"`
	Password string `json:"password" binding:"required"`
}

func Login(c *gin.Context) {
	var in LoginInput
	if err := c.ShouldBindJSON(&in); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	var user models.User
	if err := db.DB.Where("email = ?", in.Email).First(&user).Error; err != nil {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "credenciais inválidas"})
		return
	}
	if !checkPasswordHash(in.Password, user.PasswordHash) {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "credenciais inválidas"})
		return
	}
	accessToken, err := generateAccessToken(user.ID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to create access token"})
		return
	}
	refreshRaw, err := generateAndStoreRefreshToken(user.ID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to create refresh token"})
		return
	}
	c.JSON(http.StatusOK, gin.H{
		"access_token":  accessToken,
		"refresh_token": refreshRaw,
		"user":          gin.H{"id": user.ID, "email": user.Email, "full_name": user.FullName},
	})
}

type RefreshInput struct {
	RefreshToken string `json:"refresh_token" binding:"required"`
}

func Refresh(c *gin.Context) {
	var in RefreshInput
	if err := c.ShouldBindJSON(&in); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	// Hash received raw token and check DB
	sum := sha256.Sum256([]byte(in.RefreshToken))
	hash := hex.EncodeToString(sum[:])
	var rt models.RefreshToken
	if err := db.DB.Where("token_hash = ? AND revoked = false AND expires_at > ?", hash, time.Now()).First(&rt).Error; err != nil {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "refresh token inválido"})
		return
	}
	// generate new access + refresh (optionally revoke old)
	accessToken, err := generateAccessToken(rt.UserID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to create access token"})
		return
	}
	// revoke old token
	db.DB.Model(&rt).Update("revoked", true)
	newRaw, err := generateAndStoreRefreshToken(rt.UserID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to create refresh token"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"access_token": accessToken, "refresh_token": newRaw})
}
