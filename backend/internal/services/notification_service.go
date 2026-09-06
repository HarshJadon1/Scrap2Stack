package services

import (
	"context"
	"github.com/scrap2stack/backend/internal/models"
)

type NotificationService interface {
	Notify(ctx context.Context, userID string, notifType models.NotificationType, title, message string) error
}

type notificationService struct{}

func NewNotificationService() NotificationService {
	return &notificationService{}
}

func (s *notificationService) Notify(ctx context.Context, userID string, notifType models.NotificationType, title, message string) error {
	// Architecture for notifications
	return nil
}
