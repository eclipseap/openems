package io.openems.edge.controller.api.mqpp;
//package io.openems.edge.controller.api.amqp;
//
//import org.java_websocket.WebSocket;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import io.openems.common.exceptions.OpenemsException;
//
//public class OnError implements io.openems.common.websocket.OnError {
//
//	private final Logger log = LoggerFactory.getLogger(OnError.class);
//	private final BackendApiImpl parent;
//
//	public OnError(BackendApiImpl parent) {
//		this.parent = parent;
//	}
//
//	@Override
//	public void run(WebSocket ws, Exception ex) throws OpenemsException {
//		this.parent.logWarn(this.log, "Error: " + ex.getMessage());
//	}
//
//}
