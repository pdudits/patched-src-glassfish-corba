/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1998-2007 Sun Microsystems, Inc. All rights reserved.
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

package test;

import sun.rmi.registry.RegistryImpl;
import java.rmi.registry.Registry;
import java.rmi.RMISecurityManager;

public class StartRMIRegistry {

    private static Registry registry = null;
    
    /**
     * Main program to start a registry. <br>
     * The port number can be specified on the command line.
     */
    public static void main(String args[]) {
    	// Create and install the security manager
    	System.setSecurityManager(new RMISecurityManager());

    	try {
    	    int port = Registry.REGISTRY_PORT;
    	    if (args.length >= 1) {
		port = Integer.parseInt(args[0]);
    	    }
    	    registry = new RegistryImpl(port);
    	    System.out.println(Util.HANDSHAKE);


    	    // prevent registry from exiting
    	    while (true) {
    		try {
    		    // The following timeout is used because a bug in the
    		    // native C code for Thread.sleep() cause it to return
    		    // immediately for any higher value.
    		    Thread.sleep(Integer.MAX_VALUE - 1);
    		} catch (InterruptedException e) {
    		}
    	    }
    	} catch (NumberFormatException e) {
    	    System.out.println("Port is not a number.");
    	} catch (Exception e) {
    	    System.out.println("RegistryImpl.main: an exception occurred: " +
    			       e.getMessage());
    	    e.printStackTrace();
    	}
    	System.exit(1);
    }
}