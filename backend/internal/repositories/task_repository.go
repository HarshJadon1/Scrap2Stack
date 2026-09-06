package repositories

import (
	"context"
	"time"

	"github.com/scrap2stack/backend/internal/models"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
)

type TaskRepository interface {
	Create(ctx context.Context, task *models.Task) error
	GetByID(ctx context.Context, id primitive.ObjectID) (*models.Task, error)
	ListByProjectID(ctx context.Context, projectID primitive.ObjectID) ([]models.Task, error)
	ListByWorkspaceID(ctx context.Context, workspaceID primitive.ObjectID) ([]models.Task, error)
	Update(ctx context.Context, task *models.Task) error
	Delete(ctx context.Context, id primitive.ObjectID) error
}

type taskRepository struct {
	collection *mongo.Collection
}

func NewTaskRepository(db *mongo.Database) TaskRepository {
	return &taskRepository{
		collection: db.Collection("tasks"),
	}
}

func (r *taskRepository) Create(ctx context.Context, task *models.Task) error {
	task.CreatedAt = time.Now()
	task.UpdatedAt = time.Now()
	res, err := r.collection.InsertOne(ctx, task)
	if err != nil {
		return err
	}
	task.ID = res.InsertedID.(primitive.ObjectID)
	return nil
}

func (r *taskRepository) GetByID(ctx context.Context, id primitive.ObjectID) (*models.Task, error) {
	var task models.Task
	err := r.collection.FindOne(ctx, bson.M{"_id": id}).Decode(&task)
	if err != nil {
		return nil, err
	}
	return &task, nil
}

func (r *taskRepository) ListByProjectID(ctx context.Context, projectID primitive.ObjectID) ([]models.Task, error) {
	cursor, err := r.collection.Find(ctx, bson.M{"project_id": projectID})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var tasks []models.Task
	if err = cursor.All(ctx, &tasks); err != nil {
		return nil, err
	}
	return tasks, nil
}

func (r *taskRepository) ListByWorkspaceID(ctx context.Context, workspaceID primitive.ObjectID) ([]models.Task, error) {
	cursor, err := r.collection.Find(ctx, bson.M{"workspace_id": workspaceID})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var tasks []models.Task
	if err = cursor.All(ctx, &tasks); err != nil {
		return nil, err
	}
	return tasks, nil
}

func (r *taskRepository) Update(ctx context.Context, task *models.Task) error {
	task.UpdatedAt = time.Now()
	_, err := r.collection.UpdateOne(ctx, bson.M{"_id": task.ID}, bson.M{"$set": task})
	return err
}

func (r *taskRepository) Delete(ctx context.Context, id primitive.ObjectID) error {
	_, err := r.collection.DeleteOne(ctx, bson.M{"_id": id})
	return err
}
