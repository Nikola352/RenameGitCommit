package com.jetbrains.internship.rename_git_commit

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import git4idea.repo.GitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * An IntelliJ action that allows renaming the most recent Git commit in the current repository.
 *
 * This action performs several steps:
 * 1. Retrieves the current Git repository
 * 2. Gets the message from the last commit
 * 3. Shows a dialog to edit the commit message
 * 4. Performs the commit amend operation with the new message
 */
class RenameCommitAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(event: AnActionEvent) {
        event.presentation.setEnabledAndVisible(event.project != null)
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        // Create coroutine scope using default dispatcher and switch to EDT or IO dispatcher when needed
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val gitService = project.getService(GitService::class.java)
                val repository = gitService.getRepository()
                if (repository == null) {
                    showError(project, "No Git repository found.")
                    return@launch
                }

                val lastCommitMessage = gitService.getLastCommitMessage(repository)
                if (lastCommitMessage.isNullOrEmpty()) {
                    showError(project, "Could not retrieve last commit message.")
                    return@launch
                }

                showRenameDialog(project, lastCommitMessage, gitService, repository)
            } catch (e: Exception) {
                showError(project, "Error: ${e.message}")
                throw e
            }
        }
    }

    private suspend fun showRenameDialog(
        project: Project,
        lastCommitMessage: String,
        gitService: GitService,
        repository: GitRepository
    ) = withContext(Dispatchers.EDT) {
        val newMessage = RenameCommitDialog(project, lastCommitMessage).showAndGetMessage()
        if (!newMessage.isNullOrBlank()) {
            withContext(Dispatchers.Default) {
                gitService.renameLastCommit(repository, newMessage)
            }
            showSuccessMessage(project)
        }
    }

    private suspend fun showError(project: Project, message: String) = withContext(Dispatchers.EDT) {
        Messages.showErrorDialog(project, message, "Error")
    }

    private suspend fun showSuccessMessage(project: Project) = withContext(Dispatchers.EDT) {
        Messages.showInfoMessage(project, "Commit message updated successfully", "Success")
    }
}
