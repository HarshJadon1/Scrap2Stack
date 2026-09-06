package services

import (
	"context"
	"fmt"
	"time"

	"github.com/scrap2stack/backend/internal/ai"
	"github.com/scrap2stack/backend/internal/github"
	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/repositories"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type ImportService interface {
	ImportFromGitHub(ctx context.Context, userID, repoURL string) (*models.Project, *models.ProjectAnalysis, error)
}

type importService struct {
	githubService github.GitHubService
	aiService     AIAnalyzer
	projectRepo   repositories.ProjectRepository
	githubRepo    repositories.GitHubRepository
	analysisRepo  repositories.AnalysisRepository
	revivalService RevivalScoreService
}

func NewImportService(
	gh github.GitHubService,
	ai AIAnalyzer,
	pRepo repositories.ProjectRepository,
	ghRepo repositories.GitHubRepository,
	aRepo repositories.AnalysisRepository,
	rev RevivalScoreService,
) ImportService {
	return &importService{
		githubService: gh,
		aiService:     ai,
		projectRepo:   pRepo,
		githubRepo:    ghRepo,
		analysisRepo:  aRepo,
		revivalService: rev,
	}
}

func (s *importService) ImportFromGitHub(ctx context.Context, userID, repoURL string) (*models.Project, *models.ProjectAnalysis, error) {
	// 1. Parse URL
	owner, repoName, err := s.githubService.ParseURL(repoURL)
	if err != nil {
		return nil, nil, err
	}

	// 2. Fetch Data from GitHub
	ghData, err := s.githubService.FetchRepositoryData(ctx, owner, repoName)
	if err != nil {
		return nil, nil, err
	}

	// 3. Save to GitHub Collection (Upsert)
	err = s.githubRepo.Upsert(ctx, ghData)
	if err != nil {
		return nil, nil, fmt.Errorf("failed to save github data: %w", err)
	}

	uID, _ := primitive.ObjectIDFromHex(userID)

	// 4. Check if project already exists for this GitHub ID
	filter := bson.M{"github_repository_id": ghData.GitHubRepositoryID}
	existingProjects, _, _ := s.projectRepo.List(ctx, filter, 1, 1)

	var project *models.Project
	if len(existingProjects) > 0 {
		project = &existingProjects[0]
	} else {
		// Create new project
		project = &models.Project{
			OwnerID:            uID,
			Name:               ghData.Name,
			Description:        ghData.Description,
			Status:             models.ProjectStatusAbandoned,
			GitHubURL:          ghData.HTMLURL,
			GitHubRepositoryID: ghData.GitHubRepositoryID,
			Technologies:       s.extractTechnologies(ghData.Languages),
			LastActivity:       ghData.LastActivityDate,
			CreatedAt:          time.Now(),
		}
		err = s.projectRepo.Create(ctx, project)
		if err != nil {
			return nil, nil, fmt.Errorf("failed to create project: %w", err)
		}
	}

	// 5. Trigger Advanced ScrapAI Analysis
	aiInput := ai.ProjectAnalysisInput{
		Name:         project.Name,
		Description:  project.Description,
		README:       ghData.README,
		Languages:    ghData.Languages,
		Technologies: project.Technologies,
		OpenIssues:   ghData.OpenIssues,
		Stars:        ghData.Stars,
		Activity:     fmt.Sprintf("Last active on %s", ghData.LastActivityDate.Format("2006-01-02")),
		Contributors: 1, // Placeholder, usually would fetch from GH
	}

	analysis, err := s.aiService.AnalyzeProject(ctx, aiInput)
	if err != nil {
		return nil, nil, fmt.Errorf("ai analysis failed: %w", err)
	}

	analysis.ProjectID = project.ID

	// Save or Update Analysis
	existingAnalysis, _ := s.analysisRepo.GetByProjectID(ctx, project.ID)
	if existingAnalysis != nil {
		analysis.ID = existingAnalysis.ID
		err = s.analysisRepo.Update(ctx, analysis)
	} else {
		err = s.analysisRepo.Create(ctx, analysis)
	}
	if err != nil {
		return nil, nil, fmt.Errorf("failed to save analysis: %w", err)
	}

	// 6. Calculate Deterministic Revival Score
	revivalResult, _ := s.revivalService.CalculateScore(ctx, project)

	// 7. Update Project with Scores and Analysis ID
	project.RevivalScore = revivalResult.Score
	project.QualityScore = analysis.QualityScore
	project.AnalysisID = analysis.ID

	var skills []string
	for _, sk := range analysis.RequiredSkills {
		skills = append(skills, sk.Name)
	}
	project.RequiredSkills = s.mapToProjectRequiredSkills(analysis.RequiredSkills)

	err = s.projectRepo.Update(ctx, project)
	if err != nil {
		return nil, nil, fmt.Errorf("failed to update project: %w", err)
	}

	return project, analysis, nil
}

func (s *importService) extractTechnologies(languages map[string]int) []string {
	var techs []string
	for lang := range languages {
		techs = append(techs, lang)
	}
	return techs
}

func (s *importService) mapToProjectRequiredSkills(skills []models.SkillRequirement) []models.RequiredSkill {
	var reqSkills []models.RequiredSkill
	for _, sk := range skills {
		reqSkills = append(reqSkills, models.RequiredSkill{
			Name:       sk.Name,
			Importance: sk.Importance,
		})
	}
	return reqSkills
}
