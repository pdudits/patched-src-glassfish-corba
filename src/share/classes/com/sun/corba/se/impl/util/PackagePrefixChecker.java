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
package com.sun.corba.se.impl.util;

import java.io.File;

/**
 * PackagePrefixChecker provides static utility methods for getting package prefixes.
 * @author M. Mortazavi
 */

public final class PackagePrefixChecker 
{
    private static final String PACKAGE_PREFIX = "org.omg.stub.";

    public static String packagePrefix(){ return PACKAGE_PREFIX;}

    public static String correctPackageName (String p)
    {
	if (isOffendingPackage(p))
	    return PACKAGE_PREFIX+p;
	else 
	    return p;
    }

    public static boolean isOffendingPackage(String p)
    {
        return p!=null && hasOffendingPrefix(p);
    }

    public static boolean hasOffendingPrefix(String p)
    {
        return 
            p.startsWith("java.") || p.equals("java")
	    // || p.startsWith("com.sun.") || p.equals("com.sun")
	    || p.startsWith("net.jini.") || p.equals("net.jini")
	    || p.startsWith("jini.") || p.equals("jini")
	    || p.startsWith("javax.") || p.equals("javax") ;
    }

    public static boolean hasBeenPrefixed(String p)
    {
        return p.startsWith(packagePrefix());
    }

    public static String withoutPackagePrefix(String p)
    {
        if (hasBeenPrefixed(p)) 
	    return p.substring(packagePrefix().length());
        else 
	    return p;
    }
}