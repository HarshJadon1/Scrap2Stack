package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type RoadmapStatus string

const (
	RoadmapNotStarted RoadmapStatus = "NOT_STARTED"
	RoadmapInProgress RoadmapStatus = "IN_PROGRESS"
	RoadmapCompleted  RoadmapStatus = "COMPLETED"
	RoadmapBlocked    RoadmapStatus = "BLOCKED"
)

type Roadmap struct {
	ID           primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	ProjectID    primitive.ObjectID `bson:"project_id" json:"projectId"`
	WorkspaceID  primitive.ObjectID `bson:"workspace_id" json:"workspaceId"`
	Title        string             `bson:"title" json:"title"`
	Description  string             `bson:"description" json:"description"`
	Status       RoadmapStatus      `bson:"status" json:"status"`
	CreatedBy    primitive.ObjectID `bson:"created_by" json:"createdBy"`
	GeneratedByAI bool               `bson:"generated_by_ai" json:"generatedByAI"`
	CreatedAt    time.Time          `bson:"created_at" json:"createdAt"`
	UpdatedAt    time.Time          `bson:"updated_at" json:"updatedAt"`
}

type RoadmapItem struct {
	ID              primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	RoadmapID       primitive.ObjectID `bson:"roadmap_id" json:"roadmapId"`
	Title           string             `bson:"title" json:"title"`
	Description     string             `bson:"description" json:"description"`
	Order           int                `bson:"order" json:"order"`
	Status          RoadmapStatus      `bson:"status" json:"status"`
	RequiredSkills  []string           `bson:"required_skills" json:"requiredSkills"`
	RequiredRoles   []TeamRole         `bson:"required_roles" json:"requiredRoles"`
	EstimatedEffort string             `bson:"estimated_effort" json:"estimatedEffort"`
	Dependencies    []string           `bson:"dependencies" json:"dependencies"`
	Milestone       bool               `bson:"milestone" json:"milestone"`
	CreatedAt       time.Time          `bson:"created_at" json:"createdAt"`
	UpdatedAt       time.Time          `bson:"updated_at" json:"updatedAt"`
}
