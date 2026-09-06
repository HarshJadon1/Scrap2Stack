package services

import (
	"context"

	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/repositories"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type UserService interface {
	GetMe(ctx context.Context, userID string) (*models.User, error)
	UpdateMe(ctx context.Context, userID string, updateData map[string]interface{}) (*models.User, error)
	GetUserByID(ctx context.Context, id string) (*models.User, error)
}

type userService struct {
	userRepo repositories.UserRepository
}

func NewUserService(userRepo repositories.UserRepository) UserService {
	return &userService{
		userRepo: userRepo,
	}
}

func (s *userService) GetMe(ctx context.Context, userID string) (*models.User, error) {
	objID, err := primitive.ObjectIDFromHex(userID)
	if err != nil {
		return nil, err
	}
	return s.userRepo.GetByID(ctx, objID)
}

func (s *userService) UpdateMe(ctx context.Context, userID string, updateData map[string]interface{}) (*models.User, error) {
	objID, err := primitive.ObjectIDFromHex(userID)
	if err != nil {
		return nil, err
	}

	user, err := s.userRepo.GetByID(ctx, objID)
	if err != nil {
		return nil, err
	}

	// Update fields selectively
	if val, ok := updateData["name"].(string); ok { user.Name = val }
	if val, ok := updateData["bio"].(string); ok { user.Bio = val }
	if val, ok := updateData["experience_level"].(string); ok { user.ExperienceLevel = val }
	if val, ok := updateData["github_username"].(string); ok { user.GitHubUsername = val }
	if val, ok := updateData["github_profile_url"].(string); ok { user.GitHubProfileURL = val }
	if val, ok := updateData["profile_image"].(string); ok { user.ProfileImage = val }

	if skills, ok := updateData["skills"].([]interface{}); ok {
		var sList []string
		for _, s := range skills {
			if str, ok := s.(string); ok { sList = append(sList, str) }
		}
		user.Skills = sList
	}

	if interests, ok := updateData["interests"].([]interface{}); ok {
		var iList []string
		for _, i := range interests {
			if str, ok := i.(string); ok { iList = append(iList, str) }
		}
		user.Interests = iList
	}

	if err := s.userRepo.Update(ctx, user); err != nil {
		return nil, err
	}

	return user, nil
}

func (s *userService) GetUserByID(ctx context.Context, id string) (*models.User, error) {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, err
	}
	return s.userRepo.GetByID(ctx, objID)
}
