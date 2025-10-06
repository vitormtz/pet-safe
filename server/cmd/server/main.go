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

			secured.GET("/users/:id", handlers.GetUserByID) // cuidado: permitir só admin
			// TODO:
			// PATCH /users/{id} → atualizar nome, telefone etc.
			// DELETE /users/{id} → excluir usuário

			// pets
			secured.POST("/pets", handlers.CreatePet)
			secured.GET("/pets", handlers.ListPets)
			// TODO:
			// PATCH /pets/{id} → atualizar informações do pet
			// DELETE /pets/{id} → excluir

			// devices
			secured.POST("/devices", handlers.CreateDevice)
			// PATCH /devices/{id} → atualizar dados (pet vinculado, firmware, ativo/inativo)
			// DELETE /devices/{id} → remover dispositivo
			secured.GET("/devices/:id/status", handlers.DeviceStatus)
			// GET /devices/{id}/battery-history → histórico de bateria
			// GET /devices/{id}/locations → rota do dispositivo (últimos N pontos)

		}
	}

	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}
	r.Run(":" + port)
	fmt.Println("server running on", port)
}
