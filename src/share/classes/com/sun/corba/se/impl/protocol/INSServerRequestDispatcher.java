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
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.sun.corba.se.impl.protocol;

import com.sun.corba.se.pept.protocol.MessageMediator;

import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.ObjectKey;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.protocol.CorbaServerRequestDispatcher;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;

import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;

/**
 * INSServerRequestDispatcher handles all INS related discovery request. The INS Service
 * can be registered using ORB.register_initial_reference().
 * This Singleton subcontract just 
 * finds the target IOR and does location forward.
 * XXX PI points are not invoked in either dispatch() or locate() method this
 * should be fixed in Tiger.
 */ 
public class INSServerRequestDispatcher 
    implements CorbaServerRequestDispatcher 
{

    private ORB orb = null;
    private ORBUtilSystemException wrapper ;

    public INSServerRequestDispatcher( ORB orb ) {
        this.orb = orb;
	this.wrapper = orb.getLogWrapperTable().get_RPC_PROTOCOL_ORBUtil() ;
    }

    // Need to signal one of OBJECT_HERE, OBJECT_FORWARD, OBJECT_NOT_EXIST.
    public IOR locate(ObjectKey okey) { 
        // send a locate forward with the right IOR. If the insKey is not 
        // registered then it will throw OBJECT_NOT_EXIST Exception
        String insKey = new String( okey.getBytes(orb) );
        return getINSReference( insKey );
    }

    public void dispatch(MessageMediator mediator) 
    {
	CorbaMessageMediator request = (CorbaMessageMediator) mediator;
        // send a locate forward with the right IOR. If the insKey is not 
        // registered then it will throw OBJECT_NOT_EXIST Exception
        String insKey = new String( 
	    request.getObjectKeyCacheEntry().getObjectKey().getBytes(orb) );
	request.getProtocolHandler()
	    .createLocationForward(request, getINSReference( insKey ), null);
        return;
    }

    /**
     * getINSReference if it is registered in INSObjectKeyMap.
     */
    private IOR getINSReference( String insKey ) {
        IOR entry = orb.getIOR( orb.getLocalResolver().resolve( insKey ), false ) ;
        if( entry != null ) {
            // If entry is not null then the locate is with an INS Object key,
            // so send a location forward with the right IOR.
            return entry;
        }

	throw wrapper.servantNotFound() ;
    }
}