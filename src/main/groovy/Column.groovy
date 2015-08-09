/**
 * A column has and id and a width
 * A column object does not contain the values in that column. They reside in the Row object.
 */
class Column {
    final def id
    int width = 10

    Column(def columnId, int columnWidth = 0) { id = columnId; width = columnWidth }
}
