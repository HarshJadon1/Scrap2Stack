package services

import (
	"context"
	"fmt"
	"time"

	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/repositories"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type CharmService interface {
	AwardCharms(ctx context.Context, userID, projectID string, action models.CharmAction, contributionID *primitive.ObjectID) error
	GetUserBalance(ctx context.Context, userID string) (int, error)
	GetTransactionHistory(ctx context.Context, userID string) ([]models.CharmTransaction, error)
}

type charmService struct {
	charmRepo repositories.CharmRepository
	userRepo  repositories.UserRepository
}

func NewCharmService(charmRepo repositories.CharmRepository, userRepo repositories.UserRepository) CharmService {
	return &charmService{
		charmRepo: charmRepo,
		userRepo:  userRepo,
	}
}

func (s *charmService) AwardCharms(ctx context.Context, userID, projectID string, action models.CharmAction, contributionID *primitive.ObjectID) error {
	uID, _ := primitive.ObjectIDFromHex(userID)
	pID, _ := primitive.ObjectIDFromHex(projectID)

	// Idempotency check: combine user, project, action, and contribution
	idempotencyKey := fmt.Sprintf("%s_%s_%s", userID, projectID, action)
	if contributionID != nil {
		idempotencyKey += "_" + contributionID.Hex()
	}

	existing, _ := s.charmRepo.GetByIdempotencyKey(ctx, idempotencyKey)
	if existing != nil {
		return nil // Already awarded
	}

	amount := s.getCharmAmount(action)
	transaction := &models.CharmTransaction{
		UserID:             uID,
		ProjectID:          pID,
		ContributionID:     contributionID,
		Action:             action,
		Charms:             amount,
		VerificationStatus: models.VerificationVerified,
		IdempotencyKey:     idempotencyKey,
		CreatedAt:          time.Now(),
	}

	err := s.charmRepo.CreateTransaction(ctx, transaction)
	if err != nil {
		return err
	}

	// Update User total balance
	user, err := s.userRepo.GetByID(ctx, uID)
	if err == nil {
		user.Charms += amount
		user.UpdatedAt = time.Now()
		s.userRepo.Update(ctx, user)
	}

	return nil
}

func (s *charmService) GetUserBalance(ctx context.Context, userID string) (int, error) {
	uID, _ := primitive.ObjectIDFromHex(userID)
	user, err := s.userRepo.GetByID(ctx, uID)
	if err != nil {
		return 0, err
	}
	return user.Charms, nil
}

func (s *charmService) GetTransactionHistory(ctx context.Context, userID string) ([]models.CharmTransaction, error) {
	uID, _ := primitive.ObjectIDFromHex(userID)
	return s.charmRepo.GetByUserID(ctx, uID)
}

func (s *charmService) getCharmAmount(action models.CharmAction) int {
	switch action {
	case models.ActionVerifiedCommit:
		return 5
	case models.ActionTaskCompleted:
		return 30
	case models.ActionVerifiedPullRequest:
		return 25
	case models.ActionMergedPullRequest:
		return 50
	case models.ActionIssueResolved:
		return 20
	case models.ActionMvpReleased:
		return 100
	case models.ActionProjectCompleted:
		return 200
	default:
		return 10
	}
}
