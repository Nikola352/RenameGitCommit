package com.jetbrains.internship.rename_git_commit

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.util.concurrency.annotations.RequiresEdt
import git4idea.repo.GitRepository
import javax.swing.SwingUtilities

class RenameCommitAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(event: AnActionEvent) {
        event.presentation.setEnabledAndVisible(event.project != null)
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Renaming git commit", true) {
            private var repository: GitRepository? = null
            private var lastCommitMessage: String? = null

            override fun run(indicator: com.intellij.openapi.progress.ProgressIndicator) {
                try {
                    val gitService = project.getService(GitService::class.java)
                    repository = gitService.getRepository(project)

                    if (repository == null) {
                        showErrorOnEdt(project, "No Git repository found.")
                        return
                    }

                    lastCommitMessage = gitService.getLastCommitMessage(project, repository!!)

                    if (lastCommitMessage == null) {
                        showErrorOnEdt(project, "Could not retrieve last commit message.")
                        return
                    }

                    SwingUtilities.invokeLater {
                        showRenameDialog(project, repository!!, lastCommitMessage!!)
                    }
                } catch (e: Exception) {
                    showErrorOnEdt(project, "Error: ${e.message}")
                }
            }
        })
    }

    @RequiresEdt
    private fun showRenameDialog(project: Project, repository: GitRepository, message: String) {
        val newMessage = RenameCommitDialog(project, message).showAndGetMessage()

        if (!newMessage.isNullOrBlank()) {
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Renaming commit", true) {
                override fun run(indicator: com.intellij.openapi.progress.ProgressIndicator) {
                    try {
                        val gitService = project.getService(GitService::class.java)
                        gitService.renameLastCommit(project, repository, newMessage)
                        showInfoOnEdt(project, "Commit message updated successfully")
                    } catch (e: Exception) {
                        showErrorOnEdt(project, "Error renaming commit: ${e.message}")
                    }
                }
            })
        }
    }

    private fun showErrorOnEdt(project: Project, message: String) {
        SwingUtilities.invokeLater {
            Messages.showErrorDialog(project, message, "Error")
        }
    }

    private fun showInfoOnEdt(project: Project, message: String) {
        SwingUtilities.invokeLater {
            Messages.showInfoMessage(project, message, "Success")
        }
    }
}