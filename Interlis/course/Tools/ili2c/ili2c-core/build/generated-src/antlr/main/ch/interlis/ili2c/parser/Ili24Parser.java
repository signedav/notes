// $ANTLR : "interlis24.g" -> "Ili24Parser.java"$

	package ch.interlis.ili2c.parser;
	import ch.interlis.ili2c.metamodel.*;
	import ch.interlis.ili2c.generator.Interlis2Generator;
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

public class Ili24Parser extends antlr.LLkParser       implements Ili24ParserTokenTypes
 {

  protected PredefinedModel modelInterlis;
  protected Type predefinedBooleanType;
  protected Table predefinedScalSystemClass;
  protected Table predefinedCoordSystemClass;
  protected TransferDescription td;
  private Ili24Lexer lexer;
  private antlr.TokenStreamHiddenTokenFilter filter;
  private boolean checkMetaObjs;
  private Ili2cMetaAttrs externalMetaAttrs=new Ili2cMetaAttrs();

  /** Parse the contents of a stream according to INTERLIS-1 or INTERLIS-2 syntax
      (the version is detected automatically by the parser) and add the
      encountered contents to this TransferDescription.

      @return false if there have been any fatal errors which would lead
              this TransferDescription in an inconsistent state.
  */
  static public boolean parseIliFile (TransferDescription td
    ,String filename
    ,java.io.Reader stream
    ,boolean checkMetaObjects
    ,int line0Offest
    ,Ili2cMetaAttrs metaAttrs
    )
  {
  	return parseIliFile (td,filename,new Ili24Lexer (stream),checkMetaObjects,line0Offest,metaAttrs);
  }
  static public boolean parseIliFile (TransferDescription td
    ,String filename
    ,java.io.InputStream stream
    ,boolean checkMetaObjects
    ,int line0Offest
    ,Ili2cMetaAttrs metaAttrs
    )
  {
  	return parseIliFile (td,filename,new Ili24Lexer (stream),checkMetaObjects,line0Offest,metaAttrs);
  }
  static public boolean parseIliFile (TransferDescription td
    ,String filename
    ,Ili24Lexer lexer
    ,boolean checkMetaObjects
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
      Ili24Parser parser = new Ili24Parser (filter);
      
      // Ili2.3 always check existence of metaobject
      parser.checkMetaObjs=true; // checkMetaObjects;
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

	/** check if there is a attribute with the given name.
	* @returns null if no attribute found or scope not a Viewable
	*/
	protected AttributeDef findAttribute(Container scope,String name)
	{
		if(!(scope instanceof Viewable))return null;
		Viewable currentViewable=(Viewable)scope;
		AttributeDef attrdef=null;
		Iterator it=currentViewable.getAttributes();
		while(it.hasNext()){
			AttributeDef ele=(AttributeDef)it.next();
			if(ele.getName().equals(name)){
				attrdef=ele;
				break;
			}
		}
		return attrdef; // may be null if no attribute found
	}
	/** check if the given name is part of a AttributeRef, that is 
	*   not a reference attribute and not a proxy for a 
	*   basename inside a viewable.
	*   semantic predicate
	*/
	protected boolean isAttributeRef(Viewable v,String name)
	{
			AttributeDef attr=findAttribute(v,name);
			if(attr==null){
			    // no attribute name in v
			    return false;
			}
			// check if attribute is a reference attribute
			Type type=attr.getDomainResolvingAliases();
			if(type instanceof ReferenceType){
				// attr is a reference attribute
				return false;
			}
			if(type instanceof ObjectType){
				// attr is a proxy for a basename
				return false;
			}
		return true;
	}
	protected void validateFormattedConst(FormattedType ft,String value, int srcLine)
	{
			try{
				if(!ft.isValueInRange(value)){
					reportError(formatMessage("err_formattedType_valueOutOfRange",value),srcLine);
				}
			}catch(NumberFormatException ex){
				reportError(formatMessage("err_formattedType_illegalFormat",value),srcLine);
			}
	}
    protected void validateEqualsArgumentTypes(Evaluable exprRet, Evaluable expr, Evaluable comparedWith,int srcLine)
    {
            if(!expr.isDirty() && !comparedWith.isDirty()){
            	if(expr instanceof Constant.Undefined || comparedWith instanceof Constant.Undefined){
            		// UNDEFINED is always == or !=
            	}else{
					Type expr1Type=expr.getType();
					Type expr2Type=comparedWith.getType();
					if(expr1Type!=null && expr2Type!=null){
						if(expr1Type.resolveAliases() instanceof TextType && expr2Type.resolveAliases() instanceof TextType){
							// text
						}else if(expr1Type.resolveAliases() instanceof CompositionType && expr2Type.resolveAliases() instanceof CompositionType){
							// structs
						}else if(expr1Type.resolveAliases() instanceof OIDType && expr2Type.resolveAliases() instanceof OIDType){
						}else if(expr1Type.resolveAliases() instanceof ClassType && expr2Type.resolveAliases() instanceof ClassType){
						}else if(expr1Type.resolveAliases() instanceof AttributePathType && expr2Type.resolveAliases() instanceof AttributePathType){
						}else if(expr1Type.resolveAliases() instanceof NumericType && expr2Type.resolveAliases() instanceof NumericType){
							// numeric
						}else if(expr1Type.resolveAliases() instanceof FormattedType && expr2Type.resolveAliases() instanceof FormattedType){
							// formatted
						}else if(expr1Type.resolveAliases() instanceof FormattedType && comparedWith instanceof Constant.Text){
							// formatted
							validateFormattedConst((FormattedType)expr1Type.resolveAliases(),((Constant.Text)comparedWith).getValue(),srcLine);
						}else if(expr instanceof Constant.Text && expr2Type.resolveAliases() instanceof FormattedType){
							// formatted
							validateFormattedConst((FormattedType)expr2Type.resolveAliases(),((Constant.Text)expr).getValue(),srcLine);
						}else if(expr1Type.resolveAliases() instanceof CoordType && expr2Type.resolveAliases() instanceof CoordType){
							// coord
						}else if(expr1Type.resolveAliases() instanceof EnumerationType && expr2Type.resolveAliases() instanceof EnumerationType){
							// enum
							if(expr instanceof Constant.Enumeration && comparedWith instanceof Constant.Enumeration){
								// both factors are constants of unknown enumerations
							}else if(expr instanceof Constant.Enumeration){
								// validate that constant is a member of the enumeration type
								String value=((Constant.Enumeration)expr).toString();
								List<String> values=((EnumerationType)expr2Type.resolveAliases()).getValues();
								if(!values.contains(value.substring(1))){
									reportError (formatMessage ("err_enumerationType_MissingEle",value),
										srcLine);
								}
								((Constant.Enumeration)expr).setType((EnumerationType)expr2Type.resolveAliases());
							}else if(comparedWith instanceof Constant.Enumeration){
								// validate that constant is a member of the enumeration type
								String value=((Constant.Enumeration)comparedWith).toString();
								List<String> values=((EnumerationType)expr1Type.resolveAliases()).getValues();
								if(!values.contains(value.substring(1))){
									reportError (formatMessage ("err_enumerationType_MissingEle",value),
										srcLine);
								}
								((Constant.Enumeration)comparedWith).setType((EnumerationType)expr1Type.resolveAliases());
							}else{
							 if(expr1Type.resolveAliases()!=expr2Type.resolveAliases()){
								reportError (formatMessage ("err_expr_incompatibleTypes",(String)null),
										 srcLine);
								exprRet.setDirty(true);
							 }
							}
						}else if(expr1Type.resolveAliases() instanceof EnumTreeValueType && expr2Type.resolveAliases() instanceof EnumTreeValueType){
							 if(expr1Type.resolveAliases()!=expr2Type.resolveAliases()){
								reportError (formatMessage ("err_expr_incompatibleTypes",(String)null),
										 srcLine);
								exprRet.setDirty(true);
							 }
						}else if(expr instanceof Constant.Enumeration && expr2Type.resolveAliases() instanceof EnumTreeValueType){
								// validate that constant is a member of the enumeration type
								String value=((Constant.Enumeration)expr).toString();
								List<String> values=((EnumTreeValueType)expr2Type.resolveAliases()).getValues();
								if(!values.contains(value.substring(1))){
									reportError (formatMessage ("err_enumerationType_MissingEle",value),
										srcLine);
								}
								((Constant.Enumeration)expr).setType((EnumTreeValueType)expr2Type.resolveAliases());
						}else if(expr1Type.resolveAliases() instanceof EnumTreeValueType && comparedWith instanceof Constant.Enumeration){
								// validate that constant is a member of the enumeration type
								String value=((Constant.Enumeration)comparedWith).toString();
								List<String> values=((EnumTreeValueType)expr1Type.resolveAliases()).getValues();
								if(!values.contains(value.substring(1))){
									reportError (formatMessage ("err_enumerationType_MissingEle",value),
										srcLine);
								}
								((Constant.Enumeration)comparedWith).setType((EnumTreeValueType)expr1Type.resolveAliases());
						}else if(expr1Type.resolveAliases() instanceof ObjectType && expr2Type.resolveAliases() instanceof ObjectType){
							// object
						}else{
							reportError (formatMessage ("err_expr_incompatibleTypes",(String)null),
									 srcLine);
							exprRet.setDirty(true);
						}
					}else{
							reportError (formatMessage ("err_expr_incompatibleTypes",(String)null),
									 srcLine);
							exprRet.setDirty(true);
					}
            	}
            }
    }
    protected void validateCompareArgumentTypes(Evaluable exprRet, Evaluable expr, Evaluable comparedWith,int srcLine)
    {
            if(!expr.isDirty() && !comparedWith.isDirty()){
            	{
					Type expr1Type=expr.getType();
					Type expr2Type=comparedWith.getType();
					if(expr1Type!=null && expr2Type!=null){
						if(expr1Type.resolveAliases() instanceof NumericType && expr2Type.resolveAliases() instanceof NumericType){
							// numeric
						}else if(expr1Type.resolveAliases() instanceof FormattedType && expr2Type.resolveAliases() instanceof FormattedType){
							// formatted
						}else if(expr1Type.resolveAliases() instanceof FormattedType && comparedWith instanceof Constant.Text){
							// formatted
							validateFormattedConst((FormattedType)expr1Type.resolveAliases(),((Constant.Text)comparedWith).getValue(),srcLine);
						}else if(expr instanceof Constant.Text && expr2Type.resolveAliases() instanceof FormattedType){
							// formatted
							validateFormattedConst((FormattedType)expr2Type.resolveAliases(),((Constant.Text)expr).getValue(),srcLine);
						}else if(expr1Type.resolveAliases() instanceof EnumerationType && expr2Type.resolveAliases() instanceof EnumerationType){
							// enum
							if(expr instanceof Constant.Enumeration && comparedWith instanceof Constant.Enumeration){
								// both factors are constants of unknown enumerations
							}else if(expr instanceof Constant.Enumeration){
								// validate that constant is a member of the enumeration type
								EnumerationType enumType=(EnumerationType)expr2Type.resolveAliases();
								if(enumType.isOrdered() && !enumType.isCircular()){
									String value=((Constant.Enumeration)expr).toString();
									List<String> values=enumType.getValues();
									if(!values.contains(value.substring(1))){
										reportError (formatMessage ("err_enumerationType_MissingEle",value),
											srcLine);
									}
								}else{
									reportError (formatMessage ("err_expr_incompatibleTypes",(String)null),
											 srcLine);
									exprRet.setDirty(true);
								}
							}else if(comparedWith instanceof Constant.Enumeration){
								// validate that constant is a member of the enumeration type
								EnumerationType enumType=(EnumerationType)expr1Type.resolveAliases();
								if(enumType.isOrdered() && !enumType.isCircular()){
									String value=((Constant.Enumeration)comparedWith).toString();
									List<String> values=enumType.getValues();
									if(!values.contains(value.substring(1))){
										reportError (formatMessage ("err_enumerationType_MissingEle",value),
											srcLine);
									}
								}else{
									reportError (formatMessage ("err_expr_incompatibleTypes",(String)null),
											 srcLine);
									exprRet.setDirty(true);
								}
							}else{
							 if(expr1Type.resolveAliases()!=expr2Type.resolveAliases()){
								reportError (formatMessage ("err_expr_incompatibleTypes",(String)null),
										 srcLine);
								exprRet.setDirty(true);
							 }else{
								EnumerationType enumType=(EnumerationType)expr1Type.resolveAliases();
								if(enumType.isOrdered() && !enumType.isCircular()){
									// ok
								}else{
									reportError (formatMessage ("err_expr_incompatibleTypes",(String)null),
											 srcLine);
									exprRet.setDirty(true);
								}
							 }
							}
						}else if(expr1Type.resolveAliases() instanceof EnumTreeValueType && expr2Type.resolveAliases() instanceof EnumTreeValueType){
							 if(expr1Type.resolveAliases()!=expr2Type.resolveAliases()){
								reportError (formatMessage ("err_expr_incompatibleTypes",(String)null),
										 srcLine);
								exprRet.setDirty(true);
							 }else{
								EnumerationType enumType=(EnumerationType)((EnumTreeValueType)expr1Type.resolveAliases()).getEnumType().getType().resolveAliases();
								if(enumType.isOrdered() && !enumType.isCircular()){
									// ok
								}else{
									reportError (formatMessage ("err_expr_incompatibleTypes",(String)null),
											 srcLine);
									exprRet.setDirty(true);
								}
							 }
						}else if(expr instanceof Constant.Enumeration && expr2Type.resolveAliases() instanceof EnumTreeValueType){
								// validate that constant is a member of the enumeration type
								EnumerationType enumType=(EnumerationType)((EnumTreeValueType)expr2Type.resolveAliases()).getEnumType().getType().resolveAliases();
								if(enumType.isOrdered() && !enumType.isCircular()){
									String value=((Constant.Enumeration)expr).toString();
									List<String> values=((EnumTreeValueType)expr2Type.resolveAliases()).getValues();
									if(!values.contains(value.substring(1))){
										reportError (formatMessage ("err_enumerationType_MissingEle",value),
											srcLine);
									}
								}else{
									reportError (formatMessage ("err_expr_incompatibleTypes",(String)null),
											 srcLine);
									exprRet.setDirty(true);
								}
						}else if(expr1Type.resolveAliases() instanceof EnumTreeValueType && comparedWith instanceof Constant.Enumeration){
								// validate that constant is a member of the enumeration type
								EnumerationType enumType=(EnumerationType)((EnumTreeValueType)expr1Type.resolveAliases()).getEnumType().getType().resolveAliases();
								if(enumType.isOrdered() && !enumType.isCircular()){
									String value=((Constant.Enumeration)comparedWith).toString();
									List<String> values=((EnumTreeValueType)expr1Type.resolveAliases()).getValues();
									if(!values.contains(value.substring(1))){
										reportError (formatMessage ("err_enumerationType_MissingEle",value),
											srcLine);
									}
								}else{
									reportError (formatMessage ("err_expr_incompatibleTypes",(String)null),
											 srcLine);
									exprRet.setDirty(true);
								}
						}else{
							reportError (formatMessage ("err_expr_incompatibleTypes",(String)null),
									 srcLine);
							exprRet.setDirty(true);
						}
					}else{
							reportError (formatMessage ("err_expr_incompatibleTypes",(String)null),
									 srcLine);
							exprRet.setDirty(true);
					}
            	}
            }
    }

	protected Domain resolveDomainRef(Container scope,String[] nams, int lin)
	{
	      Model model;
	      AbstractPatternDef topic;
	      String domainName=null;
	      Domain d=null;

	switch (nams.length) {
	      case 1:
		model = (Model) scope.getContainerOrSame(Model.class);
		topic = (AbstractPatternDef) scope.getContainerOrSame(AbstractPatternDef.class);
		domainName = (String) nams[0];
		break;

	      case 2:
		model = resolveOrFixModelName (scope, nams[0], lin);
		topic = null;
		domainName = nams[1];
		break;

	      case 3:
		model = resolveOrFixModelName (scope, nams[0], lin);
		topic = resolveOrFixTopicName (model, nams[1], lin);
		domainName = nams[2];
		break;

	      default:
		reportError(rsrc.getString("err_domainRef_weird"), lin);
		model = resolveModelName (scope, nams[0]);
		topic = null;
		if (model == null)
		  model = (Model) scope.getContainerOrSame(Model.class);
		domainName = (String) nams[nams.length - 1];
		break;
	      }

	      d = null;
	      if (topic != null)
		d = (Domain) topic.getRealElement (Domain.class, domainName);

	      if ((d == null) && ((topic == null) | (nams.length == 1)))
		d = (Domain) model.getRealElement(Domain.class, domainName);

	      if ((d == null) && (nams.length == 1)){
	      	// unqualified name; search also in unqaulified imported models
		d = (Domain) model.getImportedElement(Domain.class, domainName);
	      }
	      if (d == null)
	      {
		if (topic == null)
		  reportError(
		    formatMessage ("err_domainRef_notInModel", domainName, model.toString()),
		    lin);
		else
		  reportError(
		    formatMessage ("err_domainRef_notInModelOrTopic", domainName,
				   topic.toString(), model.toString()),
		    lin);

		try {
		  d = new Domain();
		  d.setName(domainName);
		  if (topic == null)
		    model.add(d);
		  else
		    topic.add(d);
		} catch (Exception ex) {
		  panic();
		}
	      }
	      return d;
	}
	protected Element resolveStructureOrDomainRef(Container scope,String[] nams,int lin)
	{
      Model model;
      AbstractPatternDef    topic;
      String   modelName, topicName,tableName;
      Table t;

      switch (nams.length) {
      case 1:
        model = (Model) scope.getContainerOrSame(Model.class);
        topic = (AbstractPatternDef) scope.getContainerOrSame(AbstractPatternDef.class);
        tableName = nams[0];
        break;

      case 2:
        modelName = nams[0];
        model = resolveOrFixModelName(scope, modelName, lin);
        tableName = nams[1];
        topic = null;
        break;

      case 3:
        modelName = nams[0];
        topicName = nams[1];
        model = resolveOrFixModelName(scope, modelName, lin);
        topic = resolveOrFixTopicName(model, topicName, lin);
        tableName = nams[2];
        break;

      default:
        reportError(rsrc.getString("err_weirdTableRef"), lin);
        model = resolveModelName(scope, nams[0]);
        if (model == null)
          model = (Model) scope.getContainerOrSame(Model.class);

        topic = null;
        tableName = nams[nams.length - 1];
        break;
      }

      t = null;
      if (topic != null)
        t = (Table) topic.getElement (Table.class, tableName);
      if ((t == null) && (model != null))
        t = (Table) model.getElement (Table.class, tableName);
      if ((t == null) && (nams.length == 1))
        t = (Table) model.getImportedElement (Table.class, tableName);
        if(t!=null){
		if(t.isIdentifiable()){
			reportError(formatMessage("err_structRef_StructRequired",t.getScopedName(null)),lin);
		}
          return t;
        }
                 return resolveDomainRef(scope,nams,lin);
	}

	protected GraphicParameterDef resolveRuntimeParameterRef(Container scope,String[] nams, int lin)
	{
	      Model model;
	      String domainName=null;
	      GraphicParameterDef d=null;

	switch (nams.length) {
	      case 1:
		model = (Model) scope.getContainerOrSame(Model.class);
		domainName = (String) nams[0];
		break;

	      case 2:
		model = resolveOrFixModelName (scope, nams[0], lin);
		domainName = nams[1];
		break;

	      default:
		reportError(rsrc.getString("err_runtimeParamRef_weird"), lin);
		model = resolveModelName (scope, nams[0]);
		if (model == null)
		  model = (Model) scope.getContainerOrSame(Model.class);
		domainName = (String) nams[nams.length - 1];
		break;
	      }

	      d = null;
	      d = (GraphicParameterDef) model.getRealElement(GraphicParameterDef.class, domainName);
	      if ((d == null) && (nams.length == 1)){
	      	// unqualified name; search also in unqaulified imported models
		d = (GraphicParameterDef) model.getImportedElement(GraphicParameterDef.class, domainName);
	      }
	      if (d == null)
	      {
		  reportError(
		    formatMessage ("err_runtimeParamRef_notInModel", domainName, model.toString()),
		    lin);
	      }
	      return d;
	}
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


  protected void reportError (String message, int lineNumber)
  {
      String filename=getFilename();
      CompilerLogEvent.logError(filename,lineNumber,message);
  }


  protected void reportWarning (String message, int lineNumber)
  {
      String filename=getFilename();
      CompilerLogEvent.logWarning(filename,lineNumber,message);
  }


  protected void reportError (Throwable ex, int lineNumber)
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
  protected void reportError (Ili2cSemanticException ex)
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

  protected void reportInternalError(int lineNumber)
  {
      String filename=getFilename();
      CompilerLogEvent.logError(filename,lineNumber,formatMessage("err_internalCompilerError", /* exception */ ""));
  }


  protected void reportInternalError (Throwable ex, int lineNumber)
  {
      String filename=getFilename();
      CompilerLogEvent.logError(filename,lineNumber,formatMessage("err_internalCompilerError", ""),ex);
  }


  protected String formatMessage (String msg, Object[] args)
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


  protected String formatMessage(String msg, String arg) {
    return formatMessage(msg, new Object[] { arg });
  }


  protected String formatMessage(String msg, String arg1, String arg2) {
    return formatMessage(msg, new Object[] { arg1, arg2 });
  }

  protected String formatMessage(String msg, String arg1, String arg2, String arg3) {
    return formatMessage(msg, new Object[] { arg1, arg2, arg3 });
  }

  public static void panic ()
  {
    throw new antlr.ANTLRError();
  }


  protected Model resolveModelName (Container scope, String modelName)
  {
    Model scopeModel;

    if (scope instanceof TransferDescription)
      scopeModel = null;
    else
      scopeModel = (Model) scope.getContainerOrSame (Model.class);

    if ((scopeModel != null)
        && modelName.equals(((Model) scopeModel).getName()))
      return scopeModel;

    return (Model) td.getRealElement (Model.class, modelName);
  }


  protected Model resolveOrFixModelName(
      Container    scope,
      String                      modelName,
      int                         line)
  {
    Model m;
    Model scopeModel = (Model) scope.getContainerOrSame (Model.class);

    m = resolveModelName (scope, modelName);
    if (m != null)
    {
      if ((scopeModel != null)
          && (scopeModel != m)
          && !scopeModel.isImporting (m) && m!=modelInterlis)
      {
        reportError (formatMessage ("err_model_notImported",
          scopeModel.toString(), m.toString()),
          line);

        try {
          scopeModel.addImport (m,false);
        } catch (Exception ex) {
          panic ();
        }
      }
      return m;
    }

    if (scopeModel != null) {
      Topic referredTopic = (Topic) scopeModel.getRealElement (Topic.class, modelName);
      if (referredTopic != null)
      {
        reportError (formatMessage (
          "err_topicRef_withoutModelScope", referredTopic.toString()), line);
        panic ();
      }
    }

    /* find the model and transfer description that contain scope */
    try {
      reportError(formatMessage("err_noSuchModel", modelName), line);
      Model artificialModel = new DataModel ();
      artificialModel.setName (modelName);
      td.add (artificialModel);
    } catch (Exception ex) {
      /* Can't fix, for whatever reason this might occur */
      panic();
    }

    /* Check out whether the fix was successful. Should
       always be the case when reaching this line. */
    m = resolveModelName(scope, modelName);
    if (m == null)
      panic();

    return m;
  }


  protected Topic resolveOrFixTopicName(
      Model      model,
      String     topicName,
      int        line)
  {
    Topic  topic;

    topic = (Topic) model.getRealElement(Topic.class, topicName);
    if (topic == null) {
      reportError(
        formatMessage("err_noSuchTopic", topicName, model.toString()),
        line);
      try {
        topic = new Topic();
        topic.setName(topicName);
        model.add(topic);
      } catch (Exception ex) {
        panic();
      }
    }
    return topic;
  }


  protected MetaObject resolveMetaObject (MetaDataUseDef basket, Table polymorphicTo, String name, int line)
  {

  List matching = basket.findMatchingMetaObjects (polymorphicTo, name);
    if (matching.size() >= 2)
    {
      reportError (formatMessage ("err_metaObject_refAmbiguous",
                                  name,
                                  ((MetaObject) matching.get(0)).getScopedName (null),
                                  ((MetaObject) matching.get(1)).getScopedName (null)),
                   line);
      return (MetaObject) matching.get(0);
    }
    else if (matching.size() == 1)
      return (MetaObject) matching.get(0);
    else
    {
      /* Nothing found. */
      reportError (formatMessage ("err_metaObject_unknownName",
                                  name,
                                  basket.getScopedName(null),polymorphicTo.getScopedName(null)),
                   line);
      return null;
    }
  }

	protected MetaDataUseDef resolveOrFixBasketName(Container container,String basketName,int line)
 	{
	        Model model = (Model) container.getContainerOrSame(Model.class);
	        AbstractPatternDef topic = (AbstractPatternDef) container.getContainerOrSame(AbstractPatternDef.class);
		MetaDataUseDef basket=null;
		if(topic!=null){
			basket=  (MetaDataUseDef) topic.getRealElement (MetaDataUseDef.class, basketName);
		}
		if(basket==null){
			basket=  (MetaDataUseDef) model.getRealElement (MetaDataUseDef.class, basketName);
		}
		if(basket!=null){
			return basket;
		}
		reportError (formatMessage ("err_noSuchMetaDataUseDef",
			basketName, container.toString()),
                   line);
		try {
		  MetaDataUseDef ref = new MetaDataUseDef();
		  ref.setName(basketName);
		  if (topic != null)
		    topic.add(ref);
		  else
		    model.add(ref);
		  return ref;
		} catch (Exception ex) {
		  panic();
		}

		return null;
	}

  protected AttributeDef findOverridingAttribute (
    Viewable container, int mods, String name, int line)
  {
    boolean      declaredExtended = (mods & ch.interlis.ili2c.metamodel.Properties.eEXTENDED) != 0;
    AttributeDef overriding;

    overriding =  (AttributeDef) container.getRealElement (AttributeDef.class, name);
    if ((overriding == null) && declaredExtended)
    {
      reportError (formatMessage ("err_noAttrToExtend",
                                  name, container.toString()),
                   line);
    }

    if ((overriding != null)
        && (overriding.getContainer(Viewable.class) == container))
    {
      reportError (formatMessage ("err_attrNameInSameViewable",
                                  container.toString(), name),
                   line);
    }
    else if ((overriding != null) && !declaredExtended)
    {
      reportError (formatMessage ("err_attrExtendedWithoutDecl",
                                  name, container.toString(),
                                  overriding.toString()),
                   line);
    }
    return overriding;
  }
	private Table buildDecomposedStruct(AttributeDef attrdef,boolean areaDecomp)
		throws java.beans.PropertyVetoException
	{
		Type type=attrdef.getDomainResolvingAliases(); 
		if(type instanceof CompositionType){
			// composition
			return ((CompositionType)type).getComponentType();
		}else if(type instanceof PolylineType){
			// polyline
			return ((PolylineType)type).getImplicitLineGeometryStructure();
		}else if(type instanceof SurfaceType){
			// surface
			return ((SurfaceType)type).getImplicitSurfaceBoundaryStructure();
		}else if(type instanceof AreaType){
			// area
			if(areaDecomp){
				return ((AreaType)type).getImplicitSurfaceEdgeStructure();
			}else{
				return ((AreaType)type).getImplicitSurfaceBoundaryStructure();
			}
		}else{
			throw new IllegalArgumentException(formatMessage(
				"err_decompositionView_notDecomposable",attrdef.getName()
			));
		}
	}
	private AttributeDef getBaseViewableProxyAttr(Viewable start1,String base,int line)
	{
		AttributeDef baseProxy =  (AttributeDef)start1.getRealElement (AttributeDef.class, base);
		if(baseProxy==null){
			return null;
		}
		Type proxyType=baseProxy.getDomain();
		if(!(proxyType instanceof ObjectType)){
			return null;
		}
		return baseProxy;
	}
	private Viewable getBaseViewable(AttributeDef baseProxy)
	{
		if(baseProxy==null){
			return null;
		}
		Type proxyType=baseProxy.getDomain();
		if(!(proxyType instanceof ObjectType)){
			return null;
		}
		return ((ObjectType)proxyType).getRef();
	}
	
	private String getIliDoc()
	throws antlr.TokenStreamException
	{ 
		String ilidoc=null;
		ArrayList docs=new ArrayList();
		antlr.CommonHiddenStreamToken cmtToken=((antlr.CommonHiddenStreamToken)LT(1)).getHiddenBefore();
		while(cmtToken!=null){
			if(cmtToken.getType()==ILI_DOC){
				docs.add(0,cmtToken);
			}
			cmtToken=cmtToken.getHiddenBefore();
		}
		Iterator doci=docs.iterator();
		StringBuilder buf=new StringBuilder();
		String sep="";
		while(doci.hasNext()){
			cmtToken=(antlr.CommonHiddenStreamToken)doci.next();
			ilidoc=cmtToken.getText().trim();
			if(ilidoc.startsWith("/**")){
				ilidoc=ilidoc.substring(3);
			}
			if(ilidoc.endsWith("*/")){
				ilidoc=ilidoc.substring(0,ilidoc.length()-2);
			}
			java.io.LineNumberReader lines=new java.io.LineNumberReader(new java.io.StringReader(ilidoc.trim()));
			String line;
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
		}
			ilidoc=buf.toString();
			if(ilidoc.length()==0){
				ilidoc=null;
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


protected Ili24Parser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public Ili24Parser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected Ili24Parser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public Ili24Parser(TokenStream lexer) {
  this(lexer,1);
}

public Ili24Parser(ParserSharedInputState state) {
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
			interlis2Def();
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
	
	protected final void interlis2Def() throws RecognitionException, TokenStreamException {
		
		Token  ili = null;
		
				PrecisionDecimal version;
			
		
		try {      // for error handling
			ili = LT(1);
			match(LITERAL_INTERLIS);
			version=decimal();
			if ( inputState.guessing==0 ) {
				
					      if (version.doubleValue()!=2.4) {
					        reportError(formatMessage("err_wrongInterlisVersion",version.toString()),
					                    ili.getLine());
					        panic();
					      }
				// set lexer mode to Ili 2
				lexer.isIli1=false;
					
			}
			match(SEMI);
			{
			_loop519:
			do {
				if (((LA(1) >= LITERAL_CONTRACTED && LA(1) <= LITERAL_TYPE))) {
					modelDef();
				}
				else {
					break _loop519;
				}
				
			} while (true);
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
	
	protected final PrecisionDecimal  decimal() throws RecognitionException, TokenStreamException {
		PrecisionDecimal dec;
		
		Token  d = null;
		Token  p = null;
		Token  n = null;
		
				dec = null;
			
		
		try {      // for error handling
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
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_1);
			} else {
			  throw ex;
			}
		}
		return dec;
	}
	
	protected final void modelDef() throws RecognitionException, TokenStreamException {
		
		Token  n1 = null;
		Token  n = null;
		Token  issuerToken = null;
		Token  ver = null;
		Token  verexpl = null;
		Token  translationOf = null;
		Token  translationOfVersion = null;
		Token  ianaNameToken = null;
		Token  xmlnsToken = null;
		Token  imp1 = null;
		Token  imp2 = null;
		Token  endDot = null;
		
			  Model md = null;
			  String[] importedNames = null;
			  Table tabDef;
			  int mods = 0;
			  boolean unqualified=false;
			  boolean contracted=false;
			  String ilidoc=null;
			  Settings metaValues=null;
			
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc(); metaValues=getMetaValues();
						
			}
			{
			if ((LA(1)==LITERAL_CONTRACTED)) {
				match(LITERAL_CONTRACTED);
				if ( inputState.guessing==0 ) {
					contracted=true;
				}
			}
			else if (((LA(1) >= LITERAL_REFSYSTEM && LA(1) <= LITERAL_TYPE))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			switch ( LA(1)) {
			case LITERAL_REFSYSTEM:
			{
				match(LITERAL_REFSYSTEM);
				match(LITERAL_MODEL);
				if ( inputState.guessing==0 ) {
					md = new RefSystemModel();
				}
				break;
			}
			case LITERAL_SYMBOLOGY:
			{
				match(LITERAL_SYMBOLOGY);
				match(LITERAL_MODEL);
				if ( inputState.guessing==0 ) {
					md = new SymbologyModel();
				}
				break;
			}
			case LITERAL_TYPE:
			{
				match(LITERAL_TYPE);
				match(LITERAL_MODEL);
				if ( inputState.guessing==0 ) {
					md = new TypeModel();
				}
				break;
			}
			case LITERAL_MODEL:
			{
				match(LITERAL_MODEL);
				if ( inputState.guessing==0 ) {
					md = new DataModel();
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			n1 = LT(1);
			match(NAME);
			if ( inputState.guessing==0 ) {
				
								try {
								 md.setContracted(contracted);
								 md.setName(n1.getText());
								 md.setSourceLine(n1.getLine());
				md.setFileName(getFilename());
								  md.setDocumentation(ilidoc);
								  md.setMetaValues(metaValues);
								  md.setIliVersion(Model.ILI2_4);
								 td.add(md);
								} catch (Exception ex) {
								 reportError(ex, n1.getLine());
								 panic();
								}
							
			}
			{
			if ((LA(1)==LPAREN)) {
				match(LPAREN);
				n = LT(1);
				match(NAME);
				match(RPAREN);
				if ( inputState.guessing==0 ) {
					
							        try {
							          md.setLanguage(n.getText());
							        } catch (Exception ex) {
							          reportError(ex, n.getLine());
							        }
							
				}
			}
			else if ((LA(1)==LITERAL_NOINCREMENTALTRANSFER||LA(1)==LITERAL_AT)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((LA(1)==LITERAL_NOINCREMENTALTRANSFER)) {
				match(LITERAL_NOINCREMENTALTRANSFER);
				if ( inputState.guessing==0 ) {
					
								md.setNoIncrementalTransfer(true);
							
				}
			}
			else if ((LA(1)==LITERAL_AT)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(LITERAL_AT);
			issuerToken = LT(1);
			match(STRING);
			if ( inputState.guessing==0 ) {
				
							String issuer=issuerToken.getText();
							md.setIssuer(issuer);
							// http://www.ietf.org/rfc/rfc3986.txt
							// <scheme>:<scheme-specific-part>
							// scheme = alpha *( alpha | digit | "+" | "-" | "." )
							//  ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
							java.util.regex.Pattern pattern=java.util.regex.Pattern.compile("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?");
							java.util.regex.Matcher matcher=pattern.matcher(issuer);
							if(matcher.matches()){
								String scheme=matcher.group(1);
								if(scheme==null){
									reportError("issuer doesn't match URI syntax <"+issuer+">",issuerToken.getLine());
								}
							}else{
								reportError("issuer doesn't match URI syntax <"+issuer+">",issuerToken.getLine());
							}
						
			}
			match(LITERAL_VERSION);
			ver = LT(1);
			match(STRING);
			{
			if ((LA(1)==EXPLANATION)) {
				verexpl = LT(1);
				match(EXPLANATION);
			}
			else if ((LA(1)==LITERAL_TRANSLATION||LA(1)==EQUALS)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
							md.setModelVersion(ver.getText());
							if(verexpl!=null){
								md.setModelVersionExpl(verexpl.getText());
							}
						
			}
			{
			if ((LA(1)==LITERAL_TRANSLATION)) {
				match(LITERAL_TRANSLATION);
				match(LITERAL_OF);
				translationOf = LT(1);
				match(NAME);
				match(LBRACE);
				translationOfVersion = LT(1);
				match(STRING);
				match(RBRACE);
				if ( inputState.guessing==0 ) {
					md.setTranslationOf(translationOf.getText(),translationOfVersion.getText());
				}
			}
			else if ((LA(1)==EQUALS)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(EQUALS);
			{
			if ((LA(1)==LITERAL_CHARSET)) {
				match(LITERAL_CHARSET);
				ianaNameToken = LT(1);
				match(STRING);
				match(SEMI);
				if ( inputState.guessing==0 ) {
					
								md.setCharSetIANAName(ianaNameToken.getText());
							
				}
			}
			else if ((_tokenSet_2.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((LA(1)==LITERAL_XMLNS)) {
				match(LITERAL_XMLNS);
				xmlnsToken = LT(1);
				match(STRING);
				match(SEMI);
				if ( inputState.guessing==0 ) {
					
								md.setXmlns(xmlnsToken.getText());
							
				}
			}
			else if ((_tokenSet_3.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			_loop536:
			do {
				if ((LA(1)==LITERAL_IMPORTS)) {
					match(LITERAL_IMPORTS);
					{
					if ((LA(1)==LITERAL_UNQUALIFIED)) {
						match(LITERAL_UNQUALIFIED);
						if ( inputState.guessing==0 ) {
							unqualified=true;
						}
					}
					else if ((LA(1)==LITERAL_INTERLIS||LA(1)==NAME)) {
						if ( inputState.guessing==0 ) {
							unqualified=false;
						}
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
					{
					if ((LA(1)==NAME)) {
						imp1 = LT(1);
						match(NAME);
						if ( inputState.guessing==0 ) {
							
											Model imported;
											imported=resolveOrFixModelName(td, imp1.getText(), imp1.getLine());
											if(imported!=null){
										        md.addImport(imported,unqualified);
											}
										
						}
					}
					else if ((LA(1)==LITERAL_INTERLIS)) {
						match(LITERAL_INTERLIS);
						if ( inputState.guessing==0 ) {
							
										        md.addImport(modelInterlis,unqualified);
											
						}
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
					{
					_loop535:
					do {
						if ((LA(1)==COMMA)) {
							match(COMMA);
							{
							if ((LA(1)==LITERAL_UNQUALIFIED)) {
								match(LITERAL_UNQUALIFIED);
								if ( inputState.guessing==0 ) {
									unqualified=true;
								}
							}
							else if ((LA(1)==LITERAL_INTERLIS||LA(1)==NAME)) {
								if ( inputState.guessing==0 ) {
									unqualified=false;
								}
							}
							else {
								throw new NoViableAltException(LT(1), getFilename());
							}
							
							}
							{
							if ((LA(1)==NAME)) {
								imp2 = LT(1);
								match(NAME);
								if ( inputState.guessing==0 ) {
									
														Model imported;
														imported=resolveOrFixModelName(td, imp2.getText(), imp2.getLine());
														if(imported!=null){
													        	md.addImport(imported,unqualified);
														}
														
								}
							}
							else if ((LA(1)==LITERAL_INTERLIS)) {
								match(LITERAL_INTERLIS);
								if ( inputState.guessing==0 ) {
									
														md.addImport(modelInterlis,unqualified);
														
								}
							}
							else {
								throw new NoViableAltException(LT(1), getFilename());
							}
							
							}
						}
						else {
							break _loop535;
						}
						
					} while (true);
					}
					match(SEMI);
				}
				else {
					break _loop536;
				}
				
			} while (true);
			}
			{
			_loop538:
			do {
				switch ( LA(1)) {
				case LITERAL_REFSYSTEM:
				case LITERAL_SIGN:
				{
					metaDataBasketDef(md);
					break;
				}
				case LITERAL_UNIT:
				{
					unitDefs(md);
					break;
				}
				case LITERAL_FUNCTION:
				{
					functionDef(md);
					break;
				}
				case LITERAL_LINE:
				{
					lineFormTypeDef(md);
					break;
				}
				case LITERAL_DOMAIN:
				{
					domainDefs(md);
					break;
				}
				case LITERAL_CONTEXT:
				{
					contextDefs(md);
					break;
				}
				case LITERAL_PARAMETER:
				{
					runTimeParameterDef(md);
					break;
				}
				case LITERAL_CLASS:
				case LITERAL_STRUCTURE:
				{
					classDef(md);
					break;
				}
				case LITERAL_VIEW:
				case LITERAL_TOPIC:
				{
					topicDef(md);
					break;
				}
				default:
				{
					break _loop538;
				}
				}
			} while (true);
			}
			end(md);
			endDot = LT(1);
			match(DOT);
			if ( inputState.guessing==0 ) {
				
					       try {
							 List<Ili2cSemanticException> errs=new java.util.ArrayList<Ili2cSemanticException>();	       		
					         md.checkIntegrity (errs);
					         reportError(errs);
					       } catch (Ili2cSemanticException ex) {
					         reportError (ex);
					       } catch (Exception ex) {
					         reportError (ex, endDot.getLine());
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
	}
	
	protected final void metaDataBasketDef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		Token  ext = null;
		Token  classNameTok = null;
		Token  objNameTok1 = null;
		Token  objNameTok2 = null;
		
		int mods;
		boolean sign=false;
		MetaDataUseDef def=null;
		MetaDataUseDef base=null;
		Topic topic = null;
		Table aclass=null;
			  String ilidoc=null;
			  Settings metaValues=null;
		
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			{
			if ((LA(1)==LITERAL_SIGN)) {
				match(LITERAL_SIGN);
				if ( inputState.guessing==0 ) {
					sign=true;
				}
			}
			else if ((LA(1)==LITERAL_REFSYSTEM)) {
				match(LITERAL_REFSYSTEM);
				if ( inputState.guessing==0 ) {
					sign=false;
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(LITERAL_BASKET);
			n = LT(1);
			match(NAME);
			mods=properties(ch.interlis.ili2c.metamodel.Properties.eFINAL);
			if ( inputState.guessing==0 ) {
				
							def=new MetaDataUseDef();
							def.setDocumentation(ilidoc);
							def.setMetaValues(metaValues);
							def.setSignData(sign);
							def.setName(n.getText());
							try{
							  def.setFinal((mods & ch.interlis.ili2c.metamodel.Properties.eFINAL)!=0);
							} catch (Exception ex) {
						            reportError(ex, n.getLine());
						        }
						
			}
			{
			if ((LA(1)==LITERAL_EXTENDS)) {
				ext = LT(1);
				match(LITERAL_EXTENDS);
				base=metaDataBasketRef(scope);
				if ( inputState.guessing==0 ) {
					
								try{
							      def.setExtending(base);
								} catch (Exception ex) {
							            reportError(ex, ext.getLine());
							        }
					
				}
			}
			else if ((LA(1)==TILDE)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(TILDE);
			topic=topicRef(scope);
			if ( inputState.guessing==0 ) {
				
							def.setTopic(topic);
							scope.add(def);
							// ili2.3
							// - add proxies from contents of ili-file
							// (no data container)
						
			}
			{
			_loop774:
			do {
				if ((LA(1)==LITERAL_OBJECTS)) {
					match(LITERAL_OBJECTS);
					match(LITERAL_OF);
					classNameTok = LT(1);
					match(NAME);
					match(COLON);
					if ( inputState.guessing==0 ) {
						ilidoc=getIliDoc(); metaValues=getMetaValues();
									
					}
					objNameTok1 = LT(1);
					match(NAME);
					if ( inputState.guessing==0 ) {
						
											// find class in topic
											String className=classNameTok.getText();
											aclass = (Table) topic.getRealElement(Table.class, className);
											if (aclass == null) {
												reportError (formatMessage ("err_noSuchTable", className,
													topic.toString()), classNameTok.getLine());
											}else{
												// check if aclass is an extension of INTERLIS.METAOBJECT
												if(!aclass.isExtending(td.INTERLIS.METAOBJECT)){
												reportError (formatMessage ("err_class_superNonMetaObject", className
													), classNameTok.getLine());
												}
											}
											// add object to basket
											if(aclass!=null){
												String objName=objNameTok1.getText();
												MetaObject mo=new MetaObject(objName,aclass);
												mo.setDocumentation(ilidoc);
												mo.setMetaValues(metaValues);
												def.add(mo);
											}
										
					}
					{
					_loop773:
					do {
						if ((LA(1)==COMMA)) {
							match(COMMA);
							if ( inputState.guessing==0 ) {
								ilidoc=getIliDoc(); metaValues=getMetaValues();
												
							}
							objNameTok2 = LT(1);
							match(NAME);
							if ( inputState.guessing==0 ) {
								
													// add object to basket
													if(aclass!=null){
														String objName=objNameTok2.getText();
														MetaObject mo=new MetaObject(objName,aclass);
														mo.setDocumentation(ilidoc);
														mo.setMetaValues(metaValues);
														def.add(mo);
													}
												
							}
						}
						else {
							break _loop773;
						}
						
					} while (true);
					}
				}
				else {
					break _loop774;
				}
				
			} while (true);
			}
			match(SEMI);
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
	
	protected final void unitDefs(
		Container scope
	) throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(LITERAL_UNIT);
			{
			_loop753:
			do {
				if ((LA(1)==NAME)) {
					unitDef(scope);
				}
				else {
					break _loop753;
				}
				
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
	
	protected final void functionDef(
		Container container
	) throws RecognitionException, TokenStreamException {
		
		Token  fn = null;
		Token  lpar = null;
		Token  sem = null;
		Token  col = null;
		Token  explan = null;
		
		Type t = null;
		FormalArgument arg=null;
		Function f = null;
		ArrayList     args = null;
			  String ilidoc=null;
			  Settings metaValues=null;
		
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			match(LITERAL_FUNCTION);
			fn = LT(1);
			match(NAME);
			if ( inputState.guessing==0 ) {
				
				f = new Function ();
				args = new ArrayList ();
				try {
				f.setName(fn.getText());
					f.setDocumentation(ilidoc);
					f.setMetaValues(metaValues);
				f.setSourceLine(fn.getLine());
				} catch (Exception ex) {
				reportError(ex, fn.getLine());
					return;
				}
				
			}
			lpar = LT(1);
			match(LPAREN);
			{
			if ((LA(1)==NAME)) {
				arg=formalArgument(container, lpar.getLine(),args);
				if ( inputState.guessing==0 ) {
					
					try {
					args.add (arg);
					} catch (Exception ex) {
					reportError (ex, lpar.getLine ());
						return;
					}
					
				}
				{
				_loop881:
				do {
					if ((LA(1)==SEMI)) {
						sem = LT(1);
						match(SEMI);
						arg=formalArgument(container, sem.getLine(),args);
						if ( inputState.guessing==0 ) {
							
							try {
							args.add (arg);
							} catch (Exception ex) {
							reportError (ex, sem.getLine ());
								  return;
							}
							
						}
					}
					else {
						break _loop881;
					}
					
				} while (true);
				}
			}
			else if ((LA(1)==RPAREN)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(RPAREN);
			col = LT(1);
			match(COLON);
			t=argumentType(container, col.getLine(),null);
			if ( inputState.guessing==0 ) {
				
				try {
				f.setArguments ((FormalArgument[]) args.toArray (new FormalArgument[args.size()]));
				} catch (Exception ex) {
				reportError (ex, col.getLine());
					return;
				}
				
				try {
				if (t != null)
				f.setDomain(t);
				} catch (Exception ex) {
				reportError(ex, col.getLine());
					return;
				}
				
			}
			{
			if ((LA(1)==EXPLANATION)) {
				explan = LT(1);
				match(EXPLANATION);
				if ( inputState.guessing==0 ) {
					
					try {
					f.setExplanation(explan.getText());
					} catch (Exception ex) {
					reportError(ex, explan.getLine());
						  return;
					}
					
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
				
				try {
				container.add(f);
				} catch (Exception ex) {
				reportError(ex, fn.getLine());
					return;
				}
				
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
	
	protected final void lineFormTypeDef(
		Model model
	) throws RecognitionException, TokenStreamException {
		
		Token  linform = null;
		Token  nam = null;
		Token  lineStructure = null;
		
		String explString = null;
			  String ilidoc=null;
			  Settings metaValues=null;
		
		
		try {      // for error handling
			linform = LT(1);
			match(LITERAL_LINE);
			match(LITERAL_FORM);
			{
			_loop750:
			do {
				if ((LA(1)==NAME)) {
					if ( inputState.guessing==0 ) {
						ilidoc=getIliDoc();metaValues=getMetaValues();
					}
					nam = LT(1);
					match(NAME);
					match(COLON);
					lineStructure = LT(1);
					match(NAME);
					match(SEMI);
					if ( inputState.guessing==0 ) {
						
						LineForm lf = new LineForm ();
						try {
						lf.setName (nam.getText ());
							  lf.setDocumentation(ilidoc);
							  lf.setMetaValues(metaValues);
						lf.setSourceLine(nam.getLine());
							  Table seg=(Table)model.getImportedElement(Table.class,lineStructure.getText());
							  if(seg==null){
							          reportError (formatMessage ("err_noSuchTable", lineStructure.getText(),
						model.toString()), lineStructure.getLine());
							  }
						lf.setSegmentStructure(seg);
						model.add (lf);
						} catch (Exception ex) {
						reportError (ex, nam.getLine());
						}
						
					}
				}
				else {
					break _loop750;
				}
				
			} while (true);
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
	
	protected final void domainDefs(
		Container container
	) throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(LITERAL_DOMAIN);
			{
			_loop635:
			do {
				if ((LA(1)==NAME)) {
					domainDef(container);
				}
				else {
					break _loop635;
				}
				
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
	
	protected final void contextDefs(
		Container container
	) throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
			  ContextDefs defs=null;
			  int nameIdx=1;
			
		
		try {      // for error handling
			match(LITERAL_CONTEXT);
			n = LT(1);
			match(NAME);
			match(EQUALS);
			if ( inputState.guessing==0 ) {
				defs=new ContextDefs(n.getText());
						
			}
			{
			_loop707:
			do {
				if ((_tokenSet_7.member(LA(1)))) {
					contextDef(container,defs,nameIdx++);
				}
				else {
					break _loop707;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				
						container.add(defs);
						
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
	
	protected final void runTimeParameterDef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
				Type domain;
				GraphicParameterDef def=null;
			  String ilidoc=null;
			  Settings metaValues=null;
			
		
		try {      // for error handling
			match(LITERAL_PARAMETER);
			{
			_loop784:
			do {
				if ((LA(1)==NAME)) {
					if ( inputState.guessing==0 ) {
						ilidoc=getIliDoc();metaValues=getMetaValues();
					}
					n = LT(1);
					match(NAME);
					match(COLON);
					if ( inputState.guessing==0 ) {
						
									def=new GraphicParameterDef();
									def.setSourceLine(n.getLine());
									def.setName(n.getText());
									def.setDocumentation(ilidoc);
									def.setMetaValues(metaValues);
						
					}
					domain=attrTypeDef(scope,true,null,n.getLine(),null);
					if ( inputState.guessing==0 ) {
						
						def.setDomain(domain);
						scope.add(def);
						
					}
					match(SEMI);
				}
				else {
					break _loop784;
				}
				
			} while (true);
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
	
	protected final void classDef(
		Container container
	) throws RecognitionException, TokenStreamException {
		
		Token  n1 = null;
		Token  extToken = null;
		Token  oid = null;
		
			  Table table = null;
			  Table extending = null;
			  Table overwriting = null;
			  boolean identifiable = true;
			  Constraint constr = null;
			  int mods;
			  Domain classOid=null;
			  String ilidoc=null;
			  Settings metaValues=null;
			
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			{
			if ((LA(1)==LITERAL_CLASS)) {
				match(LITERAL_CLASS);
			}
			else if ((LA(1)==LITERAL_STRUCTURE)) {
				{
				match(LITERAL_STRUCTURE);
				if ( inputState.guessing==0 ) {
					identifiable = false;
				}
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			n1 = LT(1);
			match(NAME);
			if ( inputState.guessing==0 ) {
				
					      try {
					        table = new Table();
					        table.setSourceLine(n1.getLine());
					        table.setName (n1.getText());
						table.setDocumentation(ilidoc);
						table.setMetaValues(metaValues);
					        table.setIdentifiable (identifiable);
					        table.setAbstract (true);
					      } catch (Exception ex) {
					        reportError(ex, n1.getLine());
					      }
				
					      overwriting = (Table)container.getElement(Table.class, n1.getText());
					
			}
			mods=properties(ch.interlis.ili2c.metamodel.Properties.eABSTRACT | ch.interlis.ili2c.metamodel.Properties.eEXTENDED | ch.interlis.ili2c.metamodel.Properties.eFINAL);
			if ( inputState.guessing==0 ) {
				
					      try {
					        table.setAbstract((mods & ch.interlis.ili2c.metamodel.Properties.eABSTRACT) != 0);
					        table.setFinal((mods & ch.interlis.ili2c.metamodel.Properties.eFINAL) != 0);
					        if ((mods & ch.interlis.ili2c.metamodel.Properties.eEXTENDED) != 0)
					        {
					          if (overwriting == null)
					          {
					            reportError (formatMessage (
					              "err_noTableOrStructureToExtend",
					              n1.getText(),
					              container.toString()),
					            n1.getLine());
					          }
					          else
					          {
					            table.setExtending (overwriting);
					            table.setExtended(true);
					          }
					        }
				
					        /* Correct non-ABSTRACT table in Model */
					        if ((container instanceof Model)
					            && !table.isAbstract() && table.isIdentifiable())
					        {
					           reportError (formatMessage (
					             "err_table_concreteOutsideTopic",
					              table.toString()),
					              n1.getLine());
				
					           table.setFinal(false);
					           table.setAbstract(true);
					        }
					      } catch (Exception ex) {
					        reportError(ex, n1.getLine());
					      }
					
			}
			{
			if ((LA(1)==LITERAL_EXTENDS)) {
				extToken = LT(1);
				match(LITERAL_EXTENDS);
				extending=classRef(container);
				if ( inputState.guessing==0 ) {
					
							        if ((mods & ch.interlis.ili2c.metamodel.Properties.eEXTENDED) != 0)
							        {
							          reportError(rsrc.getString("err_extendedWithExtends"),
							                      extToken.getLine());
							        }
							        else
							        {
							          try {
							            table.setExtending(extending);
							          } catch (Exception ex) {
							            reportError(ex, extToken.getLine());
							          }
							        }
							
				}
			}
			else if ((LA(1)==EQUALS)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(EQUALS);
			if ( inputState.guessing==0 ) {
				
						      try {
						        container.add (table);
						      } catch (Exception ex) {
						        reportError (ex, n1.getLine ());
						        panic ();
						      }
						
			}
			{
			if ((LA(1)==LITERAL_OID||LA(1)==LITERAL_NO)) {
				{
				if ((LA(1)==LITERAL_OID)) {
					oid = LT(1);
					match(LITERAL_OID);
					match(LITERAL_AS);
					classOid=domainRef(container);
					if ( inputState.guessing==0 ) {
						
											if(!(classOid.getType() instanceof OIDType)){
												reportError (formatMessage ("err_topic_domainnotanoid",classOid.toString()),oid.getLine());
											}
									
					}
				}
				else if ((LA(1)==LITERAL_NO)) {
					match(LITERAL_NO);
					match(LITERAL_OID);
					if ( inputState.guessing==0 ) {
						classOid=NoOid.createNoOid();
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				if ( inputState.guessing==0 ) {
					
									table.setOid(classOid);
								
				}
				match(SEMI);
			}
			else if ((_tokenSet_8.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((LA(1)==LITERAL_ATTRIBUTE)) {
				match(LITERAL_ATTRIBUTE);
			}
			else if ((_tokenSet_9.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			_loop563:
			do {
				if ((LA(1)==NAME||LA(1)==LITERAL_CONTINUOUS||LA(1)==LITERAL_SUBDIVISION)) {
					attributeDef(table);
				}
				else {
					break _loop563;
				}
				
			} while (true);
			}
			{
			_loop565:
			do {
				if ((_tokenSet_10.member(LA(1)))) {
					constr=constraintDef(table,table.getContainer());
					if ( inputState.guessing==0 ) {
						
								        if (constr != null)
								          table.add (constr);
								
					}
				}
				else {
					break _loop565;
				}
				
			} while (true);
			}
			{
			if ((LA(1)==LITERAL_PARAMETER)) {
				match(LITERAL_PARAMETER);
				{
				_loop568:
				do {
					if ((LA(1)==NAME)) {
						parameterDef(table);
					}
					else {
						break _loop568;
					}
					
				} while (true);
				}
			}
			else if ((LA(1)==LITERAL_END)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			end(table);
			match(SEMI);
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
	
	protected final void topicDef(
		Container container
	) throws RecognitionException, TokenStreamException {
		
		Token  n1 = null;
		Token  extToken = null;
		Token  oid2 = null;
		Token  oid = null;
		Token  on = null;
		Token  com = null;
		
			  Topic topic = null;
			  Topic extending = null;
			  Topic depTopic = null;
			  int mods;
			  boolean viewTopic=false;
			  Domain topicOid=null;
			  Domain topicOid2=null;
			  Domain genericDomain=null;
			  String ilidoc=null;
			  Settings metaValues=null;
			
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			{
			if ((LA(1)==LITERAL_VIEW)) {
				match(LITERAL_VIEW);
				if ( inputState.guessing==0 ) {
					viewTopic=true;
				}
			}
			else if ((LA(1)==LITERAL_TOPIC)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(LITERAL_TOPIC);
			n1 = LT(1);
			match(NAME);
			if ( inputState.guessing==0 ) {
				
						      try {
						        topic = new Topic();
						        topic.setSourceLine(n1.getLine());
							topic.setViewTopic(viewTopic);
						        topic.setName(n1.getText());
							topic.setDocumentation(ilidoc);
							topic.setMetaValues(metaValues);
						      } catch (Exception ex) {
						        reportError(ex, n1.getLine());
						      }
						
			}
			mods=properties(ch.interlis.ili2c.metamodel.Properties.eABSTRACT | ch.interlis.ili2c.metamodel.Properties.eFINAL);
			if ( inputState.guessing==0 ) {
				
						      try {
						        container.add(topic);
						        topic.setAbstract((mods & ch.interlis.ili2c.metamodel.Properties.eABSTRACT) != 0);
						        topic.setFinal((mods & ch.interlis.ili2c.metamodel.Properties.eFINAL) != 0);
						      } catch (Exception ex) {
						        reportError(ex, n1.getLine());
						      }
						
			}
			{
			if ((LA(1)==LITERAL_EXTENDS)) {
				extToken = LT(1);
				match(LITERAL_EXTENDS);
				extending=topicRef(container);
				if ( inputState.guessing==0 ) {
					
							        try {
							          topic.setExtending(extending);
							        } catch (Exception ex) {
							          reportError(ex, extToken.getLine());
							        }
							
				}
			}
			else if ((LA(1)==EQUALS)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(EQUALS);
			{
			if ((LA(1)==LITERAL_BASKET)) {
				match(LITERAL_BASKET);
				oid2 = LT(1);
				match(LITERAL_OID);
				match(LITERAL_AS);
				topicOid2=domainRef(container);
				match(SEMI);
				if ( inputState.guessing==0 ) {
					
									if(!(topicOid2.getType() instanceof OIDType)){
										reportError (formatMessage ("err_topic_domainnotanoid",topicOid2.toString()),oid2.getLine());
									}
									topic.setBasketOid(topicOid2);
								
				}
			}
			else if ((_tokenSet_11.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((LA(1)==LITERAL_OID)) {
				oid = LT(1);
				match(LITERAL_OID);
				match(LITERAL_AS);
				topicOid=domainRef(container);
				match(SEMI);
				if ( inputState.guessing==0 ) {
					
									if(!(topicOid.getType() instanceof OIDType)){
										reportError (formatMessage ("err_topic_domainnotanoid",topicOid.toString()),oid.getLine());
									}
									topic.setOid(topicOid);
								
				}
			}
			else if ((_tokenSet_12.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			_loop547:
			do {
				if ((LA(1)==LITERAL_DEPENDS)) {
					match(LITERAL_DEPENDS);
					on = LT(1);
					match(LITERAL_ON);
					depTopic=topicRef(/*scope*/ topic);
					if ( inputState.guessing==0 ) {
						
							        try {
							          topic.makeDependentOn(depTopic);
							        } catch (Exception ex) {
							          reportError(ex, on.getLine());
							        }
							
					}
					{
					_loop546:
					do {
						if ((LA(1)==COMMA)) {
							com = LT(1);
							match(COMMA);
							depTopic=topicRef(/*scope*/ topic);
							if ( inputState.guessing==0 ) {
								
										        try {
										          topic.makeDependentOn(depTopic);
										        } catch (Exception ex) {
										          reportError(ex, com.getLine());
										        }
										
							}
						}
						else {
							break _loop546;
						}
						
					} while (true);
					}
					match(SEMI);
				}
				else {
					break _loop547;
				}
				
			} while (true);
			}
			{
			if ((LA(1)==LITERAL_DEFERRED)) {
				match(LITERAL_DEFERRED);
				match(LITERAL_GENERICS);
				genericDomain=domainRef(container);
				if ( inputState.guessing==0 ) {
					
										topic.addDeferredGeneric(genericDomain);
									
				}
				{
				_loop550:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						genericDomain=domainRef(container);
						if ( inputState.guessing==0 ) {
							
												topic.addDeferredGeneric(genericDomain);
											
						}
					}
					else {
						break _loop550;
					}
					
				} while (true);
				}
				match(SEMI);
			}
			else if ((_tokenSet_13.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			definitions(topic);
			end(topic);
			match(SEMI);
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
				recover(ex,_tokenSet_14);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final int  properties(
		int acceptable
	) throws RecognitionException, TokenStreamException {
		int mods;
		
		
		mods = 0;
		int mod;
		
		
		try {      // for error handling
			{
			if ((LA(1)==LPAREN)) {
				match(LPAREN);
				mod=property(acceptable, mods);
				if ( inputState.guessing==0 ) {
					mods = mod;
				}
				{
				_loop960:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						mod=property(acceptable, mods);
						if ( inputState.guessing==0 ) {
							mods |= mod;
						}
					}
					else {
						break _loop960;
					}
					
				} while (true);
				}
				match(RPAREN);
			}
			else if ((_tokenSet_15.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_15);
			} else {
			  throw ex;
			}
		}
		return mods;
	}
	
	protected final Topic  topicRef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		Topic topic;
		
		
			  List      nams = new LinkedList();
			  topic = null;
			  int lin = 0;
			
		
		try {      // for error handling
			lin=names2(nams);
			if ( inputState.guessing==0 ) {
				
				Model model;
				String   topicName;
				
				switch (nams.size()) {
				case 1:
				model = (Model) scope.getContainerOrSame(Model.class);
				topicName = (String) nams.get(0);
				break;
				
				case 2:
				String modelName = (String) nams.get(0);
				model = resolveOrFixModelName(scope, modelName, lin);
				topicName = (String) nams.get(1);
				break;
				
				default:
				reportError(rsrc.getString("err_weirdTopicRef"), lin);
				model = resolveModelName(scope, (String) nams.get(0));
				if (model == null)
				model = (Model) scope.getContainerOrSame(Model.class);
				topicName = (String) nams.get(nams.size() - 1);
				break;
				}
				
				topic = (Topic) model.getRealElement(Topic.class, topicName);
				if (topic == null && nams.size()==1) {
					topic = (Topic) model.getImportedElement(Topic.class, topicName);
				}
				if (topic == null) {
				reportError(
				formatMessage("err_noSuchTopic", topicName, model.toString()),
				lin);
				try {
				topic = new Topic();
				topic.setName(topicName);
				model.add(topic);
				} catch (Exception ex) {
				panic();
				}
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_16);
			} else {
			  throw ex;
			}
		}
		return topic;
	}
	
	protected final Domain  domainRef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		Domain d;
		
		
		List      nams = new LinkedList();
		d = null;
		int lin = 0;
		
		
		try {      // for error handling
			lin=names2(nams);
			if ( inputState.guessing==0 ) {
				
				d=resolveDomainRef(scope,(String[]) nams.toArray(new String[nams.size()]),lin);
				
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
		return d;
	}
	
	protected final void definitions(
		Container scope
	) throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			_loop553:
			do {
				switch ( LA(1)) {
				case LITERAL_REFSYSTEM:
				case LITERAL_SIGN:
				{
					metaDataBasketDef(scope);
					break;
				}
				case LITERAL_UNIT:
				{
					unitDefs(scope);
					break;
				}
				case LITERAL_FUNCTION:
				{
					functionDef(scope);
					break;
				}
				case LITERAL_DOMAIN:
				{
					domainDefs(scope);
					break;
				}
				case LITERAL_CONTEXT:
				{
					contextDefs(scope);
					break;
				}
				case LITERAL_CLASS:
				case LITERAL_STRUCTURE:
				{
					classDef(scope);
					break;
				}
				case LITERAL_ASSOCIATION:
				{
					associationDef(scope);
					break;
				}
				case LITERAL_CONSTRAINTS:
				{
					constraintsDef(scope);
					break;
				}
				case LITERAL_VIEW:
				{
					viewDef(scope);
					break;
				}
				case LITERAL_GRAPHIC:
				{
					graphicDef(scope);
					break;
				}
				default:
				{
					break _loop553;
				}
				}
			} while (true);
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
	
	protected final void associationDef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		
		Token  a = null;
		Token  n = null;
		Token  extToken = null;
		Token  derivedToken = null;
		Token  oid = null;
		Token  nam = null;
		
			int mods;
			AssociationDef def=new AssociationDef();
			AssociationDef extending = null;
			ViewableAlias derivedFrom;
			Constraint constr;
			Cardinality card=null;
			  String ilidoc=null;
			  Settings metaValues=null;
			  Domain assocOid=null;
			
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			a = LT(1);
			match(LITERAL_ASSOCIATION);
			{
			if ((LA(1)==NAME)) {
				n = LT(1);
				match(NAME);
				if ( inputState.guessing==0 ) {
					
									try{
										def.setName(n.getText());
									} catch (Exception ex) {
										reportError(ex, n.getLine());
									}
								
				}
			}
			else if ((_tokenSet_19.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			mods=properties(ch.interlis.ili2c.metamodel.Properties.eABSTRACT
			|ch.interlis.ili2c.metamodel.Properties.eEXTENDED
			|ch.interlis.ili2c.metamodel.Properties.eFINAL
			|ch.interlis.ili2c.metamodel.Properties.eOID
			);
			if ( inputState.guessing==0 ) {
				
								try {
									def.setSourceLine(a.getLine());
									def.setDocumentation(ilidoc);
									def.setMetaValues(metaValues);
									def.setAbstract((mods & ch.interlis.ili2c.metamodel.Properties.eABSTRACT) != 0);
									def.setFinal((mods & ch.interlis.ili2c.metamodel.Properties.eFINAL) != 0);
					    				def.setExtended((mods & ch.interlis.ili2c.metamodel.Properties.eEXTENDED) != 0);
					    				def.setIdentifiable((mods & ch.interlis.ili2c.metamodel.Properties.eOID) != 0);
								} catch (Exception ex) {
									reportError(ex, a.getLine());
								}
							
			}
			{
			if ((LA(1)==LITERAL_EXTENDS)) {
				extToken = LT(1);
				match(LITERAL_EXTENDS);
				extending=associationRef(scope);
				if ( inputState.guessing==0 ) {
					
							        if ((mods & ch.interlis.ili2c.metamodel.Properties.eEXTENDED) != 0)
							        {
							          reportError(rsrc.getString("err_extendedWithExtends"),
							                      extToken.getLine());
							        }
							        else
							        {
							          try {
							            def.setExtending(extending);
							          } catch (Exception ex) {
							            reportError(ex, extToken.getLine());
							          }
							        }
							
				}
			}
			else if ((LA(1)==EQUALS||LA(1)==LITERAL_DERIVED)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((LA(1)==LITERAL_DERIVED)) {
				derivedToken = LT(1);
				match(LITERAL_DERIVED);
				match(LITERAL_FROM);
				derivedFrom=renamedViewableRef(scope);
				if ( inputState.guessing==0 ) {
					
							          try {
							            def.setDerivedFrom(derivedFrom.getAliasing());
									LocalAttribute attrib=new LocalAttribute();
									attrib.setName(derivedFrom.getName());
									attrib.setDomain(new ObjectType(derivedFrom.getAliasing()));
									def.add(attrib);
							          } catch (Exception ex) {
							            reportError(ex, derivedToken.getLine());
							          }
							
				}
			}
			else if ((LA(1)==EQUALS)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(EQUALS);
			{
			if ((LA(1)==LITERAL_OID||LA(1)==LITERAL_NO)) {
				{
				if ((LA(1)==LITERAL_OID)) {
					oid = LT(1);
					match(LITERAL_OID);
					match(LITERAL_AS);
					assocOid=domainRef(scope);
					if ( inputState.guessing==0 ) {
						
											if(!(assocOid.getType() instanceof OIDType)){
												reportError (formatMessage ("err_topic_domainnotanoid",assocOid.toString()),oid.getLine());
											}
										
					}
				}
				else if ((LA(1)==LITERAL_NO)) {
					match(LITERAL_NO);
					match(LITERAL_OID);
					if ( inputState.guessing==0 ) {
						assocOid=NoOid.createNoOid();
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				match(SEMI);
				if ( inputState.guessing==0 ) {
					
										def.setOid(assocOid); 
									
				}
			}
			else if ((_tokenSet_20.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
							scope.add(def);
						
			}
			roleDefs(def);
			if ( inputState.guessing==0 ) {
				
							if ((mods & ch.interlis.ili2c.metamodel.Properties.eEXTENDED) != 0)
							{
								AssociationDef overwriting = null;
								AbstractPatternDef baseTopic=(AbstractPatternDef)((AbstractPatternDef)scope).getExtending();
								if(baseTopic!=null){
									overwriting = (AssociationDef) baseTopic.getRealElement
					                                       (AssociationDef.class, def.getName());
								}
							  if (overwriting == null)
							  {
							    reportError (formatMessage (
							      "err_noAssociationToExtend",
							      def.getName(),
							      scope.toString()),
							    a.getLine());
							  }
							  else
							  {
							  	try{
								    def.setExtending (overwriting);
								}catch(Exception ex){
							            reportError(ex, a.getLine());
								}
							  }
							}
						  	try{
							    // check roleDefs
							    def.fixupRoles();
							}catch(Ili2cSemanticException ex){
						            reportError(ex);
							}catch(Exception ex){
						            reportError(ex, a.getLine());
							}
						
			}
			{
			if ((LA(1)==LITERAL_ATTRIBUTE)) {
				match(LITERAL_ATTRIBUTE);
			}
			else if ((_tokenSet_21.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			_loop606:
			do {
				if ((LA(1)==NAME||LA(1)==LITERAL_CONTINUOUS||LA(1)==LITERAL_SUBDIVISION)) {
					attributeDef(def);
				}
				else {
					break _loop606;
				}
				
			} while (true);
			}
			{
			if ((LA(1)==LITERAL_CARDINALITY)) {
				match(LITERAL_CARDINALITY);
				match(EQUALS);
				card=cardinality();
				match(SEMI);
				if ( inputState.guessing==0 ) {
					
								def.setCardinality(card);
								
				}
			}
			else if ((_tokenSet_22.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			_loop609:
			do {
				if ((_tokenSet_10.member(LA(1)))) {
					constr=constraintDef(def,def.getContainer());
					if ( inputState.guessing==0 ) {
						if(constr!=null)def.add(constr);
					}
				}
				else {
					break _loop609;
				}
				
			} while (true);
			}
			match(LITERAL_END);
			{
			if ((LA(1)==NAME)) {
				nam = LT(1);
				match(NAME);
				if ( inputState.guessing==0 ) {
					
								if (!nam.getText().equals(def.getName())){
									reportError(
										formatMessage ("err_end_mismatch", def.toString(),
										def.getName(), nam.getText()),
										nam.getLine());
								}
								
				}
			}
			else if ((LA(1)==SEMI)) {
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
				recover(ex,_tokenSet_13);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final void constraintsDef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		
		Token  c = null;
		
				Viewable def;
				Constraint constr;
			
		
		try {      // for error handling
			c = LT(1);
			match(LITERAL_CONSTRAINTS);
			match(LITERAL_OF);
			def=viewableRef(scope);
			match(EQUALS);
			if ( inputState.guessing==0 ) {
				
					  if(scope==def.getContainer() || (scope instanceof Extendable && ((Extendable)scope).isExtending(def.getContainer()))){
					  	// ok
					  }else{
								reportError (formatMessage ("err_constraint_viewref",
								scope.getScopedName(null)), c.getLine());
					  }
					
			}
			{
			_loop830:
			do {
				if ((_tokenSet_10.member(LA(1)))) {
					constr=constraintDef(def,scope);
					if ( inputState.guessing==0 ) {
						if(constr!=null)def.add(constr);
					}
				}
				else {
					break _loop830;
				}
				
			} while (true);
			}
			match(LITERAL_END);
			match(SEMI);
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
	
	protected final void viewDef(
		Container container
	) throws RecognitionException, TokenStreamException {
		
		Token  viewToken = null;
		Token  n = null;
		Token  extToken = null;
		
			View view = null;
			View base = null;
			LinkedList aliases=new LinkedList();
			Viewable decomposedViewable=null;
			boolean areaDecomp=false;
			Constraint constr;
			Selection select;
			int selLine=0;
			LinkedList cols=null;
			int props=0;
			  String ilidoc=null;
			  Settings metaValues=null;
			
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			viewToken = LT(1);
			match(LITERAL_VIEW);
			n = LT(1);
			match(NAME);
			props=properties(ch.interlis.ili2c.metamodel.Properties.eABSTRACT
			|ch.interlis.ili2c.metamodel.Properties.eEXTENDED
			|ch.interlis.ili2c.metamodel.Properties.eFINAL
			|ch.interlis.ili2c.metamodel.Properties.eTRANSIENT
			);
			{
			switch ( LA(1)) {
			case LITERAL_AREA:
			case LITERAL_INSPECTION:
			case LITERAL_PROJECTION:
			case LITERAL_JOIN:
			case LITERAL_UNION:
			case LITERAL_AGGREGATION:
			{
				view=formationDef(container);
				if ( inputState.guessing==0 ) {
					if((props&ch.interlis.ili2c.metamodel.Properties.eEXTENDED)!=0){
								reportError(formatMessage("err_view_formationdef",n.getText()),n.getLine());
							}
							
				}
				break;
			}
			case LITERAL_EXTENDS:
			{
				extToken = LT(1);
				match(LITERAL_EXTENDS);
				base=viewRef(container);
				if ( inputState.guessing==0 ) {
					
							        if ((props & ch.interlis.ili2c.metamodel.Properties.eEXTENDED) != 0)
							        {
							          reportError(rsrc.getString("err_extendedWithExtends"),
							                      extToken.getLine());
									// create a dummy view
									view=new Projection(); 
							        }else{
									try {
										view=new ExtendedView(base);
									} catch (Exception ex) {
										reportError(ex, extToken.getLine());
									}
									((ExtendedView)view).setExtended(false);
								}
							
				}
				break;
			}
			case EQUALS:
			case LITERAL_WHERE:
			case LITERAL_BASE:
			{
				if ( inputState.guessing==0 ) {
					if((props&ch.interlis.ili2c.metamodel.Properties.eEXTENDED)==0){
								reportError(formatMessage("err_view_missingFormationdef",n.getText()),n.getLine());
								// create a dummy view
								view=new Projection(); 
							}else{
								// check if base topic contains a viewdef with the same name
								base = null;
								AbstractPatternDef baseTopic=(AbstractPatternDef)((AbstractPatternDef)container).getExtending();
								if(baseTopic!=null){
									base = (View) baseTopic.getRealElement(View.class, n.getText());
								}
								  if (base == null)
								  {
								    reportError (formatMessage (
								      "err_view_nothingToExtend",
								      n.getText(),
								      container.toString()),
								      viewToken.getLine());
								  }
								  else
								  {
									try {
										view=new ExtendedView(base);
									} catch (Exception ex) {
										reportError(ex, n.getLine());
									}
									((ExtendedView)view).setExtended(true);
								  }
							}
							
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				
						try {
							view.setSourceLine(n.getLine());
							view.setName(n.getText());
							view.setAbstract((props & ch.interlis.ili2c.metamodel.Properties.eABSTRACT) != 0);
							view.setFinal((props & ch.interlis.ili2c.metamodel.Properties.eFINAL) != 0);
							view.setTransient((props & ch.interlis.ili2c.metamodel.Properties.eTRANSIENT) != 0);
						} catch (Exception ex) {
							reportError(ex, n.getLine());
						}
						container.add(view);		
					
			}
			{
			_loop894:
			do {
				if ((LA(1)==LITERAL_BASE)) {
					baseExtensionDef(view);
				}
				else {
					break _loop894;
				}
				
			} while (true);
			}
			{
			_loop896:
			do {
				if ((LA(1)==LITERAL_WHERE)) {
					if ( inputState.guessing==0 ) {
						
								selLine=LT(1).getLine(); 
							
					}
					select=selection(view,view);
					if ( inputState.guessing==0 ) {
						
						try {
						if (select != null)
						view.add (select);
						} catch (Exception ex) {
						reportError (ex, selLine );
						}
						
					}
				}
				else {
					break _loop896;
				}
				
			} while (true);
			}
			match(EQUALS);
			if ( inputState.guessing==0 ) {
				
					view.setDocumentation(ilidoc);
					view.setMetaValues(metaValues);
					
			}
			viewAttributes(view);
			{
			_loop898:
			do {
				if ((_tokenSet_10.member(LA(1)))) {
					constr=constraintDef(view,view.getContainer());
					if ( inputState.guessing==0 ) {
						
						if (constr != null)
						view.add (constr);
						
					}
				}
				else {
					break _loop898;
				}
				
			} while (true);
			}
			end(view);
			match(SEMI);
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
	
	protected final void graphicDef(
		Container cont
	) throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
		Viewable basedOn = null;
		int mods = 0;
		Graphic graph = null;
		Graphic extending = null;
		Selection sel = null;
		int selLine=0;
			  String ilidoc=null;
			  Settings metaValues=null;
		
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			match(LITERAL_GRAPHIC);
			n = LT(1);
			match(NAME);
			mods=properties(ch.interlis.ili2c.metamodel.Properties.eABSTRACT
			|ch.interlis.ili2c.metamodel.Properties.eFINAL);
			{
			if ((LA(1)==LITERAL_EXTENDS)) {
				match(LITERAL_EXTENDS);
				extending=graphicRef(cont);
			}
			else if ((LA(1)==EQUALS||LA(1)==LITERAL_BASED)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((LA(1)==LITERAL_BASED)) {
				match(LITERAL_BASED);
				match(LITERAL_ON);
				basedOn=viewableRefDepReq(cont);
			}
			else if ((LA(1)==EQUALS)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(EQUALS);
			if ( inputState.guessing==0 ) {
				
				graph = new Graphic ();
				try {
				graph.setName (n.getText());
					graph.setDocumentation(ilidoc);
					graph.setMetaValues(metaValues);
				} catch (Exception ex) {
				reportError (ex, n.getLine());
				}
				
				try {
				graph.setAbstract ((mods & ch.interlis.ili2c.metamodel.Properties.eABSTRACT) != 0);
				} catch (Exception ex) {
				reportError (ex, n.getLine ());
				}
				
				try {
				graph.setFinal ((mods & ch.interlis.ili2c.metamodel.Properties.eFINAL) != 0);
				} catch (Exception ex) {
				reportError (ex, n.getLine ());
				}
				
				
				try {
				graph.setExtending (extending);
				} catch (Exception ex) {
				reportError (ex, n.getLine ());
				}
				
				try {
				if (basedOn == null)
				{
				if (extending == null)
				{
				reportError (formatMessage (
				"err_graphic_basedOnOmitted",
				graph.toString ()),
				n.getLine ());
				panic ();
				}
				basedOn = extending.getBasedOn ();
				}
				
				graph.setBasedOn (basedOn);
				} catch (Exception ex) {
				reportError (ex, n.getLine ());
				}
				
				try {
				cont.add (graph);
				} catch (Exception ex) {
				reportError (ex, n.getLine());
				}
				
				
			}
			{
			_loop936:
			do {
				if ((LA(1)==LITERAL_WHERE)) {
					if ( inputState.guessing==0 ) {
						selLine=LT(1).getLine();
					}
					sel=selection(basedOn,graph);
					if ( inputState.guessing==0 ) {
						
						if (sel != null)
						{
						try {
						graph.add (sel);
						} catch (Exception ex) {
						reportError (ex, selLine);
						}
						}
						
					}
				}
				else {
					break _loop936;
				}
				
			} while (true);
			}
			{
			_loop938:
			do {
				if ((LA(1)==NAME)) {
					drawingRule(graph);
				}
				else {
					break _loop938;
				}
				
			} while (true);
			}
			end(graph);
			match(SEMI);
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
	
	protected final int  names2(
		List names
	) throws RecognitionException, TokenStreamException {
		int lineNumber;
		
		Token  ili = null;
		Token  iName = null;
		Token  i2Name = null;
		Token  isg = null;
		Token  irf = null;
		Token  imo = null;
		Token  iur = null;
		Token  inm = null;
		Token  ibo = null;
		Token  iha = null;
		Token  iva = null;
		Token  sg = null;
		Token  rf = null;
		Token  mo = null;
		Token  firstName = null;
		Token  nextName = null;
		
		lineNumber = 0;
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_INTERLIS:
			{
				{
				ili = LT(1);
				match(LITERAL_INTERLIS);
				match(DOT);
				if ( inputState.guessing==0 ) {
					lineNumber = ili.getLine();
				}
				{
				switch ( LA(1)) {
				case NAME:
				{
					{
					iName = LT(1);
					match(NAME);
					if ( inputState.guessing==0 ) {
						names.add(ili.getText());names.add(iName.getText());
					}
					{
					if ((LA(1)==DOT)) {
						match(DOT);
						i2Name = LT(1);
						match(NAME);
						if ( inputState.guessing==0 ) {
							names.add(i2Name.getText());
						}
					}
					else if ((_tokenSet_23.member(LA(1)))) {
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
					}
					break;
				}
				case LITERAL_SIGN:
				{
					isg = LT(1);
					match(LITERAL_SIGN);
					if ( inputState.guessing==0 ) {
						names.add(ili.getText());names.add(isg.getText());
					}
					break;
				}
				case LITERAL_REFSYSTEM:
				{
					irf = LT(1);
					match(LITERAL_REFSYSTEM);
					if ( inputState.guessing==0 ) {
						names.add(ili.getText());names.add(irf.getText());
					}
					break;
				}
				case LITERAL_METAOBJECT:
				{
					imo = LT(1);
					match(LITERAL_METAOBJECT);
					if ( inputState.guessing==0 ) {
						names.add(ili.getText());names.add(imo.getText());
					}
					break;
				}
				case LITERAL_URI:
				{
					iur = LT(1);
					match(LITERAL_URI);
					if ( inputState.guessing==0 ) {
						names.add(ili.getText());names.add(iur.getText());
					}
					break;
				}
				case LITERAL_NAME:
				{
					inm = LT(1);
					match(LITERAL_NAME);
					if ( inputState.guessing==0 ) {
						names.add(ili.getText());names.add(inm.getText());
					}
					break;
				}
				case LITERAL_BOOLEAN:
				{
					ibo = LT(1);
					match(LITERAL_BOOLEAN);
					if ( inputState.guessing==0 ) {
						names.add(ili.getText());names.add(ibo.getText());
					}
					break;
				}
				case LITERAL_HALIGNMENT:
				{
					iha = LT(1);
					match(LITERAL_HALIGNMENT);
					if ( inputState.guessing==0 ) {
						names.add(ili.getText());names.add(iha.getText());
					}
					break;
				}
				case LITERAL_VALIGNMENT:
				{
					iva = LT(1);
					match(LITERAL_VALIGNMENT);
					if ( inputState.guessing==0 ) {
						names.add(ili.getText());names.add(iva.getText());
					}
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				}
				break;
			}
			case LITERAL_SIGN:
			{
				sg = LT(1);
				match(LITERAL_SIGN);
				if ( inputState.guessing==0 ) {
					lineNumber = sg.getLine();names.add(sg.getText());
				}
				break;
			}
			case LITERAL_REFSYSTEM:
			{
				rf = LT(1);
				match(LITERAL_REFSYSTEM);
				if ( inputState.guessing==0 ) {
					lineNumber = rf.getLine();names.add(rf.getText());
				}
				break;
			}
			case LITERAL_METAOBJECT:
			{
				mo = LT(1);
				match(LITERAL_METAOBJECT);
				if ( inputState.guessing==0 ) {
					lineNumber = mo.getLine();names.add(mo.getText());
				}
				break;
			}
			case NAME:
			{
				{
				firstName = LT(1);
				match(NAME);
				if ( inputState.guessing==0 ) {
					
					lineNumber = firstName.getLine();
					names.add(firstName.getText());
					
				}
				{
				_loop980:
				do {
					if ((LA(1)==DOT)) {
						match(DOT);
						nextName = LT(1);
						match(NAME);
						if ( inputState.guessing==0 ) {
							names.add(nextName.getText());
						}
					}
					else {
						break _loop980;
					}
					
				} while (true);
				}
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
				recover(ex,_tokenSet_23);
			} else {
			  throw ex;
			}
		}
		return lineNumber;
	}
	
	protected final Table  classRef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		Table t;
		
		
		List      nams = new LinkedList();
		t = null;
		int lin = 0;
		String tableName = null;
		
		
		try {      // for error handling
			lin=names2(nams);
			if ( inputState.guessing==0 ) {
				
				Model model;
				AbstractPatternDef    topic;
				String   modelName, topicName;
				
				switch (nams.size()) {
				case 1:
				model = (Model) scope.getContainerOrSame(Model.class);
				topic = (AbstractPatternDef) scope.getContainerOrSame(AbstractPatternDef.class);
				tableName = (String) nams.get(0);
				break;
				
				case 2:
				modelName = (String) nams.get(0);
				model = resolveOrFixModelName(scope, modelName, lin);
				tableName = (String) nams.get(1);
				topic = null;
				break;
				
				case 3:
				modelName = (String) nams.get(0);
				topicName = (String) nams.get(1);
				model = resolveOrFixModelName(scope, modelName, lin);
				topic = resolveOrFixTopicName(model, topicName, lin);
				tableName = (String) nams.get(2);
				break;
				
				default:
				reportError(rsrc.getString("err_weirdTableRef"), lin);
				model = resolveModelName(scope, (String) nams.get(0));
				if (model == null)
				model = (Model) scope.getContainerOrSame(Model.class);
				
				topic = null;
				tableName = (String) nams.get(nams.size() - 1);
				break;
				}
				
				t = null;
				if (topic != null)
				t = (Table) topic.getElement (Table.class, tableName);
				if ((t == null) && (model != null)){
				t = (Table) model.getElement (Table.class, tableName);
				}
				if ((t == null) && (nams.size() == 1)){
					// unqualified name; search also in unqaulified imported models
				t = (Table) model.getImportedElement (Table.class, tableName);
				}
				
				if (t == null)
				{
				if (topic == null)
				reportError (formatMessage ("err_noSuchTable", tableName,
				model.toString()), lin);
				else
				reportError (formatMessage ("err_noSuchTable", tableName,
				topic.toString()), lin);
				
				if (model != modelInterlis)
				{
				try {
				/* try a fix, so we can continue parsing */
				t = new Table();
				t.setName(tableName);
				if (topic == null) {
				t.setAbstract(true);
				model.add(t);
				} else {
				topic.add(t);
				}
				} catch (Exception ex) {
				panic();
				}
				}
				
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_24);
			} else {
			  throw ex;
			}
		}
		return t;
	}
	
	protected final void attributeDef(
		Viewable container
	) throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
			  int mods = 0;
			  LocalAttribute attrib = null;
			  AttributeDef overriding = null;
			  Type overridingDomain = null;
			  Cardinality overridingCardinality = new Cardinality(0,Cardinality.UNBOUND);
			  Type type = null;
			  boolean mandatory = false;
			  boolean isContinuous=false;
			  boolean isSubdivision=false;
			  String ilidoc=null;
			  Settings metaValues=null;
			  Evaluable f=null;
			  ArrayList fv=new ArrayList();
			
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			{
			if ((LA(1)==LITERAL_CONTINUOUS||LA(1)==LITERAL_SUBDIVISION)) {
				{
				if ((LA(1)==LITERAL_CONTINUOUS)) {
					match(LITERAL_CONTINUOUS);
					if ( inputState.guessing==0 ) {
						isContinuous=true;
					}
				}
				else if ((LA(1)==LITERAL_SUBDIVISION)) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				match(LITERAL_SUBDIVISION);
				if ( inputState.guessing==0 ) {
					isSubdivision=true;
				}
			}
			else if ((LA(1)==NAME)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			n = LT(1);
			match(NAME);
			mods=properties(ch.interlis.ili2c.metamodel.Properties.eABSTRACT
		|ch.interlis.ili2c.metamodel.Properties.eEXTENDED
		|ch.interlis.ili2c.metamodel.Properties.eFINAL
		|ch.interlis.ili2c.metamodel.Properties.eTRANSIENT
		);
			match(COLON);
			if ( inputState.guessing==0 ) {
				
				overriding = findOverridingAttribute (
				container, mods, n.getText(), n.getLine());
				if (overriding != null){
				overridingDomain = overriding.getDomainResolvingAliases();
				}
					attrib = new LocalAttribute();
				try {
				attrib.setName(n.getText());
				attrib.setSourceLine(n.getLine());
					  attrib.setDocumentation(ilidoc);
					  attrib.setMetaValues(metaValues);
				attrib.setAbstract((mods & ch.interlis.ili2c.metamodel.Properties.eABSTRACT) != 0);
				attrib.setFinal((mods & ch.interlis.ili2c.metamodel.Properties.eFINAL) != 0);
					  attrib.setSubdivision(isSubdivision);
					  attrib.setContinuous(isContinuous);
					  attrib.setTransient((mods & ch.interlis.ili2c.metamodel.Properties.eTRANSIENT) != 0);
				} catch (Exception ex) {
				reportError(ex, n.getLine());
				}
				
			}
			type=attrTypeDef(container,/* alias ok */ true, overridingDomain,
                     n.getLine(),null);
			if ( inputState.guessing==0 ) {
				
				if(type!=null){
						    	if(type instanceof ReferenceType){
								ReferenceType rt=(ReferenceType)type;
								if(!((Table)rt.getReferred()).isIdentifiable()){
									reportError(formatMessage("err_attributeDef_refattrToStruct",n.getText()),n.getLine());
								}
							}
							try{
							attrib.setDomain(type);
							}catch(Exception ex){
								reportError(ex, n.getLine());
							}
				}
						
			}
			{
			if ((LA(1)==COLONEQUALS)) {
				match(COLONEQUALS);
				f=factor(container,container);
				if ( inputState.guessing==0 ) {
					
									fv.add(f);
									
				}
				{
				_loop576:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						f=factor(container,container);
						if ( inputState.guessing==0 ) {
							
											fv.add(f);
											
						}
					}
					else {
						break _loop576;
					}
					
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					
								attrib.setBasePaths((Evaluable[])fv.toArray(new Evaluable[fv.size()]));
								
				}
			}
			else if ((LA(1)==SEMI)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
							if(attrib.isTransient() && f==null && !attrib.isAbstract()){
								reportError(formatMessage("err_attributeDef_transientWoFactor",n.getText()),n.getLine());
							}
							if(overriding!=null){
								if(overriding.isTransient()!=attrib.isTransient()){
									reportError(formatMessage("err_attributeDef_transientModeChange",n.getText()),n.getLine());
								}
							}
						
			}
			match(SEMI);
			if ( inputState.guessing==0 ) {
				
				try {
				container.add(attrib);
				attrib.setExtending(overriding);
				} catch (Exception ex) {
				reportError(ex, n.getLine());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_25);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final Constraint  constraintDef(
		Viewable constrained,Container context
	) throws RecognitionException, TokenStreamException {
		Constraint constr;
		
		
			constr = null;
			  String ilidoc=null;
			  Settings metaValues=null;
			
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			{
			switch ( LA(1)) {
			case LITERAL_MANDATORY:
			{
				constr=mandatoryConstraint(constrained,context);
				break;
			}
			case LITERAL_CONSTRAINT:
			{
				constr=plausibilityConstraint(constrained,context);
				break;
			}
			case LITERAL_EXISTENCE:
			{
				constr=existenceConstraint(constrained,context);
				break;
			}
			case LITERAL_UNIQUE:
			{
				constr=uniquenessConstraint(constrained,context);
				break;
			}
			case LITERAL_SET:
			{
				constr=setConstraint(constrained,context);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				
						if(constr!=null){
							constr.setDocumentation(ilidoc);
							constr.setMetaValues(metaValues);
						}
					
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		return constr;
	}
	
	protected final void parameterDef(
		Table container
	) throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		Token  pt = null;
		
			int mods = 0;
			Type type = null;
			boolean mandatory = false;
			boolean declaredExtended = false;
			Parameter overriding = null;
			Type overridingDomain = null;
			Table referred = null;
			  String ilidoc=null;
			  Settings metaValues=null;
			
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			n = LT(1);
			match(NAME);
			mods=properties(ch.interlis.ili2c.metamodel.Properties.eABSTRACT
			|ch.interlis.ili2c.metamodel.Properties.eEXTENDED
			|ch.interlis.ili2c.metamodel.Properties.eFINAL);
			match(COLON);
			if ( inputState.guessing==0 ) {
				
					/* TODO handle ABSTRACT and FINAL */
				declaredExtended = (mods & ch.interlis.ili2c.metamodel.Properties.eEXTENDED) != 0;
				
				overriding =  (Parameter) container.getRealElement (
				Parameter.class, n.getText());
				if (overriding != null)
				overridingDomain = overriding.getType();
				
				if ((overriding == null) && declaredExtended)
				{
				reportError (formatMessage ("err_parameter_nothingToExtend",
				n.getText(),
				container.toString()),
				n.getLine());
				}
				
				if ((overriding != null)
				&& (container == overriding.getContainer (Viewable.class)))
				{
				reportError (formatMessage ("err_parameter_nameInSameContainer",
				container.toString(), n.getText()),
				n.getLine());
				}
				else if ((overriding != null) && !declaredExtended)
				{
				reportError (formatMessage ("err_parameter_extendedWithoutDecl",
				n.getText(), container.toString(),
				overriding.toString()),
				n.getLine());
				}
				
			}
			{
			boolean synPredMatched780 = false;
			if (((LA(1)==LITERAL_METAOBJECT))) {
				int _m780 = mark();
				synPredMatched780 = true;
				inputState.guessing++;
				try {
					{
					match(LITERAL_METAOBJECT);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched780 = false;
				}
				rewind(_m780);
inputState.guessing--;
			}
			if ( synPredMatched780 ) {
				pt = LT(1);
				match(LITERAL_METAOBJECT);
				{
				if ((LA(1)==LITERAL_OF)) {
					match(LITERAL_OF);
					referred=classRef(container);
				}
				else if ((LA(1)==SEMI)) {
					if ( inputState.guessing==0 ) {
						
											referred=container;
										
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				if ( inputState.guessing==0 ) {
					
										MetaobjectType reference = new MetaobjectType();
					
										try {
											reference.setReferred (referred);
										} catch (Exception ex) {
											reportError (ex, pt.getLine());
										}
										type = reference;
									
				}
			}
			else if ((_tokenSet_27.member(LA(1)))) {
				type=attrTypeDef(container,true,overridingDomain,n.getLine(),null);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
				Parameter p = new Parameter();
				
				try {
				p.setName (n.getText());
					p.setDocumentation(ilidoc);
					p.setMetaValues(metaValues);
				container.add(p);
				p.setType (type);
				p.setExtending (overriding);
				p.setAbstract((mods & ch.interlis.ili2c.metamodel.Properties.eABSTRACT) != 0);
				p.setFinal((mods & ch.interlis.ili2c.metamodel.Properties.eFINAL) != 0);
				} catch (Exception ex) {
				reportError(ex, n.getLine());
				}
				
			}
			match(SEMI);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_28);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final Table  structureRef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		Table t;
		
		
			t=null;
			int refto=0;
			
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				refto=LT(1).getLine();
			}
			t=classRef(scope);
			if ( inputState.guessing==0 ) {
				
						if(t.isIdentifiable()){
							reportError(formatMessage("err_structRef_StructRequired",t.getScopedName(null)),refto);
						}
						
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_29);
			} else {
			  throw ex;
			}
		}
		return t;
	}
	
	protected final Type  attrTypeDef(
		Container  scope,
	boolean    allowAliases,
	Type       extending,
	int        line,
	ArrayList formalArgs
	) throws RecognitionException, TokenStreamException {
		Type typ;
		
		
				typ=null;
				Cardinality card=null;
				CompositionType ct=null;
				boolean ordered=false;
			
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_MANDATORY:
			{
				match(LITERAL_MANDATORY);
				{
				if ((_tokenSet_30.member(LA(1)))) {
					typ=attrType(scope,allowAliases,extending,line,formalArgs);
					if ( inputState.guessing==0 ) {
						
									     try {
										if (typ != null){
										  typ.setMandatory(true);
										}
									      } catch (Exception ex) {
										reportError(ex, line);
									      }
									  if(extending!=null && typ instanceof EnumerationType){
										try {
										  ((EnumerationType)typ).checkTypeExtension(extending,false);
										} catch (Exception ex) {
										  reportError (ex, line);
										  typ = null;
										}
									  }
									
					}
				}
				else if ((_tokenSet_31.member(LA(1)))) {
					if ( inputState.guessing==0 ) {
						
								      if (extending != null){
										try {
										  typ = (Type) extending.clone ();
										} catch (Exception ex) {
										  reportError (ex, line);
										  typ = null;
										}
								      }else{
										reportError (rsrc.getString ("err_type_mandatoryLonely"), line);
										typ = null;
								      }
								      try {
									if (typ != null){
									  typ.setMandatory(true);
									}
								      } catch (Exception ex) {
									reportError(ex, line);
								      }
								
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				break;
			}
			case LITERAL_INTERLIS:
			case LITERAL_REFSYSTEM:
			case NAME:
			case LPAREN:
			case STRING:
			case LITERAL_OID:
			case LITERAL_CLASS:
			case LITERAL_STRUCTURE:
			case LITERAL_ATTRIBUTE:
			case LITERAL_ANYSTRUCTURE:
			case LITERAL_REFERENCE:
			case LITERAL_URI:
			case LITERAL_NAME:
			case LITERAL_MTEXT:
			case LITERAL_TEXT:
			case LITERAL_ALL:
			case LITERAL_HALIGNMENT:
			case LITERAL_VALIGNMENT:
			case LITERAL_BOOLEAN:
			case LITERAL_NUMERIC:
			case LITERAL_FORMAT:
			case LITERAL_DATE:
			case LITERAL_TIMEOFDAY:
			case LITERAL_DATETIME:
			case LITERAL_COORD:
			case LITERAL_MULTICOORD:
			case LITERAL_BLACKBOX:
			case LITERAL_DIRECTED:
			case LITERAL_POLYLINE:
			case LITERAL_MULTIPOLYLINE:
			case LITERAL_SURFACE:
			case LITERAL_MULTISURFACE:
			case LITERAL_AREA:
			case LITERAL_MULTIAREA:
			case LITERAL_SIGN:
			case LITERAL_METAOBJECT:
			case DEC:
			case POSINT:
			case NUMBER:
			{
				typ=attrType(scope,allowAliases,extending,line,formalArgs);
				if ( inputState.guessing==0 ) {
					
								  if(extending!=null && typ instanceof EnumerationType){
									try {
									  ((EnumerationType)typ).checkTypeExtension(extending,false);
									} catch (Exception ex) {
									  reportError (ex, line);
									  typ = null;
									}
								  }
							
				}
				break;
			}
			case LITERAL_BAG:
			case LITERAL_LIST:
			{
				{
				if ((LA(1)==LITERAL_BAG)) {
					match(LITERAL_BAG);
					if ( inputState.guessing==0 ) {
						ordered=false;
					}
				}
				else if ((LA(1)==LITERAL_LIST)) {
					match(LITERAL_LIST);
					if ( inputState.guessing==0 ) {
						ordered=true;
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				{
				if ((LA(1)==LCURLY)) {
					card=cardinality();
				}
				else if ((LA(1)==LITERAL_OF)) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				match(LITERAL_OF);
				typ=attrType(scope,allowAliases,extending,line,formalArgs);
				if ( inputState.guessing==0 ) {
					
								try{
									if(card!=null){
										typ.setCardinality(card);
									}else{
										typ.setCardinality(new Cardinality(0,Cardinality.UNBOUND));
									}
									typ.setOrdered(ordered);
								}catch(Exception ex){
								    reportError(ex, line);
								}
							
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
				recover(ex,_tokenSet_31);
			} else {
			  throw ex;
			}
		}
		return typ;
	}
	
	protected final Evaluable  factor(
		Container ns,Container functionNs
	) throws RecognitionException, TokenStreamException {
		Evaluable ev;
		
		
			ev = null;
			Viewable dummy;
			Function dummyFunc;
			Evaluable dyEv;
			GraphicParameterDef param=null;
			List      nams = new LinkedList();
			int lin = 0;
			InspectionFactor inspFactor=null;
			ObjectPath inspRestriction=null;
			
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_PARAMETER:
			{
				{
				match(LITERAL_PARAMETER);
				lin=names2(nams);
				}
				if ( inputState.guessing==0 ) {
					
								param=resolveRuntimeParameterRef(ns,(String[]) nams.toArray(new String[nams.size()]),lin);
								ev=new ParameterValue(param);
							
				}
				break;
			}
			case LITERAL_AREA:
			case LITERAL_INSPECTION:
			{
				{
				{
				boolean synPredMatched853 = false;
				if (((LA(1)==LITERAL_AREA||LA(1)==LITERAL_INSPECTION))) {
					int _m853 = mark();
					synPredMatched853 = true;
					inputState.guessing++;
					try {
						{
						{
						if ((LA(1)==LITERAL_AREA)) {
							match(LITERAL_AREA);
						}
						else if ((LA(1)==LITERAL_INSPECTION)) {
						}
						else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						
						}
						match(LITERAL_INSPECTION);
						match(LITERAL_OF);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched853 = false;
					}
					rewind(_m853);
inputState.guessing--;
				}
				if ( synPredMatched853 ) {
					dummy=inspection(ns);
					if ( inputState.guessing==0 ) {
						
									DecompositionView insp=(DecompositionView)dummy;
									ev=inspFactor=new InspectionFactor();
									inspFactor.setRenamedViewable(insp.getRenamedViewable());
									inspFactor.setDecomposedAttribute(insp.getDecomposedAttribute());
									inspFactor.setAreaInspection(insp.isAreaDecomposition());
									
					}
				}
				else if ((LA(1)==LITERAL_INSPECTION)) {
					match(LITERAL_INSPECTION);
					dummy=viewableRef(ns);
					if ( inputState.guessing==0 ) {
						ev=inspFactor=new InspectionFactor();
									inspFactor.setInspectionViewable((DecompositionView)dummy);
									
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				{
				if ((LA(1)==LITERAL_OF)) {
					match(LITERAL_OF);
					inspRestriction=objectOrAttributePath((Viewable)ns,functionNs);
					if ( inputState.guessing==0 ) {
						inspFactor.setRestriction(inspRestriction); 
									
					}
				}
				else if ((_tokenSet_32.member(LA(1)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				}
				break;
			}
			case STRING:
			case LITERAL_UNDEFINED:
			case HASH:
			case GREATER:
			case LITERAL_PI:
			case LITERAL_LNBASE:
			case GREATERGREATER:
			case DEC:
			case POSINT:
			case NUMBER:
			{
				ev=constant(ns);
				break;
			}
			default:
				boolean synPredMatched847 = false;
				if (((_tokenSet_7.member(LA(1))))) {
					int _m847 = mark();
					synPredMatched847 = true;
					inputState.guessing++;
					try {
						{
						xyRef();
						match(LPAREN);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched847 = false;
					}
					rewind(_m847);
inputState.guessing--;
				}
				if ( synPredMatched847 ) {
					ev=functionCall(ns,functionNs);
				}
				else if ((_tokenSet_33.member(LA(1)))) {
					{
					if ( inputState.guessing==0 ) {
						
									if(!(ns instanceof Viewable)){
										reportError (formatMessage ("err_Container_currentIsNotViewable",
										ns.toString()), LT(1).getLine());
									}
								
					}
					ev=objectOrAttributePath((Viewable)ns,functionNs);
					}
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_32);
			} else {
			  throw ex;
			}
		}
		return ev;
	}
	
	protected final Type  attrType(
		Container  scope,
	boolean    allowAliases,
	Type       extending,
	int        line,
	ArrayList formalArgs
	) throws RecognitionException, TokenStreamException {
		Type typ;
		
		Token  as = null;
		
				List nams = new LinkedList();
				typ=null;
				Table restrictedTo=null;
				int lin=0;
				CompositionType ct=null;
			
		
		try {      // for error handling
			switch ( LA(1)) {
			case LPAREN:
			case STRING:
			case LITERAL_OID:
			case LITERAL_CLASS:
			case LITERAL_STRUCTURE:
			case LITERAL_ATTRIBUTE:
			case LITERAL_URI:
			case LITERAL_NAME:
			case LITERAL_MTEXT:
			case LITERAL_TEXT:
			case LITERAL_ALL:
			case LITERAL_HALIGNMENT:
			case LITERAL_VALIGNMENT:
			case LITERAL_BOOLEAN:
			case LITERAL_NUMERIC:
			case LITERAL_FORMAT:
			case LITERAL_DATE:
			case LITERAL_TIMEOFDAY:
			case LITERAL_DATETIME:
			case LITERAL_COORD:
			case LITERAL_MULTICOORD:
			case LITERAL_BLACKBOX:
			case LITERAL_DIRECTED:
			case LITERAL_POLYLINE:
			case LITERAL_MULTIPOLYLINE:
			case LITERAL_SURFACE:
			case LITERAL_MULTISURFACE:
			case LITERAL_AREA:
			case LITERAL_MULTIAREA:
			case DEC:
			case POSINT:
			case NUMBER:
			{
				typ=type(scope,extending,formalArgs,false);
				break;
			}
			case LITERAL_INTERLIS:
			case LITERAL_REFSYSTEM:
			case NAME:
			case LITERAL_ANYSTRUCTURE:
			case LITERAL_SIGN:
			case LITERAL_METAOBJECT:
			{
				{
				if ((_tokenSet_7.member(LA(1)))) {
					lin=names2(nams);
					if ( inputState.guessing==0 ) {
						
									Table s;
									Element e=resolveStructureOrDomainRef(scope,(String[]) nams.toArray(new String[nams.size()]),lin);
									if(e instanceof Table){
										s=(Table)e;
										ct=new CompositionType();
										try{
											ct.setSourceLine(lin);
											ct.setCardinality(new Cardinality(0,1));
											ct.setComponentType(s);
										}catch(Exception ex){
										    reportError(ex, line);
										}
										typ=ct;
									}else{
										Domain aliased=(Domain)e;
										if ((!allowAliases)
										  && !((aliased != null) && (aliased.getContainer (Model.class) == td.INTERLIS)))
										{
										  reportError (rsrc.getString ("err_aliasAtWrongPlace"), line);
										}
										else
										{
										  typ = new TypeAlias();
										  try {
										    ((TypeAlias) typ).setAliasing (aliased);
										  } catch (Exception ex) {
										    reportError(ex, line);
										  }
										}
										ct=null;
									}
						
								
					}
				}
				else if ((LA(1)==LITERAL_ANYSTRUCTURE)) {
					as = LT(1);
					match(LITERAL_ANYSTRUCTURE);
					if ( inputState.guessing==0 ) {
						
										ct=new CompositionType();
										try{
											ct.setSourceLine(as.getLine());
											ct.setCardinality(new Cardinality(0,1));
											ct.setComponentType(modelInterlis.ANYSTRUCTURE);
										}catch(Exception ex){
										    reportError(ex, as.getLine());
										}
										typ=ct;
								
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				{
				if (((LA(1)==LITERAL_RESTRICTION))&&(ct!=null)) {
					match(LITERAL_RESTRICTION);
					match(LPAREN);
					restrictedTo=structureRef(scope);
					if ( inputState.guessing==0 ) {
						ct.addRestrictedTo(restrictedTo);
					}
					{
					_loop585:
					do {
						if ((LA(1)==SEMI)) {
							match(SEMI);
							restrictedTo=structureRef(scope);
							if ( inputState.guessing==0 ) {
								ct.addRestrictedTo(restrictedTo);
							}
						}
						else {
							break _loop585;
						}
						
					} while (true);
					}
					match(RPAREN);
				}
				else if ((_tokenSet_31.member(LA(1)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				break;
			}
			case LITERAL_REFERENCE:
			{
				typ=referenceAttr(scope);
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
				recover(ex,_tokenSet_31);
			} else {
			  throw ex;
			}
		}
		return typ;
	}
	
	protected final Cardinality  cardinality() throws RecognitionException, TokenStreamException {
		Cardinality card;
		
		Token  lcurl = null;
		
			  long min = 0;
			  long max = Cardinality.UNBOUND;
			  card=null;
			
		
		try {      // for error handling
			lcurl = LT(1);
			match(LCURLY);
			{
			if ((LA(1)==STAR)) {
				match(STAR);
			}
			else if ((LA(1)==POSINT)) {
				min=posInteger();
				{
				if ((LA(1)==DOTDOT)) {
					match(DOTDOT);
					{
					if ((LA(1)==POSINT)) {
						max=posInteger();
					}
					else if ((LA(1)==STAR)) {
						match(STAR);
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
				}
				else if ((LA(1)==RCURLY)) {
					if ( inputState.guessing==0 ) {
						max = min;
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(RCURLY);
			if ( inputState.guessing==0 ) {
				
				try {
				card = new Cardinality(min, max);
				} catch (Exception ex) {
				reportError(ex, lcurl.getLine());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_34);
			} else {
			  throw ex;
			}
		}
		return card;
	}
	
	protected final Type  type(
		Container scope,Type extending,ArrayList formalArgs,boolean isGeneric
	) throws RecognitionException, TokenStreamException {
		Type typ;
		
		typ=null;
			
		
		try {      // for error handling
			{
			if ((_tokenSet_35.member(LA(1)))) {
				typ=baseType(scope,extending,formalArgs,isGeneric);
			}
			else if (((LA(1) >= LITERAL_DIRECTED && LA(1) <= LITERAL_MULTIAREA))) {
				typ=lineType(scope,extending);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_36);
			} else {
			  throw ex;
			}
		}
		return typ;
	}
	
	protected final ReferenceType  referenceAttr(
		Container scope
	) throws RecognitionException, TokenStreamException {
		ReferenceType rt;
		
		Token  refkw = null;
		
			rt=null;
			int mods=0;
			
		
		try {      // for error handling
			refkw = LT(1);
			match(LITERAL_REFERENCE);
			match(LITERAL_TO);
			mods=properties(ch.interlis.ili2c.metamodel.Properties.eEXTERNAL);
			rt=restrictedClassOrAssRef(scope);
			if ( inputState.guessing==0 ) {
				
						boolean external=(mods & ch.interlis.ili2c.metamodel.Properties.eEXTERNAL)!=0;
						Topic thisTopic=(Topic)scope.getContainerOrSame(Topic.class);
					  	try{
						  Iterator<AbstractClassDef> targeti=rt.iteratorRestrictedTo();
						  if(targeti.hasNext()){
							  while(targeti.hasNext()){
								  AbstractClassDef<?> target=targeti.next();
								  AbstractPatternDef.checkRefTypeTarget(thisTopic, null,null, target, external);
							  }
						  }else{
							  AbstractClassDef<?> target=rt.getReferred();
							  AbstractPatternDef.checkRefTypeTarget(thisTopic, null, null,target, external);
						  }
						}catch(Ili2cSemanticException ex){
					            reportError(ex,refkw.getLine());
					        }
						rt.setExternal(external);
					
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_31);
			} else {
			  throw ex;
			}
		}
		return rt;
	}
	
	protected final ReferenceType  restrictedClassOrAssRef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		ReferenceType rt;
		
		
				AbstractClassDef ref;
				AbstractClassDef restrictedTo;
				rt=new ReferenceType();
				int refto=0;
				int mods=0;
			
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				refto=LT(1).getLine();
			}
			{
			if ((LA(1)==LITERAL_ANYCLASS)) {
				match(LITERAL_ANYCLASS);
				if ( inputState.guessing==0 ) {
					
							try{
								rt.setReferred(modelInterlis.ANYCLASS);
							}catch(Exception ex){
								reportError(ex,refto);
							}
						
				}
			}
			else if ((_tokenSet_7.member(LA(1)))) {
				ref=classOrAssociationRef(scope);
				if ( inputState.guessing==0 ) {
					
							try{
							  rt.setReferred(ref);
							}catch(Exception ex){
								reportError(ex,refto);
							}
						
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				rt.setSourceLine(refto);
					
			}
			{
			if ((LA(1)==LITERAL_RESTRICTION)) {
				match(LITERAL_RESTRICTION);
				match(LPAREN);
				restrictedTo=classOrAssociationRef(scope);
				if ( inputState.guessing==0 ) {
					rt.addRestrictedTo(restrictedTo);
				}
				{
				_loop591:
				do {
					if ((LA(1)==SEMI)) {
						match(SEMI);
						restrictedTo=classOrAssociationRef(scope);
						if ( inputState.guessing==0 ) {
							rt.addRestrictedTo(restrictedTo);
						}
					}
					else {
						break _loop591;
					}
					
				} while (true);
				}
				match(RPAREN);
			}
			else if ((_tokenSet_37.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_37);
			} else {
			  throw ex;
			}
		}
		return rt;
	}
	
	protected final AbstractClassDef  classOrAssociationRef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		AbstractClassDef def;
		
		
			def=null;
			Viewable ref;
			int line=0;
			
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				line=LT(1).getLine();
			}
			ref=viewableRef(scope);
			if ( inputState.guessing==0 ) {
				
							if(ref instanceof AbstractClassDef){
								def=(AbstractClassDef)ref;
							}else{
								reportError(formatMessage ("err_classOrAssociationRef",ref.getScopedName(null)),line);
							}
						
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_38);
			} else {
			  throw ex;
			}
		}
		return def;
	}
	
	protected final Viewable  viewableRef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		Viewable found;
		
		
		List      nams = new LinkedList();
		int lin = 0;
		found = null;
		String elementName = null;
		ch.interlis.ili2c.metamodel.Element elt;
		
		
		try {      // for error handling
			lin=names2(nams);
			if ( inputState.guessing==0 ) {
				
				Container container = null;
				
				switch (nams.size()) {
				case 1:
				/* VIEWorTABLE */
				elementName = (String) nams.get(0);
				container = scope.getContainerOrSame(AbstractPatternDef.class);
				if (container == null)
				container = scope.getContainerOrSame(Model.class);
				break;
				
				case 2:
				/* MODEL.VIEWorTABLE */
				container = resolveOrFixModelName(scope, (String) nams.get(0), lin);
				elementName = (String) nams.get(1);
				break;
				
				case 3:
				/* MODEL.TOPIC.VIEWorTABLE */
				container = resolveOrFixTopicName(
				resolveOrFixModelName(scope, (String) nams.get(0), lin),
				(String) nams.get(1),
				lin);
				elementName = (String) nams.get(2);
				break;
				
				default:
				reportError(rsrc.getString("err_weirdViewOrTableRef"), lin);
				panic();
				break;
				}
				
				elt = (Viewable) container.getRealElement(Viewable.class, elementName);
				if ((elt == null) && (nams.size() == 1)){
					// unqualified name; search also in unqaulified imported models
				Model model = (Model) scope.getContainerOrSame(Model.class);
				elt = (Viewable) model.getImportedElement (Viewable.class, elementName);
				}
				if (elt == null) {
				reportError(
				formatMessage("err_noSuchViewOrTable", elementName, container.toString()),
				lin);
				panic();
				}
				found = (Viewable) elt;
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_39);
			} else {
			  throw ex;
			}
		}
		return found;
	}
	
	protected final CompositionType  restrictedStructureRef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		CompositionType ct;
		
		
				Table ref=null;
				Table restrictedTo;
				ct=null;
				int line=0;
			
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				line=LT(1).getLine();
			}
			{
			if ((LA(1)==LITERAL_ANYSTRUCTURE)) {
				match(LITERAL_ANYSTRUCTURE);
				if ( inputState.guessing==0 ) {
					
							ref=modelInterlis.ANYSTRUCTURE;
							
				}
			}
			else if ((_tokenSet_7.member(LA(1)))) {
				ref=structureRef(scope);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
								ct=new CompositionType();
								try{
									ct.setSourceLine(line);
									ct.setComponentType(ref);
								}catch(Exception ex){
									reportError(ex, line);
								}
							
			}
			{
			if ((LA(1)==LITERAL_RESTRICTION)) {
				match(LITERAL_RESTRICTION);
				match(LPAREN);
				restrictedTo=structureRef(scope);
				if ( inputState.guessing==0 ) {
					ct.addRestrictedTo(restrictedTo);
				}
				{
				_loop597:
				do {
					if ((LA(1)==SEMI)) {
						match(SEMI);
						restrictedTo=structureRef(scope);
						if ( inputState.guessing==0 ) {
							ct.addRestrictedTo(restrictedTo);
						}
					}
					else {
						break _loop597;
					}
					
				} while (true);
				}
				match(RPAREN);
			}
			else if ((LA(1)==EOF)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		return ct;
	}
	
	protected final AssociationDef  associationRef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		AssociationDef ref;
		
		ref=null;
			Viewable t;
			int refline=0;
			
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				refline=LT(1).getLine();
			}
			t=viewableRef(scope);
			if ( inputState.guessing==0 ) {
				
							if(t instanceof AssociationDef){
								ref=(AssociationDef)t;
							}else{
								reportError(formatMessage ("err_assocref_notAnAssoc",t.getScopedName(null)),refline);
							}
						
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_40);
			} else {
			  throw ex;
			}
		}
		return ref;
	}
	
	protected final ViewableAlias  renamedViewableRef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		ViewableAlias found;
		
		Token  n = null;
		
				String aliasName=null;
				Viewable aliasFor=null;
				found=null;
			
		
		try {      // for error handling
			{
			boolean synPredMatched919 = false;
			if (((LA(1)==NAME))) {
				int _m919 = mark();
				synPredMatched919 = true;
				inputState.guessing++;
				try {
					{
					match(NAME);
					match(TILDE);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched919 = false;
				}
				rewind(_m919);
inputState.guessing--;
			}
			if ( synPredMatched919 ) {
				n = LT(1);
				match(NAME);
				match(TILDE);
				if ( inputState.guessing==0 ) {
					aliasName=n.getText();
				}
			}
			else if ((_tokenSet_7.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			aliasFor=viewableRefDepReq(scope);
			if ( inputState.guessing==0 ) {
				
							found=new ViewableAlias(aliasName,aliasFor);
						
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_41);
			} else {
			  throw ex;
			}
		}
		return found;
	}
	
	protected final void roleDefs(
		AssociationDef container
	) throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			boolean synPredMatched614 = false;
			if (((LA(1)==NAME))) {
				int _m614 = mark();
				synPredMatched614 = true;
				inputState.guessing++;
				try {
					{
					match(NAME);
					properties(ch.interlis.ili2c.metamodel.Properties.eABSTRACT
			|ch.interlis.ili2c.metamodel.Properties.eEXTENDED
			|ch.interlis.ili2c.metamodel.Properties.eFINAL
			|ch.interlis.ili2c.metamodel.Properties.eHIDING
			|ch.interlis.ili2c.metamodel.Properties.eORDERED
			|ch.interlis.ili2c.metamodel.Properties.eEXTERNAL
			);
					{
					switch ( LA(1)) {
					case ASSOCIATE:
					{
						match(ASSOCIATE);
						break;
					}
					case AGGREGATE:
					{
						match(AGGREGATE);
						break;
					}
					case COMPOSITE:
					{
						match(COMPOSITE);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					}
				}
				catch (RecognitionException pe) {
					synPredMatched614 = false;
				}
				rewind(_m614);
inputState.guessing--;
			}
			if ( synPredMatched614 ) {
				roleDef(container);
				roleDefs(container);
			}
			else if ((_tokenSet_20.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
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
	
	protected final void roleDef(
		AssociationDef container
	) throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		Token  col = null;
		
				Cardinality card=null;
				int mods;
				ReferenceType ref=null;
				Evaluable obj=null;
				RoleDef def=new RoleDef(true);
				int kind=0;
				boolean external=false;
			  String ilidoc=null;
			  Settings metaValues=null;
			
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			n = LT(1);
			match(NAME);
			if ( inputState.guessing==0 ) {
				def.setSourceLine(n.getLine());
			}
			mods=properties(ch.interlis.ili2c.metamodel.Properties.eABSTRACT
		|ch.interlis.ili2c.metamodel.Properties.eEXTENDED
		|ch.interlis.ili2c.metamodel.Properties.eFINAL
		|ch.interlis.ili2c.metamodel.Properties.eHIDING
		|ch.interlis.ili2c.metamodel.Properties.eORDERED
		|ch.interlis.ili2c.metamodel.Properties.eEXTERNAL
		);
			{
			switch ( LA(1)) {
			case ASSOCIATE:
			{
				match(ASSOCIATE);
				if ( inputState.guessing==0 ) {
					kind=RoleDef.Kind.eASSOCIATE;
				}
				break;
			}
			case AGGREGATE:
			{
				match(AGGREGATE);
				if ( inputState.guessing==0 ) {
					kind=RoleDef.Kind.eAGGREGATE;
				}
				break;
			}
			case COMPOSITE:
			{
				match(COMPOSITE);
				if ( inputState.guessing==0 ) {
					kind=RoleDef.Kind.eCOMPOSITE;
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			if ((LA(1)==LCURLY)) {
				card=cardinality();
			}
			else if ((_tokenSet_42.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			ref=restrictedClassOrAssRef(container);
			if ( inputState.guessing==0 ) {
				
						try {
							
							external=(mods & ch.interlis.ili2c.metamodel.Properties.eEXTERNAL)!=0;
							Topic targetTopic=(Topic)ref.getReferred().getContainerOrSame(Topic.class);
							Topic thisTopic=(Topic)container.getContainerOrSame(Topic.class);
							// target in a topic and targets topic not a base of this topic 
							if(targetTopic!=null && thisTopic!=null && !thisTopic.isExtending(targetTopic)){
								if(!external){
									// must be external
									reportError(formatMessage ("err_role_externalreq",""),n.getLine());
								}else{
								  if(targetTopic!=thisTopic){
								    if(!thisTopic.isDependentOn(targetTopic)){
								      reportError(formatMessage ("err_viewableref_topicdepreq",
									thisTopic.getName(),
									targetTopic.getName()),n.getLine());
								    }
								  }
								}
							}
							ref.setExternal(external);
						  def.setName(n.getText());
						  def.setDocumentation(ilidoc);
						  def.setMetaValues(metaValues);
						  def.setExtended((mods & ch.interlis.ili2c.metamodel.Properties.eEXTENDED) != 0);
						  def.setAbstract((mods & ch.interlis.ili2c.metamodel.Properties.eABSTRACT) != 0);
						  def.setFinal((mods & ch.interlis.ili2c.metamodel.Properties.eFINAL) != 0);
						  def.setHiding((mods & ch.interlis.ili2c.metamodel.Properties.eHIDING) != 0);
						  def.setOrdered((mods & ch.interlis.ili2c.metamodel.Properties.eORDERED) != 0);
						  def.setKind(kind);
						  if(card!=null){
						  	if(kind==RoleDef.Kind.eCOMPOSITE
								&& card.getMaximum()>1){
				reportError(formatMessage ("err_role_maxcard1",n.getText())
							      	,n.getLine());
							}
						  	def.setCardinality(card);
						  }
						if((ref.getReferred() instanceof Table) && !((Table)ref.getReferred()).isIdentifiable()){
							reportError(formatMessage("err_role_toStruct",n.getText()),n.getLine());
						}
						  def.addReference(ref);
						  container.add(def);
						} catch (Exception ex) {
						  reportError(ex, n.getLine());
						}
					
			}
			{
			_loop620:
			do {
				if ((LA(1)==LITERAL_OR)) {
					match(LITERAL_OR);
					ref=restrictedClassOrAssRef(container);
					if ( inputState.guessing==0 ) {
						
									Topic targetTopic=(Topic)ref.getReferred().getContainerOrSame(Topic.class);
									Topic thisTopic=(Topic)container.getContainerOrSame(Topic.class);
									// target in a topic and targets topic not a base of this topic 
									if(targetTopic!=null && thisTopic!=null && !thisTopic.isExtending(targetTopic)){
										if(!external){
											// must be external
											reportError(formatMessage ("err_role_externalreq",""),n.getLine());
										}else{
										  if(targetTopic!=thisTopic){
										    if(!thisTopic.isDependentOn(targetTopic)){
										      reportError(formatMessage ("err_viewableref_topicdepreq",
											thisTopic.getName(),
											targetTopic.getName()),n.getLine());
										    }
										  }
										}
									}
									ref.setExternal(external);
									if((ref.getReferred() instanceof Table) && !((Table)ref.getReferred()).isIdentifiable()){
										reportError(formatMessage("err_role_toStruct",n.getText()),n.getLine());
									}
									def.addReference(ref);
								
								
					}
				}
				else {
					break _loop620;
				}
				
			} while (true);
			}
			{
			if ((LA(1)==COLONEQUALS)) {
				col = LT(1);
				match(COLONEQUALS);
				obj=factor(container,container);
				if ( inputState.guessing==0 ) {
					
							  	if(!(obj instanceof ObjectPath)){
					reportError(formatMessage ("err_role_factorNotAnObjectPath","")
								      	,col.getLine());
					
								}
							  	def.setDerivedFrom((ObjectPath)obj);
						
				}
			}
			else if ((LA(1)==SEMI)) {
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
				recover(ex,_tokenSet_20);
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
				recover(ex,_tokenSet_43);
			} else {
			  throw ex;
			}
		}
		return i;
	}
	
	protected final void domainDef(
		Container container
	) throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		Token  eq = null;
		
			  Domain     extending = null;
			  Type       extendingType = null;
			  Type       declared = null;
			  int        mods = 0;
			  String ilidoc=null;
			  Settings metaValues=null;
			
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			n = LT(1);
			match(NAME);
			mods=properties(ch.interlis.ili2c.metamodel.Properties.eABSTRACT|ch.interlis.ili2c.metamodel.Properties.eFINAL|ch.interlis.ili2c.metamodel.Properties.eGENERIC);
			{
			if ((LA(1)==LITERAL_EXTENDS)) {
				match(LITERAL_EXTENDS);
				extending=domainRef(container);
				if ( inputState.guessing==0 ) {
					
								if (extending != null)
								  extendingType = extending.getType();
							
				}
			}
			else if ((LA(1)==EQUALS)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			eq = LT(1);
			match(EQUALS);
			{
			if ((LA(1)==LITERAL_MANDATORY)) {
				match(LITERAL_MANDATORY);
				{
				if ((_tokenSet_44.member(LA(1)))) {
					declared=type(container,extendingType,null,(mods&ch.interlis.ili2c.metamodel.Properties.eGENERIC)!=0);
				}
				else if ((LA(1)==SEMI||LA(1)==LITERAL_CONSTRAINTS)) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
			}
			else if ((_tokenSet_44.member(LA(1)))) {
				declared=type(container,extendingType,null,(mods&ch.interlis.ili2c.metamodel.Properties.eGENERIC)!=0);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((LA(1)==LITERAL_CONSTRAINTS)) {
				match(LITERAL_CONSTRAINTS);
				match(NAME);
				match(COLON);
				expression(container,predefinedBooleanType,container);
				{
				_loop632:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						match(NAME);
						match(COLON);
						expression(container,predefinedBooleanType,container);
					}
					else {
						break _loop632;
					}
					
				} while (true);
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
				
						Domain dd = new Domain ();
				
						try {
						dd.setName (n.getText());
						dd.setDocumentation(ilidoc);
						dd.setMetaValues(metaValues);
						dd.setSourceLine(n.getLine());
						try {
						  if ((mods & ch.interlis.ili2c.metamodel.Properties.eABSTRACT) != 0)
						    dd.setAbstract (true);
						} catch (Exception ex) {
						  reportError (ex, n.getLine());
						}
				
						try {
						  if ((mods & ch.interlis.ili2c.metamodel.Properties.eFINAL) != 0)
						    dd.setFinal (true);
						} catch (Exception ex) {
						  reportError (ex, n.getLine());
						}
				
						try {
						  if (declared != null)
						    dd.setType (declared);
						} catch (Exception ex) {
						  reportError (ex, n.getLine());
						}
				
						try {
						  if ((declared != null)
						      && !declared.isMandatory()
						      && (extendingType != null)
						      && extendingType.isMandatory())
						  {
						    reportError(
							formatMessage("err_extendingMandatoryDomain",
								      dd.toString (), extending.toString ()),
							eq.getLine());
						    declared.setMandatory(true);
						  }
				
						  if(declared!=null && declared instanceof EnumerationType){
							try {
							  ((EnumerationType)declared).checkTypeExtension(extendingType,false);
							} catch (Exception ex) {
							  reportError (ex, n.getLine());
							}
						  }
						  dd.setExtending (extending);
						} catch (Exception ex) {
						  reportError(ex, n.getLine());
						}
				
						container.add (dd);
						} catch (Exception ex) {
						reportError(ex, n.getLine());
						}
						
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_45);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final Evaluable  expression(
		Container ns, Type expectedType,Container functionNs
	) throws RecognitionException, TokenStreamException {
		Evaluable expr;
		
		
			expr = null;
			
		
		try {      // for error handling
			expr=term(ns,expectedType, functionNs);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_46);
			} else {
			  throw ex;
			}
		}
		return expr;
	}
	
	protected final Type  baseType(
		Container scope, Type extending,ArrayList formalArgs,boolean isGeneric
	) throws RecognitionException, TokenStreamException {
		Type bt;
		
		
				bt = null;
			
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_URI:
			case LITERAL_NAME:
			case LITERAL_MTEXT:
			case LITERAL_TEXT:
			{
				bt=textType(extending);
				break;
			}
			case LPAREN:
			{
				bt=enumerationType(extending);
				break;
			}
			case LITERAL_ALL:
			{
				bt=enumTreeValueType(scope,extending);
				break;
			}
			case LITERAL_HALIGNMENT:
			case LITERAL_VALIGNMENT:
			{
				bt=alignmentType();
				break;
			}
			case LITERAL_BOOLEAN:
			{
				bt=booleanType();
				break;
			}
			case LITERAL_NUMERIC:
			case DEC:
			case POSINT:
			case NUMBER:
			{
				bt=numericType(scope,extending,false);
				break;
			}
			case STRING:
			case LITERAL_FORMAT:
			{
				bt=formattedType(scope,extending);
				break;
			}
			case LITERAL_DATE:
			case LITERAL_TIMEOFDAY:
			case LITERAL_DATETIME:
			{
				bt=dateTimeType(scope,extending);
				break;
			}
			case LITERAL_COORD:
			case LITERAL_MULTICOORD:
			{
				bt=coordinateType(scope,extending,isGeneric);
				break;
			}
			case LITERAL_OID:
			{
				bt=oIDType(scope,extending);
				break;
			}
			case LITERAL_BLACKBOX:
			{
				bt=blackboxType(scope,extending);
				break;
			}
			case LITERAL_CLASS:
			case LITERAL_STRUCTURE:
			{
				bt=classType(scope,extending);
				break;
			}
			case LITERAL_ATTRIBUTE:
			{
				bt=attributePathType(scope,extending,formalArgs);
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
				recover(ex,_tokenSet_36);
			} else {
			  throw ex;
			}
		}
		return bt;
	}
	
	protected final LineType  lineType(
		Container scope, Type extending
	) throws RecognitionException, TokenStreamException {
		LineType lt;
		
		Token  pl = null;
		Token  mpl = null;
		Token  surf = null;
		Token  msurf = null;
		Token  area = null;
		Token  marea = null;
		
		boolean directed = false;
		boolean withStraights = false;
		boolean withArcs = false;
		LineForm[] theLineForms = null;
		PrecisionDecimal theMaxOverlap = null;
		Domain controlPointDomain = null;
		int line = 0;
		lt = null;
		
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_DIRECTED:
			case LITERAL_POLYLINE:
			case LITERAL_MULTIPOLYLINE:
			{
				{
				if ((LA(1)==LITERAL_DIRECTED)) {
					match(LITERAL_DIRECTED);
					if ( inputState.guessing==0 ) {
						directed = true;
					}
				}
				else if ((LA(1)==LITERAL_POLYLINE||LA(1)==LITERAL_MULTIPOLYLINE)) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				{
				if ((LA(1)==LITERAL_POLYLINE)) {
					pl = LT(1);
					match(LITERAL_POLYLINE);
					if ( inputState.guessing==0 ) {
						line = pl.getLine();
					}
				}
				else if ((LA(1)==LITERAL_MULTIPOLYLINE)) {
					mpl = LT(1);
					match(LITERAL_MULTIPOLYLINE);
					if ( inputState.guessing==0 ) {
						line = mpl.getLine();
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				if ( inputState.guessing==0 ) {
					
						if(pl!=null){
							lt = new PolylineType ();
							try {
							  ((PolylineType) lt).setDirected (directed);
							} catch (Exception ex) {
							  reportError (ex, line);
							}
						}else{
							lt = new MultiPolylineType ();
							try {
							  ((MultiPolylineType) lt).setDirected (directed);
							} catch (Exception ex) {
							  reportError (ex, line);
							}
						}
					
				}
				break;
			}
			case LITERAL_SURFACE:
			{
				surf = LT(1);
				match(LITERAL_SURFACE);
				if ( inputState.guessing==0 ) {
					line = surf.getLine(); lt = new SurfaceType();
				}
				break;
			}
			case LITERAL_MULTISURFACE:
			{
				msurf = LT(1);
				match(LITERAL_MULTISURFACE);
				if ( inputState.guessing==0 ) {
					line = msurf.getLine(); lt = new MultiSurfaceType();
				}
				break;
			}
			case LITERAL_AREA:
			{
				area = LT(1);
				match(LITERAL_AREA);
				if ( inputState.guessing==0 ) {
					line = area.getLine(); lt = new AreaType();
				}
				break;
			}
			case LITERAL_MULTIAREA:
			{
				marea = LT(1);
				match(LITERAL_MULTIAREA);
				if ( inputState.guessing==0 ) {
					line = marea.getLine(); lt = new MultiAreaType();
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			if ((LA(1)==LITERAL_WITH)) {
				theLineForms=lineForm(scope);
			}
			else if ((_tokenSet_47.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((LA(1)==LITERAL_VERTEX)) {
				match(LITERAL_VERTEX);
				controlPointDomain=domainRef(scope);
			}
			else if ((_tokenSet_48.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((LA(1)==LITERAL_WITHOUT)) {
				match(LITERAL_WITHOUT);
				match(LITERAL_OVERLAPS);
				match(GREATER);
				theMaxOverlap=decimal();
			}
			else if ((_tokenSet_36.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
				
				try {
				if (theLineForms != null)
				lt.setLineForms (theLineForms);
				} catch (Exception ex) {
				reportError (ex, line);
				}
				
				try {
				if(controlPointDomain!=null){
				lt.setControlPointDomain (controlPointDomain);
				}
				} catch (Exception ex) {
				reportError (ex, line);
				}
				
				try {
				if(theMaxOverlap!=null){
				lt.setMaxOverlap (theMaxOverlap);
				}
				} catch (Exception ex) {
				reportError (ex, line);
				}
				
			}
			if ( inputState.guessing==0 ) {
				
				if(extending!=null){
				try{
				lt.setExtending(extending);
				}catch(Exception ex){
				reportError (ex, line);
				}
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_36);
			} else {
			  throw ex;
			}
		}
		return lt;
	}
	
	protected final Type  textType(
		Type extending
	) throws RecognitionException, TokenStreamException {
		Type tt;
		
		Token  ur = null;
		Token  nm = null;
		Token  star = null;
		
				tt = null;
				int i = -1;
				boolean normalizedText=false;
			
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_URI:
			{
				ur = LT(1);
				match(LITERAL_URI);
				if ( inputState.guessing==0 ) {
					
					tt = new TypeAlias ();
					try {
					((TypeAlias) tt).setAliasing (modelInterlis.URI);
					} catch (Exception ex) {
					reportInternalError (ex, ur.getLine());
					}
					
				}
				break;
			}
			case LITERAL_NAME:
			{
				nm = LT(1);
				match(LITERAL_NAME);
				if ( inputState.guessing==0 ) {
					
					tt = new TypeAlias ();
					try {
					((TypeAlias) tt).setAliasing (modelInterlis.NAME);
					} catch (Exception ex) {
					reportInternalError (ex, nm.getLine());
					}
					
				}
				break;
			}
			case LITERAL_MTEXT:
			case LITERAL_TEXT:
			{
				{
				if ((LA(1)==LITERAL_MTEXT)) {
					match(LITERAL_MTEXT);
				}
				else if ((LA(1)==LITERAL_TEXT)) {
					match(LITERAL_TEXT);
					if ( inputState.guessing==0 ) {
						normalizedText=true;
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				{
				if ((LA(1)==STAR)) {
					{
					star = LT(1);
					match(STAR);
					i=posInteger();
					if ( inputState.guessing==0 ) {
						
						try {
						tt = new TextType(i);
							    ((TextType)tt).setNormalized(normalizedText);
						} catch (Exception ex) {
						reportError (ex, star.getLine());
						}
						
					}
					}
				}
				else if ((_tokenSet_36.member(LA(1)))) {
					if ( inputState.guessing==0 ) {
						
						tt = new TextType ();
						((TextType)tt).setNormalized(normalizedText);
						
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
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
				recover(ex,_tokenSet_36);
			} else {
			  throw ex;
			}
		}
		return tt;
	}
	
	protected final EnumerationType  enumerationType(
		Type extending
	) throws RecognitionException, TokenStreamException {
		EnumerationType et;
		
		Token  ord = null;
		Token  circ = null;
		
		et = new EnumerationType();
		ch.interlis.ili2c.metamodel.Enumeration enumer;
		
		
		try {      // for error handling
			enumer=enumeration(extending);
			if ( inputState.guessing==0 ) {
				
				try {
				et.setEnumeration(enumer);
				} catch (Exception ex) {
				reportError(ex, 0);
				}
				
			}
			{
			switch ( LA(1)) {
			case LITERAL_ORDERED:
			{
				ord = LT(1);
				match(LITERAL_ORDERED);
				if ( inputState.guessing==0 ) {
					
					try {
					et.setOrdered(true);
					} catch (Exception ex) {
					reportError(ex, ord.getLine());
					}
					
				}
				break;
			}
			case LITERAL_CIRCULAR:
			{
				circ = LT(1);
				match(LITERAL_CIRCULAR);
				if ( inputState.guessing==0 ) {
					
					try {
					et.setCircular(true);
					} catch (Exception ex) {
					reportError(ex, circ.getLine());
					}
					
				}
				break;
			}
			case SEMI:
			case RPAREN:
			case EXPLANATION:
			case COLONEQUALS:
			case LITERAL_CONSTRAINTS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_36);
			} else {
			  throw ex;
			}
		}
		return et;
	}
	
	protected final EnumTreeValueType  enumTreeValueType(
		Container scope, Type extending
	) throws RecognitionException, TokenStreamException {
		EnumTreeValueType et;
		
		
		Domain ref=null;
		et=null;
		
		
		try {      // for error handling
			match(LITERAL_ALL);
			match(LITERAL_OF);
			ref=domainRef(scope);
			if ( inputState.guessing==0 ) {
				/* TODO check that enumType */
					et=new EnumTreeValueType();
					et.setEnumType(ref);
					return et;
					
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_36);
			} else {
			  throw ex;
			}
		}
		return et;
	}
	
	protected final Type  alignmentType() throws RecognitionException, TokenStreamException {
		Type tt;
		
		Token  h = null;
		Token  v = null;
		
		tt = null;
		
		
		try {      // for error handling
			if ((LA(1)==LITERAL_HALIGNMENT)) {
				h = LT(1);
				match(LITERAL_HALIGNMENT);
				if ( inputState.guessing==0 ) {
					
					tt = new TypeAlias ();
					try {
					((TypeAlias) tt).setAliasing (modelInterlis.HALIGNMENT);
					} catch (Exception ex) {
					reportInternalError (ex, h.getLine());
					}
					
				}
			}
			else if ((LA(1)==LITERAL_VALIGNMENT)) {
				v = LT(1);
				match(LITERAL_VALIGNMENT);
				if ( inputState.guessing==0 ) {
					
					tt = new TypeAlias ();
					try {
					((TypeAlias) tt).setAliasing (modelInterlis.VALIGNMENT);
					} catch (Exception ex) {
					reportInternalError (ex, v.getLine());
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
				recover(ex,_tokenSet_36);
			} else {
			  throw ex;
			}
		}
		return tt;
	}
	
	protected final Type  booleanType() throws RecognitionException, TokenStreamException {
		Type tt;
		
		Token  b = null;
		
		tt = null;
		
		
		try {      // for error handling
			b = LT(1);
			match(LITERAL_BOOLEAN);
			if ( inputState.guessing==0 ) {
				
				tt = new TypeAlias ();
				try {
				((TypeAlias) tt).setAliasing (modelInterlis.BOOLEAN);
				} catch (Exception ex) {
				reportInternalError (ex, b.getLine());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_36);
			} else {
			  throw ex;
			}
		}
		return tt;
	}
	
	protected final NumericType  numericType(
		Container scope, Type extending,boolean isCoord
	) throws RecognitionException, TokenStreamException {
		NumericType ntyp;
		
		Token  dots = null;
		Token  numer = null;
		Token  circ = null;
		Token  lbrac = null;
		Token  cw = null;
		Token  ccw = null;
		
		PrecisionDecimal min = null, max = null;
		int rotation = 0;
		int rotationLine = 0;
		ntyp = null;
		Unit u = null;
		RefSystemRef referenceSystem = null;
		int line = 0;
		
		
		try {      // for error handling
			{
			if (((LA(1) >= DEC && LA(1) <= NUMBER))) {
				min=decimal();
				dots = LT(1);
				match(DOTDOT);
				max=decimal();
				if ( inputState.guessing==0 ) {
					
					line = rotationLine = dots.getLine();
					try {
					ntyp = new NumericType(min, max);
					} catch (Exception ex) {
					reportError(ex, dots.getLine());
					ntyp = new NumericType();
					}
					
				}
			}
			else if ((LA(1)==LITERAL_NUMERIC)) {
				numer = LT(1);
				match(LITERAL_NUMERIC);
				if ( inputState.guessing==0 ) {
					
					ntyp = new NumericType();
					line = rotationLine = numer.getLine();
					
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((LA(1)==LITERAL_CIRCULAR)) {
				circ = LT(1);
				match(LITERAL_CIRCULAR);
				if ( inputState.guessing==0 ) {
					
					try {
					ntyp.setCircular(true);
					} catch (Exception ex) {
					reportError(ex, circ.getLine());
					}
					
				}
			}
			else if ((_tokenSet_49.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((LA(1)==LBRACE)) {
				lbrac = LT(1);
				match(LBRACE);
				u=unitRef(scope);
				match(RBRACE);
			}
			else if ((_tokenSet_50.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			switch ( LA(1)) {
			case LITERAL_CLOCKWISE:
			{
				cw = LT(1);
				match(LITERAL_CLOCKWISE);
				if ( inputState.guessing==0 ) {
					
					rotation = NumericalType.ROTATION_CLOCKWISE;
					rotationLine = cw.getLine();
					
				}
				break;
			}
			case LITERAL_COUNTERCLOCKWISE:
			{
				ccw = LT(1);
				match(LITERAL_COUNTERCLOCKWISE);
				if ( inputState.guessing==0 ) {
					
					rotation = NumericalType.ROTATION_COUNTERCLOCKWISE;
					rotationLine = ccw.getLine();
					
				}
				break;
			}
			case LCURLY:
			case LESS:
			{
				referenceSystem=refSys(scope,isCoord);
				break;
			}
			case SEMI:
			case RPAREN:
			case EXPLANATION:
			case COMMA:
			case COLONEQUALS:
			case LITERAL_CONSTRAINTS:
			case LITERAL_REFSYS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				
				try {
				if ((u == null) && (extending instanceof NumericType))
				u = ((NumericType) extending).getUnit ();
				
				ntyp.setUnit(u);
				} catch (Exception ex) {
				reportError(ex, lbrac.getLine());
				}
				
				try {
				if (ntyp != null)
				ntyp.setRotation(rotation);
				} catch (Exception ex) {
				reportError(ex, rotationLine);
				}
				
				try {
				if ((referenceSystem == null) && (extending instanceof NumericType))
				referenceSystem = ((NumericType) extending).getReferenceSystem ();
				
				ntyp.setReferenceSystem (referenceSystem);
				} catch (Exception ex) {
				reportError(ex, line);
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_51);
			} else {
			  throw ex;
			}
		}
		return ntyp;
	}
	
	protected final FormattedType  formattedType(
		Container scope, Type extending
	) throws RecognitionException, TokenStreamException {
		FormattedType ft;
		
		Token  format_kw = null;
		Token  prefix = null;
		Token  postfix = null;
		Token  min = null;
		Token  max = null;
		Token  min2 = null;
		Token  max2 = null;
		Token  min3 = null;
		Token  max3 = null;
		
				ft=new FormattedType();
				Domain domain=null;
				Table struct=null;
				FormattedTypeBaseAttrRef baseAttr=null;
				int line=0;
			
		
		try {      // for error handling
			{
			boolean synPredMatched677 = false;
			if (((LA(1)==LITERAL_FORMAT))) {
				int _m677 = mark();
				synPredMatched677 = true;
				inputState.guessing++;
				try {
					{
					match(LITERAL_FORMAT);
					match(LITERAL_BASED);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched677 = false;
				}
				rewind(_m677);
inputState.guessing--;
			}
			if ( synPredMatched677 ) {
				{
				format_kw = LT(1);
				match(LITERAL_FORMAT);
				match(LITERAL_BASED);
				match(LITERAL_ON);
				struct=structureRef(scope);
				if ( inputState.guessing==0 ) {
					
								line=format_kw.getLine();
								ft=new FormattedType();
								if(extending!=null){
									try{
									ft.setExtending(extending);
									}catch(Exception ex){
										reportError(ex,line);
									}
								}
								ft.setBaseStruct(struct);
							
				}
				match(LPAREN);
				{
				if ((LA(1)==LITERAL_INHERITANCE)) {
					match(LITERAL_INHERITANCE);
				}
				else if ((LA(1)==NAME||LA(1)==RPAREN||LA(1)==STRING)) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				{
				if ((LA(1)==STRING)) {
					prefix = LT(1);
					match(STRING);
					if ( inputState.guessing==0 ) {
						
										if(prefix!=null){
											ft.setPrefix(prefix.getText());
										}
							
					}
				}
				else if ((LA(1)==NAME||LA(1)==RPAREN)) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				{
				_loop683:
				do {
					if ((LA(1)==NAME)) {
						baseAttr=baseAttrRef(ft,struct);
						if ( inputState.guessing==0 ) {
							postfix=null;
						}
						{
						if ((LA(1)==STRING)) {
							postfix = LT(1);
							match(STRING);
						}
						else if ((LA(1)==NAME||LA(1)==RPAREN)) {
						}
						else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						
						}
						if ( inputState.guessing==0 ) {
								if(postfix!=null){
													baseAttr.setPostfix(postfix.getText());
												}
												ft.addBaseAttrRef(baseAttr);
											
						}
					}
					else {
						break _loop683;
					}
					
				} while (true);
				}
				match(RPAREN);
				{
				if ((LA(1)==STRING)) {
					min = LT(1);
					match(STRING);
					match(DOTDOT);
					max = LT(1);
					match(STRING);
				}
				else if ((_tokenSet_36.member(LA(1)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				if ( inputState.guessing==0 ) {
					
								if(min!=null){
									validateFormattedConst(ft,min.getText(),min.getLine());
									validateFormattedConst(ft,max.getText(),max.getLine());
									ft.setMinimum(min.getText());
									ft.setMaximum(max.getText());
								}
							
				}
				}
			}
			else if ((LA(1)==STRING)) {
				{
				min2 = LT(1);
				match(STRING);
				match(DOTDOT);
				max2 = LT(1);
				match(STRING);
				if ( inputState.guessing==0 ) {
					
								line=min2.getLine();
								ft=new FormattedType();
								if(extending!=null){
									//EhiLogger.debug("extending baseStruct "+((FormattedType)extending).getBaseStruct().getScopedName(null));
									//EhiLogger.debug("extending format "+((FormattedType)extending).getFormat());
									try{
									ft.setExtending(extending);
									}catch(Exception ex){
										reportError(ex,line);
									}
								}
								validateFormattedConst(ft,min2.getText(),min2.getLine());
								validateFormattedConst(ft,max2.getText(),max2.getLine());
								ft.setMinimum(min2.getText());
								ft.setMaximum(max2.getText());
							
				}
				}
			}
			else if ((LA(1)==LITERAL_FORMAT)) {
				{
				match(LITERAL_FORMAT);
				domain=domainRef(scope);
				min3 = LT(1);
				match(STRING);
				match(DOTDOT);
				max3 = LT(1);
				match(STRING);
				if ( inputState.guessing==0 ) {
					
								line=min3.getLine();
								ft=new FormattedType();
								if(extending!=null){
									try{
									ft.setExtending(extending);
									}catch(Exception ex){
										reportError(ex,line);
									}
								}
								ft.setBaseDomain(domain);
								try{
									if(!ft.isValueInRange(min3.getText())){
										reportError(formatMessage("err_formattedType_valueOutOfRange",min3.getText()),min3.getLine());
									}
								}catch(NumberFormatException ex){
									reportError(formatMessage("err_formattedType_illegalFormat",min3.getText()),min3.getLine());
								}
								try{
									if(!ft.isValueInRange(max3.getText())){
										reportError(formatMessage("err_formattedType_valueOutOfRange",max3.getText()),max3.getLine());
									}
								}catch(NumberFormatException ex){
									reportError(formatMessage("err_formattedType_illegalFormat",max3.getText()),max3.getLine());
								}
								ft.setMinimum(min3.getText());
								ft.setMaximum(max3.getText());
							
				}
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_36);
			} else {
			  throw ex;
			}
		}
		return ft;
	}
	
	protected final CoordType  dateTimeType(
		Container scope, Type extending
	) throws RecognitionException, TokenStreamException {
		CoordType ct;
		
		
			ct=null;
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_DATE:
			{
				match(LITERAL_DATE);
				break;
			}
			case LITERAL_TIMEOFDAY:
			{
				match(LITERAL_TIMEOFDAY);
				break;
			}
			case LITERAL_DATETIME:
			{
				match(LITERAL_DATETIME);
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
				recover(ex,_tokenSet_36);
			} else {
			  throw ex;
			}
		}
		return ct;
	}
	
	protected final AbstractCoordType  coordinateType(
		Container scope, Type extending,boolean isGeneric
	) throws RecognitionException, TokenStreamException {
		AbstractCoordType ct;
		
		Token  coord = null;
		Token  refsysT = null;
		
		NumericalType nt1 = null;
		NumericalType nt2 = null;
		NumericalType nt3 = null;
		int[] rots = null;
		ct = null;
		boolean multiCoord=false;
		NumericalType ext_nt1 = null;
		NumericalType ext_nt2 = null;
		NumericalType ext_nt3 = null;
		String refsys=null;
		
		if (extending instanceof CoordType)
		{
		NumericalType[] ext_dimensions = ((CoordType) extending).getDimensions ();
		if (ext_dimensions.length >= 1)
		ext_nt1 = ext_dimensions [0];
		if (ext_dimensions.length >= 2)
		ext_nt2 = ext_dimensions [1];
		if (ext_dimensions.length >= 3)
		ext_nt3 = ext_dimensions [2];
		}
		
		
		try {      // for error handling
			{
			if ((LA(1)==LITERAL_COORD)) {
				coord = LT(1);
				match(LITERAL_COORD);
			}
			else if ((LA(1)==LITERAL_MULTICOORD)) {
				match(LITERAL_MULTICOORD);
				if ( inputState.guessing==0 ) {
					multiCoord=true;
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			nt1=numericType(scope, ext_nt1,true);
			{
			if ((LA(1)==COMMA)) {
				match(COMMA);
				nt2=numericType(scope, ext_nt2,true);
				{
				if ((LA(1)==COMMA)) {
					match(COMMA);
					{
					if ((LA(1)==LITERAL_ROTATION)) {
						rots=rotationDef();
					}
					else if ((_tokenSet_52.member(LA(1)))) {
						{
						nt3=numericType(scope, ext_nt3,true);
						{
						if ((LA(1)==COMMA)) {
							match(COMMA);
							rots=rotationDef();
						}
						else if ((_tokenSet_53.member(LA(1)))) {
						}
						else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						
						}
						}
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
				}
				else if ((_tokenSet_53.member(LA(1)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				{
				if ((LA(1)==LITERAL_REFSYS)) {
					match(LITERAL_REFSYS);
					refsysT = LT(1);
					match(STRING);
					if ( inputState.guessing==0 ) {
						refsys=refsysT.getText();
					}
				}
				else if ((_tokenSet_36.member(LA(1)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
			}
			else if ((_tokenSet_36.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			}
			if ( inputState.guessing==0 ) {
				
				NumericalType[] nts;
				
				if (nt3 != null)
				nts = new NumericalType[] { nt1, nt2, nt3 };
				else if (nt2 != null)
				nts = new NumericalType[] { nt1, nt2 };
				else
				nts = new NumericalType[] { nt1 };
				
				try {
				if (rots == null){
				if(multiCoord){
					          ct = new MultiCoordType (nts);
				}else{
					          ct = new CoordType (nts);
				}
				}else{
				if(multiCoord){
					          ct = new MultiCoordType (nts, rots[0], rots[1]);
				}else{
					          ct = new CoordType (nts, rots[0], rots[1]);
				}
				}
				ct.setGeneric(isGeneric);
				ct.setCrs(refsys);
				} catch (Exception ex) {
				reportError (ex, coord.getLine());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_36);
			} else {
			  throw ex;
			}
		}
		return ct;
	}
	
	protected final OIDType  oIDType(
		Container scope,Type extending
	) throws RecognitionException, TokenStreamException {
		OIDType bt;
		
		
				bt=null;
				Type t;
				NumericType nt;
				Type extendingOidType=null;
				if(extending!=null && extending instanceof OIDType){
					extendingOidType=((OIDType)extending).getOIDType();
				}
			
		
		try {      // for error handling
			match(LITERAL_OID);
			{
			switch ( LA(1)) {
			case LITERAL_ANY:
			{
				match(LITERAL_ANY);
				if ( inputState.guessing==0 ) {
					bt=new AnyOIDType();
				}
				break;
			}
			case LITERAL_URI:
			case LITERAL_NAME:
			case LITERAL_MTEXT:
			case LITERAL_TEXT:
			{
				t=textType(extendingOidType);
				if ( inputState.guessing==0 ) {
					bt=new TextOIDType(t);
				}
				break;
			}
			case LITERAL_NUMERIC:
			case DEC:
			case POSINT:
			case NUMBER:
			{
				nt=numericType(scope,extendingOidType,false);
				if ( inputState.guessing==0 ) {
					bt=new NumericOIDType(nt);
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_36);
			} else {
			  throw ex;
			}
		}
		return bt;
	}
	
	protected final BlackboxType  blackboxType(
		Container scope, Type extending
	) throws RecognitionException, TokenStreamException {
		BlackboxType sutype;
		
		
		sutype=null;
		
		
		try {      // for error handling
			match(LITERAL_BLACKBOX);
			{
			if ((LA(1)==LITERAL_XML)) {
				match(LITERAL_XML);
				if ( inputState.guessing==0 ) {
					sutype=new BlackboxType();sutype.setKind(BlackboxType.eXML);
				}
			}
			else if ((LA(1)==LITERAL_BINARY)) {
				match(LITERAL_BINARY);
				if ( inputState.guessing==0 ) {
					sutype=new BlackboxType();sutype.setKind(BlackboxType.eBINARY);
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_36);
			} else {
			  throw ex;
			}
		}
		return sutype;
	}
	
	protected final ClassType  classType(
		Container scope,Type extending
	) throws RecognitionException, TokenStreamException {
		ClassType bt;
		
		
				bt=new ClassType();
				Viewable restrictedTo;
			
		
		try {      // for error handling
			if ((LA(1)==LITERAL_CLASS)) {
				{
				match(LITERAL_CLASS);
				{
				if ((LA(1)==LITERAL_RESTRICTION)) {
					match(LITERAL_RESTRICTION);
					match(LPAREN);
					restrictedTo=classOrAssociationRef(scope);
					if ( inputState.guessing==0 ) {
						bt.addRestrictedTo(restrictedTo);
					}
					{
					_loop719:
					do {
						if ((LA(1)==SEMI)) {
							match(SEMI);
							restrictedTo=classOrAssociationRef(scope);
							if ( inputState.guessing==0 ) {
								bt.addRestrictedTo(restrictedTo);
							}
						}
						else {
							break _loop719;
						}
						
					} while (true);
					}
					match(RPAREN);
				}
				else if ((_tokenSet_36.member(LA(1)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				}
			}
			else if ((LA(1)==LITERAL_STRUCTURE)) {
				{
				match(LITERAL_STRUCTURE);
				if ( inputState.guessing==0 ) {
					bt.setStructure(true);
				}
				{
				if ((LA(1)==LITERAL_RESTRICTION)) {
					match(LITERAL_RESTRICTION);
					match(LPAREN);
					restrictedTo=structureRef(scope);
					if ( inputState.guessing==0 ) {
						bt.addRestrictedTo(restrictedTo);
					}
					{
					_loop723:
					do {
						if ((LA(1)==SEMI)) {
							match(SEMI);
							restrictedTo=structureRef(scope);
							if ( inputState.guessing==0 ) {
								bt.addRestrictedTo(restrictedTo);
							}
						}
						else {
							break _loop723;
						}
						
					} while (true);
					}
					match(RPAREN);
				}
				else if ((_tokenSet_36.member(LA(1)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
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
				recover(ex,_tokenSet_36);
			} else {
			  throw ex;
			}
		}
		return bt;
	}
	
	protected final AttributePathType  attributePathType(
		Container scope, Type extending,ArrayList formalArgs
	) throws RecognitionException, TokenStreamException {
		AttributePathType sutype;
		
		Token  of = null;
		Token  n = null;
		Token  lp = null;
		Token  semi = null;
		
			sutype=null;
			ObjectPath attrRestr=null;
			Type type=null;
			ArrayList typev=new ArrayList();
			FormalArgument argRestr=null;
		
		
		try {      // for error handling
			match(LITERAL_ATTRIBUTE);
			{
			if ((LA(1)==LITERAL_OF)) {
				of = LT(1);
				match(LITERAL_OF);
				{
				{
				if (((_tokenSet_33.member(LA(1))))&&(scope instanceof Viewable)) {
					attrRestr=attributePath((Viewable)scope,scope);
					if ( inputState.guessing==0 ) {
						
									Type attrType=null;
									try{
									  attrType=attrRestr.getType();
									}catch(Exception ex){
										reportError(ex.getLocalizedMessage(),of.getLine());
										attrRestr=null;
									}
									if(attrType!=null && !(attrType instanceof ClassType)){
										reportError(formatMessage ("err_objectPath_targetAttrNotClassType",attrRestr.toString()), of.getLine());
										attrRestr=null;
									}
								
					}
				}
				else if ((_tokenSet_54.member(LA(1)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				{
				if (((LA(1)==AT))&&(formalArgs!=null)) {
					match(AT);
					n = LT(1);
					match(NAME);
					if ( inputState.guessing==0 ) {
							
									  for(int argi=0;argi<formalArgs.size();argi++){
									  	FormalArgument arg=(FormalArgument)formalArgs.get(argi);
										  if(arg.getName().equals(n.getText())){
											 argRestr=arg;
											 break;
										  }
									  }
									if(argRestr==null){
										reportError (formatMessage ("err_function_noSuchArgWoScope", n.getText()), n.getLine());
									}
								
					}
				}
				else if ((_tokenSet_55.member(LA(1)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				}
			}
			else if ((_tokenSet_55.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((LA(1)==LITERAL_RESTRICTION)) {
				match(LITERAL_RESTRICTION);
				lp = LT(1);
				match(LPAREN);
				type=attrTypeDef(scope,/* alias ok */ true, null,
                     lp.getLine(),null);
				if ( inputState.guessing==0 ) {
					
								typev.add(type);
							
				}
				{
				_loop731:
				do {
					if ((LA(1)==SEMI)) {
						semi = LT(1);
						match(SEMI);
						type=attrTypeDef(scope,/* alias ok */ true, null,
                     semi.getLine(),null);
						if ( inputState.guessing==0 ) {
							
									     	typev.add(type);
									
						}
					}
					else {
						break _loop731;
					}
					
				} while (true);
				}
				match(RPAREN);
			}
			else if ((_tokenSet_36.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
						sutype=new AttributePathType();
						if(attrRestr!=null){
							sutype.setAttrRestriction(attrRestr);
						}else if(argRestr!=null){
							sutype.setArgRestriction(argRestr);
						}
						if(typev.size()>0){
							sutype.setTypeRestriction((Type[])typev.toArray(new Type[typev.size()]));
						}
				
					
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_36);
			} else {
			  throw ex;
			}
		}
		return sutype;
	}
	
	protected final Constant  constant(
		Container scope
	) throws RecognitionException, TokenStreamException {
		Constant c;
		
		
			  c = null;
			
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_UNDEFINED:
			{
				match(LITERAL_UNDEFINED);
				if ( inputState.guessing==0 ) {
					c = new Constant.Undefined();
				}
				break;
			}
			case LITERAL_PI:
			case LITERAL_LNBASE:
			case DEC:
			case POSINT:
			case NUMBER:
			{
				c=numericConst(scope);
				break;
			}
			case STRING:
			{
				c=textConst();
				break;
			}
			case HASH:
			{
				c=enumerationConst();
				break;
			}
			case GREATER:
			{
				c=classConst(scope);
				break;
			}
			case GREATERGREATER:
			{
				c=attributeConst(scope);
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
				recover(ex,_tokenSet_56);
			} else {
			  throw ex;
			}
		}
		return c;
	}
	
	protected final Constant  numericConst(
		Container scope
	) throws RecognitionException, TokenStreamException {
		Constant c;
		
		Unit un=null;
			PrecisionDecimal val;
			c=null;
			
		
		try {      // for error handling
			val=decConst();
			{
			if ((LA(1)==LBRACE)) {
				match(LBRACE);
				un=unitRef(scope);
				match(RBRACE);
			}
			else if ((_tokenSet_56.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
						if (un == null){
							c = new Constant.Numeric (val);
						}else{
							c = new Constant.Numeric (val, un);
						}
					
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_56);
			} else {
			  throw ex;
			}
		}
		return c;
	}
	
	protected final Constant  textConst() throws RecognitionException, TokenStreamException {
		Constant c;
		
		Token  s = null;
		
				c=null;
			
		
		try {      // for error handling
			s = LT(1);
			match(STRING);
			if ( inputState.guessing==0 ) {
				c=new Constant.Text(s.getText());
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_57);
			} else {
			  throw ex;
			}
		}
		return c;
	}
	
	protected final Constant.Enumeration  enumerationConst() throws RecognitionException, TokenStreamException {
		Constant.Enumeration c;
		
		
			List mentionedNames=new ArrayList();
			int lin=0;
			c=null;
			
		
		try {      // for error handling
			match(HASH);
			{
			if ((LA(1)==NAME)) {
				lin=enumNameList(mentionedNames);
				{
				if ((LA(1)==DOT)) {
					match(DOT);
					match(LITERAL_OTHERS);
					if ( inputState.guessing==0 ) {
						mentionedNames.add(Constant.Enumeration.OTHERS);
									
					}
				}
				else if ((_tokenSet_58.member(LA(1)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				if ( inputState.guessing==0 ) {
					
							c = new Constant.Enumeration(mentionedNames);
							
				}
			}
			else if ((LA(1)==LITERAL_OTHERS)) {
				match(LITERAL_OTHERS);
				if ( inputState.guessing==0 ) {
					
							mentionedNames.add(Constant.Enumeration.OTHERS);
							c = new Constant.Enumeration(mentionedNames);
							
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_58);
			} else {
			  throw ex;
			}
		}
		return c;
	}
	
	public final Constant  classConst(
		Container scope
	) throws RecognitionException, TokenStreamException {
		Constant c;
		
		
			c=null;
			Viewable ref=null;
			
		
		try {      // for error handling
			match(GREATER);
			ref=viewableRef(scope);
			if ( inputState.guessing==0 ) {
				
					c=new Constant.Class(ref);
					
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_56);
			} else {
			  throw ex;
			}
		}
		return c;
	}
	
	public final Constant  attributeConst(
		Container scope
	) throws RecognitionException, TokenStreamException {
		Constant c;
		
		Token  n = null;
		
			c=null;
			Viewable ref=null;
			AttributeDef attr=null;
			
		
		try {      // for error handling
			match(GREATERGREATER);
			{
			boolean synPredMatched736 = false;
			if (((_tokenSet_7.member(LA(1))))) {
				int _m736 = mark();
				synPredMatched736 = true;
				inputState.guessing++;
				try {
					{
					xyRef();
					match(POINTSTO);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched736 = false;
				}
				rewind(_m736);
inputState.guessing--;
			}
			if ( synPredMatched736 ) {
				ref=viewableRef(scope);
				match(POINTSTO);
			}
			else if ((LA(1)==NAME)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			n = LT(1);
			match(NAME);
			if ( inputState.guessing==0 ) {
				
					if(ref!=null){
						scope=ref;
					}
					// find attribute in scope
					attr=findAttribute(scope,n.getText());
					if(attr==null){
						reportError (formatMessage ("err_attributePathConst_unknownAttr", n.getText(),
						scope.toString()), n.getLine());
					}
					
					c=new Constant.AttributePath(attr);
					
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_56);
			} else {
			  throw ex;
			}
		}
		return c;
	}
	
	protected final ch.interlis.ili2c.metamodel.Enumeration  enumeration(
		Type extending
	) throws RecognitionException, TokenStreamException {
		ch.interlis.ili2c.metamodel.Enumeration enumer;
		
		Token  lp = null;
		
		List elements = new LinkedList();
		ch.interlis.ili2c.metamodel.Enumeration.Element curElement;
		boolean isFinal=false;
		enumer = null;
		
		
		try {      // for error handling
			lp = LT(1);
			match(LPAREN);
			{
			if ((LA(1)==NAME)) {
				{
				curElement=enumElement(elements,extending);
				if ( inputState.guessing==0 ) {
					elements.add(curElement);
				}
				{
				_loop653:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						curElement=enumElement(elements,extending);
						if ( inputState.guessing==0 ) {
							
								  	// new element?
								  	if(curElement!=null){
									  Iterator elei=elements.iterator();
									  while(elei.hasNext()){
										  ch.interlis.ili2c.metamodel.Enumeration.Element ele=(ch.interlis.ili2c.metamodel.Enumeration.Element)elei.next();
										  if(ele.getName().equals(curElement.getName())){
											  reportError(formatMessage("err_enumerationType_DupEle",curElement.getName()),curElement.getSourceLine());
											  break;
										  }
									  }
									  elements.add(curElement); 
								  	}else{
								  	  // extension of existing element alread done in enumElement[]
								  	}
								
						}
					}
					else {
						break _loop653;
					}
					
				} while (true);
				}
				{
				if ((LA(1)==COLON)) {
					match(COLON);
					match(LITERAL_FINAL);
					if ( inputState.guessing==0 ) {
						isFinal=true;
					}
				}
				else if ((LA(1)==RPAREN)) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				}
			}
			else if ((LA(1)==LITERAL_FINAL)) {
				match(LITERAL_FINAL);
				if ( inputState.guessing==0 ) {
					isFinal=true;
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				
				enumer = new ch.interlis.ili2c.metamodel.Enumeration(elements);
				enumer.setFinal(isFinal);
				enumer.setSourceLine(lp.getLine());
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_59);
			} else {
			  throw ex;
			}
		}
		return enumer;
	}
	
	protected final ch.interlis.ili2c.metamodel.Enumeration.Element  enumElement(
		List elements,Type extending
	) throws RecognitionException, TokenStreamException {
		ch.interlis.ili2c.metamodel.Enumeration.Element ee;
		
		
		ch.interlis.ili2c.metamodel.Enumeration subEnum = null;
		ch.interlis.ili2c.metamodel.Enumeration.Element curEnum = null;
		ee = null;
		int lineNumber = 0;
		List elt = new LinkedList();
		int siz = 0;
			  String ilidoc=null;
			  Settings metaValues=null;
		
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			lineNumber=enumNameList(elt);
			{
			if ((LA(1)==LPAREN)) {
				subEnum=enumeration(extending);
			}
			else if ((LA(1)==RPAREN||LA(1)==COMMA||LA(1)==COLON)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
				siz = elt.size();
				if ((subEnum == null) && (siz > 1)){
				reportError (rsrc.getString("err_dottedEnum"), lineNumber);
				}
				if(extending!=null){
					List eles=elements;
					ch.interlis.ili2c.metamodel.Enumeration.Element existingEle=null;
					int existingEleNameIdx=0;
					for(int i=0;i<siz;i++){
						String curPathEle=(String)elt.get(i);
						ch.interlis.ili2c.metamodel.Enumeration.Element foundEle=null;
						Iterator elei=eles.iterator();
						while(elei.hasNext()){
							  ch.interlis.ili2c.metamodel.Enumeration.Element ele=(ch.interlis.ili2c.metamodel.Enumeration.Element)elei.next();
							  if(ele.getName().equals(curPathEle)){
							  	foundEle=ele;
							  	break;
							  }
						}
						if(foundEle!=null){
							existingEleNameIdx=i;
							existingEle=foundEle;
							if(foundEle.getSubEnumeration()!=null){
								eles=new ArrayList<ch.interlis.ili2c.metamodel.Enumeration.Element>();
								for(Iterator<ch.interlis.ili2c.metamodel.Enumeration.Element> e=foundEle.getSubEnumeration().getElements();e.hasNext();){
									eles.add(e.next());
								}
								continue;
							}
						}
						break;
					}
					// new path?
					if(existingEle==null){
					      for (int i = siz - 1; i >= 0; i--)
					      {
						// last path element?
						if (i == siz - 1)
						{
						  if (subEnum == null)
						  {
						    // new leaf
						    ee = new ch.interlis.ili2c.metamodel.Enumeration.Element (
						       (String) elt.get(i));
						    ee.setDocumentation(ilidoc);
						    ee.setMetaValues(metaValues);
						    ee.setSourceLine(lineNumber);
						  }
						  else
						  {
						    // new subtree
						    ee = new ch.interlis.ili2c.metamodel.Enumeration.Element (
						       (String) elt.get(i),
						       subEnum);
						    ee.setDocumentation(ilidoc);
						    ee.setMetaValues(metaValues);
						    ee.setSourceLine(lineNumber);
						  }
						}
						else
						{
						  // not last path element
						  List subEe=new ArrayList();
						  subEe.add(ee);
						  ee = new ch.interlis.ili2c.metamodel.Enumeration.Element (
						       (String) elt.get(i),
						       new ch.interlis.ili2c.metamodel.Enumeration (subEe)
						  );
						  ee.setSourceLine(lineNumber);
						}
					      }
					}else{
					      	// extend exisiting path
					      ch.interlis.ili2c.metamodel.Enumeration.Element newEle=null;
					      for (int i = siz - 1; i >= existingEleNameIdx; i--)
					      {
						// last path element?
						if (i == siz - 1)
						{
						  if (subEnum == null)
						  {
						    // new leaf
						    newEle = new ch.interlis.ili2c.metamodel.Enumeration.Element (
						       (String) elt.get(i));
						    newEle.setDocumentation(ilidoc);
						    newEle.setMetaValues(metaValues);
						    newEle.setSourceLine(lineNumber);
						  }
						  else
						  {
						    // new subtree
						    newEle = new ch.interlis.ili2c.metamodel.Enumeration.Element (
						       (String) elt.get(i),
						       subEnum);
						    newEle.setDocumentation(ilidoc);
						    newEle.setMetaValues(metaValues);
						    newEle.setSourceLine(lineNumber);
						  }
						}
						else
						{
						  // not last path element
						  // first new path element?
						  if(i==existingEleNameIdx){
						  	// check that it is a new name in this extension
							  Iterator elei=existingEle.getSubEnumeration().getElements();
							  while(elei.hasNext()){
								  ch.interlis.ili2c.metamodel.Enumeration.Element ele=(ch.interlis.ili2c.metamodel.Enumeration.Element)elei.next();
								  if(ele.getName().equals(newEle.getName())){
									  reportError(formatMessage("err_enumerationType_DupEle",newEle.getName()),existingEle.getSourceLine());
									  break;
								  }
							  }
						  	existingEle.getSubEnumeration().addElement(newEle);
						  }else{
							  // not first new path element
							  List subEe=new ArrayList();
							  subEe.add(newEle);
							  newEle = new ch.interlis.ili2c.metamodel.Enumeration.Element (
							       (String) elt.get(i),
							       new ch.interlis.ili2c.metamodel.Enumeration (subEe)
							  );
							  newEle.setSourceLine(lineNumber);
						  }
						}
					      }
					}
				}else{
					if(siz > 1){
					reportError (rsrc.getString("err_dottedEnum"), lineNumber);
				}
				if (subEnum == null)
				{
					    // new leaf
				ee = new ch.interlis.ili2c.metamodel.Enumeration.Element (
				(String) elt.get(0));
					    ee.setDocumentation(ilidoc);
					    ee.setMetaValues(metaValues);
					    ee.setSourceLine(lineNumber);
				}
				else
				{
					    // new subtree
				ee = new ch.interlis.ili2c.metamodel.Enumeration.Element (
				(String) elt.get(0),
				subEnum);
					    ee.setDocumentation(ilidoc);
					    ee.setMetaValues(metaValues);
					    ee.setSourceLine(lineNumber);
				}
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_60);
			} else {
			  throw ex;
			}
		}
		return ee;
	}
	
	protected final int  enumNameList(
		List namList
	) throws RecognitionException, TokenStreamException {
		int lin;
		
		Token  firstName = null;
		
		lin=0;
		
		
		try {      // for error handling
			firstName = LT(1);
			match(NAME);
			if ( inputState.guessing==0 ) {
				namList.add(firstName.getText());
				lin=firstName.getLine();
				
			}
			enumNameListHelper(namList);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_61);
			} else {
			  throw ex;
			}
		}
		return lin;
	}
	
	protected final Unit  unitRef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		Unit u;
		
		
		List      nams = new LinkedList();
		u = null;
		int lin = 0;
		
		
		try {      // for error handling
			lin=names2(nams);
			if ( inputState.guessing==0 ) {
				
				Model model;
				Topic topic;
				
				String   modelName, topicName, unitName;
				
				switch (nams.size()) {
				case 1:
				model = (Model) scope.getContainerOrSame(Model.class);
				topic = (Topic) scope.getContainerOrSame(Topic.class);
				unitName = (String) nams.get(0);
				break;
				
				case 2:
				modelName = (String) nams.get(0);
				model = resolveOrFixModelName(scope, modelName, lin);
				topic = null;
				unitName = (String) nams.get(1);
				break;
				
				case 3:
				modelName = (String) nams.get(0);
				topicName = (String) nams.get(1);
				unitName = (String) nams.get(2);
				model = resolveOrFixModelName(scope, modelName, lin);
				topic = resolveOrFixTopicName(model, topicName, lin);
				break;
				
				default:
				reportError(rsrc.getString("err_weirdUnitRef"), lin);
				model = resolveModelName(scope, (String) nams.get(0));
				if (model == null)
				model = (Model) scope.getContainerOrSame(Model.class);
				topic = null;
				unitName = (String) nams.get(nams.size() - 1);
				break;
				}
				
				u = null;
				if (topic != null)
				u = (Unit) topic.getRealElement(Unit.class, unitName);
				
				if (u == null)
				u = (Unit) model.getRealElement(Unit.class, unitName);
				
				if ((u == null) && (nams.size() == 1))
				u = (Unit) model.getImportedElement (Unit.class, unitName);
				
				if (u == null)
				{
				/* A presumably common error is to use the docName instead of the name,
				e.g. by referring to a unit by "meters" instead of using "m". */
				Iterator it;
				
				if (topic != null)
				it = topic.iterator();
				else
				it = model.iterator();
				
				while (it.hasNext()) {
				Object o = it.next();
				if ((o instanceof Unit) && unitName.equals(((Unit) o).getDocName()))
				{
				u = (Unit) o;
				reportError(
				formatMessage("err_unitRefByDocName", unitName,
				model.toString(), u.getScopedName(scope)),
				lin);
				break;
				}
				}
				}
				
				if (u == null)
				{
				if (topic != null)
				reportError (formatMessage ("err_noSuchUnitInModelOrTopic",
				unitName,
				topic.toString(),
				model.toString()),
				lin);
				else
				reportError (formatMessage ("err_noSuchUnit", unitName,
				model.toString()),
				lin);
				
				try {
				u = new BaseUnit();
				u.setName(unitName);
				if (topic != null)
				topic.add(u);
				else
				model.add(u);
				} catch (Exception ex) {
				panic();
				}
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_62);
			} else {
			  throw ex;
			}
		}
		return u;
	}
	
	protected final RefSystemRef  refSys(
		Container scope,boolean isCoord
	) throws RecognitionException, TokenStreamException {
		RefSystemRef rsr;
		
		Token  lpar = null;
		Token  slash1 = null;
		Token  less = null;
		Token  slash2 = null;
		
			rsr = null;
		
			MetaObject  system = null;
			Domain        domain = null;
			int axisNumber = 0;
			
		
		try {      // for error handling
			if ((LA(1)==LCURLY)) {
				lpar = LT(1);
				match(LCURLY);
				{
				if (((_tokenSet_7.member(LA(1))))&&(isCoord)) {
					{
					system=metaObjectRef(scope,predefinedCoordSystemClass);
					slash1 = LT(1);
					match(LBRACE);
					axisNumber=posInteger();
					match(RBRACE);
					if ( inputState.guessing==0 ) {
						
									try {
									  if (system != null)
									    rsr = new RefSystemRef.CoordSystemAxis (system, axisNumber);
									} catch (Exception ex) {
									  reportError (ex, slash1.getLine ());
									}
								
					}
					}
				}
				else if (((_tokenSet_7.member(LA(1))))&&(!isCoord)) {
					system=metaObjectRef(scope,predefinedScalSystemClass);
					if ( inputState.guessing==0 ) {
						
									try {
									  if (system != null)
									    rsr = new RefSystemRef.CoordSystem (system);
									} catch (Exception ex) {
									  reportError (ex, lpar.getLine ());
									}
								
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				match(RCURLY);
			}
			else if ((LA(1)==LESS)) {
				less = LT(1);
				match(LESS);
				domain=domainRef(scope);
				{
				if ((LA(1)==LBRACE)) {
					slash2 = LT(1);
					match(LBRACE);
					axisNumber=posInteger();
					match(RBRACE);
					if ( inputState.guessing==0 ) {
						
						try {
						if (domain != null)
						rsr = new RefSystemRef.CoordDomainAxis (domain, axisNumber);
						} catch (Exception ex) {
						reportError (ex, slash2.getLine ());
						}
						
					}
				}
				else if ((LA(1)==GREATER)) {
					if ( inputState.guessing==0 ) {
						
						try {
						if (domain != null)
						rsr = new RefSystemRef.CoordDomain (domain);
						} catch (Exception ex) {
						reportError (ex, less.getLine ());
						}
						
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				match(GREATER);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_51);
			} else {
			  throw ex;
			}
		}
		return rsr;
	}
	
	protected final MetaObject  metaObjectRef(
		Container scope,Table polymorphicTo
	) throws RecognitionException, TokenStreamException {
		MetaObject referred;
		
		referred=null;
			int lin=0;
			LinkedList nams=new LinkedList();
			
		
		try {      // for error handling
			lin=names2(nams);
			if ( inputState.guessing==0 ) {
				
				Model model;
				Topic topic;
				MetaDataUseDef ref;
				
				String   modelName, topicName, basketName,objectName;
				
				switch (nams.size()) {
				case 1:
						ref=null;
					    model = (Model) scope.getContainerOrSame(Model.class);
					    topic = (Topic) scope.getContainerOrSame(Topic.class);
						MetaDataUseDef basket=null;
						if(topic!=null){
							Iterator i=topic.iterator();
							while(i.hasNext()){
								Object ele=i.next();
								if(ele instanceof MetaDataUseDef){
									if(ref==null){
										ref=(MetaDataUseDef)ele;
									}else{
										// multiple MetaDataUseDef and unqualified basketref
								        reportError(rsrc.getString("err_metaObject_scopeRequired"), lin);
									}
								}
							}
						}
							Iterator i=model.iterator();
							while(i.hasNext()){
								Object ele=i.next();
								if(ele instanceof MetaDataUseDef){
									if(ref==null){
										ref=(MetaDataUseDef)ele;
									}else{
										// multiple MetaDataUseDef and unqualified basketref
								        reportError(rsrc.getString("err_metaObject_scopeRequired"), lin);
									}
								}
							}
						if(ref==null){
							// no MetaDataUseDef at all
					        reportError(rsrc.getString("err_noMetaDataUseDef"), lin);
						}
				objectName = (String) nams.get(0);
						referred=resolveMetaObject(ref,polymorphicTo,objectName,lin);
				break;
				
				case 2:
				basketName = (String) nams.get(0);
				ref = resolveOrFixBasketName(scope, basketName, lin);
				objectName = (String) nams.get(1);
					referred=resolveMetaObject(ref,polymorphicTo,objectName,lin);
				break;
				
				case 3:
				modelName = (String) nams.get(0);
				basketName = (String) nams.get(1);
				objectName = (String) nams.get(2);
				model = resolveOrFixModelName(scope, modelName, lin);
				ref = resolveOrFixBasketName(model, basketName, lin);
					referred=resolveMetaObject(ref,polymorphicTo,objectName,lin);
				break;
				
				case 4:
				modelName = (String) nams.get(0);
				topicName = (String) nams.get(1);
				basketName = (String) nams.get(2);
				objectName = (String) nams.get(3);
				model = resolveOrFixModelName(scope, modelName, lin);
				topic = resolveOrFixTopicName(model, topicName, lin);
				ref = resolveOrFixBasketName(topic, basketName, lin);
					referred=resolveMetaObject(ref,polymorphicTo,objectName,lin);
				break;
				
				default:
				reportError(rsrc.getString("err_weirdMetaObjectRef"), lin);
				break;
				}
				
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_63);
			} else {
			  throw ex;
			}
		}
		return referred;
	}
	
	protected final PrecisionDecimal  decConst() throws RecognitionException, TokenStreamException {
		PrecisionDecimal dec;
		
		
			dec=null;
			
		
		try {      // for error handling
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
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_64);
			} else {
			  throw ex;
			}
		}
		return dec;
	}
	
	protected final FormattedTypeBaseAttrRef  baseAttrRef(
		FormattedType ft,Table scope
	) throws RecognitionException, TokenStreamException {
		FormattedTypeBaseAttrRef baseAttr;
		
		Token  name = null;
		Token  name2 = null;
		
				baseAttr=null;
				Domain domain=null;
				int intPos=0;
			
		
		try {      // for error handling
			boolean synPredMatched689 = false;
			if (((LA(1)==NAME))) {
				int _m689 = mark();
				synPredMatched689 = true;
				inputState.guessing++;
				try {
					{
					match(NAME);
					match(SLASH);
					match(NAME);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched689 = false;
				}
				rewind(_m689);
inputState.guessing--;
			}
			if ( synPredMatched689 ) {
				{
				name = LT(1);
				match(NAME);
				match(SLASH);
				domain=domainRef(scope);
				}
				if ( inputState.guessing==0 ) {
					
								AttributeDef attrdef=findAttribute(scope,name.getText());
								if(attrdef==null){
									reportError (formatMessage ("err_formattedType_unknownAttr", name.getText(),
									scope.toString()), name.getLine());
								}
								Type type=attrdef.getDomain();
								if(!(type instanceof CompositionType)){
									reportError (formatMessage ("err_formattedType_StructAttrRequired", name.getText(),
									scope.toString()), name.getLine());
								}
								if(((CompositionType)type).getCardinality().getMaximum()>1){
									reportError (formatMessage ("err_formattedType_maxCard1", name.getText(),
									scope.toString()), name.getLine());
								}
								baseAttr=new FormattedTypeBaseAttrRef(ft,attrdef,domain);
						
				}
			}
			else if ((LA(1)==NAME)) {
				{
				name2 = LT(1);
				match(NAME);
				{
				if ((LA(1)==SLASH)) {
					match(SLASH);
					intPos=posInteger();
				}
				else if ((LA(1)==NAME||LA(1)==RPAREN||LA(1)==STRING)) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				}
				if ( inputState.guessing==0 ) {
					// TODO formattedTyp
								AttributeDef attrdef=findAttribute(scope,name2.getText());
								if(attrdef==null){
									reportError (formatMessage ("err_formattedType_unknownAttr", name2.getText(),
									scope.toString()), name2.getLine());
								}
								Type type=attrdef.getDomainResolvingAliases();
								if(!(type instanceof NumericType)){
									reportError (formatMessage ("err_formattedType_NumericAttrRequired", name2.getText(),
									scope.toString()), name2.getLine());
								}
								baseAttr=new FormattedTypeBaseAttrRef(ft,attrdef,intPos);
						
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_65);
			} else {
			  throw ex;
			}
		}
		return baseAttr;
	}
	
	protected final Constant  formattedConst() throws RecognitionException, TokenStreamException {
		Constant c;
		
		
				c=null;
			
		
		try {      // for error handling
			c=textConst();
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		return c;
	}
	
	protected final int[]  rotationDef() throws RecognitionException, TokenStreamException {
		int[] rots;
		
		Token  rot = null;
		
			  rots = null;
			  int nullAxis;
			  int piHalfAxis;
			
		
		try {      // for error handling
			rot = LT(1);
			match(LITERAL_ROTATION);
			nullAxis=posInteger();
			match(POINTSTO);
			piHalfAxis=posInteger();
			if ( inputState.guessing==0 ) {
				
				if ((nullAxis == 0) || (piHalfAxis == 0))
				reportError (rsrc.getString ("err_axisNumber_zero"), rot.getLine ());
				else
				rots = new int[] { nullAxis, piHalfAxis };
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_53);
			} else {
			  throw ex;
			}
		}
		return rots;
	}
	
	protected final void contextDef(
		Container container,ContextDefs defs,int nameIdx
	) throws RecognitionException, TokenStreamException {
		
		Token  eq = null;
		
			  String ilidoc=null;
			  Settings metaValues=null;
			  Domain genericCoordDef=null;
			  Domain concreteCoordDef=null;
			  ArrayList<Domain> concreteCoordDefs=new ArrayList<Domain>();
			
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			genericCoordDef=domainRef(container);
			eq = LT(1);
			match(EQUALS);
			concreteCoordDef=domainRef(container);
			if ( inputState.guessing==0 ) {
				concreteCoordDefs.add(concreteCoordDef);
							
			}
			{
			_loop710:
			do {
				if ((LA(1)==LITERAL_OR)) {
					match(LITERAL_OR);
					concreteCoordDef=domainRef(container);
					if ( inputState.guessing==0 ) {
						concreteCoordDefs.add(concreteCoordDef);
									
					}
				}
				else {
					break _loop710;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				
							ContextDef def=new ContextDef(nameIdx,genericCoordDef,concreteCoordDefs.toArray(new Domain[concreteCoordDefs.size()]));
							def.setDocumentation(ilidoc);
							def.setMetaValues(metaValues);
							defs.add(def);
						
			}
			match(SEMI);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_66);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final ObjectPath  attributePath(
		Viewable ns,Container context
	) throws RecognitionException, TokenStreamException {
		ObjectPath object;
		
		
			object=null;
			
		
		try {      // for error handling
			object=objectOrAttributePath(ns,context);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_67);
			} else {
			  throw ex;
			}
		}
		return object;
	}
	
	protected final void xyRef() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			if ((LA(1)==NAME)) {
				match(NAME);
			}
			else if ((LA(1)==LITERAL_INTERLIS)) {
				match(LITERAL_INTERLIS);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			_loop967:
			do {
				if ((LA(1)==DOT)) {
					match(DOT);
					match(NAME);
				}
				else {
					break _loop967;
				}
				
			} while (true);
			}
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
	
	protected final LineForm[]  lineForm(
		Container scope
	) throws RecognitionException, TokenStreamException {
		LineForm[] lf;
		
		
		List ll = null;
		LineForm linForm = null;
		lf = new LineForm[0];
		
		
		try {      // for error handling
			match(LITERAL_WITH);
			match(LPAREN);
			if ( inputState.guessing==0 ) {
				
				ll = new LinkedList();
				
			}
			linForm=lineFormType(scope);
			if ( inputState.guessing==0 ) {
				if (linForm != null) ll.add(linForm);
			}
			{
			_loop746:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					linForm=lineFormType(scope);
					if ( inputState.guessing==0 ) {
						if (linForm != null) ll.add(linForm);
					}
				}
				else {
					break _loop746;
				}
				
			} while (true);
			}
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				
				lf = (LineForm[]) ll.toArray (lf);
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_47);
			} else {
			  throw ex;
			}
		}
		return lf;
	}
	
	protected final LineForm  lineFormType(
		Container scope
	) throws RecognitionException, TokenStreamException {
		LineForm lf;
		
		Token  nam = null;
		
		lf = null;
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_ARCS:
			{
				match(LITERAL_ARCS);
				if ( inputState.guessing==0 ) {
					
					lf = modelInterlis.ARCS;
					
				}
				break;
			}
			case LITERAL_STRAIGHTS:
			{
				match(LITERAL_STRAIGHTS);
				if ( inputState.guessing==0 ) {
					
					lf = modelInterlis.STRAIGHTS;
					
				}
				break;
			}
			case NAME:
			{
				nam = LT(1);
				match(NAME);
				if ( inputState.guessing==0 ) {
					
						/* TODO als Verweis ist hier auch ein qualifizierter Name zulaessig
						*/
					Model scopeModel = (Model) scope.getContainerOrSame (Model.class);
					lf = (LineForm) scopeModel.getRealElement (LineForm.class, nam.getText());
					if (lf == null)
					lf = (LineForm) modelInterlis.getRealElement (LineForm.class, nam.getText());
					
					if (lf == null)
					reportError (formatMessage (
					"err_lineForm_unknownName", nam.getText(), scopeModel.toString()),
					nam.getLine());
					
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
				recover(ex,_tokenSet_68);
			} else {
			  throw ex;
			}
		}
		return lf;
	}
	
	protected final void unitDef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		Token  idn = null;
		Token  ext = null;
		
		int mods = 0;
		Unit extending = null;
		Unit u = null;
		boolean _abstract = false;
		int unitSourceLine=0;
		String docName = null, idName = null;
			  String ilidoc=null;
			  Settings metaValues=null;
		
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc();metaValues=getMetaValues();
			}
			n = LT(1);
			match(NAME);
			if ( inputState.guessing==0 ) {
				docName = idName = n.getText(); unitSourceLine=n.getLine();
			}
			{
			switch ( LA(1)) {
			case LBRACE:
			{
				match(LBRACE);
				idn = LT(1);
				match(NAME);
				match(RBRACE);
				if ( inputState.guessing==0 ) {
					idName = idn.getText ();
				}
				break;
			}
			case LPAREN:
			{
				match(LPAREN);
				match(LITERAL_ABSTRACT);
				match(RPAREN);
				if ( inputState.guessing==0 ) {
					_abstract=true;
				}
				break;
			}
			case SEMI:
			case EQUALS:
			case LITERAL_EXTENDS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			if ((LA(1)==LITERAL_EXTENDS)) {
				ext = LT(1);
				match(LITERAL_EXTENDS);
				extending=unitRef(scope);
			}
			else if ((LA(1)==SEMI||LA(1)==EQUALS)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((LA(1)==EQUALS)) {
				match(EQUALS);
				{
				if ((_tokenSet_69.member(LA(1)))) {
					u=derivedUnit(scope, idName, docName, _abstract);
					if ( inputState.guessing==0 ) {
						
						if (extending != null)
						reportError (rsrc.getString ("err_derivedUnit_ext"), ext.getLine());
						
					}
				}
				else if ((LA(1)==LPAREN)) {
					u=composedUnit(scope, idName, docName, _abstract);
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				match(SEMI);
			}
			else if ((LA(1)==SEMI)) {
				match(SEMI);
				if ( inputState.guessing==0 ) {
					
					u = new BaseUnit();
					try {
					u.setName (idName);
					u.setDocName (docName);
					} catch (Exception ex) {
					reportError (ex, n.getLine());
					}
					
					try {
					u.setAbstract (_abstract);
					} catch (Exception ex) {
					reportError (ex, n.getLine());
					}
					
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
				try {
				u.setSourceLine(unitSourceLine);
					u.setDocumentation(ilidoc);
					u.setMetaValues(metaValues);
				scope.add(u);
					if(extending!=null){
					u.setExtending(extending);
					}
				} catch (Exception ex) {
				reportError(ex, n.getLine());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_45);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final Unit  derivedUnit(
		Container scope, String idName, String docName, boolean _abstract
	) throws RecognitionException, TokenStreamException {
		Unit u;
		
		Token  f = null;
		Token  exp = null;
		Token  st = null;
		Token  sl = null;
		
		u = null;
		Unit baseUnit = null;
		List factors = null;
		int line = 0;
		char compOp = '*';
		PrecisionDecimal fac = new PrecisionDecimal("1");
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_FUNCTION:
			{
				f = LT(1);
				match(LITERAL_FUNCTION);
				exp = LT(1);
				match(EXPLANATION);
				match(LBRACE);
				baseUnit=unitRef(scope);
				match(RBRACE);
				if ( inputState.guessing==0 ) {
					
					u = new FunctionallyDerivedUnit ();
					
					try {
					u.setName (idName);
					u.setDocName (docName);
					} catch (Exception ex) {
					reportError(ex, f.getLine());
					}
					
					try {
					((FunctionallyDerivedUnit) u).setExplanation (exp.getText());
					} catch (Exception ex) {
					reportError(ex, exp.getLine());
					}
					
					try {
					u.setAbstract (_abstract);
					} catch (Exception ex) {
					reportError(ex, f.getLine());
					}
					
					try {
					((FunctionallyDerivedUnit) u).setExtending(baseUnit);
					} catch (Exception ex) {
					reportError(ex, f.getLine());
					}
					
				}
				break;
			}
			case LITERAL_PI:
			case LITERAL_LNBASE:
			case DEC:
			case POSINT:
			case NUMBER:
			{
				fac=decConst();
				if ( inputState.guessing==0 ) {
					
					u = new NumericallyDerivedUnit();
					factors = new LinkedList();
					
					try {
					factors.add (new NumericallyDerivedUnit.Factor ('*', fac));
					} catch (Exception ex) {
					reportError (ex, line);
					}
					
				}
				{
				_loop762:
				do {
					if ((LA(1)==STAR||LA(1)==SLASH)) {
						{
						if ((LA(1)==STAR)) {
							st = LT(1);
							match(STAR);
							if ( inputState.guessing==0 ) {
								compOp = '*'; line=st.getLine();
							}
						}
						else if ((LA(1)==SLASH)) {
							sl = LT(1);
							match(SLASH);
							if ( inputState.guessing==0 ) {
								compOp = '/'; line=sl.getLine();
							}
						}
						else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						
						}
						fac=decConst();
						if ( inputState.guessing==0 ) {
							
							try {
							factors.add (new NumericallyDerivedUnit.Factor (compOp, fac));
							} catch (Exception ex) {
							reportError (ex, line);
							}
							
						}
					}
					else {
						break _loop762;
					}
					
				} while (true);
				}
				match(LBRACE);
				baseUnit=unitRef(scope);
				match(RBRACE);
				if ( inputState.guessing==0 ) {
					
					NumericallyDerivedUnit ndu = (NumericallyDerivedUnit) u;
					try {
					u.setName (idName);
					u.setDocName (docName);
					} catch (Exception ex) {
					reportError(ex, line);
					}
					
					try {
					u.setAbstract (_abstract);
					} catch (Exception ex) {
					reportError(ex, line);
					}
					
					try {
					ndu.setExtending (baseUnit);
					} catch (Exception ex) {
					reportError (ex, line);
					}
					
					try {
					ndu.setConversionFactors (
					(NumericallyDerivedUnit.Factor[]) factors.toArray (
					new NumericallyDerivedUnit.Factor[factors.size()]));
					} catch (Exception ex) {
					reportError (ex, line);
					}
					
				}
				break;
			}
			case LBRACE:
			{
				match(LBRACE);
				baseUnit=unitRef(scope);
				match(RBRACE);
				if ( inputState.guessing==0 ) {
					
					u = new NumericallyDerivedUnit();
					NumericallyDerivedUnit ndu = (NumericallyDerivedUnit) u;
					try {
					u.setName (idName);
					u.setDocName (docName);
					} catch (Exception ex) {
					reportError(ex, line);
					}
					
					try {
					u.setAbstract (_abstract);
					} catch (Exception ex) {
					reportError(ex, line);
					}
					
					try {
					ndu.setExtending (baseUnit);
					} catch (Exception ex) {
					reportError (ex, line);
					}
					
					try {
					ndu.setConversionFactors (
					new NumericallyDerivedUnit.Factor[] {
					new NumericallyDerivedUnit.Factor ('*', new PrecisionDecimal("1"))
					});
					} catch (Exception ex) {
					reportError (ex, line);
					}
					
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
				recover(ex,_tokenSet_70);
			} else {
			  throw ex;
			}
		}
		return u;
	}
	
	protected final ComposedUnit  composedUnit(
		Container scope, String idName, String docName, boolean _abstract
	) throws RecognitionException, TokenStreamException {
		ComposedUnit u;
		
		Token  lbrac = null;
		Token  st = null;
		Token  sl = null;
		
		u = null;
		List composed = null;
		
		int line = 0;
		char compOp = '*';
		Unit compUnit = null;
		
		
		try {      // for error handling
			lbrac = LT(1);
			match(LPAREN);
			compUnit=unitRef(scope);
			if ( inputState.guessing==0 ) {
				
				u = new ComposedUnit();
				composed = new LinkedList();
				
				try {
				composed.add (new ComposedUnit.Composed ('*', compUnit));
				u.setName (idName);
				u.setDocName (docName);
				} catch (Exception ex) {
				reportError(ex, lbrac.getLine());
				}
				
				try {
				u.setAbstract (_abstract);
				} catch (Exception ex) {
				reportError(ex, lbrac.getLine());
				}
				
			}
			{
			_loop766:
			do {
				if ((LA(1)==STAR||LA(1)==SLASH)) {
					{
					if ((LA(1)==STAR)) {
						st = LT(1);
						match(STAR);
						if ( inputState.guessing==0 ) {
							compOp = '*'; line=st.getLine();
						}
					}
					else if ((LA(1)==SLASH)) {
						sl = LT(1);
						match(SLASH);
						if ( inputState.guessing==0 ) {
							compOp = '/'; line=sl.getLine();
						}
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
					compUnit=unitRef(scope);
					if ( inputState.guessing==0 ) {
						
						try {
						composed.add (new ComposedUnit.Composed (compOp, compUnit));
						} catch (Exception ex) {
						reportError (ex, line);
						}
						
					}
				}
				else {
					break _loop766;
				}
				
			} while (true);
			}
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				
				try {
				u.setComposedUnits (
				(ComposedUnit.Composed[]) composed.toArray (
				new ComposedUnit.Composed[composed.size()]));
				} catch (Exception ex) {
				reportError (ex, lbrac.getLine());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_70);
			} else {
			  throw ex;
			}
		}
		return u;
	}
	
	protected final MetaDataUseDef  metaDataBasketRef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		MetaDataUseDef ref;
		
		ref=null;
			int lin=0;
			LinkedList nams=new LinkedList();
			
		
		try {      // for error handling
			lin=names2(nams);
			if ( inputState.guessing==0 ) {
				
				Model model;
				Topic topic;
				
				String   modelName, topicName, basketName;
				
				switch (nams.size()) {
				case 1:
				model = (Model) scope.getContainerOrSame(Model.class);
				topic = (Topic) scope.getContainerOrSame(Topic.class);
				basketName = (String) nams.get(0);
				break;
				
				case 2:
				modelName = (String) nams.get(0);
				model = resolveOrFixModelName(scope, modelName, lin);
				topic = null;
				basketName = (String) nams.get(1);
				break;
				
				case 3:
				modelName = (String) nams.get(0);
				topicName = (String) nams.get(1);
				basketName = (String) nams.get(2);
				model = resolveOrFixModelName(scope, modelName, lin);
				topic = resolveOrFixTopicName(model, topicName, lin);
				break;
				
				default:
				reportError(rsrc.getString("err_weirdMetaDataUseRef"), lin);
				model = resolveModelName(scope, (String) nams.get(0));
				if (model == null)
				model = (Model) scope.getContainerOrSame(Model.class);
				topic = null;
				basketName = (String) nams.get(nams.size() - 1);
				break;
				}
				
				ref = null;
				if (topic != null)
				ref = (MetaDataUseDef) topic.getRealElement(MetaDataUseDef.class, basketName);
				
				if (ref == null)
				ref = (MetaDataUseDef) model.getRealElement(MetaDataUseDef.class, basketName);
				
				if ((ref == null) && (nams.size() == 1))
				ref = (MetaDataUseDef) model.getImportedElement (MetaDataUseDef.class, basketName);
				
				if (ref == null)
				{
				if (topic != null)
				reportError (formatMessage ("err_noSuchMetaDataUseDefInModelOrTopic",
				basketName,
				topic.toString(),
				model.toString()),
				lin);
				else
				reportError (formatMessage ("err_noSuchMetaDataUseDef", basketName,
				model.toString()),
				lin);
				
				try {
				ref = new MetaDataUseDef();
				ref.setName(basketName);
				if (topic != null)
				topic.add(ref);
				else
				model.add(ref);
				} catch (Exception ex) {
				panic();
				}
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_71);
			} else {
			  throw ex;
			}
		}
		return ref;
	}
	
	protected final MandatoryConstraint  mandatoryConstraint(
		Viewable v,Container context
	) throws RecognitionException, TokenStreamException {
		MandatoryConstraint constr;
		
		Token  mand = null;
		Token  n = null;
		
		Evaluable condition = null;
		constr = null;
		
		
		try {      // for error handling
			mand = LT(1);
			match(LITERAL_MANDATORY);
			match(LITERAL_CONSTRAINT);
			{
			boolean synPredMatched790 = false;
			if (((LA(1)==NAME))) {
				int _m790 = mark();
				synPredMatched790 = true;
				inputState.guessing++;
				try {
					{
					match(NAME);
					match(COLON);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched790 = false;
				}
				rewind(_m790);
inputState.guessing--;
			}
			if ( synPredMatched790 ) {
				n = LT(1);
				match(NAME);
				match(COLON);
			}
			else if ((_tokenSet_72.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			condition=expression(v, /* expectedType */ predefinedBooleanType,context);
			match(SEMI);
			if ( inputState.guessing==0 ) {
				
				try {
				constr = new MandatoryConstraint();
					if(n!=null){constr.setName(n.getText());}
					if(!condition.isDirty() && !condition.isLogical()){
							reportError (formatMessage ("err_expr_noLogical",(String)null),
				mand.getLine());
				constr.setDirty(true);
					}
				constr.setCondition(condition);
				} catch (Exception ex) {
				reportError(ex, mand.getLine());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		return constr;
	}
	
	protected final PlausibilityConstraint  plausibilityConstraint(
		Viewable v,Container context
	) throws RecognitionException, TokenStreamException {
		PlausibilityConstraint constr;
		
		Token  tok = null;
		Token  n = null;
		
		PrecisionDecimal       percentage;
		int                    direction = 0;
		Evaluable              condition = null;
		constr = null;
		
		
		try {      // for error handling
			tok = LT(1);
			match(LITERAL_CONSTRAINT);
			{
			boolean synPredMatched794 = false;
			if (((LA(1)==NAME))) {
				int _m794 = mark();
				synPredMatched794 = true;
				inputState.guessing++;
				try {
					{
					match(NAME);
					match(COLON);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched794 = false;
				}
				rewind(_m794);
inputState.guessing--;
			}
			if ( synPredMatched794 ) {
				n = LT(1);
				match(NAME);
				match(COLON);
			}
			else if ((LA(1)==LESSEQUAL||LA(1)==GREATEREQUAL)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((LA(1)==LESSEQUAL)) {
				match(LESSEQUAL);
				if ( inputState.guessing==0 ) {
					direction = PlausibilityConstraint.DIRECTION_AT_MOST;
				}
			}
			else if ((LA(1)==GREATEREQUAL)) {
				match(GREATEREQUAL);
				if ( inputState.guessing==0 ) {
					direction = PlausibilityConstraint.DIRECTION_AT_LEAST;
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			percentage=decimal();
			match(PERCENT);
			condition=expression(v, /* expectedType */ predefinedBooleanType,context);
			match(SEMI);
			if ( inputState.guessing==0 ) {
				
				try {
				constr = new PlausibilityConstraint();
					if(n!=null){constr.setName(n.getText());}
					if(!condition.isDirty() && !condition.isLogical()){
							reportError (formatMessage ("err_expr_noLogical",(String)null),
				tok.getLine());
				constr.setDirty(true);
					}
				constr.setDirection(direction);
				constr.setCondition(condition);
				constr.setPercentage(percentage.doubleValue());
				} catch (Exception ex) {
				reportError (ex, tok.getLine());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		return constr;
	}
	
	protected final ExistenceConstraint  existenceConstraint(
		Viewable v,Container context
	) throws RecognitionException, TokenStreamException {
		ExistenceConstraint constr;
		
		Token  e = null;
		Token  n = null;
		
				ObjectPath attr;
				Viewable ref;
				constr=new ExistenceConstraint();
				ObjectPath attrRef=null;
			
		
		try {      // for error handling
			e = LT(1);
			match(LITERAL_EXISTENCE);
			match(LITERAL_CONSTRAINT);
			{
			boolean synPredMatched799 = false;
			if (((LA(1)==NAME))) {
				int _m799 = mark();
				synPredMatched799 = true;
				inputState.guessing++;
				try {
					{
					match(NAME);
					match(COLON);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched799 = false;
				}
				rewind(_m799);
inputState.guessing--;
			}
			if ( synPredMatched799 ) {
				n = LT(1);
				match(NAME);
				match(COLON);
			}
			else if ((_tokenSet_33.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			attr=attributePath(v,context);
			if ( inputState.guessing==0 ) {
				
							try{
								if(n!=null){constr.setName(n.getText());}
							} catch (Exception ex) {
								reportError(ex, e.getLine());
							}
							constr.setRestrictedAttribute(attr);
						
			}
			match(LITERAL_REQUIRED);
			match(LITERAL_IN);
			ref=viewableRefDepReq(v);
			match(COLON);
			attrRef=attributePath(ref,context);
			if ( inputState.guessing==0 ) {
				
							constr.addRequiredIn(attrRef); 
						
			}
			{
			_loop801:
			do {
				if ((LA(1)==LITERAL_OR)) {
					match(LITERAL_OR);
					ref=viewableRefDepReq(v);
					match(COLON);
					attrRef=attributePath(ref,context);
					if ( inputState.guessing==0 ) {
						
									constr.addRequiredIn(attrRef); 
								
					}
				}
				else {
					break _loop801;
				}
				
			} while (true);
			}
			match(SEMI);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		return constr;
	}
	
	protected final UniquenessConstraint  uniquenessConstraint(
		Viewable v,Container context
	) throws RecognitionException, TokenStreamException {
		UniquenessConstraint constr;
		
		Token  u = null;
		Token  n = null;
		
			Evaluable preCond=null;
				constr=new UniquenessConstraint();
			
		
		try {      // for error handling
			u = LT(1);
			match(LITERAL_UNIQUE);
			{
			boolean synPredMatched805 = false;
			if (((LA(1)==LPAREN))) {
				int _m805 = mark();
				synPredMatched805 = true;
				inputState.guessing++;
				try {
					{
					match(LPAREN);
					match(LITERAL_BASKET);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched805 = false;
				}
				rewind(_m805);
inputState.guessing--;
			}
			if ( synPredMatched805 ) {
				match(LPAREN);
				match(LITERAL_BASKET);
				match(RPAREN);
			}
			else if ((_tokenSet_73.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			boolean synPredMatched808 = false;
			if (((LA(1)==NAME))) {
				int _m808 = mark();
				synPredMatched808 = true;
				inputState.guessing++;
				try {
					{
					match(NAME);
					match(COLON);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched808 = false;
				}
				rewind(_m808);
inputState.guessing--;
			}
			if ( synPredMatched808 ) {
				n = LT(1);
				match(NAME);
				match(COLON);
			}
			else if ((_tokenSet_73.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
						try{
							if(n!=null){constr.setName(n.getText());}
						} catch (Exception ex) {
							reportError(ex, u.getLine());
						}
					
			}
			{
			if ((LA(1)==LITERAL_WHERE)) {
				match(LITERAL_WHERE);
				preCond=expression(v, /* expectedType */ predefinedBooleanType,context);
				match(COLON);
			}
			else if ((_tokenSet_74.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((_tokenSet_33.member(LA(1)))) {
				constr=globalUniqueness(v,context);
			}
			else if ((LA(1)==LPAREN)) {
				constr=localUniqueness(v);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
						if(preCond!=null){ 
							if(!preCond.isDirty() && !preCond.isLogical()){
								reportError (formatMessage ("err_expr_noLogical",(String)null),
										 u.getLine());
								constr.setDirty(true);
							}
							constr.setPreCondition(preCond);
						}
						// check that all attrPaths do not point to a struct
						UniqueEl elements=constr.getElements();
						Iterator attri=elements.iteratorAttribute();
						while(attri.hasNext()){
							ObjectPath attr=(ObjectPath)attri.next();
							if(attr.isAttributePath() && attr.getType() instanceof CompositionType){
								reportError(formatMessage ("err_uniqueness_StructNoAllowed",attr.toString()),u.getLine());
							}
						}
					
			}
			match(SEMI);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		return constr;
	}
	
	protected final SetConstraint  setConstraint(
		Viewable v,Container context
	) throws RecognitionException, TokenStreamException {
		SetConstraint constr;
		
		Token  tok = null;
		Token  n = null;
		
			Evaluable preCond=null;
			Evaluable condition=null;
		constr = new SetConstraint();
		
		
		try {      // for error handling
			tok = LT(1);
			match(LITERAL_SET);
			match(LITERAL_CONSTRAINT);
			{
			boolean synPredMatched823 = false;
			if (((LA(1)==LPAREN))) {
				int _m823 = mark();
				synPredMatched823 = true;
				inputState.guessing++;
				try {
					{
					match(LPAREN);
					match(LITERAL_BASKET);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched823 = false;
				}
				rewind(_m823);
inputState.guessing--;
			}
			if ( synPredMatched823 ) {
				match(LPAREN);
				match(LITERAL_BASKET);
				match(RPAREN);
			}
			else if ((_tokenSet_75.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			boolean synPredMatched826 = false;
			if (((LA(1)==NAME))) {
				int _m826 = mark();
				synPredMatched826 = true;
				inputState.guessing++;
				try {
					{
					match(NAME);
					match(COLON);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched826 = false;
				}
				rewind(_m826);
inputState.guessing--;
			}
			if ( synPredMatched826 ) {
				n = LT(1);
				match(NAME);
				match(COLON);
			}
			else if ((_tokenSet_75.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((LA(1)==LITERAL_WHERE)) {
				match(LITERAL_WHERE);
				preCond=expression(v, /* expectedType */ predefinedBooleanType,context);
				match(COLON);
				if ( inputState.guessing==0 ) {
					
								if(!preCond.isDirty() && !preCond.isLogical()){
									reportError (formatMessage ("err_expr_noLogical",(String)null),
											 tok.getLine());
									constr.setDirty(true);
								}
						        constr.setPreCondition(preCond);
							
				}
			}
			else if ((_tokenSet_72.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			condition=expression(v, /* expectedType */ predefinedBooleanType,context);
			match(SEMI);
			if ( inputState.guessing==0 ) {
				
					  if(v instanceof Table && !((Table)v).isIdentifiable()){
							reportError (formatMessage ("err_constraint_illegalSetInStruct",
								v.getScopedName(null)), tok.getLine());
					  }else{
							if(!condition.isDirty() && !condition.isLogical()){
								reportError (formatMessage ("err_expr_noLogical",(String)null),
										 tok.getLine());
								constr.setDirty(true);
							}
						try {
							if(n!=null){constr.setName(n.getText());}
							constr.setCondition(condition);
						} catch (Exception ex) {
							reportError(ex, tok.getLine());
						}
					  }
					
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		return constr;
	}
	
	protected final Viewable  viewableRefDepReq(
		Container scope
	) throws RecognitionException, TokenStreamException {
		Viewable ref;
		
		
			ref=null;
			int refLine=0;
		
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				refLine=LT(1).getLine();
			}
			ref=viewableRef(scope);
			if ( inputState.guessing==0 ) {
				
					if(ref!=null){
				// check that scope's topic depends on ref's topic
				AbstractPatternDef scopeTopic=(AbstractPatternDef)scope.getContainerOrSame(AbstractPatternDef.class);
				AbstractPatternDef refTopic=(AbstractPatternDef)ref.getContainer(AbstractPatternDef.class);
				if(refTopic!=scopeTopic){
				if(!scopeTopic.isDependentOn(refTopic)){
				reportError(formatMessage ("err_viewableref_topicdepreq",
								scopeTopic.getName(),
								refTopic.getName()),refLine);
				}
				}
					}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_76);
			} else {
			  throw ex;
			}
		}
		return ref;
	}
	
	protected final UniquenessConstraint  globalUniqueness(
		Viewable scope,Container context
	) throws RecognitionException, TokenStreamException {
		UniquenessConstraint constr;
		
		
				constr=new UniquenessConstraint();
				UniqueEl elements=null;
			
		
		try {      // for error handling
			elements=uniqueEl(scope,context);
			if ( inputState.guessing==0 ) {
				
						constr.setElements(elements);
					
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_70);
			} else {
			  throw ex;
			}
		}
		return constr;
	}
	
	protected final UniquenessConstraint  localUniqueness(
		Viewable start
	) throws RecognitionException, TokenStreamException {
		UniquenessConstraint constr;
		
		Token  n = null;
		Token  n2 = null;
		Token  n3 = null;
		Token  n4 = null;
		
				constr=new UniquenessConstraint();
				constr.setLocal(true);
				UniqueEl elements=new UniqueEl();
				constr.setElements(elements);
				ObjectPath prefix=null;
				PathEl el;
				LinkedList path=new LinkedList();
				Viewable next=null;
				Viewable localViewable=null;
			
		
		try {      // for error handling
			match(LPAREN);
			match(LITERAL_LOCAL);
			match(RPAREN);
			n = LT(1);
			match(NAME);
			if ( inputState.guessing==0 ) {
				
							next=start;
							AttributeDef attrdef=findAttribute(start,n.getText());
							if(attrdef==null){
								reportError (formatMessage ("err_localUniqueness_unknownAttr", n.getText(),
								next.toString()), n.getLine());
							}
							if(!(attrdef.getDomainResolvingAliases() instanceof CompositionType)){
								reportError (formatMessage ("err_localUniqueness_unknownStructAttr", n.getText(),
									next.toString()), n.getLine());
							}
							el=new AttributeRef(attrdef);
							path.add(el);
							next=el.getViewable();
						
			}
			{
			_loop817:
			do {
				if ((LA(1)==POINTSTO)) {
					match(POINTSTO);
					n2 = LT(1);
					match(NAME);
					if ( inputState.guessing==0 ) {
						
									AttributeDef attrdef=findAttribute(next,n2.getText());
									if(attrdef==null){
										reportError (formatMessage ("err_localUniqueness_unknownAttr", n2.getText(),
										next.toString()), n2.getLine());
									}
									if(!(attrdef.getDomainResolvingAliases() instanceof CompositionType)){
										reportError (formatMessage ("err_localUniqueness_unknownStructAttr", n2.getText(),
											next.toString()), n2.getLine());
									}
									el=new AttributeRef(attrdef);
									path.add(el);
									next=el.getViewable();
								
					}
				}
				else {
					break _loop817;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				
							prefix=new ObjectPath(start,(PathEl[])path.toArray(new PathEl[path.size()]));
							constr.setPrefix(prefix);
						
			}
			match(COLON);
			n3 = LT(1);
			match(NAME);
			if ( inputState.guessing==0 ) {
				
							localViewable=next;
							AttributeDef attrdef=findAttribute(localViewable,n3.getText());
							if(attrdef==null){
								reportError (formatMessage ("err_localUniqueness_unknownAttr", n3.getText(),
								localViewable.toString()), n3.getLine());
							}
							AttributeRef[] attrRef=new AttributeRef[1];
							attrRef[0]=new AttributeRef(attrdef);
							elements.addAttribute(new ObjectPath(localViewable,attrRef));
						
			}
			{
			_loop819:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					n4 = LT(1);
					match(NAME);
					if ( inputState.guessing==0 ) {
						
									AttributeDef attrdef=findAttribute(localViewable,n4.getText());
									if(attrdef==null){
										reportError (formatMessage ("err_localUniqueness_unknownAttr", n4.getText(),
										localViewable.toString()), n4.getLine());
									}
									AttributeRef[] attrRef=new AttributeRef[1];
									attrRef[0]=new AttributeRef(attrdef);
									elements.addAttribute(new ObjectPath(localViewable,attrRef));
								
					}
				}
				else {
					break _loop819;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_70);
			} else {
			  throw ex;
			}
		}
		return constr;
	}
	
	protected final UniqueEl  uniqueEl(
		Viewable scope,Container context
	) throws RecognitionException, TokenStreamException {
		UniqueEl ret;
		
		
				ret=null;
				ObjectPath attr=null;
			
		
		try {      // for error handling
			attr=objectOrAttributePath(scope,context);
			if ( inputState.guessing==0 ) {
				
							ret=new UniqueEl();
							ret.addAttribute(attr);
						
			}
			{
			_loop814:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					attr=objectOrAttributePath(scope,context);
					if ( inputState.guessing==0 ) {
						
									ret.addAttribute(attr);
								
					}
				}
				else {
					break _loop814;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_77);
			} else {
			  throw ex;
			}
		}
		return ret;
	}
	
	protected final ObjectPath  objectOrAttributePath(
		Viewable start,Container context
	) throws RecognitionException, TokenStreamException {
		ObjectPath object;
		
		Token  p = null;
		
				object=null;
				PathEl el;
				LinkedList path=new LinkedList();
				Viewable next=null;
			
		
		try {      // for error handling
			el=pathEl(start,context);
			if ( inputState.guessing==0 ) {
				
							path.add(el);
							next=start;
						
			}
			{
			_loop858:
			do {
				if ((LA(1)==POINTSTO)) {
					p = LT(1);
					match(POINTSTO);
					if ( inputState.guessing==0 ) {
						
									Object prenext=next;
									next=el.getViewable();
									// System.err.println(el+": "+prenext+"->"+next);
								
					}
					el=pathEl(next,null);
					if ( inputState.guessing==0 ) {
						
									path.add(el);
								
					}
				}
				else {
					break _loop858;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				
							object=new ObjectPath(start,(PathEl[])path.toArray(new PathEl[path.size()]));
						
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_78);
			} else {
			  throw ex;
			}
		}
		return object;
	}
	
	protected final Evaluable  term(
		Container ns, Type expectedType, Container functionNs
	) throws RecognitionException, TokenStreamException {
		Evaluable expr;
		
		
		expr = null;
		int lineNumber = 0;
		boolean dirty=false;
		
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				lineNumber=LT(1).getLine();
			}
			expr=term0(ns, expectedType, functionNs);
			{
			if ((LA(1)==IMPLIES)) {
				match(IMPLIES);
				expr=term0(ns, expectedType, functionNs);
			}
			else if ((_tokenSet_46.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_46);
			} else {
			  throw ex;
			}
		}
		return expr;
	}
	
	protected final Evaluable  term0(
		Container ns, Type expectedType, Container functionNs
	) throws RecognitionException, TokenStreamException {
		Evaluable expr;
		
		Token  o = null;
		
		List disjoined = null;
		expr = null;
		int lineNumber = 0;
		boolean dirty=false;
		
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				lineNumber=LT(1).getLine();
			}
			expr=term1(ns, expectedType, functionNs);
			if ( inputState.guessing==0 ) {
				
				disjoined = new LinkedList ();
				if(expr!=null){
					      disjoined.add(expr);
				}
				
			}
			{
			_loop836:
			do {
				if ((LA(1)==LITERAL_OR)) {
					o = LT(1);
					match(LITERAL_OR);
					expr=term1(ns, expectedType, functionNs);
					if ( inputState.guessing==0 ) {
						
							if(expr!=null){
									disjoined.add (expr);
									lineNumber = o.getLine();
									if(!dirty && !expr.isDirty() && !expr.isLogical()){
										reportError (formatMessage ("err_expr_noLogical",(String)null),
												 lineNumber);
										dirty=true;
									}
							}
						
					}
				}
				else {
					break _loop836;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				
				if (disjoined.size() == 1){
				expr = (Evaluable) disjoined.get(0);
				if(dirty){
				expr.setDirty(dirty);
				}
				}else if (disjoined.size() > 1){
				try {
							expr = (Evaluable) disjoined.get(0);
							if(!dirty && !expr.isDirty() && !expr.isLogical()){
								reportError (formatMessage ("err_expr_noLogical",(String)null),
										 lineNumber);
								dirty=true;
							}
				expr = new Expression.Disjunction (
				(Evaluable[]) disjoined.toArray (new Evaluable[disjoined.size()]));
				expr.setDirty(dirty);
				} catch (Exception ex) {
				reportError (ex, lineNumber);
				}
				}else{
					expr=null;
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_79);
			} else {
			  throw ex;
			}
		}
		return expr;
	}
	
	protected final Evaluable  term1(
		Container ns, Type expectedType,Container functionNs
	) throws RecognitionException, TokenStreamException {
		Evaluable expr;
		
		Token  an = null;
		
		List conjoined = null;
		expr = null;
		int lineNumber = 0;
		boolean dirty=false;
		
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				lineNumber=LT(1).getLine();
			}
			expr=term2(ns, expectedType, functionNs);
			if ( inputState.guessing==0 ) {
				
				conjoined = new LinkedList ();
				if(expr!=null){
					    conjoined.add(expr);
					  }
				
			}
			{
			_loop839:
			do {
				if ((LA(1)==LITERAL_AND)) {
					an = LT(1);
					match(LITERAL_AND);
					expr=term2(ns, expectedType, functionNs);
					if ( inputState.guessing==0 ) {
						
							if(expr!=null){
									conjoined.add (expr);
									lineNumber = an.getLine();
									if(!dirty && !expr.isDirty() && !expr.isLogical()){
										reportError (formatMessage ("err_expr_noLogical",(String)null),
												 lineNumber);
										dirty=true;
									}
								}
						
					}
				}
				else {
					break _loop839;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				
				if (conjoined.size() == 1){
				expr = (Evaluable) conjoined.get(0);
				if(dirty){
				expr.setDirty(dirty);
				}
				} else if (conjoined.size() > 1){
				try {
				
							expr = (Evaluable) conjoined.get(0);        
							if(!dirty && !expr.isDirty() && !expr.isLogical()){
									reportError (formatMessage ("err_expr_noLogical",(String)null),
											 lineNumber);
									dirty=true;
							}
							expr = new Expression.Conjunction(
					(Evaluable[]) conjoined.toArray(new Evaluable[conjoined.size()]));
				expr.setDirty(dirty);
				} catch (Exception ex) {
				reportError (ex, lineNumber);
				}
				}else{
					expr=null;
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_80);
			} else {
			  throw ex;
			}
		}
		return expr;
	}
	
	protected final Evaluable  term2(
		Container ns, Type expectedType,Container functionNs
	) throws RecognitionException, TokenStreamException {
		Evaluable expr;
		
		
		expr = null;
		Evaluable exprRet = null;
		Evaluable comparedWith = null;
		char op = '=';
		int[] lineNumberPar = null;
		
		
		try {      // for error handling
			expr=predicate(ns, expectedType,functionNs);
			{
			if ((_tokenSet_81.member(LA(1)))) {
				if ( inputState.guessing==0 ) {
					
					lineNumberPar = new int[1];
					
				}
				op=relation(lineNumberPar);
				comparedWith=predicate(ns, expectedType,functionNs);
				if ( inputState.guessing==0 ) {
					
					try {
					switch (op)
					{
					/* EQUALSEQUALS */
					case '=':
					exprRet = new Expression.Equality (expr, comparedWith);
					validateEqualsArgumentTypes(exprRet,expr,comparedWith,lineNumberPar[0]);
					break;
					
					/* LESSGREATER, BANGEQUALS */
					case '!':
					exprRet = new Expression.Inequality (expr, comparedWith);
					validateEqualsArgumentTypes(exprRet,expr,comparedWith,lineNumberPar[0]);
					break;
					
					/* LESSEQUAL */
					case 'l':
					exprRet = new Expression.LessThanOrEqual (expr, comparedWith);
					validateCompareArgumentTypes(exprRet,expr,comparedWith,lineNumberPar[0]);
					break;
					
					/* GREATEREQUAL */
					case 'g':
					exprRet = new Expression.GreaterThanOrEqual (expr, comparedWith);
					validateCompareArgumentTypes(exprRet,expr,comparedWith,lineNumberPar[0]);
					break;
					
					/* LESS */
					case '<':
					exprRet = new Expression.LessThan (expr, comparedWith);
					validateCompareArgumentTypes(exprRet,expr,comparedWith,lineNumberPar[0]);
					break;
					
					/* GREATER */
					case '>':
					exprRet = new Expression.GreaterThan (expr, comparedWith);
					validateCompareArgumentTypes(exprRet,expr,comparedWith,lineNumberPar[0]);
					break;
					}
					expr=exprRet;
					} catch (Exception ex) {
					reportError (ex, lineNumberPar[0]);
					}
					
				}
			}
			else if ((_tokenSet_82.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_82);
			} else {
			  throw ex;
			}
		}
		return expr;
	}
	
	protected final Evaluable  predicate(
		Container ns, Type expectedType,Container functionNs
	) throws RecognitionException, TokenStreamException {
		Evaluable expr;
		
		Token  nt = null;
		Token  def = null;
		
			expr = null;
			boolean negation=false;
			
		
		try {      // for error handling
			switch ( LA(1)) {
			case LPAREN:
			case LITERAL_NOT:
			{
				{
				if ((LA(1)==LITERAL_NOT)) {
					nt = LT(1);
					match(LITERAL_NOT);
					if ( inputState.guessing==0 ) {
						negation=true;
					}
				}
				else if ((LA(1)==LPAREN)) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				match(LPAREN);
				expr=expression(ns,expectedType,functionNs);
				match(RPAREN);
				if ( inputState.guessing==0 ) {
					if(negation){
							    	boolean dirty=false;
									if(!expr.isDirty() && !expr.isLogical()){
										reportError (formatMessage ("err_expr_noLogical",(String)null),
												 nt.getLine());
										dirty=true;
									}
								      try {
								      	expr = new Expression.Negation (expr);
								      	expr.setDirty(dirty);
								      } catch (Exception ex) {
								      	reportError (ex, nt.getLine());
								      }
								}
							
				}
				break;
			}
			case LITERAL_DEFINED:
			{
				def = LT(1);
				match(LITERAL_DEFINED);
				match(LPAREN);
				expr=factor(ns,functionNs);
				match(RPAREN);
				if ( inputState.guessing==0 ) {
					
							      try {
								expr = new Expression.DefinedCheck (expr);
							      } catch (Exception ex) {
								reportError (ex, def.getLine());
							      }
							
				}
				break;
			}
			case LITERAL_INTERLIS:
			case LITERAL_REFSYSTEM:
			case NAME:
			case STRING:
			case LITERAL_PARAMETER:
			case LITERAL_UNDEFINED:
			case HASH:
			case GREATER:
			case LITERAL_PI:
			case LITERAL_LNBASE:
			case GREATERGREATER:
			case LITERAL_AREA:
			case LITERAL_SIGN:
			case LITERAL_METAOBJECT:
			case LITERAL_INSPECTION:
			case LITERAL_THIS:
			case LITERAL_THISAREA:
			case LITERAL_THATAREA:
			case LITERAL_PARENT:
			case BACKSLASH:
			case LITERAL_AGGREGATES:
			case DEC:
			case POSINT:
			case NUMBER:
			{
				expr=factor(ns,functionNs);
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
				recover(ex,_tokenSet_32);
			} else {
			  throw ex;
			}
		}
		return expr;
	}
	
	protected final char  relation(
		int[] lineNumberPar
	) throws RecognitionException, TokenStreamException {
		char code;
		
		Token  eq = null;
		Token  be = null;
		Token  lg = null;
		Token  le = null;
		Token  ge = null;
		Token  ls = null;
		Token  gr = null;
		
		code = '=';
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case EQUALSEQUALS:
			{
				eq = LT(1);
				match(EQUALSEQUALS);
				if ( inputState.guessing==0 ) {
					code = '='; lineNumberPar[0]=eq.getLine();
				}
				break;
			}
			case BANGEQUALS:
			{
				be = LT(1);
				match(BANGEQUALS);
				if ( inputState.guessing==0 ) {
					code = '!'; lineNumberPar[0]=be.getLine();
				}
				break;
			}
			case LESSGREATER:
			{
				lg = LT(1);
				match(LESSGREATER);
				if ( inputState.guessing==0 ) {
					code = '!'; lineNumberPar[0]=lg.getLine();
				}
				break;
			}
			case LESSEQUAL:
			{
				le = LT(1);
				match(LESSEQUAL);
				if ( inputState.guessing==0 ) {
					code = 'l'; lineNumberPar[0]=le.getLine();
				}
				break;
			}
			case GREATEREQUAL:
			{
				ge = LT(1);
				match(GREATEREQUAL);
				if ( inputState.guessing==0 ) {
					code = 'g'; lineNumberPar[0]=ge.getLine();
				}
				break;
			}
			case LESS:
			{
				ls = LT(1);
				match(LESS);
				if ( inputState.guessing==0 ) {
					code = '<'; lineNumberPar[0]=ls.getLine();
				}
				break;
			}
			case GREATER:
			{
				gr = LT(1);
				match(GREATER);
				if ( inputState.guessing==0 ) {
					code = '>'; lineNumberPar[0]=gr.getLine();
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
				recover(ex,_tokenSet_72);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	protected final FunctionCall  functionCall(
		Container ns,Container functionNs
	) throws RecognitionException, TokenStreamException {
		FunctionCall call;
		
		Token  lpar = null;
		
		call = null;
		Function called = null;
		Evaluable arg = null;
		LinkedList args = null;
		FormalArgument       formalArguments[] = null;
		Type       expectedType = null;
		int        curArgument = 0;
		List      nams = new LinkedList();
		int lin = 0;
		
		
		try {      // for error handling
			lin=names2(nams);
			if ( inputState.guessing==0 ) {
				
					      String elementName=null;
					      Container container = null;
					
					      switch (nams.size()) {
					      case 1:
						/* FUNCTION */
						elementName = (String) nams.get(0);
						container = functionNs.getContainerOrSame(AbstractPatternDef.class);
						if (container == null)
						  container = functionNs.getContainerOrSame(Model.class);
						break;
					
					      case 2:
						/* MODEL.FUNCTION */
						container = resolveOrFixModelName(functionNs, (String) nams.get(0), lin);
						elementName = (String) nams.get(1);
						break;
					
					      case 3:
						/* MODEL.TOPIC.FUNCTION */
						container = resolveOrFixTopicName(
							      resolveOrFixModelName(functionNs, (String) nams.get(0), lin),
							      (String) nams.get(1),
							      lin);
						elementName = (String) nams.get(2);
						break;
					
					      default:
						reportError(rsrc.getString("err_weirdFunctionRef"), lin);
						panic();
						break;
					      }
					
					      called = (Function) container.getRealElement(Function.class, elementName);
					      if ((called == null) && (nams.size() == 1)){
						// unqualified name; search also in unqaulified imported models
						Model model = (Model) functionNs.getContainerOrSame(Model.class);
						called = (Function) model.getImportedElement (Function.class, elementName);
					      }
					      if (called == null) {
						reportError(
						  formatMessage("err_functionRef_weird", elementName, container.toString()),
						  lin);
						panic();
					      }
					
			}
			lpar = LT(1);
			match(LPAREN);
			if ( inputState.guessing==0 ) {
				
				args = new LinkedList();
				if (called != null)
				formalArguments = called.getArguments();
				
			}
			if ( inputState.guessing==0 ) {
				
				curArgument = curArgument + 1;
				/* The semantic layer will complain if there are too many arguments,
				just make sure here that the parser does not crash in that case. */
				if ((formalArguments == null) || (curArgument > formalArguments.length))
				expectedType = null;
				else
				expectedType = formalArguments[curArgument - 1].getType();
				
			}
			{
			if ((_tokenSet_83.member(LA(1)))) {
				arg=argument(ns, expectedType,functionNs);
				if ( inputState.guessing==0 ) {
					
					args.add (arg);
					
				}
				{
				_loop869:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						if ( inputState.guessing==0 ) {
							
							curArgument = curArgument + 1;
							/* The semantic layer will complain if there are too many arguments,
							just make sure here that the parser does not crash in that case. */
							if ((formalArguments == null) || (curArgument > formalArguments.length))
							expectedType = null;
							else
							expectedType = formalArguments[curArgument - 1].getType();
							
						}
						arg=argument(ns, expectedType,functionNs);
						if ( inputState.guessing==0 ) {
							
							args.add (arg);
							
						}
					}
					else {
						break _loop869;
					}
					
				} while (true);
				}
			}
			else if ((LA(1)==RPAREN)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				
				try {
				call = new FunctionCall (
				called,
				(Evaluable[]) args.toArray (new Evaluable[args.size()]));
				} catch (Exception ex) {
				reportError (ex, lpar.getLine());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_32);
			} else {
			  throw ex;
			}
		}
		return call;
	}
	
	protected final View  inspection(
		Container container
	) throws RecognitionException, TokenStreamException {
		View view;
		
		Token  decomp = null;
		Token  n1 = null;
		Token  n2 = null;
		
			LinkedList aliases=new LinkedList();
			ViewableAlias decomposedViewable=null;
			boolean areaDecomp=false;
			view=null;
			
		
		try {      // for error handling
			{
			if ((LA(1)==LITERAL_AREA)) {
				match(LITERAL_AREA);
				if ( inputState.guessing==0 ) {
					areaDecomp=true;
				}
			}
			else if ((LA(1)==LITERAL_INSPECTION)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			decomp = LT(1);
			match(LITERAL_INSPECTION);
			match(LITERAL_OF);
			decomposedViewable=renamedViewableRef(container);
			match(POINTSTO);
			n1 = LT(1);
			match(NAME);
			if ( inputState.guessing==0 ) {
				aliases.add(n1.getText());
			}
			{
			_loop915:
			do {
				if ((LA(1)==POINTSTO)) {
					match(POINTSTO);
					n2 = LT(1);
					match(NAME);
					if ( inputState.guessing==0 ) {
						aliases.add(n2.getText());
					}
				}
				else {
					break _loop915;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				
						view= new DecompositionView();
						try{
							String[] aliasv=(String[])aliases.toArray (new String[aliases.size()]);
							AttributeRef attrRef[]=new AttributeRef[aliasv.length];
							Viewable currentView=decomposedViewable.getAliasing();
							AttributeDef attrdef=null;
							for(int i=0;i<aliasv.length;i++){
								if(i>0){
									if(!(attrRef[i-1].getDomain() instanceof CompositionType)){
										reportError (formatMessage ("err_inspection_noStructAttr", aliasv[i-1]
											), n1.getLine());
									}
									currentView=attrRef[i-1].getViewable();
								}
								attrdef=findAttribute(currentView,aliasv[i]);
								if(attrdef==null){
									// no attribute 'name' in 'currentView'
									reportError (formatMessage ("err_attributeRef_unknownAttr", aliasv[i],
										currentView.toString()), n1.getLine());
								}
								attrRef[i]=new AttributeRef(attrdef);
							}
							((DecompositionView) view).setDecomposedAttribute(
								new ObjectPath(decomposedViewable.getAliasing(),attrRef)
								);
							((DecompositionView) view).setAreaDecomposition(areaDecomp);
							((DecompositionView) view).setRenamedViewable(decomposedViewable);
							Table decomposedStruct=buildDecomposedStruct(attrdef,areaDecomp);
							LocalAttribute attrib=new LocalAttribute();
							attrib.setName(decomposedViewable.getName());
							attrib.setDomain(new ObjectType(decomposedStruct));
							view.add(attrib);
						}catch( Exception ex){
							reportError(ex,decomp.getLine());
						}
					
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_84);
			} else {
			  throw ex;
			}
		}
		return view;
	}
	
	protected final PathEl  pathEl(
		Viewable currentViewable,Container context
	) throws RecognitionException, TokenStreamException {
		PathEl el;
		
		Token  kwi = null;
		Token  kwa = null;
		Token  kwp = null;
		Token  n = null;
		
				el=null;
			
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_THIS:
			{
				match(LITERAL_THIS);
				if ( inputState.guessing==0 ) {
					el=new PathElThis(currentViewable);
				}
				break;
			}
			case LITERAL_THISAREA:
			{
				kwi = LT(1);
				match(LITERAL_THISAREA);
				if ( inputState.guessing==0 ) {
					
								// can only be applied to an area inspection view
								if(!(currentViewable instanceof DecompositionView) || !((DecompositionView)currentViewable).isAreaDecomposition()){
									reportError (formatMessage ("err_pathEl_thisareaOnNonAreaInspection",
										currentViewable.toString()), kwi.getLine());
								}
								el=new ThisArea((DecompositionView)currentViewable,false);
							
				}
				break;
			}
			case LITERAL_THATAREA:
			{
				kwa = LT(1);
				match(LITERAL_THATAREA);
				if ( inputState.guessing==0 ) {
					
								// can only be applied to an area inspection view
								if(!(currentViewable instanceof DecompositionView) || !((DecompositionView)currentViewable).isAreaDecomposition()){
									reportError (formatMessage ("err_pathEl_thatareaOnNonAreaInspection",
										currentViewable.toString()), kwa.getLine());
								}
								el=new ThisArea((DecompositionView)currentViewable,true);
							
				}
				break;
			}
			case LITERAL_PARENT:
			{
				kwp = LT(1);
				match(LITERAL_PARENT);
				if ( inputState.guessing==0 ) {
					
								// can only be applied to an inspection view that is not an area inspection
								if(!(currentViewable instanceof DecompositionView) || ((DecompositionView)currentViewable).isAreaDecomposition()){
									reportError (formatMessage ("err_pathEl_parentOnNonInspection",
										currentViewable.toString()), kwp.getLine());
								}
								el=new PathElParent(currentViewable);
							
				}
				break;
			}
			case BACKSLASH:
			{
				el=associationPath(currentViewable);
				if ( inputState.guessing==0 ) {
					// TODO pathEl adapt associationPath
							
				}
				break;
			}
			default:
				if (((LA(1)==NAME||LA(1)==LITERAL_AGGREGATES))&&((isAttributeRef(currentViewable,LT(1).getText()) || LT(1).getText().equals("AGGREGATES")) )) {
					el=attributeRef(currentViewable);
				}
				else if ((LA(1)==NAME)) {
					n = LT(1);
					match(NAME);
					if ( inputState.guessing==0 ) {
						
								AttributeDef refattr=null;
								refattr=findAttribute(currentViewable,n.getText());
								RoleDef oppend=null;
								if(currentViewable instanceof Viewable){
									if(context!=null){
										oppend=currentViewable.findOpposideRole(context,n.getText());
									}else{
										oppend=currentViewable.findOpposideRole(n.getText());
									}
								}
								if(refattr!=null && refattr.getDomainResolvingAliases() instanceof ReferenceType){
									// ReferenceAttribute
									el=new PathElRefAttr(refattr);
								}else if(refattr!=null && refattr.getDomainResolvingAliases() instanceof ObjectType){
									ObjectType ref=(ObjectType)refattr.getDomainResolvingAliases();
									el=new PathElBase(currentViewable,n.getText(),ref.getRef());
								}else if(currentViewable.findRole(n.getText())!=null){
									// currentView is an Association? -> role name
									el=new PathElAssocRole(currentViewable.findRole(n.getText()));
								}else if(oppend!=null){
									// currentView is an AbstractClassDef -> role name
									el=new PathElAbstractClassRole(oppend);
								}else{
									reportError (formatMessage ("err_pathEl_wrongName",
										n.getText(),currentViewable.toString()), n.getLine());
								}
								
					}
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_85);
			} else {
			  throw ex;
			}
		}
		return el;
	}
	
	protected final AssociationPath  associationPath(
		Viewable currentViewable
	) throws RecognitionException, TokenStreamException {
		AssociationPath el;
		
		Token  bs = null;
		Token  roleName = null;
		
				el=null;
			
		
		try {      // for error handling
			bs = LT(1);
			match(BACKSLASH);
			roleName = LT(1);
			match(NAME);
			if ( inputState.guessing==0 ) {
				
							// check if currentViewable is a Class or an Asssociation
							if(!(currentViewable instanceof Table) && !(currentViewable instanceof AssociationDef)){
								// an association path may only be applied to a class or association
								reportError (formatMessage ("err_associationPath_currentIsNotClass",
									currentViewable.toString()), bs.getLine());
							}
							// check if role exists in currentViewable
							RoleDef targetRole=((AbstractClassDef)currentViewable).findOpposideRole(roleName.getText());
							if(targetRole==null){
								// no role with given name
								reportError (formatMessage ("err_associationPath_noRole",
									roleName.getText(),currentViewable.toString()), roleName.getLine());
							}
							el=new AssociationPath(targetRole);
							
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_85);
			} else {
			  throw ex;
			}
		}
		return el;
	}
	
	protected final AbstractAttributeRef  attributeRef(
		Viewable currentViewable
	) throws RecognitionException, TokenStreamException {
		AbstractAttributeRef el;
		
		Token  n = null;
		Token  aggr = null;
		
				long idx;
				el=null;
			
		
		try {      // for error handling
			if ((LA(1)==NAME)) {
				{
				n = LT(1);
				match(NAME);
				{
				if ((LA(1)==LBRACE)) {
					match(LBRACE);
					idx=listIndex();
					match(RBRACE);
					if ( inputState.guessing==0 ) {
						
										AttributeDef attrdef=findAttribute(currentViewable,n.getText());
										if(attrdef==null){
											// no attribute 'name' in 'currentView'
											reportError (formatMessage ("err_attributeRef_unknownAttr", n.getText(),
												currentViewable.toString()), n.getLine());
										}
										Type type=attrdef.getDomainResolvingAliases();
										if(type instanceof CoordType){
											if(idx<=0 || idx>=((CoordType)type).getDimensions().length){
												reportError (formatMessage ("err_attributeRef_axisOutOfBound", Integer.toString(((CoordType)type).getDimensions().length)), n.getLine());
						
											}
											AxisAttributeRef attrref=new AxisAttributeRef(attrdef,(int)idx);
											el=attrref;
										}else if(type instanceof CompositionType){
											StructAttributeRef attrref=new StructAttributeRef(attrdef,idx);
											el=attrref;
										}else{
											// AttributeRef name[] requires a COORD or STRUCTURE attribute in 'currentView'
											reportError (formatMessage ("err_attributeRef_unknownStructAttr", n.getText(),
												currentViewable.toString()), n.getLine());
										}
										
					}
				}
				else if ((_tokenSet_85.member(LA(1)))) {
					if ( inputState.guessing==0 ) {
						
										AttributeDef attrdef=findAttribute(currentViewable,n.getText());
										if(attrdef==null){
											// no attribute 'name' in 'currentView'
											reportError (formatMessage ("err_attributeRef_unknownAttr", n.getText(),
												currentViewable.toString()), n.getLine());
										}
										AttributeRef attrref=new AttributeRef(attrdef);
										el=attrref;
										
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				}
			}
			else if ((LA(1)==LITERAL_AGGREGATES)) {
				aggr = LT(1);
				match(LITERAL_AGGREGATES);
				if ( inputState.guessing==0 ) {
					
								// check if currentView is an Aggregation
								if(!(currentViewable instanceof AggregationView)){
									// no attribute 'name' in 'currentView'
									reportError (formatMessage ("err_attributeRef_noAggregates",
										currentViewable.toString()), aggr.getLine());
								}
								el=new AggregationRef((AggregationView)currentViewable);
								
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_85);
			} else {
			  throw ex;
			}
		}
		return el;
	}
	
	protected final long  listIndex() throws RecognitionException, TokenStreamException {
		long index;
		
		
				index=0;
			
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_FIRST:
			{
				match(LITERAL_FIRST);
				if ( inputState.guessing==0 ) {
					index=StructAttributeRef.eFIRST;
				}
				break;
			}
			case LITERAL_LAST:
			{
				match(LITERAL_LAST);
				if ( inputState.guessing==0 ) {
					index=StructAttributeRef.eLAST;
				}
				break;
			}
			case POSINT:
			{
				index=posInteger();
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
				recover(ex,_tokenSet_86);
			} else {
			  throw ex;
			}
		}
		return index;
	}
	
	protected final Evaluable  argument(
		Container ns,Type expectedType,Container functionNs
	) throws RecognitionException, TokenStreamException {
		Evaluable arg;
		
		
			Viewable ref=null;
			Viewable restrictedTo=null;
			boolean classRequired=(expectedType instanceof ClassType);
			arg=null;
			ch.interlis.ili2c.metamodel.Objects objs=null;
			
		
		try {      // for error handling
			if ((_tokenSet_72.member(LA(1)))) {
				arg=expression(ns,expectedType,functionNs);
			}
			else if ((LA(1)==LITERAL_ALL)) {
				{
				match(LITERAL_ALL);
				if ( inputState.guessing==0 ) {
					
								 Viewable context=(Viewable)ns.getContainerOrSame(Viewable.class);
								 arg=objs=new ch.interlis.ili2c.metamodel.Objects(context);
								
				}
				{
				if ((LA(1)==LPAREN)) {
					match(LPAREN);
					{
					if ((_tokenSet_7.member(LA(1)))) {
						ref=viewableRef(ns);
					}
					else if ((LA(1)==LITERAL_ANYCLASS)) {
						match(LITERAL_ANYCLASS);
						if ( inputState.guessing==0 ) {
							ref=modelInterlis.ANYCLASS;
						}
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
					if ( inputState.guessing==0 ) {
						
							       		objs.setBase(ref);
							
					}
					{
					if (!(ref instanceof AbstractClassDef))
					  throw new SemanticException("ref instanceof AbstractClassDef");
					{
					if ((LA(1)==LITERAL_RESTRICTION)) {
						match(LITERAL_RESTRICTION);
						match(LPAREN);
						restrictedTo=classOrAssociationRef(ns);
						if ( inputState.guessing==0 ) {
							objs.addRestrictedTo(restrictedTo);
						}
						{
						_loop877:
						do {
							if ((LA(1)==SEMI)) {
								match(SEMI);
								restrictedTo=classOrAssociationRef(ns);
								if ( inputState.guessing==0 ) {
									objs.addRestrictedTo(restrictedTo);
								}
							}
							else {
								break _loop877;
							}
							
						} while (true);
						}
						match(RPAREN);
					}
					else if ((LA(1)==RPAREN)) {
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
					}
					match(RPAREN);
				}
				else if ((LA(1)==RPAREN||LA(1)==COMMA)) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
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
				recover(ex,_tokenSet_68);
			} else {
			  throw ex;
			}
		}
		return arg;
	}
	
	protected final FormalArgument  formalArgument(
		Container scope, int line,ArrayList formalArgs
	) throws RecognitionException, TokenStreamException {
		FormalArgument arg;
		
		Token  n = null;
		arg=null;
			Type domain=null;
			
		
		try {      // for error handling
			n = LT(1);
			match(NAME);
			match(COLON);
			domain=argumentType(scope,line,formalArgs);
			if ( inputState.guessing==0 ) {
				
							arg=new FormalArgument(n.getText(),domain);
						
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_77);
			} else {
			  throw ex;
			}
		}
		return arg;
	}
	
	protected final Type  argumentType(
		Container scope,int line,ArrayList formalArgs
	) throws RecognitionException, TokenStreamException {
		Type domain;
		
		
				Viewable ref=null;
				domain=null;
				boolean objects=false;
				ObjectType ot=null;
				AbstractClassDef restrictedTo=null;
			
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_INTERLIS:
			case LITERAL_REFSYSTEM:
			case NAME:
			case LPAREN:
			case STRING:
			case LITERAL_OID:
			case LITERAL_CLASS:
			case LITERAL_STRUCTURE:
			case LITERAL_ATTRIBUTE:
			case LITERAL_MANDATORY:
			case LITERAL_BAG:
			case LITERAL_LIST:
			case LITERAL_ANYSTRUCTURE:
			case LITERAL_REFERENCE:
			case LITERAL_URI:
			case LITERAL_NAME:
			case LITERAL_MTEXT:
			case LITERAL_TEXT:
			case LITERAL_ALL:
			case LITERAL_HALIGNMENT:
			case LITERAL_VALIGNMENT:
			case LITERAL_BOOLEAN:
			case LITERAL_NUMERIC:
			case LITERAL_FORMAT:
			case LITERAL_DATE:
			case LITERAL_TIMEOFDAY:
			case LITERAL_DATETIME:
			case LITERAL_COORD:
			case LITERAL_MULTICOORD:
			case LITERAL_BLACKBOX:
			case LITERAL_DIRECTED:
			case LITERAL_POLYLINE:
			case LITERAL_MULTIPOLYLINE:
			case LITERAL_SURFACE:
			case LITERAL_MULTISURFACE:
			case LITERAL_AREA:
			case LITERAL_MULTIAREA:
			case LITERAL_SIGN:
			case LITERAL_METAOBJECT:
			case DEC:
			case POSINT:
			case NUMBER:
			{
				domain=attrTypeDef(scope,true,null,line,formalArgs);
				break;
			}
			case LITERAL_OBJECTS:
			case LITERAL_OBJECT:
			{
				{
				if ((LA(1)==LITERAL_OBJECT)) {
					match(LITERAL_OBJECT);
					if ( inputState.guessing==0 ) {
						objects=false;
					}
				}
				else if ((LA(1)==LITERAL_OBJECTS)) {
					match(LITERAL_OBJECTS);
					if ( inputState.guessing==0 ) {
						objects=true;
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				match(LITERAL_OF);
				{
				if ((_tokenSet_7.member(LA(1)))) {
					ref=viewableRef(scope);
				}
				else if ((LA(1)==LITERAL_ANYCLASS)) {
					match(LITERAL_ANYCLASS);
					if ( inputState.guessing==0 ) {
						ref=modelInterlis.ANYCLASS;
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				if ( inputState.guessing==0 ) {
					
						       		domain=ot=new ObjectType(ref,objects);
						
				}
				{
				if (!(ref instanceof AbstractClassDef))
				  throw new SemanticException("ref instanceof AbstractClassDef");
				{
				if ((LA(1)==LITERAL_RESTRICTION)) {
					match(LITERAL_RESTRICTION);
					match(LPAREN);
					restrictedTo=classOrAssociationRef(scope);
					if ( inputState.guessing==0 ) {
						ot.addRestrictedTo(restrictedTo);
					}
					{
					_loop890:
					do {
						if ((LA(1)==SEMI)) {
							match(SEMI);
							restrictedTo=classOrAssociationRef(scope);
							if ( inputState.guessing==0 ) {
								ot.addRestrictedTo(restrictedTo);
							}
						}
						else {
							break _loop890;
						}
						
					} while (true);
					}
					match(RPAREN);
				}
				else if ((LA(1)==SEMI||LA(1)==RPAREN||LA(1)==EXPLANATION)) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				}
				break;
			}
			case LITERAL_ENUMVAL:
			{
				match(LITERAL_ENUMVAL);
				if ( inputState.guessing==0 ) {
					domain=new EnumValType(true);
				}
				break;
			}
			case LITERAL_ENUMTREEVAL:
			{
				match(LITERAL_ENUMTREEVAL);
				if ( inputState.guessing==0 ) {
					domain=new EnumValType(false);
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
				recover(ex,_tokenSet_87);
			} else {
			  throw ex;
			}
		}
		return domain;
	}
	
	protected final View  formationDef(
		Container container
	) throws RecognitionException, TokenStreamException {
		View view;
		
		
			view=null;
			
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_PROJECTION:
			{
				view=projection(container);
				break;
			}
			case LITERAL_JOIN:
			{
				view=join(container);
				break;
			}
			case LITERAL_UNION:
			{
				view=union(container);
				break;
			}
			case LITERAL_AGGREGATION:
			{
				view=aggregation(container);
				break;
			}
			case LITERAL_AREA:
			case LITERAL_INSPECTION:
			{
				view=inspection(container);
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
				recover(ex,_tokenSet_88);
			} else {
			  throw ex;
			}
		}
		return view;
	}
	
	protected final View  viewRef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		View view;
		
		
		view=null;
		Viewable viewable;
		
		
		try {      // for error handling
			viewable=viewableRef(scope);
			if ( inputState.guessing==0 ) {
				view=(View)viewable;
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_88);
			} else {
			  throw ex;
			}
		}
		return view;
	}
	
	protected final void baseExtensionDef(
		Viewable scope
	) throws RecognitionException, TokenStreamException {
		
		Token  baseName = null;
		Token  by = null;
		Token  cm = null;
		
			Viewable base=null;
			ViewableAlias ext1=null;
			ViewableAlias ext2=null;
		
		
		try {      // for error handling
			match(LITERAL_BASE);
			baseName = LT(1);
			match(NAME);
			match(LITERAL_EXTENDED);
			by = LT(1);
			match(LITERAL_BY);
			ext1=renamedViewableRef(scope);
			if ( inputState.guessing==0 ) {
				
					base=getBaseViewable(getBaseViewableProxyAttr(scope,baseName.getText(),baseName.getLine()));
				if (base == null)
				{
				reportError(
				formatMessage ("err_viewable_noSuchBase",
				baseName.getText(), scope.toString()),
				baseName.getLine());
				}
					    AttributeDef exstAttr =  (AttributeDef)scope.getRealElement (AttributeDef.class, ext1.getName());
					    if(exstAttr!=null){
					    	reportError (formatMessage ("err_attrNameInSameViewable",
				scope.toString(), ext1.getName()),
				by.getLine());
					    }
					    // check ext1 is a extension of base
					    if(!ext1.getAliasing().isExtending(base)){
					    	reportError (formatMessage ("err_viewext_notBase",
				ext1.getName(), baseName.getText()),
				by.getLine());
					    }
					    try{
						LocalAttribute attrib=new LocalAttribute();
						attrib.setName(ext1.getName());
						attrib.setDomain(new ObjectType(ext1.getAliasing()));
						scope.add(attrib);
					    }catch( Exception ex){
					    	reportError(ex,by.getLine());
					    }
					
			}
			{
			_loop924:
			do {
				if ((LA(1)==COMMA)) {
					cm = LT(1);
					match(COMMA);
					ext2=renamedViewableRef(scope);
					if ( inputState.guessing==0 ) {
						
							    AttributeDef exstAttr =  (AttributeDef)scope.getRealElement (AttributeDef.class, ext2.getName());
							    if(exstAttr!=null){
							    	reportError (formatMessage ("err_attrNameInSameViewable",
						scope.toString(), ext2.getName()),
						cm.getLine());
							    }
							    // check ext2 is a extension of base
							    if(!ext2.getAliasing().isExtending(base)){
							    	reportError (formatMessage ("err_viewext_notBase",
						ext2.getName(), baseName.getText()),
						cm.getLine());
							    }
							    try{
								LocalAttribute attrib=new LocalAttribute();
								attrib.setName(ext2.getName());
								attrib.setDomain(new ObjectType(ext2.getAliasing()));
								scope.add(attrib);
							    }catch( Exception ex){
							    	reportError(ex,cm.getLine());
							    }
							
					}
				}
				else {
					break _loop924;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_88);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final Selection  selection(
		Viewable view,Container functionNs
	) throws RecognitionException, TokenStreamException {
		Selection sel;
		
		Token  tok = null;
		
				sel = null;
				Evaluable logex = null;
				Viewable ref;
				LinkedList base=new LinkedList();
			
		
		try {      // for error handling
			tok = LT(1);
			match(LITERAL_WHERE);
			logex=expression(view, /* expectedType */ predefinedBooleanType,functionNs);
			if ( inputState.guessing==0 ) {
				
							if(!logex.isDirty() && !logex.isLogical()){
								reportError (formatMessage ("err_expr_noLogical",(String)null),
										 tok.getLine());
								logex.setDirty(true);
							}
							sel = new ExpressionSelection(view, logex);
						
			}
			match(SEMI);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_89);
			} else {
			  throw ex;
			}
		}
		return sel;
	}
	
	protected final void viewAttributes(
		Viewable view
	) throws RecognitionException, TokenStreamException {
		
		Token  all = null;
		Token  v = null;
		Token  n = null;
		
				int mods=0;
				Evaluable f=null;
			
		
		try {      // for error handling
			{
			if ((LA(1)==LITERAL_ATTRIBUTE)) {
				match(LITERAL_ATTRIBUTE);
			}
			else if ((_tokenSet_90.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			_loop931:
			do {
				if ((LA(1)==LITERAL_ALL)) {
					all = LT(1);
					match(LITERAL_ALL);
					match(LITERAL_OF);
					v = LT(1);
					match(NAME);
					match(SEMI);
					if ( inputState.guessing==0 ) {
						
							AttributeDef allOfAttr=null;
						{
						Viewable attrScope = (Viewable) view.getContainerOrSame (Viewable.class);
						if (attrScope == null)
						reportInternalError (v.getLine());
						else
						{
							if(attrScope instanceof UnionView){
						reportError(
						formatMessage ("err_unionView_illegalallof",
						v.getText()),
						v.getLine());
							}
							allOfAttr=getBaseViewableProxyAttr(attrScope,v.getText(),v.getLine());
						if (allOfAttr == null)
						{
						reportError(
						formatMessage ("err_viewable_noSuchBase",
						v.getText(), attrScope.toString()),
						v.getLine());
						}
						}
						}
						
						if (allOfAttr != null)
						{
						ObjectType proxyType=(ObjectType)allOfAttr.getDomain();
						proxyType.setAllOf(true);
							  Viewable allOf=getBaseViewable(allOfAttr);
						Iterator attrs = allOf.getAttributes ();
						while (attrs.hasNext ())
						{
						AttributeDef attr = (AttributeDef) attrs.next();
							    AttributeDef exstAttr =  (AttributeDef)view.getRealElement (AttributeDef.class, attr.getName());
							    if(exstAttr!=null){
							    	reportError (formatMessage ("err_attrNameInSameViewable",
						view.toString(), attr.getName()),
						v.getLine());
							    }
							    if(attr.isAbstract() && !view.isAbstract()){
							    	reportError (formatMessage ("err_view_abstractAttr",
						view.toString(), attr.getName()),
						v.getLine());
							    }
						LocalAttribute pa = new LocalAttribute ();
						pa.setGeneratedByAllOf(true);
						try {
						ObjectPath path;
						AttributeRef[] pathItems;
						
						pathItems = new AttributeRef[]
						{
						new AttributeRef(attr)
						};
						path = new ObjectPath(allOf, pathItems);
						
						pa.setName (attr.getName ());
						pa.setDomain (attr.getDomain ());
						pa.setBasePaths (new ObjectPath[] { path });
						view.add (pa);
						} catch (Exception ex) {
						reportError (ex, all.getLine ());
						}
						}
						}
						
					}
				}
				else {
					boolean synPredMatched930 = false;
					if (((LA(1)==NAME))) {
						int _m930 = mark();
						synPredMatched930 = true;
						inputState.guessing++;
						try {
							{
							match(NAME);
							mods=properties(ch.interlis.ili2c.metamodel.Properties.eABSTRACT
		|ch.interlis.ili2c.metamodel.Properties.eEXTENDED
		|ch.interlis.ili2c.metamodel.Properties.eFINAL
		|ch.interlis.ili2c.metamodel.Properties.eTRANSIENT
		);
							match(COLONEQUALS);
							}
						}
						catch (RecognitionException pe) {
							synPredMatched930 = false;
						}
						rewind(_m930);
inputState.guessing--;
					}
					if ( synPredMatched930 ) {
						n = LT(1);
						match(NAME);
						mods=properties(ch.interlis.ili2c.metamodel.Properties.eABSTRACT
		|ch.interlis.ili2c.metamodel.Properties.eEXTENDED
		|ch.interlis.ili2c.metamodel.Properties.eFINAL
		|ch.interlis.ili2c.metamodel.Properties.eTRANSIENT
		);
						match(COLONEQUALS);
						f=factor(view,view);
						if ( inputState.guessing==0 ) {
							
										AttributeDef overriding = findOverridingAttribute (
											view, mods, n.getText(), n.getLine());
										Type overridingDomain = null;
										if (overriding != null){
											overridingDomain = overriding.getDomainResolvingAliases();
										}
										LocalAttribute attrib=new LocalAttribute();
										try {
											attrib.setName(n.getText());
											attrib.setAbstract((mods & ch.interlis.ili2c.metamodel.Properties.eABSTRACT) != 0);
											attrib.setTransient((mods & ch.interlis.ili2c.metamodel.Properties.eTRANSIENT) != 0);
											// always final, but don't set, so that ili-export is the same as import
											// attrib.setFinal(true);
											if(overridingDomain!=null){
												attrib.setDomain((Type)overridingDomain.clone());
												attrib.setTypeProxy(true);
											}else{
												// type derived from the follwoing constructs
												// Factor = ( ObjectOrAttributePath
												// | ( Inspection | 'INSPECTION' Inspection-ViewableRef )
												// [ 'OF' ObjectOrAttributePath ]
												// | FunctionCall
												// | 'PARAMETER' [ Model-Name '.' ] RunTimeParameter-Name
												// | Constant ).				
											}
											attrib.setBasePaths (new Evaluable[] { f });
										} catch (Exception ex) {
											reportError(ex, n.getLine());
										}
										try {
											view.add(attrib);
											attrib.setExtending(overriding);
										} catch (Exception ex) {
											reportError(ex, n.getLine());
										}
									
						}
						match(SEMI);
					}
					else if ((LA(1)==NAME||LA(1)==LITERAL_CONTINUOUS||LA(1)==LITERAL_SUBDIVISION)) {
						attributeDef(view);
					}
					else {
						break _loop931;
					}
					}
				} while (true);
				}
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_22);
				} else {
				  throw ex;
				}
			}
		}
		
	protected final Projection  projection(
		Container container
	) throws RecognitionException, TokenStreamException {
		Projection view;
		
		Token  projToken = null;
		
			view=null;
			ViewableAlias base=null;
			
		
		try {      // for error handling
			projToken = LT(1);
			match(LITERAL_PROJECTION);
			match(LITERAL_OF);
			base=renamedViewableRef(container);
			if ( inputState.guessing==0 ) {
				
							view=new Projection();
							try{
								view.setSelected(base);
								LocalAttribute attrib=new LocalAttribute();
								attrib.setName(base.getName());
								attrib.setDomain(new ObjectType(base.getAliasing()));
								view.add(attrib);
							} catch (Exception ex) {
							  reportError(ex, projToken.getLine());
							}
						
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_70);
			} else {
			  throw ex;
			}
		}
		return view;
	}
	
	protected final View  join(
		Container container
	) throws RecognitionException, TokenStreamException {
		View view;
		
		Token  join = null;
		
			ViewableAlias viewable = null;
			LinkedList aliases=new LinkedList();
			view=null;
			
		
		try {      // for error handling
			join = LT(1);
			match(LITERAL_JOIN);
			match(LITERAL_OF);
			viewable=renamedViewableRef(container);
			if ( inputState.guessing==0 ) {
				aliases.add(viewable);
			}
			{
			int _cnt906=0;
			_loop906:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					viewable=renamedViewableRef(container);
					{
					if ((LA(1)==LPAREN)) {
						match(LPAREN);
						match(LITERAL_OR);
						match(LITERAL_NULL);
						match(RPAREN);
						if ( inputState.guessing==0 ) {
							viewable.setIncludeNull(true);
						}
					}
					else if ((LA(1)==SEMI||LA(1)==COMMA)) {
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
					if ( inputState.guessing==0 ) {
						aliases.add(viewable);
					}
				}
				else {
					if ( _cnt906>=1 ) { break _loop906; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt906++;
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				
							view = new JoinView();
							try {
							  ((JoinView) view).setJoining((ViewableAlias[]) aliases.toArray (new ViewableAlias[aliases.size()]));
							  for(int i=0;i<aliases.size();i++){
							  	ViewableAlias base=(ViewableAlias)aliases.get(i);
								    AttributeDef exstAttr =  (AttributeDef)view.getRealElement (AttributeDef.class, base.getName());
								    if(exstAttr!=null){
									reportError (formatMessage ("err_attrNameInSameViewable",
											  view.toString(), base.getName()),
									   join.getLine());
								    }
								LocalAttribute attrib=new LocalAttribute();
								attrib.setName(base.getName());
								attrib.setDomain(new ObjectType(base.getAliasing()));
								view.add(attrib);
							  }
							} catch (Exception ex) {
							  reportError(ex, join.getLine());
							}
						
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_70);
			} else {
			  throw ex;
			}
		}
		return view;
	}
	
	protected final View  union(
		Container container
	) throws RecognitionException, TokenStreamException {
		View view;
		
		Token  union = null;
		
			ViewableAlias viewable = null;
			LinkedList aliases=new LinkedList();
			view=null;
			
		
		try {      // for error handling
			union = LT(1);
			match(LITERAL_UNION);
			match(LITERAL_OF);
			viewable=renamedViewableRef(container);
			if ( inputState.guessing==0 ) {
				aliases.add(viewable);
			}
			{
			int _cnt909=0;
			_loop909:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					viewable=renamedViewableRef(container);
					if ( inputState.guessing==0 ) {
						aliases.add(viewable);
					}
				}
				else {
					if ( _cnt909>=1 ) { break _loop909; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt909++;
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				
							view = new UnionView();
							try {
							  ((UnionView) view).setUnited((ViewableAlias[]) aliases.toArray (new ViewableAlias[aliases.size()]));
							  for(int i=0;i<aliases.size();i++){
							  	ViewableAlias base=(ViewableAlias)aliases.get(i);
								    AttributeDef exstAttr =  (AttributeDef)view.getRealElement (AttributeDef.class, base.getName());
								    if(exstAttr!=null){
									reportError (formatMessage ("err_attrNameInSameViewable",
											  view.toString(), base.getName()),
									   union.getLine());
								    }
								LocalAttribute attrib=new LocalAttribute();
								attrib.setName(base.getName());
								attrib.setDomain(new ObjectType(base.getAliasing()));
								view.add(attrib);
							  }
							} catch (Exception ex) {
							  reportError(ex, union.getLine());
							}
						
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_70);
			} else {
			  throw ex;
			}
		}
		return view;
	}
	
	protected final AggregationView  aggregation(
		Container container
	) throws RecognitionException, TokenStreamException {
		AggregationView view;
		
		Token  agg = null;
		Token  eq = null;
		
			ViewableAlias base = null;
			UniqueEl cols=null;
			view=null;
			
		
		try {      // for error handling
			agg = LT(1);
			match(LITERAL_AGGREGATION);
			match(LITERAL_OF);
			base=renamedViewableRef(container);
			if ( inputState.guessing==0 ) {
				
				view=new AggregationView(base);
								try{
									LocalAttribute attrib=new LocalAttribute();
									attrib.setName(base.getName());
									attrib.setDomain(new ObjectType(base.getAliasing()));
									view.add(attrib);
								} catch (Exception ex) {
								  reportError(ex, agg.getLine());
								}
							
			}
			{
			if ((LA(1)==LITERAL_ALL)) {
				match(LITERAL_ALL);
			}
			else if ((LA(1)==LITERAL_EQUAL)) {
				eq = LT(1);
				match(LITERAL_EQUAL);
				match(LPAREN);
				cols=uniqueEl(view,container);
				match(RPAREN);
				if ( inputState.guessing==0 ) {
					
					view.setEqual(cols);
								
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_70);
			} else {
			  throw ex;
			}
		}
		return view;
	}
	
	protected final Graphic  graphicRef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		Graphic graph;
		
		
		List      nams = new LinkedList();
		graph = null;
		String   graphicName = null;
		int lin = 0;
		
		
		try {      // for error handling
			lin=names2(nams);
			if ( inputState.guessing==0 ) {
				
				Model model;
				Topic topic;
				
				switch (nams.size()) {
				case 1:
				model = (Model) scope.getContainerOrSame (Model.class);
				topic = (Topic) scope.getContainerOrSame (Topic.class);
				graphicName = (String) nams.get(0);
				break;
				
				case 2:
				model = resolveOrFixModelName (scope, (String) nams.get(0), lin);
				topic = null;
				graphicName = (String) nams.get(1);
				break;
				
				case 3:
				model = resolveOrFixModelName (scope, (String) nams.get(0), lin);
				topic = resolveOrFixTopicName (model, (String) nams.get(1), lin);
				graphicName = (String) nams.get(2);
				break;
				
				default:
				reportError (rsrc.getString("err_graphicRef_weird"), lin);
				model = resolveModelName(scope, (String) nams.get(0));
				topic = null;
				if (model == null)
				model = (Model) scope.getContainerOrSame(Model.class);
				graphicName = (String) nams.get(nams.size() - 1);
				break;
				}
				
				graph = null;
				if (topic != null)
				graph = (Graphic) topic.getRealElement (Graphic.class, graphicName);
				
				if ((graph == null) && (topic == null))
				graph = (Graphic) model.getRealElement(Graphic.class, graphicName);
				if ((graph == null) && (nams.size() == 1))
				graph = (Graphic) model.getImportedElement(Graphic.class, graphicName);
				
				if (graph == null)
				{
				if (topic == null)
				reportError(
				formatMessage ("err_graphicRef_notInModel", graphicName, model.toString()),
				lin);
				else
				reportError(
				formatMessage ("err_graphicRef_notInModelOrTopic", graphicName,
				topic.toString(), model.toString()),
				lin);
				
				try {
				graph = new Graphic();
				graph.setName (graphicName);
				if (topic == null)
				model.add (graph);
				else
				topic.add (graph);
				} catch (Exception ex) {
				panic();
				}
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_91);
			} else {
			  throw ex;
			}
		}
		return graph;
	}
	
	protected final void drawingRule(
		Graphic graph
	) throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
		int mods = 0;
		Table signTab = null;
		SignAttribute attr = null;
		SignAttribute overriding = null;
		boolean declaredExtended = false;
		SignInstruction instruct = null;
		List instructs = null;
		Viewable basedOn=graph.getBasedOn();
		
		
		try {      // for error handling
			n = LT(1);
			match(NAME);
			mods=properties(ch.interlis.ili2c.metamodel.Properties.eABSTRACT
			|ch.interlis.ili2c.metamodel.Properties.eEXTENDED
			|ch.interlis.ili2c.metamodel.Properties.eFINAL);
			{
			if ((LA(1)==LITERAL_OF)) {
				match(LITERAL_OF);
				signTab=classRef(graph);
			}
			else if ((LA(1)==COLON)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(COLON);
			if ( inputState.guessing==0 ) {
				
				declaredExtended = (mods & ch.interlis.ili2c.metamodel.Properties.eEXTENDED) != 0;
				overriding =  (SignAttribute) graph.getRealElement (
				SignAttribute.class, n.getText());
				
				if ((overriding == null) && declaredExtended)
				{
				if (graph.getRealExtending() == null)
				{
				reportError (formatMessage ("err_signAttr_extendedInRootGraphic",
				n.getText(),
				graph.toString()),
				n.getLine());
				}
				else
				{
				reportError (formatMessage ("err_signAttr_nothingToExtend",
				n.getText(),
				graph.getRealExtending().toString()),
				n.getLine());
				}
				}
				
				if ((overriding != null)
				&& (overriding.getContainer(Graphic.class) == graph))
				{
				reportError (formatMessage ("err_signAttr_inSameGraphic",
				graph.toString(),
				n.getText()),
				n.getLine());
				}
				else if ((overriding != null) && !declaredExtended)
				{
				reportError (formatMessage ("err_signAttr_extendedWithoutDecl",
				n.getText(),
				graph.toString(),
				overriding.toString()),
				n.getLine());
				}
				
			}
			if ( inputState.guessing==0 ) {
				
				attr = new SignAttribute ();
				try {
				attr.setName (n.getText());
				attr.setExtending (overriding);
					attr.setAbstract((mods & ch.interlis.ili2c.metamodel.Properties.eABSTRACT)!=0);
				graph.add (attr);
				} catch (Exception ex) {
				reportError (ex, n.getLine());
				panic ();
				}
				
				try {
				if (signTab == null)
				{
				if (overriding != null)
				signTab = overriding.getGenerating ();
				else
				{
				reportError (formatMessage ("err_signAttr_ofOmitted",
				attr.toString()),
				n.getLine());
				}
				}
				attr.setGenerating (signTab);
				} catch (Exception ex) {
				reportError (ex, n.getLine());
				}
				
			}
			instruct=condSigParamAssignment(graph, signTab);
			if ( inputState.guessing==0 ) {
				
				instructs = new LinkedList();
				if (instruct != null)
				instructs.add (instruct);
				
			}
			{
			_loop943:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					instruct=condSigParamAssignment(graph, signTab);
					if ( inputState.guessing==0 ) {
						
						if (instruct != null)
						instructs.add (instruct);
						
					}
				}
				else {
					break _loop943;
				}
				
			} while (true);
			}
			match(SEMI);
			if ( inputState.guessing==0 ) {
				
				try {
				attr.setInstructions (
				(SignInstruction[]) instructs.toArray (new SignInstruction[instructs.size()])
				);
				} catch (Exception ex) {
				reportError (ex, n.getLine());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_28);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final SignInstruction  condSigParamAssignment(
		Graphic graph,  Table signTab
	) throws RecognitionException, TokenStreamException {
		SignInstruction instruct;
		
		Token  tok = null;
		
		Evaluable            restrictor = null;
		List                 paramAssignments = null;
		ParameterAssignment  assign = null;
		Viewable basedOn=graph.getBasedOn();
		
		instruct = null;
		
		
		try {      // for error handling
			{
			if ((LA(1)==LITERAL_WHERE)) {
				tok = LT(1);
				match(LITERAL_WHERE);
				restrictor=expression(basedOn, /* expectedType */ predefinedBooleanType,graph);
				if ( inputState.guessing==0 ) {
					
								if(!restrictor.isDirty() && !restrictor.isLogical()){
									reportError (formatMessage ("err_expr_noLogical",(String)null),
											 tok.getLine());
									restrictor.setDirty(true);
								}
					
				}
			}
			else if ((LA(1)==LPAREN)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(LPAREN);
			assign=sigParamAssignment(graph, signTab);
			if ( inputState.guessing==0 ) {
				
				paramAssignments = new LinkedList ();
				if (assign != null)
				paramAssignments.add (assign);
				
			}
			{
			_loop947:
			do {
				if ((LA(1)==SEMI)) {
					match(SEMI);
					assign=sigParamAssignment(graph, signTab);
					if ( inputState.guessing==0 ) {
						
						if (assign != null)
						paramAssignments.add (assign);
						
					}
				}
				else {
					break _loop947;
				}
				
			} while (true);
			}
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				
				instruct = new SignInstruction (
				restrictor,
				(ParameterAssignment[]) paramAssignments.toArray (
				new ParameterAssignment[paramAssignments.size()])
				);
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_92);
			} else {
			  throw ex;
			}
		}
		return instruct;
	}
	
	protected final ParameterAssignment  sigParamAssignment(
		Graphic graph, Table signTab
	) throws RecognitionException, TokenStreamException {
		ParameterAssignment assign;
		
		Token  parm = null;
		Token  doteq = null;
		
		List assignments = null;
		Parameter assignedParam = null;
		Evaluable value = null;
		assign = null;
		Type   expectedType = null;
		FunctionCall dummyFunc;
		FunctionCall fcall;
		MetaObject metaObj=null;
		Viewable basedOn=graph.getBasedOn();
		
		
		try {      // for error handling
			parm = LT(1);
			match(NAME);
			doteq = LT(1);
			match(COLONEQUALS);
			if ( inputState.guessing==0 ) {
				
				assignedParam = (Parameter) signTab.getRealElement(Parameter.class,
				parm.getText());
				if (assignedParam == null)
				{
				reportError (formatMessage ("err_parameter_unknownInSignTable",
				parm.getText(),
				signTab.toString()),
				parm.getLine());
				}else{
				expectedType = Type.findReal (assignedParam.getType());
					}
				
			}
			{
			switch ( LA(1)) {
			case LITERAL_ACCORDING:
			{
				value=conditionalExpression(graph, expectedType,(expectedType instanceof MetaobjectType ? ((MetaobjectType)expectedType).getReferred() : null));
				break;
			}
			case LCURLY:
			{
				match(LCURLY);
				metaObj=metaObjectRef(graph,((MetaobjectType)expectedType).getReferred());
				match(RCURLY);
				if ( inputState.guessing==0 ) {
					value=new Constant.ReferenceToMetaObject(metaObj);
						
				}
				break;
			}
			case LITERAL_INTERLIS:
			case LITERAL_REFSYSTEM:
			case NAME:
			case STRING:
			case LITERAL_PARAMETER:
			case LITERAL_UNDEFINED:
			case HASH:
			case GREATER:
			case LITERAL_PI:
			case LITERAL_LNBASE:
			case GREATERGREATER:
			case LITERAL_AREA:
			case LITERAL_SIGN:
			case LITERAL_METAOBJECT:
			case LITERAL_INSPECTION:
			case LITERAL_THIS:
			case LITERAL_THISAREA:
			case LITERAL_THATAREA:
			case LITERAL_PARENT:
			case BACKSLASH:
			case LITERAL_AGGREGATES:
			case DEC:
			case POSINT:
			case NUMBER:
			{
				value=factor(basedOn,graph);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				
				try {
				assign = new ParameterAssignment (assignedParam, value);
				} catch (Exception ex) {
				reportError (ex, doteq.getLine());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_77);
			} else {
			  throw ex;
			}
		}
		return assign;
	}
	
	protected final ConditionalExpression  conditionalExpression(
		Graphic graph, Type expectedType,Table metaobjectclass
	) throws RecognitionException, TokenStreamException {
		ConditionalExpression condex;
		
		
		ObjectPath attrPath = null;
		List items = null;
		ConditionalExpression.Condition cond = null;
		Viewable basedOn=graph.getBasedOn();
		
		condex = null;
		
		
		try {      // for error handling
			match(LITERAL_ACCORDING);
			attrPath=attributePath(basedOn,graph.getContainer());
			match(LPAREN);
			cond=enumAssignment(graph, expectedType,metaobjectclass);
			if ( inputState.guessing==0 ) {
				
				items = new LinkedList();
				if (cond != null)
				items.add (cond);
				
			}
			{
			_loop952:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					cond=enumAssignment(graph, expectedType,metaobjectclass);
					if ( inputState.guessing==0 ) {
						
						if (cond != null)
						items.add (cond);
						
					}
				}
				else {
					break _loop952;
				}
				
			} while (true);
			}
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				
				condex = new ConditionalExpression (
				null /* TODO should be attrPath */ ,
				(ConditionalExpression.Condition[]) items.toArray (
				new ConditionalExpression.Condition[items.size()]));
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_77);
			} else {
			  throw ex;
			}
		}
		return condex;
	}
	
	protected final ConditionalExpression.Condition  enumAssignment(
		Graphic graph, Type expectedType,Table metaobjectclass
	) throws RecognitionException, TokenStreamException {
		ConditionalExpression.Condition cond;
		
		Token  wh = null;
		
		Constant cnst = null;
		Constant.EnumConstOrRange range = null;
		cond = null;
		MetaObject metaObj=null;
		
		
		try {      // for error handling
			{
			if ((_tokenSet_93.member(LA(1)))) {
				cnst=constant(graph);
			}
			else if ((LA(1)==LCURLY)) {
				match(LCURLY);
				metaObj=metaObjectRef(graph,metaobjectclass);
				match(RCURLY);
				if ( inputState.guessing==0 ) {
					
								cnst=new Constant.ReferenceToMetaObject(metaObj);
							
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			wh = LT(1);
			match(LITERAL_WHEN);
			match(LITERAL_IN);
			range=enumRange();
			if ( inputState.guessing==0 ) {
				
				try {
				cond = new ConditionalExpression.Condition (cnst, range);
				} catch (Exception ex) {
				reportError (ex, wh.getLine());
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_68);
			} else {
			  throw ex;
			}
		}
		return cond;
	}
	
	protected final Constant.EnumConstOrRange  enumRange() throws RecognitionException, TokenStreamException {
		Constant.EnumConstOrRange rangeOrEnum;
		
		Token  ddot = null;
		
		rangeOrEnum = null;
		Constant.Enumeration from = null;
		Constant.Enumeration to = null;
		String[] commonPrefix = null;
		
		
		try {      // for error handling
			from=enumerationConst();
			{
			if ((LA(1)==DOTDOT)) {
				ddot = LT(1);
				match(DOTDOT);
				to=enumerationConst();
				if ( inputState.guessing==0 ) {
					
					/* length of "from" and "to" must be the same */
					if (from.getValue().length != to.getValue().length)
					{
					rangeOrEnum = from;
					reportError (formatMessage ("err_enumRange_notSameLength",
					rangeOrEnum.toString(),
					to.toString()),
					ddot.getLine());
					}
					else
					{
					commonPrefix = new String[from.getValue().length - 1];
					/* must have common prefix */
					for (int i = 0; i < commonPrefix.length; i++)
					{
					commonPrefix[i] = from.getValue()[i];
					if (!from.getValue()[i].equals(to.getValue()[i]))
					{
					reportError (formatMessage ("err_enumRange_notCommonPrefix",
					from.toString(),
					to.toString()),
					ddot.getLine());
					break;
					}
					}
					rangeOrEnum = new Constant.EnumerationRange (
					commonPrefix,
					from.getValue()[from.getValue().length - 1],
					to.getValue()[from.getValue().length -1]);
					}
					
				}
			}
			else if ((LA(1)==RPAREN||LA(1)==COMMA)) {
				if ( inputState.guessing==0 ) {
					
					rangeOrEnum = from;
					
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_68);
			} else {
			  throw ex;
			}
		}
		return rangeOrEnum;
	}
	
	protected final int  property(
		int acceptable, int encountered
	) throws RecognitionException, TokenStreamException {
		int mod;
		
		Token  a = null;
		Token  f = null;
		Token  e = null;
		Token  o = null;
		Token  d = null;
		Token  v = null;
		Token  b = null;
		Token  g = null;
		Token  x = null;
		Token  t = null;
		Token  oidTok = null;
		Token  hidTok = null;
		Token  genTok = null;
		
		mod = 0;
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_ABSTRACT:
			{
				a = LT(1);
				match(LITERAL_ABSTRACT);
				if ( inputState.guessing==0 ) {
					
					if ((acceptable & ch.interlis.ili2c.metamodel.Properties.eABSTRACT) == 0)
					reportError(rsrc.getString("err_cantBeAbstract"),
					a.getLine());
					else
					mod = ch.interlis.ili2c.metamodel.Properties.eABSTRACT;
					
					if ((encountered & mod) != 0)
					reportWarning (rsrc.getString ("err_multipleAbstract"),
					a.getLine());
					
				}
				break;
			}
			case LITERAL_FINAL:
			{
				f = LT(1);
				match(LITERAL_FINAL);
				if ( inputState.guessing==0 ) {
					
					if ((acceptable & ch.interlis.ili2c.metamodel.Properties.eFINAL) == 0)
					reportError(rsrc.getString ("err_cantBeFinal"),
					f.getLine());
					else
					mod = ch.interlis.ili2c.metamodel.Properties.eFINAL;
					
					if ((encountered & mod) != 0)
					reportWarning (rsrc.getString ("err_multipleFinal"),
					f.getLine());
					
				}
				break;
			}
			case LITERAL_EXTENDED:
			{
				e = LT(1);
				match(LITERAL_EXTENDED);
				if ( inputState.guessing==0 ) {
					
					if ((acceptable & ch.interlis.ili2c.metamodel.Properties.eEXTENDED) == 0)
					reportError(rsrc.getString ("err_cantBeExtended"),
					e.getLine());
					else
					mod = ch.interlis.ili2c.metamodel.Properties.eEXTENDED;
					
					if ((encountered & mod) != 0)
					reportWarning (rsrc.getString("err_multipleExtended"),
					e.getLine());
					
				}
				break;
			}
			case LITERAL_ORDERED:
			{
				o = LT(1);
				match(LITERAL_ORDERED);
				if ( inputState.guessing==0 ) {
					
					if ((acceptable & ch.interlis.ili2c.metamodel.Properties.eORDERED) == 0)
					reportError(rsrc.getString ("err_cantBeOrdered"),
					o.getLine());
					else
					mod = ch.interlis.ili2c.metamodel.Properties.eORDERED;
					
					if ((encountered & mod) != 0)
					reportWarning (rsrc.getString("err_multipleOrdered"),
					o.getLine());
					
				}
				break;
			}
			case LITERAL_DATA:
			{
				d = LT(1);
				match(LITERAL_DATA);
				if ( inputState.guessing==0 ) {
					
					if ((acceptable & ch.interlis.ili2c.metamodel.Properties.eDATA) == 0)
					reportError(rsrc.getString ("err_cantBeData"),
					d.getLine());
					else
					mod = ch.interlis.ili2c.metamodel.Properties.eDATA;
					
					if ((encountered & mod) != 0)
					reportWarning (rsrc.getString("err_multipleData"),
					d.getLine());
					
				}
				break;
			}
			case LITERAL_VIEW:
			{
				v = LT(1);
				match(LITERAL_VIEW);
				if ( inputState.guessing==0 ) {
					
					if ((acceptable & ch.interlis.ili2c.metamodel.Properties.eVIEW) == 0)
					reportError(rsrc.getString ("err_cantBeView"),
					v.getLine());
					else
					mod = ch.interlis.ili2c.metamodel.Properties.eVIEW;
					
					if ((encountered & mod) != 0)
					reportWarning (rsrc.getString("err_multipleView"),
					v.getLine());
					
				}
				break;
			}
			case LITERAL_BASE:
			{
				b = LT(1);
				match(LITERAL_BASE);
				if ( inputState.guessing==0 ) {
					
					if ((acceptable & ch.interlis.ili2c.metamodel.Properties.eBASE) == 0)
					reportError(rsrc.getString ("err_cantBeBase"),
					b.getLine());
					else
					mod = ch.interlis.ili2c.metamodel.Properties.eBASE;
					
					if ((encountered & mod) != 0)
					reportWarning (rsrc.getString("err_multipleBase"),
					b.getLine());
					
				}
				break;
			}
			case LITERAL_GRAPHIC:
			{
				g = LT(1);
				match(LITERAL_GRAPHIC);
				if ( inputState.guessing==0 ) {
					
					if ((acceptable & ch.interlis.ili2c.metamodel.Properties.eGRAPHIC) == 0)
					reportError(rsrc.getString ("err_cantBeGraphic"),
					g.getLine());
					else
					mod = ch.interlis.ili2c.metamodel.Properties.eGRAPHIC;
					
					if ((encountered & mod) != 0)
					reportWarning (rsrc.getString("err_multipleGraphic"),
					g.getLine());
					
				}
				break;
			}
			case LITERAL_EXTERNAL:
			{
				x = LT(1);
				match(LITERAL_EXTERNAL);
				if ( inputState.guessing==0 ) {
					
					if ((acceptable & ch.interlis.ili2c.metamodel.Properties.eEXTERNAL) == 0)
					reportError(rsrc.getString ("err_cantBeExternal"),
					x.getLine());
					else
					mod = ch.interlis.ili2c.metamodel.Properties.eEXTERNAL;
					
					if ((encountered & mod) != 0)
					reportWarning (rsrc.getString("err_multipleExternal"),
					x.getLine());
					
				}
				break;
			}
			case LITERAL_TRANSIENT:
			{
				t = LT(1);
				match(LITERAL_TRANSIENT);
				if ( inputState.guessing==0 ) {
					
					if ((acceptable & ch.interlis.ili2c.metamodel.Properties.eTRANSIENT) == 0)
					reportError(rsrc.getString ("err_cantBeTransient"),
					t.getLine());
					else
					mod = ch.interlis.ili2c.metamodel.Properties.eTRANSIENT;
					
					if ((encountered & mod) != 0)
					reportWarning (rsrc.getString("err_multipleTransient"),
					t.getLine());
					
				}
				break;
			}
			case LITERAL_OID:
			{
				oidTok = LT(1);
				match(LITERAL_OID);
				if ( inputState.guessing==0 ) {
					
					if ((acceptable & ch.interlis.ili2c.metamodel.Properties.eOID) == 0)
					reportError(rsrc.getString ("err_cantBeOid"),
					oidTok.getLine());
					else
					mod = ch.interlis.ili2c.metamodel.Properties.eOID;
					
					if ((encountered & mod) != 0)
					reportWarning (rsrc.getString("err_multipleOid"),
					oidTok.getLine());
					
				}
				break;
			}
			case LITERAL_HIDING:
			{
				hidTok = LT(1);
				match(LITERAL_HIDING);
				if ( inputState.guessing==0 ) {
					
					if ((acceptable & ch.interlis.ili2c.metamodel.Properties.eHIDING) == 0)
					reportError(rsrc.getString ("err_cantBeHiding"),
					hidTok.getLine());
					else
					mod = ch.interlis.ili2c.metamodel.Properties.eHIDING;
					
					if ((encountered & mod) != 0)
					reportWarning (rsrc.getString("err_multipleHiding"),
					hidTok.getLine());
					
				}
				break;
			}
			case LITERAL_GENERIC:
			{
				genTok = LT(1);
				match(LITERAL_GENERIC);
				if ( inputState.guessing==0 ) {
					
					if ((acceptable & ch.interlis.ili2c.metamodel.Properties.eGENERIC) == 0)
					reportError(rsrc.getString ("err_cantBeGeneric"),
					genTok.getLine());
					else
					mod = ch.interlis.ili2c.metamodel.Properties.eGENERIC;
					
					if ((encountered & mod) != 0)
					reportWarning (rsrc.getString("err_multipleGeneric"),
					genTok.getLine());
					
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
				recover(ex,_tokenSet_68);
			} else {
			  throw ex;
			}
		}
		return mod;
	}
	
	protected final void enumNameListHelper(
		List namList
	) throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
		try {      // for error handling
			boolean synPredMatched972 = false;
			if (((LA(1)==DOT))) {
				int _m972 = mark();
				synPredMatched972 = true;
				inputState.guessing++;
				try {
					{
					match(DOT);
					match(NAME);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched972 = false;
				}
				rewind(_m972);
inputState.guessing--;
			}
			if ( synPredMatched972 ) {
				match(DOT);
				n = LT(1);
				match(NAME);
				if ( inputState.guessing==0 ) {
					namList.add(n.getText());
				}
				enumNameListHelper(namList);
			}
			else if ((_tokenSet_61.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_61);
			} else {
			  throw ex;
			}
		}
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"INTERLIS\"",
		"';'",
		"\"CONTRACTED\"",
		"\"REFSYSTEM\"",
		"\"MODEL\"",
		"\"SYMBOLOGY\"",
		"\"TYPE\"",
		"NAME",
		"'('",
		"')'",
		"\"NOINCREMENTALTRANSFER\"",
		"\"AT\"",
		"STRING",
		"\"VERSION\"",
		"EXPLANATION",
		"\"TRANSLATION\"",
		"\"OF\"",
		"'['",
		"']'",
		"'='",
		"\"CHARSET\"",
		"\"XMLNS\"",
		"\"IMPORTS\"",
		"\"UNQUALIFIED\"",
		"','",
		"'.'",
		"\"VIEW\"",
		"\"TOPIC\"",
		"\"EXTENDS\"",
		"\"BASKET\"",
		"\"OID\"",
		"\"AS\"",
		"\"DEPENDS\"",
		"\"ON\"",
		"\"DEFERRED\"",
		"\"GENERICS\"",
		"\"CLASS\"",
		"\"STRUCTURE\"",
		"\"NO\"",
		"\"ATTRIBUTE\"",
		"\"PARAMETER\"",
		"\"CONTINUOUS\"",
		"\"SUBDIVISION\"",
		"':'",
		"':='",
		"\"MANDATORY\"",
		"\"BAG\"",
		"\"LIST\"",
		"\"ANYSTRUCTURE\"",
		"\"RESTRICTION\"",
		"\"REFERENCE\"",
		"\"TO\"",
		"\"ANYCLASS\"",
		"\"ASSOCIATION\"",
		"\"DERIVED\"",
		"\"FROM\"",
		"\"CARDINALITY\"",
		"\"END\"",
		"'--'",
		"'-<>'",
		"'-<#>'",
		"\"OR\"",
		"'{'",
		"'*'",
		"'..'",
		"'}'",
		"\"CONSTRAINTS\"",
		"\"DOMAIN\"",
		"\"UNDEFINED\"",
		"\"URI\"",
		"\"NAME\"",
		"\"MTEXT\"",
		"\"TEXT\"",
		"\"ORDERED\"",
		"\"CIRCULAR\"",
		"\"ALL\"",
		"\"FINAL\"",
		"'#'",
		"\"OTHERS\"",
		"\"HALIGNMENT\"",
		"\"VALIGNMENT\"",
		"\"BOOLEAN\"",
		"\"NUMERIC\"",
		"\"CLOCKWISE\"",
		"\"COUNTERCLOCKWISE\"",
		"'<'",
		"'>'",
		"\"PI\"",
		"\"LNBASE\"",
		"\"FORMAT\"",
		"\"BASED\"",
		"\"INHERITANCE\"",
		"'/'",
		"\"DATE\"",
		"\"TIMEOFDAY\"",
		"\"DATETIME\"",
		"\"COORD\"",
		"\"MULTICOORD\"",
		"\"REFSYS\"",
		"\"ROTATION\"",
		"'->'",
		"\"CONTEXT\"",
		"\"ANY\"",
		"\"BLACKBOX\"",
		"\"XML\"",
		"\"BINARY\"",
		"'@'",
		"'>>'",
		"\"DIRECTED\"",
		"\"POLYLINE\"",
		"\"MULTIPOLYLINE\"",
		"\"SURFACE\"",
		"\"MULTISURFACE\"",
		"\"AREA\"",
		"\"MULTIAREA\"",
		"\"VERTEX\"",
		"\"WITHOUT\"",
		"\"OVERLAPS\"",
		"\"WITH\"",
		"\"ARCS\"",
		"\"STRAIGHTS\"",
		"\"LINE\"",
		"\"FORM\"",
		"\"UNIT\"",
		"\"ABSTRACT\"",
		"\"FUNCTION\"",
		"\"SIGN\"",
		"'~'",
		"\"OBJECTS\"",
		"\"METAOBJECT\"",
		"\"CONSTRAINT\"",
		"'<='",
		"'>='",
		"'%'",
		"\"EXISTENCE\"",
		"\"REQUIRED\"",
		"\"IN\"",
		"\"UNIQUE\"",
		"\"WHERE\"",
		"\"LOCAL\"",
		"\"SET\"",
		"'=>'",
		"\"AND\"",
		"\"NOT\"",
		"\"DEFINED\"",
		"'=='",
		"'!='",
		"'<>'",
		"\"INSPECTION\"",
		"\"THIS\"",
		"\"THISAREA\"",
		"\"THATAREA\"",
		"\"PARENT\"",
		"'\\\\'",
		"\"AGGREGATES\"",
		"\"FIRST\"",
		"\"LAST\"",
		"\"OBJECT\"",
		"\"ENUMVAL\"",
		"\"ENUMTREEVAL\"",
		"\"PROJECTION\"",
		"\"JOIN\"",
		"\"NULL\"",
		"\"UNION\"",
		"\"AGGREGATION\"",
		"\"EQUAL\"",
		"\"BASE\"",
		"\"EXTENDED\"",
		"\"BY\"",
		"\"GRAPHIC\"",
		"\"ACCORDING\"",
		"\"WHEN\"",
		"\"DATA\"",
		"\"EXTERNAL\"",
		"\"TRANSIENT\"",
		"\"HIDING\"",
		"\"GENERIC\"",
		"DEC",
		"POSINT",
		"NUMBER",
		"PLUS",
		"MINUS",
		"WS",
		"ILI_METAVALUE",
		"SL_COMMENT",
		"ILI_DOC",
		"ML_COMMENT",
		"'<-'",
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
		long[] data = { 2L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 422212735873056L, 279298719838L, 140737503429504L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 2305863903256510592L, -6917526828617826176L, 6L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 2305863903222956160L, -6917526828617826176L, 6L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 1986L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 2449979091231703168L, -6917526828617826112L, 35184372088838L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 2305863903155847296L, -6917526828617826176L, 6L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 2192L, 0L, 36L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 2306537900562450432L, 0L, 74816L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 2306529104469428224L, 0L, 74816L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = { 562949953421312L, 0L, 74816L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { 2449961857675427968L, -9223369837831520064L, 35184372088838L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = { 2449961840495558784L, -9223369837831520064L, 35184372088838L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { 2449961496898175104L, -9223369837831520064L, 35184372088838L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = { 536870944L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { -4250975831468275568L, 9007200328482817L, 6253489176620L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = { 276824096L, 0L, 16L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = { 281475255969824L, 72057594105036866L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = { 2305843009213693952L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	private static final long[] mk_tokenSet_19() {
		long[] data = { 288230380455071744L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
	private static final long[] mk_tokenSet_20() {
		long[] data = { 3459441812983252992L, 0L, 74816L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
	private static final long[] mk_tokenSet_21() {
		long[] data = { 3459433016890230784L, 0L, 74816L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
	private static final long[] mk_tokenSet_22() {
		long[] data = { 2306405959167115264L, 0L, 74816L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
	private static final long[] mk_tokenSet_23() {
		long[] data = { 297659788156024866L, 72058699018961002L, 147334573212056L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
	private static final long[] mk_tokenSet_24() {
		long[] data = { 9147936751497250L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_24 = new BitSet(mk_tokenSet_24());
	private static final long[] mk_tokenSet_25() {
		long[] data = { 3459450609076275200L, 32768L, 74816L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_25 = new BitSet(mk_tokenSet_25());
	private static final long[] mk_tokenSet_26() {
		long[] data = { 2306423551353159680L, 0L, 74816L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_26 = new BitSet(mk_tokenSet_26());
	private static final long[] mk_tokenSet_27() {
		long[] data = { 26470759618648208L, 35756384968023552L, 63050394783186980L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_27 = new BitSet(mk_tokenSet_27());
	private static final long[] mk_tokenSet_28() {
		long[] data = { 2305843009213696000L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_28 = new BitSet(mk_tokenSet_28());
	private static final long[] mk_tokenSet_29() {
		long[] data = { 9007199254753314L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_29 = new BitSet(mk_tokenSet_29());
	private static final long[] mk_tokenSet_30() {
		long[] data = { 22530109944699024L, 35756384968023552L, 63050394783186980L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_30 = new BitSet(mk_tokenSet_30());
	private static final long[] mk_tokenSet_31() {
		long[] data = { 281474976981024L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_31 = new BitSet(mk_tokenSet_31());
	private static final long[] mk_tokenSet_32() {
		long[] data = { 140737756803104L, 100663298L, 15073664L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_32 = new BitSet(mk_tokenSet_32());
	private static final long[] mk_tokenSet_33() {
		long[] data = { 2048L, 0L, 2113929216L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_33 = new BitSet(mk_tokenSet_33());
	private static final long[] mk_tokenSet_34() {
		long[] data = { 72057594038978736L, 0L, 36L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_34 = new BitSet(mk_tokenSet_34());
	private static final long[] mk_tokenSet_35() {
		long[] data = { 12111807844352L, 9062925770240L, 63050394783186944L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_35 = new BitSet(mk_tokenSet_35());
	private static final long[] mk_tokenSet_36() {
		long[] data = { 281474976981024L, 64L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_36 = new BitSet(mk_tokenSet_36());
	private static final long[] mk_tokenSet_37() {
		long[] data = { 281474976981024L, 2L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_37 = new BitSet(mk_tokenSet_37());
	private static final long[] mk_tokenSet_38() {
		long[] data = { 9288674231722016L, 2L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_38 = new BitSet(mk_tokenSet_38());
	private static final long[] mk_tokenSet_39() {
		long[] data = { 297659788149665824L, 1099612323842L, 147334573212032L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_39 = new BitSet(mk_tokenSet_39());
	private static final long[] mk_tokenSet_40() {
		long[] data = { 288230376160100352L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_40 = new BitSet(mk_tokenSet_40());
	private static final long[] mk_tokenSet_41() {
		long[] data = { 276828192L, 1099511660544L, 6597069783040L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_41 = new BitSet(mk_tokenSet_41());
	private static final long[] mk_tokenSet_42() {
		long[] data = { 72057594037930128L, 0L, 36L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_42 = new BitSet(mk_tokenSet_42());
	private static final long[] mk_tokenSet_43() {
		long[] data = { 281474981242912L, 1374389534832L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_43 = new BitSet(mk_tokenSet_43());
	private static final long[] mk_tokenSet_44() {
		long[] data = { 12111807844352L, 35756384968023552L, 63050394783186944L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_44 = new BitSet(mk_tokenSet_44());
	private static final long[] mk_tokenSet_45() {
		long[] data = { 2449979091231705216L, -6917526828617826112L, 35184372088838L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_45 = new BitSet(mk_tokenSet_45());
	private static final long[] mk_tokenSet_46() {
		long[] data = { 140737756803104L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_46 = new BitSet(mk_tokenSet_46());
	private static final long[] mk_tokenSet_47() {
		long[] data = { 281474976981024L, 108086391056891968L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_47 = new BitSet(mk_tokenSet_47());
	private static final long[] mk_tokenSet_48() {
		long[] data = { 281474976981024L, 72057594037928000L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_48 = new BitSet(mk_tokenSet_48());
	private static final long[] mk_tokenSet_49() {
		long[] data = { 281475247513632L, 274936627268L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_49 = new BitSet(mk_tokenSet_49());
	private static final long[] mk_tokenSet_50() {
		long[] data = { 281475245416480L, 274936627268L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_50 = new BitSet(mk_tokenSet_50());
	private static final long[] mk_tokenSet_51() {
		long[] data = { 281475245416480L, 274877907008L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_51 = new BitSet(mk_tokenSet_51());
	private static final long[] mk_tokenSet_52() {
		long[] data = { 0L, 4194304L, 63050394783186944L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_52 = new BitSet(mk_tokenSet_52());
	private static final long[] mk_tokenSet_53() {
		long[] data = { 281474976981024L, 274877907008L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_53 = new BitSet(mk_tokenSet_53());
	private static final long[] mk_tokenSet_54() {
		long[] data = { 9288674231722016L, 70368744177728L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_54 = new BitSet(mk_tokenSet_54());
	private static final long[] mk_tokenSet_55() {
		long[] data = { 9288674231722016L, 64L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_55 = new BitSet(mk_tokenSet_55());
	private static final long[] mk_tokenSet_56() {
		long[] data = { 140737756803104L, 100663298L, 140737503428992L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_56 = new BitSet(mk_tokenSet_56());
	private static final long[] mk_tokenSet_57() {
		long[] data = { 140737756803106L, 100663298L, 140737503428992L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_57 = new BitSet(mk_tokenSet_57());
	private static final long[] mk_tokenSet_58() {
		long[] data = { 140737756803104L, 100663314L, 140737503428992L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_58 = new BitSet(mk_tokenSet_58());
	private static final long[] mk_tokenSet_59() {
		long[] data = { 422212733771808L, 24640L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_59 = new BitSet(mk_tokenSet_59());
	private static final long[] mk_tokenSet_60() {
		long[] data = { 140737756798976L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_60 = new BitSet(mk_tokenSet_60());
	private static final long[] mk_tokenSet_61() {
		long[] data = { 140738293674016L, 100663314L, 140737503428992L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_61 = new BitSet(mk_tokenSet_61());
	private static final long[] mk_tokenSet_62() {
		long[] data = { 12591136L, 4294967304L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_62 = new BitSet(mk_tokenSet_62());
	private static final long[] mk_tokenSet_63() {
		long[] data = { 2097152L, 32L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_63 = new BitSet(mk_tokenSet_63());
	private static final long[] mk_tokenSet_64() {
		long[] data = { 140737758900256L, 4395630602L, 140737503428992L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_64 = new BitSet(mk_tokenSet_64());
	private static final long[] mk_tokenSet_65() {
		long[] data = { 75776L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_65 = new BitSet(mk_tokenSet_65());
	private static final long[] mk_tokenSet_66() {
		long[] data = { 2449979091231705232L, -6917526828617826112L, 35184372088870L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_66 = new BitSet(mk_tokenSet_66());
	private static final long[] mk_tokenSet_67() {
		long[] data = { 9288674231726112L, 70368744177730L, 2048L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_67 = new BitSet(mk_tokenSet_67());
	private static final long[] mk_tokenSet_68() {
		long[] data = { 268443648L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_68 = new BitSet(mk_tokenSet_68());
	private static final long[] mk_tokenSet_69() {
		long[] data = { 2097152L, 402653184L, 63050394783186946L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_69 = new BitSet(mk_tokenSet_69());
	private static final long[] mk_tokenSet_70() {
		long[] data = { 32L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_70 = new BitSet(mk_tokenSet_70());
	private static final long[] mk_tokenSet_71() {
		long[] data = { 0L, 0L, 8L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_71 = new BitSet(mk_tokenSet_71());
	private static final long[] mk_tokenSet_72() {
		long[] data = { 17592186116240L, 9147937212989696L, 63050396915466276L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_72 = new BitSet(mk_tokenSet_72());
	private static final long[] mk_tokenSet_73() {
		long[] data = { 6144L, 0L, 2113945600L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_73 = new BitSet(mk_tokenSet_73());
	private static final long[] mk_tokenSet_74() {
		long[] data = { 6144L, 0L, 2113929216L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_74 = new BitSet(mk_tokenSet_74());
	private static final long[] mk_tokenSet_75() {
		long[] data = { 17592186116240L, 9147937212989696L, 63050396915482660L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_75 = new BitSet(mk_tokenSet_75());
	private static final long[] mk_tokenSet_76() {
		long[] data = { 140737765183520L, 1099511660544L, 6597069783040L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_76 = new BitSet(mk_tokenSet_76());
	private static final long[] mk_tokenSet_77() {
		long[] data = { 8224L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_77 = new BitSet(mk_tokenSet_77());
	private static final long[] mk_tokenSet_78() {
		long[] data = { 9429411988516896L, 70368844841026L, 15075712L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_78 = new BitSet(mk_tokenSet_78());
	private static final long[] mk_tokenSet_79() {
		long[] data = { 140737756803104L, 0L, 131072L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_79 = new BitSet(mk_tokenSet_79());
	private static final long[] mk_tokenSet_80() {
		long[] data = { 140737756803104L, 2L, 131072L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_80 = new BitSet(mk_tokenSet_80());
	private static final long[] mk_tokenSet_81() {
		long[] data = { 0L, 100663296L, 14680448L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_81 = new BitSet(mk_tokenSet_81());
	private static final long[] mk_tokenSet_82() {
		long[] data = { 140737756803104L, 2L, 393216L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_82 = new BitSet(mk_tokenSet_82());
	private static final long[] mk_tokenSet_83() {
		long[] data = { 17592186116240L, 9147937213022464L, 63050396915466276L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_83 = new BitSet(mk_tokenSet_83());
	private static final long[] mk_tokenSet_84() {
		long[] data = { 140737757851680L, 100663298L, 15073664L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_84 = new BitSet(mk_tokenSet_84());
	private static final long[] mk_tokenSet_85() {
		long[] data = { 9429411988516896L, 71468356468802L, 15075712L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_85 = new BitSet(mk_tokenSet_85());
	private static final long[] mk_tokenSet_86() {
		long[] data = { 4194304L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_86 = new BitSet(mk_tokenSet_86());
	private static final long[] mk_tokenSet_87() {
		long[] data = { 270368L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_87 = new BitSet(mk_tokenSet_87());
	private static final long[] mk_tokenSet_88() {
		long[] data = { 8388608L, 0L, 4398046527488L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_88 = new BitSet(mk_tokenSet_88());
	private static final long[] mk_tokenSet_89() {
		long[] data = { 2305843009222084608L, 0L, 16384L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_89 = new BitSet(mk_tokenSet_89());
	private static final long[] mk_tokenSet_90() {
		long[] data = { 2306511512283383808L, 32768L, 74816L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_90 = new BitSet(mk_tokenSet_90());
	private static final long[] mk_tokenSet_91() {
		long[] data = { 8388608L, 1073741824L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_91 = new BitSet(mk_tokenSet_91());
	private static final long[] mk_tokenSet_92() {
		long[] data = { 268435488L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_92 = new BitSet(mk_tokenSet_92());
	private static final long[] mk_tokenSet_93() {
		long[] data = { 65536L, 140737958248704L, 63050394783186944L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_93 = new BitSet(mk_tokenSet_93());
	
	}
