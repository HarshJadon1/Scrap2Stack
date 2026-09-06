package response

import (
	"github.com/gin-gonic/gin"
)

type Response struct {
	Success bool        `json:"success"`
	Message string      `json:"message"`
	Data    interface{} `json:"data,omitempty"`
	Error   *ErrorData  `json:"error,omitempty"`
}

type ErrorData struct {
	Code    string      `json:"code"`
	Details interface{} `json:"details,omitempty"`
}

func Success(c *gin.Context, statusCode int, message string, data interface{}) {
	c.JSON(statusCode, Response{
		Success: true,
		Message: message,
		Data:    data,
	})
}

func Error(c *gin.Context, statusCode int, message string, errorCode string, details interface{}) {
	c.JSON(statusCode, Response{
		Success: false,
		Message: message,
		Error: &ErrorData{
			Code:    errorCode,
			Details: details,
		},
	})
}
