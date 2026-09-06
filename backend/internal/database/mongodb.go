package database

import (
	"context"
	"log"
	"time"

	"github.com/scrap2stack/backend/internal/config"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

type MongoDB struct {
	Client   *mongo.Client
	Database *mongo.Database
}

func ConnectMongoDB(cfg *config.Config) *MongoDB {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	clientOptions := options.Client().ApplyURI(cfg.MongoURI)
	client, err := mongo.Connect(ctx, clientOptions)
	if err != nil {
		log.Fatalf("Failed to connect to MongoDB: %v", err)
	}

	err = client.Ping(ctx, nil)
	if err != nil {
		log.Fatalf("Failed to ping MongoDB: %v", err)
	}

	log.Println("Connected to MongoDB")

	return &MongoDB{
		Client:   client,
		Database: client.Database(cfg.MongoDatabase),
	}
}

func (db *MongoDB) Close() {
	if db.Client != nil {
		err := db.Client.Disconnect(context.Background())
		if err != nil {
			log.Printf("Error disconnecting from MongoDB: %v", err)
		}
	}
}
