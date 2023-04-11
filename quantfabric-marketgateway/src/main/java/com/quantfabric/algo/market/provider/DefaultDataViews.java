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
package com.quantfabric.algo.market.provider;
import com.quantfabric.algo.market.dataprovider.DataView;
import com.quantfabric.algo.market.dataprovider.DataViewImpl;
import com.quantfabric.cep.StatementDefinitionImpl;

public class DefaultDataViews {

	public static final String MonitoringView_NAME = "MonitoringView";
	public static final String AllFeedsView_NAME = "AllFeedsView";
	public static final String FeedView_NAME = "FeedView";
	public static final String FeedTopOfBookView_NAME = "FeedTopOfBook";

	public static DataView getDataView(String viewName){

		switch(viewName){
			case MonitoringView_NAME:
				return getMonitoringView();
			case AllFeedsView_NAME:
				return getAllFeedsView();
			case FeedView_NAME:
				return getMarketFeedView();
			case FeedTopOfBookView_NAME:
				return getFeedTopOfBook();
			default:
				return null;
		}
	}

	public static DataView getMonitoringView(){
		DataView view = new DataViewImpl(MonitoringView_NAME,true);
		view.getStatements().add(new StatementDefinitionImpl("EngineMetric",
				"select * from com.espertech.esper.client.metric.EngineMetric",false,false));		
		view.getStatements().add(new StatementDefinitionImpl("StatementMetric",
				"select * from com.espertech.esper.client.metric.StatementMetric",false,false));		
		///////////////////////////////////////////////////////////////////////
		return view;
	}
	public static DataView getAllFeedsView(){
		DataViewImpl view = new DataViewImpl(AllFeedsView_NAME,false);
		view.setEventType("OrderBook");
		return view;
	}
	public static DataView getMarketFeedView(){
		DataViewImpl view = new DataViewImpl(FeedView_NAME);//create a named window for market snapshot 
		view.getStatements().add(new StatementDefinitionImpl("win_MarketSnapshot","create window $$feedName$$_MarketSnapshot.win:keepall()" +
			" as select " +
			" aggregated,amountOrders,depthLevel,feedId,feedName,mdItemId,mdItemType,messageId, " +
			" messageType,price,priceType,size,sourceName,sourceTimestamp,symbol,timestamp,timestampAsDate,0L as snapshotId " +
			" from MDPrice",false,false));
		//delete all prices from snapshot when new snapshot comes from market
		view.getStatements().add(new StatementDefinitionImpl("cleanup_MarketSnapshot","on NewSnapshot(feedName ='@@feedName@@') as snsh " +
			" delete from $$feedName$$_MarketSnapshot as marketSnap where marketSnap.feedId = snsh.feedId",false,false));
		// detect if item exists in the window
		view.getStatements().add(new StatementDefinitionImpl("merge_MarketSnapshot","" +
		"on pattern [every (newPrice=MDPrice(feedName ='@@feedName@@') or priceDel=MDDelete(feedName = '@@feedName@@'))]" +
			" merge $$feedName$$_MarketSnapshot marketSnap " +
			" where (marketSnap.mdItemId = newPrice.mdItemId) or (marketSnap.mdItemId = priceDel.mdItemId) " +
		" when NOT matched and newPrice is NOT null then insert " +//insert on newPrice
			" select newPrice.aggregated as aggregated,newPrice.amountOrders as amountOrders,newPrice.depthLevel as depthLevel" +
			" ,newPrice.feedId as feedId,newPrice.feedName as feedName" +
			" ,newPrice.mdItemId as mdItemId,newPrice.mdItemType as mdItemType,newPrice.messageId as messageId " +
			" ,newPrice.messageType as messageType,newPrice.price as price,newPrice.priceType as priceType" +
			" ,newPrice.size as size,newPrice.sourceName as sourceName" +
			" ,newPrice.sourceTimestamp as sourceTimestamp,newPrice.symbol as symbol,newPrice.timestamp as timestamp" +
			" ,newPrice.timestampAsDate as timestampAsDate, 0L  as snapshotId " +
		" when matched and newPrice is NOT null then update" +//update on newPrice existing
			" set price = newPrice.price, size = newPrice.size,aggregated = newPrice.aggregated,amountOrders = newPrice.amountOrders," +
			" messageId = newPrice.messageId " +
		" when matched and priceDel is NOT null then delete "//delete on priceDelete event
			,false,false));
		//insert endUpdate on first EndUpdate event - all other endUpdates will be processed via updates
		view.getStatements().add(new StatementDefinitionImpl("insert_SnapshotPrice","insert into $$feedName$$_MarketSnapshot  " +
			" select true as aggregated,0 as amountOrders,0 as depthLevel, 0 as feedId, " +
			" '$$feedName$$' as feedName, '-100' as mdItemId, MDItem$MDItemType.BID as mdItemType, messageId as messageId, " +
			" MDMessageInfo$MDMessageType.UNKNOWN as messageType, 0D as price, MDPrice$PriceType.BID as priceType, " +
			" 0D as size, 'END_UPD' as sourceName, sourceTimestamp as sourceTimestamp, " +
			" 'END_UPD' as symbol, timestamp as timestamp,timestampAsDate as timestampAsDate, messageId as snapshotId  " +
			" from  EndUpdate.std:firstevent()",false,false));
		//insert endUpdate on first EndUpdate event - all other endUpdates will be processed via updates
		view.getStatements().add(new StatementDefinitionImpl("update_SnapshotEndEvent"," on EndUpdate eUpd " +
		" update $$feedName$$_MarketSnapshot set" +
		" snapshotId= eUpd.messageId, messageId= eUpd.messageId,sourceTimestamp=eUpd.sourceTimestamp where mdItemId='-100' ",false,false));
		view.getParameters().add("feedName");
		view.setEventType("OrderBook");
		return view;
	}
	public static DataView getFeedTopOfBook() {
		DataViewImpl view = new DataViewImpl(FeedTopOfBookView_NAME);
		//add dependent veiws
		view.getDependences().add(FeedView_NAME);		
		view.getStatements().add(new StatementDefinitionImpl("EndSnapshot",
				"insert into $$feedName$$_EndSnapshot select * " +
				"from $$feedName$$_MarketSnapshot(mdItemId='-100')"	,false, false));	
		view.getStatements().add(new StatementDefinitionImpl("TopOfBook_bid",
			"insert into  $$feedName$$_MDQuoteTopOfBookBid  " +
			"select bid.messageId as messageId,lastupdate.messageId as snapshotId," +
				"bid.sourceName as sourceName,bid.sourceTimestamp as sourceTimestamp ,bid.mdItemId as mdItemId, " +
				"bid.symbol as symbol,bid.feedId as feedId,1L as level," +
				"bid.price as price,bid.size as size,bid.amountOrders as priceCount," +
				"bid.mdItemType as priceType " +
			"from $$feedName$$_EndSnapshot lastupdate unidirectional,"+
				"$$feedName$$_MarketSnapshot(mdItemType=MDItem$MDItemType.BID ) bid " +
			"where bid.price=(select max(price) from $$feedName$$_MarketSnapshot(mdItemType=MDItem$MDItemType.BID)) " 
			,false, false));	
		view.getStatements().add(new StatementDefinitionImpl("TopOfBook_offer",
			"insert into  $$feedName$$_MDQuoteTopOfBookOffer  " +
			"select offer.messageId as messageId,lastupdate.messageId as snapshotId," +
				" offer.sourceName as sourceName,offer.sourceTimestamp as sourceTimestamp ,offer.mdItemId as mdItemId," +
				" offer.symbol as symbol,offer.feedId as feedId,1L as level," +
				" offer.price as price,offer.size as size,offer.amountOrders as priceCount," +
				" offer.mdItemType as priceType " +
			" from $$feedName$$_EndSnapshot lastupdate unidirectional,"+
				" $$feedName$$_MarketSnapshot(mdItemType=MDItem$MDItemType.OFFER ) offer " +
			"where offer.price=(select min(price) from $$feedName$$_MarketSnapshot(mdItemType=MDItem$MDItemType.OFFER)) " 
			,false, false));
		view.getStatements().add(new StatementDefinitionImpl("changesOnlyTopOfBookBid","" +
				" insert into MDQuoteTopOfBook select " +
				" messageId, snapshotId, sourceName,sourceTimestamp , mdItemId, " +
				" symbol,feedId,level, price,size,priceCount,priceType" +
				" from " +
				" $$feedName$$_MDQuoteTopOfBookBid.win:length(2) quote where price!=prev(1,price) or size!=prev(1,size)",false,false));
		view.getStatements().add(new StatementDefinitionImpl("changesOnlyTopOfBookOffer","" +
				" insert into MDQuoteTopOfBook select " +
				" messageId, snapshotId, sourceName,sourceTimestamp , mdItemId, " +
				" symbol,feedId,level, price,size,priceCount,priceType" +
				" from " +
				" $$feedName$$_MDQuoteTopOfBookOffer.win:length(2) quote where price!=prev(1,price) or size!=prev(1,size)",false,false));
		view.getStatements().add(new StatementDefinitionImpl("DEBUGXXXXXXXXXXX","select * from " +
				" LatencyOffset ",false,true));
		view.getStatements().add(new StatementDefinitionImpl("AllEndUpd", "  " +
				" insert into  AllEndUpd  " +
				" select lastupdate.messageId as messageId,lastupdate.messageId as snapshotId," +
				" lastupdate.sourceName as sourceName,lastupdate.sourceTimestamp as sourceTimestamp ,lastupdate.mdItemId as mdItemId," +
				" lastupdate.symbol as symbol,lastupdate.feedId as feedId,-1L as level," +
				" lastupdate.price as price,lastupdate.size as size,lastupdate.amountOrders as priceCount," +
				" lastupdate.mdItemType as priceType " +
				" from $$feedName$$_MarketSnapshot(mdItemId='-100')  lastupdate",false,false));
		view.getStatements().add(new StatementDefinitionImpl("endBatch", "  " +
				//" insert into MDQuoteTopOfBook " +
				" select b.messageId as messageId,b.snapshotId as snapshotId," +
				" a.sourceName as sourceName,b.sourceTimestamp as sourceTimestamp ,b.mdItemId as mdItemId," +
				" a.symbol as symbol,a.feedId as feedId,b.level as level," +
				" b.price as price,b.size as size,b.priceCount as priceCount," +
				" b.priceType as priceType from  pattern[every (a=MDQuoteTopOfBook->b=AllEndUpd)]  "	,false,false));		
///////////////////////////////////////////////////////////////////
		view.setEventType("MDQuote");
		//add view's parameters
		view.getParameters().add("feedName");//
		return view;
	}
}
