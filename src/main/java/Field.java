import com.mysql.cj.util.StringUtils;
import lombok.Data;

import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;

@Data
public class Field {

  private String table;
  private String name;
  private String type;
  private String alias;
  private int isNullable;
  private int displaySize;
  private int scale;

  private static Map<Integer, String> nullMap = new HashMap<>();

  static {
    nullMap.put(ResultSetMetaData.columnNoNulls, "NOT NULL");
    nullMap.put(ResultSetMetaData.columnNullable, "DEFAULT NULL");
    nullMap.put(ResultSetMetaData.columnNullableUnknown, "");
  }

  public String parse() {
    String result = "";
    String name = StringUtils.isNullOrEmpty(this.getAlias()) ? this.getName() : this.getAlias();
    String type = this.getType();
    int isNullable = this.getIsNullable();
    int displaySize = this.getDisplaySize();
    int scale = this.getScale();

    switch (this.getType()) {
      case "INT":
      case "DATE":
      case "DATETIME":
      case "TIME":
      case "BIGINT":
        result = String.format("%s %s %s", name, type, nullMap.get(isNullable));
        break;
      case "DECIMAL":
        result =
            String.format(
                "%s %s(%s,%s) %s", name, type, displaySize, scale, nullMap.get(isNullable));
        break;
      case "CHAR":
      case "VARCHAR":
        result = String.format("%s %s(%s) %s", name, type, displaySize, nullMap.get(isNullable));
        break;
      default:
        System.out.println("unknown type: " + type);
        System.exit(0);
    }
    return result;
  }
}
