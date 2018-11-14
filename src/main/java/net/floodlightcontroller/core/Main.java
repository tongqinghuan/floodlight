/**
 *    Copyright 2013, Big Switch Networks, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

package net.floodlightcontroller.core;

import com.react.compiler.MiniCompiler;
import com.react.topo.Network;
import net.floodlightcontroller.core.internal.CmdLineSettings;
import net.floodlightcontroller.core.module.FloodlightModuleConfigFileNotFoundException;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.FloodlightModuleLoader;
import net.floodlightcontroller.core.module.IFloodlightModuleContext;
import net.floodlightcontroller.restserver.IRestApiService;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;



/**
 * Host for the Floodlight main method
 * @author alexreimers
 */
public class Main {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	/**
	 * Main method to load configuration and modules
	 * @param args
	 * @throws FloodlightModuleException 
	 */
	public static void main(String[] args) throws FloodlightModuleException {
		try {
			new Thread(new Runnable() {
				public void run() {
					// RPCServer.RPCServerRun();
					logger.info("Thread is sleeping");
					try {
						TimeUnit.SECONDS.sleep(60);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					logger.info("Thread is waking up");

					Network.TopologyInit();
					Network.printLinks();
					Network.printSwitch();
					Network.printEdge_ports();
					Network.printEdgeHosts();
					MiniCompiler.installRules();
				}
			}).start();
			// Setup logger
			System.setProperty("org.restlet.engine.loggerFacadeClass", 
					"org.restlet.ext.slf4j.Slf4jLoggerFacade");

			CmdLineSettings settings = new CmdLineSettings();
			CmdLineParser parser = new CmdLineParser(settings);
			try {
				parser.parseArgument(args);
			} catch (CmdLineException e) {
				parser.printUsage(System.out);
				System.exit(1);
			}

			// Load modules
			FloodlightModuleLoader fml = new FloodlightModuleLoader();
			try {
				IFloodlightModuleContext moduleContext = fml.loadModulesFromConfig(settings.getModuleFile());
				IRestApiService restApi = moduleContext.getServiceImpl(IRestApiService.class);
				restApi.run(); 
			} catch (FloodlightModuleConfigFileNotFoundException e) {
				// we really want to log the message, not the stack trace
				logger.error("Could not read config file: {}", e.getMessage());
				System.exit(1);
			}
			try {
                fml.runModules(); // run the controller module and all modules
            } catch (FloodlightModuleException e) {
                logger.error("Failed to run controller modules", e);
                System.exit(1);
            }
		} catch (Exception e) {
			logger.error("Exception in main", e);
			System.exit(1);
		}
	}
}
