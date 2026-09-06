package repositories

import (
	"context"
	"time"

	"github.com/scrap2stack/backend/internal/models"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
)

type SkillRepository interface {
	Create(ctx context.Context, skill *models.Skill) error
	List(ctx context.Context) ([]models.Skill, error)
}

type skillRepository struct {
	collection *mongo.Collection
}

func NewSkillRepository(db *mongo.Database) SkillRepository {
	return &skillRepository{
		collection: db.Collection("skills"),
	}
}

func (r *skillRepository) Create(ctx context.Context, skill *models.Skill) error {
	skill.CreatedAt = time.Now()
	res, err := r.collection.InsertOne(ctx, skill)
	if err != nil {
		return err
	}
	skill.ID = res.InsertedID.(primitive.ObjectID)
	return nil
}

func (r *skillRepository) List(ctx context.Context) ([]models.Skill, error) {
	cursor, err := r.collection.Find(ctx, bson.M{})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var skills []models.Skill
	if err = cursor.All(ctx, &skills); err != nil {
		return nil, err
	}
	return skills, nil
}
