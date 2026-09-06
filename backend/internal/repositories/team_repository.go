package repositories

import (
	"context"
	"time"

	"github.com/scrap2stack/backend/internal/models"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
)

type TeamRepository interface {
	Create(ctx context.Context, team *models.Team) error
	GetByID(ctx context.Context, id primitive.ObjectID) (*models.Team, error)
	GetByProjectID(ctx context.Context, projectID primitive.ObjectID) (*models.Team, error)
	AddMember(ctx context.Context, member *models.TeamMember) error
	GetMembers(ctx context.Context, teamID primitive.ObjectID) ([]models.TeamMember, error)
	RemoveMember(ctx context.Context, teamID, userID primitive.ObjectID) error
}

type teamRepository struct {
	teams       *mongo.Collection
	teamMembers *mongo.Collection
}

func NewTeamRepository(db *mongo.Database) TeamRepository {
	return &teamRepository{
		teams:       db.Collection("teams"),
		teamMembers: db.Collection("team_members"),
	}
}

func (r *teamRepository) Create(ctx context.Context, team *models.Team) error {
	team.CreatedAt = time.Now()
	team.UpdatedAt = time.Now()
	res, err := r.teams.InsertOne(ctx, team)
	if err != nil {
		return err
	}
	team.ID = res.InsertedID.(primitive.ObjectID)
	return nil
}

func (r *teamRepository) GetByID(ctx context.Context, id primitive.ObjectID) (*models.Team, error) {
	var team models.Team
	err := r.teams.FindOne(ctx, bson.M{"_id": id}).Decode(&team)
	if err != nil {
		return nil, err
	}
	return &team, nil
}

func (r *teamRepository) GetByProjectID(ctx context.Context, projectID primitive.ObjectID) (*models.Team, error) {
	var team models.Team
	err := r.teams.FindOne(ctx, bson.M{"project_id": projectID}).Decode(&team)
	if err != nil {
		return nil, err
	}
	return &team, nil
}

func (r *teamRepository) AddMember(ctx context.Context, member *models.TeamMember) error {
	member.JoinedAt = time.Now()
	res, err := r.teamMembers.InsertOne(ctx, member)
	if err != nil {
		return err
	}
	member.ID = res.InsertedID.(primitive.ObjectID)
	return nil
}

func (r *teamRepository) GetMembers(ctx context.Context, teamID primitive.ObjectID) ([]models.TeamMember, error) {
	cursor, err := r.teamMembers.Find(ctx, bson.M{"team_id": teamID})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var members []models.TeamMember
	if err = cursor.All(ctx, &members); err != nil {
		return nil, err
	}
	return members, nil
}

func (r *teamRepository) RemoveMember(ctx context.Context, teamID, userID primitive.ObjectID) error {
	_, err := r.teamMembers.DeleteOne(ctx, bson.M{"team_id": teamID, "user_id": userID})
	return err
}
