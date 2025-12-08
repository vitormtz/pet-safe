package models

import (
	"time"
)

type User struct {
	ID           uint64    `gorm:"primaryKey" json:"id"`
	Email        string    `gorm:"size:255;uniqueIndex;not null" json:"email"`
	PasswordHash string    `gorm:"size:255;not null" json:"-"`
	FullName     string    `gorm:"size:120" json:"full_name"`
	Phone        string    `gorm:"size:20" json:"phone"`
	CreatedAt    time.Time `gorm:"autoCreateTime" json:"created_at"`
	UpdatedAt    time.Time `gorm:"autoUpdateTime" json:"updated_at"`
	Pets         []Pet     `gorm:"foreignKey:OwnerID" json:"pets,omitempty"`
	Devices      []Device  `gorm:"foreignKey:OwnerID" json:"devices,omitempty"`
}

type Pet struct {
	ID          uint64     `gorm:"primaryKey" json:"id"`
	OwnerID     uint64     `gorm:"not null;index" json:"owner_id"`
	Name        string     `gorm:"size:80;not null" json:"name"`
	Species     string     `gorm:"size:40" json:"species"`
	Breed       string     `gorm:"size:80" json:"breed"`
	DOB         *time.Time `json:"dob"`
	MicrochipID string     `gorm:"size:80" json:"microchip_id"`
	CreatedAt   time.Time  `gorm:"autoCreateTime" json:"created_at"`
}

type Device struct {
	ID             uint64     `gorm:"primaryKey" json:"id"`
	SerialNumber   string     `gorm:"size:64;uniqueIndex;not null" json:"serial_number"`
	IMEI           string     `gorm:"size:64" json:"imei"`
	Model          string     `gorm:"size:80" json:"model"`
	Firmware       string     `gorm:"size:50" json:"firmware"`
	OwnerID        uint64     `gorm:"not null;index" json:"owner_id"`
	PetID          *uint64    `json:"pet_id"`
	Connectivity   string     `gorm:"size:20" json:"connectivity"`
	Active         bool       `gorm:"default:true" json:"active"`
	LastComm       *time.Time `json:"last_comm"`
	LastLatitude   *float64   `gorm:"type:numeric(9,6)" json:"last_latitude"`
	LastLongitude  *float64   `gorm:"type:numeric(9,6)" json:"last_longitude"`
	BatteryPercent *float32   `json:"battery_percent"`
	CreatedAt      time.Time  `gorm:"autoCreateTime" json:"created_at"`
}

type BatteryHistory struct {
	ID             uint64     `gorm:"primaryKey" json:"id"`
	DeviceID       uint64     `gorm:"index;not null" json:"device_id"`
	Ts             *time.Time `json:"ts"`
	BatteryPercent float32    `json:"battery_percent"`
}

type DeviceEvent struct {
	ID             uint64    `gorm:"primaryKey" json:"id"`
	DeviceID       uint64    `gorm:"index;not null" json:"device_id"`
	EventType      string    `gorm:"size:80;not null" json:"event_type"`
	EventTimestamp time.Time `json:"event_timestamp"`
	Metadata       string    `gorm:"type:json" json:"metadata"`
}

type Geofence struct {
	ID        uint64    `gorm:"primaryKey" json:"id"`
	OwnerID   uint64    `gorm:"not null;index" json:"owner_id"`
	Name      string    `gorm:"size:100" json:"name"`
	Latitude  *float64  `gorm:"type:numeric(9,6)" json:"latitude"`
	Longitude *float64  `gorm:"type:numeric(9,6)" json:"longitude"`
	RadiusM   *int      `json:"radius_m"`
	Active    bool      `gorm:"default:true" json:"active"`
	CreatedAt time.Time `gorm:"autoCreateTime" json:"created_at"`
}

type Alert struct {
	ID             uint64     `gorm:"primaryKey" json:"id"`
	DeviceID       uint64     `gorm:"index;not null" json:"device_id"`
	GeofenceID     *uint64    `json:"geofence_id"`
	EventID        *uint64    `json:"event_id"`
	AlertType      string     `gorm:"size:80;not null" json:"alert_type"`
	AlertTimestamp time.Time  `json:"alert_timestamp"`
	AcknowledgedBy *uint64    `json:"acknowledged_by"`
	AcknowledgedAt *time.Time `json:"acknowledged_at"`
}

type Notification struct {
	ID       uint64     `gorm:"primaryKey" json:"id"`
	AlertID  uint64     `gorm:"index;not null" json:"alert_id"`
	UserID   uint64     `gorm:"index;not null" json:"user_id"`
	SentAt   *time.Time `json:"sent_at"`
	Attempts *int       `json:"attempts"`
}

type Location struct {
	ID         uint64    `gorm:"primaryKey" json:"id"`
	DeviceID   uint64    `gorm:"index;not null" json:"device_id"`
	Latitude   float64   `gorm:"type:numeric(9,6);not null" json:"latitude"`
	Longitude  float64   `gorm:"type:numeric(9,6);not null" json:"longitude"`
	Accuracy   *float32  `json:"accuracy"`
	Speed      *float32  `json:"speed"`
	Heading    *float32  `json:"heading"`
	UpdatedAt  int64     `json:"updated_at"`
	ReceivedAt time.Time `gorm:"autoCreateTime" json:"received_at"`
}

type GeofenceDevice struct {
	GeofenceID uint64 `gorm:"primaryKey;autoIncrement:false" json:"geofence_id"`
	DeviceID   uint64 `gorm:"primaryKey;autoIncrement:false" json:"device_id"`
}

func (GeofenceDevice) TableName() string {
	return "geofence_device"
}

type RefreshToken struct {
	ID        uint64    `gorm:"primaryKey" json:"id"`
	UserID    uint64    `gorm:"index;not null" json:"user_id"`
	TokenHash string    `gorm:"size:255" json:"token_hash"`
	Revoked   bool      `json:"revoked"`
	ExpiresAt time.Time `json:"expires_at"`
}
