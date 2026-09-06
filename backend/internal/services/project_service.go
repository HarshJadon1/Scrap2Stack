package services

import (
	"context"
	"errors"

	"github.com/scrap2stack/backend/internal/dto"
	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/repositories"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type ProjectService interface {
	CreateProject(ctx context.Context, userID string, req dto.CreateProjectRequest) (*models.Project, error)
	GetProject(ctx context.Context, id string) (*models.Project, error)
	ListProjects(ctx context.Context, query map[string]string, page, limit int) (*dto.ProjectPaginationResponse, error)
	UpdateProject(ctx context.Context, userID, projectID string, req dto.UpdateProjectRequest) (*models.Project, error)
	DeleteProject(ctx context.Context, userID, projectID string) error
}

type projectService struct {
	projectRepo repositories.ProjectRepository
}

func NewProjectService(projectRepo repositories.ProjectRepository) ProjectService {
	return &projectService{
		projectRepo: projectRepo,
	}
}

func (s *projectService) CreateProject(ctx context.Context, userID string, req dto.CreateProjectRequest) (*models.Project, error) {
	ownerID, err := primitive.ObjectIDFromHex(userID)
	if err != nil {
		return nil, err
	}

	project := &models.Project{
		OwnerID:        ownerID,
		Name:           req.Name,
		Description:    req.Description,
		Problem:        req.Problem,
		Category:       req.Category,
		Technologies:   req.Technologies,
		RequiredSkills: req.RequiredSkills,
		Status:         req.Status,
		GitHubURL:      req.GitHubURL,
		TeamSize:       req.TeamSize,
		Difficulty:     req.Difficulty,
		Progress:       0,
	}

	if err := s.projectRepo.Create(ctx, project); err != nil {
		return nil, err
	}

	return project, nil
}

func (s *projectService) GetProject(ctx context.Context, id string) (*models.Project, error) {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, err
	}
	return s.projectRepo.GetByID(ctx, objID)
}

func (s *projectService) ListProjects(ctx context.Context, query map[string]string, page, limit int) (*dto.ProjectPaginationResponse, error) {
	filter := bson.M{}

	if q, ok := query["search"]; ok && q != "" {
		filter["$or"] = []bson.M{
			{"name": bson.M{"$regex": q, "$options": "i"}},
			{"description": bson.M{"$regex": q, "$options": "i"}},
		}
	}

	if status, ok := query["status"]; ok && status != "" {
		filter["status"] = status
	}

	projects, total, err := s.projectRepo.List(ctx, filter, page, limit)
	if err != nil {
		return nil, err
	}

	totalPages := int(total) / limit
	if int(total)%limit != 0 {
		totalPages++
	}

	return &dto.ProjectPaginationResponse{
		Projects:   projects,
		Page:       page,
		Limit:      limit,
		Total:      total,
		TotalPages: totalPages,
	}, nil
}

func (s *projectService) UpdateProject(ctx context.Context, userID, projectID string, req dto.UpdateProjectRequest) (*models.Project, error) {
	objID, err := primitive.ObjectIDFromHex(projectID)
	if err != nil {
		return nil, err
	}

	project, err := s.projectRepo.GetByID(ctx, objID)
	if err != nil {
		return nil, err
	}

	if project.OwnerID.Hex() != userID {
		return nil, errors.New("unauthorized: not the project owner")
	}

	if req.Name != nil { project.Name = *req.Name }
	if req.Description != nil { project.Description = *req.Description }
	if req.Problem != nil { project.Problem = *req.Problem }
	if req.Category != nil { project.Category = *req.Category }
	if req.Technologies != nil { project.Technologies = req.Technologies }
	if req.RequiredSkills != nil { project.RequiredSkills = req.RequiredSkills }
	if req.Status != nil { project.Status = *req.Status }
	if req.GitHubURL != nil { project.GitHubURL = *req.GitHubURL }
	if req.TeamSize != nil { project.TeamSize = *req.TeamSize }
	if req.Difficulty != nil { project.Difficulty = *req.Difficulty }
	if req.Progress != nil { project.Progress = *req.Progress }

	if err := s.projectRepo.Update(ctx, project); err != nil {
		return nil, err
	}

	return project, nil
}

func (s *projectService) DeleteProject(ctx context.Context, userID, projectID string) error {
	objID, err := primitive.ObjectIDFromHex(projectID)
	if err != nil {
		return err
	}

	project, err := s.projectRepo.GetByID(ctx, objID)
	if err != nil {
		return err
	}

	if project.OwnerID.Hex() != userID {
		return errors.New("unauthorized: not the project owner")
	}

	return s.projectRepo.Delete(ctx, objID)
}
