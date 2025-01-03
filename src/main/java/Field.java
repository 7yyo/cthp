import com.mysql.cj.util.StringUtils;
import lombok.Data;

@Data
public class Field {

  private String table;
  private String name;
  private String type;
  private String alias;
  private int displaySize;
  private int scale;

  public String parse() {
    String result = "";
    String name = StringUtils.isNullOrEmpty(this.getAlias()) ? this.getName() : this.getAlias();
    String type = this.getType();
    int displaySize = this.getDisplaySize();
    int scale = this.getScale();

    switch (this.getType()) {
      case "INT":
      case "DATE":
      case "DATETIME":
      case "TIME":
        result = String.format("%s %s", name, type);
        break;
      case "DECIMAL":
        result = String.format("%s %s(%s,%s)", name, type, displaySize, scale);
        break;
      case "CHAR":
      case "VARCHAR":
        result = String.format("%s %s(%s)", name, type, displaySize);
        break;
      default:
        System.out.println("unknown type: " + type);
        System.exit(0);
    }
    return result;
  }
}
