// $ANTLR : "interlis1.g" -> "Ili1Lexer.java"$

	package ch.interlis.ili2c.parser;
	import ch.interlis.ili2c.metamodel.*;
	import ch.interlis.ili2c.CompilerLogEvent;
	import java.util.*;
	import ch.ehi.basics.logging.EhiLogger;
	import ch.ehi.basics.settings.Settings;

public interface Ili1ParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int LPAREN = 4;
	int COMMA = 5;
	int RPAREN = 6;
	int NAME = 7;
	int LITERAL_END = 8;
	int POSINT = 9;
	int LITERAL_TRANSFER = 10;
	int SEMI = 11;
	int LITERAL_MODEL = 12;
	int DOT = 13;
	int LITERAL_DOMAIN = 14;
	int EQUALS = 15;
	int LITERAL_TOPIC = 16;
	int LITERAL_OPTIONAL = 17;
	int LITERAL_TABLE = 18;
	int COLON = 19;
	int POINTSTO = 20;
	int EXPLANATION = 21;
	int LITERAL_NO = 22;
	int LITERAL_IDENT = 23;
	// "COORD2" = 24
	// "COORD3" = 25
	int LBRACE = 26;
	int DOTDOT = 27;
	int RBRACE = 28;
	// "DIM1" = 29
	// "DIM2" = 30
	int LITERAL_RADIANS = 31;
	int LITERAL_DEGREES = 32;
	int LITERAL_GRADS = 33;
	int LITERAL_TEXT = 34;
	int STAR = 35;
	int LITERAL_DATE = 36;
	int LITERAL_HALIGNMENT = 37;
	int LITERAL_VALIGNMENT = 38;
	int LITERAL_POLYLINE = 39;
	int LITERAL_WITHOUT = 40;
	int LITERAL_SURFACE = 41;
	int LITERAL_AREA = 42;
	int LITERAL_LINEATTR = 43;
	int LITERAL_WITH = 44;
	int LITERAL_STRAIGHTS = 45;
	int LITERAL_ARCS = 46;
	int LITERAL_OVERLAPS = 47;
	int GREATER = 48;
	int LITERAL_VERTEX = 49;
	int LITERAL_BASE = 50;
	int LITERAL_DERIVATIVES = 51;
	int LITERAL_VIEW = 52;
	int LITERAL_VERTEXINFO = 53;
	int LITERAL_PERIPHERY = 54;
	int LITERAL_CONTOUR = 55;
	int LESSMINUS = 56;
	int LITERAL_FORMAT = 57;
	int LITERAL_FREE = 58;
	int LITERAL_FIX = 59;
	int LITERAL_LINESIZE = 60;
	int LITERAL_TIDSIZE = 61;
	int LITERAL_CODE = 62;
	int LITERAL_FONT = 63;
	int LITERAL_BLANK = 64;
	int LITERAL_DEFAULT = 65;
	int LITERAL_UNDEFINED = 66;
	int LITERAL_CONTINUE = 67;
	int LITERAL_TID = 68;
	// "I16" = 69
	// "I32" = 70
	int LITERAL_ANY = 71;
	int HEXNUMBER = 72;
	int ILI1_DEC = 73;
	int NUMBER = 74;
	int PLUS = 75;
	int MINUS = 76;
	int WS = 77;
	int ILI_METAVALUE = 78;
	int SL_COMMENT = 79;
	int ILI_DOC = 80;
	int ML_COMMENT = 81;
	int ESC = 82;
	int STRING = 83;
	int DIGIT = 84;
	int LETTER = 85;
	int ILI1_SCALING = 86;
	int HEXDIGIT = 87;
	int NUMERICSTUFF = 88;
}
