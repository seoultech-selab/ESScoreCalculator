package kr.ac.seoultech.selab.esscore.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import kr.ac.seoultech.selab.esscore.model.ESNode;

class CodeHandlerTest {

	private static String oldCode;
	private static String newCode;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		oldCode = FileHandler.readFile("resources/test/OldCode.java");
		newCode = FileHandler.readFile("resources/test/NewCode.java");

	}

	@Test
	void testParseIJM() {
		try {
			List<ESNode> nodes = CodeHandler.parseIJM(oldCode);
			assertEquals(28, nodes.size());
			assertTrue(nodes.contains(new ESNode("int", "PrimitiveType", 54, 3)));
			assertTrue(nodes.contains(new ESNode("0", "NumberLiteral", 66, 1)));
			assertTrue(nodes.contains(new ESNode("", "ThisExpression", 140, 4)));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
