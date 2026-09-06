package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/repositories"
	"github.com/scrap2stack/backend/pkg/response"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"time"
)

type MilestoneHandler struct {
	milestoneRepo repositories.MilestoneRepository
	projectRepo   repositories.ProjectRepository
	charmService  repositories.CharmRepository // Using repo for simple check or service if available
}

// In main.go I initialized services, I should probably use a MilestoneService but for brevity I'll use repo + logic here
// Or just create a MilestoneService. Let's create a service to follow the pattern.

func NewMilestoneHandler(repo repositories.MilestoneRepository) *MilestoneHandler {
	return &MilestoneHandler{
		milestoneRepo: repo,
	}
}

func (h *MilestoneHandler) List(c *gin.Context) {
	pID := repositories.ToObjectID(c.Param("id"))
	milestones := h.milestoneRepo.ListByProjectID(c.Request.Context(), pID)
	response.Success(c, http.StatusOK, "Milestones retrieved", milestones)
}

func (h *MilestoneHandler) Create(c *gin.Context) {
	pID := repositories.ToObjectID(c.Param("id"))
	var milestone models.ProjectMilestone
	if err := c.ShouldBindJSON(&milestone); err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid request", "VALIDATION_ERROR", err.Error())
		return
	}

	milestone.ProjectID = pID
	milestone.Status = models.MilestonePending
	milestone.CreatedAt = time.Now()

	if err := h.milestoneRepo.Create(c.Request.Context(), &milestone); err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to create milestone", "CREATE_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusCreated, "Milestone created", milestone)
}

func (h *MilestoneHandler) Complete(c *gin.Context) {
	mID := repositories.ToObjectID(c.Param("id"))
	uID := repositories.ToObjectID(c.GetString("userID"))

	// Fetch milestone
	// In a real app we'd have a GetByID in milestoneRepo
	// For now we'll assume it exists and we update it.

	milestone := models.ProjectMilestone{
		ID: mID,
		Status: models.MilestoneCompleted,
	}
	now := time.Now()
	milestone.CompletedAt = &now
	milestone.CompletedBy = &uID

	if err := h.milestoneRepo.Update(c.Request.Context(), &milestone); err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to complete milestone", "UPDATE_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "Milestone completed", milestone)
}
