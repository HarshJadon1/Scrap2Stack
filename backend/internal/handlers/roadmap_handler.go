package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/services"
	"github.com/scrap2stack/backend/pkg/response"
)

type RoadmapHandler struct {
	roadmapService services.RoadmapService
}

func NewRoadmapHandler(roadmapService services.RoadmapService) *RoadmapHandler {
	return &RoadmapHandler{
		roadmapService: roadmapService,
	}
}

func (h *RoadmapHandler) GetRoadmap(c *gin.Context) {
	projectID := c.Param("id")
	roadmap, items, err := h.roadmapService.GetRoadmap(c.Request.Context(), projectID)
	if err != nil {
		response.Error(c, http.StatusNotFound, "Roadmap not found", "NOT_FOUND", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "Roadmap retrieved successfully", gin.H{
		"roadmap": roadmap,
		"items":   items,
	})
}

func (h *RoadmapHandler) GenerateAI(c *gin.Context) {
	projectID := c.Param("id")
	roadmap, items, err := h.roadmapService.GenerateAIRoadmap(c.Request.Context(), projectID)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to generate AI roadmap", "GENERATION_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusCreated, "AI Roadmap generated successfully", gin.H{
		"roadmap": roadmap,
		"items":   items,
	})
}

func (h *RoadmapHandler) AddItem(c *gin.Context) {
	userID := c.GetString("userID")
	var item models.RoadmapItem
	if err := c.ShouldBindJSON(&item); err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid request", "VALIDATION_ERROR", err.Error())
		return
	}

	err := h.roadmapService.AddRoadmapItem(c.Request.Context(), userID, &item)
	if err != nil {
		response.Error(c, http.StatusForbidden, err.Error(), "ADD_FAILED", nil)
		return
	}

	response.Success(c, http.StatusCreated, "Roadmap item added", item)
}
