package ai

import (
	"context"
	"encoding/json"
	"fmt"
	"strings"
)

type AIAnalyzer interface {
	AnalyzeProject(ctx context.Context, input ProjectAnalysisInput) (*ProjectAnalysisOutput, error)
}

type aiService struct {
	provider AIProvider
}

func NewAIService(provider AIProvider) AIAnalyzer {
	return &aiService{
		provider: provider,
	}
}

func (s *aiService) AnalyzeProject(ctx context.Context, input ProjectAnalysisInput) (*ProjectAnalysisOutput, error) {
	// 1. Generate Prompt
	prompt := s.generatePrompt(input)

	// 2. Call Provider
	rawResponse, err := s.provider.GenerateStructuredResponse(ctx, prompt)
	if err != nil {
		return nil, fmt.Errorf("ai provider error: %w", err)
	}

	// 3. Parse and Validate
	output, err := s.parseAndValidate(rawResponse)
	if err != nil {
		return nil, fmt.Errorf("failed to parse AI response: %w", err)
	}

	return output, nil
}

func (s *aiService) generatePrompt(input ProjectAnalysisInput) string {
	var sb strings.Builder
	sb.WriteString("You are an experienced software project analyst specializing in evaluating incomplete and abandoned software projects.\n")
	sb.WriteString(fmt.Sprintf("Analyze the project: %s\n", input.Name))
	sb.WriteString(fmt.Sprintf("Description: %s\n", input.Description))
	if input.README != "" {
		sb.WriteString(fmt.Sprintf("README Snippet: %s\n", input.README))
	}
	sb.WriteString(fmt.Sprintf("Languages: %v\n", input.Languages))
	sb.WriteString("Provide a detailed analysis in JSON format including summary, problem statement, project state, technologies, required skills, missing skills, quality score (0-100), revival potential (0-100), complexity, estimated effort, risks, recommended team size, roles, roadmap, and next steps.")
	return sb.String()
}

func (s *aiService) parseAndValidate(rawResponse string) (*ProjectAnalysisOutput, error) {
	var output ProjectAnalysisOutput
	if err := json.Unmarshal([]byte(rawResponse), &output); err != nil {
		return nil, err
	}

	// Basic validation
	if output.QualityScore < 0 { output.QualityScore = 0 }
	if output.QualityScore > 100 { output.QualityScore = 100 }
	if output.RevivalPotential < 0 { output.RevivalPotential = 0 }
	if output.RevivalPotential > 100 { output.RevivalPotential = 100 }

	return &output, nil
}
