/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1996-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.legacy.connection;

import java.util.Iterator ;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INTERNAL;

import com.sun.corba.se.spi.ior.IORTemplate;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.impl.oa.poa.Policies;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.transport.SocketOrChannelContactInfoImpl;
import com.sun.corba.se.impl.transport.SocketOrChannelAcceptorImpl;

/**
 * @author Harold Carr
 */
public class SocketFactoryAcceptorImpl
    extends
	SocketOrChannelAcceptorImpl
{
    public SocketFactoryAcceptorImpl(ORB orb, int port, 
				     String name, String type)
    {
	super(orb, port, name, type);
    }

    ////////////////////////////////////////////////////
    //
    // pept Acceptor
    //

    public boolean initialize()
    {
	if (initialized) {
	    return false;
	}
	if (orb.transportDebugFlag) {
	    dprint("initialize: " + this);
	}
	try {
	    serverSocket = orb.getORBData()
		.getLegacySocketFactory().createServerSocket(type, port);
	    internalInitialize();
	} catch (Throwable t) {
	    throw wrapper.createListenerFailed( t, Integer.toString(port) ) ;
	}
	initialized = true;
	return true;
    }

    ////////////////////////////////////////////////////
    //
    // Implementation.
    //

    protected String toStringName()
    {
	return "SocketFactoryAcceptorImpl";
    }

    protected void dprint(String msg)
    {
	ORBUtility.dprint(toStringName(), msg);
    }

    // Fix for 6331566.
    // This Acceptor must NOT contribute alternate IIOP address components
    // to the standard IIOPProfileTemplate,
    // because typically this is used for special addresses (such as SSL
    // ports) that must NOT be present in tag alternate components.
    // However, this method MUST add an IIOPProfileTemplate if one is
    // not already present.
    public void addToIORTemplate( IORTemplate iorTemplate,
	Policies policies, String codebase ) 
    {
	Iterator iterator = iorTemplate.iteratorById(
            org.omg.IOP.TAG_INTERNET_IOP.value);

	String hostname = orb.getORBData().getORBServerHost();

	if (!iterator.hasNext()) {
	    // If this is the first call, create the IIOP profile template.
	    IIOPProfileTemplate iiopProfile = makeIIOPProfileTemplate(
		policies, codebase ) ;
	    iorTemplate.add(iiopProfile);
	}
    }
}

// End of file.