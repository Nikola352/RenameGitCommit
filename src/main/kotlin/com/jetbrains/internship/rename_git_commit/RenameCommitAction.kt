package com.jetbrains.internship.rename_git_commit

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val gitService = project.getService(GitService::class.java)
                val repository = gitService.getRepository()

                if (repository == null) {
                    showError(project, "No Git repository found.")
                    return@launch
                }

                val lastCommitMessage = gitService.getLastCommitMessage(repository)

                if (lastCommitMessage == null) {
                    showError(project, "Could not retrieve last commit message.")
                    return@launch
                }

                val newMessage = RenameCommitDialog(project, lastCommitMessage).showAndGetMessage()

                if (!newMessage.isNullOrBlank()) {
                    try {
                        gitService.renameLastCommit(repository, newMessage)
                        Messages.showInfoMessage(project, "Commit message updated successfully", "Success")
                    } catch (e: Exception) {
                        showError(project, "Error renaming commit: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                showError(project, "Error: ${e.message}")
            }
        }
    }

    private fun showError(project: Project, message: String) {
        Messages.showErrorDialog(project, message, "Error")
    }
}