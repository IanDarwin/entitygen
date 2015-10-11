package entitygen;

import java.sql.*;

import com.darwinsys.sql.*;

/**
 * Create an Entity from a JDBC table 
 */
public class FromDB {
	static Connection conn;
	static String tableName;
	static String entityClass;

	public static void main(String[] args) {

		conn = ConnectionUtil.getConnection("todo");
		tableName = "task";
		entityClass = "Task";

		FromDB prog = new FromDB();
		try {
			prog.process(conn, tableName, entityClass);
		} catch (SQLException e) {
			System.out.println("Fail: " + e);
		}
	}

	void process(Connection conn, String tableName, String entityClass) throws SQLException {
		System.out.println("Running wild");
		if (conn == null) {
			throw new IllegalArgumentException("Connection object is null");
		}
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet tables = meta.getTables(null, null, null, null);
		while(tables.next()) {
			String table = tables.getString(1);
			System.out.println("TABLE: " + table);
		}
	}
}