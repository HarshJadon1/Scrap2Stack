package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/scrap2stack/backend/internal/services"
	"github.com/scrap2stack/backend/pkg/response"
)

type UserHandler struct {
	userService services.UserService
}

func NewUserHandler(userService services.UserService) *UserHandler {
	return &UserHandler{
		userService: userService,
	}
}

func (h *UserHandler) GetMe(c *gin.Context) {
	userID := c.GetString("userID")
	user, err := h.userService.GetMe(c.Request.Context(), userID)
	if err != nil {
		response.Error(c, http.StatusNotFound, "User not found", "NOT_FOUND", nil)
		return
	}

	response.Success(c, http.StatusOK, "User profile retrieved", user)
}

func (h *UserHandler) UpdateMe(c *gin.Context) {
	userID := c.GetString("userID")
	var updateData map[string]interface{}
	if err := c.ShouldBindJSON(&updateData); err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid request", "VALIDATION_ERROR", err.Error())
		return
	}

	user, err := h.userService.UpdateMe(c.Request.Context(), userID, updateData)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to update profile", "UPDATE_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "User profile updated", user)
}

func (h *UserHandler) GetByID(c *gin.Context) {
	id := c.Param("id")
	user, err := h.userService.GetUserByID(c.Request.Context(), id)
	if err != nil {
		response.Error(c, http.StatusNotFound, "User not found", "NOT_FOUND", nil)
		return
	}

	response.Success(c, http.StatusOK, "User profile retrieved", user)
}

func (h *UserHandler) GetUserProjects(c *gin.Context) {
	// Placeholder for Phase 3
	response.Success(c, http.StatusOK, "User projects retrieved", []interface{}{})
}

func (h *UserHandler) GetUserContributions(c *gin.Context) {
	// Placeholder for Phase 3
	response.Success(c, http.StatusOK, "User contributions retrieved", []interface{}{})
}
