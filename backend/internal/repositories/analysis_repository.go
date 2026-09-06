package repositories

import (
	"context"
	"time"

	"github.com/scrap2stack/backend/internal/models"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
)

type AnalysisRepository interface {
	Create(ctx context.Context, analysis *models.ProjectAnalysis) error
	GetByProjectID(ctx context.Context, projectID primitive.ObjectID) (*models.ProjectAnalysis, error)
	Update(ctx context.Context, analysis *models.ProjectAnalysis) error
}

type analysisRepository struct {
	collection *mongo.Collection
}

func NewAnalysisRepository(db *mongo.Database) AnalysisRepository {
	return &analysisRepository{
		collection: db.Collection("project_analyses"),
	}
}

func (r *analysisRepository) Create(ctx context.Context, analysis *models.ProjectAnalysis) error {
	analysis.CreatedAt = time.Now()
	analysis.UpdatedAt = time.Now()
	res, err := r.collection.InsertOne(ctx, analysis)
	if err != nil {
		return err
	}
	analysis.ID = res.InsertedID.(primitive.ObjectID)
	return nil
}

func (r *analysisRepository) GetByProjectID(ctx context.Context, projectID primitive.ObjectID) (*models.ProjectAnalysis, error) {
	var analysis models.ProjectAnalysis
	err := r.collection.FindOne(ctx, bson.M{"project_id": projectID}).Decode(&analysis)
	if err != nil {
		return nil, err
	}
	return &analysis, nil
}

func (r *analysisRepository) Update(ctx context.Context, analysis *models.ProjectAnalysis) error {
	analysis.UpdatedAt = time.Now()
	_, err := r.collection.UpdateOne(ctx, bson.M{"_id": analysis.ID}, bson.M{"$set": analysis})
	return err
}
