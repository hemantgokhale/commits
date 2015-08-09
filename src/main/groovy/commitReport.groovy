import java.nio.file.Paths
import java.text.DecimalFormat
import java.util.regex.Matcher

final String workDir = '/Users/hgokhale/work'
final List<String> releaseDates = ['150204', '150218', '150304', '150318', '150408', '150506', '150520', '150603', '150617', '150722', '150805']
repos = ['turn', 'webapp', 'quest']
interestingFolders = [
        "src/java/com/turn/platform" : "platform",
        "src/java/com/turn/quest" : "quest",
        "src/java/com/turn/product" : "product",
        "config" : "config",
        "properties" : "properties",
        "webapp" : "webapp"]


Map<String, Map<String, Integer>> byReleaseAndPath = [:]
Map<String, Map<Date, Integer>> byReleaseAndDate = [:]

/* Processing begins */
releaseDates.each {String releaseDate ->

    Map<String, Integer> byPath = [:]
    Map<String, Integer> byDate = [:]
    repos.each {String repoName ->
        List<CommitDetails> commits = getCommits(workDir, repoName, releaseDate)
        getLinesChangedByPath(commits).each {key, value ->
            byPath[key] = (byPath[key] ?: 0) + value
        }
    }
    byReleaseAndDate[releaseDate] = byDate
    byReleaseAndPath[releaseDate] = byPath

    List<String> paths = byPath.keySet().sort()
    println "Release: $releaseDate"
    print "Lines by path \t Total: ${numberFormatter.format(byPath.values().sum())} \t["
    byPath.sort{ a, b -> b.value <=> a.value }.each{key, value ->
        print "$key:${numberFormatter.format(value)} "
    }
    println "]"
}

printResults(byReleaseAndPath, byReleaseAndDate)

/* Processing ends */

void printResults(Map<String, Map<String, Integer>> byPath, Map<String, Map<Date, Integer>> byDate) {

    DecimalFormat numberFormatter = new DecimalFormat("###,###,###")

    // By Path

    // Headers
    printf ("%10s", "Release")
    def folders = interestingFolders.values()
    folders.each{ printf("%10s", it)}
    println ""
    byPath.each {releaseDate, linesByPath ->

    }

}


Map<String, Integer> getLinesChangedByPath(List<CommitDetails> commits) {

    Map<String, Integer> byPath = [:]
    repos.each {String repoName ->
        getLinesChangedByPath(commits, repoName).each {key, value ->
            byPath[key] = (byPath[key] ?: 0) + value
        }
    }
}

Map<String, Integer> getLinesChangedByPath(List<CommitDetails> commits, String repoName) {

    String OTHER = '"other"'
    Map<String, Integer> linesByPath = [:]
    interestingFolders.each {String folder, String shortName -> linesByPath[shortName] = 0}
    linesByPath[OTHER] = 0

    commits.each { CommitDetails details ->
        details.paths?.each {AffectedPath affectedPath ->
            Boolean processed = false
            if (repoName == "webapp" || repoName == "quest") {
                linesByPath[repoName] = linesByPath[repoName] + affectedPath.linesAdded + affectedPath.linesDeleted
                processed = true
            } else {
                interestingFolders.each {String folder, String shortName ->
                    if (affectedPath.path.startsWith(folder)) {
                        linesByPath[shortName] = linesByPath[shortName] + affectedPath.linesAdded + affectedPath.linesDeleted
                        processed = true
                        return
                    }
                }
            }

            if (!processed) {
                linesByPath[OTHER] = linesByPath[OTHER] + affectedPath.linesAdded + affectedPath.linesDeleted
            }
        }
    }

    linesByPath
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
            // Ignore commits by Alan and Vivian
            if (!details.author.contains("Alan Qian") && !details.author.contains("Vivian Zhang")) {
                commits << details
            }
        }
    }
    commits
}

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
