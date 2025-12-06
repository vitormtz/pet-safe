package main

import (
	"fmt"
	"os"

	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"

	"petsafe/internal/db"
	"petsafe/internal/handlers"
	"petsafe/internal/middleware"
)

func main() {
	_ = godotenv.Load()
	db.Init()

	r := gin.Default()

	api := r.Group("/api/v1")
	{

		// locations (device posts; pode ser pública se dispositivos não autenticarem com JWT)
		api.POST("/locations", handlers.PostLocation)

		auth := api.Group("/auth")
		{
			auth.POST("/register", handlers.Register)
			auth.POST("/login", handlers.Login)
			auth.POST("/refresh", handlers.Refresh)
			// auth.POST("/logout", handlers.Logout) // implementar se necessário
		}

		// rotas que requerem autenticação
		secured := api.Group("/")
		secured.Use(middleware.JWTAuthMiddleware())
		{
			secured.GET("/me", handlers.GetMe)
			secured.PATCH("/me", handlers.UpdateUser)
			secured.PATCH("/me/password", handlers.UpdatePassword)

			// secured.GET("/users/:id", handlers.GetUserByID) // cuidado: permitir só admin
			// TODO:
			// PATCH /users/{id} → atualizar nome, telefone etc.
			// DELETE /users/{id} → excluir usuário

			// pets
			secured.POST("/pets", handlers.CreatePet)
			secured.GET("/pets", handlers.ListPets)
			secured.GET("/pets/:id", handlers.DetailsPet)
			secured.PATCH("/pets/:id", handlers.UpdatePet)
			secured.DELETE("/pets/:id", handlers.DeletePet)

			// devices
			secured.POST("/devices", handlers.CreateDevice)
			secured.GET("/devices", handlers.ListDevices)
			secured.PATCH("/devices/:id", handlers.UpdateDevice)
			secured.DELETE("/devices/:id", handlers.DeleteDevice)

			// device data
			secured.GET("/devices/:id/status", handlers.DeviceStatus)
			secured.GET("/devices/:id/locations/:limit", handlers.ListDeviceLocations)
			// GET /devices/{id}/battery-history → histórico de bateria

			// geofences (apenas 1 por usuário)
			secured.POST("/geofence", handlers.CreateGeofence)
			secured.GET("/geofence", handlers.GetGeofence)
			secured.PATCH("/geofence", handlers.UpdateGeofence)
			secured.DELETE("/geofence", handlers.DeleteGeofence)

		}
	}

	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}
	r.Run(":" + port)
	fmt.Println("server running on", port)
}
