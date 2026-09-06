package handlers

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/scrap2stack/backend/internal/services"
	"github.com/scrap2stack/backend/pkg/response"
)

type MatchingHandler struct {
	matchingService services.MatchingService
}

func NewMatchingHandler(matchingService services.MatchingService) *MatchingHandler {
	return &MatchingHandler{
		matchingService: matchingService,
	}
}

func (h *MatchingHandler) GetMatches(c *gin.Context) {
	projectID := c.Param("id")
	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	limit, _ := strconv.Atoi(c.DefaultQuery("limit", "10"))

	matches, err := h.matchingService.GetMatchesForProject(c.Request.Context(), projectID, page, limit)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to generate matches", "MATCHING_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "Developer matches generated successfully", gin.H{
		"projectId": projectID,
		"matches":   matches,
	})
}

func (h *MatchingHandler) GetRecommendedProjects(c *gin.Context) {
	userID := c.GetString("userID")
	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	limit, _ := strconv.Atoi(c.DefaultQuery("limit", "10"))

	recommendations, err := h.matchingService.GetRecommendedProjects(c.Request.Context(), userID, page, limit)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to get recommendations", "RECOMMENDATION_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "Project recommendations retrieved", recommendations)
}
