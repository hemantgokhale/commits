import spock.lang.Specification

class TableTest extends Specification {

    def "empty"() {
        Table table = new Table("Table Name")
        def expectedValue = """\
Table Name
"""

        expect:
        table.toString() == expectedValue
    }

    def "one string value"() {
        Table table = new Table("Table Name")
        table.set("Row Name", "Column Name", "Value")

        def expectedValue = """\
Table Name
          Column Name  
Row Name  Value        
"""

        expect:
        table.toString() == expectedValue
    }


    def "one row two columns"() {
        Table table = new Table("Table")
        table.set("Row", "C1", "Value1")
        table.set("Row", "C2", "Value2")

        def expectedValue = """\
Table
     C1      C2      
Row  Value1  Value2  
"""

        expect:
        table.toString() == expectedValue
    }

    def "two rows one column"() {
        Table table = new Table("Table")
        table.set("Row1", "Column", "Value1")
        table.set("Row2", "Column", "Value2")

        def expectedValue = """\
Table
      Column  
Row1  Value1  
Row2  Value2  
"""

        expect:
        table.toString() == expectedValue
    }

    def "integer values"() {
        Table table = new Table("Table")
        table.set("Row1", "Column", 1)
        table.set("Row2", "Column", 10)
        table.set("Row3", "Column", 100)
        table.set("Row4", "Column", 1000)
        table.set("Row5", "Column", 10000)

        def expectedValue = """\
Table
      Column  
Row1       1  
Row2      10  
Row3     100  
Row4   1,000  
Row5  10,000  
"""

        expect:
        table.toString() == expectedValue
    }
}
