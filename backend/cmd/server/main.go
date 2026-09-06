package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/scrap2stack/backend/internal/ai"
	"github.com/scrap2stack/backend/internal/config"
	"github.com/scrap2stack/backend/internal/database"
	"github.com/scrap2stack/backend/internal/github"
	"github.com/scrap2stack/backend/internal/handlers"
	"github.com/scrap2stack/backend/internal/middleware"
	"github.com/scrap2stack/backend/internal/repositories"
	"github.com/scrap2stack/backend/internal/services"
	"github.com/scrap2stack/backend/pkg/response"
)

func main() {
	// Load config
	cfg := config.LoadConfig()

	// Connect to Database
	db := database.ConnectMongoDB(cfg)
	defer db.Close()

	// Initialize Repositories
	userRepo := repositories.NewUserRepository(db.Database)
	projectRepo := repositories.NewProjectRepository(db.Database)
	skillRepo := repositories.NewSkillRepository(db.Database)
	teamRepo := repositories.NewTeamRepository(db.Database)
	taskRepo := repositories.NewTaskRepository(db.Database)
	collabRepo := repositories.NewCollaborationRepository(db.Database)
	roadmapRepo := repositories.NewRoadmapRepository(db.Database)
	githubRepo := repositories.NewGitHubRepository(db.Database)
	analysisRepo := repositories.NewAnalysisRepository(db.Database)
	workspaceRepo := repositories.NewWorkspaceRepository(db.Database)
	contributeRepo := repositories.NewContributionRepository(db.Database)
	charmRepo := repositories.NewCharmRepository(db.Database)
	milestoneRepo := repositories.NewMilestoneRepository(db.Database)

	// Initialize Clients/Providers
	ghToken := os.Getenv("GITHUB_TOKEN")
	githubClient := github.NewGitHubClient(ghToken)
	aiProvider := ai.NewMockAIProvider()

	// Initialize Services
	notifService := services.NewNotificationService()
	charmService := services.NewCharmService(charmRepo, userRepo)
	authService := services.NewAuthService(userRepo, cfg)
	userService := services.NewUserService(userRepo)
	projectService := services.NewProjectService(projectRepo)
	skillService := services.NewSkillService(skillRepo)
	revivalService := services.NewRevivalScoreService(githubRepo)
	teamService := services.NewTeamService(teamRepo, projectRepo, userRepo, contributeRepo, charmRepo)
	collabService := services.NewCollaborationService(collabRepo, projectRepo, teamRepo, notifService)
	workspaceService := services.NewWorkspaceService(workspaceRepo, projectRepo, taskRepo, roadmapRepo)
	taskService := services.NewTaskService(taskRepo, projectRepo, teamRepo, notifService)
	matchingService := services.NewMatchingService(userRepo, projectRepo, teamRepo)

	aiAnalyzer := services.NewAIAnalyzer(aiProvider)
	roadmapService := services.NewRoadmapService(roadmapRepo, projectRepo, teamRepo, taskRepo, aiAnalyzer)
	githubService := github.NewGitHubService(githubClient)
	contributionService := services.NewContributionService(githubClient, projectRepo, githubRepo, contributeRepo, userRepo, charmService)
	importService := services.NewImportService(githubService, aiAnalyzer, projectRepo, githubRepo, analysisRepo, revivalService)
	milestoneService := services.NewMilestoneService(milestoneRepo, projectRepo, teamRepo, charmService)

	// Initialize Handlers
	authHandler := handlers.NewAuthHandler(authService)
	userHandler := handlers.NewUserHandler(userService)
	projectHandler := handlers.NewProjectHandler(projectService)
	skillHandler := handlers.NewSkillHandler(skillService)
	revivalHandler := handlers.NewRevivalHandler(revivalService, projectService)
	collabHandler := handlers.NewCollaborationHandler(collabService)
	teamHandler := handlers.NewTeamHandler(teamService)
	taskHandler := handlers.NewTaskHandler(taskRepo)
	githubHandler := handlers.NewGitHubHandler(importService)
	matchingHandler := handlers.NewMatchingHandler(matchingService)
	workspaceHandler := handlers.NewWorkspaceHandler(workspaceService)
	roadmapHandler := handlers.NewRoadmapHandler(roadmapService)
	contributionHandler := handlers.NewContributionHandler(contributionService)
	milestoneHandler := handlers.NewMilestoneHandler(milestoneRepo)

	// Setup Router
	r := gin.Default()

	// Global Middleware
	r.Use(gin.Recovery())
	r.Use(cors.New(cors.Config{
		AllowOrigins:     []string{"*"},
		AllowMethods:     []string{"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"Origin", "Content-Type", "Accept", "Authorization"},
		ExposeHeaders:    []string{"Content-Length"},
		AllowCredentials: true,
		MaxAge:           12 * time.Hour,
	}))

	// Health Check
	r.GET("/health", func(c *gin.Context) {
		response.Success(c, http.StatusOK, "Scrap2Stack backend is running", gin.H{
			"environment": cfg.Environment,
		})
	})

	// API Routes V1
	v1 := r.Group("/api/v1")
	{
		// Public Auth
		auth := v1.Group("/auth")
		{
			auth.POST("/register", authHandler.Register)
			auth.POST("/login", authHandler.Login)
		}

		v1.GET("/projects", projectHandler.List)
		v1.GET("/projects/:id", projectHandler.GetByID)
		v1.GET("/skills", skillHandler.List)

		protected := v1.Group("")
		protected.Use(middleware.JWTMiddleware(cfg.JWTSecret))
		{
			// User Profile & Recommendations
			protected.GET("/users/me", userHandler.GetMe)
			protected.PUT("/users/me", userHandler.UpdateMe)
			protected.GET("/users/:id", userHandler.GetByID)
			protected.GET("/users/me/contributions", contributionHandler.GetMyContributions)

			// PHASE 7: Recommendation Engine Endpoints
			protected.GET("/users/me/recommended-projects", matchingHandler.GetRecommendedProjects)
			protected.GET("/projects/:id/recommended-developers", matchingHandler.GetMatches)

			protected.GET("/users/me/charms", func(c *gin.Context) {
				balance, _ := charmService.GetUserBalance(c.Request.Context(), c.GetString("userID"))
				response.Success(c, http.StatusOK, "Balance retrieved", gin.H{"balance": balance})
			})

			// Project Management
			protected.POST("/projects", projectHandler.Create)
			protected.PUT("/projects/:id", projectHandler.Update)
			protected.DELETE("/projects/:id", projectHandler.Delete)

			// GitHub Integration
			protected.POST("/github/import", githubHandler.ImportRepository)
			protected.POST("/projects/:id/github/sync", contributionHandler.SyncGitHub)

			// Analysis, Revival & Matching (Matching is also handled by recommended-developers)
			protected.GET("/projects/:id/revival-score", revivalHandler.GetScore)
			protected.GET("/projects/:id/matches", matchingHandler.GetMatches)
			protected.GET("/projects/:id/analysis", func(c *gin.Context) {
				res, err := analysisRepo.GetByProjectID(c.Request.Context(), repositories.ToObjectID(c.Param("id")))
				if err != nil {
					response.Error(c, http.StatusNotFound, "Analysis not found", "NOT_FOUND", nil)
					return
				}
				response.Success(c, http.StatusOK, "Analysis retrieved", res)
			})

			// Workspace
			protected.GET("/projects/:id/workspace", workspaceHandler.GetWorkspace)
			protected.POST("/projects/:id/workspace", workspaceHandler.CreateWorkspace)
			protected.PUT("/workspaces/:id/sync", workspaceHandler.SyncProgress)

			// Roadmap
			protected.GET("/projects/:id/roadmap", roadmapHandler.GetRoadmap)
			protected.POST("/projects/:id/roadmap/generate", roadmapHandler.GenerateAI)
			protected.POST("/roadmap/:id/items", roadmapHandler.AddItem)

			// Collaboration
			protected.POST("/projects/:id/collaboration-requests", collabHandler.CreateRequest)
			protected.GET("/collaboration-requests", collabHandler.GetReceivedRequests)
			protected.PUT("/collaboration-requests/:id/accept", collabHandler.AcceptRequest)
			protected.PUT("/collaboration-requests/:id/reject", collabHandler.RejectRequest)

			// Teams
			protected.POST("/teams", teamHandler.Create)
			protected.GET("/teams/:id", teamHandler.GetByID)
			protected.GET("/teams/:id/members", teamHandler.GetMembers)
			protected.POST("/teams/:id/members", teamHandler.AddMember)
			protected.PUT("/teams/:id/members/:userId/role", teamHandler.ChangeRole)
			protected.DELETE("/teams/:id/members/:userId", teamHandler.RemoveMember)
			protected.GET("/projects/:id/leaderboard", func(c *gin.Context) {
				res, err := teamService.GetProjectLeaderboard(c.Request.Context(), c.Param("id"))
				if err != nil { response.Error(c, http.StatusInternalServerError, err.Error(), "FETCH_FAILED", nil); return }
				response.Success(c, http.StatusOK, "Leaderboard retrieved", res)
			})

			// Milestones
			protected.GET("/projects/:id/milestones", milestoneHandler.List)
			protected.POST("/projects/:id/milestones", milestoneHandler.Create)
			protected.PUT("/milestones/:id/complete", func(c *gin.Context) {
				err := milestoneService.CompleteMilestone(c.Request.Context(), c.GetString("userID"), c.Param("id"))
				if err != nil { response.Error(c, http.StatusInternalServerError, err.Error(), "UPDATE_FAILED", nil); return }
				response.Success(c, http.StatusOK, "Milestone completed", nil)
			})

			// Tasks
			protected.GET("/projects/:id/tasks", taskHandler.ListByProject)
			protected.POST("/projects/:id/tasks", taskHandler.Create)
			protected.PUT("/tasks/:id/status", func(c *gin.Context) {
				var req struct { Status string `json:"status"` }
				c.ShouldBindJSON(&req)
				err := taskService.UpdateTaskStatus(c.Request.Context(), c.GetString("userID"), c.Param("id"), models.TaskStatus(req.Status))
				if err != nil { response.Error(c, http.StatusForbidden, err.Error(), "UPDATE_FAILED", nil); return }
				response.Success(c, http.StatusOK, "Status updated", nil)
			})
			protected.PUT("/tasks/:id/assign", func(c *gin.Context) {
				var req struct { AssigneeID string `json:"assigneeId"` }
				c.ShouldBindJSON(&req)
				err := taskService.AssignTask(c.Request.Context(), c.GetString("userID"), c.Param("id"), req.AssigneeID)
				if err != nil { response.Error(c, http.StatusForbidden, err.Error(), "ASSIGN_FAILED", nil); return }
				response.Success(c, http.StatusOK, "Task assigned", nil)
			})
			protected.DELETE("/tasks/:id", taskHandler.Delete)
		}
	}

	srv := &http.Server{
		Addr:    ":" + cfg.Port,
		Handler: r,
	}

	go func() {
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("listen: %s\n", err)
		}
	}()

	log.Printf("Server started on port %s", cfg.Port)

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Println("Shutting down server...")

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	if err := srv.Shutdown(ctx); err != nil {
		log.Fatal("Server forced to shutdown:", err)
	}

	log.Println("Server exiting")
}
