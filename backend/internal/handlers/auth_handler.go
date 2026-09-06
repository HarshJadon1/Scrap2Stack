package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/scrap2stack/backend/internal/dto"
	"github.com/scrap2stack/backend/internal/services"
	"github.com/scrap2stack/backend/pkg/response"
)

type AuthHandler struct {
	authService services.AuthService
}

func NewAuthHandler(authService services.AuthService) *AuthHandler {
	return &AuthHandler{
		authService: authService,
	}
}

func (h *AuthHandler) Register(c *gin.Context) {
	var req dto.RegisterRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid request", "VALIDATION_ERROR", err.Error())
		return
	}

	res, err := h.authService.Register(c.Request.Context(), req)
	if err != nil {
		response.Error(c, http.StatusConflict, err.Error(), "REGISTRATION_FAILED", nil)
		return
	}

	response.Success(c, http.StatusCreated, "User registered successfully", res)
}

func (h *AuthHandler) Login(c *gin.Context) {
	var req dto.LoginRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid request", "VALIDATION_ERROR", err.Error())
		return
	}

	res, err := h.authService.Login(c.Request.Context(), req)
	if err != nil {
		response.Error(c, http.StatusUnauthorized, err.Error(), "LOGIN_FAILED", nil)
		return
	}

	response.Success(c, http.StatusOK, "Login successful", res)
}
