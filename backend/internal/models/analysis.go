package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type AnalysisStatus string

const (
	AnalysisStatusNotAnalyzed AnalysisStatus = "NOT_ANALYZED"
	AnalysisStatusAnalyzing   AnalysisStatus = "ANALYZING"
	AnalysisStatusCompleted   AnalysisStatus = "COMPLETED"
	AnalysisStatusFailed      AnalysisStatus = "FAILED"
)

type RevivalRecommendation string

const (
	ReviveNow         RevivalRecommendation = "REVIVE_NOW"
	ReviveWithCaution RevivalRecommendation = "REVIVE_WITH_CAUTION"
	NeedsRework       RevivalRecommendation = "NEEDS_REWORK"
	NotRecommended    RevivalRecommendation = "NOT_RECOMMENDED"
)

type ProjectAnalysis struct {
	ID                    primitive.ObjectID    `bson:"_id,omitempty" json:"id"`
	ProjectID             primitive.ObjectID    `bson:"project_id" json:"project_id"`
	Status                AnalysisStatus        `bson:"status" json:"status"`
	ErrorCode             string                `bson:"error_code,omitempty" json:"errorCode,omitempty"`
	Summary               string                `bson:"summary" json:"summary"`
	ProblemStatement      string                `bson:"problem_statement" json:"problemStatement"`
	ProjectState          string                `bson:"project_state" json:"projectState"`
	DetectedTechnologies  []TechnologyMatch     `bson:"detected_technologies" json:"detected_technologies"`
	RequiredSkills        []SkillRequirement    `bson:"required_skills" json:"required_skills"`
	MissingSkills         []string              `bson:"missing_skills" json:"missing_skills"`
	QualityScore          int                   `bson:"quality_score" json:"qualityScore"`
	QualityFactors        []ScoreFactor         `bson:"quality_factors" json:"qualityFactors"`
	RevivalScore          int                   `bson:"revival_score" json:"revivalScore"`
	RevivalRecommendation RevivalRecommendation `bson:"revival_recommendation" json:"revivalRecommendation"`
	Explanation           string                `bson:"explanation" json:"explanation"`
	Complexity            string                `bson:"complexity" json:"complexity"`
	EstimatedEffort       string                `bson:"estimated_effort" json:"estimated_effort"`
	Risks                 []RiskFactor          `bson:"risks" json:"risks"`
	RecommendedTeamSize   int                   `bson:"recommended_team_size" json:"recommendedTeamSize"`
	RecommendedRoles      []string              `bson:"recommended_roles" json:"recommendedRoles"`
	Roadmap               []RoadmapStep         `bson:"roadmap" json:"roadmap"`
	NextSteps             []string              `bson:"next_steps" json:"nextSteps"`
	CreatedAt             time.Time             `bson:"created_at" json:"createdAt"`
	UpdatedAt             time.Time             `bson:"updated_at" json:"updatedAt"`
}

type TechnologyMatch struct {
	Name       string  `bson:"name" json:"name"`
	Confidence float64 `bson:"confidence" json:"confidence"`
}

type SkillRequirement struct {
	Name       string  `bson:"name" json:"name"`
	Confidence float64 `bson:"confidence" json:"confidence"`
	Importance string  `bson:"importance" json:"importance"`
	Why        string  `bson:"why" json:"why"`
}

type RiskFactor struct {
	Risk        string `bson:"risk" json:"risk"`
	Severity    string `bson:"severity" json:"severity"`
	Explanation string `bson:"explanation" json:"explanation"`
}

type ScoreFactor struct {
	Name        string `bson:"name" json:"name"`
	Score       int    `bson:"score" json:"score"`
	Explanation string `bson:"explanation" json:"explanation"`
}

type RoadmapStep struct {
	Phase           int      `bson:"phase" json:"phase"`
	Title           string   `bson:"title" json:"title"`
	Description     string   `bson:"description" json:"description"`
	RequiredSkills  []string `bson:"required_skills" json:"requiredSkills"`
	EstimatedEffort string   `bson:"estimated_effort" json:"estimatedEffort"`
	Priority        string   `bson:"priority" json:"priority"`
}
