package main

import (
	"context"
	"log"
	"time"

	"github.com/scrap2stack/backend/internal/config"
	"github.com/scrap2stack/backend/internal/database"
	"github.com/scrap2stack/backend/internal/models"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"golang.org/x/crypto/bcrypt"
)

func main() {
	cfg := config.LoadConfig()
	db := database.ConnectMongoDB(cfg)
	defer db.Close()

	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()

	// Clear collections
	log.Println("Clearing database...")
	db.Database.Collection("users").Drop(ctx)
	db.Database.Collection("projects").Drop(ctx)
	db.Database.Collection("skills").Drop(ctx)
	db.Database.Collection("teams").Drop(ctx)
	db.Database.Collection("team_members").Drop(ctx)
	db.Database.Collection("collaboration_requests").Drop(ctx)

	// Seed Users (Realistic Developers)
	users := seedUsers(ctx, db)

	// Seed Projects with Requirements
	seedProjects(ctx, db, users)

	log.Println("Seeding completed successfully")
}

func seedUsers(ctx context.Context, db *database.MongoDB) []models.User {
	password, _ := bcrypt.GenerateFromPassword([]byte("password123"), bcrypt.DefaultCost)

	devs := []models.User{
		{
			ID:              primitive.NewObjectID(),
			Name:            "Alex Kumar",
			Username:        "alexk",
			Email:           "alex@example.com",
			PasswordHash:    string(password),
			Bio:             "Expert Backend Developer with a passion for Go and Distributed Systems.",
			ExperienceLevel: models.ProficiencyAdvanced,
			Skills: []models.UserSkill{
				{Name: "Go", Level: models.ProficiencyAdvanced, Years: 4},
				{Name: "MongoDB", Level: models.ProficiencyIntermediate, Years: 2},
				{Name: "Docker", Level: models.ProficiencyAdvanced, Years: 3},
				{Name: "REST API", Level: models.ProficiencyExpert, Years: 5},
			},
			Interests:         []string{"Backend", "Open Source", "Infrastructure"},
			PreferredRoles:    []models.TeamRole{models.RoleBackendDev, models.RoleDevOps},
			Availability:      models.AvailabilityAvailable,
			Charms:            1250,
			CompletedProjects: 8,
			MergedPRs:         45,
			CompletedTasks:    120,
			CreatedAt:         time.Now(),
		},
		{
			ID:              primitive.NewObjectID(),
			Name:            "Priya Sharma",
			Username:        "priyas",
			Email:           "priya@example.com",
			PasswordHash:    string(password),
			Bio:             "UI/UX Designer and Frontend enthusiast. Lover of clean interfaces.",
			ExperienceLevel: models.ProficiencyIntermediate,
			Skills: []models.UserSkill{
				{Name: "Figma", Level: models.ProficiencyAdvanced, Years: 3},
				{Name: "UI/UX", Level: models.ProficiencyAdvanced, Years: 3},
				{Name: "React", Level: models.ProficiencyIntermediate, Years: 2},
			},
			Interests:         []string{"Design", "Product", "Social Impact"},
			PreferredRoles:    []models.TeamRole{models.RoleUiUxDesigner, models.RoleFrontendDev},
			Availability:      models.AvailabilityAvailable,
			Charms:            850,
			CreatedAt:         time.Now(),
		},
		{
			ID:              primitive.NewObjectID(),
			Name:            "John Developer",
			Username:        "johndev",
			Email:           "john@example.com",
			PasswordHash:    string(password),
			Bio:             "Full-stack dev and project reviver.",
			ExperienceLevel: models.ProficiencyIntermediate,
			Skills: []models.UserSkill{
				{Name: "Kotlin", Level: models.ProficiencyAdvanced, Years: 3},
				{Name: "Android", Level: models.ProficiencyAdvanced, Years: 3},
			},
			Availability: models.AvailabilityAvailable,
			Charms:       1280,
			CreatedAt:    time.Now(),
		},
		{
			ID:              primitive.NewObjectID(),
			Name:            "Sarah Chen",
			Username:        "sarahc",
			Email:           "sarah@example.com",
			PasswordHash:    string(password),
			Bio:             "AI Researcher and Python developer.",
			ExperienceLevel: models.ProficiencyAdvanced,
			Skills: []models.UserSkill{
				{Name: "Python", Level: models.ProficiencyAdvanced, Years: 4},
				{Name: "TensorFlow", Level: models.ProficiencyIntermediate, Years: 2},
				{Name: "Machine Learning", Level: models.ProficiencyAdvanced, Years: 3},
			},
			Interests:         []string{"AI", "Health", "Research"},
			PreferredRoles:    []models.TeamRole{models.RoleAiMlDev, models.RoleDataScientist},
			Availability:      models.AvailabilityPartiallyAvailable,
			Charms:            920,
			CreatedAt:         time.Now(),
		},
	}

	var seeded []models.User
	for _, dev := range devs {
		_, err := db.Database.Collection("users").InsertOne(ctx, dev)
		if err != nil {
			log.Fatalf("Failed to seed user %s: %v", dev.Username, err)
		}
		seeded = append(seeded, dev)
	}
	return seeded
}

func seedProjects(ctx context.Context, db *database.MongoDB, users []models.User) {
	projects := []models.Project{
		{
			ID:          primitive.NewObjectID(),
			OwnerID:     users[2].ID, // John
			Name:        "AI Crop Disease Detection",
			Description: "An unfinished ML project for detecting crop diseases.",
			Category:    "AI",
			Status:      models.ProjectStatusAbandoned,
			Difficulty:  models.DifficultyIntermediate,
			Technologies: []string{"Python", "TensorFlow Lite", "Kotlin"},
			RequiredSkills: []models.RequiredSkill{
				{Name: "Python", RequiredLevel: models.ProficiencyIntermediate, Importance: "HIGH"},
				{Name: "Machine Learning", RequiredLevel: models.ProficiencyIntermediate, Importance: "HIGH"},
				{Name: "Go", RequiredLevel: models.ProficiencyIntermediate, Importance: "MEDIUM"},
				{Name: "UI/UX", RequiredLevel: models.ProficiencyBeginner, Importance: "MEDIUM"},
			},
			RequiredRoles: []models.RequiredRole{
				{Role: models.RoleProjectLead, Count: 1, Filled: 1, Importance: "CRITICAL"},
				{Role: models.RoleBackendDev, Count: 1, Filled: 0, Importance: "HIGH"},
				{Role: models.RoleAiMlDev, Count: 1, Filled: 0, Importance: "HIGH"},
				{Role: models.RoleUiUxDesigner, Count: 1, Filled: 0, Importance: "MEDIUM"},
			},
			TeamSize:     4,
			RevivalScore: 87,
			CreatedAt:    time.Now(),
		},
	}

	for _, p := range projects {
		_, err := db.Database.Collection("projects").InsertOne(ctx, p)
		if err != nil {
			log.Fatalf("Failed to seed project %s: %v", p.Name, err)
		}

		// Create associated team
		team := models.Team{
			ID:         primitive.NewObjectID(),
			ProjectID:  p.ID,
			Name:       p.Name + " Team",
			OwnerID:    p.OwnerID,
			Status:     models.TeamStatusForming,
			MaxMembers: p.TeamSize,
			CreatedAt:  time.Now(),
		}
		db.Database.Collection("teams").InsertOne(ctx, team)

		// Add lead
		member := models.TeamMember{
			TeamID:   team.ID,
			UserID:   p.OwnerID,
			Role:     models.RoleProjectLead,
			Status:   models.MemberStatusActive,
			JoinedAt: time.Now(),
		}
		db.Database.Collection("team_members").InsertOne(ctx, member)

		// Update project with team ID
		db.Database.Collection("projects").UpdateOne(ctx, primitive.M{"_id": p.ID}, primitive.M{"$set": primitive.M{"team_id": team.ID}})
	}
}
