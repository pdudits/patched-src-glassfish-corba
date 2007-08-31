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
 * 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997,1998
 * RMI-IIOP v1.0
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.sun.tools.corba.se.idl.som.cff;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.lang.String;
import java.lang.System;

/**
 * This class provides messaging services for accessing, and merging
 * parameters into, translatable message text.  The text is presumed
 * to reside in a .properties file.  A "cff.properties" file that
 * contains all of the message text needed for the CFF framework itself
 * is loaded during class initialization.  All of the messages in the
 * cff.properties file that are needed by the CFF framework contain keys
 * that begin with the string "cff.".
 * <p>
 * The static method Messages.msgLoad may be used to merge additional
 * message text .properties files needed by other frameworks or user
 * programs.
 *
 * @see com.sun.tools.corba.se.idl.som.cff.Messages#msgLoad
 *
 * @author      Larry K. Raper
 */

public abstract class Messages {

    /* Class variables */


    /* Metasymbol for leading or trailing blank */
    private static final String LTB = "%B";
    /* Metasymbol for line separator */
    private static final char NL  = '\n';

    private static final String lineSeparator =
        System.getProperty ("line.separator");

    private static final Properties m = new Properties ();
    private static boolean loadNeeded = true;

    /* Class methods for message loading and formatting */

    private static final synchronized void loadDefaultProperties () {

        if (!loadNeeded)
            return;
        try {
            m.load (FileLocator.locateLocaleSpecificFileInClassPath (
                "com/sun/tools/corba/se/idl/som/cff/cff.properties"));
        } catch (IOException ioe) { }
        fixMessages (m);  /* #26964 Replace any metasymbols */
        loadNeeded = false;

    }

    /**
     * This method was introduced to fix defect #26964.  For Java 1.0.2
     * on Win NT, the escape sequence \u0020 was not being handled
     * correctly by the Java Properties class when it was the final
     * character of a line.  Instead the trailing blank was dropped
     * and the next line was swallowed as a continuation.  To work
     * around the problem, we introduced our own metasymbol to represent
     * a trailing blank.  Hence:
     *
     * Performs substitution for any metasymbols in the message
     * templates.  So far only %B is needed.  This was introduced
     * to make it more convenient for .properties files to
     * contain message templates with leading or trailing blanks
     * (although %B may actually occur anywhere in a template).
     * Subsequently, checking for '\n' has also been added.  Now,
     * wherever '\n' occurs in a message template, it is replaced
     * with the value of System.getProperty ("line.separator").
     */
    private static final void fixMessages (Properties p) {

        Enumeration keys = p.keys ();
        Enumeration elems = p.elements ();
        while (keys.hasMoreElements ()) {
            String key = (String) keys.nextElement ();
            String elem = (String) elems.nextElement ();
            int i = elem.indexOf (LTB);
            boolean changed = false;
            while (i != -1) {
                if (i == 0)
                    elem = " " + elem.substring (2);
                else
                    elem = elem.substring (0, i) + " " + elem.substring (i+2);
                changed = true;
                i = elem.indexOf (LTB);
            }
            int lsIncr = lineSeparator.length () - 1;
            for (i=0; i<elem.length (); i++) {
                if (elem.charAt (i) == NL) {
                    elem = elem.substring (0, i) +
                        lineSeparator + elem.substring (i+1);
                    i += lsIncr;
                    changed = true;
                }
            }
            if (changed)
                p.put (key, elem);
        }

    }

    /**
     * Loads additional message keys and text found in the passed
     * properties file.  The specified properties file is assumed to
     * reside in the CLASSPATH. An IOException is thrown if the loading fails.
     */
    public static final synchronized void msgLoad (String propertyFileName)
        throws IOException {

        m.load (FileLocator.locateLocaleSpecificFileInClassPath (
            propertyFileName));
        fixMessages (m);   /* #26964 Replace any metasymbols */
        loadNeeded = false;

    }

    /**
     * Returns the message text corresponding to the passed msgkey
     * string.  If the msgkey cannot be found, its value is returned
     * as the output message text.
     */
    public static final String msg (String msgkey) {

        if (loadNeeded)
            loadDefaultProperties ();
        String msgtext = m.getProperty (msgkey, msgkey);
        return msgtext;

    }

    /**
     * Returns the message text corresponding to the passed msgkey
     * string.  The message text is assumed to require the insertion
     * of a single argument, supplied by the "parm" parameter.
     * If the message text does not contain the meta characters "%1"
     * that indicate where to place the argument, the passed argument
     * is appended at the end of the message text.
     * <p>
     * If the msgkey cannot be found, its value is used as the
     * message text.
     */
    public static final String msg (String msgkey, String parm) {

        if (loadNeeded)
            loadDefaultProperties ();
        String msgtext = m.getProperty (msgkey, msgkey);
        int i = msgtext.indexOf ("%1");
        if (i >= 0) {
            String ending = "";
            if ((i+2) < msgtext.length ())
                ending = msgtext.substring (i+2);
            return msgtext.substring (0, i) + parm + ending;
        } else
            msgtext += " " + parm;
        return msgtext;

    }

    /**
     * Returns the message text corresponding to the passed msgkey
     * string.  The message text is assumed to require the insertion
     * of a single argument, supplied by the "parm" parameter.
     * If the message text does not contain the meta characters "%1"
     * that indicate where to place the argument, the passed argument
     * is appended at the end of the message text.
     * <p>
     * If the msgkey cannot be found, its value is used as the
     * message text.
     */
    public static final String msg (String msgkey, int parm) {

        return msg (msgkey, String.valueOf (parm));

    }

    /**
     * Returns the message text corresponding to the passed msgkey
     * string.  The message text is assumed to require the insertion
     * of two arguments, supplied by the "parm1" and "parm2" parameters.
     * If the message text does not contain the meta characters "%1" and
     * "%2" that indicate where to place the arguments, the passed arguments
     * are appended at the end of the message text.
     * <p>
     * If the msgkey cannot be found, its value is used as the
     * message text.
     */
    public static final String msg (String msgkey, String parm1, String parm2) {

        if (loadNeeded)
            loadDefaultProperties ();
        String result = m.getProperty (msgkey, msgkey);
        String ending = "";
        int i = result.indexOf ("%1");
        if (i >= 0) {
            if ((i+2) < result.length ())
                ending = result.substring (i+2);
            result = result.substring (0, i) + parm1 + ending;
        } else
            result += " " + parm1;
        i = result.indexOf ("%2");
        if (i >= 0) {
            ending = "";
            if ((i+2) < result.length ())
                ending = result.substring (i+2);
            result = result.substring (0, i) + parm2 + ending;
        } else
            result += " " + parm2;
        return result;

    }

    /**
     * Returns the message text corresponding to the passed msgkey
     * string.  The message text is assumed to require the insertion
     * of two arguments, supplied by the "parm1" and "parm2" parameters.
     * If the message text does not contain the meta characters "%1" and
     * "%2" that indicate where to place the arguments, the passed arguments
     * are appended at the end of the message text.
     * <p>
     * If the msgkey cannot be found, its value is used as the
     * message text.
     */
    public static final String msg (String msgkey, int parm1, String parm2) {

        return msg (msgkey, String.valueOf (parm1), parm2);

    }

    /**
     * Returns the message text corresponding to the passed msgkey
     * string.  The message text is assumed to require the insertion
     * of two arguments, supplied by the "parm1" and "parm2" parameters.
     * If the message text does not contain the meta characters "%1" and
     * "%2" that indicate where to place the arguments, the passed arguments
     * are appended at the end of the message text.
     * <p>
     * If the msgkey cannot be found, its value is used as the
     * message text.
     */
    public static final String msg (String msgkey, String parm1, int parm2) {

        return msg (msgkey, parm1, String.valueOf (parm2));

    }

    /**
     * Returns the message text corresponding to the passed msgkey
     * string.  The message text is assumed to require the insertion
     * of two arguments, supplied by the "parm1" and "parm2" parameters.
     * If the message text does not contain the meta characters "%1" and
     * "%2" that indicate where to place the arguments, the passed arguments
     * are appended at the end of the message text.
     * <p>
     * If the msgkey cannot be found, its value is used as the
     * message text.
     */
    public static final String msg (String msgkey, int parm1, int parm2) {

        return msg (msgkey, String.valueOf (parm1), String.valueOf (parm2));

    }


    /**
     * Returns the message text corresponding to the passed msgkey
     * string.  The message text is assumed to require the insertion
     * of three arguments, supplied by the "parm1", "parm2" and "parm3"
     *  parameters.
     * If the message text does not contain the meta characters "%1" and
     * "%2" that indicate where to place the arguments, the passed arguments
     * are appended at the end of the message text.
     * <p>
     * If the msgkey cannot be found, its value is used as the
     * message text.
     */
    public static final String msg (String msgkey, String parm1,
                            String parm2, String parm3) {
        if (loadNeeded)
            loadDefaultProperties ();
        String result = m.getProperty (msgkey, msgkey);
        result = substituteString(result, 1, parm1);
        result = substituteString(result, 2, parm2);
        result = substituteString(result, 3, parm3);

        return result;
    }

    /* helper function for string substition.
     @return the substituted string, it substitution is possible.
       Otherwise, return a new string with subst at the end.
     @orig: original string
     @paramNum the parameter number to search. For example,
         paramNam == 1 means search for "%1".
     @subst: string for the substitution.
    */
    private static String substituteString(String orig, int paramNum,
                     String subst){
        String result = orig;
        String paramSubst = "%"+ paramNum;
        int len = paramSubst.length();
        int index = result.indexOf (paramSubst);
        String ending = "";
        if (index >= 0) {
            if ((index+len) < result.length ())
                ending = result.substring (index+len);
            result = result.substring (0, index) + subst + ending;
        }
        else result += " " + subst;

         return result;
    }


}