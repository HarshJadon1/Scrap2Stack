package repositories

import (
	"context"
	"time"

	"github.com/scrap2stack/backend/internal/models"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
)

type RoadmapRepository interface {
	Create(ctx context.Context, roadmap *models.Roadmap) error
	GetByID(ctx context.Context, id primitive.ObjectID) (*models.Roadmap, error)
	GetByProjectID(ctx context.Context, projectID primitive.ObjectID) (*models.Roadmap, error)
	Update(ctx context.Context, roadmap *models.Roadmap) error
	AddItem(ctx context.Context, item *models.RoadmapItem) error
	GetItems(ctx context.Context, roadmapID primitive.ObjectID) ([]models.RoadmapItem, error)
	UpdateItem(ctx context.Context, item *models.RoadmapItem) error
	DeleteItem(ctx context.Context, id primitive.ObjectID) error
}

type roadmapRepository struct {
	roadmaps     *mongo.Collection
	roadmapItems *mongo.Collection
}

func NewRoadmapRepository(db *mongo.Database) RoadmapRepository {
	return &roadmapRepository{
		roadmaps:     db.Collection("roadmaps"),
		roadmapItems: db.Collection("roadmap_items"),
	}
}

func (r *roadmapRepository) Create(ctx context.Context, roadmap *models.Roadmap) error {
	res, err := r.roadmaps.InsertOne(ctx, roadmap)
	if err != nil {
		return err
	}
	roadmap.ID = res.InsertedID.(primitive.ObjectID)
	return nil
}

func (r *roadmapRepository) GetByID(ctx context.Context, id primitive.ObjectID) (*models.Roadmap, error) {
	var roadmap models.Roadmap
	err := r.roadmaps.FindOne(ctx, bson.M{"_id": id}).Decode(&roadmap)
	if err != nil {
		return nil, err
	}
	return &roadmap, nil
}

func (r *roadmapRepository) GetByProjectID(ctx context.Context, projectID primitive.ObjectID) (*models.Roadmap, error) {
	var roadmap models.Roadmap
	err := r.roadmaps.FindOne(ctx, bson.M{"project_id": projectID}).Decode(&roadmap)
	if err != nil {
		return nil, err
	}
	return &roadmap, nil
}

func (r *roadmapRepository) Update(ctx context.Context, roadmap *models.Roadmap) error {
	_, err := r.roadmaps.UpdateOne(ctx, bson.M{"_id": roadmap.ID}, bson.M{"$set": roadmap})
	return err
}

func (r *roadmapRepository) AddItem(ctx context.Context, item *models.RoadmapItem) error {
	res, err := r.roadmapItems.InsertOne(ctx, item)
	if err != nil {
		return err
	}
	item.ID = res.InsertedID.(primitive.ObjectID)
	return nil
}

func (r *roadmapRepository) GetItems(ctx context.Context, roadmapID primitive.ObjectID) ([]models.RoadmapItem, error) {
	cursor, err := r.roadmapItems.Find(ctx, bson.M{"roadmap_id": roadmapID})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var items []models.RoadmapItem
	if err = cursor.All(ctx, &items); err != nil {
		return nil, err
	}
	return items, nil
}

func (r *roadmapRepository) UpdateItem(ctx context.Context, item *models.RoadmapItem) error {
	_, err := r.roadmapItems.UpdateOne(ctx, bson.M{"_id": item.ID}, bson.M{"$set": item})
	return err
}

func (r *roadmapRepository) DeleteItem(ctx context.Context, id primitive.ObjectID) error {
	_, err := r.roadmapItems.DeleteOne(ctx, bson.M{"_id": id})
	return err
}
