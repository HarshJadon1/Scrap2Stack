package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type CollabRequestStatus string

const (
	CollabPending   CollabRequestStatus = "PENDING"
	CollabAccepted  CollabRequestStatus = "ACCEPTED"
	CollabRejected  CollabRequestStatus = "REJECTED"
	CollabCancelled CollabRequestStatus = "CANCELLED"
	CollabExpired   CollabRequestStatus = "EXPIRED"
)

type CollaborationRequest struct {
	ID           primitive.ObjectID  `bson:"_id,omitempty" json:"id"`
	ProjectID    primitive.ObjectID  `bson:"project_id" json:"project_id"`
	SenderID     primitive.ObjectID  `bson:"sender_id" json:"sender_id"`
	ReceiverID   primitive.ObjectID  `bson:"receiver_id" json:"receiver_id"`
	ProposedRole TeamRole            `bson:"proposed_role" json:"proposedRole"`
	Message      string              `bson:"message" json:"message"`
	Status       CollabRequestStatus `bson:"status" json:"status"`
	CreatedAt    time.Time           `bson:"created_at" json:"created_at"`
	UpdatedAt    time.Time           `bson:"updated_at" json:"updated_at"`
}
