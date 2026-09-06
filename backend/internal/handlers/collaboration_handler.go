package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/services"
	"github.com/scrap2stack/backend/pkg/response"
)

type CollaborationHandler struct {
	collabService services.CollaborationService
}

func NewCollaborationHandler(collabService services.CollaborationService) *CollaborationHandler {
	return &CollaborationHandler{
		collabService: collabService,
	}
}

func (h *CollaborationHandler) CreateRequest(c *gin.Context) {
	senderID := c.GetString("userID")
	projectID := c.Param("id")

	var req struct {
		ReceiverID   string          `json:"receiverId" binding:"required"`
		ProposedRole models.TeamRole `json:"proposedRole" binding:"required"`
		Message      string          `json:"message"`
	}

	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid request", "VALIDATION_ERROR", err.Error())
		return
	}

	res, err := h.collabService.CreateRequest(c.Request.Context(), senderID, projectID, req.ReceiverID, req.ProposedRole, req.Message)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, err.Error(), "CREATE_FAILED", nil)
		return
	}

	response.Success(c, http.StatusCreated, "Collaboration request sent", res)
}

func (h *CollaborationHandler) GetReceivedRequests(c *gin.Context) {
	userID := c.GetString("userID")
	res, err := h.collabService.GetReceivedRequests(c.Request.Context(), userID)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to retrieve requests", "FETCH_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "Requests retrieved successfully", res)
}

func (h *CollaborationHandler) AcceptRequest(c *gin.Context) {
	userID := c.GetString("userID")
	requestID := c.Param("id")

	err := h.collabService.AcceptRequest(c.Request.Context(), userID, requestID)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, err.Error(), "ACCEPT_FAILED", nil)
		return
	}

	response.Success(c, http.StatusOK, "Request accepted successfully", nil)
}

func (h *CollaborationHandler) RejectRequest(c *gin.Context) {
	userID := c.GetString("userID")
	requestID := c.Param("id")

	err := h.collabService.RejectRequest(c.Request.Context(), userID, requestID)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, err.Error(), "REJECT_FAILED", nil)
		return
	}

	response.Success(c, http.StatusOK, "Request rejected successfully", nil)
}
