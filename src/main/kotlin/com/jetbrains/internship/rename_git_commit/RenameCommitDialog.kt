package com.jetbrains.internship.rename_git_commit

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.*
import javax.swing.border.CompoundBorder

class RenameCommitDialog(project: Project, lastCommitMessage: String) : DialogWrapper(project) {

    private val textArea = JTextArea(lastCommitMessage, 5, 40).apply {
        lineWrap = true
        wrapStyleWord = true
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(JBUI.CurrentTheme.CustomFrameDecorations.paneBackground()),
            JBUI.Borders.empty(5)
        )
        font = JBUI.Fonts.create("Default", 14)
    }

    private val characterCountLabel = JBLabel("Characters: ${lastCommitMessage.length}").apply {
        foreground = JBUI.CurrentTheme.Label.foreground()
    }

    init {
        title = "Rename Commit"
        isResizable = true
        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout())
        mainPanel.preferredSize = Dimension(500, 300)

        // Warning Panel
        val warningPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            border = BorderFactory.createEmptyBorder(0, 0, 10, 0)

            val warningIcon = JLabel(UIManager.getIcon("OptionPane.warningIcon"))
            val warningLabel = JBLabel("<html><b>Warning:</b> This will rewrite git history!</html>").apply {
                foreground = Color.YELLOW.darker()
            }

            add(warningIcon)
            add(Box.createHorizontalStrut(10))
            add(warningLabel)
            add(Box.createHorizontalGlue())
        }

        // Scroll Pane for Text Area
        val scrollPane = JBScrollPane(textArea).apply {
            border = CompoundBorder(
                BorderFactory.createLineBorder(JBUI.CurrentTheme.CustomFrameDecorations.paneBackground()),
                JBUI.Borders.empty(5)
            )
        }

        // Character Count Panel
        val characterCountPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.emptyTop(5)
            add(characterCountLabel, BorderLayout.WEST)
        }

        // Assemble Main Panel
        mainPanel.add(warningPanel, BorderLayout.NORTH)
        mainPanel.add(scrollPane, BorderLayout.CENTER)
        mainPanel.add(characterCountPanel, BorderLayout.SOUTH)

        // Add listener for character count
        textArea.document.addDocumentListener(object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) {
                updateCharacterCount()
            }

            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) {
                updateCharacterCount()
            }

            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) {
                updateCharacterCount()
            }
        })

        return mainPanel
    }

    private fun updateCharacterCount() {
        characterCountLabel.text = "Characters: ${textArea.text.length}"
    }

    override fun doValidate(): ValidationInfo? {
        return if (textArea.text.isBlank()) {
            ValidationInfo("Commit message cannot be empty", textArea)
        } else if (textArea.text.length > 72) {
            ValidationInfo("Commit message should be 72 characters or less", textArea)
        } else {
            null
        }
    }

    fun showAndGetMessage(): String? {
        return if (showAndGet()) textArea.text.trim() else null
    }
}
