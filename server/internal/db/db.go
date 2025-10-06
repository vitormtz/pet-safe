package db

import (
	"log"
	"os"
	"time"

	"gorm.io/driver/postgres"
	"gorm.io/gorm"

	"github.com/joho/godotenv"
)

var DB *gorm.DB

func Init() {
	_ = godotenv.Load()
	dsn := os.Getenv("DATABASE_DSN")
	if dsn == "" {
		log.Fatal("DATABASE_DSN not set")
	}
	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{})
	if err != nil {
		log.Fatal("failed to connect db:", err)
	}
	// AutoMigrate — você pode comentar em produção e usar migrations reais
	// if err := db.AutoMigrate(
	// 	&models.User{},
	// 	&models.Pet{},
	// 	&models.Device{},
	// 	&models.BatteryHistory{},
	// 	&models.DeviceEvent{},
	// 	&models.Geofence{},
	// 	&models.Alert{},
	// 	&models.Notification{},
	// 	&models.Location{},
	// 	&models.RefreshToken{},
	// ); err != nil {
	// 	log.Fatal("migrate error:", err)
	// }

	// Set connection pool options (opcional)
	sqlDB, _ := db.DB()
	sqlDB.SetMaxIdleConns(10)
	sqlDB.SetMaxOpenConns(50)
	sqlDB.SetConnMaxLifetime(30 * time.Minute)

	DB = db
}
