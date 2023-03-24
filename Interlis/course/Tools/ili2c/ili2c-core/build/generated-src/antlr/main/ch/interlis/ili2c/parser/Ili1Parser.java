// $ANTLR : "interlis1.g" -> "Ili1Parser.java"$

	package ch.interlis.ili2c.parser;
	import ch.interlis.ili2c.metamodel.*;
	import ch.interlis.ili2c.CompilerLogEvent;
	import java.util.*;
	import ch.ehi.basics.logging.EhiLogger;
	import ch.ehi.basics.settings.Settings;

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

public class Ili1Parser extends antlr.LLkParser       implements Ili1ParserTokenTypes
 {

  protected PredefinedModel modelInterlis;
  protected Type predefinedBooleanType;
  protected Table predefinedScalSystemClass;
  protected Table predefinedCoordSystemClass;
  protected TransferDescription td;
  private Ili1Lexer lexer;
  private antlr.TokenStreamHiddenTokenFilter filter;
  private Map ili1TableRefAttrs;
  private Ili2cMetaAttrs externalMetaAttrs=new Ili2cMetaAttrs();
  /** helps to remember ordering of reference attributes
  */
  private int ili1AttrCounter=0;
  
  /** list of assocs generate from ili1 ref attrs. 
   *  Used to fix names of assocs at end of topic parse.
  */
  private ArrayList ili1assocs=null;

  /** Parse the contents of a stream according to INTERLIS-1 or INTERLIS-2 syntax
      (the version is detected automatically by the parser) and add the
      encountered contents to this TransferDescription.

      @return false if there have been any fatal errors which would lead
              this TransferDescription in an inconsistent state.
  */
  static public boolean parseIliFile (TransferDescription td
    ,String filename
    ,java.io.Reader stream
    ,int line0Offest
    ,Ili2cMetaAttrs metaAttrs
    )
  {
  	return parseIliFile(td,filename,new Ili1Lexer(stream),line0Offest,metaAttrs);
  }
  static public boolean parseIliFile (TransferDescription td
    ,String filename
    ,java.io.InputStream stream
    ,int line0Offest
    ,Ili2cMetaAttrs metaAttrs
    )
  {
  	return parseIliFile(td,filename,new Ili1Lexer(stream),line0Offest,metaAttrs);
  }
  static public boolean parseIliFile (TransferDescription td
    ,String filename
    ,Ili1Lexer lexer
    ,int line0Offest
    ,Ili2cMetaAttrs metaAttrs
    )
  {
    try {
	if ((filename != null) && "".equals (td.getName())){
		td.setName(filename);
	}
      
      //
      // setup token stream splitting to filter out comments
      //
      //filter.getHiddenAfter(end)
      //filter.getHiddenBefore(begin)

      // create token objects augmented with links to hidden tokens. 
      lexer.setTokenObjectClass("antlr.CommonHiddenStreamToken");

      // create filter that pulls tokens from the lexer
      antlr.TokenStreamHiddenTokenFilter filter = new antlr.TokenStreamHiddenTokenFilter(lexer);

      // tell the filter which tokens to hide, and which to discard
      filter.hide(ILI_DOC);
      filter.hide(ILI_METAVALUE);

      // connect parser to filter (instead of lexer)
      Ili1Parser parser = new Ili1Parser (filter);
      if(metaAttrs!=null){
	parser.externalMetaAttrs=metaAttrs;
      }
      
      parser.lexer=lexer;
      parser.filter=filter;
      parser.setFilename (filename);
      return parser.interlisDescription (td);
    }catch(antlr.RecognitionException ex){
      //listener.error (new ErrorListener.ErrorEvent (ex, filename, ex.recog.getLine(),
      //  ErrorListener.ErrorEvent.SEVERITY_ERROR));
      int line=ex.getLine();
      CompilerLogEvent.logError(filename,line,ex.getLocalizedMessage());
      return false;
    }catch(Ili2cSemanticException ex){
	      int line=((Ili2cSemanticException)ex).getSourceLine();
	      CompilerLogEvent.logError(filename,line,ex.getLocalizedMessage());
	      return false;
    }catch(antlr.TokenStreamRecognitionException ex){
    	if(ex.recog instanceof antlr.NoViableAltForCharException){
		antlr.NoViableAltForCharException ex2=(antlr.NoViableAltForCharException)ex.recog;
		String msg="unexpected char: "+ex2.foundChar+" (0x"+Integer.toHexString(ex2.foundChar).toUpperCase()+")";
		CompilerLogEvent.logError(filename,ex2.getLine(),msg);
	}else{
		CompilerLogEvent.logError(filename,ex.recog.getLine(),ex.getLocalizedMessage());
	}
      return false;
    } catch (antlr.ANTLRError ex) {
      EhiLogger.traceState(filename+": "+ex);
      return false;
    } catch (Exception ex) {
      CompilerLogEvent.logError(filename,0,ex);
      return false;
    }
  }

	/** compiler error messages
	*/
	ResourceBundle rsrc = ResourceBundle.getBundle(
		ErrorMessages.class.getName(),
		Locale.getDefault());

  public void reportError (String message)
  {
      String filename=getFilename();
      CompilerLogEvent.logError(filename,0,message);
  }


  public void reportWarning (String message)
  {
      String filename=getFilename();
      CompilerLogEvent.logWarning(filename,0,message);
  }


  private void reportError (String message, int lineNumber)
  {
      String filename=getFilename();
      CompilerLogEvent.logError(filename,lineNumber,message);
  }


  private void reportWarning (String message, int lineNumber)
  {
      String filename=getFilename();
      CompilerLogEvent.logWarning(filename,lineNumber,message);
  }


  private void reportError (Throwable ex, int lineNumber)
  {
      String filename=getFilename();
      if(ex instanceof antlr.RecognitionException){
	      CompilerLogEvent.logError(filename,lineNumber,ex.getLocalizedMessage());
      }else if(ex instanceof Ili2cSemanticException){
	      CompilerLogEvent.logError(filename,lineNumber,ex.getLocalizedMessage());
      }else{
	      CompilerLogEvent.logError(filename,lineNumber,ex);
      }
  }
  private void reportError (Ili2cSemanticException ex)
  {
      String filename=getFilename();
      CompilerLogEvent.logError(filename,ex.getSourceLine(),ex.getLocalizedMessage());
  }
  protected void reportError (List<Ili2cSemanticException> errs)
  {
      String filename=getFilename();
  	for(Ili2cSemanticException ex:errs){
      CompilerLogEvent.logError(filename,ex.getSourceLine(),ex.getLocalizedMessage());
  	}
  }
  public void reportError (antlr.RecognitionException ex)
  {
      String filename=getFilename();
      int lineNumber=((antlr.RecognitionException)ex).getLine();
      CompilerLogEvent.logError(filename,lineNumber,ex.getLocalizedMessage());
  }

  private void reportInternalError(int lineNumber)
  {
      String filename=getFilename();
      CompilerLogEvent.logError(filename,lineNumber,formatMessage("err_internalCompilerError", /* exception */ ""));
  }


  private void reportInternalError (Throwable ex, int lineNumber)
  {
      String filename=getFilename();
      CompilerLogEvent.logError(filename,lineNumber,formatMessage("err_internalCompilerError", ""),ex);
  }


  private String formatMessage (String msg, Object[] args)
  {
    try {
      java.text.MessageFormat mess = new java.text.MessageFormat(
        rsrc.getString(msg));
      return mess.format(args);
    } catch (Exception ex) {
      EhiLogger.logError("Internal compiler error",ex);
      return "Internal compiler error [" + ex.getLocalizedMessage() + "]";
    }
  }


  private String formatMessage(String msg, String arg) {
    return formatMessage(msg, new Object[] { arg });
  }


  private String formatMessage(String msg, String arg1, String arg2) {
    return formatMessage(msg, new Object[] { arg1, arg2 });
  }

  private String formatMessage(String msg, String arg1, String arg2, String arg3) {
    return formatMessage(msg, new Object[] { arg1, arg2, arg3 });
  }

  public static void panic ()
  {
    throw new antlr.ANTLRError();
  }


  /** Find a LineForm given its explanation string. Used by INTERLIS-1 parser. */
  private LineForm findLineFormByExplanation (Container scope, String explanation)
  {
    Model model = (Model) scope.getContainerOrSame (Model.class);
    Iterator iter = model.iterator();
    while (iter.hasNext ())
    {
      Object obj = iter.next ();
      if ((obj instanceof LineForm)
          && explanation.equals (((LineForm) obj).getExplanation ()))
        return (LineForm) obj;
    }
    return null;
  }

  private int numIli1LineAttrStructures = 0;

  private Table createImplicitLineAttrStructure (Container container, Viewable table,int lineNumber)
  {
    Table result = new Table ();

    ++numIli1LineAttrStructures;
    try {
      result.setName ("LineAttrib" + numIli1LineAttrStructures);
      result.setIdentifiable (false); /* make it a structure */
      result.setIli1LineAttrStruct(true);
      if(container instanceof Topic && table!=null){
	      ((Topic)container).addBefore (result,table);
      }else if(container instanceof Model && table!=null){
	      ((Model)container).addBefore (result,table);
      }else{
	      container.add (result);
      }
    } catch (Exception ex) {
      reportInternalError (ex, lineNumber);
    }

    return result;
  }


  private int countElements (Container container, Class klass)
  {
    int numMatchingElements = 0;
    Iterator iter = container.iterator ();
    while (iter.hasNext ())
    {
      if (klass.isInstance (iter.next()))
        numMatchingElements = numMatchingElements + 1;
    }

    return numMatchingElements;
  }


  /** Used by INTERLIS-1 parser. */
  private LineForm addLineFormIfNoSuchExplanation (Container scope, String explanation, int line)
  {
    LineForm result;

    result = findLineFormByExplanation (scope, explanation);
    if (result != null)
      return result;

    Model model = (Model) scope.getContainerOrSame (Model.class);

    /* No line form with the same explanation has been found. Create a new one. */
    if (!model.isContracted())
    {
      /* Only contracted models may contain line forms.
         Add an artificial contract, but emit a warning. */
      reportWarning (formatMessage ("err_lineForm_ili1_artificialContract",
                                    model.getName ()),
                     line);
      try {
      	Contract contract=new Contract(
		rsrc.getString ("err_lineForm_ili1_artificialContractorName")
		,rsrc.getString ("err_lineForm_ili1_artificialContractExplanation")
		);
        model.addContract(contract);
      } catch (Exception ex) {
        reportError (ex, line);
        panic ();
      }
    } /* if (!model.isContracted()) */

    try {
      result = new LineForm (formatMessage ("err_lineForm_ili1_artificialName",
                                            Integer.toString (countElements (model,
                                                                             LineForm.class) + 1)));
      result.setExplanation (explanation);
      model.add (result);
    } catch (Exception ex) {
      reportError (ex, line);
    }

    return result;
  }
	
	private String getIliDoc()
	throws antlr.TokenStreamException
	{ 
		String ilidoc=null;
		antlr.Token cmtToken=filter.getHiddenBefore((antlr.CommonHiddenStreamToken)LT(1));
		if(cmtToken!=null){
			ilidoc=cmtToken.getText().trim();
			if(ilidoc.startsWith("/**")){
				ilidoc=ilidoc.substring(3);
			}
			if(ilidoc.endsWith("*/")){
				ilidoc=ilidoc.substring(0,ilidoc.length()-2);
			}
			java.io.LineNumberReader lines=new java.io.LineNumberReader(new java.io.StringReader(ilidoc.trim()));
			String line;
			StringBuilder buf=new StringBuilder();
			String sep="";
			try{
				while((line=lines.readLine())!=null){
					line=line.trim();
					if(line.startsWith("*")){
						line=line.substring(1).trim();
					}
					buf.append(sep);
					buf.append(line);
					sep="\n";
				}
			}catch(java.io.IOException ex){
				EhiLogger.logError(ex);
			}
			ilidoc=buf.toString();
			if(ilidoc.length()==0){
				ilidoc=null;
			}
		}
		return ilidoc;
	}

	private ch.ehi.basics.settings.Settings getMetaValues()
	throws antlr.TokenStreamException
	{ 
		ArrayList docs=new ArrayList();
		antlr.CommonHiddenStreamToken cmtToken=((antlr.CommonHiddenStreamToken)LT(1)).getHiddenBefore();
		while(cmtToken!=null){
			if(cmtToken.getType()==ILI_METAVALUE){
				docs.add(0,cmtToken);
			}
			cmtToken=cmtToken.getHiddenBefore();
		}
		Iterator doci=docs.iterator();
		StringBuilder metaValuesText=new StringBuilder();
		String sep="";
		while(doci.hasNext()){
			cmtToken=(antlr.CommonHiddenStreamToken)doci.next();
			String valueText=cmtToken.getText().trim();
			metaValuesText.append(sep+valueText);
			sep=";";
		}
		ch.ehi.basics.settings.Settings metaValues=null;
		try{
			metaValues=MetaValue.parseMetaValues(metaValuesText.toString());
		}catch(antlr.ANTLRException ex){
			reportError("failed to parse Metavalue <"+metaValuesText.toString()+">",LT(1).getLine());
		}
		return metaValues;
	}


protected Ili1Parser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public Ili1Parser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected Ili1Parser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public Ili1Parser(TokenStream lexer) {
  this(lexer,1);
}

public Ili1Parser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
}

	public final boolean  interlisDescription(
		TransferDescription td1
	) throws RecognitionException, TokenStreamException {
		boolean canProceed;
		
		
				canProceed = true;
				this.td = td1;
				this.modelInterlis = td.INTERLIS;
				this.predefinedBooleanType = Type.findReal (td.INTERLIS.BOOLEAN.getType());
				this.predefinedScalSystemClass = td.INTERLIS.SCALSYSTEM;
				this.predefinedCoordSystemClass = td.INTERLIS.COORDSYSTEM;
			
		
		try {      // for error handling
			{
			interlis1Def();
			}
		}
		catch (NoViableAltException nvae) {
			if (inputState.guessing==0) {
				
						      reportError (rsrc.getString ("err_notIliDescription"));
						      canProceed = false;
						
			} else {
				throw nvae;
			}
		}
		return canProceed;
	}
	
/****************************************************************
* INTERLIS 1
*/
	protected final void interlis1Def() throws RecognitionException, TokenStreamException {
		
		Token  transferName = null;
		Token  modelName = null;
		Token  modelName2 = null;
		Token  endDot = null;
		
		Model   model = null;
		Ili1Format format = new Ili1Format();
		String ilidoc=null;
		Settings metaValues=null;
		
		
		try {      // for error handling
			match(LITERAL_TRANSFER);
			transferName = LT(1);
			match(NAME);
			match(SEMI);
			if ( inputState.guessing==0 ) {
				
				model = new DataModel ();
				try {
				td.setName (transferName.getText ());
				model.setName (transferName.getText ());
					model.setIliVersion(Model.ILI1);
				} catch (Exception ex) {
				reportError (ex, transferName.getLine ());
				}
				
			}
			{
			if ((LA(1)==LITERAL_DOMAIN)) {
				ili1_domainDefs(model);
			}
			else if ((LA(1)==LITERAL_MODEL)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			match(LITERAL_MODEL);
			modelName = LT(1);
			match(NAME);
			if ( inputState.guessing==0 ) {
				
				try {
				model.setName (modelName.getText ());
				model.setSourceLine (modelName.getLine());
				model.setFileName(getFilename());
					    model.setDocumentation(ilidoc);
					    model.setMetaValues(metaValues);
				String translationOfName=externalMetaAttrs.getMetaAttrValue(model.getName(),Ili2cMetaAttrs.ILI2C_TRANSLATION_OF);
				if(translationOfName!=null){
					Model translationOf=(Model)td.getElement(Model.class,translationOfName);
						if(translationOf==null){
					        	reportError(formatMessage("err_noSuchModel", translationOfName), modelName.getLine());
						}else{
							model.setTranslationOf(translationOf.getName(),translationOf.getModelVersion());
						}
				}
				} catch (Exception ex) {
				reportError (ex, transferName.getLine ());
				}
				
				try {
				td.add (model);
				} catch (Exception ex) {
				reportError (ex, transferName.getLine ());
				}
				
			}
			{
			if ((LA(1)==LITERAL_DOMAIN)) {
				ili1_domainDefs(model);
			}
			else if ((LA(1)==LITERAL_TOPIC)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			int _cnt1890=0;
			_loop1890:
			do {
				if ((LA(1)==LITERAL_TOPIC)) {
					ili1_topic(model);
				}
				else {
					if ( _cnt1890>=1 ) { break _loop1890; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt1890++;
			} while (true);
			}
			match(LITERAL_END);
			modelName2 = LT(1);
			match(NAME);
			endDot = LT(1);
			match(DOT);
			if ( inputState.guessing==0 ) {
				
				if (!model.getName().equals (modelName2.getText()))
				{
				/* model.toString() would return "DATA MODEL xxx", which
				could confuse users. Construct a string according to
				INTERLIS-1 on the fly. */
				reportError (formatMessage ("err_end_mismatch",
				"MODEL " + model.getName(),
				model.getName(),
				modelName2.getText()),
				modelName2.getLine ());
				}
					       try {
							List<Ili2cSemanticException> errs=new java.util.ArrayList<Ili2cSemanticException>();	       		
					         model.checkIntegrity (errs);
					         reportError(errs);
					       } catch (Ili2cSemanticException ex) {
					         reportError (ex);
					       } catch (Exception ex) {
					         reportError (ex, endDot.getLine());
					       }
				
			}
			{
			if ((LA(1)==LITERAL_DERIVATIVES)) {
				ili1_derivatives(model);
			}
			else if ((LA(1)==LITERAL_VIEW||LA(1)==LITERAL_FORMAT)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			_loop1893:
			do {
				if ((LA(1)==LITERAL_VIEW)) {
					ili1_view(model);
				}
				else {
					break _loop1893;
				}
				
			} while (true);
			}
			ili1_format(format);
			ili1_encoding(format);
			if ( inputState.guessing==0 ) {
				td.setIli1Format(format);
			}
			match(Token.EOF_TYPE);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final ch.interlis.ili2c.metamodel.Enumeration  enumeration(
		Type extending
	) throws RecognitionException, TokenStreamException {
		ch.interlis.ili2c.metamodel.Enumeration enumer;
		
		Token  lp = null;
		
		List elements = new LinkedList();
		ch.interlis.ili2c.metamodel.Enumeration.Element curElement;
		enumer = null;
		
		
		try {      // for error handling
			lp = LT(1);
			match(LPAREN);
			{
			{
			curElement=enumElement(extending);
			if ( inputState.guessing==0 ) {
				elements.add(curElement);
			}
			{
			_loop1881:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					curElement=enumElement(extending);
					if ( inputState.guessing==0 ) {
						
								  Iterator elei=elements.iterator();
								  while(elei.hasNext()){
									  ch.interlis.ili2c.metamodel.Enumeration.Element ele=(ch.interlis.ili2c.metamodel.Enumeration.Element)elei.next();
									  if(ele.getName().equals(curElement.getName())){
										  reportError(formatMessage("err_enumerationType_DupEle",curElement.getName()),curElement.getSourceLine());
										  break;
									  }
								  }
							  	elements.add(curElement); 
							
					}
				}
				else {
					break _loop1881;
				}
				
			} while (true);
			}
			}
			}
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				
				enumer = new ch.interlis.ili2c.metamodel.Enumeration(elements);
				enumer.setFinal(false);
				enumer.setSourceLine(lp.getLine());
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_1);
			} else {
			  throw ex;
			}
		}
		return enumer;
	}
	
	protected final ch.interlis.ili2c.metamodel.Enumeration.Element  enumElement(
		Type extending
	) throws RecognitionException, TokenStreamException {
		ch.interlis.ili2c.metamodel.Enumeration.Element ee;
		
		Token  en = null;
		
		ch.interlis.ili2c.metamodel.Enumeration subEnum = null;
		ch.interlis.ili2c.metamodel.Enumeration.Element curEnum = null;
		ee = null;
		int lineNumber = 0;
		String ilidoc=null;
		Settings metaValues=null;
		
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			en = LT(1);
			match(NAME);
			{
			if ((LA(1)==LPAREN)) {
				subEnum=enumeration(extending);
			}
			else if ((LA(1)==COMMA||LA(1)==RPAREN)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
				if (subEnum == null)
				{
					    // new leaf
				ee = new ch.interlis.ili2c.metamodel.Enumeration.Element (
				en.getText());
					    ee.setDocumentation(ilidoc);
					    ee.setMetaValues(metaValues);
					    ee.setSourceLine(en.getLine());
				}
				else
				{
					    // new subtree
				ee = new ch.interlis.ili2c.metamodel.Enumeration.Element (
				en.getText(),
				subEnum);
					    ee.setDocumentation(ilidoc);
					    ee.setMetaValues(metaValues);
					    ee.setSourceLine(en.getLine());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_2);
			} else {
			  throw ex;
			}
		}
		return ee;
	}
	
	protected final void end(
		Element elt
	) throws RecognitionException, TokenStreamException {
		
		Token  nam = null;
		
		try {      // for error handling
			match(LITERAL_END);
			nam = LT(1);
			match(NAME);
			if ( inputState.guessing==0 ) {
				
								if (elt != null)
								{
									if (!nam.getText().equals(elt.getName())){
										reportError(
											formatMessage ("err_end_mismatch", elt.toString(),
											elt.getName(), nam.getText()),
											nam.getLine());
									}
								}
							
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_3);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final int  posInteger() throws RecognitionException, TokenStreamException {
		int i;
		
		Token  p = null;
		
				i = 0;
			
		
		try {      // for error handling
			p = LT(1);
			match(POSINT);
			if ( inputState.guessing==0 ) {
				
							try {
								i = Integer.parseInt(p.getText());
							} catch (Exception ex) {
								/* An exception here would mean that the lexer detects
								   numbers which are not numbers for Java. */
								reportInternalError(ex, p.getLine());
							}
						
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_4);
			} else {
			  throw ex;
			}
		}
		return i;
	}
	
	protected final void ili1_domainDefs(
		Container container
	) throws RecognitionException, TokenStreamException {
		
		Token  domainName = null;
		
		Model model = (Model) container.getContainerOrSame (Model.class);
		Topic topic = (Topic) container.getContainerOrSame (Topic.class);
		Type type = null;
		String ilidoc=null;
		Settings metaValues=null;
		
		
		try {      // for error handling
			match(LITERAL_DOMAIN);
			{
			int _cnt1896=0;
			_loop1896:
			do {
				if ((LA(1)==NAME)) {
					if ( inputState.guessing==0 ) {
						ilidoc=getIliDoc();metaValues=getMetaValues();
					}
					domainName = LT(1);
					match(NAME);
					match(EQUALS);
					type=ili1_attributeType(model, topic,null);
					match(SEMI);
					if ( inputState.guessing==0 ) {
						
						Domain domain = null;
						
						try {
						domain = new Domain ();
						domain.setSourceLine(domainName.getLine ());
						domain.setName (domainName.getText ());
							      domain.setDocumentation(ilidoc);
							      domain.setMetaValues(metaValues);
						if (type != null)
						domain.setType (type);
						domain.setAbstract (false);
						domain.setFinal (false);
						} catch (Exception ex) {
						reportError (ex, domainName.getLine ());
						try {
						/* try to fix it */
						domain = new Domain ();
						domain.setName (domainName.getText ());
						domain.setAbstract (false);
						domain.setFinal (false);
						} catch (Exception ex2) {
						panic ();
						}
						}
						
						try {
						if (domain != null)
						container.add (domain);
						} catch (Exception ex) {
						reportError (ex, domainName.getLine ());
						}
						
					}
				}
				else {
					if ( _cnt1896>=1 ) { break _loop1896; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt1896++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_5);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final void ili1_topic(
		Model model
	) throws RecognitionException, TokenStreamException {
		
		Token  topicName = null;
		
		Topic topic = null;
		String ilidoc=null;
		Settings metaValues=null;
		
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			match(LITERAL_TOPIC);
			topicName = LT(1);
			match(NAME);
			match(EQUALS);
			if ( inputState.guessing==0 ) {
				
				ili1assocs=new ArrayList();
				topic = new Topic ();
				try {
				topic.setSourceLine(topicName.getLine());
				topic.setName (topicName.getText ());
					    topic.setDocumentation(ilidoc);
					    topic.setMetaValues(metaValues);
				topic.setAbstract (false);
				model.add (topic);
				} catch (Exception ex) {
				reportError (ex, topicName.getLine ());
				}
				
			}
			{
			int _cnt1899=0;
			_loop1899:
			do {
				if ((LA(1)==LITERAL_OPTIONAL||LA(1)==LITERAL_TABLE)) {
					ili1_table(topic);
				}
				else if ((LA(1)==LITERAL_DOMAIN)) {
					ili1_domainDefs(topic);
				}
				else {
					if ( _cnt1899>=1 ) { break _loop1899; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt1899++;
			} while (true);
			}
			end(topic);
			match(DOT);
			if ( inputState.guessing==0 ) {
				
					// fix assocs name
					Iterator associ=ili1assocs.iterator();
					while(associ.hasNext()){
						AssociationDef assoc=(AssociationDef)associ.next();
						int uniqueName=2;
						String assocName=assoc.getName();
						String assocBasename=assocName.substring(0,assocName.indexOf(':'));
						assocName=assocBasename;
						boolean assocNameConflict=false;
						do{
							assocNameConflict=false;
							Iterator elei=topic.iterator();
							while (elei.hasNext()){
							      Element ele = (Element)elei.next();
							      if(ele!=assoc && ele.getName().equals(assocName)){
								assocNameConflict=true;
								break;
							      }
							}
							if(!assocNameConflict){
								try{
									assoc.setName(assocName);
								}catch(java.beans.PropertyVetoException ex){
									assocNameConflict=true;
								}
							}
							if(assocNameConflict){
								assocName=assocBasename+Integer.toString(uniqueName);
								uniqueName++;
							}
						}while(assocNameConflict);
					}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_6);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final void ili1_derivatives(
		Model mainModel
	) throws RecognitionException, TokenStreamException {
		
		Token  derivativeName = null;
		Token  derivativeName2 = null;
		
		try {      // for error handling
			match(LITERAL_DERIVATIVES);
			derivativeName = LT(1);
			match(NAME);
			{
			if ((LA(1)==LITERAL_DOMAIN)) {
				ili1_domainDefs(mainModel);
			}
			else if ((LA(1)==LITERAL_TOPIC)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			int _cnt1957=0;
			_loop1957:
			do {
				if ((LA(1)==LITERAL_TOPIC)) {
					ili1_topic(mainModel);
				}
				else {
					if ( _cnt1957>=1 ) { break _loop1957; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt1957++;
			} while (true);
			}
			match(LITERAL_END);
			derivativeName2 = LT(1);
			match(NAME);
			if ( inputState.guessing==0 ) {
				
				if (!derivativeName.getText().equals (derivativeName2.getText ()))
				reportError (formatMessage ("err_end_mismatch",
				"DERIVATIVES " + derivativeName.getText (),
				derivativeName.getText (),
				derivativeName2.getText ()),
				derivativeName2.getLine ());
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_7);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final void ili1_view(
		Model mainModel
	) throws RecognitionException, TokenStreamException {
		
		Token  viewName = null;
		Token  viewName2 = null;
		
		try {      // for error handling
			match(LITERAL_VIEW);
			viewName = LT(1);
			match(NAME);
			{
			_loop1962:
			do {
				if ((LA(1)==NAME)) {
					match(NAME);
					match(DOT);
					match(NAME);
					match(COLON);
					ili1_viewDef();
					{
					_loop1961:
					do {
						if ((LA(1)==COMMA)) {
							match(COMMA);
							ili1_viewDef();
						}
						else {
							break _loop1961;
						}
						
					} while (true);
					}
					match(SEMI);
				}
				else {
					break _loop1962;
				}
				
			} while (true);
			}
			match(LITERAL_END);
			viewName2 = LT(1);
			match(NAME);
			match(DOT);
			if ( inputState.guessing==0 ) {
				
				reportWarning (formatMessage ("err_view_ili1", viewName.getText ()),
				viewName.getLine ());
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_7);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final void ili1_format(
		Ili1Format format
	) throws RecognitionException, TokenStreamException {
		
		
		int lineSize = 0;
		int tidSize = 0;
		
		
		try {      // for error handling
			match(LITERAL_FORMAT);
			{
			if ((LA(1)==LITERAL_FREE)) {
				match(LITERAL_FREE);
				if ( inputState.guessing==0 ) {
					format.isFree=true;
				}
			}
			else if ((LA(1)==LITERAL_FIX)) {
				match(LITERAL_FIX);
				match(LITERAL_WITH);
				match(LITERAL_LINESIZE);
				match(EQUALS);
				lineSize=posInteger();
				match(COMMA);
				match(LITERAL_TIDSIZE);
				match(EQUALS);
				tidSize=posInteger();
				if ( inputState.guessing==0 ) {
					
						format.isFree=true;
						format.lineSize=lineSize;
						format.tidSize=tidSize;
						
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(SEMI);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_8);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final void ili1_encoding(
		Ili1Format format
	) throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(LITERAL_CODE);
			{
			if ((LA(1)==LITERAL_FONT)) {
				ili1_font(format);
			}
			else if ((LA(1)==LITERAL_BLANK)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			ili1_specialCharacter(format);
			ili1_transferId(format);
			match(LITERAL_END);
			match(DOT);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final Type  ili1_attributeType(
		Model forModel, Topic forTopic,Viewable table
	) throws RecognitionException, TokenStreamException {
		Type type;
		
		
		type = null;
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LPAREN:
			case 24:
			case 25:
			case LBRACE:
			case 29:
			case 30:
			case LITERAL_RADIANS:
			case LITERAL_DEGREES:
			case LITERAL_GRADS:
			case LITERAL_TEXT:
			case LITERAL_DATE:
			case LITERAL_HALIGNMENT:
			case LITERAL_VALIGNMENT:
			{
				type=ili1_baseType(forModel);
				break;
			}
			case LITERAL_POLYLINE:
			{
				type=ili1_lineType(forModel, forTopic);
				break;
			}
			case LITERAL_SURFACE:
			case LITERAL_AREA:
			{
				type=ili1_areaType(forModel, forTopic,table);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	protected final void ili1_table(
		Topic topic
	) throws RecognitionException, TokenStreamException {
		
		Token  tableName = null;
		Token  tableName2 = null;
		
		boolean optional = false;
		Table   table = null;
		ili1AttrCounter=0;
		String ilidoc=null;
		Settings metaValues=null;
		
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			{
			if ((LA(1)==LITERAL_OPTIONAL)) {
				match(LITERAL_OPTIONAL);
				if ( inputState.guessing==0 ) {
					optional = true;
				}
			}
			else if ((LA(1)==LITERAL_TABLE)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(LITERAL_TABLE);
			tableName = LT(1);
			match(NAME);
			match(EQUALS);
			if ( inputState.guessing==0 ) {
				
				table = new Table ();
				ili1TableRefAttrs=new HashMap();
				try {
				table.setName (tableName.getText ());
				table.setSourceLine(tableName.getLine());
					    table.setDocumentation(ilidoc);
					    table.setMetaValues(metaValues);
				table.setAbstract (false);
					table.setIli1Optional(optional);
				topic.add (table);
				} catch (Exception ex) {
				reportError (ex, tableName.getLine ());
				}
				
			}
			{
			int _cnt1903=0;
			_loop1903:
			do {
				if ((LA(1)==NAME)) {
					ili1_attribute(table);
					if ( inputState.guessing==0 ) {
						
								ili1AttrCounter++;
							
					}
				}
				else {
					if ( _cnt1903>=1 ) { break _loop1903; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt1903++;
			} while (true);
			}
			ili1_identifications(table);
			match(LITERAL_END);
			tableName2 = LT(1);
			match(NAME);
			match(SEMI);
			if ( inputState.guessing==0 ) {
				
				ili1TableRefAttrs=null;
				if (!table.getName().equals (tableName2.getText ()))
				{
				/* table.toString() would return "CLASS xxx", which
				could confuse users. Construct a string according to
				INTERLIS-1 on the fly. */
				reportError (formatMessage ("err_end_mismatch",
				"TABLE " + table.getName(),
				table.getName (),
				tableName2.getText ()),
				tableName2.getLine ());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_10);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final void ili1_attribute(
		Table table
	) throws RecognitionException, TokenStreamException {
		
		Token  attributeName = null;
		Token  col = null;
		Token  tabnam = null;
		Token  expl = null;
		
		boolean optional = false;
		Type type = null;
		AttributeDef attrib = null;
		Model model = (Model) table.getContainer (Model.class);
		Topic topic = (Topic) table.getContainer (Topic.class);
		String ilidoc=null;
		Settings metaValues=null;
		
		String explText=null;
		
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			attributeName = LT(1);
			match(NAME);
			col = LT(1);
			match(COLON);
			{
			if ((LA(1)==LITERAL_OPTIONAL)) {
				match(LITERAL_OPTIONAL);
				if ( inputState.guessing==0 ) {
					optional = true;
				}
			}
			else if ((_tokenSet_11.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((_tokenSet_12.member(LA(1)))) {
				type=ili1_localAttributeType(model, topic,table);
				if ( inputState.guessing==0 ) {
					
					attrib = new LocalAttribute ();
						    attrib.setDocumentation(ilidoc);
						    attrib.setMetaValues(metaValues);
					
				}
			}
			else if ((LA(1)==POINTSTO)) {
				match(POINTSTO);
				tabnam = LT(1);
				match(NAME);
				if ( inputState.guessing==0 ) {
					
					AssociationDef assoc=new AssociationDef();
						assoc.setSourceLine(tabnam.getLine ());
					Table referred = (Table) topic.getRealElement (Table.class, tabnam.getText ());
					if (referred == null)
					{
					reportError (formatMessage ("err_noSuchTable", tabnam.getText(),
					topic.toString ()),
					tabnam.getLine ());
						  assoc.setDirty(true);
					}
						if(referred==table){
					reportError (formatMessage ("err_relAttribute_recursive", attributeName.getText(),tabnam.getText()),
					tabnam.getLine ());
						  assoc.setDirty(true);
						}
					try {
						String thisRoleName=table.getName();
						String oppendRoleName=attributeName.getText();
						// ensure thisRoleName is unique in referred table and 
						// that it is different from oppendRoleName
						int uniqueName=2;
						String thisRoleBasename=thisRoleName;
						if(thisRoleName.equals(oppendRoleName)){
							thisRoleName=thisRoleName+Integer.toString(uniqueName);
							uniqueName++;
						}
						boolean roleNameConflict=false;
						do{
							roleNameConflict=false;
							if(referred.getElement(AttributeDef.class,thisRoleName)!=null){
								roleNameConflict=true;
							}else{
								Iterator rolei=referred.getOpposideRoles();
								while (rolei.hasNext()){
								      RoleDef targetOppRole = (RoleDef)rolei.next();
								      if(targetOppRole.getName().equals(thisRoleName)){
									roleNameConflict=true;
									break;
								      }
								}
							}
							if(roleNameConflict){
								thisRoleName=thisRoleBasename+Integer.toString(uniqueName);
								uniqueName++;
							}
						}while(roleNameConflict);
						// ensure assocName is unique in topic
						// use temporary/illegal name, fix it at end of topic
						String assocName=thisRoleName+oppendRoleName+":"+ili1assocs.size();
					assoc.setName(assocName);
					RoleDef role1=new RoleDef(true);
						role1.setSourceLine(tabnam.getLine ());
						role1.setIli1AttrIdx(ili1AttrCounter);
					role1.setName(thisRoleName);
						ReferenceType role1ref=new ReferenceType();
						role1ref.setReferred(table);
					role1.setReference(role1ref);
					role1.setCardinality(new Cardinality(0, Cardinality.UNBOUND));
					assoc.add(role1);
					RoleDef role2=new RoleDef(true);
						role2.setSourceLine(tabnam.getLine ());
					role2.setName(oppendRoleName);
						ReferenceType role2ref=new ReferenceType();
						role2ref.setReferred(referred);
					role2.setReference(role2ref);
					role2.setCardinality(new Cardinality(optional?0:1, 1));
					assoc.add(role2);
					topic.add(assoc);
					assoc.fixupRoles();
						// remember assoc to fix name at end of topic
						ili1assocs.add(assoc);
						// remember map from attrname to role for IDENT parsing
						// has to be attrname (and not rolename!) to be able to find role when parsing IDENTs
						ili1TableRefAttrs.put(attributeName.getText(),role2); 
					} catch (Exception ex) {
					reportError (ex, tabnam.getLine ());
					}
					
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((LA(1)==EXPLANATION)) {
				expl = LT(1);
				match(EXPLANATION);
				if ( inputState.guessing==0 ) {
					
					explText=expl.getText();
					reportWarning (formatMessage ("err_attribute_ili1_constraintLost",
					attributeName.getText ()),
					expl.getLine ());
					
				}
			}
			else if ((LA(1)==SEMI)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(SEMI);
			if ( inputState.guessing==0 ) {
				
				if(attrib!=null){
				try {
				attrib.setName (attributeName.getText ());
				attrib.setSourceLine(attributeName.getLine());
				} catch (Exception ex) {
				reportError (ex, attributeName.getLine ());
				panic ();
				}
				
				try {
				type.setMandatory (!optional);
				} catch (Exception ex) {
				reportError (ex, col.getLine ());
				}
				
				try {
				attrib.setDomain (type);
				} catch (Exception ex) {
				reportError (ex, col.getLine ());
				}
				
				attrib.setExplanation(explText);
				
				try {
				table.add (attrib);
				} catch (Exception ex) {
				reportError (ex, col.getLine ());
				}
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_13);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final void ili1_identifications(
		Table table
	) throws RecognitionException, TokenStreamException {
		
		Token  anam = null;
		Token  bnam = null;
		Token  semi = null;
		
		List<String> ll = null;
		boolean ignore=false;
		boolean selfStanding=false;
		
		
		try {      // for error handling
			if ((LA(1)==LITERAL_NO)) {
				match(LITERAL_NO);
				match(LITERAL_IDENT);
			}
			else if ((LA(1)==LITERAL_IDENT)) {
				match(LITERAL_IDENT);
				{
				int _cnt1912=0;
				_loop1912:
				do {
					if ((LA(1)==NAME)) {
						anam = LT(1);
						match(NAME);
						if ( inputState.guessing==0 ) {
							
								selfStanding=false;
							ll = new LinkedList<String>();
							ll.add (anam.getText());
							
						}
						{
						_loop1911:
						do {
							if ((LA(1)==COMMA)) {
								match(COMMA);
								bnam = LT(1);
								match(NAME);
								if ( inputState.guessing==0 ) {
									
									ll.add(bnam.getText());
									
								}
							}
							else {
								break _loop1911;
							}
							
						} while (true);
						}
						semi = LT(1);
						match(SEMI);
						if ( inputState.guessing==0 ) {
							
							try {
									UniqueEl uel=new UniqueEl();
									for(int i=0;!ignore && i<ll.size();i++){
									String attrnam = ll.get(i);
									AttributeDef curAttribute = (AttributeDef) table.getRealElement (
									  AttributeDef.class,
									  attrnam);
									if (curAttribute != null){
										AttributeRef[] attrRef=new AttributeRef[1];
										attrRef[0]=new AttributeRef(curAttribute);
										ObjectPath path;
										path=new ObjectPath(table,attrRef);
										uel.addAttribute(path);
									}else{
									  if(!ili1TableRefAttrs.containsKey(attrnam)){
										  reportError (formatMessage ("err_attributePath_unknownAttr_short",
												      attrnam,
												      table.toString ()),
										       semi.getLine ());
										ignore=true;
									  }else{
										PathElAbstractClassRole[] roleRef=new PathElAbstractClassRole[1];
										roleRef[0]=new PathElAbstractClassRole((RoleDef)ili1TableRefAttrs.get(attrnam));
										ObjectPath path;
										path=new ObjectPath(table,roleRef);
										uel.addAttribute(path);
										selfStanding=true;
									  }
									}
								  }
								  if(!ignore){
									  /* Construct a new uniqueness constraint */
									  UniquenessConstraint constr = new UniquenessConstraint();
									  constr.setElements(uel);
									  constr.setSelfStanding(selfStanding);
									  /* Add it to the table. */
									  table.add (constr);
								  }else{
								        reportWarning("IDENT constraint lost",
							semi.getLine ());
								  }
							} catch (Exception ex) {
							reportError (ex, semi.getLine ());
							}
							
						}
					}
					else {
						if ( _cnt1912>=1 ) { break _loop1912; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					
					_cnt1912++;
				} while (true);
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_14);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final Type  ili1_localAttributeType(
		Model forModel, Topic forTopic,Viewable table
	) throws RecognitionException, TokenStreamException {
		Type type;
		
		
		type = null;
		
		
		try {      // for error handling
			type=ili1_type(forModel, forTopic,table);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_15);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	protected final Type  ili1_type(
		Model forModel, Topic forTopic,Viewable table
	) throws RecognitionException, TokenStreamException {
		Type type;
		
		Token  nam = null;
		
		type = null;
		
		
		try {      // for error handling
			if ((_tokenSet_16.member(LA(1)))) {
				type=ili1_attributeType(forModel, forTopic,table);
			}
			else if ((LA(1)==NAME)) {
				nam = LT(1);
				match(NAME);
				if ( inputState.guessing==0 ) {
					
					Domain domain = null;
					
					/* Look in the topic */
					if (forTopic != null)
					{
					domain = (Domain) forTopic.getRealElement (Domain.class, nam.getText ());
					if (domain == null)
					domain = (Domain) forModel.getRealElement (Domain.class, nam.getText ());
					if (domain == null)
					{
					reportError (formatMessage ("err_domainRef_notInModelOrTopic",
					nam.getText (),
					forTopic.toString (),
					"MODEL " + forModel.getName ()),
					nam.getLine ());
					try {
					domain = new Domain ();
					domain.setName (nam.getText ());
					forTopic.add (domain);
					} catch (Exception ex) {
					panic ();
					}
					}
					}
					else /* forTopic == null */
					{
					domain = (Domain) forModel.getRealElement (Domain.class, nam.getText ());
					if (domain == null)
					{
					reportError (formatMessage ("err_domainRef_notInModel",
					nam.getText (),
					"MODEL " + forModel.getName ()),
					nam.getLine ());
					try {
					domain = new Domain ();
					domain.setName (nam.getText ());
					forModel.add (domain);
					} catch (Exception ex) {
					panic ();
					}
					}
					}
					
					/* At this point, it is guaranteed that domain != null */
					type = new TypeAlias ();
					try {
					((TypeAlias) type).setAliasing (domain);
					} catch (Exception ex) {
					reportError (ex, nam.getLine ());
					}
					
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	protected final Type  ili1_baseType(
		Model containingModel
	) throws RecognitionException, TokenStreamException {
		Type type;
		
		
		type = null;
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case 24:
			{
				type=ili1_coord2();
				break;
			}
			case 25:
			{
				type=ili1_coord3();
				break;
			}
			case 29:
			{
				type=ili1_dim1Type();
				break;
			}
			case 30:
			{
				type=ili1_dim2Type(containingModel);
				break;
			}
			case LITERAL_RADIANS:
			case LITERAL_DEGREES:
			case LITERAL_GRADS:
			{
				type=ili1_angleType(containingModel);
				break;
			}
			case LBRACE:
			{
				type=ili1_numericRange();
				break;
			}
			case LITERAL_TEXT:
			{
				type=ili1_textType();
				break;
			}
			case LITERAL_DATE:
			{
				type=ili1_dateType();
				break;
			}
			case LPAREN:
			{
				type=ili1_enumerationType();
				break;
			}
			case LITERAL_HALIGNMENT:
			{
				type=ili1_horizAlignment();
				break;
			}
			case LITERAL_VALIGNMENT:
			{
				type=ili1_vertAlignment();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	protected final LineType  ili1_lineType(
		Model forModel, Topic forTopic
	) throws RecognitionException, TokenStreamException {
		LineType type;
		
		
		type = null;
		
		
		try {      // for error handling
			match(LITERAL_POLYLINE);
			if ( inputState.guessing==0 ) {
				
				type = new PolylineType ();
				
			}
			ili1_form(type, forModel);
			ili1_controlPoints(type, forModel, forTopic);
			{
			boolean synPredMatched1932 = false;
			if (((LA(1)==LITERAL_WITHOUT))) {
				int _m1932 = mark();
				synPredMatched1932 = true;
				inputState.guessing++;
				try {
					{
					match(LITERAL_WITHOUT);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched1932 = false;
				}
				rewind(_m1932);
inputState.guessing--;
			}
			if ( synPredMatched1932 ) {
				ili1_intersectionDef(type);
			}
			else if ((_tokenSet_9.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	protected final LineType  ili1_areaType(
		Model forModel, Topic forTopic,Viewable table
	) throws RecognitionException, TokenStreamException {
		LineType type;
		
		
		type = null;
		Container scope;
		
		if (forTopic != null)
		scope = forTopic;
		else
		scope = forModel;
		
		
		try {      // for error handling
			{
			if ((LA(1)==LITERAL_SURFACE)) {
				match(LITERAL_SURFACE);
				if ( inputState.guessing==0 ) {
					
					type = new SurfaceType (true);
					
				}
				ili1_form(type, forModel);
				ili1_controlPoints(type, forModel, forTopic);
				{
				boolean synPredMatched1937 = false;
				if (((LA(1)==LITERAL_WITHOUT))) {
					int _m1937 = mark();
					synPredMatched1937 = true;
					inputState.guessing++;
					try {
						{
						match(LITERAL_WITHOUT);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched1937 = false;
					}
					rewind(_m1937);
inputState.guessing--;
				}
				if ( synPredMatched1937 ) {
					ili1_intersectionDef(type);
				}
				else if ((_tokenSet_9.member(LA(1)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
			}
			else if ((LA(1)==LITERAL_AREA)) {
				match(LITERAL_AREA);
				if ( inputState.guessing==0 ) {
					
					type = new AreaType ();
					
				}
				ili1_form(type, forModel);
				ili1_controlPoints(type, forModel, forTopic);
				ili1_intersectionDef(type);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			boolean synPredMatched1940 = false;
			if (((LA(1)==LITERAL_LINEATTR))) {
				int _m1940 = mark();
				synPredMatched1940 = true;
				inputState.guessing++;
				try {
					{
					match(LITERAL_LINEATTR);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched1940 = false;
				}
				rewind(_m1940);
inputState.guessing--;
			}
			if ( synPredMatched1940 ) {
				ili1_lineAttributes(type, scope,table);
			}
			else if ((_tokenSet_9.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	protected final CoordType  ili1_coord2() throws RecognitionException, TokenStreamException {
		CoordType type;
		
		Token  coord = null;
		
		type = null;
		PrecisionDecimal eMin, nMin, eMax, nMax;
		
		
		try {      // for error handling
			coord = LT(1);
			match(24);
			eMin=ili1_decimal();
			nMin=ili1_decimal();
			eMax=ili1_decimal();
			nMax=ili1_decimal();
			if ( inputState.guessing==0 ) {
				
				NumericType easting = null;
				NumericType northing = null;
				
				try {
				easting = new NumericType (eMin, eMax);
				} catch (Exception ex) {
				reportError (ex, coord.getLine ());
				}
				
				try {
				northing = new NumericType (nMin, nMax);
				} catch (Exception ex) {
				reportError (ex, coord.getLine ());
				}
				
				if ((easting != null) && (northing != null))
				{
				try {
				type = new CoordType (
				new NumericType[]
				{
				easting,
				northing
				},
				/* null axis */ 2,
				/* pi/2 axis */ 1);
				} catch (Exception ex) {
				reportError (ex, coord.getLine ());
				}
				} /* if ((easting != null) && (northing != null)) */
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	protected final CoordType  ili1_coord3() throws RecognitionException, TokenStreamException {
		CoordType type;
		
		Token  coord = null;
		
		type = null;
		PrecisionDecimal eMin, nMin, hMin, eMax, nMax, hMax;
		
		
		try {      // for error handling
			coord = LT(1);
			match(25);
			eMin=ili1_decimal();
			nMin=ili1_decimal();
			hMin=ili1_decimal();
			eMax=ili1_decimal();
			nMax=ili1_decimal();
			hMax=ili1_decimal();
			if ( inputState.guessing==0 ) {
				
				NumericType easting = null;
				NumericType northing = null;
				NumericType height = null;
				
				try {
				easting = new NumericType (eMin, eMax);
				} catch (Exception ex) {
				reportError (ex, coord.getLine ());
				}
				
				try {
				northing = new NumericType (nMin, nMax);
				} catch (Exception ex) {
				reportError (ex, coord.getLine ());
				}
				
				try {
				height = new NumericType (hMin, hMax);
				} catch (Exception ex) {
				reportError (ex, coord.getLine ());
				}
				
				if ((easting != null) && (northing != null) && (height != null))
				{
				try {
				type = new CoordType (
				new NumericType[]
				{
				easting,
				northing,
				height
				},
				/* null axis */ 2,
				/* pi/2 axis */ 1);
				} catch (Exception ex) {
				reportError (ex, coord.getLine ());
				}
				} /* if ((easting != null) && (northing != null) && (height != null)) */
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	protected final NumericType  ili1_dim1Type() throws RecognitionException, TokenStreamException {
		NumericType type;
		
		Token  dim1 = null;
		
		type = null;
		PrecisionDecimal min = null, max = null;
		
		
		try {      // for error handling
			dim1 = LT(1);
			match(29);
			min=ili1_decimal();
			max=ili1_decimal();
			if ( inputState.guessing==0 ) {
				
				try {
				type = new NumericType (min, max);
				type.setUnit (td.INTERLIS.METER);
				} catch (Exception ex) {
				reportError (ex, dim1.getLine ());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	protected final NumericType  ili1_dim2Type(
		Model inModel
	) throws RecognitionException, TokenStreamException {
		NumericType type;
		
		Token  dim2 = null;
		
		type = null;
		PrecisionDecimal min = null, max = null;
		ComposedUnit m2 = null;
		
		
		try {      // for error handling
			dim2 = LT(1);
			match(30);
			min=ili1_decimal();
			max=ili1_decimal();
			if ( inputState.guessing==0 ) {
				
				m2 = (ComposedUnit) inModel.getRealElement (ComposedUnit.class, rsrc.getString ("err_unit_ili1_DIM2_name"));
				if (m2 == null)
				{
				try {
				ComposedUnit.Composed timesMeter;
				
				m2 = new ComposedUnit ();
				m2.setName (rsrc.getString ("err_unit_ili1_DIM2_name"));
				m2.setDocName (rsrc.getString ("err_unit_ili1_DIM2_docName"));
				m2.setComposedUnits (new ComposedUnit.Composed[] { 
						  new ComposedUnit.Composed ('*', td.INTERLIS.METER)
						  ,new ComposedUnit.Composed ('*', td.INTERLIS.METER)
					  });
				inModel.addPreLast (m2); // before reference
				} catch (Exception ex) {
				reportInternalError (dim2.getLine ());
				panic ();
				}
				}
				try {
				type = new NumericType (min, max);
				type.setUnit (m2);
				} catch (Exception ex) {
				reportError (ex, dim2.getLine ());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	protected final NumericType  ili1_angleType(
		Model containingModel
	) throws RecognitionException, TokenStreamException {
		NumericType type;
		
		Token  radians = null;
		Token  degrees = null;
		Token  grads = null;
		
		type = null;
		PrecisionDecimal min = null, max = null;
		Unit u = null;
		int lineNumber = 0;
		
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_RADIANS:
			{
				radians = LT(1);
				match(LITERAL_RADIANS);
				if ( inputState.guessing==0 ) {
					
					u = td.INTERLIS.RADIAN;
					lineNumber = radians.getLine ();
					
				}
				break;
			}
			case LITERAL_DEGREES:
			{
				degrees = LT(1);
				match(LITERAL_DEGREES);
				if ( inputState.guessing==0 ) {
					
					lineNumber = degrees.getLine ();
					u = (Unit) containingModel.getRealElement (Unit.class,
					rsrc.getString ("err_unit_ili1_DEGREES_name"));
					if (u == null)
					{
					try {
					NumericallyDerivedUnit.Factor times180;
					NumericallyDerivedUnit.Factor byPi;
					
					byPi = new NumericallyDerivedUnit.Factor ('/', PrecisionDecimal.PI);
					times180 = new NumericallyDerivedUnit.Factor ('*', new PrecisionDecimal("180.0"));
					
					NumericallyDerivedUnit degr = new NumericallyDerivedUnit ();
					degr.setName (rsrc.getString ("err_unit_ili1_DEGREES_name"));
					degr.setDocName (rsrc.getString ("err_unit_ili1_DEGREES_docName"));
					degr.setExtending (td.INTERLIS.RADIAN);
					degr.setConversionFactors (new NumericallyDerivedUnit.Factor[] {
					times180,
					byPi
					});
					u = degr;
					containingModel.addPreLast (degr);
					} catch (Exception ex) {
					reportInternalError (ex, degrees.getLine());
					}
					} /* if (u == null) */
					
				}
				break;
			}
			case LITERAL_GRADS:
			{
				grads = LT(1);
				match(LITERAL_GRADS);
				if ( inputState.guessing==0 ) {
					
					lineNumber = grads.getLine ();
					u = (Unit) containingModel.getRealElement (Unit.class,
					rsrc.getString ("err_unit_ili1_GRADS_docName"));
					if (u == null)
					{
					try {
					NumericallyDerivedUnit.Factor times200;
					NumericallyDerivedUnit.Factor byPi;
					
					byPi = new NumericallyDerivedUnit.Factor ('/', PrecisionDecimal.PI);
					times200 = new NumericallyDerivedUnit.Factor ('*', new PrecisionDecimal("200.0"));
					
					NumericallyDerivedUnit gon = new NumericallyDerivedUnit ();
					gon.setName (rsrc.getString ("err_unit_ili1_GRADS_docName"));
					gon.setDocName (rsrc.getString ("err_unit_ili1_GRADS_docName"));
					gon.setExtending (td.INTERLIS.RADIAN);
					gon.setConversionFactors (new NumericallyDerivedUnit.Factor[] {
					times200,
					byPi
					});
					u = gon;
					containingModel.addPreLast (gon);
					} catch (Exception ex) {
					reportInternalError (ex, grads.getLine());
					}
					} /* if (u == null) */
					
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			min=ili1_decimal();
			max=ili1_decimal();
			if ( inputState.guessing==0 ) {
				
				try {
				type = new NumericType (min, max);
				type.setUnit (u);
				} catch (Exception ex) {
				reportError (ex, lineNumber);
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	protected final NumericType  ili1_numericRange() throws RecognitionException, TokenStreamException {
		NumericType type;
		
		Token  lbrac = null;
		
		type = null;
		PrecisionDecimal min = null, max = null;
		
		
		try {      // for error handling
			lbrac = LT(1);
			match(LBRACE);
			min=ili1_decimal();
			match(DOTDOT);
			max=ili1_decimal();
			match(RBRACE);
			if ( inputState.guessing==0 ) {
				
				try {
				type = new NumericType (min, max);
				} catch (Exception ex) {
				reportError (ex, lbrac.getLine ());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	protected final TextType  ili1_textType() throws RecognitionException, TokenStreamException {
		TextType type;
		
		Token  star = null;
		
		type = null;
		int numChars = 0;
		
		
		try {      // for error handling
			match(LITERAL_TEXT);
			star = LT(1);
			match(STAR);
			numChars=posInteger();
			if ( inputState.guessing==0 ) {
				
				try {
				type = new TextType (numChars);
				} catch (Exception ex) {
				/* Correct only the case TEXT*0; do not correct other
				weird numbers, because we could not anticipate a
				reasonable alternative */
				if (numChars < 1)
				type = new TextType (1);
				reportError (ex, star.getLine());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	protected final TypeAlias  ili1_dateType() throws RecognitionException, TokenStreamException {
		TypeAlias type;
		
		Token  date = null;
		
		type = null;
		
		
		try {      // for error handling
			date = LT(1);
			match(LITERAL_DATE);
			if ( inputState.guessing==0 ) {
				
				type = new TypeAlias ();
				try {
				type.setAliasing (td.INTERLIS.INTERLIS_1_DATE);
				} catch (Exception ex) {
				reportInternalError (ex, date.getLine ());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	protected final EnumerationType  ili1_enumerationType() throws RecognitionException, TokenStreamException {
		EnumerationType type;
		
		
		type = null;
		ch.interlis.ili2c.metamodel.Enumeration enumer = null;
		
		
		try {      // for error handling
			enumer=enumeration(/* extending */ null);
			if ( inputState.guessing==0 ) {
				
				type = new EnumerationType ();
				try {
				type.setEnumeration (enumer);
				} catch (Exception ex) {
				reportError(ex, 0);
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	protected final TypeAlias  ili1_horizAlignment() throws RecognitionException, TokenStreamException {
		TypeAlias type;
		
		Token  halign = null;
		
		type = null;
		
		
		try {      // for error handling
			halign = LT(1);
			match(LITERAL_HALIGNMENT);
			if ( inputState.guessing==0 ) {
				
				type = new TypeAlias ();
				try {
				type.setAliasing (td.INTERLIS.HALIGNMENT);
				} catch (Exception ex) {
				reportInternalError (ex, halign.getLine ());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	protected final TypeAlias  ili1_vertAlignment() throws RecognitionException, TokenStreamException {
		TypeAlias type;
		
		Token  valign = null;
		
		type = null;
		
		
		try {      // for error handling
			valign = LT(1);
			match(LITERAL_VALIGNMENT);
			if ( inputState.guessing==0 ) {
				
				type = new TypeAlias ();
				try {
				type.setAliasing (td.INTERLIS.VALIGNMENT);
				} catch (Exception ex) {
				reportInternalError (ex, valign.getLine ());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	protected final PrecisionDecimal  ili1_decimal() throws RecognitionException, TokenStreamException {
		PrecisionDecimal dec;
		
		Token  d = null;
		Token  p = null;
		Token  n = null;
		
				dec = null;
			
		
		try {      // for error handling
			switch ( LA(1)) {
			case ILI1_DEC:
			{
				d = LT(1);
				match(ILI1_DEC);
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
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_17);
			} else {
			  throw ex;
			}
		}
		return dec;
	}
	
	protected final void ili1_form(
		LineType lineType, Model forModel
	) throws RecognitionException, TokenStreamException {
		
		Token  with = null;
		
		List lineFormList = null;
		LineForm curLineForm = null;
		LineForm[] lineForms = null;
		
		
		try {      // for error handling
			with = LT(1);
			match(LITERAL_WITH);
			match(LPAREN);
			curLineForm=ili1_lineForm(forModel);
			if ( inputState.guessing==0 ) {
				
				lineFormList = new LinkedList ();
				if (curLineForm != null)
				lineFormList.add (curLineForm);
				
			}
			{
			_loop1943:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					curLineForm=ili1_lineForm(forModel);
					if ( inputState.guessing==0 ) {
						
						if (curLineForm != null)
						lineFormList.add (curLineForm);
						
					}
				}
				else {
					break _loop1943;
				}
				
			} while (true);
			}
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				
				lineForms = (LineForm[]) lineFormList.toArray (new LineForm[lineFormList.size()]);
				try {
				if (lineType != null)
				lineType.setLineForms (lineForms);
				} catch (Exception ex) {
				reportError (ex, with.getLine ());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_18);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final void ili1_controlPoints(
		LineType lineType, Model inModel, Topic inTopic
	) throws RecognitionException, TokenStreamException {
		
		Token  vertex = null;
		Token  base = null;
		
		Type type = null;
		Domain controlPointDomain = null;
		
		
		try {      // for error handling
			vertex = LT(1);
			match(LITERAL_VERTEX);
			type=ili1_type(inModel, inTopic,null);
			{
			boolean synPredMatched1949 = false;
			if (((LA(1)==LITERAL_BASE))) {
				int _m1949 = mark();
				synPredMatched1949 = true;
				inputState.guessing++;
				try {
					{
					match(LITERAL_BASE);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched1949 = false;
				}
				rewind(_m1949);
inputState.guessing--;
			}
			if ( synPredMatched1949 ) {
				base = LT(1);
				match(LITERAL_BASE);
				match(EXPLANATION);
				if ( inputState.guessing==0 ) {
					
					reportWarning (rsrc.getString ("err_lineType_ili1_baseLost"),
					base.getLine ());
					
				}
			}
			else if ((_tokenSet_9.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
				Type realType = Type.findReal (type);
				if (realType != null)
				{
				if (realType instanceof CoordType)
				{
				if (!(type instanceof TypeAlias))
				{
				/* In INTERLIS-1, it is possible to define coordinates
				inside the line type. This is not possible with
				INTERLIS-2. So, declare a new "dummy" domain
				if needed.
				*/
				controlPointDomain = new Domain ();
				try {
				int numDomains = 0;
				Iterator iter = inModel.iterator();
				while (iter.hasNext())
				{
				if (iter.next() instanceof Domain)
				numDomains = numDomains + 1;
				}
				controlPointDomain.setName (formatMessage ("err_domain_artificialName",
				Integer.toString (numDomains + 1)));
				controlPointDomain.setType (type);
					      inModel.addPreLast(controlPointDomain);
				} catch (Exception ex) {
				reportError (ex, vertex.getLine ());
				controlPointDomain = null;
				}
				}
				else
				controlPointDomain = ((TypeAlias) type).getAliasing ();
				}
				else
				{
				reportError (formatMessage ("err_lineType_vertexNotCoordType",
				type.toString()),
				vertex.getLine ());
				}
				}
				
				if (lineType != null)
				{
				try {
				lineType.setControlPointDomain (controlPointDomain);
				} catch (Exception ex) {
				reportError (ex, vertex.getLine());
				}
				} /* if (lineType != null) */
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final void ili1_intersectionDef(
		LineType lineType
	) throws RecognitionException, TokenStreamException {
		
		Token  without = null;
		
		PrecisionDecimal maxOverlap = null;
		
		
		try {      // for error handling
			without = LT(1);
			match(LITERAL_WITHOUT);
			match(LITERAL_OVERLAPS);
			match(GREATER);
			maxOverlap=ili1_decimal();
			if ( inputState.guessing==0 ) {
				
				if ((lineType != null) && (maxOverlap != null))
				{
				try {
				lineType.setMaxOverlap (maxOverlap);
				} catch (Exception ex) {
				reportError (ex, without.getLine ());
				}
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final void ili1_lineAttributes(
		LineType lineType, Container scope,Viewable table
	) throws RecognitionException, TokenStreamException {
		
		Token  att = null;
		
		Table lineAttrStruct = null;
		
		
		try {      // for error handling
			att = LT(1);
			match(LITERAL_LINEATTR);
			match(EQUALS);
			if ( inputState.guessing==0 ) {
				
				lineAttrStruct = createImplicitLineAttrStructure (scope, table, att.getLine());
				try {
				((SurfaceOrAreaType) lineType).setLineAttributeStructure (lineAttrStruct);
				} catch (Exception ex) {
				reportInternalError (ex, att.getLine ());
				panic ();
				}
				
			}
			{
			int _cnt1952=0;
			_loop1952:
			do {
				if ((LA(1)==NAME)) {
					ili1_attribute(lineAttrStruct);
				}
				else {
					if ( _cnt1952>=1 ) { break _loop1952; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt1952++;
			} while (true);
			}
			{
			if ((LA(1)==LITERAL_NO||LA(1)==LITERAL_IDENT)) {
				ili1_identifications(lineAttrStruct);
			}
			else if ((LA(1)==LITERAL_END)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(LITERAL_END);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final LineForm  ili1_lineForm(
		Model forModel
	) throws RecognitionException, TokenStreamException {
		LineForm lineForm;
		
		Token  expl = null;
		
		lineForm = null;
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_STRAIGHTS:
			{
				match(LITERAL_STRAIGHTS);
				if ( inputState.guessing==0 ) {
					
					lineForm = td.INTERLIS.STRAIGHTS;
					
				}
				break;
			}
			case LITERAL_ARCS:
			{
				match(LITERAL_ARCS);
				if ( inputState.guessing==0 ) {
					
					lineForm = td.INTERLIS.ARCS;
					
				}
				break;
			}
			case EXPLANATION:
			{
				expl = LT(1);
				match(EXPLANATION);
				if ( inputState.guessing==0 ) {
					
					/* Check out whether there is already a line form with the same
					explanation. */
					lineForm = addLineFormIfNoSuchExplanation (forModel,
					expl.getText (),
					expl.getLine ());
					
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_2);
			} else {
			  throw ex;
			}
		}
		return lineForm;
	}
	
	protected final void ili1_viewDef() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_VERTEXINFO:
			{
				match(LITERAL_VERTEXINFO);
				match(NAME);
				match(EXPLANATION);
				break;
			}
			case LITERAL_WITH:
			{
				match(LITERAL_WITH);
				match(LITERAL_PERIPHERY);
				match(NAME);
				break;
			}
			case LITERAL_CONTOUR:
			{
				match(LITERAL_CONTOUR);
				match(NAME);
				{
				if ((LA(1)==LITERAL_WITH)) {
					match(LITERAL_WITH);
					match(LITERAL_PERIPHERY);
				}
				else if ((LA(1)==COMMA||LA(1)==SEMI)) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				break;
			}
			case LESSMINUS:
			{
				match(LESSMINUS);
				match(NAME);
				match(DOT);
				match(NAME);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_19);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final void ili1_font(
		Ili1Format format
	) throws RecognitionException, TokenStreamException {
		
		Token  expl = null;
		
		try {      // for error handling
			match(LITERAL_FONT);
			match(EQUALS);
			expl = LT(1);
			match(EXPLANATION);
			match(SEMI);
			if ( inputState.guessing==0 ) {
				format.font=expl.getText();
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_20);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final void ili1_specialCharacter(
		Ili1Format format
	) throws RecognitionException, TokenStreamException {
		
		
		int blankCode = 0x5f;
		int undefinedCode = 0x40;
		int continueCode = 0x5c;
		
		
		try {      // for error handling
			match(LITERAL_BLANK);
			match(EQUALS);
			{
			if ((LA(1)==LITERAL_DEFAULT)) {
				match(LITERAL_DEFAULT);
			}
			else if ((LA(1)==POSINT||LA(1)==HEXNUMBER)) {
				blankCode=code();
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(COMMA);
			match(LITERAL_UNDEFINED);
			match(EQUALS);
			{
			if ((LA(1)==LITERAL_DEFAULT)) {
				match(LITERAL_DEFAULT);
			}
			else if ((LA(1)==POSINT||LA(1)==HEXNUMBER)) {
				undefinedCode=code();
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(COMMA);
			match(LITERAL_CONTINUE);
			match(EQUALS);
			{
			if ((LA(1)==LITERAL_DEFAULT)) {
				match(LITERAL_DEFAULT);
			}
			else if ((LA(1)==POSINT||LA(1)==HEXNUMBER)) {
				continueCode=code();
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(SEMI);
			if ( inputState.guessing==0 ) {
				
					  format.blankCode=blankCode;
					  format.undefinedCode=undefinedCode;
					  format.continueCode=continueCode;
					
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_21);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final void ili1_transferId(
		Ili1Format format
	) throws RecognitionException, TokenStreamException {
		
		Token  exp = null;
		
		try {      // for error handling
			match(LITERAL_TID);
			match(EQUALS);
			{
			switch ( LA(1)) {
			case 69:
			{
				match(69);
				if ( inputState.guessing==0 ) {
					format.tidKind=Ili1Format.TID_I16;
				}
				break;
			}
			case 70:
			{
				match(70);
				if ( inputState.guessing==0 ) {
					format.tidKind=Ili1Format.TID_I32;
				}
				break;
			}
			case LITERAL_ANY:
			{
				match(LITERAL_ANY);
				if ( inputState.guessing==0 ) {
					format.tidKind=Ili1Format.TID_ANY;
				}
				break;
			}
			case EXPLANATION:
			{
				exp = LT(1);
				match(EXPLANATION);
				if ( inputState.guessing==0 ) {
					format.tidKind=Ili1Format.TID_EXPLANATION;
						format.tidExplanation=exp.getText();
						
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(SEMI);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_14);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final int  code() throws RecognitionException, TokenStreamException {
		int i;
		
		Token  p = null;
		Token  h = null;
		
		i = 0;
		
		
		try {      // for error handling
			if ((LA(1)==POSINT)) {
				p = LT(1);
				match(POSINT);
				if ( inputState.guessing==0 ) {
					i = Integer.parseInt(p.getText());
				}
			}
			else if ((LA(1)==HEXNUMBER)) {
				h = LT(1);
				match(HEXNUMBER);
				if ( inputState.guessing==0 ) {
					i = Integer.parseInt(h.getText().substring(2), 16);
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_19);
			} else {
			  throw ex;
			}
		}
		return i;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"'('",
		"','",
		"')'",
		"NAME",
		"\"END\"",
		"POSINT",
		"\"TRANSFER\"",
		"';'",
		"\"MODEL\"",
		"'.'",
		"\"DOMAIN\"",
		"'='",
		"\"TOPIC\"",
		"\"OPTIONAL\"",
		"\"TABLE\"",
		"':'",
		"'->'",
		"EXPLANATION",
		"\"NO\"",
		"\"IDENT\"",
		"\"COORD2\"",
		"\"COORD3\"",
		"'['",
		"'..'",
		"']'",
		"\"DIM1\"",
		"\"DIM2\"",
		"\"RADIANS\"",
		"\"DEGREES\"",
		"\"GRADS\"",
		"\"TEXT\"",
		"'*'",
		"\"DATE\"",
		"\"HALIGNMENT\"",
		"\"VALIGNMENT\"",
		"\"POLYLINE\"",
		"\"WITHOUT\"",
		"\"SURFACE\"",
		"\"AREA\"",
		"\"LINEATTR\"",
		"\"WITH\"",
		"\"STRAIGHTS\"",
		"\"ARCS\"",
		"\"OVERLAPS\"",
		"'>'",
		"\"VERTEX\"",
		"\"BASE\"",
		"\"DERIVATIVES\"",
		"\"VIEW\"",
		"\"VERTEXINFO\"",
		"\"PERIPHERY\"",
		"\"CONTOUR\"",
		"'<-'",
		"\"FORMAT\"",
		"\"FREE\"",
		"\"FIX\"",
		"\"LINESIZE\"",
		"\"TIDSIZE\"",
		"\"CODE\"",
		"\"FONT\"",
		"\"BLANK\"",
		"\"DEFAULT\"",
		"\"UNDEFINED\"",
		"\"CONTINUE\"",
		"\"TID\"",
		"\"I16\"",
		"\"I32\"",
		"\"ANY\"",
		"HEXNUMBER",
		"ILI1_DEC",
		"NUMBER",
		"PLUS",
		"MINUS",
		"WS",
		"ILI_METAVALUE",
		"SL_COMMENT",
		"ILI_DOC",
		"ML_COMMENT",
		"ESC",
		"STRING",
		"DIGIT",
		"LETTER",
		"ILI1_SCALING",
		"HEXDIGIT",
		"NUMERICSTUFF"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 1135795513591904L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 96L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 8192L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 1135795513591840L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 479488L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 65792L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 148618787703226368L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 4611686018427387904L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 1135795513591808L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = { 409856L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { 7661803274384L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = { 7661802225808L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { 12583296L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = { 256L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { 2099200L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = { 7661802225680L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = { 1135795916245504L, 1536L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = { 562949953421312L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	private static final long[] mk_tokenSet_19() {
		long[] data = { 2080L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
	private static final long[] mk_tokenSet_20() {
		long[] data = { 0L, 1L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
	private static final long[] mk_tokenSet_21() {
		long[] data = { 0L, 16L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
	
	}
