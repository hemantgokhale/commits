/**
 * A row has an id and a bunch of values. values are stored in a map (key = columnId)
 */
class Row {
    final def id
    Map<Object, Object> data = [:] // columnId -> value

    /**
     * Must specify a rowId to create a row
     * @param rowId
     */
    Row(def rowId) {id = rowId}
    Object get(def columnId) {data[columnId]}
    void set(def columnId, def value) {data[columnId] = value != null ? value : "null"}
    List<Column> getColumns() {
        List<Column> columns = []
        data.keySet().each {columnId ->
            int width = getValueWidth(data[columnId])
            int columnIdWidth = columnId.toString().size() 
            if (width < columnIdWidth) {
                width = columnIdWidth
            }
            columns << new Column(columnId, width)
        }
        columns
    }
    
    private int getValueWidth(def value) {
        value.toString().size()
    }
}
