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
// Created       : 2000 Oct 16 (Mon) 16:49:37 by Harold Carr.
// Last Modified : 2003 Feb 11 (Tue) 14:10:14 by Harold Carr.
//

package corba.connectintercept_1_4;

import com.sun.corba.se.spi.legacy.interceptor.RequestInfoExt;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

public class SRI
    extends
        org.omg.CORBA.LocalObject
    implements
	ServerRequestInterceptor
{

    public static final String baseMsg = SRI.class.getName();

    public int balance = 0;

    public String name() { return baseMsg; }

    public void destroy() 
    {
	if (balance != 0) {
	    throw new RuntimeException(baseMsg + ": Interceptors not balanced.");
	}
    }

    public void receive_request_service_contexts(ServerRequestInfo sri)
    {
	balance++;
	System.out.println(baseMsg + ".receive_request_service_contexts " +
			   sri.operation());
	System.out.println("    request on connection: " +
			   ((RequestInfoExt)sri).connection());
    }

    public void receive_request(ServerRequestInfo sri)
    {
	//balance++; // DO NOT DO THIS IN AN INTERMEDIATE POINT!
	System.out.println(baseMsg + ".receive_request " + sri.operation());
    }

    public void send_reply(ServerRequestInfo sri)
    {
	balance--;
	System.out.println(baseMsg + ".send_reply " + sri.operation());
    }

    public void send_exception(ServerRequestInfo sri)
    {
	balance--;
	System.out.println(baseMsg + ".send_exception " + sri.operation());
    }

    public void send_other(ServerRequestInfo sri)
    {
	balance--;
	System.out.println(baseMsg + ".send_other " + sri.operation());
    }
}


// End of file.