package services

import (
	"context"
	"errors"
	"time"

	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/repositories"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type CollaborationService interface {
	CreateRequest(ctx context.Context, senderID, projectID, receiverID string, proposedRole models.TeamRole, message string) (*models.CollaborationRequest, error)
	GetReceivedRequests(ctx context.Context, userID string) ([]models.CollaborationRequest, error)
	GetSentRequests(ctx context.Context, userID string) ([]models.CollaborationRequest, error)
	AcceptRequest(ctx context.Context, userID, requestID string) error
	RejectRequest(ctx context.Context, userID, requestID string) error
}

type collaborationService struct {
	collabRepo repositories.CollaborationRepository
	projectRepo repositories.ProjectRepository
	teamRepo    repositories.TeamRepository
	notifService NotificationService
}

func NewCollaborationService(
	collabRepo repositories.CollaborationRepository,
	projectRepo repositories.ProjectRepository,
	teamRepo repositories.TeamRepository,
	notifService NotificationService,
) CollaborationService {
	return &collaborationService{
		collabRepo: collabRepo,
		projectRepo: projectRepo,
		teamRepo:    teamRepo,
		notifService: notifService,
	}
}

func (s *collaborationService) CreateRequest(ctx context.Context, senderID, projectID, receiverID string, proposedRole models.TeamRole, message string) (*models.CollaborationRequest, error) {
	sID, _ := primitive.ObjectIDFromHex(senderID)
	pID, _ := primitive.ObjectIDFromHex(projectID)
	rID, _ := primitive.ObjectIDFromHex(receiverID)

	if sID == rID {
		return nil, errors.New("cannot invite yourself")
	}

	exists, err := s.collabRepo.Exists(ctx, pID, sID, rID)
	if err != nil {
		return nil, err
	}
	if exists {
		return nil, errors.New("a pending request already exists")
	}

	request := &models.CollaborationRequest{
		ProjectID:    pID,
		SenderID:     sID,
		ReceiverID:   rID,
		ProposedRole: proposedRole,
		Message:      message,
		Status:       models.CollabPending,
		CreatedAt:    time.Now(),
		UpdatedAt:    time.Now(),
	}

	if err := s.collabRepo.Create(ctx, request); err != nil {
		return nil, err
	}

	// Notify receiver
	s.notifService.Notify(ctx, receiverID, models.NotifCollabRequest, "Collaboration Request", "You have been invited to join a project.")

	return request, nil
}

func (s *collaborationService) GetReceivedRequests(ctx context.Context, userID string) ([]models.CollaborationRequest, error) {
	uID, _ := primitive.ObjectIDFromHex(userID)
	return s.collabRepo.ListByReceiverID(ctx, uID)
}

func (s *collaborationService) GetSentRequests(ctx context.Context, userID string) ([]models.CollaborationRequest, error) {
	// Need to implement ListBySenderID in repository
	return []models.CollaborationRequest{}, nil
}

func (s *collaborationService) AcceptRequest(ctx context.Context, userID, requestID string) error {
	reqID, _ := primitive.ObjectIDFromHex(requestID)
	request, err := s.collabRepo.GetByID(ctx, reqID)
	if err != nil {
		return err
	}

	if request.ReceiverID.Hex() != userID {
		return errors.New("unauthorized")
	}

	if request.Status != models.CollabPending {
		return errors.New("request is no longer pending")
	}

	// 1. Update Request Status
	request.Status = models.CollabAccepted
	request.UpdatedAt = time.Now()
	if err := s.collabRepo.Update(ctx, request); err != nil {
		return err
	}

	// 2. Add User to Team
	team, err := s.teamRepo.GetByProjectID(ctx, request.ProjectID)
	if err != nil || team == nil {
		return errors.New("project team not found")
	}

	member := &models.TeamMember{
		TeamID:   team.ID,
		UserID:   request.ReceiverID,
		Role:     request.ProposedRole,
		Status:   models.MemberStatusActive,
		JoinedAt: time.Now(),
	}

	if err := s.teamRepo.AddMember(ctx, member); err != nil {
		return err
	}

	// 3. Notify Sender
	s.notifService.Notify(ctx, request.SenderID.Hex(), models.NotifRequestAccepted, "Request Accepted", "Your collaboration request was accepted.")

	return nil
}

func (s *collaborationService) RejectRequest(ctx context.Context, userID, requestID string) error {
	reqID, _ := primitive.ObjectIDFromHex(requestID)
	request, err := s.collabRepo.GetByID(ctx, reqID)
	if err != nil {
		return err
	}

	if request.ReceiverID.Hex() != userID {
		return errors.New("unauthorized")
	}

	request.Status = models.CollabRejected
	request.UpdatedAt = time.Now()
	return s.collabRepo.Update(ctx, request)
}
