package github

import (
	"encoding/json"
	"fmt"
	"net/http"
	"time"
)

type GitHubClient interface {
	GetRepository(owner, repo string) (*GitHubRepoResponse, error)
	GetLanguages(owner, repo string) (map[string]int, error)
	GetREADME(owner, repo string) (string, error)
}

type githubClient struct {
	httpClient *http.Client
	token      string
}

func NewGitHubClient(token string) GitHubClient {
	return &githubClient{
		httpClient: &http.Client{Timeout: 10 * time.Second},
		token:      token,
	}
}

func (c *githubClient) GetRepository(owner, repo string) (*GitHubRepoResponse, error) {
	url := fmt.Sprintf("https://api.github.com/repos/%s/%s", owner, repo)
	req, _ := http.NewRequest("GET", url, nil)
	c.setHeaders(req)

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode == http.StatusNotFound {
		return nil, fmt.Errorf("repository not found")
	}
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("github api error: %d", resp.StatusCode)
	}

	var result GitHubRepoResponse
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return nil, err
	}

	return &result, nil
}

func (c *githubClient) GetLanguages(owner, repo string) (map[string]int, error) {
	url := fmt.Sprintf("https://api.github.com/repos/%s/%s/languages", owner, repo)
	req, _ := http.NewRequest("GET", url, nil)
	c.setHeaders(req)

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	var result map[string]int
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return nil, err
	}

	return result, nil
}

func (c *githubClient) GetREADME(owner, repo string) (string, error) {
	url := fmt.Sprintf("https://api.github.com/repos/%s/%s/readme", owner, repo)
	req, _ := http.NewRequest("GET", url, nil)
	req.Header.Set("Accept", "application/vnd.github.v3.raw")
	c.setHeaders(req)

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return "", nil // README not found or error
	}

	var content string
	// Simplified README retrieval for Phase 4
	// In a real scenario, we might want to handle encoding or large files
	buf := make([]byte, 10000) // Read first 10k bytes
	n, _ := resp.Body.Read(buf)
	content = string(buf[:n])

	return content, nil
}

func (c *githubClient) setHeaders(req *http.Request) {
	req.Header.Set("User-Agent", "Scrap2Stack-Backend")
	if c.token != "" {
		req.Header.Set("Authorization", "token "+c.token)
	}
}
