package repositories

import (
	"context"
	"time"

	"github.com/scrap2stack/backend/internal/models"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
)

type CharmRepository interface {
	CreateTransaction(ctx context.Context, transaction *models.CharmTransaction) error
	GetByUserID(ctx context.Context, userID primitive.ObjectID) ([]models.CharmTransaction, error)
	GetByIdempotencyKey(ctx context.Context, key string) (*models.CharmTransaction, error)
}

type charmRepository struct {
	collection *mongo.Collection
}

func NewCharmRepository(db *mongo.Database) CharmRepository {
	return &charmRepository{
		collection: db.Collection("charm_transactions"),
	}
}

func (r *charmRepository) CreateTransaction(ctx context.Context, t *models.CharmTransaction) error {
	t.CreatedAt = time.Now()
	res, err := r.collection.InsertOne(ctx, t)
	if err != nil {
		return err
	}
	t.ID = res.InsertedID.(primitive.ObjectID)
	return nil
}

func (r *charmRepository) GetByUserID(ctx context.Context, userID primitive.ObjectID) ([]models.CharmTransaction, error) {
	cursor, err := r.collection.Find(ctx, bson.M{"user_id": userID})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)
	var transactions []models.CharmTransaction
	if err = cursor.All(ctx, &transactions); err != nil {
		return nil, err
	}
	return transactions, nil
}

func (r *charmRepository) GetByIdempotencyKey(ctx context.Context, key string) (*models.CharmTransaction, error) {
	var t models.CharmTransaction
	err := r.collection.FindOne(ctx, bson.M{"idempotency_key": key}).Decode(&t)
	if err != nil {
		return nil, err
	}
	return &t, nil
}
