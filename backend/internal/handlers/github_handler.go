package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/scrap2stack/backend/internal/services"
	"github.com/scrap2stack/backend/pkg/response"
)

type GitHubHandler struct {
	importService services.ImportService
}

func NewGitHubHandler(importService services.ImportService) *GitHubHandler {
	return &GitHubHandler{
		importService: importService,
	}
}

func (h *GitHubHandler) ImportRepository(c *gin.Context) {
	userID := c.GetString("userID")
	var req struct {
		RepositoryURL string `json:"repositoryUrl" binding:"required,url"`
	}

	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid request", "VALIDATION_ERROR", err.Error())
		return
	}

	project, analysis, err := h.importService.ImportFromGitHub(c.Request.Context(), userID, req.RepositoryURL)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to import repository", "IMPORT_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusCreated, "Repository imported and analyzed successfully", gin.H{
		"project":  project,
		"analysis": analysis,
	})
}
