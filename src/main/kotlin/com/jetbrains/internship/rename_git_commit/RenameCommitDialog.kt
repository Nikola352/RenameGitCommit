package com.jetbrains.internship.rename_git_commit

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import javax.swing.*

class RenameCommitDialog(project: Project, lastCommitMessage: String) : DialogWrapper(project) {

    private val textArea = JTextArea(lastCommitMessage, 5, 40).apply {
        lineWrap = true
        wrapStyleWord = true
    }

    init {
        title = "Rename Commit"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        val warningLabel = JLabel("<html><b style='color:red;'>Warning:</b> This will rewrite history!</html>")
        val scrollPane = JBScrollPane(textArea)

        panel.add(warningLabel)
        panel.add(Box.createVerticalStrut(10))
        panel.add(scrollPane)

        return panel
    }

    fun showAndGetMessage(): String? {
        return if (showAndGet()) textArea.text.trim() else null
    }
}
