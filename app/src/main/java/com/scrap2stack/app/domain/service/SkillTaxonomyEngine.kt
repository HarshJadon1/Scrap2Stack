package com.scrap2stack.app.domain.service

import java.util.Locale

/**
 * SkillTaxonomyEngine
 * 
 * Industrial-grade semantic taxonomy and fuzzy matching engine for developer skills and technologies.
 * Implements token-level n-gram Jaccard similarity, canonical alias mapping, and hierarchical domain clustering.
 */
object SkillTaxonomyEngine {

    // Noise suffixes to strip during normalization
    private val NOISE_TOKENS = setOf(
        "developer", "dev", "engineer", "specialist", "maintainer",
        "expert", "lead", "architect", "programmer", "designer",
        "framework", "library", "app", "application", "stack", "core", "junior", "senior"
    )

    // Canonical synonym dictionary
    private val SYNONYMS = mapOf(
        "reactjs" to "react",
        "react.js" to "react",
        "nextjs" to "next.js",
        "nodejs" to "node",
        "node.js" to "node",
        "compose" to "jetpack compose",
        "jetpack-compose" to "jetpack compose",
        "postgres" to "postgresql",
        "psql" to "postgresql",
        "k8s" to "kubernetes",
        "golang" to "go",
        "py" to "python",
        "ml" to "machine learning",
        "ai" to "artificial intelligence",
        "ts" to "typescript",
        "js" to "javascript",
        "rn" to "react native",
        "gh-actions" to "github actions",
        "ui/ux" to "ui design",
        "ui ux" to "ui design",
        "ux" to "ui design",
        "ui" to "ui design"
    )

    // Domain Taxonomy Clusters
    private val CLUSTERS = mapOf(
        "MOBILE" to setOf(
            "kotlin", "android", "jetpack compose", "swift", "ios", "swiftui",
            "flutter", "dart", "react native", "mobile ui", "coroutine", "room"
        ),
        "FRONTEND" to setOf(
            "react", "next.js", "vue", "angular", "typescript", "javascript",
            "html", "css", "tailwind", "redux", "ui design", "web", "frontend", "svelte"
        ),
        "BACKEND" to setOf(
            "node", "express", "python", "django", "fastapi", "flask", "go",
            "java", "spring", "spring boot", "rust", "c#", ".net", "graphql",
            "rest api", "backend", "microservices", "ktor"
        ),
        "AIML" to setOf(
            "python", "machine learning", "deep learning", "pytorch", "tensorflow",
            "keras", "scikit-learn", "nlp", "llm", "computer vision", "data science",
            "pandas", "numpy", "artificial intelligence"
        ),
        "DEVOPS" to setOf(
            "docker", "kubernetes", "ci/cd", "github actions", "aws", "gcp",
            "azure", "terraform", "linux", "bash", "nginx", "cloud", "devops"
        ),
        "DATABASE" to setOf(
            "postgresql", "mysql", "sqlite", "mongodb", "redis", "supabase",
            "firebase", "firestore", "sql", "prisma", "database"
        )
    )

    /**
     * Normalizes a skill or technology string into canonical form:
     * lowercased, noise-stripped, and alias-resolved.
     */
    fun normalize(input: String): String {
        val cleaned = input.lowercase(Locale.ROOT)
            .replace("-", " ")
            .replace("_", " ")
            .replace("/", " ")
            .trim()

        val tokens = cleaned.split(" ")
            .filter { it.isNotBlank() && !NOISE_TOKENS.contains(it) }

        val normalizedString = if (tokens.isNotEmpty()) tokens.joinToString(" ") else cleaned
        return SYNONYMS[normalizedString] ?: SYNONYMS[cleaned] ?: normalizedString
    }

    /**
     * Calculates semantic similarity between two skills/technologies.
     * Returns a score between 0.0 (unrelated) and 1.0 (identical or perfect synonym).
     */
    fun calculateSkillSimilarity(skillA: String, skillB: String): Double {
        val normA = normalize(skillA)
        val normB = normalize(skillB)

        // 1. Exact match
        if (normA == normB) return 1.0

        // 2. Direct synonym match
        val synA = SYNONYMS[normA] ?: normA
        val synB = SYNONYMS[normB] ?: normB
        if (synA == synB) return 1.0

        // 3. Substring inclusion (e.g. "kotlin" in "kotlin android" or vice versa)
        if (normA.contains(normB) || normB.contains(normA)) {
            val shorter = minOf(normA.length, normB.length)
            val longer = maxOf(normA.length, normB.length)
            return (0.80 + 0.15 * (shorter.toDouble() / longer.toDouble())).coerceAtMost(0.95)
        }

        // 4. Token-level Jaccard similarity
        val tokensA = normA.split(" ").filter { it.isNotBlank() }.toSet()
        val tokensB = normB.split(" ").filter { it.isNotBlank() }.toSet()
        if (tokensA.isNotEmpty() && tokensB.isNotEmpty()) {
            val intersection = tokensA.intersect(tokensB).size
            val union = tokensA.union(tokensB).size
            val jaccard = intersection.toDouble() / union.toDouble()
            if (jaccard > 0.0) {
                return (0.70 + 0.25 * jaccard).coerceAtMost(0.95)
            }
        }

        // 5. Taxonomy Cluster overlap (relatedness in same domain family)
        val clustersA = getClustersFor(normA)
        val clustersB = getClustersFor(normB)
        val sharedClusters = clustersA.intersect(clustersB)
        if (sharedClusters.isNotEmpty()) {
            // Sharing a domain cluster provides moderate affinity (0.50)
            return 0.50
        }

        return 0.0
    }

    /**
     * Finds the best matching candidate skill for a target skill.
     * Returns Pair(bestMatchingSkill, similarityScore).
     */
    fun findBestMatch(target: String, candidates: List<String>): Pair<String?, Double> {
        if (candidates.isEmpty()) return Pair(null, 0.0)

        var bestSkill: String? = null
        var maxScore = 0.0

        for (candidate in candidates) {
            val score = calculateSkillSimilarity(target, candidate)
            if (score > maxScore) {
                maxScore = score
                bestSkill = candidate
            }
            if (maxScore >= 0.98) break // Early exit on perfect match
        }

        return Pair(bestSkill, maxScore)
    }

    /**
     * Determines whether two skills are considered matching for requirement satisfaction
     * (similarity threshold >= 0.70 or direct normalized substring/equality).
     */
    fun isSkillMatch(requiredSkill: String, candidateSkill: String, threshold: Double = 0.70): Boolean {
        val normReq = normalize(requiredSkill)
        val normCand = normalize(candidateSkill)
        if (normReq == normCand) return true
        if (normReq.contains(normCand) || normCand.contains(normReq)) return true
        return calculateSkillSimilarity(requiredSkill, candidateSkill) >= threshold
    }

    /**
     * Retrieves all taxonomy clusters that contain the given term or any of its tokens.
     */
    fun getClustersFor(term: String): Set<String> {
        val norm = normalize(term)
        val tokens = norm.split(" ")
        val matchedClusters = mutableSetOf<String>()

        for ((clusterName, members) in CLUSTERS) {
            if (members.contains(norm) || members.any { m -> norm.contains(m) || m.contains(norm) }) {
                matchedClusters.add(clusterName)
            } else if (tokens.any { token -> members.contains(token) }) {
                matchedClusters.add(clusterName)
            }
        }
        return matchedClusters
    }
}
