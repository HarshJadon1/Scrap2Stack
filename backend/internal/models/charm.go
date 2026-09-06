package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type CharmAction string

const (
	ActionVerifiedCommit        CharmAction = "VERIFIED_COMMIT"
	ActionTaskCompleted         CharmAction = "TASK_COMPLETED"
	ActionVerifiedPullRequest   CharmAction = "VERIFIED_PULL_REQUEST"
	ActionMergedPullRequest     CharmAction = "MERGED_PULL_REQUEST"
	ActionIssueResolved         CharmAction = "ISSUE_RESOLVED"
	ActionMvpReleased           CharmAction = "MVP_RELEASED"
	ActionProjectCompleted      CharmAction = "PROJECT_COMPLETED"
	ActionMeaningfulContribution CharmAction = "MEANINGFUL_CONTRIBUTION"
)

type VerificationStatus string

const (
	VerificationPending  VerificationStatus = "PENDING"
	VerificationVerified VerificationStatus = "VERIFIED"
	VerificationRejected VerificationStatus = "REJECTED"
)

type CharmTransaction struct {
	ID                 primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	UserID             primitive.ObjectID `bson:"user_id" json:"userId"`
	ProjectID          primitive.ObjectID `bson:"project_id" json:"projectId"`
	ContributionID     *primitive.ObjectID `bson:"contribution_id,omitempty" json:"contributionId,omitempty"`
	Action             CharmAction        `bson:"action" json:"action"`
	Charms             int                `bson:"charms" json:"charms"`
	VerificationStatus VerificationStatus `bson:"verification_status" json:"verificationStatus"`
	IdempotencyKey     string             `bson:"idempotency_key" json:"idempotencyKey"`
	Metadata           map[string]string  `bson:"metadata" json:"metadata"`
	CreatedAt          time.Time          `bson:"created_at" json:"createdAt"`
}
