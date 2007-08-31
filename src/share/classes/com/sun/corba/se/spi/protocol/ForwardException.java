/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.spi.protocol;

import org.omg.CORBA.BAD_PARAM ;

import com.sun.corba.se.impl.orbutil.ORBUtility ;

import com.sun.corba.se.spi.ior.IOR ;

import com.sun.corba.se.spi.orb.ORB ;

/**
 * Thrown to signal an OBJECT_FORWARD or LOCATION_FORWARD
 */
public class ForwardException extends RuntimeException {
    private ORB orb ;
    private org.omg.CORBA.Object obj;
    private IOR ior ;

    public ForwardException( ORB orb, IOR ior ) {
        super();

	this.orb = orb ;
	this.obj = null ;
	this.ior = ior ;
    }

    public ForwardException( ORB orb, org.omg.CORBA.Object obj) {
        super();

	// This check is done early so that no attempt
	// may be made to do a location forward to a local
	// object.  Doing this lazily would allow 
	// forwarding to locals in some restricted cases.
	if (obj instanceof org.omg.CORBA.LocalObject)
	    throw new BAD_PARAM() ;

	this.orb = orb ;
	this.obj = obj ;
	this.ior = null ;
    }

    public synchronized org.omg.CORBA.Object getObject()
    {
	if (obj == null) {
	    obj = ORBUtility.makeObjectReference( ior ) ;
	}

	return obj ;
    }

    public synchronized IOR getIOR() 
    {
	if (ior == null) {
	    ior = orb.getIOR( obj, false ) ;
	}

	return ior ;
    }
}