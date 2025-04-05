# RenameGitCommit

A plugin for IntelliJ IDEA that simplifies the process of renaming Git commit messages after they've been committed.

## Features

- Adds a "Rename current commit" action in the Git section of the main toolbar
- Provides a user-friendly dialog to enter a new commit message
- Uses Git amend functionality under the hood for a clean commit history
- Integrates seamlessly with IntelliJ IDEA's UI.

## Installation

You can install the plugin using one of two methods:

### Option 1: Install from ZIP

- Download the ZIP archive from the releases section
- In IntelliJ IDEA, go to Settings/Preferences → Plugins → ⚙️ → Install Plugin from Disk...
- Select the downloaded ZIP file

### Option 2: Run from Source

- Clone the repository
- Open the project in IntelliJ IDEA
- Run the plugin using ./gradlew runIde

## Usage

- Make a Git commit
- Click on the "Rename current commit" action in the Git section of the main toolbar
- Enter your new commit message in the dialog
- Click OK to apply the changes

### Rewriting history

The plugin does not check if the commit has been pushed to a remote for performance reasons. Note that this command does rewrite history and a force push will be required afterwards.

## How it Works

The plugin leverages IntelliJ's Git4Idea API to perform a Git amend operation that preserves the commit's content while updating its message. This approach ensures your commit history remains clean while allowing you to make corrections to commit messages.

## Requirements

- IntelliJ IDEA 20XX.X or later
- Git installed and configured

## Future Plans

- Support for batch renaming of multiple commits
- Integration with commit history view

## Feedback and Contributions

Feedback and contributions are welcome! Please feel free to submit issues or pull requests on the GitHub repository.

## License

This project is licensed under the MIT License - see the [LICENSE](https://github.com/Nikola352/RenameGitCommit/blob/main/LICENSE) file for details.