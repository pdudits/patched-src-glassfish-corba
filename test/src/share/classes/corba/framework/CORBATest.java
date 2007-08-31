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
package corba.framework;

import java.io.*;
import java.util.*;
import test.*;

import com.sun.corba.se.impl.orbutil.ORBConstants;

/**
 * Main CORBA Technologies test class which should be extended
 * for each individual test.  It provides an easy way to run
 * IDL, RMI, and Java compilers; start ORBD, client(s), and server(s);
 * and manage output from each.
 * <P>
 * The default is to run all of these in separate processes, but this
 * can be changed for ORBD, client, and server by specifying what execution
 * strategy to use.
 * <P>
 * It extends test.RemoteTest to gain access to some data (command
 * line arguments, especially), but doesn't use RemoteTest's RMI/RMI-IIOP
 * system by default.
 * <P>
 * An individual test can configure a wide variety of things by
 * setting values in the Options singleton class.
 *
 *@see corba.example.Example
 *@see test.Test
 *@see test.RemoteTest
 *@see corba.framework.Options
 */
public abstract class CORBATest extends test.RemoteTest
{
    /**
     * Skip any IDL compiling.  This could be useful if you
     * didn't wipe the output directory, and wanted to change
     * stubs or skeletons by hand in debugging.
     */
    public static final String SKIP_IDLJ_FLAG = "-noidlj";

    /**
     * Skip compiling generated (or user specified) .java files.
     */
    public static final String SKIP_JAVAC_FLAG = "-nojavac";

    /**
     * Force use of the DebugExec execution strategy for the
     * process(es) with the comma separated name(s) following
     * this command line flag.  It is currently set to the
     * normal framework debug flag, so you see everything
     * that goes on.
     */
    public static final String DEBUG_STRATEGY_FLAG = "-debug";

    /**
     * Force use of the RDebugExec execution strategy for the
     * process(es) with the comma separated name(s) following
     * this command line flag.  It does not set the framework
     * debug flag.
     */
    public static final String RDEBUG_STRATEGY_FLAG = "-rdebug";

    /**
     * Pass the ORBDebug settings into a controller.
     * Syntax is  -orbtrace name:flag1,flag2,...;name:flag1,...
     * This sets the system property com.sun.corba.se.ORBDebug to the list
     * of flags for a controller named name
     */
    public static final String TRACE_FLAG = "-orbtrace" ;

    /**
     * Force use of the ODebugExec execution strategy for the
     * process(es) with the comma separated name(s) following
     * this command line flag.  It does not set the framework
     * debug flag.
     */
    public static final String ODEBUG_STRATEGY_FLAG = "-odebug";

    /**
     * IDLCompiler instance used by compileIDLFiles()
     */
    protected IDLCompiler idlCompiler = new IDLCompiler();

    /**
     * RMICCompiler instance used by compileRMIFiles()
     */
    protected RMICompiler rmic = new RMICompiler();

    /**
     * JavaCompiler instance used by compileJavaFiles()
     */
    protected JavaCompiler javac = new JavaCompiler();

    /**
     * Initializes this test.  If you override this method, make sure to
     * either call super.setup() first, or duplicate some of the code,
     * especially the (re)initialization of the Options class.
     * Subclasses shouldn't need to modify this since they can handle
     * all their settings at the beginning of doTest with Options.
     */
    public void setup()
    {
        try {

            if (usesOldFramework())
                super.setup();

            Options.init(this);

            parseDebugExecFlags();
	    parseTraceFlag() ;

        } catch (ThreadDeath death) {
            throw death;
        } catch (Throwable e) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(out));
            throw new Error(out.toString());
        }
    }

    /**
     * test.RemoteTest's way of performing an RMI or RMI-IIOP test.  The given
     * ServantContext allows remote control of servants by using RMI.  This
     * is used by the IBM tests.  Any exceptions of errors thrown will result
     * in the test framework reporting "FAILED," and printing the exception's
     * stack trace.
     * <P>
     * Tests which don't use RMI/RMI-IIOP can avoid this method by using
     * the Options class to set files for RMIC compilation, then calling
     * compileRMIFiles().  After that, they can load a servant or client
     * just as they would with CORBA and IDL.
     * <P>
     * Calls doTest() by default.  To use this, override usesOldFramework
     * to return true.  You will also need to specify what remote
     * files to use with getRemoteSevantClasses.  See test.RemoteTestExample.
     *
     *@param       context    Allows the test to manage servant life-cycle
     *@exception   Throwable  Any errors which signal that this test failed
     *@see         test.RemoteTest
     *@see         test.ServantContext
     *@see         test.RemoteTestExample
     */
    protected void doTest(ServantContext context) throws Throwable
    {
        doTest();
    }

    /**
     * Subclasses can override this method if they need to use
     * test.RemoteTest's way of managing servants (with ServantContexts).
     * Returns false by default.
     *
     *@return  True when the framework should use test.RemoteTest's
     *         servant management, false for the newer framework's
     *         system.
     */
    protected boolean usesOldFramework()
    {
        return false;
    }

    /**
     * Subclasses override this method to actually perform their tests.
     * Any exceptions or errors thrown will result in the test framework
     * reporting "FAILED," and printing the exception's stack trace.
     * No-op by default.
     *
     *@exception   Throwable  Any errors which signal that this test failed
     */
    protected void doTest() throws Throwable
    {
        // Subclasses should override one of the doTest methods
    }

    /**
     * Calls the appropriate version of doTest, then handles exit values
     * and exceptions.
     *
     * This is called by the test framework and shouldn't need to be changed.
     */
    public void run()
    {
	CORBAUtil.mkdir( Options.getOutputDirectory() ) ;
	CORBAUtil.mkdir( Options.getReportDirectory() ) ;
        boolean errorOccured = false;

        try {

            if (usesOldFramework())
                super.run();
            else
                doTest(null);

        } catch (ThreadDeath death) {
            errorOccured = true;
            throw death;
        } catch (Throwable e) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(out));
            errorOccured = true;
            status = new Error("Caught " + out.toString());
        } finally {
	    // Make sure processes are dead and streams are closed
            cleanUp();

	    // Make sure all exit values were Controller.SUCCESS
	    // unless there was already an error (don't want to
	    // overwrite the more specific error message).
            if (!errorOccured)
                handleExitValues();
        }
    }

    /**
     * test.RemoteTest's way of getting the classes that need
     * to go through RMIC.  This newer framework does this within
     * the Options singleton, just as it does with IDL and
     * .java files.
     *
     *@return  Array of class names to go through RMIC
     */
    protected String[] getRemoteServantClasses()
    {
        return new String [0];
    }

    /**
     * Compile IDL files (called by subclasses in doTest).
     * To use this, simply specify file names with the Options
     * singleton.  They must include the .java ending, but don't have
     * to be full paths, just the names.  To toggle IDL
     * compilation on the command line, use the -noidlj parameter.
     *
     *@see Options#setIDLFiles
     *
     *@exception  Any error that occured during compilation (bad exit
     *            value for the process would be the most common)
     */
    protected void compileIDLFiles() throws Exception
    {
        if (!getArgs().containsKey(SKIP_IDLJ_FLAG)) {

            String files[] = Options.getIDLFiles();

            // Get the absolute paths of the files
            if (files == null)
                return;

            String searchPath[] = new String[] { Options.getTestDirectory() };

            CORBAUtil.toAbsolutePaths(files,
                                      searchPath);

            idlCompiler.compile(files,
                                Options.getIDLCompilerArgs(),
                                Options.getOutputDirectory(),
                                Options.getReportDirectory());
        }
    }

    /**
     * Compile using RMIC (called by subclasses in doTest).
     * Specify the class names with the Options singleton.  To
     * toggle RMIC use on the command line, use the -normic parameter.
     *
     *@see Options#setRMICClasses
     *
     *@exception  Any error that occured during compilation (bad exit
     *            value for the process would be the most common)
     */
    protected void compileRMICFiles() throws Exception
    {
        if (!getArgs().containsKey(super.SKIP_RMIC_FLAG)) {
            rmic.compile(Options.getRMICClasses(),
                         Options.getRMICArgs(),
                         Options.getOutputDirectory(),
                         Options.getReportDirectory());
        }
    }

    /**
     * Adds absolute paths to the given Vector of any .java files in the
     * specified directory or its subdirectories.  This won't add a
     * file if its class file is present and the class file is newer
     * than the .java file.
     *
     *@param     files     Current Vector of .java files to augment
     *@param     dir       Directory to examine
     */
    private void getGenJavaFilesHelper(Vector files, File dir)
    {
	// Create a filter that accepts directory names and .java files
        FileFilter dotJavaFilter = new FileFilter() {
            public boolean accept(File pathname) {
                return ((pathname.isFile()
                         && pathname.toString().endsWith(".java"))
                        || (pathname.isDirectory()));
            }
        };

        File fileArray[] = dir.listFiles(dotJavaFilter);

        if (fileArray == null)
            return;

	// Recurse down through any directories, gathering absolute paths
	// of all .java files
        for (int i = 0; i < fileArray.length; i++) {
            if (fileArray[i].isDirectory()) {
                getGenJavaFilesHelper(files,
                                      new File(fileArray[i].getAbsolutePath()));
            } else {

                // Add the absolute path of this .java file unless
                // its .class file exists and the .java file was
                // modified more recently.  (Assumes they are in the
                // same directory -- they should be for generated files.)

                String path = fileArray[i].getAbsolutePath();
                String dotClassName = path.substring(0, path.indexOf(".java"))
                    + ".class";

                File dotClassFile = new File(dotClassName);

                if (!dotClassFile.exists() ||
                    fileArray[i].lastModified() > dotClassFile.lastModified()) {

                    files.add(fileArray[i].getAbsolutePath());
                }
            }
        }
    }

    /**
     * Return an array of absolute paths to java files which were created
     * from compiling IDL.  Currently, this just searches the output directory
     * and its subdirectories for *.java files.  Since the output directory is
     * assumes to be wiped each time the test is run (unless
     * debugging), this is reasonable.
     *
     *@return   Array of absolute paths to .java files or the empty array
     *          if none
     */
    private Vector getGeneratedJavaFiles()
    {
        File dir = new File (Options.getOutputDirectory());

        Vector files = new Vector();

        getGenJavaFilesHelper(files, dir);

        return files;
    }

    /**
     * Compile .java files (called by subclasses in their doTest
     * method).
     * Convenience method for subclasses to compile both specified
     * and any generated .java files.  Subclasses can specify any
     * non-generated files that need compilation with the Options
     * singleton.  This can be skipped by specifying the -nojavac
     * flag on the command line.
     *
     *@see Options#setJavaFiles
     *
     *@exception  Any error that occured during compilation (bad exit
     *            value for the process would be the most common)
     */
    protected void compileJavaFiles() throws Exception
    {
        if (getArgs().containsKey(SKIP_JAVAC_FLAG))
            return;

        // Get any .java files that were generated
        Vector fileVector = getGeneratedJavaFiles();

	// Add any subclass-specified files
        String userFiles[] = Options.getJavaFiles();

        if (userFiles != null)
            for (int i = 0; i < userFiles.length; i++)
                fileVector.add(userFiles[i]);

        if (fileVector.size() == 0)
            return;

        String[] files = CORBAUtil.toArray(fileVector);

        // Convert any non-generated file names to absolute paths
        CORBAUtil.toAbsolutePaths(files,
                                  new String[] { Options.getTestDirectory(),
                                                 Options.getOutputDirectory() });

        javac.compile(files,
                      Options.getJavacArgs(),
                      Options.getOutputDirectory(),
                      Options.getReportDirectory());
    }

    /**
     * Create and initialize the Controller for the ORBD.  This initializes
     * and returns a Controller wrapped in a FileOutputDecorator.  Most of the
     * arguments to the Controller can be set in the Options class.
     *
     * The following properties are provided, but can be set or overriden
     * by using the Options class:
     * <PRE>
     * output.dir
     * com.sun.corba.se.JavaIDLHome
     * org.omg.CORBA.ORBClass
     * org.omg.CORBA.ORBSingletonClass
     * com.sun.corba.se.impl.activation.DbDir
     * org.omg.CORBA.ORBInitialPort
     * LD_LIBRARY_PATH (if available)
     * persistentServerId
     * com.sun.corba.se.impl.activation.Port
     * javax.rmi.CORBA.UtilClass (if available)
     * javax.rmi.CORBA.StubClass (if available)
     * javax.rmi.CORBA.PortableRemoteObjectClass (if available)
     * </PRE>
     *
     *@return   Controller object representing the ORBD process, but
     *          not yet started.
     *
     *@exception  Exception   Any problem generated by initializing the
     *                        Controller
     *
     *@see Controller
     *@see CORBATest#newORBDController
     */
    public Controller createORBD() throws Exception
    {
        String javaIDLHome = Options.getJavaIDLHome();

        CORBAUtil.mkdir(javaIDLHome);

        Properties props = new Properties();

	String traceFlags = (String)traceMap.get( "ORBD" ) ;
	if (traceFlags != null)
	    props.setProperty( ORBConstants.DEBUG_PROPERTY,
		traceFlags ) ;

        props.setProperty("output.dir",
                          Options.getOutputDirectory());
        props.setProperty("com.sun.corba.se.JavaIDLHome",
                          javaIDLHome);
        props.setProperty("org.omg.CORBA.ORBClass",
                          Options.getORBClass());
        props.setProperty("org.omg.CORBA.ORBSingletonClass",
                          Options.getORBSingleton());
        props.setProperty( ORBConstants.DB_DIR_PROPERTY,
                          javaIDLHome
                          + File.separator
                          + Options.getActivationDbDirName());
        props.setProperty( ORBConstants.INITIAL_PORT_PROPERTY,
                          Options.getORBInitialPort());
	int emmaPort = EmmaControl.setCoverageProperties( props ) ;

        String ioser = Options.getioserPath();
        if (ioser != null)
            props.setProperty("LD_LIBRARY_PATH",
                              ioser);

        Options.addSpecialProperties(props);

        props.setProperty(ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY,
                          Options.getActivationPort());

        String persistentServerID = Options.getPersistentServerID();
        if (persistentServerID != null)
            props.setProperty(ORBConstants.ORB_SERVER_ID_PROPERTY, persistentServerID);

        Test.dprint("ORB initial port: " + Options.getORBInitialPort());
        Test.dprint("Activation port: " + Options.getActivationPort());

        // Allows user to override these or set new ones
        props.putAll(Options.getExtraORBDProperties());

        Controller delegate;
        if (odebugProcessNames.contains("ORBD"))
	    delegate = new ODebugExec() ;
	else if (rdebugProcessNames.contains("ORBD"))
	    delegate = new RDebugExec() ;
        else if (debugProcessNames.contains("ORBD"))
            delegate = new DebugExec();
        else
            delegate = newORBDController();

        FileOutputDecorator exec = new FileOutputDecorator(delegate);

        exec.initialize(Options.getORBDMainClass(),
                        "ORBD",
                        props,
                        null,
                        CORBAUtil.toArray(Options.getORBDArgs()),
                        Options.getReportDirectory()
                        + "ORBD.out.txt",
                        Options.getReportDirectory()
                        + "ORBD.err.txt",
                        Options.getORBDExtra(),
			emmaPort );

        // Kept around for cleaning up at the end, not for users
        controllers.add(exec);

        return exec;
    }

    /**
     * Creates a new Controller of the appropriate type for the ORBD.
     *
     *@return Controller  A new Controller object
     *@see ExternalExec
     *@see Options#getORBDExtra
     */
    protected Controller newORBDController()
    {
        return new ExternalExec();
    }

    /**
     * Create and initialize the Controller for the/a server.  This
     * initializes and wraps it in a FileOutputDecorator.
     * <P>
     * The following things are provided to client and server
     * processes and can be augmented/changed with the Options class:
     * <PRE>
     * Properties:
     *   output.dir
     *   org.omg.CORBA.ORBClass
     *   org.omg.CORBA.ORBSingletonClass
     *   LD_LIBRARY_PATH (if available)
     *   java.naming.factory.initial
     *   java.naming.provider.url
     *   javax.rmi.CORBA.UtilClass (if available)
     *   javax.rmi.CORBA.StubClass (if available)
     *   javax.rmi.CORBA.PortableRemoteObjectClass (if available)
     *
     * Command line parameters:
     *   -ORBInitialPort
     * </PRE>
     *
     *@param       className   Fully qualified class name of the server
     *@param       serverName  Name used to identify this server for
     *                         output file name purposes
     *@return      Controller  Initialized (but not started) Controller
     *                         for the server
     *@exception   Exception   Any problem generated by initializing
     *
     *@see Controller
     *@see #newServerController
     *@see Options
     */
    public Controller createServer(String className, String serverName)
        throws Exception
    {
        Test.dprint("Creating server object...");

        Controller executionStrategy;
	if (odebugProcessNames.contains(serverName))
            executionStrategy = new ODebugExec();
        else if (rdebugProcessNames.contains(serverName))
            executionStrategy = new RDebugExec();
        else if (debugProcessNames.contains(serverName))
            executionStrategy = new DebugExec();
        else
            executionStrategy = newServerController();

	Properties props = Options.getExtraServerProperties() ;
	int emmaPort = EmmaControl.setCoverageProperties( props ) ;

        // Keep the reference for clean-up
        Controller server
            = initServerOrClient(className,
                                 serverName,
                                 Options.getServerArgs(),
                                 serverName + ".out.txt",
                                 serverName + ".err.txt",
				 props,
                                 Options.getServerExtra(),
                                 executionStrategy,
				 emmaPort);

        controllers.add(server);

        return server;
    }

    /**
     * Same as createServer(String, String) but uses the default
     * name "server."  This is sufficient for one server tests.
     */
    public Controller createServer(String className) throws Exception
    {
        return createServer(className, "server");
    }


    /**
     * Creates a new Controller of the appropriate type for the server.
     *
     *@return Controller  A new Controller object
     *@see ExternalExec
     */
    protected Controller newServerController()
    {
        return new ExternalExec();
    }

    /**
     * Create and initialize the Controller for the client.  This
     * initializes and wraps it in a FileOutputDecorator;
     * <P>
     * The following things are provided to client and server
     * processes and can be augmented/changed with the Options class:
     * <PRE>
     * Properties:
     *   output.dir
     *   org.omg.CORBA.ORBClass
     *   org.omg.CORBA.ORBSingletonClass
     *   LD_LIBRARY_PATH (if available)
     *   java.naming.factory.initial
     *   java.naming.provider.url
     *   javax.rmi.CORBA.UtilClass (if available)
     *   javax.rmi.CORBA.StubClass (if available)
     *   javax.rmi.CORBA.PortableRemoteObjectClass (if available)
     *
     * Command line parameters:
     *   -ORBInitialPort
     * </PRE>
     *
     *@param       className    Fully qualified class name of the client
     *@param       clientName   Name used to identify this client for
     *                          output file name purposes.
     *@return      Controller   Initialized (but not started) Controller
     *                          for the client
     *@exception   Exception    Any problem generated by starting
     *@see Controller
     *@see #newClientController
     *@see Options
     */
    public Controller createClient(String className, String clientName)
        throws Exception
    {
        Test.dprint("Creating client object...");

        Controller executionStrategy;
	if (odebugProcessNames.contains(clientName))
            executionStrategy = new ODebugExec();
        if (rdebugProcessNames.contains(clientName))
            executionStrategy = new RDebugExec();
        else if (debugProcessNames.contains(clientName))
            executionStrategy = new DebugExec();
        else
            executionStrategy = newClientController();

	Properties props = Options.getExtraClientProperties() ;
	int emmaPort = EmmaControl.setCoverageProperties( props ) ;

        Controller client
            = initServerOrClient(className,
                                 clientName,
                                 Options.getClientArgs(),
                                 clientName + ".out.txt",
                                 clientName + ".err.txt",
				 props,
                                 Options.getClientExtra(),
                                 executionStrategy,
				 emmaPort);

        controllers.add(client);

        return client;
    }

    /**
     * Same as createClient(String, String) but uses the default name
     * of "client."  This is sufficient for one client tests.
     */
    public Controller createClient(String className) throws Exception
    {
        return createClient(className, "client");
    }

    /**
     * Creates a new Controller of the appropriate type for the client.
     * The default implementation returns an ExternalExec object,
     * so the Controller's start() method returns immediately after
     * starting a separate Process.
     *
     *@return Controller  A new Controller object
     *@see ExternalExec
     */
    protected Controller newClientController()
    {
        return new ExternalExec();
    }

    /**
     * Initializes the Controller with the given parameters, and wraps a
     * FileOutputDecorator around it.
     *
     *@param className    Fully qualified name of the class to execute
     *@param processName  Name used to identify this client or server
     *@param args         Arguments to the class
     *@param outFileName  Name of the file to direct stdout to
     *@param errFileName  Name of the file to direct stderr to
     *@param extraProps   Extra properties to set when running
     *@param extra        Extra initializer (if any)
     *@param delegate     Controller used to execute the class
     *
     *@return Controller which has initialized the delegate's streams and
     *        started the class appropriately
     *
     *@exception  Exception  Any problem generated by starting, such as
     *            abnormal termination
     */
    private Controller initServerOrClient(String className,
                                          String processName,
                                          Vector args,
                                          String outFileName,
                                          String errFileName,
                                          Properties extraProps,
                                          Hashtable extra,
                                          Controller delegate,
					  int emmaPort )
        throws Exception
    {
        Properties props = new Properties();

	String traceFlags = (String)traceMap.get( processName ) ;
	if (traceFlags != null)
	    props.setProperty( ORBConstants.DEBUG_PROPERTY,
		traceFlags ) ;

        props.setProperty("output.dir",
                          Options.getOutputDirectory());
        props.setProperty("org.omg.CORBA.ORBClass",
                          Options.getORBClass());
        props.setProperty("org.omg.CORBA.ORBSingletonClass",
                          Options.getORBSingleton());

        String ioser = Options.getioserPath();
        if (ioser != null)
            props.setProperty("LD_LIBRARY_PATH",
                              ioser);

	props.setProperty("java.naming.factory.initial",
			  Options.getNamingFactory());

	props.setProperty("java.naming.provider.url",
			  Options.getNamingProviderURL());

        Options.addSpecialProperties(props);

        props.putAll(extraProps);

        args.add("-ORBInitialPort");
        args.add(Options.getORBInitialPort());

        String argArray[] = CORBAUtil.toArray(args);

        FileOutputDecorator exec =
            new FileOutputDecorator(delegate);

        exec.initialize(className,
                        processName,
                        props,
                        null,
                        argArray,
                        Options.getReportDirectory() + outFileName,
                        Options.getReportDirectory() + errFileName,
                        extra,
			emmaPort);

        return exec;
    }

    /**
     * Makes sure the given process has ended, and cleans up its
     * output streams.  This catches all exceptions.
     */
    private void cleanUpHelp(Controller process)
    {
        if (process == null)
            return;

        String name = process.getProcessName();

        Test.dprint("Cleaning up " + name + "...");

        try {
            process.kill();
        } catch (Throwable t) {
            System.err.println("Problem stopping " + name + ": " + t);
            System.err.println("May need to kill this process");
            // Disregard
        }

        OutputStream out = process.getOutputStream();
        OutputStream err = process.getErrorStream();

        // For ExternalExec processes, the ProcessMonitors will
        // close the output streams when the process dies and
        // they finish writing all of the output.
        if (process instanceof corba.framework.ExternalExec)
            return;

        if (out != null && out != System.out) {

            try {
                out.flush();
            } catch (IOException e) {
                // Disregard
            }

            try {
                out.close();
            } catch (IOException e) {
                // Disregard
            }
        }

        if (err != null && err != System.err) {

            try {
                err.flush();
            } catch (IOException e) {
                // Disregard
            }

            try {
                err.close();
            } catch (IOException e) {
                // Disregard
            }
        }
    }

    /**
     * Call cleanUpHelp for any Controllers that were created.
     */
    private void cleanUp()
    {
        Enumeration ctrls = controllers.elements();

        while (ctrls.hasMoreElements())
            cleanUpHelp((Controller)ctrls.nextElement());

	EmmaControl.resetPortAllocator() ;
    }


    /**
     * Confirm all Controllers were either stopped or exited with
     * success values.  If something failed, the test's status
     * member is set to a new Error documenting the problem(s).
     */
    private void handleExitValues()
    {
        int failures = 0;
        String lineSeparator = System.getProperty("line.separator");
        StringBuffer failedMsg = new StringBuffer("Bad exit value(s):"
                                                  + lineSeparator);

        Enumeration ctrls = controllers.elements();

        while (ctrls.hasMoreElements()) {
            Controller controller = (Controller)ctrls.nextElement();

            try {
                controller.kill();
            } catch (IllegalThreadStateException ex) {}

            int exitValue = 1;

            try {
                exitValue = controller.exitValue();
            } catch (IllegalThreadStateException ex) {
                // The process was never started, probably because of
                // a prior error
                continue;
            }

            if (exitValue != Controller.SUCCESS &&
		exitValue != Controller.STOPPED) {
                failedMsg.append(controller.getProcessName()
                                 + "[" + exitValue + "]"
                                 + lineSeparator);
                failures++;
            }
        }

        if (failures != 0)
            status = new Error(failedMsg.toString());
    }

    /**
     * Parse the command line list after the DEBUG_STRATEGY_FLAG.  The
     * list should be process names separated by colons.  If clients,
     * servers, or the ORBD process is created and its process name is
     * found in the set, it will be executed with the DebugExec strategy.
     */
    private void parseDebugExecFlags()
    {
        String processNames = (String)getArgs().get(DEBUG_STRATEGY_FLAG);

        if (processNames != null) {
	    // The process names should be separated by commas
	    StringTokenizer tokenizer = new StringTokenizer(processNames, ":");

	    // Add the names
	    while (tokenizer.hasMoreTokens())
		debugProcessNames.add(tokenizer.nextToken());
	}

	processNames = (String)getArgs().get(RDEBUG_STRATEGY_FLAG) ;

	if (processNames != null) {
	    // The process names should be separated by commas
	    StringTokenizer tokenizer = new StringTokenizer(processNames, ":");

	    // Add the names
	    while (tokenizer.hasMoreTokens())
		rdebugProcessNames.add(tokenizer.nextToken());
	}

	processNames = (String)getArgs().get(ODEBUG_STRATEGY_FLAG) ;

	if (processNames != null) {
	    // The process names should be separated by commas
	    StringTokenizer tokenizer = new StringTokenizer(processNames, ":");

	    // Add the names
	    while (tokenizer.hasMoreTokens())
		odebugProcessNames.add(tokenizer.nextToken());
	}
    }

    private void parseTraceFlag()
    {
	String traceData = (String)getArgs().get(TRACE_FLAG) ;
	if (traceData == null)
	    return ;

	// traceData should look like
	// data == group ( ";" group ) *
	// group == name ":" name ( "," name ) *
	// name == [a-zA-Z0-9]+

	StringTokenizer dataST = new StringTokenizer( traceData, ";" ) ;
	while (dataST.hasMoreTokens()) {
	    String group = dataST.nextToken() ;
	    StringTokenizer groupST = new StringTokenizer( group, ":" ) ;
	    if (groupST.countTokens() != 2)
		throw new IllegalArgumentException( "Bad syntax in trace command" ) ;

	    String name = groupST.nextToken() ;
	    String debugArgs = groupST.nextToken() ;

	    traceMap.put( name, debugArgs ) ;
	}
    }

    /**
     * Set of process names which will be executed with the
     * DebugExec execution strategy.  These are specified in
     * a colon separated list after the DEBUG_STRATEGY_FLAG
     * command line flag.
     */
    protected HashSet debugProcessNames = new HashSet();

    /**
     * Set of process names which will be executed with the
     * RDebugExec execution strategy.  These are specified in
     * a colon separated list after the RDEBUG_STRATEGY_FLAG
     * command line flag.
     */
    protected HashSet rdebugProcessNames = new HashSet();

    /**
     * Set of process names which will be executed with the
     * ODebugExec execution strategy.  These are specified in
     * a colon separated list after the DEBUG_STRATEGY_FLAG
     * command line flag.
     */
    protected HashSet odebugProcessNames = new HashSet();

    /**
     * Map from controller name to com.sun.corba.se.ORBDebug value.
     * This allows setting the ORB debug flags from the test
     * arguments.
     */
    protected Map traceMap = new HashMap() ;

    protected Vector controllers = new Vector();
}