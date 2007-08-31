/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.corba.se.spi.presentation.rmi ;

import java.io.PrintStream ;

import java.util.Map ;

import java.lang.reflect.Method ;
import java.lang.reflect.InvocationHandler ;

import javax.rmi.CORBA.Tie ;

import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.orbutil.proxy.InvocationHandlerFactory ;


/** Provides access to RMI-IIOP stubs and ties.  
 * Any style of stub and tie generation may be used.  
 * This includes compiler generated stubs and runtime generated stubs 
 * as well as compiled and reflective ties.  There is normally
 * only one instance of this interface per VM.  The instance
 * is obtained from the static method
 * com.sun.corba.se.spi.orb.ORB.getPresentationManager.
 * <p>
 * Note that
 * the getClassData and getDynamicMethodMarshaller methods
 * maintain caches to avoid redundant computation.
 */
public interface PresentationManager
{
    /** Creates StubFactory and Tie instances.
     */
    public interface StubFactoryFactory
    {
	/** Return the standard name of a stub (according to the RMI-IIOP specification
	 * and rmic).  This is needed so that the name of a stub is known for
	 * standalone clients of the app server.
	 */
	String getStubName( String className ) ;

	/** Create a stub factory for stubs for the interface whose type is given by
	 * className.  className may identify either an IDL interface or an RMI-IIOP
	 * interface.  
	 * @param className The name of the remote interface as a Java class name.
	 * @param isIDLStub True if className identifies an IDL stub, else false.
	 * @param remoteCodeBase The CodeBase to use for loading Stub classes, if
	 * necessary (may be null or unused).
	 * @param expectedClass The expected stub type (may be null or unused).
	 * @param classLoader The classLoader to use (may be null).
	 */
	PresentationManager.StubFactory createStubFactory( String className, 
	    boolean isIDLStub, String remoteCodeBase, Class expectedClass, 
	    ClassLoader classLoader);

	/** Return a Tie for the given class.
	 */
	Tie getTie( Class cls ) ;

	/** Return whether or not this StubFactoryFactory creates StubFactory
	 * instances that create dynamic stubs and ties.  At the top level, 
	 * true indicates that rmic -iiop is not needed for generating stubs
	 * or ties.
	 */
	boolean createsDynamicStubs() ;
    }

    /** Creates the actual stub needed for RMI-IIOP remote
     * references.
     */
    public interface StubFactory
    {
	/** Create a new dynamic stub.  It has the type that was
	 * used to create this factory.
	 */
	org.omg.CORBA.Object makeStub() ;

	/** Return the repository ID information for all Stubs
	 * created by this stub factory.
	 */
	String[] getTypeIds() ;
    }

    public interface ClassData 
    {
	/** Get the class used to create this ClassData instance
	 */
	Class getMyClass() ;

	/** Get the IDLNameTranslator for the class used to create
	 * this ClassData instance.
	 */
	IDLNameTranslator getIDLNameTranslator() ;

	/** Return the array of repository IDs for all of the remote
	 * interfaces implemented by this class.
	 */
	String[] getTypeIds() ;

	/** Get the InvocationHandlerFactory that is used to create
	 * an InvocationHandler for dynamic stubs of the type of the
	 * ClassData.  
	 */
	InvocationHandlerFactory getInvocationHandlerFactory() ;

	/** Get the dictionary for this ClassData instance.
	 * This is used to hold class-specific information for a Class
	 * in the class data.  This avoids the need to create other
	 * caches for accessing the information.
	 */
	Map getDictionary() ;
    }

    /** Get the ClassData for a particular class.
     * This class may be an implementation class, in which 
     * case the IDLNameTranslator handles all Remote interfaces implemented by 
     * the class.  If the class implements more than one remote interface, and not 
     * all of the remote interfaces are related by inheritance, then the type 
     * IDs have the implementation class as element 0.  
     */
    ClassData getClassData( Class cls ) ;

    /** Given a particular method, return a DynamicMethodMarshaller 
     * for that method.  This is used for dynamic stubs and ties.
     */
    DynamicMethodMarshaller getDynamicMethodMarshaller( Method method ) ;

    /** Return the registered StubFactoryFactory.
     */
    StubFactoryFactory getStubFactoryFactory( boolean isDynamic ) ;

    /** Register the StubFactoryFactory.  Note that
     * a static StubFactoryFactory is always required for IDL.  The
     * dynamic stubFactoryFactory is optional.
     */
    void setStubFactoryFactory( boolean isDynamic, StubFactoryFactory sff ) ;

    /** Equivalent to getStubFactoryFactory( true ).getTie( null ).
     * Provided for compatibility with earlier versions of PresentationManager
     * as used in the app server.  The class argument is ignored in
     * the dynamic case, so this is safe.
     */
    Tie getTie() ;

    /** Get the correct repository ID for the given implementation
     * instance.  This is useful when using dynamic RMI with the POA.
     */
    String getRepositoryId( java.rmi.Remote impl ) ;

    /** Returns the value of the com.sun.corba.se.ORBUseDynamicStub
     * property.
     */
    boolean useDynamicStubs() ;

    /** Remove all internal references to Class cls from the 
     *  PresentationManager. This allows ClassLoaders to
     *  be garbage collected when they are no longer needed.
     */
    void flushClass( Class cls ) ;

    /** Turn on internal debugging flags, which dump information
     * about stub code generation to the PrintStream.
     */
    void enableDebug( PrintStream ps ) ;

    /** Turn off internal debugging.
     */
    void disableDebug() ;

    boolean getDebug() ;

    PrintStream getPrintStream() ;
}