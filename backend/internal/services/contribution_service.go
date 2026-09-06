package services

import (
	"context"
	"fmt"
	"time"

	"github.com/scrap2stack/backend/internal/github"
	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/repositories"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type ContributionService interface {
	SyncGitHubActivity(ctx context.Context, projectID string) error
	GetContributions(ctx context.Context, userID string) ([]models.GitHubContribution, error)
}

type contributionService struct {
	ghClient       github.GitHubClient
	projectRepo    repositories.ProjectRepository
	ghRepo         repositories.GitHubRepository
	contributeRepo repositories.ContributionRepository
	userRepo       repositories.UserRepository
	charmService   CharmService
}

func NewContributionService(
	ghClient github.GitHubClient,
	pRepo repositories.ProjectRepository,
	ghRepo repositories.GitHubRepository,
	cRepo repositories.ContributionRepository,
	uRepo repositories.UserRepository,
	charmService CharmService,
) ContributionService {
	return &contributionService{
		ghClient:       ghClient,
		projectRepo:    pRepo,
		ghRepo:         ghRepo,
		contributeRepo: cRepo,
		userRepo:       uRepo,
		charmService:   charmService,
	}
}

func (s *contributionService) SyncGitHubActivity(ctx context.Context, projectID string) error {
	pID, _ := primitive.ObjectIDFromHex(projectID)
	project, err := s.projectRepo.GetByID(ctx, pID)
	if err != nil {
		return err
	}

	ghData, err := s.ghRepo.GetByGitHubID(ctx, project.GitHubRepositoryID)
	if err != nil {
		return err
	}

	// Fetch Events from GitHub (Simulated for Phase 6 as we don't have list events in client yet)
	// In a real app, s.ghClient.ListEvents(ghData.Owner, ghData.Name)

	// Mock syncing a specific known event for testing idempotency
	mockEventID := "12345"
	existing, _ := s.contributeRepo.GetByGitHubEventID(ctx, mockEventID)
	if existing != nil {
		return nil // Already synced
	}

	// For demo: search for John Developer's GitHub username
	user, err := s.userRepo.GetByUsername(ctx, "johndev")
	if err != nil {
		return nil // No user to assign to
	}

	contribution := &models.GitHubContribution{
		UserID:           user.ID,
		ProjectID:        pID,
		RepositoryID:     ghData.GitHubRepositoryID,
		Type:             models.ContributionMergedPullRequest,
		GitHubEventID:    mockEventID,
		Title:            "Fix authentication middleware",
		URL:              "https://github.com/example/repo/pull/123",
		Verified:         true,
		ContributionDate: time.Now(),
	}

	err = s.contributeRepo.Create(ctx, contribution)
	if err != nil {
		return err
	}

	// Award Charms
	return s.charmService.AwardCharms(ctx, user.ID.Hex(), projectID, models.ActionMergedPullRequest, &contribution.ID)
}

func (s *contributionService) GetContributions(ctx context.Context, userID string) ([]models.GitHubContribution, error) {
	uID, _ := primitive.ObjectIDFromHex(userID)
	return s.contributeRepo.ListByUserID(ctx, uID)
}
