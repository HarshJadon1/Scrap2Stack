package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type Skill struct {
	ID        primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	Name      string             `bson:"name" json:"name" validate:"required"`
	Category  string             `bson:"category" json:"category"`
	Aliases   []string           `bson:"aliases" json:"aliases"`
	CreatedAt time.Time          `bson:"created_at" json:"created_at"`
}
