package dto

import "github.com/scrap2stack/backend/internal/models"

type CreateProjectRequest struct {
	Name           string               `json:"name" binding:"required"`
	Description    string               `json:"description" binding:"required"`
	Problem        string               `json:"problem"`
	Category       string               `json:"category"`
	Technologies   []string             `json:"technologies"`
	RequiredSkills []string             `json:"required_skills"`
	Status         models.ProjectStatus `json:"status" binding:"required"`
	GitHubURL      string               `json:"github_url"`
	TeamSize       int                  `json:"team_size"`
	Difficulty     models.Difficulty    `json:"difficulty"`
}

type UpdateProjectRequest struct {
	Name           *string               `json:"name"`
	Description    *string               `json:"description"`
	Problem        *string               `json:"problem"`
	Category       *string               `json:"category"`
	Technologies   []string             `json:"technologies"`
	RequiredSkills []string             `json:"required_skills"`
	Status         *models.ProjectStatus `json:"status"`
	GitHubURL      *string               `json:"github_url"`
	TeamSize       *int                  `json:"team_size"`
	Difficulty     *models.Difficulty    `json:"difficulty"`
	Progress       *int                  `json:"progress"`
}

type ProjectPaginationResponse struct {
	Projects   []models.Project `json:"projects"`
	Page       int              `json:"page"`
	Limit      int              `json:"limit"`
	Total      int64            `json:"total"`
	TotalPages int              `json:"totalPages"`
}
