package entitygen;

import static org.junit.Assert.*;

import org.junit.Test;

public class FromDBTest {

	@Test
	public void testUppercaseName() {
		assertEquals("uc name", "Toronto", FromDB.upperCaseNameChar0("toronto"));
	}
	
	@Test
	public void testFixName() {
		// As any-but-last char, "_x" should become "X"
		assertEquals("someName", FromDB.upperCaseColName("some_name"));
		// As last char, _ should be idempotent
		assertEquals("somename_", FromDB.upperCaseColName("somename_"));
	}

}
