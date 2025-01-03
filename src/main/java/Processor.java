import lombok.Data;
import lombok.SneakyThrows;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Data
public class Processor {

  private CreateTable createTable;
  private String targetTable;
  private String query;
  private Connection connection;
  private List<Field> fields;
  private String ddl;

  @SneakyThrows
  public Processor(String host, String port, String user, String password, String db, String sql) {
    this.createTable = (CreateTable) CCJSqlParserUtil.parse(sql);
    String url = String.format("jdbc:mysql://%s:%s/%s", host, port, db);
    this.connection = DriverManager.getConnection(url, user, password);
    this.query = createTable.getSelect().toString() + " LIMIT 1";
  }

  @SneakyThrows
  public void parse() {

    Statement sm = connection.createStatement();
    ResultSet rs = sm.executeQuery(query);
    ResultSetMetaData rsmd = rs.getMetaData();
    List<Field> fieldList = new ArrayList<>();
    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
      Field field = new Field();
      field.setName(rsmd.getColumnName(i));
      field.setType(rsmd.getColumnTypeName(i));
      field.setDisplaySize(rsmd.getColumnDisplaySize(i));
      field.setScale(rsmd.getScale(i));
      field.setTable(rsmd.getTableName(i));
      fieldList.add(field);
    }
    rs.close();
    sm.close();

    Select selectStmt = this.getCreateTable().getSelect();
    PlainSelect plainSelect = selectStmt.getPlainSelect();

    this.targetTable = this.getCreateTable().getTable().getName();

    for (SelectItem selectItem : plainSelect.getSelectItems()) {
      if (selectItem.getAlias() == null) {
        continue;
      }

      String alias = selectItem.getAlias().getName();

      for (Field field : fieldList) {
        if (selectItem.getExpression() instanceof Column) {
          Column column = (Column) selectItem.getExpression();
          if (column.getColumnName().equals(field.getName())
              && column.getTable().getName().equals(field.getTable())) {
            field.setAlias(alias);
            break;
          }
        } else if (field.getName().equals(selectItem.toString())) {
          field.setAlias(alias);
          break;
        }
      }
    }

    this.fields = fieldList;
    this.printDDL();
  }

  public void printDDL() {
    StringBuilder ddl =
        new StringBuilder("CREATE TABLE ").append(this.getTargetTable()).append(" (\n");
    List<Field> fieldList = this.getFields();
    for (int i = 0; i < fieldList.size(); i++) {
      ddl.append(fieldList.get(i).parse());
      if (i < fieldList.size() - 1) {
        ddl.append(",\n");
      }
    }
    ddl.append("\n);");
    System.out.println(ddl);
    this.ddl = ddl.toString();
  }

  @SneakyThrows
  public void run() {
    Statement sm = this.getConnection().createStatement();
    sm.execute(this.getDdl());
    String dml =
        String.format(
            "INSERT INTO %s  %s",
            this.getTargetTable(), this.getCreateTable().getSelect().toString());
    System.out.println(dml);
    sm.execute(dml);
  }
}
