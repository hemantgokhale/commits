import groovy.transform.ToString

import java.util.regex.Matcher

@ToString(includeNames=true, ignoreNulls=true)
class CommitDetails {
    String commitId
    String author
    Date date
    List<AffectedPath> paths
    Boolean mergeCommit
    List<String> parentCommits // populated only if this is a merge commit
    String repo
    String releaseDate
}
