// $ANTLR : "ili1undef.g" -> "Ili1Undef.java"$

	package ch.interlis.ili2c.parser;
	import ch.interlis.ili2c.metamodel.*;
	import java.util.*;
	import ch.ehi.basics.logging.EhiLogger;

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

public class Ili1Undef extends antlr.LLkParser       implements Ili1UndefTokenTypes
 {

  static public Evaluable parseValueIfUndefined(String attrName,String explanation)
  {
  	try{
		Ili2LexerClone lexer = new Ili2LexerClone (new java.io.StringReader(explanation));
		Ili1Undef parser = new Ili1Undef(lexer);
		return parser.defValue();
  	}catch(RecognitionException ex){
  		EhiLogger.logError(attrName+": syntax error in default value specification ("+ex.getLocalizedMessage()+")");
	}catch(antlr.TokenStreamRecognitionException ex){
    		if(ex.recog instanceof antlr.NoViableAltForCharException){
			// ignore unexpected char's
		}else{
			EhiLogger.logError(attrName,ex);
		}
	}catch(TokenStreamException ex){
		EhiLogger.logError(attrName,ex);
	}
	return null;
  }

protected Ili1Undef(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public Ili1Undef(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected Ili1Undef(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public Ili1Undef(TokenStream lexer) {
  this(lexer,1);
}

public Ili1Undef(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
}

	public final Evaluable  defValue() throws RecognitionException, TokenStreamException {
		Evaluable c;
		
		
			c=null;
			
		
		match(LITERAL_undefiniert);
		match(EQUALS);
		{
		if ((_tokenSet_0.member(LA(1)))) {
			c=constant();
		}
		else if ((LA(1)==LITERAL_letztes)) {
			match(LITERAL_letztes);
			match(LITERAL_Zeichen);
			if ( inputState.guessing==0 ) {
				c=new LengthOfReferencedText();
						
			}
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		return c;
	}
	
	protected final Constant  constant() throws RecognitionException, TokenStreamException {
		Constant c;
		
		
			  c = null;
			
		
		switch ( LA(1)) {
		case LITERAL_PI:
		case LITERAL_LNBASE:
		case DEC:
		case POSINT:
		case NUMBER:
		{
			c=numericConst();
			break;
		}
		case STRING:
		{
			c=textConst();
			break;
		}
		case NAME:
		{
			c=enumerationConst();
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		return c;
	}
	
	protected final Constant  numericConst() throws RecognitionException, TokenStreamException {
		Constant c;
		
		
			PrecisionDecimal val;
			c=null;
			
		
		val=decConst();
		if ( inputState.guessing==0 ) {
			
					c = new Constant.Numeric (val);
				
		}
		return c;
	}
	
	protected final Constant  textConst() throws RecognitionException, TokenStreamException {
		Constant c;
		
		Token  s = null;
		
				c=null;
			
		
		s = LT(1);
		match(STRING);
		if ( inputState.guessing==0 ) {
			c=new Constant.Text(s.getText());
		}
		return c;
	}
	
	protected final Constant.Enumeration  enumerationConst() throws RecognitionException, TokenStreamException {
		Constant.Enumeration c;
		
		
			List mentionedNames=new ArrayList();
			int lin=0;
			c=null;
			
		
		{
		lin=enumNameList(mentionedNames);
		if ( inputState.guessing==0 ) {
			
					c = new Constant.Enumeration(mentionedNames);
					
		}
		}
		return c;
	}
	
	protected final int  enumNameList(
		List namList
	) throws RecognitionException, TokenStreamException {
		int lin;
		
		Token  firstName = null;
		
		lin=0;
		
		
		firstName = LT(1);
		match(NAME);
		if ( inputState.guessing==0 ) {
			namList.add(firstName.getText());
			lin=firstName.getLine();
			
		}
		enumNameListHelper(namList);
		return lin;
	}
	
	protected final void enumNameListHelper(
		List namList
	) throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
		boolean synPredMatched1768 = false;
		if (((LA(1)==DOT))) {
			int _m1768 = mark();
			synPredMatched1768 = true;
			inputState.guessing++;
			try {
				{
				match(DOT);
				match(NAME);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched1768 = false;
			}
			rewind(_m1768);
inputState.guessing--;
		}
		if ( synPredMatched1768 ) {
			match(DOT);
			n = LT(1);
			match(NAME);
			if ( inputState.guessing==0 ) {
				namList.add(n.getText());
			}
			enumNameListHelper(namList);
		}
		else if ((LA(1)==EOF)) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
	}
	
	protected final PrecisionDecimal  decConst() throws RecognitionException, TokenStreamException {
		PrecisionDecimal dec;
		
		
			 dec=null;
			
		
		switch ( LA(1)) {
		case LITERAL_PI:
		{
			match(LITERAL_PI);
			if ( inputState.guessing==0 ) {
				dec = PrecisionDecimal.PI;
			}
			break;
		}
		case LITERAL_LNBASE:
		{
			match(LITERAL_LNBASE);
			if ( inputState.guessing==0 ) {
				dec = PrecisionDecimal.LNBASE;
			}
			break;
		}
		case DEC:
		case POSINT:
		case NUMBER:
		{
			dec=decimal();
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		return dec;
	}
	
	protected final PrecisionDecimal  decimal() throws RecognitionException, TokenStreamException {
		PrecisionDecimal dec;
		
		Token  d = null;
		Token  p = null;
		Token  n = null;
		
				dec = null;
			
		
		switch ( LA(1)) {
		case DEC:
		{
			d = LT(1);
			match(DEC);
			if ( inputState.guessing==0 ) {
				dec = new PrecisionDecimal(d.getText());
			}
			break;
		}
		case POSINT:
		{
			p = LT(1);
			match(POSINT);
			if ( inputState.guessing==0 ) {
				dec = new PrecisionDecimal(p.getText());
			}
			break;
		}
		case NUMBER:
		{
			n = LT(1);
			match(NUMBER);
			if ( inputState.guessing==0 ) {
				dec = new PrecisionDecimal(n.getText());
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		return dec;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"undefiniert\"",
		"'='",
		"\"letztes\"",
		"\"Zeichen\"",
		"STRING",
		"NAME",
		"'.'",
		"\"PI\"",
		"\"LNBASE\"",
		"DEC",
		"POSINT",
		"NUMBER",
		"PLUS",
		"MINUS",
		"WS",
		"SL_COMMENT",
		"ILI_DOC",
		"ML_COMMENT",
		"EXPLANATION",
		"'('",
		"')'",
		"'['",
		"']'",
		"'{'",
		"'}'",
		"'*'",
		"'/'",
		"'\\\\'",
		"'%'",
		"'@'",
		"'#'",
		"'~'",
		"'<'",
		"'<='",
		"'>'",
		"'>='",
		"';'",
		"'=='",
		"'<>'",
		"'!='",
		"':='",
		"'..'",
		"':'",
		"','",
		"'<-'",
		"'->'",
		"'-<>'",
		"'--'",
		"'-<#>'",
		"ESC",
		"DIGIT",
		"LETTER",
		"ILI1_SCALING",
		"SCALING",
		"ILI1_DEC",
		"HEXDIGIT",
		"HEXNUMBER",
		"NUMERICSTUFF"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 64256L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	
	}
