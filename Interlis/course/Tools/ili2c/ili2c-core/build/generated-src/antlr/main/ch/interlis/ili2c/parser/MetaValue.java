// $ANTLR : "metavalue.g" -> "MetaValue.java"$

	package ch.interlis.ili2c.parser;
	import ch.interlis.ili2c.metamodel.*;
	import java.util.*;
	import ch.ehi.basics.logging.EhiLogger;
	import ch.ehi.basics.tools.StringUtility;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;

public class MetaValue extends antlr.LLkParser       implements MetaValueTokenTypes
 {

  static public ch.ehi.basics.settings.Settings parseMetaValues(String metaValueText)
  throws ANTLRException
  {
  	ch.ehi.basics.settings.Settings ret=null;
		MetaValueLexer lexer = new MetaValueLexer (new java.io.StringReader(metaValueText));
		MetaValue parser = new MetaValue(lexer);
		ret=parser.metaValues();
	return ret;
  }

protected MetaValue(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public MetaValue(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected MetaValue(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public MetaValue(TokenStream lexer) {
  this(lexer,1);
}

public MetaValue(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
}

	public final ch.ehi.basics.settings.Settings  metaValues() throws RecognitionException, TokenStreamException {
		ch.ehi.basics.settings.Settings c;
		
		Token  n = null;
		Token  n2 = null;
		
			c=null;
			String v=null;
			
		
		{
		if ((LA(1)==VALUE)) {
			n = LT(1);
			match(VALUE);
			match(EQUALS);
			v=avalue();
			
				c=new ch.ehi.basics.settings.Settings(true);
				c.setValue(n.getText(),StringUtility.purge(v));
				
			{
			_loop494:
			do {
				if ((LA(1)==SEMI)) {
					match(SEMI);
					{
					if ((LA(1)==VALUE)) {
						n2 = LT(1);
						match(VALUE);
						match(EQUALS);
						v=avalue();
						
								c.setValue(n2.getText(),StringUtility.purge(v));
								
					}
					else if ((LA(1)==EOF||LA(1)==SEMI)) {
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
				}
				else {
					break _loop494;
				}
				
			} while (true);
			}
		}
		else if ((LA(1)==EOF)) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		match(Token.EOF_TYPE);
		return c;
	}
	
	public final String  avalue() throws RecognitionException, TokenStreamException {
		String c;
		
		Token  s = null;
		Token  p = null;
		
			c=null;
		
		
		if ((LA(1)==STRING)) {
			s = LT(1);
			match(STRING);
			c=s.getText();
		}
		else if ((LA(1)==VALUE)) {
			p = LT(1);
			match(VALUE);
			c=p.getText();
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		return c;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"VALUE",
		"'='",
		"';'",
		"STRING",
		"WS",
		"','",
		"ESC",
		"HEXDIGIT"
	};
	
	
	}
