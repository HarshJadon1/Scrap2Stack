package handlers

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/scrap2stack/backend/internal/dto"
	"github.com/scrap2stack/backend/internal/services"
	"github.com/scrap2stack/backend/pkg/response"
)

type ProjectHandler struct {
	projectService services.ProjectService
}

func NewProjectHandler(projectService services.ProjectService) *ProjectHandler {
	return &ProjectHandler{
		projectService: projectService,
	}
}

func (h *ProjectHandler) Create(c *gin.Context) {
	userID := c.GetString("userID")
	var req dto.CreateProjectRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid request", "VALIDATION_ERROR", err.Error())
		return
	}

	res, err := h.projectService.CreateProject(c.Request.Context(), userID, req)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to create project", "CREATE_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusCreated, "Project created successfully", res)
}

func (h *ProjectHandler) GetByID(c *gin.Context) {
	id := c.Param("id")
	res, err := h.projectService.GetProject(c.Request.Context(), id)
	if err != nil {
		response.Error(c, http.StatusNotFound, "Project not found", "NOT_FOUND", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "Project retrieved successfully", res)
}

func (h *ProjectHandler) List(c *gin.Context) {
	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	limit, _ := strconv.Atoi(c.DefaultQuery("limit", "10"))

	query := map[string]string{
		"search": c.Query("search"),
		"status": c.Query("status"),
	}

	res, err := h.projectService.ListProjects(c.Request.Context(), query, page, limit)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to list projects", "LIST_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "Projects retrieved successfully", res)
}

func (h *ProjectHandler) Update(c *gin.Context) {
	userID := c.GetString("userID")
	projectID := c.Param("id")
	var req dto.UpdateProjectRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid request", "VALIDATION_ERROR", err.Error())
		return
	}

	res, err := h.projectService.UpdateProject(c.Request.Context(), userID, projectID, req)
	if err != nil {
		response.Error(c, http.StatusForbidden, err.Error(), "UPDATE_FAILED", nil)
		return
	}

	response.Success(c, http.StatusOK, "Project updated successfully", res)
}

func (h *ProjectHandler) Delete(c *gin.Context) {
	userID := c.GetString("userID")
	projectID := c.Param("id")

	err := h.projectService.DeleteProject(c.Request.Context(), userID, projectID)
	if err != nil {
		response.Error(c, http.StatusForbidden, err.Error(), "DELETE_FAILED", nil)
		return
	}

	response.Success(c, http.StatusOK, "Project deleted successfully", nil)
}
