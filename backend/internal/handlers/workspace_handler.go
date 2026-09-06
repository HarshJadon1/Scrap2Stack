package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/scrap2stack/backend/internal/services"
	"github.com/scrap2stack/backend/pkg/response"
)

type WorkspaceHandler struct {
	workspaceService services.WorkspaceService
}

func NewWorkspaceHandler(workspaceService services.WorkspaceService) *WorkspaceHandler {
	return &WorkspaceHandler{
		workspaceService: workspaceService,
	}
}

func (h *WorkspaceHandler) GetWorkspace(c *gin.Context) {
	projectID := c.Param("id")
	ws, err := h.workspaceService.GetWorkspace(c.Request.Context(), projectID)
	if err != nil {
		response.Error(c, http.StatusNotFound, "Workspace not found", "NOT_FOUND", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "Workspace retrieved successfully", ws)
}

func (h *WorkspaceHandler) CreateWorkspace(c *gin.Context) {
	projectID := c.Param("id")
	var req struct {
		TeamID string `json:"teamId" binding:"required"`
	}

	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid request", "VALIDATION_ERROR", err.Error())
		return
	}

	ws, err := h.workspaceService.CreateWorkspace(c.Request.Context(), projectID, req.TeamID)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to create workspace", "CREATE_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusCreated, "Workspace created successfully", ws)
}

func (h *WorkspaceHandler) SyncProgress(c *gin.Context) {
	workspaceID := c.Param("id")
	progress, err := h.workspaceService.UpdateProgress(c.Request.Context(), workspaceID)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to update progress", "SYNC_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "Progress updated", gin.H{"progress": progress})
}
