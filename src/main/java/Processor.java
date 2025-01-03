import com.github.vertical_blank.sqlformatter.SqlFormatter;
import lombok.Data;
import lombok.SneakyThrows;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
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
  private List<Field> fields;
  private Connection conn;
  private String query;
  private String ctas1;
  private String ctas2;

  @SneakyThrows
  public Processor(String host, String port, String user, String password, String db, String sql) {
    this.createTable = (CreateTable) CCJSqlParserUtil.parse(sql);
    String url = String.format("jdbc:mysql://%s:%s/%s", host, port, db);
    this.conn = DriverManager.getConnection(url, user, password);
    this.query = this.getCreateTable().getSelect().toString() + " LIMIT 1";
  }

  @SneakyThrows
  public void parse() {

    Statement sm = conn.createStatement();
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
      field.setIsNullable(rsmd.isNullable(i));
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
  }

  public void generateSQL() {
    StringBuilder ctas1 =
        new StringBuilder("CREATE TABLE ").append(this.getTargetTable()).append(" (\n");
    List<Field> fieldList = this.getFields();
    for (int i = 0; i < fieldList.size(); i++) {
      ctas1.append(fieldList.get(i).parse());
      if (i < fieldList.size() - 1) {
        ctas1.append(",\n");
      }
    }
    ctas1.append("\n);");
    this.setCtas1(ctas1.toString());

    StringBuilder ctas2 =
        new StringBuilder("INSERT INTO ")
            .append(this.getTargetTable())
            .append(" ")
            .append(this.getCreateTable().getSelect().toString());
    this.setCtas2(ctas2.toString());
  }

  @SneakyThrows
  public void run() {
    Statement sm = this.getConn().createStatement();

    String c1 = SqlFormatter.format(this.getCtas1());
    String c2 = SqlFormatter.format(this.getCtas2());

    System.out.println("==========\n");
    System.out.println(c1);
    System.out.println("\n==========\n");
    System.out.println(c2);
    System.out.println("\n==========");
    sm.execute(c1);
    sm.execute(c2);
    sm.close();
    this.getConn().close();
  }
}
