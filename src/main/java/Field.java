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
    String columnName;
    if (StringUtils.isNullOrEmpty(this.getAlias())) {
      columnName = this.getName();
    } else {
      columnName = this.getAlias();
    }
    switch (this.getType()) {
      case "INT":
      case "DATE":
      case "DATETIME":
      case "TIME":
        result = String.format("%s %s", columnName, this.getType());
        break;
      case "DECIMAL":
        result =
            String.format(
                "%s %s(%s,%s)", columnName, this.getType(), this.getDisplaySize(), this.getScale());
        break;
      case "CHAR":
      case "VARCHAR":
        result = String.format("%s %s(%s)", columnName, this.getType(), this.getDisplaySize());
        break;
    }
    return result;
  }
}
