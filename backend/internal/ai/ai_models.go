package ai

type ProjectAnalysisInput struct {
	Name         string         `json:"name"`
	Description  string         `json:"description"`
	README       string         `json:"readme"`
	Languages    map[string]int `json:"languages"`
	Technologies []string       `json:"technologies"`
	OpenIssues   int            `json:"openIssues"`
	Stars        int            `json:"stars"`
	Activity     string         `json:"activity"`
	Contributors int            `json:"contributors"`
}

type ProjectAnalysisOutput struct {
	Summary               string               `json:"summary"`
	ProblemStatement      string               `json:"problemStatement"`
	ProjectState          string               `json:"projectState"`
	DetectedTechnologies  []DetectedTechnology `json:"detectedTechnologies"`
	RequiredSkills        []RequiredSkill      `json:"requiredSkills"`
	MissingSkills         []string             `json:"missingSkills"`
	TechnicalRisks        []RiskFactor         `json:"technicalRisks"`
	QualityScore          int                  `json:"qualityScore"`
	RevivalScore          int                  `json:"revivalScore"`
	RevivalRecommendation string               `json:"revivalRecommendation"` // REVIVE_NOW, REVIVE_WITH_CAUTION, NEEDS_REWORK, NOT_RECOMMENDED
	Explanation           string               `json:"explanation"`
	Complexity            string               `json:"complexity"`
	EstimatedEffort       string               `json:"estimatedEffort"`
	RecommendedTeamSize   int                  `json:"recommendedTeamSize"`
	RecommendedRoles      []string             `json:"recommendedRoles"`
	Roadmap               []RoadmapPhase       `json:"roadmap"`
	NextSteps             []string             `json:"nextSteps"`
}

type DetectedTechnology struct {
	Name       string  `json:"name"`
	Confidence float64 `json:"confidence"`
}

type RequiredSkill struct {
	Name       string  `json:"name"`
	Level      string  `json:"level"`
	Importance string  `json:"importance"`
	Confidence float64 `json:"confidence"`
	Why        string  `json:"why"`
}

type RiskFactor struct {
	Risk        string `json:"risk"`
	Severity    string `json:"severity"`
	Explanation string `json:"explanation"`
}

type RoadmapPhase struct {
	Phase           int      `json:"phase"`
	Title           string   `json:"title"`
	Description     string   `json:"description"`
	RequiredSkills  []string `json:"requiredSkills"`
	EstimatedEffort string   `json:"estimatedEffort"`
	Priority        string   `json:"priority"`
}
