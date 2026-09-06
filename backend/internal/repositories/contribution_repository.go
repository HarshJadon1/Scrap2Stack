package repositories

import (
	"context"
	"time"

	"github.com/scrap2stack/backend/internal/models"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
)

type ContributionRepository interface {
	Create(ctx context.Context, contribution *models.GitHubContribution) error
	GetByGitHubEventID(ctx context.Context, eventID string) (*models.GitHubContribution, error)
	ListByUserID(ctx context.Context, userID primitive.ObjectID) ([]models.GitHubContribution, error)
	ListByProjectID(ctx context.Context, projectID primitive.ObjectID) ([]models.GitHubContribution, error)
	GetStatsByUserID(ctx context.Context, userID primitive.ObjectID) (map[string]int, error)
}

type contributionRepository struct {
	collection *mongo.Collection
}

func NewContributionRepository(db *mongo.Database) ContributionRepository {
	return &contributionRepository{
		collection: db.Collection("github_contributions"),
	}
}

func (r *contributionRepository) Create(ctx context.Context, c *models.GitHubContribution) error {
	c.CreatedAt = time.Now()
	res, err := r.collection.InsertOne(ctx, c)
	if err != nil {
		return err
	}
	c.ID = res.InsertedID.(primitive.ObjectID)
	return nil
}

func (r *contributionRepository) GetByGitHubEventID(ctx context.Context, eventID string) (*models.GitHubContribution, error) {
	var c models.GitHubContribution
	err := r.collection.FindOne(ctx, bson.M{"github_event_id": eventID}).Decode(&c)
	if err != nil {
		return nil, err
	}
	return &c, nil
}

func (r *contributionRepository) ListByUserID(ctx context.Context, userID primitive.ObjectID) ([]models.GitHubContribution, error) {
	cursor, err := r.collection.Find(ctx, bson.M{"user_id": userID})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)
	var contributions []models.GitHubContribution
	if err = cursor.All(ctx, &contributions); err != nil {
		return nil, err
	}
	return contributions, nil
}

func (r *contributionRepository) ListByProjectID(ctx context.Context, projectID primitive.ObjectID) ([]models.GitHubContribution, error) {
	cursor, err := r.collection.Find(ctx, bson.M{"project_id": projectID})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)
	var contributions []models.GitHubContribution
	if err = cursor.All(ctx, &contributions); err != nil {
		return nil, err
	}
	return contributions, nil
}

func (r *contributionRepository) GetStatsByUserID(ctx context.Context, userID primitive.ObjectID) (map[string]int, error) {
	// Simple implementation for Phase 6
	contributions, err := r.ListByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}
	stats := make(map[string]int)
	for _, c := range contributions {
		stats[string(c.Type)]++
	}
	return stats, nil
}
