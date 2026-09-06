package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type ProjectStatus string

const (
	ProjectStatusIdea       ProjectStatus = "IDEA"
	ProjectStatusIncomplete ProjectStatus = "INCOMPLETE"
	ProjectStatusAbandoned  ProjectStatus = "ABANDONED"
	ProjectStatusActive      ProjectStatus = "ACTIVE"
	ProjectStatusReviving    ProjectStatus = "REVIVING"
	ProjectStatusCompleted   ProjectStatus = "COMPLETED"
	ProjectStatusArchived    ProjectStatus = "ARCHIVED"
)

type Difficulty string

const (
	DifficultyBeginner     Difficulty = "BEGINNER"
	DifficultyIntermediate Difficulty = "INTERMEDIATE"
	DifficultyAdvanced     Difficulty = "ADVANCED"
)

type RequiredRole struct {
	Role       TeamRole `bson:"role" json:"role"`
	Count      int      `bson:"count" json:"count"`
	Filled     int      `bson:"filled" json:"filled"`
	Importance string   `bson:"importance" json:"importance"`
}

type RequiredSkill struct {
	Name          string           `bson:"name" json:"name"`
	RequiredLevel ProficiencyLevel `bson:"required_level" json:"requiredLevel"`
	Importance    string           `bson:"importance" json:"importance"`
}

type Project struct {
	ID                 primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	OwnerID            primitive.ObjectID `bson:"owner_id" json:"owner_id"`
	Name               string             `bson:"name" json:"name"`
	Description        string             `bson:"description" json:"description"`
	Problem            string             `bson:"problem" json:"problem"`
	Category           string             `bson:"category" json:"category"`
	Technologies       []string           `bson:"technologies" json:"technologies"`
	RequiredSkills     []RequiredSkill    `bson:"required_skills" json:"requiredSkills"`
	RequiredRoles      []RequiredRole     `bson:"required_roles" json:"requiredRoles"`
	Status             ProjectStatus      `bson:"status" json:"status"`
	GitHubURL          string             `bson:"github_url" json:"github_url"`
	GitHubRepositoryID int64              `bson:"github_repository_id" json:"github_repository_id"`
	TeamSize           int                `bson:"team_size" json:"team_size"`
	Difficulty         Difficulty         `bson:"difficulty" json:"difficulty"`
	RevivalScore       int                `bson:"revival_score" json:"revival_score"`
	QualityScore       int                `bson:"quality_score" json:"quality_score"`
	AnalysisID         primitive.ObjectID `bson:"analysis_id,omitempty" json:"analysis_id,omitempty"`
	TeamID             primitive.ObjectID `bson:"team_id,omitempty" json:"team_id,omitempty"`
	Progress           int                `bson:"progress" json:"progress"`
	LastActivity       time.Time          `bson:"last_activity" json:"last_activity"`
	CreatedAt          time.Time          `bson:"created_at" json:"created_at"`
	UpdatedAt          time.Time          `bson:"updated_at" json:"updated_at"`
}
