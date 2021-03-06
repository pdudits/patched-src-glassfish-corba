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
 * @(#)counterImpl.java 1.6 99/10/29
 *
 * Copyright 1998, 1999 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package corba.rmipoacounter;

import java.rmi.RemoteException ;
import javax.rmi.PortableRemoteObject ;
import org.omg.CORBA.ORB ;
import java.io.File ;
import java.io.RandomAccessFile ;
import org.omg.PortableServer.POA ;

public class counterImpl extends PortableRemoteObject implements counterIF  
{
    // Temporary hack to get this test to work and keep the output
    // directory clean
    private static final String outputDirOffset 
        = "/corba/rmipoacounter/".replace('/', File.separatorChar);

    String name; 
    private int value;
    ORB orb;
    private int myid;
    private static int SERVANT_ID=1;
    private boolean debug ;

    public counterImpl(ORB orb, boolean debug) throws RemoteException
    {
        this.myid = SERVANT_ID++;
        this.orb = orb;
        this.debug = debug ;

        name = System.getProperty("output.dir") 
            + outputDirOffset
            + "counterValue";

        try { 
            File f = new File(name);
            if ( !f.exists() ) {
                RandomAccessFile file = new RandomAccessFile(f, "rw");
                value = 0;
                file.writeBytes(String.valueOf(value));
                file.close();
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public synchronized long increment(long invalue) throws RemoteException
    {
        if ( debug ) 
            System.out.println( "\nIn counterServant " + myid + 
                                " increment(), invalue = " + invalue + " Server thread is " +
                                Thread.currentThread());

        try {
            // Test Current operations
            org.omg.PortableServer.Current current = 
                (org.omg.PortableServer.Current)orb.resolve_initial_references(
                                                                               "POACurrent");
            POA poa = current.get_POA();
            byte[] oid = current.get_object_id();

            if ( debug ) 
                System.out.println( "POA = " + poa.the_name() + " objectid = " +
                                    oid);

            // Increment counter and save state
            RandomAccessFile file = new RandomAccessFile(new File(name), "rw");
            String svalue = file.readLine();
            value = Integer.parseInt(svalue);
            file.seek(0);
            value += (int)invalue;
            file.writeBytes(String.valueOf(value));
            file.close();

            System.out.println("\nIn counterServant read "+svalue+" wrote "+value);
        } catch ( Exception ex ) {
            System.err.println("ERROR in counterServant !");
            ex.printStackTrace();
            System.exit(1);
        }

        return value;
    }
}

