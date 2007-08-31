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

package com.sun.corba.se.impl.protocol.giopmsgheaders;

import com.sun.corba.se.spi.servicecontext.ServiceContextDefaults;
import com.sun.corba.se.spi.servicecontext.ServiceContexts;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orb.ObjectKeyCacheEntry;
import com.sun.corba.se.impl.encoding.CDRInputStream;
import com.sun.corba.se.impl.encoding.CDROutputStream;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.impl.encoding.CDRInputStream_1_2;
import com.sun.corba.se.impl.encoding.CDROutputStream_1_2;

import com.sun.corba.se.impl.logging.ORBUtilSystemException ;

import com.sun.corba.se.impl.orbutil.newtimer.TimingPoints ;

/**
 * This implements the GIOP 1.2 Request header.
 *
 * @author Ram Jeyaraman 05/14/2000
 * @version 1.0
 */

public final class RequestMessage_1_2 extends Message_1_2
        implements RequestMessage {

    // Instance variables

    private ORB orb = null;
    private ORBUtilSystemException wrapper = null ;
    private TimingPoints tp ;
    private byte response_flags = (byte) 0;
    private byte reserved[] = null;
    private TargetAddress target = null;
    private String operation = null;
    private ServiceContexts service_contexts = null;
    private ObjectKeyCacheEntry entry = null;

    // Constructors

    RequestMessage_1_2(ORB orb) {
        this.orb = orb;
	this.tp = orb.getTimerManager().points() ;
	this.service_contexts = ServiceContextDefaults.makeServiceContexts( orb ) ;
	this.wrapper = orb.getLogWrapperTable().get_RPC_PROTOCOL_ORBUtil() ;
    }

    RequestMessage_1_2(ORB orb, int _request_id, byte _response_flags,
            byte[] _reserved, TargetAddress _target,
            String _operation, ServiceContexts _service_contexts) {
        super(Message.GIOPBigMagic, GIOPVersion.V1_2, FLAG_NO_FRAG_BIG_ENDIAN,
            Message.GIOPRequest, 0);
        this.orb = orb;
	this.tp = orb.getTimerManager().points() ;
	this.wrapper = orb.getLogWrapperTable().get_RPC_PROTOCOL_ORBUtil() ;
        request_id = _request_id;
        response_flags = _response_flags;
        reserved = _reserved;
        target = _target;
        operation = _operation;
        service_contexts = _service_contexts;
    }

    // Accessor methods (RequestMessage interface)

    public int getRequestId() {
        return this.request_id;
    }

    public boolean isResponseExpected() {
        /*
        case 1: LSBit[1] == 1
            not a oneway call (DII flag INV_NO_RESPONSE is false)  // Ox03
            LSBit[0] must be 1.
        case 2: LSBit[1] == 0
            if (LSB[0] == 0) // Ox00
                oneway call
            else if (LSB[0] == 1) // 0x01
                oneway call; but server may provide
                a location forward response or system exception response.
        */

        if ( (this.response_flags & RESPONSE_EXPECTED_BIT) == RESPONSE_EXPECTED_BIT ) {
            return true;
        }

        return false;
    }

    public byte[] getReserved() {
        return this.reserved;
    }

    public ObjectKeyCacheEntry getObjectKeyCacheEntry() {
        if (this.entry == null) {
	    // this will raise a MARSHAL exception upon errors.
	    this.entry = MessageBase.extractObjectKeyCacheEntry(target, orb);
        }

	return this.entry;
    }

    public String getOperation() {
        return this.operation;
    }

    @SuppressWarnings({"deprecation"})
    public org.omg.CORBA.Principal getPrincipal() {
        // REVISIT Should we throw an exception or return null ?
        return null;
    }

    public ServiceContexts getServiceContexts() {
        return this.service_contexts;
    }

    public void setServiceContexts(ServiceContexts sc) {
         this.service_contexts = sc;
    }

    // IO methods

    public void read(org.omg.CORBA.portable.InputStream istream) {
	tp.enter_giopHeaderReadRequest() ;
	try {
	    super.read(istream);
	    this.request_id = istream.read_ulong();
	    this.response_flags = istream.read_octet();
	    this.reserved = new byte[3];
	    for (int _o0 = 0;_o0 < (3); ++_o0) {
		this.reserved[_o0] = istream.read_octet();
	    }
	    this.target = TargetAddressHelper.read(istream);
	    getObjectKeyCacheEntry(); // this does AddressingDisposition check
	    this.operation = istream.read_string();
	    this.service_contexts = ServiceContextDefaults.makeServiceContexts(
		(org.omg.CORBA_2_3.portable.InputStream) istream);

	    // CORBA formal 00-11-0 15.4.2.2 GIOP 1.2 body must be
	    // aligned on an 8 octet boundary.
	    // Ensures that the first read operation called from the stub code,
	    // during body deconstruction, would skip the header padding, that was
	    // inserted to ensure that the body was aligned on an 8-octet boundary.
	    ((CDRInputStream)istream).setHeaderPadding(true);
	} finally {
	    tp.exit_giopHeaderReadRequest() ;
	}
        
    }

    public void write(org.omg.CORBA.portable.OutputStream ostream) {
	tp.enter_giopHeaderWriteRequest() ;
	try {
	    super.write(ostream);
	    ostream.write_ulong(this.request_id);
	    ostream.write_octet(this.response_flags);
	    nullCheck(this.reserved);
	    if (this.reserved.length != (3)) {
		throw wrapper.badReservedLength(
		    org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	    }
	    for (int _i0 = 0;_i0 < (3); ++_i0) {
		ostream.write_octet(this.reserved[_i0]);
	    }
	    nullCheck(this.target);
	    TargetAddressHelper.write(ostream, this.target);
	    ostream.write_string(this.operation);
	    service_contexts.write(
		(org.omg.CORBA_2_3.portable.OutputStream) ostream,
		GIOPVersion.V1_2);

	    // CORBA formal 00-11-0 15.4.2.2 GIOP 1.2 body must be
	    // aligned on an 8 octet boundary.
	    // Ensures that the first write operation called from the stub code,
	    // during body construction, would insert a header padding, such that
	    // the body is aligned on an 8-octet boundary.
	    ((CDROutputStream)ostream).setHeaderPadding(true);
	} finally {
	    tp.exit_giopHeaderWriteRequest() ;
	}
    }

    public void callback(MessageHandler handler)
        throws java.io.IOException
    {
        handler.handleInput(this);
    }
} // class RequestMessage_1_2