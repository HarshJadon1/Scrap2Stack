package services

import (
	"context"
	"errors"
	"sort"
	"time"

	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/repositories"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type LeaderboardEntry struct {
	Developer             models.User `json:"developer"`
	Role                  string      `json:"role"`
	VerifiedContributions int         `json:"verifiedContributions"`
	Charms                int         `json:"charms"`
}

type TeamService interface {
	CreateTeam(ctx context.Context, ownerID, projectID, name string) (*models.Team, error)
	GetTeam(ctx context.Context, id string) (*models.Team, error)
	GetTeamByProject(ctx context.Context, projectID string) (*models.Team, error)
	AddMember(ctx context.Context, teamID, userID string, role models.TeamRole) error
	GetMembers(ctx context.Context, teamID string) ([]models.TeamMember, error)
	ChangeMemberRole(ctx context.Context, leadID, teamID, userID string, newRole models.TeamRole) error
	RemoveMember(ctx context.Context, ownerID, teamID, userID string) error
	CalculateTeamCoverage(ctx context.Context, projectID string) (map[string]int, error)
	GetProjectLeaderboard(ctx context.Context, projectID string) ([]LeaderboardEntry, error)
}

type teamService struct {
	teamRepo       repositories.TeamRepository
	projectRepo    repositories.ProjectRepository
	userRepo       repositories.UserRepository
	contributeRepo repositories.ContributionRepository
	charmRepo      repositories.CharmRepository
}

func NewTeamService(
	teamRepo repositories.TeamRepository,
	projectRepo repositories.ProjectRepository,
	userRepo repositories.UserRepository,
	contributeRepo repositories.ContributionRepository,
	charmRepo repositories.CharmRepository,
) TeamService {
	return &teamService{
		teamRepo:       teamRepo,
		projectRepo:    projectRepo,
		userRepo:       userRepo,
		contributeRepo: contributeRepo,
		charmRepo:      charmRepo,
	}
}

func (s *teamService) CreateTeam(ctx context.Context, ownerID, projectID, name string) (*models.Team, error) {
	oID, _ := primitive.ObjectIDFromHex(ownerID)
	pID, _ := primitive.ObjectIDFromHex(projectID)

	team := &models.Team{
		ProjectID:   pID,
		OwnerID:     oID,
		Name:        name,
		Status:      models.TeamStatusForming,
		MaxMembers:  5,
		CreatedAt:   time.Now(),
		UpdatedAt:   time.Now(),
	}

	if err := s.teamRepo.Create(ctx, team); err != nil {
		return nil, err
	}

	member := &models.TeamMember{
		TeamID:   team.ID,
		UserID:   oID,
		Role:     models.RoleProjectLead,
		Status:   models.MemberStatusActive,
		JoinedAt: time.Now(),
	}
	s.teamRepo.AddMember(ctx, member)

	return team, nil
}

func (s *teamService) GetTeam(ctx context.Context, id string) (*models.Team, error) {
	tID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, err
	}
	return s.teamRepo.GetByID(ctx, tID)
}

func (s *teamService) GetTeamByProject(ctx context.Context, projectID string) (*models.Team, error) {
	pID, err := primitive.ObjectIDFromHex(projectID)
	if err != nil {
		return nil, err
	}
	return s.teamRepo.GetByProjectID(ctx, pID)
}

func (s *teamService) AddMember(ctx context.Context, teamID, userID string, role models.TeamRole) error {
	tID, _ := primitive.ObjectIDFromHex(teamID)
	uID, _ := primitive.ObjectIDFromHex(userID)

	team, err := s.teamRepo.GetByID(ctx, tID)
	if err != nil {
		return err
	}

	members, _ := s.teamRepo.GetMembers(ctx, tID)
	if len(members) >= team.MaxMembers {
		return errors.New("team capacity reached")
	}

	for _, m := range members {
		if m.UserID == uID {
			return errors.New("user already a member")
		}
	}

	member := &models.TeamMember{
		TeamID:   tID,
		UserID:   uID,
		Role:     role,
		Status:   models.MemberStatusActive,
		JoinedAt: time.Now(),
	}

	return s.teamRepo.AddMember(ctx, member)
}

func (s *teamService) GetMembers(ctx context.Context, teamID string) ([]models.TeamMember, error) {
	tID, _ := primitive.ObjectIDFromHex(teamID)
	return s.teamRepo.GetMembers(ctx, tID)
}

func (s *teamService) ChangeMemberRole(ctx context.Context, leadID, teamID, userID string, newRole models.TeamRole) error {
	tID, _ := primitive.ObjectIDFromHex(teamID)
	uID, _ := primitive.ObjectIDFromHex(userID)

	team, err := s.teamRepo.GetByID(ctx, tID)
	if err != nil {
		return err
	}

	if team.OwnerID.Hex() != leadID {
		return errors.New("only project lead can change roles")
	}

	// This assumes we have an UpdateMember method in the repository.
	// For Phase 6, we'll keep it as a placeholder or implement if repo updated.
	return nil
}

func (s *teamService) RemoveMember(ctx context.Context, ownerID, teamID, userID string) error {
	tID, _ := primitive.ObjectIDFromHex(teamID)
	uID, _ := primitive.ObjectIDFromHex(userID)

	team, err := s.teamRepo.GetByID(ctx, tID)
	if err != nil {
		return err
	}

	if team.OwnerID.Hex() != ownerID && userID != ownerID {
		return errors.New("unauthorized")
	}

	if team.OwnerID == uID {
		return errors.New("project lead cannot leave without transferring ownership")
	}

	return s.teamRepo.RemoveMember(ctx, tID, uID)
}

func (s *teamService) CalculateTeamCoverage(ctx context.Context, projectID string) (map[string]int, error) {
	pID, _ := primitive.ObjectIDFromHex(projectID)
	project, err := s.projectRepo.GetByID(ctx, pID)
	if err != nil {
		return nil, err
	}

	team, err := s.teamRepo.GetByProjectID(ctx, pID)
	if err != nil || team == nil {
		return nil, errors.New("team not found")
	}

	members, _ := s.teamRepo.GetMembers(ctx, team.ID)

	coverage := make(map[string]int)
	totalRoles := len(project.RequiredRoles)
	if totalRoles == 0 { return coverage, nil }

	filled := 0
	for _, req := range project.RequiredRoles {
		count := 0
		for _, m := range members {
			if m.Role == req.Role {
				count++
			}
		}
		if count >= req.Count {
			coverage[string(req.Role)] = 100
			filled++
		} else {
			coverage[string(req.Role)] = (count * 100) / req.Count
		}
	}

	coverage["OVERALL"] = (filled * 100) / totalRoles
	return coverage, nil
}

func (s *teamService) GetProjectLeaderboard(ctx context.Context, projectID string) ([]LeaderboardEntry, error) {
	pID, _ := primitive.ObjectIDFromHex(projectID)
	team, err := s.teamRepo.GetByProjectID(ctx, pID)
	if err != nil || team == nil {
		return nil, errors.New("team not found")
	}

	members, _ := s.teamRepo.GetMembers(ctx, team.ID)
	var leaderboard []LeaderboardEntry

	for _, m := range members {
		user, _ := s.userRepo.GetByID(ctx, m.UserID)
		if user == nil { continue }

		// Fetch project-specific contributions
		contributions, _ := s.contributeRepo.ListByProjectID(ctx, pID)
		verifiedCount := 0
		charms := 0
		for _, c := range contributions {
			if c.UserID == m.UserID && c.Verified {
				verifiedCount++
			}
		}

		leaderboard = append(leaderboard, LeaderboardEntry{
			Developer:             *user,
			Role:                  string(m.Role),
			VerifiedContributions: verifiedCount,
			Charms:                charms, // Would be calculated from transactions
		})
	}

	sort.Slice(leaderboard, func(i, j int) bool {
		return leaderboard[i].VerifiedContributions > leaderboard[j].VerifiedContributions
	})

	return leaderboard, nil
}
