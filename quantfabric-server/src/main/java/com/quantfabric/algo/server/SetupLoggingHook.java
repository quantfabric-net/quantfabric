/*
 * This file is part of the QuickServer library 
 * Copyright (C) 2003-2005 QuickServer.org
 *
 * Use, modification, copying and distribution of this software is subject to
 * the terms and conditions of the GNU Lesser General Public License. 
 * You should have received a copy of the GNU LGP License along with this 
 * library; if not, you can download a copy from <http://www.quickserver.org/>.
 *
 * For questions, suggestions, bug-reports, enhancement-requests etc.
 * visit http://www.quickserver.org
 *
 */

/*
 * Copyright 2022-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quantfabric.algo.server;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quickserver.net.InitServerHook;
import org.quickserver.net.server.QuickServer;
import org.quickserver.util.logging.SimpleTextFormatter;

public class SetupLoggingHook implements InitServerHook {
	public String info() {
		return "Init Server Hook to setup logging.";
	}

	public void handleInit(QuickServer quickserver) throws Exception {
		Logger logger = null;
		FileHandler txtLog = null;
		File log = new File("./log/");
		if(!log.canRead())
			log.mkdir();
		try	{
			logger = Logger.getLogger("");
			logger.setLevel(Level.FINEST);

			logger = Logger.getLogger("");
			txtLog = new FileHandler("log/BroadcastServer_%u%g.txt", 
				1024*1024, 5, true);
			txtLog.setFormatter(new SimpleTextFormatter());
			txtLog.setLevel(Level.FINEST);
			logger.addHandler(txtLog);

			quickserver.setAppLogger(logger);
		} catch(IOException e){
			System.err.println("Could not create txtLog FileHandler : "+e);
			throw e;
		}
	}
}
