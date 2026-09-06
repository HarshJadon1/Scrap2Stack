package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type WorkspaceStatus string

const (
	WorkspaceActive    WorkspaceStatus = "ACTIVE"
	WorkspacePaused    WorkspaceStatus = "PAUSED"
	WorkspaceCompleted WorkspaceStatus = "COMPLETED"
	WorkspaceArchived  WorkspaceStatus = "ARCHIVED"
)

type Workspace struct {
	ID           primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	ProjectID    primitive.ObjectID `bson:"project_id" json:"projectId"`
	TeamID       primitive.ObjectID `bson:"team_id" json:"teamId"`
	Name         string             `bson:"name" json:"name"`
	Description  string             `bson:"description" json:"description"`
	Status       WorkspaceStatus    `bson:"status" json:"status"`
	Progress     float64            `bson:"progress" json:"progress"`
	CurrentPhase string             `bson:"current_phase" json:"currentPhase"`
	CreatedAt    time.Time          `bson:"created_at" json:"createdAt"`
	UpdatedAt    time.Time          `bson:"updated_at" json:"updatedAt"`
}
