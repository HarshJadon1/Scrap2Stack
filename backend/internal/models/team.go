package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type TeamRole string

const (
	RoleProjectLead     TeamRole = "PROJECT_LEAD"
	RoleAndroidDev      TeamRole = "ANDROID_DEVELOPER"
	RoleBackendDev      TeamRole = "BACKEND_DEVELOPER"
	RoleFrontendDev     TeamRole = "FRONTEND_DEVELOPER"
	RoleAiMlDev         TeamRole = "AI_ML_DEVELOPER"
	RoleUiUxDesigner    TeamRole = "UI_UX_DESIGNER"
	RoleDevOps          TeamRole = "DEVOPS"
	RoleQa              TeamRole = "QA"
	RoleDataScientist   TeamRole = "DATA_SCIENTIST"
)

type TeamStatus string

const (
	TeamStatusForming   TeamStatus = "FORMING"
	TeamStatusActive    TeamStatus = "ACTIVE"
	TeamStatusCompleted TeamStatus = "COMPLETED"
	TeamStatusArchived  TeamStatus = "ARCHIVED"
)

type MemberStatus string

const (
	MemberStatusInvited MemberStatus = "INVITED"
	MemberStatusActive  MemberStatus = "ACTIVE"
	MemberStatusLeft    MemberStatus = "LEFT"
	MemberStatusRemoved MemberStatus = "REMOVED"
)

type Team struct {
	ID          primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	ProjectID   primitive.ObjectID `bson:"project_id" json:"project_id"`
	Name        string             `bson:"name" json:"name"`
	OwnerID     primitive.ObjectID `bson:"owner_id" json:"owner_id"`
	Description string             `bson:"description" json:"description"`
	MaxMembers  int                `bson:"max_members" json:"maxMembers"`
	Status      TeamStatus         `bson:"status" json:"status"`
	CreatedAt   time.Time          `bson:"created_at" json:"created_at"`
	UpdatedAt   time.Time          `bson:"updated_at" json:"updated_at"`
}

type TeamMember struct {
	ID       primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	TeamID   primitive.ObjectID `bson:"team_id" json:"team_id"`
	UserID   primitive.ObjectID `bson:"user_id" json:"user_id"`
	Role     TeamRole           `bson:"role" json:"role"`
	Status   MemberStatus       `bson:"status" json:"status"`
	JoinedAt time.Time          `bson:"joined_at" json:"joined_at"`
}
