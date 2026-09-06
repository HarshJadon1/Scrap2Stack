package services

import (
	"context"
	"sort"
	"strings"

	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/repositories"
)

type MatchResult struct {
	Developer        models.User    `json:"developer"`
	MatchPercentage  int            `json:"matchPercentage"`
	Label            string         `json:"label"`
	MatchedSkills    []string       `json:"matchedSkills"`
	PartialSkills    []string       `json:"partialSkills"`
	MissingSkills    []string       `json:"missingSkills"`
	MatchedInterests []string       `json:"matchedInterests"`
	Reasons          []string       `json:"reasons"`
	ScoreBreakdown   map[string]int `json:"scoreBreakdown"`
}

type ProjectRecommendation struct {
	Project         models.Project `json:"project"`
	MatchPercentage int            `json:"matchPercentage"`
	Why             string         `json:"why"`
	RequiredSkills  []string       `json:"requiredSkills"`
	RevivalScore    int            `json:"revivalScore"`
	ScoreBreakdown  map[string]int `json:"scoreBreakdown"`
}

type MatchingService interface {
	GetMatchesForProject(ctx context.Context, projectID string, page, limit int) ([]MatchResult, error)
	GetRecommendedProjects(ctx context.Context, userID string, page, limit int) ([]ProjectRecommendation, error)
	CalculateScore(project *models.Project, dev *models.User) (int, map[string]int, []string, []string, []string)
}

type matchingService struct {
	userRepo    repositories.UserRepository
	projectRepo repositories.ProjectRepository
	teamRepo    repositories.TeamRepository
}

func NewMatchingService(
	userRepo repositories.UserRepository,
	projectRepo repositories.ProjectRepository,
	teamRepo repositories.TeamRepository,
) MatchingService {
	return &matchingService{
		userRepo:    userRepo,
		projectRepo: projectRepo,
		teamRepo:    teamRepo,
	}
}

func (s *matchingService) GetMatchesForProject(ctx context.Context, projectID string, page, limit int) ([]MatchResult, error) {
	project, err := s.projectRepo.GetByID(ctx, repositories.ToObjectID(projectID))
	if err != nil {
		return nil, err
	}

	users, err := s.userRepo.ListAll(ctx)
	if err != nil {
		return nil, err
	}

	team, _ := s.teamRepo.GetByProjectID(ctx, project.ID)
	var members []models.TeamMember
	if team != nil {
		members, _ = s.teamRepo.GetMembers(ctx, team.ID)
	}

	isMember := func(userID string) bool {
		if userID == project.OwnerID.Hex() {
			return true
		}
		for _, m := range members {
			if m.UserID.Hex() == userID {
				return true
			}
		}
		return false
	}

	var candidates []models.User
	for _, u := range users {
		if !isMember(u.ID.Hex()) {
			candidates = append(candidates, u)
		}
	}

	var results []MatchResult
	for _, dev := range candidates {
		score, breakdown, reasons, matched, missing := s.CalculateScore(project, &dev)

		results = append(results, MatchResult{
			Developer:       dev,
			MatchPercentage: score,
			Label:           s.getLabel(score),
			MatchedSkills:   matched,
			MissingSkills:   missing,
			Reasons:         reasons,
			ScoreBreakdown:  breakdown,
		})
	}

	sort.Slice(results, func(i, j int) bool {
		return results[i].MatchPercentage > results[j].MatchPercentage
	})

	return s.paginateResults(results, page, limit), nil
}

func (s *matchingService) GetRecommendedProjects(ctx context.Context, userID string, page, limit int) ([]ProjectRecommendation, error) {
	user, err := s.userRepo.GetByID(ctx, repositories.ToObjectID(userID))
	if err != nil {
		return nil, err
	}

	projects, _, err := s.projectRepo.List(ctx, nil, 1, 1000)
	if err != nil {
		return nil, err
	}

	var recommendations []ProjectRecommendation
	for _, project := range projects {
		if project.OwnerID.Hex() == userID { continue }

		team, _ := s.teamRepo.GetByProjectID(ctx, project.ID)
		if team != nil {
			members, _ := s.teamRepo.GetMembers(ctx, team.ID)
			alreadyIn := false
			for _, m := range members {
				if m.UserID.Hex() == userID { alreadyIn = true; break }
			}
			if alreadyIn { continue }
		}

		score, breakdown, _, matched, _ := s.CalculateScore(&project, user)

		if score > 40 {
			var skillsList []string
			for _, rs := range project.RequiredSkills {
				skillsList = append(skillsList, rs.Name)
			}

			why := "Recommended because of your skill alignment."
			if len(matched) > 0 {
				why = "Recommended because your " + strings.Join(matched, " and ") + " skills match this project's requirements."
			}

			recommendations = append(recommendations, ProjectRecommendation{
				Project:         project,
				MatchPercentage: score,
				Why:             why,
				RequiredSkills:  skillsList,
				RevivalScore:    project.RevivalScore,
				ScoreBreakdown:  breakdown,
			})
		}
	}

	sort.Slice(recommendations, func(i, j int) bool {
		return recommendations[i].MatchPercentage > recommendations[j].MatchPercentage
	})

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

func (s *matchingService) CalculateScore(project *models.Project, dev *models.User) (int, map[string]int, []string, []string, []string) {
	weights := map[string]float64{
		"skills":      0.40,
		"proficiency": 0.15,
		"tech":         0.15,
		"experience":  0.10,
		"interests":   0.05,
		"category":    0.05,
		"contrib":     0.05,
		"avail":       0.05,
	}

	breakdown := make(map[string]int)
	reasons := []string{}
	var totalScore float64

	skillScore, matched, _, missing := s.scoreSkills(project.RequiredSkills, dev.Skills)
	breakdown["skills"] = int(skillScore)
	totalScore += skillScore * weights["skills"]
	if skillScore >= 80 {
		reasons = append(reasons, "Strong technical skill alignment.")
	}

	profScore := s.scoreProficiency(project.RequiredSkills, dev.Skills)
	breakdown["proficiency"] = int(profScore)
	totalScore += profScore * weights["proficiency"]

	techScore := s.scoreTechStack(project.Technologies, dev.Skills)
	breakdown["tech"] = int(techScore)
	totalScore += techScore * weights["tech"]

	expScore := s.scoreExperience(project.Difficulty, dev.ExperienceLevel)
	breakdown["experience"] = int(expScore)
	totalScore += expScore * weights["experience"]

	interestScore, _ := s.scoreInterests(project.Category, dev.Interests)
	breakdown["interests"] = int(interestScore)
	totalScore += interestScore * weights["interests"]

	catScore := s.scoreCategory(project.Category, dev.ProjectCategories)
	breakdown["category"] = int(catScore)
	totalScore += catScore * weights["category"]

	contribScore := float64(dev.CompletedProjects*10 + dev.MergedPRs*2 + dev.CompletedTasks)
	if contribScore > 100 { contribScore = 100 }
	breakdown["contrib"] = int(contribScore)
	totalScore += contribScore * weights["contrib"]

	availScore := 0.0
	if dev.Availability == models.AvailabilityAvailable {
		availScore = 100
	} else if dev.Availability == models.AvailabilityPartiallyAvailable {
		availScore = 50
	}
	breakdown["availability"] = int(availScore)
	totalScore += availScore * weights["avail"]

	return int(totalScore), breakdown, reasons, matched, missing
}

func (s *matchingService) scoreSkills(required []models.RequiredSkill, devSkills []models.UserSkill) (float64, []string, []string, []string) {
	if len(required) == 0 { return 100, nil, nil, nil }
	matched := []string{}; missing := []string{}
	foundCount := 0
	for _, req := range required {
		found := false
		for _, ds := range devSkills {
			if strings.EqualFold(req.Name, ds.Name) {
				found = true; matched = append(matched, req.Name); break
			}
		}
		if !found { missing = append(missing, req.Name) } else { foundCount++ }
	}
	return (float64(foundCount) / float64(len(required))) * 100, matched, nil, missing
}

func (s *matchingService) scoreProficiency(required []models.RequiredSkill, devSkills []models.UserSkill) float64 {
	if len(required) == 0 { return 100 }
	var total float64; matchCount := 0
	levelMap := map[models.ProficiencyLevel]int{
		models.ProficiencyBeginner: 1, models.ProficiencyIntermediate: 2, models.ProficiencyAdvanced: 3, models.ProficiencyExpert: 4,
	}
	for _, req := range required {
		for _, ds := range devSkills {
			if strings.EqualFold(req.Name, ds.Name) {
				matchCount++; reqVal := levelMap[req.RequiredLevel]; devVal := levelMap[ds.Level]
				if devVal >= reqVal { total += 100 } else { total += (float64(devVal) / float64(reqVal)) * 100 }
				break
			}
		}
	}
	if matchCount == 0 { return 0 }
	return total / float64(len(required))
}

func (s *matchingService) scoreTechStack(techs []string, devSkills []models.UserSkill) float64 {
	if len(techs) == 0 { return 100 }
	matchCount := 0
	for _, t := range techs {
		for _, ds := range devSkills {
			if strings.EqualFold(t, ds.Name) { matchCount++; break }
		}
	}
	return (float64(matchCount) / float64(len(techs))) * 100
}

func (s *matchingService) scoreExperience(diff models.Difficulty, devLevel models.ProficiencyLevel) float64 {
	diffVal := 1
	if diff == models.DifficultyIntermediate { diffVal = 2 }
	if diff == models.DifficultyAdvanced { diffVal = 3 }
	levelMap := map[models.ProficiencyLevel]int{
		models.ProficiencyBeginner: 1, models.ProficiencyIntermediate: 2, models.ProficiencyAdvanced: 3, models.ProficiencyExpert: 4,
	}
	devVal := levelMap[devLevel]
	if devVal >= diffVal { return 100 }
	return (float64(devVal) / float64(diffVal)) * 100
}

func (s *matchingService) scoreInterests(category string, interests []string) (float64, []string) {
	matched := []string{}
	for _, i := range interests {
		if strings.EqualFold(i, category) { matched = append(matched, i) }
	}
	if len(matched) > 0 { return 100, matched }
	return 0, nil
}

func (s *matchingService) scoreCategory(category string, preferred []string) float64 {
	for _, p := range preferred {
		if strings.EqualFold(p, category) { return 100 }
	}
	return 0
}

func (s *matchingService) getLabel(score int) string {
	if score >= 90 { return "EXCELLENT MATCH" }
	if score >= 80 { return "STRONG MATCH" }
	if score >= 70 { return "GOOD MATCH" }
	if score >= 60 { return "POTENTIAL MATCH" }
	return "LOW MATCH"
}

func (s *matchingService) paginateResults(results []MatchResult, page, limit int) []MatchResult {
	start := (page - 1) * limit
	if start >= len(results) {
		return []MatchResult{}
	}
	end := start + limit
	if end > len(results) {
		end = len(results)
	}
	return results[start:end]
}
