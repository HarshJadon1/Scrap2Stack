package github

import (
	"time"

	"github.com/scrap2stack/backend/internal/models"
)

func MapToInternalModel(repo *GitHubRepoResponse, languages map[string]int, readme string) *models.GitHubRepository {
	return &models.GitHubRepository{
		GitHubRepositoryID: repo.ID,
		Owner:              repo.Owner.Login,
		Name:               repo.Name,
		FullName:           repo.FullName,
		Description:        repo.Description,
		URL:                repo.HTMLURL,
		HTMLURL:            repo.HTMLURL,
		DefaultBranch:      repo.DefaultBranch,
		Languages:          languages,
		README:             readme,
		Stars:              repo.StargazersCount,
		Forks:              repo.ForksCount,
		OpenIssues:         repo.OpenIssuesCount,
		LatestCommitDate:   repo.PushedAt,
		LastActivityDate:   repo.UpdatedAt,
		IsPrivate:          false, // Assuming public for now as per requirements
		UpdatedAt:          time.Now(),
		SyncedAt:           time.Now(),
	}
}
