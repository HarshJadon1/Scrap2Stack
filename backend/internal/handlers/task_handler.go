package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/scrap2stack/backend/internal/models"
	"github.com/scrap2stack/backend/internal/repositories"
	"github.com/scrap2stack/backend/pkg/response"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type TaskHandler struct {
	taskRepo repositories.TaskRepository
}

func NewTaskHandler(taskRepo repositories.TaskRepository) *TaskHandler {
	return &TaskHandler{
		taskRepo: taskRepo,
	}
}

func (h *TaskHandler) Create(c *gin.Context) {
	projectID := c.Param("id")
	pID, _ := primitive.ObjectIDFromHex(projectID)

	var task models.Task
	if err := c.ShouldBindJSON(&task); err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid request", "VALIDATION_ERROR", err.Error())
		return
	}

	task.ProjectID = pID
	if err := h.taskRepo.Create(c.Request.Context(), &task); err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to create task", "CREATE_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusCreated, "Task created successfully", task)
}

func (h *TaskHandler) ListByProject(c *gin.Context) {
	projectID := c.Param("id")
	pID, _ := primitive.ObjectIDFromHex(projectID)

	tasks, err := h.taskRepo.ListByProjectID(c.Request.Context(), pID)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to retrieve tasks", "FETCH_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "Tasks retrieved successfully", tasks)
}

func (h *TaskHandler) Update(c *gin.Context) {
	taskID := c.Param("id")
	tID, _ := primitive.ObjectIDFromHex(taskID)

	var task models.Task
	if err := h.taskRepo.Update(c.Request.Context(), &task); err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to update task", "UPDATE_FAILED", err.Error())
		return
	}
    task.ID = tID

	response.Success(c, http.StatusOK, "Task updated successfully", task)
}

func (h *TaskHandler) Delete(c *gin.Context) {
	taskID := c.Param("id")
	tID, _ := primitive.ObjectIDFromHex(taskID)

	if err := h.taskRepo.Delete(c.Request.Context(), tID); err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to delete task", "DELETE_FAILED", err.Error())
		return
	}

	response.Success(c, http.StatusOK, "Task deleted successfully", nil)
}
