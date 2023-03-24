// $ANTLR : "metavalue.g" -> "MetaValue.java"$

	package ch.interlis.ili2c.parser;
	import ch.interlis.ili2c.metamodel.*;
	import java.util.*;
	import ch.ehi.basics.logging.EhiLogger;
	import ch.ehi.basics.tools.StringUtility;

public interface MetaValueTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int VALUE = 4;
	int EQUALS = 5;
	int SEMI = 6;
	int STRING = 7;
	int WS = 8;
	int COMMA = 9;
	int ESC = 10;
	int HEXDIGIT = 11;
}
