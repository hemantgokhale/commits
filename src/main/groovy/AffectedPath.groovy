import groovy.transform.ToString

import java.nio.file.Path

@ToString(includeNames=true, ignoreNulls=true)
class AffectedPath {
    Path path
    int linesAdded
    int linesDeleted
}
