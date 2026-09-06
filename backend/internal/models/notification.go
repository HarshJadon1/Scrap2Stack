package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type NotificationType string

const (
	NotifCollabRequest      NotificationType = "COLLABORATION_REQUEST"
	NotifRequestAccepted    NotificationType = "REQUEST_ACCEPTED"
	NotifTeamInvitation     NotificationType = "TEAM_INVITATION"
	NotifTaskAssigned       NotificationType = "TASK_ASSIGNED"
	NotifProjectUpdate      NotificationType = "PROJECT_UPDATE"
	NotifAiAnalysisComplete NotificationType = "AI_ANALYSIS_COMPLETED"
	NotifGithubActivity     NotificationType = "GITHUB_ACTIVITY"
)

type Notification struct {
	ID        primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	UserID    primitive.ObjectID `bson:"user_id" json:"user_id"`
	Type      NotificationType   `bson:"type" json:"type"`
	Title     string             `bson:"title" json:"title"`
	Message   string             `bson:"message" json:"message"`
	RelatedID *primitive.ObjectID `bson:"related_id,omitempty" json:"related_id,omitempty"`
	IsRead    bool               `bson:"is_read" json:"is_read"`
	CreatedAt time.Time          `bson:"created_at" json:"created_at"`
}
