import lombok.SneakyThrows;

import java.util.List;

public class Main {

  @SneakyThrows
  public static void main(String[] args) {

    String q =
        System.getProperty(
            "q",
            "create table t01\n"
                + "as\n"
                + "select\n"
                + "    basecurrency ,\n"
                + "    exchrate ,\n"
                + "    exchdate,\n"
                + "    case when begin_exchdate is null then '1900-01-01' else date_add(exchdate, interval 1 day) end begin_exchdate,\n"
                + "    ifnull(end_exchdate,'2030-01-01') end_exchdate\n"
                + "from (\n"
                + "         select basecurrency ,exchrate , exchdate\n"
                + "              ,lag(exchdate )over(partition by basecurrency ,exchcurrency  order by exchdate ) begin_exchdate\n"
                + "              ,lead(exchdate)over(partition by basecurrency ,exchcurrency  order by exchdate )  end_exchdate\n"
                + "         from test.prpdexch\n"
                + "         where exchcurrency ='CNY'\n"
                + "     ) tmp;");

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
