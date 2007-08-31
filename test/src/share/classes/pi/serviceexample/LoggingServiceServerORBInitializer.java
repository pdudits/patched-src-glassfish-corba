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
// Created       : 2001 May 23 (Wed) 20:32:27 by Harold Carr.
// Last Modified : 2001 Sep 20 (Thu) 21:02:59 by Harold Carr.
//

package pi.serviceexample;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.CurrentHelper;
import org.omg.PortableInterceptor.ORBInitInfo;

public class LoggingServiceServerORBInitializer 
    extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.ORBInitializer
{
    public void pre_init(ORBInitInfo info)
    {
    }

    public void post_init(ORBInitInfo info)
    {
	try {

	    // Create and register the logging service interceptor.
	    // Give that interceptor references to the NameService and
	    // PICurrent to avoid further lookups (i.e., optimization).
	    // More importantly, allocate and give the interceptor
	    // a slot id which is will use to tell itself not to
	    // log calls that the interceptor makes to the logging process.

	    NamingContext nameService = 
		NamingContextHelper.narrow(
                    info.resolve_initial_references("NameService"));

	    Current piCurrent =
		CurrentHelper.narrow(
	            info.resolve_initial_references("PICurrent"));

	    int outCallIndicatorSlotId = info.allocate_slot_id();

	    LoggingServiceServerInterceptor interceptor =
		new LoggingServiceServerInterceptor(nameService,
						    piCurrent,
						    outCallIndicatorSlotId);

	    info.add_client_request_interceptor(interceptor);
	    info.add_server_request_interceptor(interceptor);
	} catch (Throwable t) {
	    System.out.println("Exception handling not shown.");
	}
    }
}
 
// End of file.