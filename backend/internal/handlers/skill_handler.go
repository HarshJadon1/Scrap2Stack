package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/scrap2stack/backend/internal/services"
	"github.com/scrap2stack/backend/pkg/response"
)

type SkillHandler struct {
	skillService services.SkillService
}

func NewSkillHandler(skillService services.SkillService) *SkillHandler {
	return &SkillHandler{
		skillService: skillService,
	}
}

func (h *SkillHandler) List(c *gin.Context) {
	res, err := h.skillService.GetSkills(c.Request.Context())
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to retrieve skills", "LIST_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "Skills retrieved successfully", res)
}
