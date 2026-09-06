package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type MilestoneStatus string

const (
	MilestonePending   MilestoneStatus = "PENDING"
	MilestoneCompleted MilestoneStatus = "COMPLETED"
)

type ProjectMilestone struct {
	ID          primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	ProjectID   primitive.ObjectID `bson:"project_id" json:"projectId"`
	Name        string             `bson:"name" json:"name"`
	Description string             `bson:"description" json:"description"`
	Status      MilestoneStatus    `bson:"status" json:"status"`
	CompletedAt *time.Time         `bson:"completed_at,omitempty" json:"completedAt,omitempty"`
	CompletedBy *primitive.ObjectID `bson:"completed_by,omitempty" json:"completedBy,omitempty"`
	CreatedAt   time.Time          `bson:"created_at" json:"createdAt"`
}
