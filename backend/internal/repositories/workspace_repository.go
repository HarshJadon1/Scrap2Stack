package repositories

import (
	"context"
	"time"

	"github.com/scrap2stack/backend/internal/models"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
)

type WorkspaceRepository interface {
	Create(ctx context.Context, workspace *models.Workspace) error
	GetByID(ctx context.Context, id primitive.ObjectID) (*models.Workspace, error)
	GetByProjectID(ctx context.Context, projectID primitive.ObjectID) (*models.Workspace, error)
	Update(ctx context.Context, workspace *models.Workspace) error
}

type workspaceRepository struct {
	collection *mongo.Collection
}

func NewWorkspaceRepository(db *mongo.Database) WorkspaceRepository {
	return &workspaceRepository{
		collection: db.Collection("workspaces"),
	}
}

func (r *workspaceRepository) Create(ctx context.Context, workspace *models.Workspace) error {
	workspace.CreatedAt = time.Now()
	workspace.UpdatedAt = time.Now()
	res, err := r.collection.InsertOne(ctx, workspace)
	if err != nil {
		return err
	}
	workspace.ID = res.InsertedID.(primitive.ObjectID)
	return nil
}

func (r *workspaceRepository) GetByID(ctx context.Context, id primitive.ObjectID) (*models.Workspace, error) {
	var workspace models.Workspace
	err := r.collection.FindOne(ctx, bson.M{"_id": id}).Decode(&workspace)
	if err != nil {
		return nil, err
	}
	return &workspace, nil
}

func (r *workspaceRepository) GetByProjectID(ctx context.Context, projectID primitive.ObjectID) (*models.Workspace, error) {
	var workspace models.Workspace
	err := r.collection.FindOne(ctx, bson.M{"project_id": projectID}).Decode(&workspace)
	if err != nil {
		return nil, err
	}
	return &workspace, nil
}

func (r *workspaceRepository) Update(ctx context.Context, workspace *models.Workspace) error {
	workspace.UpdatedAt = time.Now()
	_, err := r.collection.UpdateOne(ctx, bson.M{"_id": workspace.ID}, bson.M{"$set": workspace})
	return err
}
