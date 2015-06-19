package entitygen;

import static org.junit.Assert.*;

import org.junit.Test;

public class EntityGenTest {

	@Test
	public void testToBeanName() {
		assertEquals("meh", JenGen.toBeanName("Meh"));
	}
	
	@Test
	public void testToBeanNameShort() {
		assertEquals("x", JenGen.toBeanName("X"));
		
	}

	@Test(expected=IllegalArgumentException.class)
	public void testToBeanNameChecking() {
		JenGen.toBeanName("");	// should throw
	}
}
