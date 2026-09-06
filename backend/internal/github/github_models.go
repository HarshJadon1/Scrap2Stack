package github

import "time"

type GitHubRepoResponse struct {
	ID              int64     `json:"id"`
	Name            string    `json:"name"`
	FullName        string    `json:"full_name"`
	Owner           Owner     `json:"owner"`
	HTMLURL         string    `json:"html_url"`
	Description     string    `json:"description"`
	CreatedAt       time.Time `json:"created_at"`
	UpdatedAt       time.Time `json:"updated_at"`
	PushedAt        time.Time `json:"pushed_at"`
	StargazersCount int       `json:"stargazers_count"`
	ForksCount      int       `json:"forks_count"`
	OpenIssuesCount int       `json:"open_issues_count"`
	DefaultBranch   string    `json:"default_branch"`
	License         License   `json:"license"`
}

type Owner struct {
	Login string `json:"login"`
}

type License struct {
	Name string `json:"name"`
}

type GitHubImportResult struct {
	GitHubID      int64          `json:"githubId"`
	Owner         string         `json:"owner"`
	Name          string         `json:"name"`
	FullName      string         `json:"fullName"`
	Description   string         `json:"description"`
	URL           string         `json:"url"`
	Languages     map[string]int `json:"languages"`
	README        string         `json:"readme"`
	LastCommit    time.Time      `json:"lastCommit"`
	OpenIssues    int            `json:"openIssues"`
	Stars         int            `json:"stars"`
	Forks         int            `json:"forks"`
}
