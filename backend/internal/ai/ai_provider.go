package ai

import (
	"context"
)

type AIProvider interface {
	GenerateStructuredResponse(ctx context.Context, prompt string) (string, error)
}

type mockAIProvider struct{}

func NewMockAIProvider() AIProvider {
	return &mockAIProvider{}
}

func (p *mockAIProvider) GenerateStructuredResponse(ctx context.Context, prompt string) (string, error) {
	// Mock response that matches updated ProjectAnalysisOutput structure
	return `{
		"summary": "An intelligent technical solution for crop health monitoring using mobile vision and machine learning. This project aims to empower small-scale farmers with enterprise-grade diagnostic tools.",
		"problemStatement": "Farmers lack accessible tools for early disease detection, leading to significant crop loss. Existing solutions are either too expensive or require constant internet connectivity, which is not feasible in rural areas.",
		"projectState": "ABANDONED",
		"detectedTechnologies": [
			{"name": "Python", "confidence": 0.98},
			{"name": "TensorFlow Lite", "confidence": 0.95},
			{"name": "Kotlin", "confidence": 0.90},
			{"name": "MongoDB", "confidence": 0.85},
			{"name": "FastAPI", "confidence": 0.88}
		],
		"requiredSkills": [
			{"name": "Machine Learning", "level": "ADVANCED", "importance": "CRITICAL", "confidence": 0.96, "why": "The core value proposition relies on accurate image classification of crop diseases."},
			{"name": "Mobile Development", "level": "INTERMEDIATE", "importance": "HIGH", "confidence": 0.92, "why": "The solution must run efficiently on low-mid range Android devices."},
			{"name": "Backend Engineering", "level": "INTERMEDIATE", "importance": "MEDIUM", "confidence": 0.89, "why": "Needed for data synchronization and community features."}
		],
		"missingSkills": ["Cloud Infrastructure", "Product Design"],
		"technicalRisks": [
			{"risk": "Model Drift", "severity": "MEDIUM", "explanation": "The original ML model was trained on a dataset from 2021 and may not recognize new variants of pests."},
			{"risk": "Legacy Android APIs", "severity": "HIGH", "explanation": "The app uses deprecated Camera APIs that will fail on modern Android versions."}
		],
		"qualityScore": 72,
		"revivalScore": 85,
		"revivalRecommendation": "REVIVE_NOW",
		"explanation": "High revival potential because the repository contains a functional (though aged) ML model, clear problem-solution fit, and uses a technology stack that is still highly relevant. The social impact for farmers justifies the effort to modernize the infrastructure.",
		"complexity": "MEDIUM",
		"estimatedEffort": "4-6 weeks",
		"recommendedTeamSize": 3,
		"recommendedRoles": ["ML Engineer", "Android Developer", "Full Stack Developer"],
		"roadmap": [
			{
				"phase": 1,
				"title": "Modernization & Infrastructure",
				"description": "Upgrade Android project to latest Gradle and Compose versions. Replace deprecated Camera APIs.",
				"requiredSkills": ["Kotlin", "Android SDK"],
				"estimatedEffort": "1 week",
				"priority": "CRITICAL"
			},
			{
				"phase": 2,
				"title": "ML Model Optimization",
				"description": "Quantize the existing TensorFlow model for better mobile performance and re-validate against fresh data.",
				"requiredSkills": ["Python", "TensorFlow Lite"],
				"estimatedEffort": "2 weeks",
				"priority": "HIGH"
			},
			{
				"phase": 3,
				"title": "MVP Launch",
				"description": "Implement basic disease reporting and offline sync capability.",
				"requiredSkills": ["Go", "Kotlin"],
				"estimatedEffort": "2 weeks",
				"priority": "MEDIUM"
			}
		],
		"nextSteps": [
			"Audit existing ML model performance on modern hardware",
			"Setup CI/CD pipeline for the mobile application",
			"Draft UI wireframes for the new diagnostic flow"
		]
	}`, nil
}
