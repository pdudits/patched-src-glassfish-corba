/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1999-2007 Sun Microsystems, Inc. All rights reserved.
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
/* @(#)GameView.java	1.4 99/06/07 */
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

//----------------------------------------------------------------------------
// Change History:
//
// 9/98 rtw   created
// 2/99 rtw   updated to split out Remote interface ... see RemoteGameView
//----------------------------------------------------------------------------


package ibmspace.common;

import java.rmi.RemoteException;
import java.util.Vector;

// Note:  All methods in this interface are declared to throw RemoteException
//        because some subclasses (remote servers) will do this.  Declaring
//        to throw an exception does not mean that a subclass WILL throw it
//        and Java subclasses can remove exceptions but cannot add them.

public interface GameView
{
    void              test () throws RemoteException;

    //
    // Working with budgets
    //

    BudgetSummary     getMainBudget () throws RemoteException;
    BudgetSummary     getTechBudget () throws RemoteException;
    BudgetSummary     getPlanetBudget (ID planet) throws RemoteException;
    void              setMainBudget (BudgetSummary bs) throws RemoteException;
    void              setTechBudget (BudgetSummary bs) throws RemoteException;
    void              setPlanetBudget (ID planet, BudgetSummary bs) throws RemoteException;

    //
    // Working with assets
    //

    long              getShipSavings () throws RemoteException;
    long              getIncome () throws RemoteException;
    long              getShipMetal () throws RemoteException;
    TechProfile       getTechProfile () throws RemoteException;

    //
    // Working with ships
    //

    ShipDesign        designShip (String name, int type, TechProfile tech) throws RemoteException;
    ID /*fleet*/      buildFleet (ShipDesign design, int num, ID station) throws RemoteException;
    void              scrapFleet (ID fleet) throws RemoteException;
    ID /*journey*/    sendFleet (ID fleet, ID planet) throws RemoteException;
    Fleet             getFleet (ID fleet) throws RemoteException;
    ID[]              getFleetsAt (ID planet) throws RemoteException;
    Journey           getJourney (ID journeyOrShip) throws RemoteException;
    ID[]              getAllJournies () throws RemoteException;

    //
    // Working with planets
    //

    ID                getHome () throws RemoteException;
    PlanetView        getPlanet (ID planet) throws RemoteException;
    void              abandonPlanet (ID planet) throws RemoteException;

    //
    // Turn taking
    //

    Vector            takeTurn () throws RemoteException;
    void              quit () throws RemoteException;

    long              getCalls () throws RemoteException;

}