package services

import (
	"context"
	"fmt"

	"github.com/scrap2stack/backend/internal/ai"
	"github.com/scrap2stack/backend/internal/models"
)

type AIAnalyzer interface {
	AnalyzeProject(ctx context.Context, input ai.ProjectAnalysisInput) (*models.ProjectAnalysis, error)
	GenerateRoadmap(ctx context.Context, project *models.Project) ([]models.RoadmapStep, error)
}

type aiAnalyzer struct {
	aiProvider ai.AIProvider
}

func NewAIAnalyzer(provider ai.AIProvider) AIAnalyzer {
	return &aiAnalyzer{
		aiProvider: provider,
	}
}

func (s *aiAnalyzer) AnalyzeProject(ctx context.Context, input ai.ProjectAnalysisInput) (*models.ProjectAnalysis, error) {
	// 1. Generate Prompt
	prompt := s.buildAnalysisPrompt(input)

	// 2. Call AI Provider
	resp, err := s.aiProvider.GenerateStructuredResponse(ctx, prompt)
	if err != nil {
		return nil, fmt.Errorf("AI generation failed: %w", err)
	}

	// 3. Parse and Map to Domain Model
	var output ai.ProjectAnalysisOutput
	if err := s.parseAIResponse(resp, &output); err != nil {
		return nil, fmt.Errorf("failed to parse AI response: %w", err)
	}

	return s.mapOutputToModel(&output), nil
}

func (s *aiAnalyzer) GenerateRoadmap(ctx context.Context, project *models.Project) ([]models.RoadmapStep, error) {
	// In Advanced ScrapAI, the roadmap is generated as part of AnalyzeProject
	// This remains as a fallback or specific refresh tool
	return nil, nil
}

func (s *aiAnalyzer) buildAnalysisPrompt(input ai.ProjectAnalysisInput) string {
	return fmt.Sprintf(`Analyze the following project for revival potential:
Name: %s
Description: %s
README: %s
Languages: %v
Issues: %d
Stars: %d
Activity: %s
Contributors: %d

Provide a detailed analysis including:
1. Summary and problem statement.
2. Current state (ACTIVE, ABANDONED, etc.).
3. Detected technologies with confidence scores.
4. Required skills with confidence and reasoning.
5. Technical risks and severity.
6. Quality and Revival scores (0-100).
7. A specific recommendation (REVIVE_NOW, REVIVE_WITH_CAUTION, NEEDS_REWORK, NOT_RECOMMENDED).
8. A clear explanation of the recommendation.
9. Estimated effort and recommended team roles.
10. A prioritized roadmap focusing on infrastructure and critical fixes first.`,
	input.Name, input.Description, input.README, input.Languages, input.OpenIssues, input.Stars, input.Activity, input.Contributors)
}

func (s *aiAnalyzer) parseAIResponse(resp string, output *ai.ProjectAnalysisOutput) error {
	// In real implementation, use json.Unmarshal
	// Mock implementation already returns JSON
	return nil // placeholder
}

func (s *aiAnalyzer) mapOutputToModel(o *ai.ProjectAnalysisOutput) *models.ProjectAnalysis {
	analysis := &models.ProjectAnalysis{
		Summary:               o.Summary,
		ProblemStatement:      o.ProblemStatement,
		ProjectState:          o.ProjectState,
		QualityScore:          o.QualityScore,
		RevivalScore:          o.RevivalScore,
		RevivalRecommendation: models.RevivalRecommendation(o.RevivalRecommendation),
		Explanation:           o.Explanation,
		Complexity:            o.Complexity,
		EstimatedEffort:       o.EstimatedEffort,
		RecommendedTeamSize:   o.RecommendedTeamSize,
		RecommendedRoles:      o.RecommendedRoles,
		NextSteps:             o.NextSteps,
	}

	for _, t := range o.DetectedTechnologies {
		analysis.DetectedTechnologies = append(analysis.DetectedTechnologies, models.TechnologyMatch{
			Name:       t.Name,
			Confidence: t.Confidence,
		})
	}

	for _, sk := range o.RequiredSkills {
		analysis.RequiredSkills = append(analysis.RequiredSkills, models.SkillRequirement{
			Name:       sk.Name,
			Confidence: sk.Confidence,
			Importance: sk.Importance,
			Why:        sk.Why,
		})
	}

	for _, r := range o.TechnicalRisks {
		analysis.Risks = append(analysis.Risks, models.RiskFactor{
			Risk:        r.Risk,
			Severity:    r.Severity,
			Explanation: r.Explanation,
		})
	}

	for _, step := range o.Roadmap {
		analysis.Roadmap = append(analysis.Roadmap, models.RoadmapStep{
			Phase:           step.Phase,
			Title:           step.Title,
			Description:     step.Description,
			RequiredSkills:  step.RequiredSkills,
			EstimatedEffort: step.EstimatedEffort,
			Priority:        step.Priority,
		})
	}

	return analysis
}
