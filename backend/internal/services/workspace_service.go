package services

import (
	"context"
	"errors"
	"time"

	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/repositories"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type WorkspaceService interface {
	GetWorkspace(ctx context.Context, projectID string) (*models.Workspace, error)
	CreateWorkspace(ctx context.Context, projectID, teamID string) (*models.Workspace, error)
	UpdateProgress(ctx context.Context, workspaceID string) (float64, error)
}

type workspaceService struct {
	workspaceRepo repositories.WorkspaceRepository
	projectRepo   repositories.ProjectRepository
	taskRepo      repositories.TaskRepository
	roadmapRepo   repositories.RoadmapRepository
}

func NewWorkspaceService(
	wRepo repositories.WorkspaceRepository,
	pRepo repositories.ProjectRepository,
	tRepo repositories.TaskRepository,
	rRepo repositories.RoadmapRepository,
) WorkspaceService {
	return &workspaceService{
		workspaceRepo: wRepo,
		projectRepo:   pRepo,
		taskRepo:      tRepo,
		roadmapRepo:   rRepo,
	}
}

func (s *workspaceService) GetWorkspace(ctx context.Context, projectID string) (*models.Workspace, error) {
	pID, _ := primitive.ObjectIDFromHex(projectID)
	return s.workspaceRepo.GetByProjectID(ctx, pID)
}

func (s *workspaceService) CreateWorkspace(ctx context.Context, projectID, teamID string) (*models.Workspace, error) {
	pID, _ := primitive.ObjectIDFromHex(projectID)
	tID, _ := primitive.ObjectIDFromHex(teamID)

	project, err := s.projectRepo.GetByID(ctx, pID)
	if err != nil {
		return nil, err
	}

	workspace := &models.Workspace{
		ProjectID:    pID,
		TeamID:       tID,
		Name:         project.Name + " Workspace",
		Status:       models.WorkspaceActive,
		CurrentPhase: "Initialization",
		CreatedAt:    time.Now(),
	}

	if err := s.workspaceRepo.Create(ctx, workspace); err != nil {
		return nil, err
	}

	return workspace, nil
}

func (s *workspaceService) UpdateProgress(ctx context.Context, workspaceID string) (float64, error) {
	wID, _ := primitive.ObjectIDFromHex(workspaceID)
	workspace, err := s.workspaceRepo.GetByID(ctx, wID)
	if err != nil {
		return 0, err
	}

	// Calculate Task Progress
	tasks, _ := s.taskRepo.ListByWorkspaceID(ctx, wID)
	taskProgress := 0.0
	if len(tasks) > 0 {
		completed := 0
		for _, t := range tasks {
			if t.Status == models.TaskStatusCompleted {
				completed++
			}
		}
		taskProgress = (float64(completed) / float64(len(tasks))) * 100
	}

	// Calculate Roadmap Progress
	roadmap, _ := s.roadmapRepo.GetByProjectID(ctx, workspace.ProjectID)
	roadmapProgress := 0.0
	if roadmap != nil {
		items, _ := s.roadmapRepo.GetItems(ctx, roadmap.ID)
		if len(items) > 0 {
			completed := 0
			for _, item := range items {
				if item.Status == models.RoadmapCompleted {
					completed++
				}
			}
			roadmapProgress = (float64(completed) / float64(len(items))) * 100
		}
	}

	// Combined Progress (weighted 50/50 for now)
	overall := (taskProgress + roadmapProgress) / 2
	workspace.Progress = overall
	workspace.UpdatedAt = time.Now()

	err = s.workspaceRepo.Update(ctx, workspace)
	return overall, err
}
