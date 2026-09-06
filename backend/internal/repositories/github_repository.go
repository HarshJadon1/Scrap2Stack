package repositories

import (
	"context"
	"time"

	"github.com/scrap2stack/backend/internal/models"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

type GitHubRepository interface {
	Upsert(ctx context.Context, repo *models.GitHubRepository) error
	GetByGitHubID(ctx context.Context, githubID int64) (*models.GitHubRepository, error)
	GetByFullName(ctx context.Context, fullName string) (*models.GitHubRepository, error)
}

type githubRepository struct {
	collection *mongo.Collection
}

func NewGitHubRepository(db *mongo.Database) GitHubRepository {
	return &githubRepository{
		collection: db.Collection("github_repositories"),
	}
}

func (r *githubRepository) Upsert(ctx context.Context, repo *models.GitHubRepository) error {
	repo.SyncedAt = time.Now()
	if repo.CreatedAt.IsZero() {
		repo.CreatedAt = time.Now()
	}
	repo.UpdatedAt = time.Now()

	opts := options.Update().SetUpsert(true)
	filter := bson.M{"github_repository_id": repo.GitHubRepositoryID}
	update := bson.M{"$set": repo}

	res, err := r.collection.UpdateOne(ctx, filter, update, opts)
	if err != nil {
		return err
	}

	if res.UpsertedID != nil {
		repo.ID = res.UpsertedID.(primitive.ObjectID)
	} else {
		// If not upserted, we need to fetch the ID if it wasn't set
		if repo.ID.IsZero() {
			existing, _ := r.GetByGitHubID(ctx, repo.GitHubRepositoryID)
			if existing != nil {
				repo.ID = existing.ID
			}
		}
	}

	return nil
}

func (r *githubRepository) GetByGitHubID(ctx context.Context, githubID int64) (*models.GitHubRepository, error) {
	var repo models.GitHubRepository
	err := r.collection.FindOne(ctx, bson.M{"github_repository_id": githubID}).Decode(&repo)
	if err != nil {
		return nil, err
	}
	return &repo, nil
}

func (r *githubRepository) GetByFullName(ctx context.Context, fullName string) (*models.GitHubRepository, error) {
	var repo models.GitHubRepository
	err := r.collection.FindOne(ctx, bson.M{"full_name": fullName}).Decode(&repo)
	if err != nil {
		return nil, err
	}
	return &repo, nil
}
