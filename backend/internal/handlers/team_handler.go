package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/services"
	"github.com/scrap2stack/backend/pkg/response"
)

type TeamHandler struct {
	teamService services.TeamService
}

func NewTeamHandler(teamService services.TeamService) *TeamHandler {
	return &TeamHandler{
		teamService: teamService,
	}
}

func (h *TeamHandler) Create(c *gin.Context) {
	ownerID := c.GetString("userID")
	var req struct {
		ProjectID string `json:"projectId" binding:"required"`
		Name      string `json:"name" binding:"required"`
	}

	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid request", "VALIDATION_ERROR", err.Error())
		return
	}

	res, err := h.teamService.CreateTeam(c.Request.Context(), ownerID, req.ProjectID, req.Name)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to create team", "CREATE_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusCreated, "Team created successfully", res)
}

func (h *TeamHandler) GetByID(c *gin.Context) {
	id := c.Param("id")
	res, err := h.teamService.GetTeam(c.Request.Context(), id)
	if err != nil {
		response.Error(c, http.StatusNotFound, "Team not found", "NOT_FOUND", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "Team retrieved successfully", res)
}

func (h *TeamHandler) GetMembers(c *gin.Context) {
	id := c.Param("id")
	res, err := h.teamService.GetMembers(c.Request.Context(), id)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to retrieve members", "FETCH_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "Members retrieved successfully", res)
}

func (h *TeamHandler) AddMember(c *gin.Context) {
	teamID := c.Param("id")
	var req struct {
		UserID string          `json:"userId" binding:"required"`
		Role   models.TeamRole `json:"role" binding:"required"`
	}

	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid request", "VALIDATION_ERROR", err.Error())
		return
	}

	err := h.teamService.AddMember(c.Request.Context(), teamID, req.UserID, req.Role)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to add member", "ADD_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "Member added successfully", nil)
}

func (h *TeamHandler) ChangeRole(c *gin.Context) {
	leadID := c.GetString("userID")
	teamID := c.Param("id")
	userID := c.Param("userId")
	var req struct {
		Role models.TeamRole `json:"role" binding:"required"`
	}

	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid request", "VALIDATION_ERROR", err.Error())
		return
	}

	err := h.teamService.ChangeMemberRole(c.Request.Context(), leadID, teamID, userID, req.Role)
	if err != nil {
		response.Error(c, http.StatusForbidden, err.Error(), "ROLE_CHANGE_FAILED", nil)
		return
	}

	response.Success(c, http.StatusOK, "Role changed successfully", nil)
}

func (h *TeamHandler) RemoveMember(c *gin.Context) {
	ownerID := c.GetString("userID")
	teamID := c.Param("id")
	userID := c.Param("userId")

	err := h.teamService.RemoveMember(c.Request.Context(), ownerID, teamID, userID)
	if err != nil {
		response.Error(c, http.StatusForbidden, err.Error(), "REMOVE_FAILED", nil)
		return
	}

	response.Success(c, http.StatusOK, "Member removed successfully", nil)
}

func (h *TeamHandler) GetCoverage(c *gin.Context) {
	projectID := c.Param("id")
	coverage, err := h.teamService.CalculateTeamCoverage(c.Request.Context(), projectID)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to calculate coverage", "FETCH_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "Team coverage retrieved", coverage)
}
