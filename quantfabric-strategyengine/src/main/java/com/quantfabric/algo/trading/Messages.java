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
package com.quantfabric.algo.trading;

public final class Messages {
	public final static String ERR_STR_EXECPROV_START = "Strategy '%s' can't start execution provider. Activate execution point '%s' failed";
	public final static String ERR_STR_EXEC_NOTIFICATION = "Strategy '%s' notification event failed";
	public final static String ERR_STR_EXEC_COMM = "Strategy '%s' execute command '%s' failed";
	public final static String ERR_STR_EXECPROV_STOP = "Error occured while strategy '%s' stop execution provider. Unsubcribe from connection '%s' failed";
	public final static String ERR_STR_ILLEGAL_STATE_CALL = "Call '%s' is an illegal operation for a started TradingStrategy";
	public final static String ERR_STR_ACTIVATE_SINK="Activate data sink '%s' failed in strategy '%s'";
	public final static String ERR_STR_RBACK_DEACT_SINK = "Error occured in deactivate data sink '%s' in strategy '%s'";
	public final static String ERR_STR_ILLEGAL_START ="Can't start. The strategy is already running";
	public final static String ERR_STR_ILLEGAL_STOP ="Can't stop no running strategy";
}
