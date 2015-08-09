/**
 * A table contains data organized as columns and rows.
 * Data can be added and retrieved from the table using row and column ids.
 */
class Table {
    final int INTER_COLUMN_SPACE = 2

    def name
    List<Row> rows = []

    Table(def tableName) {
        name = tableName
    }

    def get(def rowId, def columnId) {
        getRow(rowId)?.get(columnId)
    }

    void set(def rowId, def columnId, def value) {
        Row row = getOrCreateRow(rowId)
        row.set(columnId, value)
    }

    private Row getRow(def rowId) {
        rows.find { it.id == rowId }
    }

    private Row getOrCreateRow(def rowId) {
        Row row = getRow(rowId)
        if (!row) {
            row = new Row(rowId)
            rows << row
        }
        row
    }

    /**
     * Get a list of columns for this table. This is computed dynamically based on the existing row data.
     * The width of a column is set to the maximum of all widths returned by each row for that column.
     * @return
     */
    private List<Column> getColumns() {
        List<Column> columns = []
        rows.each {Row row ->
            row.getColumns().each {Column column ->
                Column existingColumn = columns.find {it.id == column.id}
                if (existingColumn) {
                    existingColumn.width = existingColumn.width < column.width ? column.width : existingColumn.width
                } else {
                    columns << column
                }
            }
        }
        columns
    }

    @Override
    String toString() {
        StringBuffer buffer = new StringBuffer()
        List<Column> columns = getColumns()

        // Table name
        buffer << sprintf('%1$s%n', name)

        if (!rows)
            return buffer.toString()

        // Space for row names
        int titleColumnWidth = getMaxRowNameWidth()
        buffer << " " * titleColumnWidth
        buffer << " " * INTER_COLUMN_SPACE

        // column headers
        columns.each { Column c ->
            String format = '%1$-' + "$c.width" + 's'
            buffer << sprintf(format, c.id)
            buffer << " " * INTER_COLUMN_SPACE
        }
        buffer << sprintf("%n", "")

        // rows
        rows.each { Row r ->
            // row name
            String format = '%1$-' + "$titleColumnWidth" + 's'
            buffer << sprintf(format, r.id)
            buffer << " " * INTER_COLUMN_SPACE

            // row data
            columns.each {Column c ->
                def value =  r.get(c.id)
                buffer << convertToString(value, c.width)
                buffer << " " * INTER_COLUMN_SPACE
            }
            buffer << sprintf("%n", "")
        }
        buffer.toString()
    }

    /**
     * Convert the provided value to a string based on the type of the value.
     * @param value is the argument to be converted.
     * @param width is the width, in number of characters, of the string returned. Type appropriate padded is expected.
     * @return a string representation
     */
    private String convertToString(def value, int width) {
        String result
        if (value instanceof Number) {
            String format = '%1$,' + "$width" + 'd'
            result = sprintf(format, value)
        } else {
            String format = '%1$-' + "$width" + 's'
            result = sprintf(format, value.toString())
        }
        result
    }

    private int getMaxRowNameWidth() {
        int width = 0
        rows.each {Row row ->
            if (row.id.size() > width) {
                width = row.id.size()
            }
        }
        width
    }
}
