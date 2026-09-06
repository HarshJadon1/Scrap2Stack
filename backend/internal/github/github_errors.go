package github

import "errors"

var (
	ErrInvalidURL      = errors.New("invalid github repository url")
	ErrRepoNotFound    = errors.New("github repository not found")
	ErrRateLimit       = errors.New("github api rate limit exceeded")
	ErrUnauthorized    = errors.New("github api unauthorized")
	ErrPrivateRepo     = errors.New("private repositories are not supported yet")
)
