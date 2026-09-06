package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type TaskPriority string

const (
	TaskPriorityLow      TaskPriority = "LOW"
	TaskPriorityMedium   TaskPriority = "MEDIUM"
	TaskPriorityHigh     TaskPriority = "HIGH"
	TaskPriorityCritical TaskPriority = "CRITICAL"
)

type TaskStatus string

const (
	TaskStatusTodo       TaskStatus = "TODO"
	TaskStatusInProgress TaskStatus = "IN_PROGRESS"
	TaskStatusInReview   TaskStatus = "IN_REVIEW"
	TaskStatusBlocked    TaskStatus = "BLOCKED"
	TaskStatusCompleted  TaskStatus = "COMPLETED"
)

type Task struct {
	ID             primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	ProjectID      primitive.ObjectID `bson:"project_id" json:"projectId"`
	WorkspaceID    primitive.ObjectID `bson:"workspace_id" json:"workspaceId"`
	Title          string             `bson:"title" json:"title" validate:"required"`
	Description    string             `bson:"description" json:"description"`
	AssigneeID     *primitive.ObjectID `bson:"assignee_id,omitempty" json:"assigneeId,omitempty"`
	CreatedBy      primitive.ObjectID `bson:"created_by" json:"createdBy"`
	Skill          string             `bson:"skill" json:"skill"`
	Role           TeamRole           `bson:"role" json:"role"`
	Priority       TaskPriority       `bson:"priority" json:"priority"`
	Status         TaskStatus         `bson:"status" json:"status"`
	DueDate        *time.Time         `bson:"due_date,omitempty" json:"dueDate,omitempty"`
	EstimatedHours int                `bson:"estimated_hours" json:"estimatedHours"`
	ActualHours    int                `bson:"actual_hours" json:"actualHours"`
	CreatedAt      time.Time          `bson:"created_at" json:"createdAt"`
	UpdatedAt      time.Time          `bson:"updated_at" json:"updatedAt"`
	CompletedAt    *time.Time         `bson:"completed_at,omitempty" json:"completedAt,omitempty"`
}
