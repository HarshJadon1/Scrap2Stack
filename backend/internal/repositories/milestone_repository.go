package repositories

import (
	"context"
	"time"

	"github.com/scrap2stack/backend/internal/models"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
)

type MilestoneRepository interface {
	Create(ctx context.Context, milestone *models.ProjectMilestone) error
	ListByProjectID(ctx context.Context, projectID primitive.ObjectID) ([]models.ProjectMilestone)
	Update(ctx context.Context, milestone *models.ProjectMilestone) error
}

type milestoneRepository struct {
	collection *mongo.Collection
}

func NewMilestoneRepository(db *mongo.Database) MilestoneRepository {
	return &milestoneRepository{
		collection: db.Collection("project_milestones"),
	}
}

func (r *milestoneRepository) Create(ctx context.Context, m *models.ProjectMilestone) error {
	m.CreatedAt = time.Now()
	res, err := r.collection.InsertOne(ctx, m)
	if err != nil {
		return err
	}
	m.ID = res.InsertedID.(primitive.ObjectID)
	return nil
}

func (r *milestoneRepository) ListByProjectID(ctx context.Context, pID primitive.ObjectID) []models.ProjectMilestone {
	cursor, err := r.collection.Find(ctx, bson.M{"project_id": pID})
	if err != nil {
		return nil
	}
	defer cursor.Close(ctx)
	var milestones []models.ProjectMilestone
	cursor.All(ctx, &milestones)
	return milestones
}

func (r *milestoneRepository) Update(ctx context.Context, m *models.ProjectMilestone) error {
	_, err := r.collection.UpdateOne(ctx, bson.M{"_id": m.ID}, bson.M{"$set": m})
	return err
}
