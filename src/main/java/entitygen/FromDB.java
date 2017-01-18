package entitygen;

import static entitygen.JenGen.OUTPUT_DIR;
import static entitygen.JenGen.PKG_NAME_MODEL;
import static entitygen.JenGen.SRC_DIR;
import static entitygen.JenGen.ensureDirectoryExists;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.darwinsys.sql.ConnectionUtil;

/**
 * Create an Entity from a JDBC table 
 */
public class FromDB {
	static Connection conn;
	static String entityClass;

	public static void main(String[] args) {

		if (args.length < 2) {
			System.err.println("Usage: connection table [...]");
			System.exit(1);
		}
		conn = ConnectionUtil.getConnection(args[0]);
		FromDB prog = new FromDB();

		for (int n = 1; n < args.length; n++) {
			String tableName = args[n];
			entityClass = fixupName(tableName);
			final String outputSourceDir = OUTPUT_DIR + "/" +
					SRC_DIR + "/" + PKG_NAME_MODEL;
			String outputFileName = outputSourceDir + "/" +
					entityClass + ".java";
			try {
				ensureDirectoryExists(new File(outputSourceDir));
				PrintWriter pout = new PrintWriter(outputFileName);
				prog.generateEntityClass(pout, conn, tableName, entityClass);
				pout.close();
				System.err.printf("Wrote Entity %s to %s%n", entityClass, outputFileName);
			} catch (SQLException e) {
				System.err.println("Failure on " + tableName);
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Output Error on " + outputFileName);
				e.printStackTrace();
			}
		}
	}

	public static String fixupName(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	void generateEntityClass(PrintWriter out, Connection conn, String tableName, String entityClass) throws SQLException {
		out.println("// Class " + entityClass + " created by " + getClass().getName() + " at " + LocalDateTime.now());
		out.println("package " + JenGen.PKG_NAME_MODEL);
		out.println();
		out.println("import javax.persistence.*;");
		out.println();
		out.println("@Entity");
		out.println("public class " + entityClass + " {");
		DatabaseMetaData meta = conn.getMetaData();
		// First the primary keys
		ResultSet pKeys = meta.getPrimaryKeys(null, null, tableName);
		List<String> pKeyNames = new ArrayList<>();
		int rowId = 0;
		while (pKeys.next()) {
			String name = pKeys.getString(4);
			pKeyNames.add(name);
			rowId = pKeys.getRow();
		}
		boolean compoundPkey = rowId > 1;
		// Then the regular columns.
		ResultSet columns = meta.getColumns(null, null, tableName, "%");
		while (columns.next()) {
			// Col 1 = cat, 2 = schema, 3 = table, 4 = column, 5 = SQL type (java.sql.Types)
			String column = columns.getString(4);
			boolean isIdColumn = pKeyNames.contains(column);
			int type = columns.getInt(5);
			Integer columnLength = columns.getInt(7); // column size
			//Integer fractDigits = (Integer) columns.getObject(9); // fractional digits, null if non-numeric
			Object tmp = columns.getObject(16); // char length
			Integer charLength = tmp == null ? -1 : Integer.parseInt(tmp.toString());
			String typeStr;
			switch (type) {
			case Types.CHAR:
				typeStr = "char";
				if (charLength != null) {
					typeStr += "[]";
				}
				break;
			case Types.BIGINT:
				typeStr = "long";
				break;
			case Types.INTEGER:
				typeStr = "int";
				break;
			case Types.SMALLINT:
				typeStr = "short";
				break;
			case Types.TINYINT:
				typeStr = "byte";
				break;
			case Types.VARCHAR:
				typeStr = "String";
				break;
			case Types.DATE:
			case Types.TIME: case Types.TIME_WITH_TIMEZONE:
			case Types.TIMESTAMP: case Types.TIMESTAMP_WITH_TIMEZONE:
				typeStr = "Date";
				break;
			default:
				typeStr = "FIXME";
				break;
			}
			if (isIdColumn) {
				out.println("\t@Id");
			}
			out.println("\t" + typeStr + " " + column + ";");
		}
		out.println("}");
	}

	@SuppressWarnings("unused") // Left here for debugging purposes
	private void display(Connection conn, String entityClass) throws SQLException {
		System.out.println("FromDB.display");
		if (conn == null) {
			throw new IllegalArgumentException("Connection object is null");
		}
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE","VIEW"});
		while (tables.next()) {
			String table = tables.getString(3);
			System.out.println("TABLE: " + table);
			ResultSet columns = meta.getColumns(null, null, table, "%");
			while (columns.next()) {
				// Col 1 = cat, 2 = schema, 3 = table, 4 = column
				String column = columns.getString(4);
				System.out.println("\tCOLUMN: " + column);
			}
		}
	}
}
