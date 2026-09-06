package services

import (
	"context"
	"errors"
	"time"

	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/repositories"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type TaskService interface {
	CreateTask(ctx context.Context, creatorID string, task *models.Task) error
	GetTasksByProject(ctx context.Context, projectID string) ([]models.Task, error)
	UpdateTaskStatus(ctx context.Context, userID, taskID string, status models.TaskStatus) error
	AssignTask(ctx context.Context, leadID, taskID, assigneeID string) error
	DeleteTask(ctx context.Context, leadID, taskID string) error
}

type taskService struct {
	taskRepo    repositories.TaskRepository
	projectRepo repositories.ProjectRepository
	teamRepo    repositories.TeamRepository
	notifService NotificationService
}

func NewTaskService(
	tRepo repositories.TaskRepository,
	pRepo repositories.ProjectRepository,
	tmRepo repositories.TeamRepository,
	nService NotificationService,
) TaskService {
	return &taskService{
		taskRepo:    tRepo,
		projectRepo: pRepo,
		teamRepo:    tmRepo,
		notifService: nService,
	}
}

func (s *taskService) CreateTask(ctx context.Context, creatorID string, task *models.Task) error {
	cID, _ := primitive.ObjectIDFromHex(creatorID)
	task.CreatedBy = cID
	task.CreatedAt = time.Now()
	task.UpdatedAt = time.Now()

	if task.Status == "" {
		task.Status = models.TaskStatusTodo
	}

	return s.taskRepo.Create(ctx, task)
}

func (s *taskService) GetTasksByProject(ctx context.Context, projectID string) ([]models.Task, error) {
	pID, _ := primitive.ObjectIDFromHex(projectID)
	return s.taskRepo.ListByProjectID(ctx, pID)
}

func (s *taskService) UpdateTaskStatus(ctx context.Context, userID, taskID string, status models.TaskStatus) error {
	tID, _ := primitive.ObjectIDFromHex(taskID)
	task, err := s.taskRepo.GetByID(ctx, tID)
	if err != nil {
		return err
	}

	// Logic to check if user can update status
	// Usually assignee or project lead
	if task.AssigneeID != nil && task.AssigneeID.Hex() != userID {
		// Check if user is project lead
		team, _ := s.teamRepo.GetByProjectID(ctx, task.ProjectID)
		if team == nil || team.OwnerID.Hex() != userID {
			return errors.New("unauthorized to update this task")
		}
	}

	task.Status = status
	task.UpdatedAt = time.Now()
	if status == models.TaskStatusCompleted {
		now := time.Now()
		task.CompletedAt = &now
	}

	return s.taskRepo.Update(ctx, task)
}

func (s *taskService) AssignTask(ctx context.Context, leadID, taskID, assigneeID string) error {
	tID, _ := primitive.ObjectIDFromHex(taskID)
	aID, _ := primitive.ObjectIDFromHex(assigneeID)

	task, err := s.taskRepo.GetByID(ctx, tID)
	if err != nil {
		return err
	}

	// Verify leadID is project lead
	team, _ := s.teamRepo.GetByProjectID(ctx, task.ProjectID)
	if team == nil || team.OwnerID.Hex() != leadID {
		return errors.New("only project lead can assign tasks")
	}

	task.AssigneeID = &aID
	task.UpdatedAt = time.Now()

	err = s.taskRepo.Update(ctx, task)
	if err == nil {
		s.notifService.Notify(ctx, assigneeID, models.NotifTaskAssigned, "New Task Assigned", "A new task has been assigned to you: "+task.Title)
	}
	return err
}

func (s *taskService) DeleteTask(ctx context.Context, leadID, taskID string) error {
	tID, _ := primitive.ObjectIDFromHex(taskID)
	task, err := s.taskRepo.GetByID(ctx, tID)
	if err != nil {
		return err
	}

	team, _ := s.teamRepo.GetByProjectID(ctx, task.ProjectID)
	if team == nil || team.OwnerID.Hex() != leadID {
		return errors.New("only project lead can delete tasks")
	}

	return s.taskRepo.Delete(ctx, tID)
}
