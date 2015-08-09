import java.nio.file.Paths
import java.util.regex.Matcher

final String workDir = '/Users/hgokhale/work'
final List<String> releaseDates = ['150204', '150218', '150304', '150318', '150408', '150506', '150520', '150603', '150617', '150722', '150805']
// final List<String> releaseDates = [ '150506', '150520', '150603', '150617', '150722', '150805']
final List<String> repoNames = ['turn', 'webapp', 'quest']

interestingFolders = [
        "src/java/com/turn/platform" : "platform",
        "src/java/com/turn/quest" : "quest",
        "src/java/com/turn/product" : "product",
        "config" : "config",
        "properties" : "properties",
        "webapp" : "webapp"]


/* Processing begins */
List<CommitDetails> commits = getAllCommits(releaseDates, repoNames, workDir)
printCommitCountByRepo(commits)

/* Processing ends */

/**
 * Print a table showing number of commits by release and repo
 * @param commits is the complete list of commits
 */
void printCommitCountByRepo(List<CommitDetails> commits) {
    Table commitsByRepo = new Table("Number of commits merged")
    
    commits.each {CommitDetails c ->
        int currentValue = commitsByRepo.get(c.releaseDate, c.repo) ?: 0
        commitsByRepo.set(c.releaseDate, c.repo, currentValue + 1)
    }

    // Generate the totals
    def rows = commitsByRepo.getRowIds()
    def columns = commitsByRepo.getColumnIds() 
    rows.each { rowId ->
        int total = 0
        columns.each {columnId ->
            total += commitsByRepo.get(rowId, columnId)
        }
        commitsByRepo.set(rowId, "total", total)
    }
    println commitsByRepo.toString()
}

/**
 * Get a list of commits that came into a release branch after it was created.
 * @param releaseDates is a list of releases we are interested in
 * @param repoNames is the list of repos we are interested in
 * @param workDir is the full path where the repos are located
 * @return a list of commits
 */
List<CommitDetails> getAllCommits(List<String> releaseDates, List<String> repoNames, String workDir) {
    List<CommitDetails> commits = []
    releaseDates.each {String releaseDate ->
        repoNames.each {String repoName ->
            commits.addAll(getCommits(workDir, repoName, releaseDate))
        }
    }
    commits
}

/**
 * Get a list of commits that came into a release branch after it was created
 * @param workspaceDir is the path of the workspace dir. Individual repos are subdirectories of this dir
 * @param repoName is the name of the repo e.g. turn, webapp
 * @param releaseDate identifies the release we are interested in
 * @return a list of commits
 */
List<CommitDetails> getCommits(String workspaceDir, String repoName, String releaseDate) {
    String workingDir = "$workspaceDir/$repoName"
    String releaseTag = "$repoName-$releaseDate-cf"
    String branchName = "origin/release/$releaseDate"

    List<CommitDetails> commits = []
    String gitLog = ShellCommand.run("git log $releaseTag..$branchName --no-merges --numstat --format=medium --date=short", workingDir)
    gitLog.split("(?m)^commit ").each {
        if (it) {
            CommitDetails details = parseCommitDetails(it)
            details.repo = repoName
            details.releaseDate = releaseDate
            
            // Ignore commits by Alan and Vivian
            if (!details.author.contains("Alan Qian") && !details.author.contains("Vivian Zhang")) {
                commits << details
            }
        }
    }
    commits
}

/**
 * Parse the output of 'git log' and extract commit details
 * @param logOutput is the output to be parsed
 * @return a CommitDetails object
 */
CommitDetails parseCommitDetails(String logOutput) {

    CommitDetails details = new CommitDetails()

    // Commit ID
    details.commitId = logOutput[0..39]

    // Capture parent commits if this is a merge commit
    Matcher matcher = logOutput =~ /Merge: (\w{7}) (\w{7})/
    if (matcher) {
        details.mergeCommit = true
        details.parentCommits = []
        details.parentCommits << matcher[0][1]
        details.parentCommits << matcher[0][2]
    }

    // Author
    matcher = logOutput =~ /Author: (.*)/
    assert matcher
    details.author = matcher[0][1]

    // Date
    matcher = logOutput =~ /Date:\s+(.*)/
    assert matcher
    details.date = Date.parse("yyyy-MM-dd", matcher[0][1])

    // Paths affected by this commit. Group 0 = lines added, Group 1 = lines deleted, Group 2 = path
    matcher = logOutput =~ /(?m)^(\d+)\s(\d+)\s(.*)/
    if (matcher) {
        details.paths = []
        matcher.each {
            details.paths << new AffectedPath(path:Paths.get(it[3]), linesAdded: it[1] as Integer, linesDeleted: it[2] as Integer)
        }
    }

    details
}
