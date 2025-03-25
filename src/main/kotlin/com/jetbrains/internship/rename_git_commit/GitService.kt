package com.jetbrains.internship.rename_git_commit

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service for performing Git operations related to commit renaming.
 *
 * This service provides functionality to:
 * - Get the current Git repository
 * - Retrieve the last commit message
 * - Rename the last commit
 */
@Service(Service.Level.PROJECT)
class GitService(private val project: Project) {

    /**
    * Gets the first available Git repository for the project.
    *
    * @return The Git repository or null if none found
    */
    fun getRepository(): GitRepository? {
        return GitRepositoryManager.getInstance(project).repositories.firstOrNull()
    }

    /**
     * Gets the message from the last commit in the repository.
     *
     * @param repository The Git repository to query
     * @return The last commit message or null if not available
     */
    suspend fun getLastCommitMessage(repository: GitRepository): String? {
        val handler = GitLineHandler(project, repository.root, GitCommand.LOG).apply {
            addParameters("-1", "--pretty=%B")
            setStdoutSuppressed(false)
        }

        return withContext(Dispatchers.IO) {
            Git.getInstance()
                .runCommand(handler)
                .getOutputOrThrow()
                .trim().takeIf { it.isNotEmpty() }
        }
    }

    /**
     * Renames the last commit in the repository with a new message.
     *
     * @param repository The Git repository to modify
     * @param newMessage The new commit message
     * @throws Exception if the Git operation fails
     */
    suspend fun renameLastCommit(repository: GitRepository, newMessage: String) {
        if (newMessage.isBlank()) return

        val handler = GitLineHandler(project, repository.root, GitCommand.COMMIT).apply {
            addParameters("--amend", "-m", newMessage)
            setStdoutSuppressed(false)
        }

        withContext(Dispatchers.IO) {
            Git.getInstance().runCommand(handler).throwOnError()
            repository.update()
        }
    }
}