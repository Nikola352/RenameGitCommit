package com.jetbrains.internship.rename_git_commit

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager

@Service(Service.Level.PROJECT)
class GitService {

    fun getRepository(project: Project): GitRepository? {
        return GitRepositoryManager.getInstance(project).repositories.firstOrNull()
    }

    fun getLastCommitMessage(project: Project, repository: GitRepository): String? {
        val handler = GitLineHandler(project, repository.root, GitCommand.LOG).apply {
            addParameters("-1", "--pretty=%B")
            setStdoutSuppressed(false)
        }

        return Git.getInstance().runCommand(handler)
            .getOutputOrThrow()
            .trim()
            .takeIf { it.isNotEmpty() }
    }

    fun renameLastCommit(project: Project, repository: GitRepository, newMessage: String) {
        if (newMessage.isBlank()) return

        val handler = GitLineHandler(project, repository.root, GitCommand.COMMIT).apply {
            addParameters("--amend", "-m", newMessage)
            setStdoutSuppressed(false)
        }

        Git.getInstance().runCommand(handler).throwOnError()
        repository.update()
    }
}