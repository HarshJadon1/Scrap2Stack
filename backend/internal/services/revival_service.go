package services

import (
	"context"
	"time"

	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/repositories"
)

type RevivalScoreResult struct {
	Score       int                  `json:"score"`
	Label       string               `json:"label"`
	Factors     []RevivalScoreFactor `json:"factors"`
	Explanation string               `json:"explanation"`
}

type RevivalScoreFactor struct {
	Name  string `json:"name"`
	Score int    `json:"score"`
}

type RevivalScoreService interface {
	CalculateScore(ctx context.Context, project *models.Project) (*RevivalScoreResult, error)
}

type revivalScoreService struct {
	githubRepo repositories.GitHubRepository
}

func NewRevivalScoreService(ghRepo repositories.GitHubRepository) RevivalScoreService {
	return &revivalScoreService{
		githubRepo: ghRepo,
	}
}

func (s *revivalScoreService) CalculateScore(ctx context.Context, project *models.Project) (*RevivalScoreResult, error) {
	// Default result if no GitHub data
	res := &RevivalScoreResult{
		Score:   50,
		Label:   "MODERATE",
		Factors: []RevivalScoreFactor{},
	}

	ghData, _ := s.githubRepo.GetByGitHubID(ctx, project.GitHubRepositoryID)
	if ghData == nil {
		res.Explanation = "No GitHub data available for deep scoring."
		return res, nil
	}

	var totalScore int
	var factors []RevivalScoreFactor

	// 1. Inactivity Score (30%)
	inactivityFactor := s.calculateInactivityScore(ghData.LastActivityDate)
	factors = append(factors, RevivalScoreFactor{Name: "Repository Activity", Score: inactivityFactor})
	totalScore += int(float64(inactivityFactor) * 0.3)

	// 2. Documentation Score (20%)
	docFactor := 0
	if len(ghData.README) > 500 {
		docFactor = 100
	} else if len(ghData.README) > 100 {
		docFactor = 60
	}
	factors = append(factors, RevivalScoreFactor{Name: "Documentation", Score: docFactor})
	totalScore += int(float64(docFactor) * 0.2)

	// 3. Technical Feasibility (Technology Relevance) (25%)
	techFactor := 70
	if _, ok := ghData.Languages["Go"]; ok || ghData.Languages["Kotlin"] != 0 {
		techFactor = 95
	}
	factors = append(factors, RevivalScoreFactor{Name: "Technology Relevance", Score: techFactor})
	totalScore += int(float64(techFactor) * 0.25)

	// 4. Contribution Potential (Stars/Forks) (25%)
	popularity := (ghData.Stars * 2) + ghData.Forks
	popFactor := 40
	if popularity > 100 { popFactor = 100 } else if popularity > 20 { popFactor = 80 }
	factors = append(factors, RevivalScoreFactor{Name: "Contribution Potential", Score: popFactor})
	totalScore += int(float64(popFactor) * 0.25)

	res.Score = totalScore
	res.Factors = factors
	res.Label = s.getLabel(totalScore)
	res.Explanation = s.generateExplanation(ghData)

	return res, nil
}

func (s *revivalScoreService) calculateInactivityScore(lastActivity time.Time) int {
	daysInactive := int(time.Since(lastActivity).Hours() / 24)
	if daysInactive < 30 { return 100 }
	if daysInactive < 180 { return 80 }
	if daysInactive < 365 { return 50 }
	return 20
}

func (s *revivalScoreService) getLabel(score int) string {
	if score > 80 { return "EXCELLENT" }
	if score > 60 { return "HIGH" }
	if score > 30 { return "MODERATE" }
	return "LOW"
}

func (s *revivalScoreService) generateExplanation(gh *models.GitHubRepository) string {
	if time.Since(gh.LastActivityDate).Hours() > 24*365 {
		return "This project has been inactive for over a year but contains significant codebase history."
	}
	return "The project uses modern technologies and has a high chance of successful revival."
}
