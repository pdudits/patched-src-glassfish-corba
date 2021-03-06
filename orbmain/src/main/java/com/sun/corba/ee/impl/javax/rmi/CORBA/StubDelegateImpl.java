/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * 
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 * 
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 * 
 * Contributor(s):
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

package com.sun.corba.ee.impl.javax.rmi.CORBA;

import java.io.IOException;

import java.rmi.RemoteException;

import org.omg.CORBA.ORB;

import com.sun.corba.ee.impl.ior.StubIORImpl ;
import com.sun.corba.ee.impl.presentation.rmi.StubConnectImpl ;

import com.sun.corba.ee.spi.logging.UtilSystemException ;

/**
 * Base class from which all static RMI-IIOP stubs must inherit.
 */
public class StubDelegateImpl implements javax.rmi.CORBA.StubDelegate 
{
    private static final UtilSystemException wrapper =
        UtilSystemException.self ;

    private StubIORImpl ior ;

    public synchronized StubIORImpl getIOR() 
    {
        return ior ;
    }
    
    public synchronized void setIOR( StubIORImpl ior ) 
    {
        this.ior = ior ;
    }

    public StubDelegateImpl() 
    {
        ior = null ;
    }

    /**
     * Sets the IOR components if not already set.
     */
    private synchronized void init (javax.rmi.CORBA.Stub self) 
    {
        // If the Stub is not connected to an ORB, BAD_OPERATION exception
        // will be raised by the code below.
        if (ior == null) {
            ior = new StubIORImpl(self);
        }
    }
        
    /**
     * Returns a hash code value for the object which is the same for all stubs
     * that represent the same remote object.
     * @return the hash code value.
     */
    public synchronized int hashCode(javax.rmi.CORBA.Stub self) 
    {
        init(self);
        return ior.hashCode() ;
    }

    /**
     * Compares two stubs for equality. Returns <code>true</code> when used to compare stubs
     * that represent the same remote object, and <code>false</code> otherwise.
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the <code>obj</code>
     *          argument; <code>false</code> otherwise.
     */
    public synchronized boolean equals(javax.rmi.CORBA.Stub self, java.lang.Object obj) 
    {
        if (self == obj) {
            return true;    
        }
        
        if (!(obj instanceof javax.rmi.CORBA.Stub)) {
            return false;            
        }
        
        // no need to call init() because of calls to hashCode() below

        javax.rmi.CORBA.Stub other = (javax.rmi.CORBA.Stub) obj;
        if (other.hashCode() != self.hashCode()) {
            return false;
        }

        // hashCodes being the same does not mean equality. The stubs still
        // could be pointing to different IORs. So, do a literal comparison.
        // Apparently the ONLY way to do this (other than using private 
        // reflection) is toString, because it is not possible to directly
        // access the StubDelegateImpl from the Stub.
        return self.toString().equals( other.toString() ) ;
    }

    @Override
    public synchronized boolean equals( Object obj )
    {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof StubDelegateImpl)) {
            return false;
        }

        StubDelegateImpl other = (StubDelegateImpl)obj ;

        if (ior == null) {
            return ior == other.ior;
        } else {
            return ior.equals(other.ior);
        }
    }

    @Override
    public synchronized int hashCode() {
        if (ior == null) {
            return 0;
        } else {
            return ior.hashCode();
        }
    }

    /**
     * Returns a string representation of this stub. Returns the same string
     * for all stubs that represent the same remote object.
     * @return a string representation of this stub.
     */
    public synchronized String toString(javax.rmi.CORBA.Stub self) 
    {
        if (ior == null) {
            return null;
        } else {
            return ior.toString();
        }
    }
    
    /**
     * Connects this stub to an ORB. Required after the stub is deserialized
     * but not after it is demarshalled by an ORB stream. If an unconnected
     * stub is passed to an ORB stream for marshalling, it is implicitly 
     * connected to that ORB. Application code should not call this method
     * directly, but should call the portable wrapper method 
     * {@link javax.rmi.PortableRemoteObject#connect}.
     * @param orb the ORB to connect to.
     * @exception RemoteException if the stub is already connected to a different
     * ORB, or if the stub does not represent an exported remote or local object.
     */
    public synchronized void connect(javax.rmi.CORBA.Stub self, ORB orb) 
        throws RemoteException 
    {
        ior = StubConnectImpl.connect( ior, self, self, orb ) ;
    }

    /**
     * Serialization method to restore the IOR state.
     */
    public synchronized void readObject(javax.rmi.CORBA.Stub self, 
        java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException 
    {
        if (ior == null) {
            ior = new StubIORImpl();
        }

        ior.doRead( stream ) ;
    }

    /**
     * Serialization method to save the IOR state.
     * @serialData The length of the IOR type ID (int), followed by the IOR type ID
     * (byte array encoded using ISO8859-1), followed by the number of IOR profiles
     * (int), followed by the IOR profiles.  Each IOR profile is written as a 
     * profile tag (int), followed by the length of the profile data (int), followed
     * by the profile data (byte array).
     */
    public synchronized void writeObject(javax.rmi.CORBA.Stub self, 
        java.io.ObjectOutputStream stream) throws IOException 
    {
        init(self);
        ior.doWrite( stream ) ;
    }
}
