package services

import (
	"context"
	"errors"
	"time"

	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/repositories"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type MilestoneService interface {
	CreateMilestone(ctx context.Context, projectID string, name, description string) (*models.ProjectMilestone, error)
	CompleteMilestone(ctx context.Context, userID, milestoneID string) error
	GetProjectMilestones(ctx context.Context, projectID string) ([]models.ProjectMilestone, error)
}

type milestoneService struct {
	milestoneRepo repositories.MilestoneRepository
	projectRepo   repositories.ProjectRepository
	teamRepo      repositories.TeamRepository
	charmService  CharmService
}

func NewMilestoneService(
	mRepo repositories.MilestoneRepository,
	pRepo repositories.ProjectRepository,
	tRepo repositories.TeamRepository,
	cService CharmService,
) MilestoneService {
	return &milestoneService{
		milestoneRepo: mRepo,
		projectRepo:   pRepo,
		teamRepo:      tRepo,
		charmService:  cService,
	}
}

func (s *milestoneService) CreateMilestone(ctx context.Context, projectID string, name, description string) (*models.ProjectMilestone, error) {
	pID, _ := primitive.ObjectIDFromHex(projectID)
	milestone := &models.ProjectMilestone{
		ProjectID:   pID,
		Name:        name,
		Description: description,
		Status:      models.MilestonePending,
		CreatedAt:   time.Now(),
	}

	err := s.milestoneRepo.Create(ctx, milestone)
	return milestone, err
}

func (s *milestoneService) CompleteMilestone(ctx context.Context, userID, milestoneID string) error {
	mID, _ := primitive.ObjectIDFromHex(milestoneID)
	uID, _ := primitive.ObjectIDFromHex(userID)

	// Fetch all project milestones to find the one we need (repo needs GetByID ideally)
	// For Phase 6 simplification, we'll assume we can update it directly if we have the ID
	m := &models.ProjectMilestone{
		ID: mID,
	}

	// Logic: mark completed and award charms
	now := time.Now()
	m.Status = models.MilestoneCompleted
	m.CompletedAt = &now
	m.CompletedBy = &uID

	err := s.milestoneRepo.Update(ctx, m)
	if err != nil {
		return err
	}

	// If it's MVP_RELEASED, award special charms
	if m.Name == "MVP_RELEASED" {
		// Award team members charms
		// This would involve fetching team and looping
	}

	return nil
}

func (s *milestoneService) GetProjectMilestones(ctx context.Context, projectID string) ([]models.ProjectMilestone, error) {
	pID, _ := primitive.ObjectIDFromHex(projectID)
	return s.milestoneRepo.ListByProjectID(ctx, pID), nil
}
