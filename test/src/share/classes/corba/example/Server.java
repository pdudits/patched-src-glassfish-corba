/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2000-2007 Sun Microsystems, Inc. All rights reserved.
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
package corba.example;

import HelloApp.*;
import com.sun.corba.se.impl.orbutil.ORBConstants;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import java.util.Properties ;
import org.omg.PortableServer.*;

/**
 * Servant implementation
 */
class helloServant extends helloPOA
{
    public String sayHello()
    {
        return "Hello world!";
    }
}

public class Server
{
    public static void main(String args[])
    {
        try {
      	    Properties props = new Properties(System.getProperties());
// Examples of how to set ORB debug properties and default fragment size
//	    props.put(ORBConstants.DEBUG_PROPERTY, "transport,giop");
//            props.put(ORBConstants.GIOP_FRAGMENT_SIZE, "32");
            ORB orb = ORB.init(args, props);
      
            // Get rootPOA
            POA rootPOA = (POA)orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();
      
            // create servant and register it with the ORB
            helloServant helloRef = new helloServant();
      
            byte[] id = rootPOA.activate_object(helloRef);
      
            // get the root naming context
            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
      
            // bind the Object Reference in Naming
            NameComponent nc = new NameComponent("Hello", "");
            NameComponent path[] = {nc};
      
            org.omg.CORBA.Object ref = rootPOA.id_to_reference(id);
            
            ncRef.rebind(path, ref);
            
            // Emit the handshake the test framework expects
            // (can be changed in Options by the running test)
            System.out.println ("Server is ready.");

            // Wait for clients
            orb.run();
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);

            // Make sure to exit with a value greater than 0 on
            // error.
            System.exit(1);
        }
    }
}