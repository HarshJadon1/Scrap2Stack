package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/scrap2stack/backend/internal/services"
	"github.com/scrap2stack/backend/pkg/response"
)

type ContributionHandler struct {
	contributionService services.ContributionService
}

func NewContributionHandler(contributionService services.ContributionService) *ContributionHandler {
	return &ContributionHandler{
		contributionService: contributionService,
	}
}

func (h *ContributionHandler) SyncGitHub(c *gin.Context) {
	projectID := c.Param("id")
	err := h.contributionService.SyncGitHubActivity(c.Request.Context(), projectID)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to sync GitHub activity", "SYNC_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "GitHub activity synced successfully", nil)
}

func (h *ContributionHandler) GetMyContributions(c *gin.Context) {
	userID := c.GetString("userID")
	contributions, err := h.contributionService.GetContributions(c.Request.Context(), userID)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to retrieve contributions", "FETCH_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "Contributions retrieved successfully", contributions)
}
