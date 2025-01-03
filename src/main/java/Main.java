import lombok.SneakyThrows;

import java.util.List;

public class Main {

  @SneakyThrows
  public static void main(String[] args) {

    String q =
        System.getProperty(
            "q",
            "create table tmp as select t1.*, t2.id t2_id, 'hello' word from t1 left join t2 on t1.id = t2.id");

    String user = System.getProperty("u", "root");
    String password = System.getProperty("p", "123456");
    String host = System.getProperty("h", "127.0.0.1");
    String port = System.getProperty("p", "3306");
    String db = System.getProperty("db", "test");

    Processor processor = new Processor(host, port, user, password, db, q);
    processor.parse();

    StringBuilder ddl = new StringBuilder("CREATE TABLE " + processor.getTargetTable() + " (\n");
    List<Field> fieldList = processor.getFields();
    for (int i = 0; i < processor.getFields().size(); i++) {
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
