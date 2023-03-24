/*****************************************************************************
 *
 * MetaObject.java
 * ---------------
 *
 * Copyright (C) 1999 Adasys AG, Kronenstr. 38, 8006 Zurich, Switzerland
 *
 * Revision 0.2  February 1999    Sascha Brawer <sb@adasys.ch>
 *
 *****************************************************************************/

package ch.interlis.ili2c.metamodel;

import java.util.List;

/** Proxy to the real metaobject
*/
public class MetaObject extends AbstractLeafElement
{
  protected String name = null;
  protected Table  table = null;

  /** Constructs a new MetaObject given its name the the table of which it is
      an instance.
  */
  public MetaObject (String name, Table table)
  {
    checkNameSanity (name, /* empty names acceptable? */ false);
    this.name = name;
    this.table = table;
  }
  @Override
  public void checkTranslationOf(List<Ili2cSemanticException> errs,String name,String baseName)
    throws java.lang.IllegalStateException
  {
      super.checkTranslationOf(errs,name,baseName);
      MetaObject baseElement=(MetaObject)getTranslationOf();
      if(baseElement==null) {
          return;
      }
      if(!getName().equals(baseElement.getName())) {
          errs.add(new Ili2cSemanticException(getSourceLine(),formatMessage("err_diff_referencedMetaObjMismatch",getName(),baseElement.getName())));
      }
      Ili2cSemanticException err=null;
      err=checkElementRef(getTable(),baseElement.getTable(),getSourceLine(),"err_diff_referencedClassMismatch");
      if(err!=null) {
          errs.add(err);
      }
      
  }
  /** Returns a String useful for debugging and error messages.

      @return <code>METAOBJECT</code> followed by the fully scoped name
              of this MetaObject.
  */
  public String toString ()
  {
    return "METAOBJECT " + getScopedName (null);
  }


  /** Determines the current value of the <code>name</code> property.
      Coordinate systems are identified and used by specifying
      their name.
  */
  public String getName()
  {
    return name;
  }

  public Table getTable ()
  {
    return table;
  }
}
