// $ANTLR : "interlis22.g" -> "Ili22Parser.java"$

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

public class Ili22Parser extends antlr.LLkParser       implements Ili22ParserTokenTypes
 {

  protected PredefinedModel modelInterlis;
  protected Type predefinedBooleanType;
  protected Table predefinedScalSystemClass;
  protected Table predefinedCoordSystemClass;
  protected TransferDescription td;
  private Ili22Lexer lexer;
  private antlr.TokenStreamHiddenTokenFilter filter;
  private Map ili1TableRefAttrs;
  private boolean checkMetaObjs;
  private Ili2cMetaAttrs externalMetaAttrs=new Ili2cMetaAttrs();
  /** ensure uniqueness of generate role names
  */
  private int ili1RoleCounter=0;
  /** helps to remember ordering of reference attributes
  */
  private int ili1AttrCounter=0;

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
  	return parseIliFile (td,filename,new Ili22Lexer (stream),checkMetaObjects,line0Offest,metaAttrs);
  }
  static public boolean parseIliFile (TransferDescription td
    ,String filename
    ,java.io.InputStream stream
    ,boolean checkMetaObjects
    ,int line0Offest
    ,Ili2cMetaAttrs metaAttrs
    )
  {
  	return parseIliFile (td,filename,new Ili22Lexer (stream),checkMetaObjects,line0Offest,metaAttrs);
  }
  static public boolean parseIliFile (TransferDescription td
    ,String filename
    ,Ili22Lexer lexer
    ,boolean checkMetaObjects
    ,int line0Offest
    ,Ili2cMetaAttrs metaAttrs
    )
  {

    try {
	if ((filename != null) && "".equals (td.getName())){
		td.setName(filename);
	}
      // create token objects augmented with links to hidden tokens. 
      lexer.setTokenObjectClass("antlr.CommonHiddenStreamToken");

      // create filter that pulls tokens from the lexer
      antlr.TokenStreamHiddenTokenFilter filter = new antlr.TokenStreamHiddenTokenFilter(lexer);

      // tell the filter which tokens to hide, and which to discard
      filter.hide(ILI_DOC);
      filter.hide(ILI_METAVALUE);

      // connect parser to filter (instead of lexer)

      Ili22Parser parser = new Ili22Parser (filter);
      parser.checkMetaObjs=checkMetaObjects;
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
          return t;
        }
                 return resolveDomainRef(scope,nams,lin);
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
	    if(!checkMetaObjs){
		MetaObject mo=new MetaObject(name,polymorphicTo);
		basket.add(mo);
		return mo;
	    }
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
  protected LineForm addLineFormIfNoSuchExplanation (Container scope, String explanation, int line)
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



  protected AttributeDef findOverridingAttribute (
    Viewable container, int mods, String name, int line)
  {
    boolean      declaredExtended = (mods & 4) != 0;
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
	private Viewable getBaseViewable(Viewable start1,String base,int line)
	{
		AttributeDef baseProxy =  (AttributeDef)start1.getRealElement (AttributeDef.class, base);
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


protected Ili22Parser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public Ili22Parser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected Ili22Parser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public Ili22Parser(TokenStream lexer) {
  this(lexer,1);
}

public Ili22Parser(ParserSharedInputState state) {
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
				
					      if (version.doubleValue() != 2.2) {
					        reportError(formatMessage("err_wrongInterlisVersion",version.toString()),
					                    ili.getLine());
					        panic();
					      }
				// set lexer mode to Ili 2
				lexer.isIli1=false;
					
			}
			match(SEMI);
			{
			_loop5:
			do {
				if (((LA(1) >= LITERAL_REFSYSTEM && LA(1) <= LITERAL_TYPE))) {
					modelDef();
				}
				else {
					break _loop5;
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
		Token  contr = null;
		Token  contrIssuer = null;
		Token  contrExpl = null;
		Token  imp1 = null;
		Token  imp2 = null;
		Token  endDot = null;
		
			  Model md = null;
			  String[] importedNames = null;
			  Table tabDef;
			  int mods = 0;
			  boolean unqualified=false;
			  Contract contract=null;
			  String ilidoc=null;
			  Settings metaValues=null;
			
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				ilidoc=getIliDoc(); metaValues=getMetaValues();
						
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
								 md.setName(n1.getText());
				md.setFileName(getFilename());
								  md.setDocumentation(ilidoc);
								  md.setMetaValues(metaValues);
								  md.setIliVersion(Model.ILI2_2);
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
			else if ((LA(1)==LITERAL_TRANSLATION||LA(1)==EQUALS)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			if ((LA(1)==LITERAL_TRANSLATION)) {
				match(LITERAL_TRANSLATION);
				match(LITERAL_OF);
				match(NAME);
				if ( inputState.guessing==0 ) {
					/* TODO */
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
			_loop12:
			do {
				if ((LA(1)==LITERAL_CONTRACT)) {
					contr = LT(1);
					match(LITERAL_CONTRACT);
					match(LITERAL_ISSUED);
					match(LITERAL_BY);
					contrIssuer = LT(1);
					match(NAME);
					{
					if ((LA(1)==EXPLANATION)) {
						contrExpl = LT(1);
						match(EXPLANATION);
						if ( inputState.guessing==0 ) {
							
										 	contract=new Contract(contrIssuer.getText(),contrExpl.getText());
									
						}
					}
					else if ((LA(1)==SEMI)) {
						if ( inputState.guessing==0 ) {
							
										 	contract=new Contract(contrIssuer.getText());
									
						}
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
					if ( inputState.guessing==0 ) {
						
								         try {
								           md.addContract(contract);
								         } catch (Exception ex) {
								           reportError(ex, contrIssuer.getLine());
								         }
								
					}
					match(SEMI);
				}
				else {
					break _loop12;
				}
				
			} while (true);
			}
			{
			_loop20:
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
					_loop19:
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
							break _loop19;
						}
						
					} while (true);
					}
					match(SEMI);
				}
				else {
					break _loop20;
				}
				
			} while (true);
			}
			{
			_loop22:
			do {
				switch ( LA(1)) {
				case LITERAL_REFSYSTEM:
				case LITERAL_SIGN:
				{
					metaDataUseDef(md);
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
					break _loop22;
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
					       } catch (Exception ex) {
					         reportError (ex, endDot.getLine());
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
	}
	
	protected final void metaDataUseDef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		Token  ext = null;
		Token  modelName = null;
		Token  topicName = null;
		
		int mods;
		boolean sign=false;
		MetaDataUseDef def=null;
		MetaDataUseDef base=null;
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
				base=metaDataUseRef(scope);
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
			modelName = LT(1);
			match(NAME);
			match(DOT);
			topicName = LT(1);
			match(NAME);
			match(SEMI);
			if ( inputState.guessing==0 ) {
				
							int lin=modelName.getLine();
							Model model = resolveOrFixModelName(scope, modelName.getText(), lin);
							Topic topic = resolveOrFixTopicName(model, topicName.getText(), lin);
							def.setTopic(topic);
							scope.add(def);
							// ili2.2 
							// - a datacontainer is a basket in a xml-file
							// - the data container should already exist
							if(checkMetaObjs){
								DataContainer basket=td.getMetaDataContainer(def.getScopedName(null));
								// assign data container to metadatausedef
								def.setDataContainer(basket);
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
	
	protected final void unitDefs(
		Container scope
	) throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(LITERAL_UNIT);
			{
			_loop186:
			do {
				if ((LA(1)==NAME)) {
					unitDef(scope);
				}
				else {
					break _loop186;
				}
				
			} while (true);
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
		List     args = null;
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
				args = new LinkedList ();
				try {
				f.setName(fn.getText());
					f.setDocumentation(ilidoc);
					f.setMetaValues(metaValues);
				f.setSourceLine(fn.getLine());
				} catch (Exception ex) {
				reportError(ex, fn.getLine());
				}
				
			}
			lpar = LT(1);
			match(LPAREN);
			arg=formalArgument(container, lpar.getLine());
			if ( inputState.guessing==0 ) {
				
				try {
				args.add (arg);
				} catch (Exception ex) {
				reportError (ex, lpar.getLine ());
				}
				
			}
			{
			_loop276:
			do {
				if ((LA(1)==SEMI)) {
					sem = LT(1);
					match(SEMI);
					arg=formalArgument(container, sem.getLine());
					if ( inputState.guessing==0 ) {
						
						try {
						args.add (arg);
						} catch (Exception ex) {
						reportError (ex, sem.getLine ());
						}
						
					}
				}
				else {
					break _loop276;
				}
				
			} while (true);
			}
			match(RPAREN);
			col = LT(1);
			match(COLON);
			t=argumentType(container, col.getLine());
			if ( inputState.guessing==0 ) {
				
				try {
				f.setArguments ((FormalArgument[]) args.toArray (new FormalArgument[args.size()]));
				} catch (Exception ex) {
				reportError (ex, col.getLine());
				}
				
				try {
				if (t != null)
				f.setDomain(t);
				} catch (Exception ex) {
				reportError(ex, col.getLine());
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
			_loop183:
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
					break _loop183;
				}
				
			} while (true);
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
	
	protected final void domainDefs(
		Container container
	) throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(LITERAL_DOMAIN);
			{
			_loop102:
			do {
				if ((LA(1)==NAME)) {
					domainDef(container);
				}
				else {
					break _loop102;
				}
				
			} while (true);
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
			_loop217:
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
					domain=attrTypeDef(scope,true,null,n.getLine());
					if ( inputState.guessing==0 ) {
						
						def.setDomain(domain);
						scope.add(def);
						
					}
					match(SEMI);
				}
				else {
					break _loop217;
				}
				
			} while (true);
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
	
	protected final void classDef(
		Container container
	) throws RecognitionException, TokenStreamException {
		
		Token  n1 = null;
		Token  extToken = null;
		
			  Table table = null;
			  Table extending = null;
			  Table overwriting = null;
			  boolean identifiable = true;
			  Constraint constr = null;
			  int mods;
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
			if ((LA(1)==LITERAL_ATTRIBUTE)) {
				match(LITERAL_ATTRIBUTE);
			}
			else if ((_tokenSet_5.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			_loop41:
			do {
				if ((LA(1)==NAME)) {
					attributeDef(table);
				}
				else {
					break _loop41;
				}
				
			} while (true);
			}
			{
			_loop43:
			do {
				if ((_tokenSet_6.member(LA(1)))) {
					constr=constraintDef(table);
					if ( inputState.guessing==0 ) {
						
								        if (constr != null)
								          table.add (constr);
								
					}
				}
				else {
					break _loop43;
				}
				
			} while (true);
			}
			{
			if ((LA(1)==LITERAL_PARAMETER)) {
				match(LITERAL_PARAMETER);
				{
				_loop46:
				do {
					if ((LA(1)==NAME)) {
						parameterDef(table);
					}
					else {
						break _loop46;
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
				recover(ex,_tokenSet_3);
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
		Token  oid = null;
		Token  on = null;
		Token  com = null;
		
			  Topic topic = null;
			  Topic extending = null;
			  Topic depTopic = null;
			  int mods;
			  boolean viewTopic=false;
			  Domain topicOid=null;
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
			if ((LA(1)==LITERAL_OID)) {
				oid = LT(1);
				match(LITERAL_OID);
				match(LITERAL_AS);
				topicOid=domainRef(container);
				if ( inputState.guessing==0 ) {
					
									if(!(topicOid.getType() instanceof OIDType)){
										reportError (formatMessage ("err_topic_domainnotanoid",topicOid.toString()),oid.getLine());
									}
									topic.setOid(topicOid);
								
				}
			}
			else if ((_tokenSet_7.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			_loop30:
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
					_loop29:
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
							break _loop29;
						}
						
					} while (true);
					}
					match(SEMI);
				}
				else {
					break _loop30;
				}
				
			} while (true);
			}
			definitions(topic);
			end(topic);
			match(SEMI);
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
				recover(ex,_tokenSet_8);
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
				_loop349:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						mod=property(acceptable, mods);
						if ( inputState.guessing==0 ) {
							mods |= mod;
						}
					}
					else {
						break _loop349;
					}
					
				} while (true);
				}
				match(RPAREN);
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
				recover(ex,_tokenSet_10);
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
				
				d=resolveDomainRef(scope,(String[]) nams.toArray(new String[0]),lin);
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_11);
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
			_loop33:
			do {
				switch ( LA(1)) {
				case LITERAL_REFSYSTEM:
				case LITERAL_SIGN:
				{
					metaDataUseDef(scope);
					break;
				}
				case LITERAL_UNIT:
				{
					unitDefs(scope);
					break;
				}
				case LITERAL_DOMAIN:
				{
					domainDefs(scope);
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
					break _loop33;
				}
				}
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_12);
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
		Token  nam = null;
		
			int mods;
			AssociationDef def=new AssociationDef();
			AssociationDef extending = null;
			ViewableAlias derivedFrom;
			Constraint constr;
			  String ilidoc=null;
			  Settings metaValues=null;
			
		
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
										def.setDocumentation(ilidoc);
										def.setMetaValues(metaValues);
									} catch (Exception ex) {
										reportError(ex, n.getLine());
									}
								
				}
			}
			else if ((_tokenSet_13.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			mods=properties(ch.interlis.ili2c.metamodel.Properties.eABSTRACT|ch.interlis.ili2c.metamodel.Properties.eEXTENDED|ch.interlis.ili2c.metamodel.Properties.eFINAL);
			if ( inputState.guessing==0 ) {
				
								try {
									def.setAbstract((mods & ch.interlis.ili2c.metamodel.Properties.eABSTRACT) != 0);
									def.setFinal((mods & ch.interlis.ili2c.metamodel.Properties.eFINAL) != 0);
					    				def.setExtended((mods & ch.interlis.ili2c.metamodel.Properties.eEXTENDED) != 0);
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
							    // in ili22 all not-embedded assocs have a tid
							    if(!def.isLightweight()){
							    	def.setIdentifiable(true);
							    }else{
							    	def.setIdentifiable(false);
							    }
							}catch(Exception ex){
						            reportError(ex, a.getLine());
							}
						
			}
			{
			if ((LA(1)==LITERAL_ATTRIBUTE)) {
				match(LITERAL_ATTRIBUTE);
			}
			else if ((_tokenSet_14.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			_loop79:
			do {
				if ((LA(1)==NAME)) {
					attributeDef(def);
				}
				else {
					break _loop79;
				}
				
			} while (true);
			}
			{
			_loop81:
			do {
				if ((_tokenSet_6.member(LA(1)))) {
					constr=constraintDef(def);
					if ( inputState.guessing==0 ) {
						if(constr!=null)def.add(constr);
					}
				}
				else {
					break _loop81;
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
				recover(ex,_tokenSet_15);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final void constraintsDef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		
		
				Viewable def;
				Constraint constr;
			
		
		try {      // for error handling
			match(LITERAL_CONSTRAINTS);
			match(LITERAL_OF);
			def=classOrAssociationRef(scope);
			match(EQUALS);
			{
			_loop239:
			do {
				if ((_tokenSet_6.member(LA(1)))) {
					constr=constraintDef(def);
					if ( inputState.guessing==0 ) {
						if(constr!=null)def.add(constr);
					}
				}
				else {
					break _loop239;
				}
				
			} while (true);
			}
			match(LITERAL_END);
			match(SEMI);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_15);
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
			case LITERAL_PROJECTION:
			case LITERAL_JOIN:
			case LITERAL_UNION:
			case LITERAL_AGGREGATION:
			case LITERAL_INSPECTION:
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
							        }
							view=new ExtendedView(base);
							((ExtendedView)view).setExtended(false);
							
				}
				break;
			}
			case EQUALS:
			case LITERAL_BASE:
			case LITERAL_WHERE:
			{
				if ( inputState.guessing==0 ) {
					if((props&ch.interlis.ili2c.metamodel.Properties.eEXTENDED)==0){
								reportError(formatMessage("err_view_missingFormationdef",n.getText()),n.getLine());
							}
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
								  	view=new ExtendedView(base);
									((ExtendedView)view).setExtended(true);
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
			_loop284:
			do {
				if ((LA(1)==LITERAL_BASE)) {
					baseExtensionDef(view);
				}
				else {
					break _loop284;
				}
				
			} while (true);
			}
			{
			_loop286:
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
					break _loop286;
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
			_loop288:
			do {
				if ((_tokenSet_6.member(LA(1)))) {
					constr=constraintDef(view);
					if ( inputState.guessing==0 ) {
						
						if (constr != null)
						view.add (constr);
						
					}
				}
				else {
					break _loop288;
				}
				
			} while (true);
			}
			end(view);
			match(SEMI);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_15);
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
			_loop325:
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
					break _loop325;
				}
				
			} while (true);
			}
			{
			_loop327:
			do {
				if ((LA(1)==NAME)) {
					drawingRule(graph);
				}
				else {
					break _loop327;
				}
				
			} while (true);
			}
			end(graph);
			match(SEMI);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_15);
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
					iName = LT(1);
					match(NAME);
					if ( inputState.guessing==0 ) {
						names.add(ili.getText());names.add(iName.getText());
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
				_loop367:
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
						break _loop367;
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
				recover(ex,_tokenSet_16);
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
				recover(ex,_tokenSet_17);
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
		|ch.interlis.ili2c.metamodel.Properties.eFINAL
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
					  attrib.setDocumentation(ilidoc);
					  attrib.setMetaValues(metaValues);
				attrib.setAbstract((mods & ch.interlis.ili2c.metamodel.Properties.eABSTRACT) != 0);
				attrib.setFinal((mods & ch.interlis.ili2c.metamodel.Properties.eFINAL) != 0);
				} catch (Exception ex) {
				reportError(ex, n.getLine());
				}
				
			}
			type=attrTypeDef(container,/* alias ok */ true, overridingDomain,
                     n.getLine());
			if ( inputState.guessing==0 ) {
				
				if(type!=null){
						    	if(type instanceof ReferenceType){
								if(!(container instanceof Table) || ((Table)container).isIdentifiable()){
									reportError(formatMessage("err_attributeDef_refattrInClass",n.getText()),n.getLine());
								}
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
				factor(container,container);
				if ( inputState.guessing==0 ) {
					/* TODO attributeDef factor */
				}
				{
				_loop52:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						factor(container,container);
						if ( inputState.guessing==0 ) {
							/* TODO attributeDef ,factor */
						}
					}
					else {
						break _loop52;
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
				recover(ex,_tokenSet_18);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final Constraint  constraintDef(
		Viewable constrained
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
				constr=mandatoryConstraint(constrained);
				break;
			}
			case LITERAL_CONSTRAINT:
			{
				constr=plausibilityConstraint(constrained);
				break;
			}
			case LITERAL_EXISTENCE:
			{
				constr=existenceConstraint(constrained);
				break;
			}
			case LITERAL_UNIQUE:
			{
				constr=uniquenessConstraint(constrained);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				constr.setDocumentation(ilidoc);
					constr.setMetaValues(metaValues);
					
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
			boolean synPredMatched213 = false;
			if (((LA(1)==LITERAL_METAOBJECT))) {
				int _m213 = mark();
				synPredMatched213 = true;
				inputState.guessing++;
				try {
					{
					match(LITERAL_METAOBJECT);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched213 = false;
				}
				rewind(_m213);
inputState.guessing--;
			}
			if ( synPredMatched213 ) {
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
			else if ((_tokenSet_20.member(LA(1)))) {
				type=attrTypeDef(container,true,overridingDomain,n.getLine());
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
				// TODO: handle FINAL, ABSTRACT
				} catch (Exception ex) {
				reportError(ex, n.getLine());
				}
				
			}
			match(SEMI);
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
	
	protected final Table  structureRef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		Table t;
		
		
			t=null;
			
		
		try {      // for error handling
			t=classRef(scope);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_22);
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
	int        line
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
				if ((_tokenSet_23.member(LA(1)))) {
					typ=attrType(scope,allowAliases,extending,line);
				}
				else if ((_tokenSet_24.member(LA(1)))) {
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
							
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				if ( inputState.guessing==0 ) {
					
						      try {
							if (typ != null){
							  typ.setMandatory(true);
					}
						      } catch (Exception ex) {
							reportError(ex, line);
						      }
						
				}
				break;
			}
			case LITERAL_INTERLIS:
			case LITERAL_REFSYSTEM:
			case NAME:
			case LPAREN:
			case LITERAL_OID:
			case LITERAL_CLASS:
			case LITERAL_STRUCTURE:
			case LITERAL_REFERENCE:
			case LITERAL_URI:
			case LITERAL_NAME:
			case LITERAL_TEXT:
			case LITERAL_HALIGNMENT:
			case LITERAL_VALIGNMENT:
			case LITERAL_BOOLEAN:
			case LITERAL_NUMERIC:
			case STRUCTDEC:
			case LITERAL_COORD:
			case LITERAL_BASKET:
			case LITERAL_DIRECTED:
			case LITERAL_POLYLINE:
			case LITERAL_SURFACE:
			case LITERAL_AREA:
			case LITERAL_SIGN:
			case LITERAL_METAOBJECT:
			case DEC:
			case POSINT:
			case NUMBER:
			{
				typ=attrType(scope,allowAliases,extending,line);
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
				ct=restrictedStructureRef(scope);
				if ( inputState.guessing==0 ) {
					
								try{
									if(card!=null){
										ct.setCardinality(card);
									}
									ct.setOrdered(ordered);
								}catch(Exception ex){
								    reportError(ex, line);
								}
								typ=ct;
							
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
				recover(ex,_tokenSet_24);
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
			GraphicParameterDef param;
			List      nams = new LinkedList();
			int lin = 0;
			
		
		try {      // for error handling
			boolean synPredMatched254 = false;
			if (((LA(1)==LITERAL_INTERLIS||LA(1)==NAME))) {
				int _m254 = mark();
				synPredMatched254 = true;
				inputState.guessing++;
				try {
					{
					xyRef();
					match(LPAREN);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched254 = false;
				}
				rewind(_m254);
inputState.guessing--;
			}
			if ( synPredMatched254 ) {
				ev=functionCall(ns,functionNs);
			}
			else if ((LA(1)==LITERAL_PARAMETER)) {
				match(LITERAL_PARAMETER);
				lin=names2(nams);
				if ( inputState.guessing==0 ) {
					/* TODO */
				}
			}
			else if ((_tokenSet_25.member(LA(1)))) {
				if ( inputState.guessing==0 ) {
					
								if(!(ns instanceof Viewable)){
									reportError (formatMessage ("err_Container_currentIsNotViewable",
									ns.toString()), LT(1).getLine());
								}
							
				}
				ev=objectOrAttributePath((Viewable)ns);
			}
			else if ((_tokenSet_26.member(LA(1)))) {
				ev=constant(ns);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_27);
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
	int        line
	) throws RecognitionException, TokenStreamException {
		Type typ;
		
		
				List nams = new LinkedList();
				typ=null;
				Table restrictedTo=null;
				int lin=0;
				CompositionType ct=null;
			
		
		try {      // for error handling
			switch ( LA(1)) {
			case LPAREN:
			case LITERAL_OID:
			case LITERAL_CLASS:
			case LITERAL_STRUCTURE:
			case LITERAL_URI:
			case LITERAL_NAME:
			case LITERAL_TEXT:
			case LITERAL_HALIGNMENT:
			case LITERAL_VALIGNMENT:
			case LITERAL_BOOLEAN:
			case LITERAL_NUMERIC:
			case STRUCTDEC:
			case LITERAL_COORD:
			case LITERAL_BASKET:
			case LITERAL_DIRECTED:
			case LITERAL_POLYLINE:
			case LITERAL_SURFACE:
			case LITERAL_AREA:
			case DEC:
			case POSINT:
			case NUMBER:
			{
				typ=type(scope,extending);
				break;
			}
			case LITERAL_INTERLIS:
			case LITERAL_REFSYSTEM:
			case NAME:
			case LITERAL_SIGN:
			case LITERAL_METAOBJECT:
			{
				lin=names2(nams);
				if ( inputState.guessing==0 ) {
					
								Table s;
								Element e=resolveStructureOrDomainRef(scope,(String[]) nams.toArray(new String[nams.size()]),lin);
								if(e instanceof Table){
									s=(Table)e;
									ct=new CompositionType();
									try{
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
				{
				if (((LA(1)==LITERAL_RESTRICTED))&&(ct!=null)) {
					match(LITERAL_RESTRICTED);
					match(LITERAL_TO);
					restrictedTo=structureRef(scope);
					if ( inputState.guessing==0 ) {
						ct.addRestrictedTo(restrictedTo);
					}
					{
					_loop60:
					do {
						if ((LA(1)==COMMA)) {
							match(COMMA);
							restrictedTo=structureRef(scope);
							if ( inputState.guessing==0 ) {
								ct.addRestrictedTo(restrictedTo);
							}
						}
						else {
							break _loop60;
						}
						
					} while (true);
					}
				}
				else if ((_tokenSet_24.member(LA(1)))) {
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
				recover(ex,_tokenSet_24);
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
				recover(ex,_tokenSet_28);
			} else {
			  throw ex;
			}
		}
		return card;
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
			else if ((_tokenSet_29.member(LA(1)))) {
				ref=structureRef(scope);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
								ct=new CompositionType();
								try{
									ct.setComponentType(ref);
								}catch(Exception ex){
									reportError(ex, line);
								}
							
			}
			{
			if ((LA(1)==LITERAL_RESTRICTED)) {
				match(LITERAL_RESTRICTED);
				match(LITERAL_TO);
				restrictedTo=structureRef(scope);
				if ( inputState.guessing==0 ) {
					ct.addRestrictedTo(restrictedTo);
				}
				{
				_loop72:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						restrictedTo=structureRef(scope);
						if ( inputState.guessing==0 ) {
							ct.addRestrictedTo(restrictedTo);
						}
					}
					else {
						break _loop72;
					}
					
				} while (true);
				}
			}
			else if ((_tokenSet_24.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
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
		return ct;
	}
	
	protected final Type  type(
		Container scope,Type extending
	) throws RecognitionException, TokenStreamException {
		Type typ;
		
		typ=null;
			
		
		try {      // for error handling
			{
			if ((_tokenSet_30.member(LA(1)))) {
				typ=baseType(scope,extending);
			}
			else if (((LA(1) >= LITERAL_DIRECTED && LA(1) <= LITERAL_AREA))) {
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
				recover(ex,_tokenSet_24);
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
						Topic targetTopic=(Topic)rt.getReferred().getContainerOrSame(Topic.class);
						Topic thisTopic=(Topic)scope.getContainerOrSame(Topic.class);
						// target in a topic and targets topic not a base of this topic 
						if(targetTopic!=null && thisTopic!=null && !thisTopic.isExtending(targetTopic)){
							if(!external){
								// must be external
								reportError(formatMessage ("err_refattr_externalreq",""),refkw.getLine());
							}
						}
						rt.setExternal(external);
					
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
			else if ((_tokenSet_29.member(LA(1)))) {
				ref=classOrAssociationRef(scope);
				if ( inputState.guessing==0 ) {
					
							try{
							  if(scope.getContainer() instanceof AbstractPatternDef){
								  // check that scope's topic depends on ref's topic
								  AbstractPatternDef scopeTopic=(AbstractPatternDef)scope.getContainer(AbstractPatternDef.class);
								  AbstractPatternDef refTopic=(AbstractPatternDef)ref.getContainer(AbstractPatternDef.class);
								  if(refTopic!=scopeTopic){
								    if(!scopeTopic.isDependentOn(refTopic)){
								      reportError(formatMessage ("err_refattr_topicdepreq",
											scopeTopic.getName(),
											refTopic.getName()),refto);
								    }
								  }
							  }
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
			{
			if ((LA(1)==LITERAL_RESTRICTED)) {
				match(LITERAL_RESTRICTED);
				match(LITERAL_TO);
				restrictedTo=classOrAssociationRef(scope);
				if ( inputState.guessing==0 ) {
					rt.addRestrictedTo(restrictedTo);
				}
				{
				_loop66:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						restrictedTo=classOrAssociationRef(scope);
						if ( inputState.guessing==0 ) {
							rt.addRestrictedTo(restrictedTo);
						}
					}
					else {
						break _loop66;
					}
					
				} while (true);
				}
			}
			else if ((_tokenSet_24.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
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
		return rt;
	}
	
	protected final AbstractClassDef  classOrAssociationRef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		AbstractClassDef def;
		
		
			def=null;
			Viewable ref;
			
		
		try {      // for error handling
			ref=viewableRef(scope);
			if ( inputState.guessing==0 ) {
				
							def=(AbstractClassDef)ref;
						
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
				recover(ex,_tokenSet_32);
			} else {
			  throw ex;
			}
		}
		return found;
	}
	
	protected final AssociationDef  associationRef(
		Container scope
	) throws RecognitionException, TokenStreamException {
		AssociationDef ref;
		
		ref=null;
			Viewable t;
			
		
		try {      // for error handling
			t=viewableRef(scope);
			if ( inputState.guessing==0 ) {
				
							ref=(AssociationDef)t;
						
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_33);
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
			boolean synPredMatched308 = false;
			if (((LA(1)==NAME))) {
				int _m308 = mark();
				synPredMatched308 = true;
				inputState.guessing++;
				try {
					{
					match(NAME);
					match(TILDE);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched308 = false;
				}
				rewind(_m308);
inputState.guessing--;
			}
			if ( synPredMatched308 ) {
				n = LT(1);
				match(NAME);
				match(TILDE);
				if ( inputState.guessing==0 ) {
					aliasName=n.getText();
				}
			}
			else if ((_tokenSet_29.member(LA(1)))) {
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
				recover(ex,_tokenSet_34);
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
			boolean synPredMatched86 = false;
			if (((LA(1)==NAME))) {
				int _m86 = mark();
				synPredMatched86 = true;
				inputState.guessing++;
				try {
					{
					match(NAME);
					properties(ch.interlis.ili2c.metamodel.Properties.eABSTRACT
			|ch.interlis.ili2c.metamodel.Properties.eEXTENDED
			|ch.interlis.ili2c.metamodel.Properties.eFINAL
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
					synPredMatched86 = false;
				}
				rewind(_m86);
inputState.guessing--;
			}
			if ( synPredMatched86 ) {
				roleDef(container);
				roleDefs(container);
			}
			else if ((_tokenSet_35.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_35);
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
				RoleDef def=new RoleDef(false);
				int kind=0;
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
		|ch.interlis.ili2c.metamodel.Properties.eFINAL
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
			else if ((_tokenSet_36.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			ref=restrictedClassOrAssRef(container);
			{
			if ((LA(1)==COLONEQUALS)) {
				col = LT(1);
				match(COLONEQUALS);
				obj=factor(container,container);
			}
			else if ((LA(1)==SEMI)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
						try {
							boolean external=(mods & ch.interlis.ili2c.metamodel.Properties.eEXTERNAL)!=0;
							Topic targetTopic=(Topic)ref.getReferred().getContainerOrSame(Topic.class);
							Topic thisTopic=(Topic)container.getContainerOrSame(Topic.class);
							// target in a topic and targets topic not a base of this topic 
							if(targetTopic!=null && thisTopic!=null && !thisTopic.isExtending(targetTopic)){
								if(!external){
									// must be external
									reportError(formatMessage ("err_role_externalreq",""),n.getLine());
								}
							}
							ref.setExternal(external);
						  def.setName(n.getText());
						  def.setDocumentation(ilidoc);
						  def.setMetaValues(metaValues);
						  def.setExtended((mods & ch.interlis.ili2c.metamodel.Properties.eEXTENDED) != 0);
						  def.setAbstract((mods & ch.interlis.ili2c.metamodel.Properties.eABSTRACT) != 0);
						  def.setFinal((mods & ch.interlis.ili2c.metamodel.Properties.eFINAL) != 0);
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
						  def.setReference(ref);
						  if(obj!=null){
						  	if(!(obj instanceof ObjectPath)){
				reportError(formatMessage ("err_role_factorNotAnObjectPath","")
							      	,col.getLine());
				
							}
							// TODO check RoleDef.derivedfrom is an extension of destination
						  	def.setDerivedFrom((ObjectPath)obj);
						  }
						  container.add(def);
						} catch (Exception ex) {
						  reportError(ex, n.getLine());
						}
					
			}
			match(SEMI);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_35);
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
				recover(ex,_tokenSet_37);
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
			mods=properties(ch.interlis.ili2c.metamodel.Properties.eABSTRACT|ch.interlis.ili2c.metamodel.Properties.eFINAL);
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
				if ((_tokenSet_38.member(LA(1)))) {
					declared=type(container,extendingType);
				}
				else if ((LA(1)==SEMI)) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
			}
			else if ((_tokenSet_38.member(LA(1)))) {
				declared=type(container,extendingType);
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
				recover(ex,_tokenSet_39);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final Type  baseType(
		Container scope, Type extending
	) throws RecognitionException, TokenStreamException {
		Type bt;
		
		
				bt = null;
			
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_URI:
			case LITERAL_NAME:
			case LITERAL_TEXT:
			{
				bt=textType(extending);
				break;
			}
			case LITERAL_NUMERIC:
			case DEC:
			case POSINT:
			case NUMBER:
			{
				bt=numericType(scope,extending);
				break;
			}
			case STRUCTDEC:
			{
				bt=structuredUnitType(scope,extending);
				break;
			}
			case LPAREN:
			{
				bt=enumerationType(extending);
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
			case LITERAL_COORD:
			{
				bt=coordinateType(scope,extending);
				break;
			}
			case LITERAL_OID:
			{
				bt=oIDType(scope,extending);
				break;
			}
			case LITERAL_BASKET:
			{
				bt=basketType(scope,extending);
				break;
			}
			case LITERAL_CLASS:
			case LITERAL_STRUCTURE:
			{
				bt=classType(scope,extending);
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
				recover(ex,_tokenSet_24);
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
		Token  surf = null;
		Token  area = null;
		Token  att = null;
		
		boolean directed = false;
		boolean withStraights = false;
		boolean withArcs = false;
		LineForm[] theLineForms = null;
		PrecisionDecimal theMaxOverlap = null;
		Domain controlPointDomain = null;
		Table lineAttrStructure = null;
		int line = 0;
		lt = null;
		
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_DIRECTED:
			case LITERAL_POLYLINE:
			{
				{
				if ((LA(1)==LITERAL_DIRECTED)) {
					match(LITERAL_DIRECTED);
					if ( inputState.guessing==0 ) {
						directed = true;
					}
				}
				else if ((LA(1)==LITERAL_POLYLINE)) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				pl = LT(1);
				match(LITERAL_POLYLINE);
				if ( inputState.guessing==0 ) {
					
					line = pl.getLine();
					lt = new PolylineType ();
					try {
					((PolylineType) lt).setDirected (directed);
					} catch (Exception ex) {
					reportError (ex, line);
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
			case LITERAL_AREA:
			{
				area = LT(1);
				match(LITERAL_AREA);
				if ( inputState.guessing==0 ) {
					line = area.getLine(); lt = new AreaType();
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
			else if ((_tokenSet_40.member(LA(1)))) {
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
			else if ((_tokenSet_41.member(LA(1)))) {
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
			else if ((_tokenSet_42.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
				/* If no line forms are specified, take the inherited value. */
				if ((theLineForms == null) && (extending instanceof LineType))
				theLineForms = ((LineType) extending).getLineForms ();
				
				/* If no maximal overlap is specified, take the inherited value. */
				if ((theMaxOverlap == null) && (extending instanceof LineType))
				theMaxOverlap = ((LineType) extending).getMaxOverlap ();
				
				/* If no control point domain is specified, take the inherited value. */
				if ((controlPointDomain == null) && (extending instanceof LineType))
				controlPointDomain = ((LineType) extending).getControlPointDomain ();
				
				try {
				if (theLineForms != null)
				lt.setLineForms (theLineForms);
				} catch (Exception ex) {
				reportError (ex, line);
				}
				
				try {
				lt.setControlPointDomain (controlPointDomain);
				} catch (Exception ex) {
				reportError (ex, line);
				}
				
				try {
				/* FIXME: Check whether it is an AREA; reportError + provide artificial value */
				
				lt.setMaxOverlap (theMaxOverlap);
				} catch (Exception ex) {
				reportError (ex, line);
				}
				
			}
			{
			if ((LA(1)==LITERAL_LINE)) {
				att = LT(1);
				match(LITERAL_LINE);
				match(LITERAL_ATTRIBUTES);
				lineAttrStructure=classRef(scope);
				if ( inputState.guessing==0 ) {
					
					/* TODO als Verweis ist hier nur ein Name auf eine STRUCTURE zulaessig
					*/
					try {
					if (lt instanceof SurfaceOrAreaType)
					((SurfaceOrAreaType) lt).setLineAttributeStructure (lineAttrStructure);
					else
					reportError (
					formatMessage ("err_lineType_lineAttrForPolyline", ""),
					att.getLine());
					} catch (Exception ex) {
					reportError (ex, att.getLine ());
					}
					
				}
			}
			else if ((_tokenSet_24.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
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
			case LITERAL_TEXT:
			{
				match(LITERAL_TEXT);
				{
				if ((LA(1)==STAR)) {
					{
					star = LT(1);
					match(STAR);
					i=posInteger();
					if ( inputState.guessing==0 ) {
						
						try {
						tt = new TextType(i);
						} catch (Exception ex) {
						reportError (ex, star.getLine());
						}
						
					}
					}
				}
				else if ((_tokenSet_24.member(LA(1)))) {
					if ( inputState.guessing==0 ) {
						
						tt = new TextType ();
						
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
				recover(ex,_tokenSet_24);
			} else {
			  throw ex;
			}
		}
		return tt;
	}
	
	protected final NumericType  numericType(
		Container scope, Type extending
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
			else if ((_tokenSet_43.member(LA(1)))) {
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
			else if ((_tokenSet_44.member(LA(1)))) {
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
				referenceSystem=refSys(scope);
				break;
			}
			case SEMI:
			case RPAREN:
			case EXPLANATION:
			case COMMA:
			case COLONEQUALS:
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
				recover(ex,_tokenSet_45);
			} else {
			  throw ex;
			}
		}
		return ntyp;
	}
	
	protected final StructuredUnitType  structuredUnitType(
		Container scope, Type extending
	) throws RecognitionException, TokenStreamException {
		StructuredUnitType sutyp;
		
		Token  min = null;
		Token  max = null;
		Token  circ = null;
		Token  lbrac = null;
		Token  cw = null;
		Token  ccw = null;
		
		sutyp = null;
		int rotation = 0;
		int rotationLine = 0;
		Unit u = null;
		RefSystemRef referenceSystem = null;
		
		
		try {      // for error handling
			{
			min = LT(1);
			match(STRUCTDEC);
			match(DOTDOT);
			max = LT(1);
			match(STRUCTDEC);
			if ( inputState.guessing==0 ) {
				
				try {
				sutyp = new StructuredUnitType (
				new Constant.Structured (min.getText()),
				new Constant.Structured (max.getText()));
				} catch (Exception ex) {
				reportError(ex, min.getLine());
				}
				
			}
			}
			{
			if ((LA(1)==LITERAL_CIRCULAR)) {
				circ = LT(1);
				match(LITERAL_CIRCULAR);
				if ( inputState.guessing==0 ) {
					
					try {
					sutyp.setCircular (true);
					} catch (Exception ex) {
					reportError (ex, circ.getLine());
					}
					
				}
			}
			else if ((LA(1)==LBRACE)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			lbrac = LT(1);
			match(LBRACE);
			u=unitRef(scope);
			match(RBRACE);
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
			case SEMI:
			case RPAREN:
			case EXPLANATION:
			case COMMA:
			case COLONEQUALS:
			case LCURLY:
			case LESS:
			{
				if ( inputState.guessing==0 ) {
					
					if (extending instanceof NumericalType)
					rotation = ((NumericalType) extending).getRotation ();
					else
					rotation = NumericalType.ROTATION_NONE;
					
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
				
				if (sutyp != null)
				{
				try {
				sutyp.setUnit(u);
				} catch (Exception ex) {
				reportError (ex, lbrac.getLine());
				}
				
				try {
				sutyp.setRotation (rotation);
				} catch (Exception ex) {
				reportError (ex, rotationLine);
				}
				}
				
			}
			{
			if ((LA(1)==LCURLY||LA(1)==LESS)) {
				referenceSystem=refSys(scope);
				if ( inputState.guessing==0 ) {
					
					try {
					if (sutyp != null)
					sutyp.setReferenceSystem (referenceSystem);
					} catch (Exception ex) {
					reportError(ex, lbrac.getLine());
					}
					
				}
			}
			else if ((_tokenSet_45.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
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
		return sutyp;
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
				recover(ex,_tokenSet_24);
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
				recover(ex,_tokenSet_24);
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
				recover(ex,_tokenSet_24);
			} else {
			  throw ex;
			}
		}
		return tt;
	}
	
	protected final CoordType  coordinateType(
		Container scope, Type extending
	) throws RecognitionException, TokenStreamException {
		CoordType ct;
		
		Token  coord = null;
		
		NumericalType nt1 = null;
		NumericalType nt2 = null;
		NumericalType nt3 = null;
		int[] rots = null;
		ct = null;
		
		NumericalType ext_nt1 = null;
		NumericalType ext_nt2 = null;
		NumericalType ext_nt3 = null;
		
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
			coord = LT(1);
			match(LITERAL_COORD);
			nt1=numericalType(scope, ext_nt1);
			{
			if ((LA(1)==COMMA)) {
				match(COMMA);
				nt2=numericalType(scope, ext_nt2);
				{
				if ((LA(1)==COMMA)) {
					match(COMMA);
					{
					if ((LA(1)==LITERAL_ROTATION)) {
						rots=rotationDef();
					}
					else if ((_tokenSet_46.member(LA(1)))) {
						nt3=numericalType(scope, ext_nt3);
						{
						if ((LA(1)==COMMA)) {
							match(COMMA);
							rots=rotationDef();
						}
						else if ((_tokenSet_24.member(LA(1)))) {
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
				}
				else if ((_tokenSet_24.member(LA(1)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
			}
			else if ((_tokenSet_24.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
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
				if (rots == null)
				ct = new CoordType (nts);
				else
				ct = new CoordType (nts, rots[0], rots[1]);
				} catch (Exception ex) {
				reportError (ex, coord.getLine());
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
				nt=numericType(scope,extendingOidType);
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
				recover(ex,_tokenSet_24);
			} else {
			  throw ex;
			}
		}
		return bt;
	}
	
	protected final BasketType  basketType(
		Container scope,Type extending
	) throws RecognitionException, TokenStreamException {
		BasketType bt;
		
		Token  b = null;
		
				int mods;
				bt=new BasketType();
				Topic topic=null;
			
		
		try {      // for error handling
			b = LT(1);
			match(LITERAL_BASKET);
			mods=properties(ch.interlis.ili2c.metamodel.Properties.eDATA
				|ch.interlis.ili2c.metamodel.Properties.eVIEW
				|ch.interlis.ili2c.metamodel.Properties.eBASE
				|ch.interlis.ili2c.metamodel.Properties.eGRAPHIC);
			if ( inputState.guessing==0 ) {
				
				try {
							    bt.setKind(mods);
				} catch (Exception ex) {
				reportError (ex, b.getLine());
							   }
							
			}
			{
			if ((LA(1)==LITERAL_OF)) {
				match(LITERAL_OF);
				topic=topicRef(scope);
				if ( inputState.guessing==0 ) {
					
									bt.setTopic(topic);
								
				}
			}
			else if ((_tokenSet_24.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
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
		return bt;
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
				if ((LA(1)==LITERAL_RESTRICTED)) {
					match(LITERAL_RESTRICTED);
					match(LITERAL_TO);
					restrictedTo=classOrAssociationRef(scope);
					if ( inputState.guessing==0 ) {
						bt.addRestrictedTo(restrictedTo);
					}
					{
					_loop165:
					do {
						if ((LA(1)==COMMA)) {
							match(COMMA);
							restrictedTo=classOrAssociationRef(scope);
							if ( inputState.guessing==0 ) {
								bt.addRestrictedTo(restrictedTo);
							}
						}
						else {
							break _loop165;
						}
						
					} while (true);
					}
				}
				else if ((_tokenSet_24.member(LA(1)))) {
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
				if ((LA(1)==LITERAL_RESTRICTED)) {
					match(LITERAL_RESTRICTED);
					match(LITERAL_TO);
					restrictedTo=structureRef(scope);
					if ( inputState.guessing==0 ) {
						bt.addRestrictedTo(restrictedTo);
					}
					{
					_loop169:
					do {
						if ((LA(1)==COMMA)) {
							match(COMMA);
							restrictedTo=structureRef(scope);
							if ( inputState.guessing==0 ) {
								bt.addRestrictedTo(restrictedTo);
							}
						}
						else {
							break _loop169;
						}
						
					} while (true);
					}
				}
				else if ((_tokenSet_24.member(LA(1)))) {
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
				recover(ex,_tokenSet_24);
			} else {
			  throw ex;
			}
		}
		return bt;
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
			case STRUCTDEC:
			{
				c=structUnitConst(scope);
				break;
			}
			case HASH:
			{
				c=enumerationConst();
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
				recover(ex,_tokenSet_47);
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
			else if ((_tokenSet_47.member(LA(1)))) {
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
				recover(ex,_tokenSet_47);
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
				recover(ex,_tokenSet_47);
			} else {
			  throw ex;
			}
		}
		return c;
	}
	
	protected final Constant  structUnitConst(
		Container scope
	) throws RecognitionException, TokenStreamException {
		Constant c;
		
		Token  str = null;
		Unit un=null;
			c=null;
			
		
		try {      // for error handling
			str = LT(1);
			match(STRUCTDEC);
			{
			if ((LA(1)==LBRACE)) {
				match(LBRACE);
				un=unitRef(scope);
				match(RBRACE);
			}
			else if ((_tokenSet_47.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
							if(un==null){
								c=new Constant.Structured (str.getText());
							}else{
								c=new Constant.Structured (str.getText(),un);
							}
						
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
				else if ((_tokenSet_48.member(LA(1)))) {
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
				recover(ex,_tokenSet_48);
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
		
		
		List elements = new LinkedList();
		ch.interlis.ili2c.metamodel.Enumeration.Element curElement;
		boolean isFinal=false;
		enumer = null;
		
		
		try {      // for error handling
			match(LPAREN);
			{
			if ((LA(1)==NAME)) {
				{
				curElement=enumElement(extending);
				if ( inputState.guessing==0 ) {
					elements.add(curElement);
				}
				{
				_loop118:
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
						break _loop118;
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
				// TODO: if extendig, check matching of prefixes with base definition
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_49);
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
				}
				else
				{
					    // new subtree
				ee = new ch.interlis.ili2c.metamodel.Enumeration.Element (
				(String) elt.get(i),
				subEnum);
						    ee.setDocumentation(ilidoc);
						    ee.setMetaValues(metaValues);
				}
				}
				else
				{
					  List subEe=new ArrayList();
					  subEe.add(ee);
				ee = new ch.interlis.ili2c.metamodel.Enumeration.Element (
				(String) elt.get(i),
				new ch.interlis.ili2c.metamodel.Enumeration (subEe)
				);
						    ee.setDocumentation(ilidoc);
						    ee.setMetaValues(metaValues);
				}
				}
				
				if ((subEnum == null) && (siz > 1))
				reportError (rsrc.getString("err_dottedEnum"), lineNumber);
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_50);
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
				recover(ex,_tokenSet_51);
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
				recover(ex,_tokenSet_52);
			} else {
			  throw ex;
			}
		}
		return u;
	}
	
	protected final RefSystemRef  refSys(
		Container scope
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
				boolean synPredMatched137 = false;
				if (((_tokenSet_29.member(LA(1))))) {
					int _m137 = mark();
					synPredMatched137 = true;
					inputState.guessing++;
					try {
						{
						match(NAME);
						{
						_loop136:
						do {
							if ((LA(1)==DOT)) {
								match(DOT);
								match(NAME);
							}
							else {
								break _loop136;
							}
							
						} while (true);
						}
						match(LBRACE);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched137 = false;
					}
					rewind(_m137);
inputState.guessing--;
				}
				if ( synPredMatched137 ) {
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
				else if ((_tokenSet_29.member(LA(1)))) {
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
				recover(ex,_tokenSet_45);
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
				topicName = (String) nams.get(0);
				basketName = (String) nams.get(1);
				objectName = (String) nams.get(2);
				model = (Model) scope.getContainerOrSame(Model.class);
				topic = resolveOrFixTopicName(model, topicName, lin);
				ref = resolveOrFixBasketName(topic, basketName, lin);
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
				recover(ex,_tokenSet_53);
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
				recover(ex,_tokenSet_54);
			} else {
			  throw ex;
			}
		}
		return dec;
	}
	
	protected final NumericalType  numericalType(
		Container scope, Type extending
	) throws RecognitionException, TokenStreamException {
		NumericalType ntyp;
		
		
		ntyp = null;
		
		
		try {      // for error handling
			if ((LA(1)==STRUCTDEC)) {
				ntyp=structuredUnitType(scope, extending);
			}
			else if ((_tokenSet_55.member(LA(1)))) {
				ntyp=numericType(scope, extending);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
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
		return ntyp;
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
				recover(ex,_tokenSet_24);
			} else {
			  throw ex;
			}
		}
		return rots;
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
			_loop179:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					linForm=lineFormType(scope);
					if ( inputState.guessing==0 ) {
						if (linForm != null) ll.add(linForm);
					}
				}
				else {
					break _loop179;
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
				recover(ex,_tokenSet_40);
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
				recover(ex,_tokenSet_56);
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
				docName = idName = n.getText();
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
				switch ( LA(1)) {
				case LBRACE:
				case LITERAL_PI:
				case LITERAL_LNBASE:
				case LITERAL_FUNCTION:
				case DEC:
				case POSINT:
				case NUMBER:
				{
					u=derivedUnit(scope, idName, docName, _abstract);
					if ( inputState.guessing==0 ) {
						
						if (extending != null)
						reportError (rsrc.getString ("err_derivedUnit_ext"), ext.getLine());
						
					}
					break;
				}
				case LPAREN:
				{
					u=composedUnit(scope, idName, docName, _abstract);
					break;
				}
				case LCURLY:
				{
					u=structuredUnit(scope, idName, docName, _abstract);
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
				recover(ex,_tokenSet_39);
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
				_loop195:
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
						break _loop195;
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
				recover(ex,_tokenSet_57);
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
			_loop199:
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
					break _loop199;
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
				recover(ex,_tokenSet_57);
			} else {
			  throw ex;
			}
		}
		return u;
	}
	
	protected final StructuredUnit  structuredUnit(
		Container scope, String idName, String docName, boolean _abstract
	) throws RecognitionException, TokenStreamException {
		StructuredUnit u;
		
		Token  lpar = null;
		Token  col = null;
		Token  lbrac = null;
		
		u = null;
		Unit firstUnit = null;
		List parts = null;
		
		int line = 0;
		PrecisionDecimal min = null;
		PrecisionDecimal max = null;
		Unit compUnit = null;
		boolean continuous = false;
		
		
		try {      // for error handling
			lpar = LT(1);
			match(LCURLY);
			firstUnit=unitRef(scope);
			if ( inputState.guessing==0 ) {
				
				u = new StructuredUnit();
				parts = new LinkedList();
				
				try {
				u.setName (idName);
				u.setDocName (docName);
				} catch (Exception ex) {
				reportError(ex, lpar.getLine());
				}
				
				try {
				u.setAbstract (_abstract);
				} catch (Exception ex) {
				reportError(ex, lpar.getLine());
				}
				
			}
			{
			_loop202:
			do {
				if ((LA(1)==COLON)) {
					col = LT(1);
					match(COLON);
					compUnit=unitRef(scope);
					lbrac = LT(1);
					match(LBRACE);
					min=decimal();
					match(DOTDOT);
					max=decimal();
					match(RBRACE);
					if ( inputState.guessing==0 ) {
						
						try {
						parts.add (new StructuredUnit.Part (compUnit, min, max));
						} catch (Exception ex) {
						reportError (ex, col.getLine());
						}
						
					}
				}
				else {
					break _loop202;
				}
				
			} while (true);
			}
			match(RCURLY);
			{
			if ((LA(1)==LITERAL_CONTINUOUS)) {
				match(LITERAL_CONTINUOUS);
				if ( inputState.guessing==0 ) {
					continuous = true;
				}
			}
			else if ((LA(1)==SEMI)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
				try {
				u.setFirstUnit (firstUnit);
				} catch (Exception ex) {
				reportError (ex, lpar.getLine());
				}
				
				try {
				u.setParts (
				(StructuredUnit.Part[]) parts.toArray (
				new StructuredUnit.Part[parts.size()]));
				} catch (Exception ex) {
				reportError (ex, lpar.getLine());
				}
				
				try {
				u.setContinuous (continuous);
				} catch (Exception ex) {
				reportError (ex, lpar.getLine ());
				}
				
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
		return u;
	}
	
	protected final MetaDataUseDef  metaDataUseRef(
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
				recover(ex,_tokenSet_58);
			} else {
			  throw ex;
			}
		}
		return ref;
	}
	
	protected final MandatoryConstraint  mandatoryConstraint(
		Viewable v
	) throws RecognitionException, TokenStreamException {
		MandatoryConstraint constr;
		
		Token  mand = null;
		
		Evaluable condition = null;
		constr = null;
		
		
		try {      // for error handling
			mand = LT(1);
			match(LITERAL_MANDATORY);
			match(LITERAL_CONSTRAINT);
			condition=expression(v, /* expectedType */ predefinedBooleanType,v);
			match(SEMI);
			if ( inputState.guessing==0 ) {
				
				try {
				constr = new MandatoryConstraint();
				constr.setCondition(condition);
				} catch (Exception ex) {
				reportError(ex, mand.getLine());
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
		return constr;
	}
	
	protected final PlausibilityConstraint  plausibilityConstraint(
		Viewable v
	) throws RecognitionException, TokenStreamException {
		PlausibilityConstraint constr;
		
		Token  tok = null;
		
		PrecisionDecimal       percentage;
		int                    direction = 0;
		Evaluable              condition = null;
		constr = null;
		
		
		try {      // for error handling
			tok = LT(1);
			match(LITERAL_CONSTRAINT);
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
			condition=expression(v, /* expectedType */ predefinedBooleanType,v);
			match(SEMI);
			if ( inputState.guessing==0 ) {
				
				try {
				constr = new PlausibilityConstraint();
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
				recover(ex,_tokenSet_19);
			} else {
			  throw ex;
			}
		}
		return constr;
	}
	
	protected final ExistenceConstraint  existenceConstraint(
		Viewable v
	) throws RecognitionException, TokenStreamException {
		ExistenceConstraint constr;
		
		
				ObjectPath attr;
				Viewable ref;
				constr=new ExistenceConstraint();
				ObjectPath attrRef=null;
			
		
		try {      // for error handling
			match(LITERAL_EXISTENCE);
			match(LITERAL_CONSTRAINT);
			attr=attributePath(v);
			if ( inputState.guessing==0 ) {
				
							constr.setRestrictedAttribute(attr);
						
			}
			match(LITERAL_REQUIRED);
			match(LITERAL_IN);
			ref=viewableRef(v);
			match(COLON);
			attrRef=attributePath(ref);
			if ( inputState.guessing==0 ) {
				
							constr.addRequiredIn(attrRef); 
						
			}
			{
			_loop225:
			do {
				if ((LA(1)==LITERAL_OR)) {
					match(LITERAL_OR);
					ref=viewableRef(v);
					match(COLON);
					attrRef=attributePath(ref);
					if ( inputState.guessing==0 ) {
						
									constr.addRequiredIn(attrRef); 
								
					}
				}
				else {
					break _loop225;
				}
				
			} while (true);
			}
			match(SEMI);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_19);
			} else {
			  throw ex;
			}
		}
		return constr;
	}
	
	protected final UniquenessConstraint  uniquenessConstraint(
		Viewable v
	) throws RecognitionException, TokenStreamException {
		UniquenessConstraint constr;
		
		
				constr=new UniquenessConstraint();
			
		
		try {      // for error handling
			match(LITERAL_UNIQUE);
			{
			if ((_tokenSet_25.member(LA(1)))) {
				constr=globalUniqueness(v);
			}
			else if ((LA(1)==LPAREN)) {
				constr=localUniqueness(v);
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
				recover(ex,_tokenSet_19);
			} else {
			  throw ex;
			}
		}
		return constr;
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
				recover(ex,_tokenSet_59);
			} else {
			  throw ex;
			}
		}
		return expr;
	}
	
	protected final ObjectPath  attributePath(
		Viewable ns
	) throws RecognitionException, TokenStreamException {
		ObjectPath object;
		
		
			object=null;
			
		
		try {      // for error handling
			object=objectOrAttributePath(ns);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_60);
			} else {
			  throw ex;
			}
		}
		return object;
	}
	
	protected final UniquenessConstraint  globalUniqueness(
		Viewable scope
	) throws RecognitionException, TokenStreamException {
		UniquenessConstraint constr;
		
		
				constr=new UniquenessConstraint();
				UniqueEl elements=null;
			
		
		try {      // for error handling
			elements=uniqueEl(scope);
			if ( inputState.guessing==0 ) {
				
						constr.setElements(elements);
					
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
			_loop234:
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
					break _loop234;
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
			_loop236:
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
					break _loop236;
				}
				
			} while (true);
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
		return constr;
	}
	
	protected final UniqueEl  uniqueEl(
		Viewable scope
	) throws RecognitionException, TokenStreamException {
		UniqueEl ret;
		
		
				ret=null;
				ObjectPath attr=null;
			
		
		try {      // for error handling
			attr=objectOrAttributePath(scope);
			if ( inputState.guessing==0 ) {
				
							ret=new UniqueEl();
							ret.addAttribute(attr);
						
			}
			{
			_loop231:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					attr=objectOrAttributePath(scope);
					if ( inputState.guessing==0 ) {
						
									ret.addAttribute(attr);
								
					}
				}
				else {
					break _loop231;
				}
				
			} while (true);
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
		return ret;
	}
	
	protected final ObjectPath  objectOrAttributePath(
		Viewable start
	) throws RecognitionException, TokenStreamException {
		ObjectPath object;
		
		Token  p = null;
		
				object=null;
				PathEl el;
				LinkedList path=new LinkedList();
				Viewable next=null;
			
		
		try {      // for error handling
			el=pathEl(start);
			if ( inputState.guessing==0 ) {
				
							path.add(el);
							next=start;
						
			}
			{
			_loop257:
			do {
				if ((LA(1)==POINTSTO)) {
					p = LT(1);
					match(POINTSTO);
					if ( inputState.guessing==0 ) {
						
									Object prenext=next;
									next=el.getViewable();
									// System.err.println(el+": "+prenext+"->"+next);
								
					}
					el=pathEl(next);
					if ( inputState.guessing==0 ) {
						
									path.add(el);
								
					}
				}
				else {
					break _loop257;
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
				recover(ex,_tokenSet_62);
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
		
		Token  o = null;
		
		List disjoined = null;
		expr = null;
		int lineNumber = 0;
		
		
		try {      // for error handling
			expr=term1(ns, expectedType, functionNs);
			if ( inputState.guessing==0 ) {
				
				disjoined = new LinkedList ();
				disjoined.add(expr);
				
			}
			{
			_loop243:
			do {
				if ((LA(1)==LITERAL_OR)) {
					o = LT(1);
					match(LITERAL_OR);
					expr=term1(ns, expectedType, functionNs);
					if ( inputState.guessing==0 ) {
						
						disjoined.add (expr);
						lineNumber = o.getLine();
						
					}
				}
				else {
					break _loop243;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				
				if (disjoined.size() == 1)
				expr = (Evaluable) disjoined.get(0);
				else
				{
				try {
				expr = new Expression.Disjunction (
				(Evaluable[]) disjoined.toArray (new Evaluable[disjoined.size()]));
				} catch (Exception ex) {
				reportError (ex, lineNumber);
				}
				}
				
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
		
		
		try {      // for error handling
			expr=term2(ns, expectedType, functionNs);
			if ( inputState.guessing==0 ) {
				
				conjoined = new LinkedList ();
				conjoined.add(expr);
				
			}
			{
			_loop246:
			do {
				if ((LA(1)==LITERAL_AND)) {
					an = LT(1);
					match(LITERAL_AND);
					expr=term2(ns, expectedType, functionNs);
					if ( inputState.guessing==0 ) {
						
						conjoined.add (expr);
						lineNumber = an.getLine();
						
					}
				}
				else {
					break _loop246;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				
				if (conjoined.size() == 1)
				expr = (Evaluable) conjoined.get(0);
				else
				{
				try {
				expr = new Expression.Conjunction(
				(Evaluable[]) conjoined.toArray(new Evaluable[conjoined.size()]));
				} catch (Exception ex) {
				reportError (ex, lineNumber);
				}
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
		return expr;
	}
	
	protected final Evaluable  term2(
		Container ns, Type expectedType,Container functionNs
	) throws RecognitionException, TokenStreamException {
		Evaluable expr;
		
		
		expr = null;
		Evaluable comparedWith = null;
		char op = '=';
		int[] lineNumberPar = null;
		
		
		try {      // for error handling
			expr=predicate(ns, expectedType,functionNs);
			{
			if ((_tokenSet_64.member(LA(1)))) {
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
					expr = new Expression.Equality (expr, comparedWith);
					break;
					
					/* LESSGREATER, BANGEQUALS */
					case '!':
					expr = new Expression.Inequality (expr, comparedWith);
					break;
					
					/* LESSEQUAL */
					case 'l':
					expr = new Expression.LessThanOrEqual (expr, comparedWith);
					break;
					
					/* GREATEREQUAL */
					case 'g':
					expr = new Expression.GreaterThanOrEqual (expr, comparedWith);
					break;
					
					/* LESS */
					case '<':
					expr = new Expression.LessThan (expr, comparedWith);
					break;
					
					/* GREATER */
					case '>':
					expr = new Expression.GreaterThan (expr, comparedWith);
					break;
					
					default:
					reportInternalError (0);
					panic ();
					}
					} catch (Exception ex) {
					reportError (ex, lineNumberPar[0]);
					}
					
				}
			}
			else if ((_tokenSet_65.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
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
								      try {
									expr = new Expression.Negation (expr);
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
				expr=factor(ns,ns);
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
			case NAME:
			case LITERAL_PARAMETER:
			case LITERAL_UNDEFINED:
			case STRING:
			case HASH:
			case LITERAL_PI:
			case LITERAL_LNBASE:
			case STRUCTDEC:
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
				expr=factor(ns,ns);
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
				recover(ex,_tokenSet_27);
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
				recover(ex,_tokenSet_66);
			} else {
			  throw ex;
			}
		}
		return code;
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
			_loop356:
			do {
				if ((LA(1)==DOT)) {
					match(DOT);
					match(NAME);
				}
				else {
					break _loop356;
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
	
	protected final FunctionCall  functionCall(
		Container ns,Container functionNs
	) throws RecognitionException, TokenStreamException {
		FunctionCall call;
		
		Token  modelName = null;
		Token  functionName = null;
		Token  functionName3 = null;
		Token  functionName2 = null;
		Token  lpar = null;
		
		call = null;
		Function called = null;
		Evaluable arg = null;
		LinkedList args = null;
		FormalArgument       formalArguments[] = null;
		Type       expectedType = null;
		int        curArgument = 0;
		
		
		try {      // for error handling
			{
			boolean synPredMatched268 = false;
			if (((LA(1)==NAME))) {
				int _m268 = mark();
				synPredMatched268 = true;
				inputState.guessing++;
				try {
					{
					match(NAME);
					match(DOT);
					match(NAME);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched268 = false;
				}
				rewind(_m268);
inputState.guessing--;
			}
			if ( synPredMatched268 ) {
				modelName = LT(1);
				match(NAME);
				match(DOT);
				functionName = LT(1);
				match(NAME);
				if ( inputState.guessing==0 ) {
					
									Model model = resolveOrFixModelName(functionNs, modelName.getText(), modelName.getLine());
									called = (Function) model.getRealElement (Function.class, functionName.getText());
									if (called == null)
									{
										reportError (formatMessage ("err_functionRef_weird", functionName.getText(),
											model.toString()), functionName.getLine());
									}
								
				}
			}
			else {
				boolean synPredMatched270 = false;
				if (((LA(1)==LITERAL_INTERLIS))) {
					int _m270 = mark();
					synPredMatched270 = true;
					inputState.guessing++;
					try {
						{
						match(LITERAL_INTERLIS);
						match(DOT);
						match(NAME);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched270 = false;
					}
					rewind(_m270);
inputState.guessing--;
				}
				if ( synPredMatched270 ) {
					match(LITERAL_INTERLIS);
					match(DOT);
					functionName3 = LT(1);
					match(NAME);
					if ( inputState.guessing==0 ) {
						
										Model model = modelInterlis;
										called = (Function) model.getRealElement (Function.class, functionName3.getText());
										if (called == null)
										{
											reportError (formatMessage ("err_functionRef_weird", functionName3.getText(),
												model.toString()), functionName3.getLine());
										}
									
					}
				}
				else if ((LA(1)==NAME)) {
					functionName2 = LT(1);
					match(NAME);
					if ( inputState.guessing==0 ) {
						
										Model model = (Model) functionNs.getContainerOrSame(Model.class);
										called = (Function) model.getRealElement (Function.class, functionName2.getText());
									      if (called == null){
										// unqualified name; search also in unqualified imported models
										called = (Function) model.getImportedElement(Function.class, functionName2.getText());
									      }
										if (called == null)
										{
											reportError (formatMessage ("err_functionRef_weird", functionName2.getText(),
												model.toString()), functionName2.getLine());
										}
									
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
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
				arg=argument(ns, expectedType,functionNs);
				if ( inputState.guessing==0 ) {
					
					args.add (arg);
					
				}
				{
				_loop272:
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
						break _loop272;
					}
					
				} while (true);
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
					recover(ex,_tokenSet_27);
				} else {
				  throw ex;
				}
			}
			return call;
		}
		
	protected final PathEl  pathEl(
		Viewable currentViewable
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
						
								// ReferenceAttribute?
								AttributeDef refattr=null;
								refattr=findAttribute(currentViewable,n.getText());
								if(refattr!=null && refattr.getDomainResolvingAliases() instanceof ReferenceType){
									el=new PathElRefAttr(refattr);
								}else if(refattr!=null && refattr.getDomainResolvingAliases() instanceof ObjectType){
									ObjectType ref=(ObjectType)refattr.getDomainResolvingAliases();
									el=new PathElBase(currentViewable,n.getText(),ref.getRef());
								}else if(currentViewable.findRole(n.getText())!=null){
									// currentView is an Association? -> role name
									el=new PathElAssocRole(currentViewable.findRole(n.getText()));
								}else if(currentViewable.findOpposideRole(n.getText())!=null){
									// currentView is an AbstractClassDef -> role name
									RoleDef oppend=currentViewable.findOpposideRole(n.getText());
									// check if only one link object
									if(oppend.getCardinality().getMaximum()>1){
										// rolename leads to multiple objects
										reportError (formatMessage ("err_pathEl_rolenameMultipleObjects",
											n.getText(),currentViewable.toString()), n.getLine());
									}
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
				recover(ex,_tokenSet_67);
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
							// check if only one link object
							if(targetRole.getCardinality().getMaximum()>1){
								// association path leads to multiple objects
								reportError (formatMessage ("err_associationPath_multipleObjects",
									roleName.getText(),currentViewable.toString()), roleName.getLine());
							}
							el=new AssociationPath(targetRole);
							
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_67);
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
				else if ((_tokenSet_67.member(LA(1)))) {
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
				recover(ex,_tokenSet_67);
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
				recover(ex,_tokenSet_68);
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
		
		
			Viewable ref;
			boolean classRequired=(expectedType instanceof ClassType);
			arg=null;
			
		
		try {      // for error handling
			if (((_tokenSet_29.member(LA(1))))&&(classRequired)) {
				ref=viewableRef(functionNs);
				if ( inputState.guessing==0 ) {
					
								arg=new ViewableAlias(null,ref);
							
				}
			}
			else if ((_tokenSet_66.member(LA(1)))) {
				arg=expression(ns,expectedType,functionNs);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
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
		return arg;
	}
	
	protected final FormalArgument  formalArgument(
		Container scope, int line
	) throws RecognitionException, TokenStreamException {
		FormalArgument arg;
		
		Token  n = null;
		arg=null;
			Type domain=null;
			
		
		try {      // for error handling
			n = LT(1);
			match(NAME);
			match(COLON);
			domain=argumentType(scope,line);
			if ( inputState.guessing==0 ) {
				
							arg=new FormalArgument(n.getText(),domain);
						
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
		return arg;
	}
	
	protected final Type  argumentType(
		Container scope,int line
	) throws RecognitionException, TokenStreamException {
		Type domain;
		
		
				Viewable ref=null;
				domain=null;
			
		
		try {      // for error handling
			if ((_tokenSet_20.member(LA(1)))) {
				domain=attrTypeDef(scope,true,null,line);
			}
			else if ((LA(1)==LITERAL_OBJECT)) {
				match(LITERAL_OBJECT);
				match(LITERAL_OF);
				{
				switch ( LA(1)) {
				case LITERAL_INTERLIS:
				case LITERAL_REFSYSTEM:
				case NAME:
				case LITERAL_SIGN:
				case LITERAL_METAOBJECT:
				{
					ref=viewableRef(scope);
					break;
				}
				case LITERAL_ANYCLASS:
				{
					match(LITERAL_ANYCLASS);
					if ( inputState.guessing==0 ) {
						ref=modelInterlis.ANYCLASS;
					}
					break;
				}
				case LITERAL_ANYSTRUCTURE:
				{
					match(LITERAL_ANYSTRUCTURE);
					if ( inputState.guessing==0 ) {
						ref=modelInterlis.ANYSTRUCTURE;
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
					
						       		domain=new ObjectType(ref);
						
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_69);
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
				recover(ex,_tokenSet_70);
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
				
					base=getBaseViewable(scope,baseName.getText(),baseName.getLine());
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
			_loop313:
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
					break _loop313;
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
	}
	
	protected final Selection  selection(
		Viewable view,Container functionNs
	) throws RecognitionException, TokenStreamException {
		Selection sel;
		
		
				sel = null;
				Evaluable logex = null;
				Viewable ref;
				LinkedList base=new LinkedList();
			
		
		try {      // for error handling
			match(LITERAL_WHERE);
			logex=expression(view, /* expectedType */ predefinedBooleanType,functionNs);
			if ( inputState.guessing==0 ) {
				
							sel = new ExpressionSelection(view, logex);
						
			}
			match(SEMI);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_71);
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
			
		
		try {      // for error handling
			{
			if ((LA(1)==LITERAL_ATTRIBUTE)) {
				match(LITERAL_ATTRIBUTE);
			}
			else if ((_tokenSet_72.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			_loop320:
			do {
				if ((LA(1)==LITERAL_ALL)) {
					all = LT(1);
					match(LITERAL_ALL);
					match(LITERAL_OF);
					v = LT(1);
					match(NAME);
					match(SEMI);
					if ( inputState.guessing==0 ) {
						
							Viewable allOf=null;
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
							allOf=getBaseViewable(attrScope,v.getText(),v.getLine());
						if (allOf == null)
						{
						reportError(
						formatMessage ("err_viewable_noSuchBase",
						v.getText(), attrScope.toString()),
						v.getLine());
						}
						}
						}
						
						if (allOf != null)
						{
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
					boolean synPredMatched319 = false;
					if (((LA(1)==NAME))) {
						int _m319 = mark();
						synPredMatched319 = true;
						inputState.guessing++;
						try {
							{
							match(NAME);
							mods=properties(ch.interlis.ili2c.metamodel.Properties.eABSTRACT
		|ch.interlis.ili2c.metamodel.Properties.eEXTENDED
		|ch.interlis.ili2c.metamodel.Properties.eFINAL
		);
							match(COLONEQUALS);
							}
						}
						catch (RecognitionException pe) {
							synPredMatched319 = false;
						}
						rewind(_m319);
inputState.guessing--;
					}
					if ( synPredMatched319 ) {
						n = LT(1);
						match(NAME);
						mods=properties(ch.interlis.ili2c.metamodel.Properties.eABSTRACT
		|ch.interlis.ili2c.metamodel.Properties.eEXTENDED
		|ch.interlis.ili2c.metamodel.Properties.eFINAL
		);
						match(COLONEQUALS);
						factor(view,view);
						if ( inputState.guessing==0 ) {
							/* TODO viewAttributes factor */
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
											// always final
											attrib.setFinal(true);
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
					else if ((LA(1)==NAME)) {
						attributeDef(view);
					}
					else {
						break _loop320;
					}
					}
				} while (true);
				}
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_73);
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
			match(SEMI);
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
			int _cnt295=0;
			_loop295:
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
					if ( _cnt295>=1 ) { break _loop295; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt295++;
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
			match(SEMI);
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
			int _cnt298=0;
			_loop298:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					viewable=renamedViewableRef(container);
					if ( inputState.guessing==0 ) {
						aliases.add(viewable);
					}
				}
				else {
					if ( _cnt298>=1 ) { break _loop298; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt298++;
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
			match(SEMI);
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
				cols=uniqueEl(view);
				match(RPAREN);
				if ( inputState.guessing==0 ) {
					
					view.setEqual(cols);
								
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
				recover(ex,_tokenSet_70);
			} else {
			  throw ex;
			}
		}
		return view;
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
			_loop304:
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
					break _loop304;
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
			match(SEMI);
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
				recover(ex,_tokenSet_34);
			} else {
			  throw ex;
			}
		}
		return ref;
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
				recover(ex,_tokenSet_74);
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
			_loop332:
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
					break _loop332;
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
				recover(ex,_tokenSet_21);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final SignInstruction  condSigParamAssignment(
		Graphic graph,  Table signTab
	) throws RecognitionException, TokenStreamException {
		SignInstruction instruct;
		
		
		Evaluable            restrictor = null;
		List                 paramAssignments = null;
		ParameterAssignment  assign = null;
		Viewable basedOn=graph.getBasedOn();
		
		instruct = null;
		
		
		try {      // for error handling
			{
			if ((LA(1)==LITERAL_WHERE)) {
				match(LITERAL_WHERE);
				restrictor=expression(basedOn, /* expectedType */ predefinedBooleanType,graph);
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
			_loop336:
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
					break _loop336;
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
				recover(ex,_tokenSet_75);
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
			case NAME:
			case LITERAL_PARAMETER:
			case LITERAL_UNDEFINED:
			case STRING:
			case HASH:
			case LITERAL_PI:
			case LITERAL_LNBASE:
			case STRUCTDEC:
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
				recover(ex,_tokenSet_61);
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
			attrPath=attributePath(basedOn);
			match(LPAREN);
			cond=enumAssignment(graph, expectedType,metaobjectclass);
			if ( inputState.guessing==0 ) {
				
				items = new LinkedList();
				if (cond != null)
				items.add (cond);
				
			}
			{
			_loop341:
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
					break _loop341;
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
				recover(ex,_tokenSet_61);
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
			if ((_tokenSet_26.member(LA(1)))) {
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
				recover(ex,_tokenSet_56);
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
				recover(ex,_tokenSet_56);
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
		return mod;
	}
	
	protected final void enumNameListHelper(
		List namList
	) throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
		try {      // for error handling
			boolean synPredMatched361 = false;
			if (((LA(1)==DOT))) {
				int _m361 = mark();
				synPredMatched361 = true;
				inputState.guessing++;
				try {
					{
					match(DOT);
					match(NAME);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched361 = false;
				}
				rewind(_m361);
inputState.guessing--;
			}
			if ( synPredMatched361 ) {
				match(DOT);
				n = LT(1);
				match(NAME);
				if ( inputState.guessing==0 ) {
					namList.add(n.getText());
				}
				enumNameListHelper(namList);
			}
			else if ((_tokenSet_51.member(LA(1)))) {
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
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"INTERLIS\"",
		"';'",
		"\"REFSYSTEM\"",
		"\"MODEL\"",
		"\"SYMBOLOGY\"",
		"\"TYPE\"",
		"NAME",
		"'('",
		"')'",
		"\"TRANSLATION\"",
		"\"OF\"",
		"'='",
		"\"CONTRACT\"",
		"\"ISSUED\"",
		"\"BY\"",
		"EXPLANATION",
		"\"IMPORTS\"",
		"\"UNQUALIFIED\"",
		"','",
		"'.'",
		"\"VIEW\"",
		"\"TOPIC\"",
		"\"EXTENDS\"",
		"\"OID\"",
		"\"AS\"",
		"\"DEPENDS\"",
		"\"ON\"",
		"\"CLASS\"",
		"\"STRUCTURE\"",
		"\"ATTRIBUTE\"",
		"\"PARAMETER\"",
		"':'",
		"':='",
		"\"MANDATORY\"",
		"\"BAG\"",
		"\"LIST\"",
		"\"RESTRICTED\"",
		"\"TO\"",
		"\"REFERENCE\"",
		"\"ANYCLASS\"",
		"\"ANYSTRUCTURE\"",
		"\"ASSOCIATION\"",
		"\"DERIVED\"",
		"\"FROM\"",
		"\"END\"",
		"'--'",
		"'-<>'",
		"'-<#>'",
		"'{'",
		"'*'",
		"'..'",
		"'}'",
		"\"DOMAIN\"",
		"\"UNDEFINED\"",
		"\"URI\"",
		"\"NAME\"",
		"\"TEXT\"",
		"STRING",
		"\"ORDERED\"",
		"\"CIRCULAR\"",
		"\"FINAL\"",
		"'#'",
		"\"OTHERS\"",
		"\"HALIGNMENT\"",
		"\"VALIGNMENT\"",
		"\"BOOLEAN\"",
		"\"NUMERIC\"",
		"'['",
		"']'",
		"\"CLOCKWISE\"",
		"\"COUNTERCLOCKWISE\"",
		"'<'",
		"'>'",
		"\"PI\"",
		"\"LNBASE\"",
		"STRUCTDEC",
		"\"COORD\"",
		"\"ROTATION\"",
		"'->'",
		"\"ANY\"",
		"\"BASKET\"",
		"\"DIRECTED\"",
		"\"POLYLINE\"",
		"\"SURFACE\"",
		"\"AREA\"",
		"\"VERTEX\"",
		"\"WITHOUT\"",
		"\"OVERLAPS\"",
		"\"LINE\"",
		"\"ATTRIBUTES\"",
		"\"WITH\"",
		"\"ARCS\"",
		"\"STRAIGHTS\"",
		"\"FORM\"",
		"\"UNIT\"",
		"\"ABSTRACT\"",
		"\"FUNCTION\"",
		"'/'",
		"\"CONTINUOUS\"",
		"\"SIGN\"",
		"'~'",
		"\"METAOBJECT\"",
		"\"CONSTRAINT\"",
		"'<='",
		"'>='",
		"'%'",
		"\"EXISTENCE\"",
		"\"REQUIRED\"",
		"\"IN\"",
		"\"OR\"",
		"\"UNIQUE\"",
		"\"LOCAL\"",
		"\"CONSTRAINTS\"",
		"\"AND\"",
		"\"NOT\"",
		"\"DEFINED\"",
		"'=='",
		"'!='",
		"'<>'",
		"\"THIS\"",
		"\"THISAREA\"",
		"\"THATAREA\"",
		"\"PARENT\"",
		"'\\\\'",
		"\"AGGREGATES\"",
		"\"FIRST\"",
		"\"LAST\"",
		"\"OBJECT\"",
		"\"PROJECTION\"",
		"\"JOIN\"",
		"\"NULL\"",
		"\"UNION\"",
		"\"AGGREGATION\"",
		"\"ALL\"",
		"\"EQUAL\"",
		"\"INSPECTION\"",
		"\"BASE\"",
		"\"EXTENDED\"",
		"\"WHERE\"",
		"\"GRAPHIC\"",
		"\"BASED\"",
		"\"ACCORDING\"",
		"\"WHEN\"",
		"\"DATA\"",
		"\"EXTERNAL\"",
		"\"TRANSIENT\"",
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
		"'@'",
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
		long[] data = { 2L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { -9191846770738980832L, 514035017832210304L, 262144L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 962L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 72374277059379264L, 4504235550965760L, 32768L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 72339092687290432L, 635923595264L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 281629595534336L, 1200666697531392L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 137438953472L, 1200666697531392L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 72374260382826560L, 4504166563053568L, 32768L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 8388640L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 4019917658051696L, 3848307474432L, 88496L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = { 68724232224L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { 72374329102864480L, 4504166898602112L, 32768L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = { 281474976710656L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { 70368811321344L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = { 281612415665152L, 1200666697531392L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { 72374259845955648L, 4504166563053568L, 32768L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = { 117481827996309600L, 518505099602172288L, 120320L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = { 1202595598368L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = { 281629595534336L, 1200666697531392L, 512L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	private static final long[] mk_tokenSet_19() {
		long[] data = { 281629595533312L, 1200666697531392L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
	private static final long[] mk_tokenSet_20() {
		long[] data = { 2017617999757839440L, 2748811673720L, 29360128L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
	private static final long[] mk_tokenSet_21() {
		long[] data = { 281474976711680L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
	private static final long[] mk_tokenSet_22() {
		long[] data = { 1168235827232L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
	private static final long[] mk_tokenSet_23() {
		long[] data = { 2017617037685165136L, 2748811673720L, 29360128L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
	private static final long[] mk_tokenSet_24() {
		long[] data = { 68720005152L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_24 = new BitSet(mk_tokenSet_24());
	private static final long[] mk_tokenSet_25() {
		long[] data = { 1024L, -576460752303423488L, 1L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_25 = new BitSet(mk_tokenSet_25());
	private static final long[] mk_tokenSet_26() {
		long[] data = { 2449958197289549824L, 57346L, 29360128L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_26 = new BitSet(mk_tokenSet_26());
	private static final long[] mk_tokenSet_27() {
		long[] data = { 4200480L, 513999695752730624L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_27 = new BitSet(mk_tokenSet_27());
	private static final long[] mk_tokenSet_28() {
		long[] data = { 8796093039696L, 2748779069440L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_28 = new BitSet(mk_tokenSet_28());
	private static final long[] mk_tokenSet_29() {
		long[] data = { 1104L, 2748779069440L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_29 = new BitSet(mk_tokenSet_29());
	private static final long[] mk_tokenSet_30() {
		long[] data = { 2017612639638652928L, 1147000L, 29360128L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_30 = new BitSet(mk_tokenSet_30());
	private static final long[] mk_tokenSet_31() {
		long[] data = { 1168235860000L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_31 = new BitSet(mk_tokenSet_31());
	private static final long[] mk_tokenSet_32() {
		long[] data = { 71571339778080L, 262144L, 22016L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_32 = new BitSet(mk_tokenSet_32());
	private static final long[] mk_tokenSet_33() {
		long[] data = { 70368744210432L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_33 = new BitSet(mk_tokenSet_33());
	private static final long[] mk_tokenSet_34() {
		long[] data = { 4229152L, 262144L, 22016L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_34 = new BitSet(mk_tokenSet_34());
	private static final long[] mk_tokenSet_35() {
		long[] data = { 281621005599744L, 1200666697531392L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_35 = new BitSet(mk_tokenSet_35());
	private static final long[] mk_tokenSet_36() {
		long[] data = { 8796093023312L, 2748779069440L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_36 = new BitSet(mk_tokenSet_36());
	private static final long[] mk_tokenSet_37() {
		long[] data = { 54043264248451104L, 262400L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_37 = new BitSet(mk_tokenSet_37());
	private static final long[] mk_tokenSet_38() {
		long[] data = { 2017612639638652928L, 32604280L, 29360128L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_38 = new BitSet(mk_tokenSet_38());
	private static final long[] mk_tokenSet_39() {
		long[] data = { 72374277059380288L, 4504235550965760L, 32768L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_39 = new BitSet(mk_tokenSet_39());
	private static final long[] mk_tokenSet_40() {
		long[] data = { 68720005152L, 369098752L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_40 = new BitSet(mk_tokenSet_40());
	private static final long[] mk_tokenSet_41() {
		long[] data = { 68720005152L, 335544320L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_41 = new BitSet(mk_tokenSet_41());
	private static final long[] mk_tokenSet_42() {
		long[] data = { 68720005152L, 268435456L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_42 = new BitSet(mk_tokenSet_42());
	private static final long[] mk_tokenSet_43() {
		long[] data = { 4503668351569952L, 3712L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_43 = new BitSet(mk_tokenSet_43());
	private static final long[] mk_tokenSet_44() {
		long[] data = { 4503668351569952L, 3584L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_44 = new BitSet(mk_tokenSet_44());
	private static final long[] mk_tokenSet_45() {
		long[] data = { 68724199456L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_45 = new BitSet(mk_tokenSet_45());
	private static final long[] mk_tokenSet_46() {
		long[] data = { 0L, 32832L, 29360128L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_46 = new BitSet(mk_tokenSet_46());
	private static final long[] mk_tokenSet_47() {
		long[] data = { 4200480L, 513999695752730624L, 262144L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_47 = new BitSet(mk_tokenSet_47());
	private static final long[] mk_tokenSet_48() {
		long[] data = { 18014398513682464L, 513999695752730624L, 262144L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_48 = new BitSet(mk_tokenSet_48());
	private static final long[] mk_tokenSet_49() {
		long[] data = { -4611685915343450080L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_49 = new BitSet(mk_tokenSet_49());
	private static final long[] mk_tokenSet_50() {
		long[] data = { 34363936768L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_50 = new BitSet(mk_tokenSet_50());
	private static final long[] mk_tokenSet_51() {
		long[] data = { 18014432881809440L, 513999695752730624L, 262144L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_51 = new BitSet(mk_tokenSet_51());
	private static final long[] mk_tokenSet_52() {
		long[] data = { 45036030633480224L, 137438953856L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_52 = new BitSet(mk_tokenSet_52());
	private static final long[] mk_tokenSet_53() {
		long[] data = { 36028797018963968L, 128L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_53 = new BitSet(mk_tokenSet_53());
	private static final long[] mk_tokenSet_54() {
		long[] data = { 9007199258941472L, 513999833191684224L, 262144L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_54 = new BitSet(mk_tokenSet_54());
	private static final long[] mk_tokenSet_55() {
		long[] data = { 0L, 64L, 29360128L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_55 = new BitSet(mk_tokenSet_55());
	private static final long[] mk_tokenSet_56() {
		long[] data = { 4198400L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_56 = new BitSet(mk_tokenSet_56());
	private static final long[] mk_tokenSet_57() {
		long[] data = { 32L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_57 = new BitSet(mk_tokenSet_57());
	private static final long[] mk_tokenSet_58() {
		long[] data = { 0L, 1099511627776L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_58 = new BitSet(mk_tokenSet_58());
	private static final long[] mk_tokenSet_59() {
		long[] data = { 4200480L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_59 = new BitSet(mk_tokenSet_59());
	private static final long[] mk_tokenSet_60() {
		long[] data = { 2080L, 703687441776640L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_60 = new BitSet(mk_tokenSet_60());
	private static final long[] mk_tokenSet_61() {
		long[] data = { 4128L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_61 = new BitSet(mk_tokenSet_61());
	private static final long[] mk_tokenSet_62() {
		long[] data = { 4200480L, 514140433241085952L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_62 = new BitSet(mk_tokenSet_62());
	private static final long[] mk_tokenSet_63() {
		long[] data = { 4200480L, 562949953421312L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_63 = new BitSet(mk_tokenSet_63());
	private static final long[] mk_tokenSet_64() {
		long[] data = { 0L, 504429546544568320L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_64 = new BitSet(mk_tokenSet_64());
	private static final long[] mk_tokenSet_65() {
		long[] data = { 4200480L, 9570149208162304L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_65 = new BitSet(mk_tokenSet_65());
	private static final long[] mk_tokenSet_66() {
		long[] data = { 2449958214469422096L, -522417556774920190L, 29360129L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_66 = new BitSet(mk_tokenSet_66());
	private static final long[] mk_tokenSet_67() {
		long[] data = { 4200480L, 514140433241348096L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_67 = new BitSet(mk_tokenSet_67());
	private static final long[] mk_tokenSet_68() {
		long[] data = { 0L, 256L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_68 = new BitSet(mk_tokenSet_68());
	private static final long[] mk_tokenSet_69() {
		long[] data = { 528416L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_69 = new BitSet(mk_tokenSet_69());
	private static final long[] mk_tokenSet_70() {
		long[] data = { 32768L, 0L, 20480L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_70 = new BitSet(mk_tokenSet_70());
	private static final long[] mk_tokenSet_71() {
		long[] data = { 281474976744448L, 0L, 16384L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_71 = new BitSet(mk_tokenSet_71());
	private static final long[] mk_tokenSet_72() {
		long[] data = { 281612415665152L, 1200666697531392L, 512L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_72 = new BitSet(mk_tokenSet_72());
	private static final long[] mk_tokenSet_73() {
		long[] data = { 281612415664128L, 1200666697531392L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_73 = new BitSet(mk_tokenSet_73());
	private static final long[] mk_tokenSet_74() {
		long[] data = { 32768L, 0L, 65536L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_74 = new BitSet(mk_tokenSet_74());
	private static final long[] mk_tokenSet_75() {
		long[] data = { 4194336L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_75 = new BitSet(mk_tokenSet_75());
	
	}
