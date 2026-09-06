package services

import (
	"context"
	"sort"
	"strings"

	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/repositories"
)

type ProjectRecommendation struct {
	Project         models.Project `json:"project"`
	MatchPercentage int            `json:"matchPercentage"`
	Why             string         `json:"why"`
	RequiredSkills  []string       `json:"requiredSkills"`
	RevivalScore    int            `json:"revivalScore"`
	ScoreBreakdown  map[string]int `json:"scoreBreakdown"`
}

type RecommendationService interface {
	GetRecommendedProjects(ctx context.Context, userID string, page, limit int) ([]ProjectRecommendation, error)
	GetRecommendedDevelopers(ctx context.Context, projectID string, page, limit int) ([]MatchResult, error)
}

type recommendationService struct {
	userRepo        repositories.UserRepository
	projectRepo     repositories.ProjectRepository
	teamRepo        repositories.TeamRepository
	matchingService MatchingService
}

func NewRecommendationService(
	userRepo repositories.UserRepository,
	projectRepo repositories.ProjectRepository,
	teamRepo repositories.TeamRepository,
	matchingService MatchingService,
) RecommendationService {
	return &recommendationService{
		userRepo:        userRepo,
		projectRepo:     projectRepo,
		teamRepo:        teamRepo,
		matchingService: matchingService,
	}
}

func (s *recommendationService) GetRecommendedProjects(ctx context.Context, userID string, page, limit int) ([]ProjectRecommendation, error) {
	user, err := s.userRepo.GetByID(ctx, repositories.ToObjectID(userID))
	if err != nil {
		return nil, err
	}

	// 1. Fetch all projects (ideally filtered by active/reviving status)
	projects, _, err := s.projectRepo.List(ctx, nil, 1, 1000)
	if err != nil {
		return nil, err
	}

	var recommendations []ProjectRecommendation
	for _, project := range projects {
		// Skip projects the user is already involved in
		if project.OwnerID.Hex() == userID {
			continue
		}

		team, _ := s.teamRepo.GetByProjectID(ctx, project.ID)
		if team != nil {
			members, _ := s.teamRepo.GetMembers(ctx, team.ID)
			isMember := false
			for _, m := range members {
				if m.UserID.Hex() == userID {
					isMember = true
					break
				}
			}
			if isMember {
				continue
			}
		}

		// Use matching service to get the score and breakdown
		score, breakdown, _, matched, _ := s.matchingService.CalculateScore(&project, user)

		// Filter out very low matches
		if score > 30 {
			var skillsList []string
			for _, rs := range project.RequiredSkills {
				skillsList = append(skillsList, rs.Name)
			}

			// Generate explanation
			explanation := s.generateRecommendationExplanation(matched, score, project.Category)

			recommendations = append(recommendations, ProjectRecommendation{
				Project:         project,
				MatchPercentage: score,
				Why:             explanation,
				RequiredSkills:  skillsList,
				RevivalScore:    project.RevivalScore,
				ScoreBreakdown:  breakdown,
			})
		}
	}

	// Sort by match percentage
	sort.Slice(recommendations, func(i, j int) bool {
		return recommendations[i].MatchPercentage > recommendations[j].MatchPercentage
	})

	// Pagination
	start := (page - 1) * limit
	if start >= len(recommendations) {
		return []ProjectRecommendation{}, nil
	}
	end := start + limit
	if end > len(recommendations) {
		end = len(recommendations)
	}

	return recommendations[start:end], nil
}

func (s *recommendationService) GetRecommendedDevelopers(ctx context.Context, projectID string, page, limit int) ([]MatchResult, error) {
	// For now, this is identical to matching service but could include additional
	// logic like availability and "soft" preferences.
	return s.matchingService.GetMatchesForProject(ctx, projectID, page, limit)
}

func (s *recommendationService) generateRecommendationExplanation(matched []string, score int, category string) string {
	if len(matched) > 0 {
		return "Recommended because your " + strings.Join(matched, ", ") + " skills match this project's requirements."
	}
	if score > 70 {
		return "Strong overall fit based on your experience and project category."
	}
	return "Potential contribution opportunity in the " + category + " space."
}
