package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/scrap2stack/backend/internal/services"
	"github.com/scrap2stack/backend/pkg/response"
)

type RevivalHandler struct {
	revivalService services.RevivalScoreService
	projectService services.ProjectService
}

func NewRevivalHandler(revivalService services.RevivalScoreService, projectService services.ProjectService) *RevivalHandler {
	return &RevivalHandler{
		revivalService: revivalService,
		projectService: projectService,
	}
}

func (h *RevivalHandler) GetScore(c *gin.Context) {
	projectID := c.Param("id")
	project, err := h.projectService.GetProject(c.Request.Context(), projectID)
	if err != nil {
		response.Error(c, http.StatusNotFound, "Project not found", "NOT_FOUND", nil)
		return
	}

	res, err := h.revivalService.CalculateScore(c.Request.Context(), project)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to calculate revival score", "CALCULATION_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "Revival score calculated successfully", res)
}
