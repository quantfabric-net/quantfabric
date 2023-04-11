//package trades;
//
//import com.quantfabric.algo.market.connector.binance.commands.BinanceSubmitOrder;
//import com.quantfabric.algo.market.connector.binance.stream.BinanceStreamingMarketConnection;
//import com.quantfabric.algo.market.connector.binance.stream.BinanceStreamingXChangeAdapter;
//import com.quantfabric.algo.order.TradeOrder;
//import org.knowm.xchange.dto.trade.LimitOrder;
//
//import java.util.Properties;
//
//public class SendTradeTest {
//
//    public static void main(String[] args) {
//        BinanceStreamingMarketConnection connection = new BinanceStreamingMarketConnection();
//        BinanceStreamingXChangeAdapter adapter = new BinanceStreamingXChangeAdapter(connection, new Properties(), new Properties());
//        TradeOrder tradeOrder = new TradeOrder();
//        tradeOrder.setPrice(1);
//        tradeOrder.setSize(1);
//        tradeOrder.setTimeInForceMode(TradeOrder.TimeInForceMode.GOOD_TILL_SECONDS);
//        tradeOrder.setOrderType(TradeOrder.OrderType.LIMIT);
//        tradeOrder.setOrderSide(TradeOrder.OrderSide.BUY);
//        tradeOrder.setInstrument(feed.getInstrument());
//        BinanceSubmitOrder xChangeSubmitOrder = new BinanceSubmitOrder(tradeOrder);
//        LimitOrder order = null;
//        try {
//            order = xChangeSubmitOrder.createOrder();
//            adapter.sendMessage(order, tradeOrder);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//    }
//
//}
