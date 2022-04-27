package eu.interopehrate.td2de;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hl7.fhir.r4.model.Bundle;

import eu.interopehrate.d2d.D2DOperation;
import eu.interopehrate.d2d.D2DRequest;
import eu.interopehrate.d2d.D2DResponse;
import eu.interopehrate.d2d.D2DStatusCodes;
import eu.interopehrate.td2de.api.TD2DListener;

class TD2DListenerInvoker extends Thread {

	private static final Logger logger = Logger.getLogger(TD2DListenerInvoker.class.getName());
	private BlockingQueue<Notification> notifications = new LinkedBlockingDeque<Notification>();
	
	TD2DListenerInvoker() {
		setName("TD2D");
	}

	
	public void addNotification(D2DRequest request, D2DResponse response, Bundle bundle, TD2DListener listener) {
		notifications.add(new Notification(request, response, bundle, listener));
	}
	
	
	@Override
	public void run() {
        try {
        	Notification notification;
        	D2DResponse response;
        	
            while (true) {
            	notification = notifications.take();
            	response = notification.getResponse();
        		try {
        			if (response.getStatus() == D2DStatusCodes.SUCCESSFULL) {
        				if(notification.getRequest().getOperation() == D2DOperation.READ) {
        					logger.fine("Invoking method onRead() on TD2DListener");
        					notification.getListener().onRead(notification.getBundle(), 
        							response.getHeader().getPage(), 
        							response.getHeader().getTotalPages());
        				} else {
        					logger.fine("Invoking method onSearch() on TD2DListener");
        					notification.getListener().onSearch(notification.getBundle(), 
        							response.getHeader().getPage(), 
        							response.getHeader().getTotalPages());
        				}
        			} else { 
        				logger.fine("Invoking method onError() on TD2DListener");
        				notification.getListener().onError(response.getStatus(), response.getMessage());
        			}
        		} catch (Exception e) {
        			logger.log(Level.SEVERE, "Exception raised by TD2DListener", e);
        		} 
            }
        } catch (InterruptedException e) {
			logger.fine("Stopping ListenerInvoker Thread.");
        }
		
		/*
		try {
			if (response.getStatus() == D2DStatusCodes.SUCCESSFULL) {
				if(request.getOperation() == D2DOperation.READ) {
					logger.fine("Invoking method onRead() on TD2DListener");
					listener.onRead(bundle, response.getHeader().getPage(), response.getHeader().getTotalPages());
				} else {
					logger.fine("Invoking method onSearch() on TD2DListener");
					listener.onSearch(bundle, response.getHeader().getPage(), response.getHeader().getTotalPages());
				}
			} else { 
				logger.fine("Invoking method onError() on TD2DListener");
				listener.onError(response.getStatus(), response.getMessage());
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception raised by TD2DListener", e);
		} finally {
			if (response.getHeader().getPage() == response.getHeader().getTotalPages()) {
				// removes request from cache because it has been completely handled
				if (requestCache.containsKey(request.getId()))
					requestCache.remove(request.getId());
			}
		}
		*/
		
	}
	
	
	class Notification {
		private D2DRequest request;
		private D2DResponse response;
		private Bundle bundle;
		private TD2DListener listener;
		
		public Notification(D2DRequest request, D2DResponse response,
				Bundle bundle, TD2DListener listener) {
			this.request = request;
			this.response = response;
			this.bundle = bundle;
			this.listener = listener;
		}
		
		public D2DRequest getRequest() {
			return request;
		}
		public D2DResponse getResponse() {
			return response;
		}
		public Bundle getBundle() {
			return bundle;
		}
		public TD2DListener getListener() {
			return listener;
		}
	}
	
}

