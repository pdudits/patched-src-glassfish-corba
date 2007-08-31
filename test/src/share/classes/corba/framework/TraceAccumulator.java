/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package corba.framework ;

import java.util.Iterator ;
import java.util.List ;
import java.util.ArrayList ;

public class TraceAccumulator implements MethodEventListener
{
    private List elements ;

    public TraceAccumulator()
    {
	clear() ;
    }

    public void clear() 
    {
	elements = new ArrayList() ;
    }

    private void addElement( boolean isEnter, MethodEvent event ) 
    {
	TraceElement tel = new TraceElement( isEnter, event ) ;
	elements.add( tel ) ;
    }

    public void methodEntered( MethodEvent event ) 
    {
	addElement( true, event ) ;
    }

    public void methodExited( MethodEvent event ) 
    {
	addElement( false, event ) ;
    }

    public List getTrace()	// List<TraceElement>
    {
	return elements ;
    }

    public boolean validate( List expectedTrace ) // List<TraceElement>
    {
	Iterator iter1 = elements.iterator() ;
	Iterator iter2 = expectedTrace.iterator() ;
	while (iter1.hasNext() && iter2.hasNext()) {
	    TraceElement tel1 = (TraceElement)(iter1.next()) ;
	    TraceElement tel2 = (TraceElement)(iter2.next()) ;
	    if (!tel1.equals( tel2 ))
		return false ;
	}

	return iter1.hasNext() == iter2.hasNext() ;
    }
}
