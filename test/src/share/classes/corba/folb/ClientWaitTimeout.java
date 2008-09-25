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
//
// Created       : 2005 Sep 23 (Fri) 15:17:47 by Harold Carr.
// Last Modified : 2005 Oct 03 (Mon) 10:28:16 by Harold Carr.
//

package corba.folb;

import java.util.Hashtable;
import java.util.Properties;
import javax.naming.InitialContext;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.SystemException;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.ORBUtility;

import corba.framework.Controller;
import corba.hcks.U;

import org.testng.annotations.BeforeSuite ;
import org.testng.annotations.Test ;

import org.testng.Assert ;

/**
 * @author Harold Carr
 */
public class ClientWaitTimeout extends ClientBase {

    @BeforeSuite
    public void clientSetup() throws Exception {
	    Properties props = getDefaultProperties();
            
	    // Set retry timeout to 5 seconds.
	    props.setProperty(ORBConstants.WAIT_FOR_RESPONSE_TIMEOUT, "5000");
	    props.setProperty(ORBConstants.DEBUG_PROPERTY,
			      "transport,subcontract");

	    setup(props);
	    circularSetup();
    }

    @Test
    public void test() throws Exception {
        dprint("--------------------------------------------------");
        dprint("neverReturns - so should timeout in wait");
        dprint("--------------------------------------------------");

        try {
            testRfmWithAddressesWithLabel.neverReturns();
            Assert.fail( "should not return, but did return" ) ;
        } catch (java.rmi.MarshalException e) {
            SystemException cf = wrapper.communicationsTimeoutWaitingForResponse(
                    CompletionStatus.COMPLETED_MAYBE, -1);
            checkMarshalException("neverReturns", e, cf);
        }
    }

    public static void main(String[] av) {
        doMain( ClientWaitTimeout.class ) ;
    }
}

// End of file.