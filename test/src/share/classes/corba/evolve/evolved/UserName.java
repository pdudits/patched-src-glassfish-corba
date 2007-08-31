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
import java.io.*;

// Evolution of the UserName class in which the real implementation
// has been split into first and last names, and readObject/writeObject
// are used to maintain backwards compatibility on the wire.  Also,
// the UserName has a new field -- middle name, which can be null.
public class UserName implements corba.evolve.UserNameInt
{
    // SerialVersionUID generated by Kestrel serialver
    static final long serialVersionUID = 6714336166694847573L;

    private static final String TEST_NAME = "Frank Furlow";
    
    // Old representation, maintains backwards serialization
    // compatibility
    private String name = null;

    // New internal representation
    private transient String firstName = null;
    private transient String lastName = null;

    // New field
    private String middleName = null;

    public UserName() {
        firstName = new String("Frank");
        lastName = new String("Furlow");
        middleName = new String("Monte");
    }

    // Call validate after reading
    public boolean validate() {
        return (firstName != null && lastName != null
                && TEST_NAME.equals(firstName + ' ' + lastName)
                && (middleName == null || middleName.equals("Monte")));
    }

    private synchronized void writeObject(java.io.ObjectOutputStream out)
        throws IOException
    {
        name = firstName + ' ' + lastName;

        out.defaultWriteObject();
    }

    private synchronized void readObject(java.io.ObjectInputStream s)
        throws IOException, ClassNotFoundException 
    {
	s.defaultReadObject();

        if (name == null)
            throw new IOException("name is null in readObject");

        int space = name.indexOf(' ');
        if (space == -1)
            throw new IOException("Name doesn't have a space: " + name);

        firstName = name.substring(0, space);
        lastName = name.substring(space + 1, name.length());
    }
}