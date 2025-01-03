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
    ResultSet resultSet = sm.executeQuery(query);
    ResultSetMetaData rsmd = resultSet.getMetaData();
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

    Select selectStmt = this.getCreateTable().getSelect();
    PlainSelect plainSelect = selectStmt.getPlainSelect();
    this.targetTable = this.getCreateTable().getTable().getName();
    for (SelectItem selectItem : plainSelect.getSelectItems()) {
      for (Field field : fieldList) {
        if (selectItem.getAlias() != null) {
          if (selectItem.getExpression() instanceof Column) {
            String columnName = ((Column) selectItem.getExpression()).getColumnName();
            String table = ((Column) selectItem.getExpression()).getTable().getName();
            if (columnName.equals(field.getName()) && table.equals(field.getTable())) {
              field.setAlias(selectItem.getAlias().getName());
            }
          } else {
            if (field.getName().equals(selectItem.toString())) {
              field.setAlias(selectItem.getAlias().getName());
            }
          }
        }
      }
    }
    this.fields = fieldList;
  }

  public void print() {
    StringBuilder ddl = new StringBuilder("CREATE TABLE " + this.getTargetTable() + " (\n");
    List<Field> fieldList = this.getFields();
    for (int i = 0; i < this.getFields().size(); i++) {
      if (i < fieldList.size() - 1) {
        ddl.append(fieldList.get(i).parse()).append(",\n");
      } else {
        ddl.append(fieldList.get(i).parse());
      }
    }
    ddl.append("\n);");
    System.out.println(ddl);
  }
}
