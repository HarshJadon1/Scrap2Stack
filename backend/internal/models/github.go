package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type GitHubRepository struct {
	ID                 primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	GitHubRepositoryID int64              `bson:"github_repository_id" json:"github_repository_id"`
	Owner              string             `bson:"owner" json:"owner"`
	Name               string             `bson:"name" json:"name"`
	FullName           string             `bson:"full_name" json:"full_name"`
	Description        string             `bson:"description" json:"description"`
	URL                string             `bson:"url" json:"url"`
	HTMLURL            string             `bson:"html_url" json:"html_url"`
	DefaultBranch      string             `bson:"default_branch" json:"default_branch"`
	Languages          map[string]int     `bson:"languages" json:"languages"`
	README             string             `bson:"readme" json:"readme"`
	Stars              int                `bson:"stars" json:"stars"`
	Forks              int                `bson:"forks" json:"forks"`
	OpenIssues         int                `bson:"open_issues" json:"open_issues"`
	Contributors       []string           `bson:"contributors" json:"contributors"`
	PullRequests       int                `bson:"pull_requests" json:"pull_requests"`
	LatestCommitDate   time.Time          `bson:"latest_commit_date" json:"latest_commit_date"`
	LastActivityDate   time.Time          `bson:"last_activity_date" json:"last_activity_date"`
	IsPrivate          bool               `bson:"is_private" json:"is_private"`
	CreatedAt          time.Time          `bson:"created_at" json:"created_at"`
	UpdatedAt          time.Time          `bson:"updated_at" json:"updated_at"`
	SyncedAt           time.Time          `bson:"synced_at" json:"synced_at"`
}
