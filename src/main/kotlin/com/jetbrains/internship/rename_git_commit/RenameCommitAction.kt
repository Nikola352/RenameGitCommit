package com.jetbrains.internship.rename_git_commit

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import git4idea.repo.GitRepository

class RenameCommitAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Renaming git commit", true) {
            private var repository: GitRepository? = null
            private var lastCommitMessage: String? = null
            private var newMessage: String? = null

            override fun run(indicator: com.intellij.openapi.progress.ProgressIndicator) {
                try {
                    val gitService = project.getService(GitService::class.java)
                    repository = gitService.getRepository(project) ?: return

                    lastCommitMessage = gitService.getLastCommitMessage(project, repository!!)
                } catch (e: Exception) {
                    Messages.showErrorDialog(project, "Error getting commit message: ${e.message}", "Error")
                }
            }

            override fun onSuccess() {
                if (repository == null) {
                    Messages.showErrorDialog(project, "No Git repository found.", "Error")
                    return
                }

                if (lastCommitMessage == null) {
                    Messages.showErrorDialog(project, "Could not retrieve last commit message.", "Error")
                    return
                }

                newMessage = RenameCommitDialog(project, lastCommitMessage!!).showAndGetMessage()

                if (!newMessage.isNullOrBlank()) {
                    ProgressManager.getInstance().run(object : Backgroundable(project, "Renaming commit", true) {
                        override fun run(indicator: com.intellij.openapi.progress.ProgressIndicator) {
                            try {
                                val gitService = project.getService(GitService::class.java)
                                gitService.renameLastCommit(project, repository!!, newMessage!!)
                            } catch (e: Exception) {
                                Messages.showErrorDialog(project, "Error renaming commit: ${e.message}", "Error")
                            }
                        }
                    })
                }
            }
        })
    }
}