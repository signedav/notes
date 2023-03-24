// $ANTLR : "modelscan.g" -> "Ili2ModelScan.java"$

	package ch.interlis.ili2c.parser;
	import ch.interlis.ili2c.modelscan.*;
	import java.util.*;
	import ch.ehi.basics.logging.EhiLogger;
	import ch.interlis.ili2c.metamodel.Ili2cSemanticException;

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

public class Ili2ModelScan extends antlr.LLkParser       implements Ili2ModelScanTokenTypes
 {

  private IliFile iliFile=null;
  private IliModel model=null;
  static public void mergeFile(IliFile iliFile1,
    java.io.Reader stream
    )
  {
  	mergeFile(iliFile1,new IliScanLexer (stream));
  }
  static public void mergeFile(IliFile iliFile1,
    java.io.InputStream stream
    )
  {
  	mergeFile(iliFile1,new IliScanLexer (stream));
  }
  static public void mergeFile(IliFile iliFile1,
    IliScanLexer lexer
    )
  {
    try{
		Ili2ModelScan parser = new Ili2ModelScan(lexer);
		parser.iliFile=iliFile1;
		  parser.file();
		if(parser.model!=null){
			parser.iliFile.addModel(parser.model);
		}
    }catch(RecognitionException ex){
  		throw new Ili2cSemanticException(ex); // not really a semantic exception; but avoid a new ili2c specific RuntimeExcpetion
    }catch(antlr.TokenStreamRecognitionException ex){
  		throw new Ili2cSemanticException(ex); // not really a semantic exception; but avoid a new ili2c specific RuntimeExcpetion
     }catch(TokenStreamException ex){
  		throw new Ili2cSemanticException(ex); // not really a semantic exception; but avoid a new ili2c specific RuntimeExcpetion
     }
  }
  static public double getIliVersion(java.io.Reader stream)
  {
  	return getIliVersion(new IliScanLexer (stream));
  }
  static public double getIliVersion(java.io.InputStream stream)
  {
  	return getIliVersion(new IliScanLexer (stream));
  }
  static public double getIliVersion(IliScanLexer lexer
    )
  {
  	try{
		Ili2ModelScan parser = new Ili2ModelScan(lexer);
		double version=parser.version();
		return version;
  	}
  	catch(Exception ex){
		// ignore errors
	}
	return 0.0;
  }

protected Ili2ModelScan(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public Ili2ModelScan(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected Ili2ModelScan(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public Ili2ModelScan(TokenStream lexer) {
  this(lexer,1);
}

public Ili2ModelScan(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
}

	public final void file() throws RecognitionException, TokenStreamException {
		
		Token  v = null;
		Token  n = null;
		Token  trsl = null;
		Token  imp1 = null;
		Token  imp2 = null;
		double version=0.0;
		
		
		try {      // for error handling
			{
			if ((LA(1)==LITERAL_INTERLIS)) {
				{
				match(LITERAL_INTERLIS);
				v = LT(1);
				match(DEC);
				version=Double.parseDouble(v.getText());
				}
			}
			else if ((LA(1)==LITERAL_TRANSFER)) {
				{
				match(LITERAL_TRANSFER);
				match(NAME);
				match(SEMI);
				version=1.0;
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			_loop1654:
			do {
				if (((LA(1) >= LITERAL_INTERLIS && LA(1) <= NUMERICSTUFF))) {
					{
					if ((LA(1)==LITERAL_MODEL)) {
						{
						match(LITERAL_MODEL);
						n = LT(1);
						match(NAME);
						}
						
									if(model!=null){
										iliFile.addModel(model);
									}
									model=new IliModel();
									model.setName(n.getText());
									model.setIliVersion(version);
									//EhiLogger.debug(iliFile.getFilename().toString() +", "+n.getText());
								
					}
					else if ((LA(1)==LITERAL_TRANSLATION)) {
						{
						match(LITERAL_TRANSLATION);
						match(LITERAL_OF);
						trsl = LT(1);
						match(NAME);
						
									String name=trsl.getText();
									//EhiLogger.debug("  "+name);
									if(model!=null){
										model.addDepenedency(name);
									}
								
						}
					}
					else if ((LA(1)==LITERAL_IMPORTS)) {
						{
						match(LITERAL_IMPORTS);
						{
						if ((LA(1)==LITERAL_UNQUALIFIED)) {
							match(LITERAL_UNQUALIFIED);
						}
						else if ((LA(1)==LITERAL_INTERLIS||LA(1)==NAME)) {
						}
						else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						
						}
						{
						if ((LA(1)==NAME)) {
							imp1 = LT(1);
							match(NAME);
							
										String name=imp1.getText();
										//EhiLogger.debug("  "+name);
										if(model!=null){
											model.addDepenedency(name);
										}
									
						}
						else if ((LA(1)==LITERAL_INTERLIS)) {
							match(LITERAL_INTERLIS);
						}
						else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						
						}
						{
						_loop1653:
						do {
							if ((LA(1)==COMMA)) {
								match(COMMA);
								{
								if ((LA(1)==LITERAL_UNQUALIFIED)) {
									match(LITERAL_UNQUALIFIED);
								}
								else if ((LA(1)==LITERAL_INTERLIS||LA(1)==NAME)) {
								}
								else {
									throw new NoViableAltException(LT(1), getFilename());
								}
								
								}
								{
								if ((LA(1)==NAME)) {
									imp2 = LT(1);
									match(NAME);
									
												String name=imp2.getText();
												//EhiLogger.debug("  "+name);
												if(model!=null){
													model.addDepenedency(name);
												}
											
								}
								else if ((LA(1)==LITERAL_INTERLIS)) {
									match(LITERAL_INTERLIS);
								}
								else {
									throw new NoViableAltException(LT(1), getFilename());
								}
								
								}
							}
							else {
								break _loop1653;
							}
							
						} while (true);
						}
						}
					}
					else if (((LA(1) >= LITERAL_INTERLIS && LA(1) <= NUMERICSTUFF))) {
						matchNot(EOF);
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
				}
				else {
					break _loop1654;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
	}
	
	public final double  version() throws RecognitionException, TokenStreamException {
		double v;
		
		Token  dec = null;
		
			v=0.0;
			
		
		try {      // for error handling
			if ((LA(1)==LITERAL_INTERLIS)) {
				{
				match(LITERAL_INTERLIS);
				dec = LT(1);
				match(DEC);
				v= Double.parseDouble(dec.getText());
				}
			}
			else if ((LA(1)==LITERAL_TRANSFER)) {
				{
				match(LITERAL_TRANSFER);
				match(NAME);
				match(SEMI);
				v= 1.0;
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
		return v;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"INTERLIS\"",
		"DEC",
		"\"TRANSFER\"",
		"NAME",
		"';'",
		"\"MODEL\"",
		"\"TRANSLATION\"",
		"\"OF\"",
		"\"IMPORTS\"",
		"\"UNQUALIFIED\"",
		"','",
		"PLUS",
		"MINUS",
		"WS",
		"SL_COMMENT",
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
		"'>>'",
		"'>'",
		"'>='",
		"'='",
		"'=='",
		"'<>'",
		"'!='",
		"':='",
		"'.'",
		"'..'",
		"':'",
		"'<-'",
		"'->'",
		"'-<>'",
		"'--'",
		"'-<#>'",
		"ESC",
		"STRING",
		"DIGIT",
		"LETTER",
		"POSINT",
		"NUMBER",
		"ILI1_SCALING",
		"SCALING",
		"ILI1_DEC",
		"HEXDIGIT",
		"HEXNUMBER",
		"NUMERICSTUFF"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	
	}
