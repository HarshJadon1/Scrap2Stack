package github

import (
	"context"
	"fmt"
	"regexp"
	"strings"

	"github.com/scrap2stack/backend/internal/models"
)

type GitHubService interface {
	ParseURL(repoURL string) (owner string, repo string, err error)
	FetchRepositoryData(ctx context.Context, owner, repo string) (*models.GitHubRepository, error)
}

type githubService struct {
	client GitHubClient
}

func NewGitHubService(client GitHubClient) GitHubService {
	return &githubService{
		client: client,
	}
}

func (s *githubService) ParseURL(repoURL string) (string, string, error) {
	// Simple regex for GitHub URLs
	re := regexp.MustCompile(`github\.com/([^/]+)/([^/]+)`)
	matches := re.FindStringSubmatch(repoURL)
	if len(matches) != 3 {
		return "", "", ErrInvalidURL
	}

	owner := matches[1]
	repo := strings.TrimSuffix(matches[2], ".git")
	return owner, repo, nil
}

func (s *githubService) FetchRepositoryData(ctx context.Context, owner, repo string) (*models.GitHubRepository, error) {
	repoResp, err := s.client.GetRepository(owner, repo)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch repository: %w", err)
	}

	languages, err := s.client.GetLanguages(owner, repo)
	if err != nil {
		// Log error but continue
		languages = make(map[string]int)
	}

	readme, err := s.client.GetREADME(owner, repo)
	if err != nil {
		// Log error but continue
		readme = ""
	}

	return MapToInternalModel(repoResp, languages, readme), nil
}
