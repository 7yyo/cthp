import com.mysql.cj.util.StringUtils;
import lombok.SneakyThrows;

public class Main {

  @SneakyThrows
  public static void main(String[] args) {

    String q = System.getProperty("q", "");

    if (StringUtils.isNullOrEmpty(q)) {
      System.out.println("-Dq is null or empty, exit.");
      return;
    }

    String user = System.getProperty("u", "root");
    String password = System.getProperty("pwd", "123456");
    String host = System.getProperty("h", "127.0.0.1");
    String port = System.getProperty("p", "3306");
    String db = System.getProperty("db", "test");

    Processor p = new Processor(host, port, user, password, db, q);
    p.parse();
    p.generateSQL();
    p.run();
  }
}
