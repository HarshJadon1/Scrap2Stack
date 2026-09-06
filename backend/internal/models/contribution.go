package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type ContributionType string

const (
	ContributionCommit              ContributionType = "COMMIT"
	ContributionPullRequest         ContributionType = "PULL_REQUEST"
	ContributionMergedPullRequest   ContributionType = "MERGED_PULL_REQUEST"
	ContributionIssue               ContributionType = "ISSUE"
	ContributionIssueClosed         ContributionType = "ISSUE_CLOSED"
	ContributionRelease             ContributionType = "RELEASE"
)

type GitHubContribution struct {
	ID               primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	UserID           primitive.ObjectID `bson:"user_id" json:"userId"`
	ProjectID        primitive.ObjectID `bson:"project_id" json:"projectId"`
	RepositoryID     int64              `bson:"repository_id" json:"repositoryId"`
	Type             ContributionType   `bson:"type" json:"type"`
	GitHubEventID    string             `bson:"github_event_id" json:"githubEventId"`
	Title            string             `bson:"title" json:"title"`
	URL              string             `bson:"url" json:"url"`
	Status           string             `bson:"status" json:"status"`
	Verified         bool               `bson:"verified" json:"verified"`
	Metadata         map[string]string  `bson:"metadata" json:"metadata"`
	ContributionDate time.Time          `bson:"contribution_date" json:"contributionDate"`
	CreatedAt        time.Time          `bson:"created_at" json:"createdAt"`
}
