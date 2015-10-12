package entitygen;

import static org.junit.Assert.*;

import org.junit.Test;

public class FromDBTest {

	@Test
	public void testUppercaseName() {
		assertEquals("uc name", "Toronto", FromDB.uppercaseName("toronto"));
	}

}
