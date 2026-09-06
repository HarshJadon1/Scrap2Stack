package repositories

import (
	"context"
	"time"

	"github.com/scrap2stack/backend/internal/models"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
)

type CollaborationRepository interface {
	Create(ctx context.Context, request *models.CollaborationRequest) error
	GetByID(ctx context.Context, id primitive.ObjectID) (*models.CollaborationRequest, error)
	ListByProjectID(ctx context.Context, projectID primitive.ObjectID) ([]models.CollaborationRequest, error)
	ListByReceiverID(ctx context.Context, receiverID primitive.ObjectID) ([]models.CollaborationRequest, error)
	Update(ctx context.Context, request *models.CollaborationRequest) error
	Exists(ctx context.Context, projectID, senderID, receiverID primitive.ObjectID) (bool, error)
}

type collaborationRepository struct {
	collection *mongo.Collection
}

func NewCollaborationRepository(db *mongo.Database) CollaborationRepository {
	return &collaborationRepository{
		collection: db.Collection("collaboration_requests"),
	}
}

func (r *collaborationRepository) Create(ctx context.Context, request *models.CollaborationRequest) error {
	request.CreatedAt = time.Now()
	request.UpdatedAt = time.Now()
	res, err := r.collection.InsertOne(ctx, request)
	if err != nil {
		return err
	}
	request.ID = res.InsertedID.(primitive.ObjectID)
	return nil
}

func (r *collaborationRepository) GetByID(ctx context.Context, id primitive.ObjectID) (*models.CollaborationRequest, error) {
	var request models.CollaborationRequest
	err := r.collection.FindOne(ctx, bson.M{"_id": id}).Decode(&request)
	if err != nil {
		return nil, err
	}
	return &request, nil
}

func (r *collaborationRepository) ListByProjectID(ctx context.Context, projectID primitive.ObjectID) ([]models.CollaborationRequest, error) {
	cursor, err := r.collection.Find(ctx, bson.M{"project_id": projectID})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)
	var requests []models.CollaborationRequest
	if err = cursor.All(ctx, &requests); err != nil {
		return nil, err
	}
	return requests, nil
}

func (r *collaborationRepository) ListByReceiverID(ctx context.Context, receiverID primitive.ObjectID) ([]models.CollaborationRequest, error) {
	cursor, err := r.collection.Find(ctx, bson.M{"receiver_id": receiverID})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)
	var requests []models.CollaborationRequest
	if err = cursor.All(ctx, &requests); err != nil {
		return nil, err
	}
	return requests, nil
}

func (r *collaborationRepository) Update(ctx context.Context, request *models.CollaborationRequest) error {
	request.UpdatedAt = time.Now()
	_, err := r.collection.UpdateOne(ctx, bson.M{"_id": request.ID}, bson.M{"$set": request})
	return err
}

func (r *collaborationRepository) Exists(ctx context.Context, projectID, senderID, receiverID primitive.ObjectID) (bool, error) {
	count, err := r.collection.CountDocuments(ctx, bson.M{
		"project_id":  projectID,
		"sender_id":   senderID,
		"receiver_id": receiverID,
		"status":      models.CollabPending,
	})
	return count > 0, err
}
