package services

import (
	"context"

	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/repositories"
)

type SkillService interface {
	GetSkills(ctx context.Context) ([]models.Skill, error)
}

type skillService struct {
	skillRepo repositories.SkillRepository
}

func NewSkillService(skillRepo repositories.SkillRepository) SkillService {
	return &skillService{
		skillRepo: skillRepo,
	}
}

func (s *skillService) GetSkills(ctx context.Context) ([]models.Skill, error) {
	return s.skillRepo.List(ctx)
}
