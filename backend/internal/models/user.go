package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type AvailabilityStatus string

const (
	AvailabilityAvailable         AvailabilityStatus = "AVAILABLE"
	AvailabilityPartiallyAvailable AvailabilityStatus = "PARTIALLY_AVAILABLE"
	AvailabilityBusy               AvailabilityStatus = "BUSY"
)

type ProficiencyLevel string

const (
	ProficiencyBeginner     ProficiencyLevel = "BEGINNER"
	ProficiencyIntermediate ProficiencyLevel = "INTERMEDIATE"
	ProficiencyAdvanced     ProficiencyLevel = "ADVANCED"
	ProficiencyExpert       ProficiencyLevel = "EXPERT"
)

type UserSkill struct {
	SkillID    primitive.ObjectID `bson:"skill_id" json:"skillId"`
	Name       string             `bson:"name" json:"name"`
	Level      ProficiencyLevel   `bson:"level" json:"level"`
	Years      int                `bson:"years" json:"years"`
}

type User struct {
	ID                primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	Name              string             `bson:"name" json:"name"`
	Username          string             `bson:"username" json:"username" validate:"required,unique"`
	Email             string             `bson:"email" json:"email" validate:"required,email,unique"`
	PasswordHash      string             `bson:"password_hash" json:"-"`
	ProfileImage      string             `bson:"profile_image" json:"profile_image"`
	Bio               string             `bson:"bio" json:"bio"`
	ExperienceLevel   ProficiencyLevel   `bson:"experience_level" json:"experience_level"`
	Skills            []UserSkill        `bson:"skills" json:"skills"`
	Interests         []string           `bson:"interests" json:"interests"`
	PreferredRoles    []TeamRole         `bson:"preferred_roles" json:"preferredRoles"`
	ProjectCategories []string           `bson:"project_categories" json:"projectCategories"`
	Availability      AvailabilityStatus `bson:"availability" json:"availability"`
	GitHubUsername    string             `bson:"github_username" json:"github_username"`
	GitHubProfileURL  string             `bson:"github_profile_url" json:"github_profile_url"`
	Charms            int                `bson:"charms" json:"charms"`
	CompletedProjects int                `bson:"completed_projects" json:"completedProjects"`
	MergedPRs         int                `bson:"merged_prs" json:"mergedPrs"`
	CompletedTasks    int                `bson:"completed_tasks" json:"completedTasks"`
	CreatedAt         time.Time          `bson:"created_at" json:"created_at"`
	UpdatedAt         time.Time          `bson:"updated_at" json:"updated_at"`
}
