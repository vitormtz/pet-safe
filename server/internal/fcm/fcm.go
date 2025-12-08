package fcm

import (
	"context"
	"fmt"
	"log"

	firebase "firebase.google.com/go/v4"
	"firebase.google.com/go/v4/messaging"
	"google.golang.org/api/option"
)

var (
	messagingClient *messaging.Client
)

// InitializeFirebase initializes Firebase Admin SDK
// credentialsPath should point to your service account JSON file
func InitializeFirebase(credentialsPath string) error {
	opt := option.WithCredentialsFile(credentialsPath)
	app, err := firebase.NewApp(context.Background(), nil, opt)
	if err != nil {
		return fmt.Errorf("error initializing Firebase app: %v", err)
	}

	client, err := app.Messaging(context.Background())
	if err != nil {
		return fmt.Errorf("error getting Messaging client: %v", err)
	}

	messagingClient = client
	log.Println("Firebase Admin SDK initialized successfully")
	return nil
}

// SendNotification sends a push notification to a specific FCM token
func SendNotification(token, title, body string, data map[string]string) error {
	if messagingClient == nil {
		return fmt.Errorf("Firebase messaging client not initialized")
	}

	message := &messaging.Message{
		Token: token,
		Notification: &messaging.Notification{
			Title: title,
			Body:  body,
		},
		Data: data,
		Android: &messaging.AndroidConfig{
			Priority: "high",
			Notification: &messaging.AndroidNotification{
				Sound: "default",
				Tag:   "petsafe_alert",
			},
		},
	}

	response, err := messagingClient.Send(context.Background(), message)
	if err != nil {
		return fmt.Errorf("error sending notification: %v", err)
	}

	log.Printf("Successfully sent notification: %s", response)
	return nil
}

// SendGeofenceExitNotification sends a geofence exit alert notification
func SendGeofenceExitNotification(token, petName string, deviceID uint64) error {
	title := "⚠️ Alerta de Geofence"
	body := fmt.Sprintf("%s saiu da área segura!", petName)

	data := map[string]string{
		"alert_type": "geofence_exit",
		"device_id":  fmt.Sprintf("%d", deviceID),
		"pet_name":   petName,
	}

	return SendNotification(token, title, body, data)
}
