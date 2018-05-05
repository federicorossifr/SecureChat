/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package securechat;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Federico Rossi
 */
public class SharedState {
    private boolean requestResponse = false;
    private boolean responseAvailable = false;
    private Lock requestLock = new ReentrantLock();
    private Condition responseAvailableCondition = requestLock.newCondition();
    private AtomicBoolean pendingRequest = new AtomicBoolean(false);
    private boolean handshakeProtocolTerminated = false;
    private boolean handshakeProtocolTerminationStatus = false;
    private Lock hpStatusLock = new ReentrantLock();
    private Condition handshakeStatusAvailable = hpStatusLock.newCondition();
    private static SharedState _instance;
    
    
    private SharedState() {
        //TODO
    }
    
    public static SharedState getInstance() {
        if(_instance == null) _instance = new SharedState();
        return _instance;
    }
    
    public boolean waitForResponse() {
        requestLock.lock();
        try {
            while(!responseAvailable) {responseAvailableCondition.await();}
            responseAvailable = false;
        } catch(Exception e) {e.printStackTrace(); return false;}
        finally {requestLock.unlock();}
        return requestResponse;
    }
    
    public boolean waitProtocol() {
        hpStatusLock.lock();
        try {
            while(!handshakeProtocolTerminated) {handshakeStatusAvailable.await();}
        } catch(Exception e ) {e.printStackTrace(); return false;}
        finally {hpStatusLock.unlock();}
        return handshakeProtocolTerminationStatus;
    }
    
    public void protocolDone(boolean result) {
        hpStatusLock.lock();
        try {
            handshakeProtocolTerminated=true;
            handshakeProtocolTerminationStatus=result;
            System.out.println("Signalling...");
            handshakeStatusAvailable.signal();
        } catch(Exception e) {e.printStackTrace();}
        finally{hpStatusLock.unlock();}
    }
    
    
    public void setResponse(boolean response) {
        requestLock.lock();
        try {
            responseAvailable=true;
            requestResponse=response;
            System.out.println("STATE: Response set, now signalling");
            responseAvailableCondition.signal();
        } catch(Exception e) {e.printStackTrace();}
        finally {requestLock.unlock();}
    }
    
    public boolean isRequestPending() {
        return pendingRequest.getAndSet(false);
    }
    
    public void setPendingRequest(boolean state) {
        pendingRequest.set(state);
    }
            
    
    
}