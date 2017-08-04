//package com.nextdoor.bender.operation.json.array;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
//
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.nextdoor.bender.InternalEvent;
//import com.nextdoor.bender.deserializer.DeserializedEvent;
//import com.nextdoor.bender.mutator.MultiplexMutator;
//import com.nextdoor.bender.mutator.UnsupportedMutationException;
//
//import edu.emory.mathcs.backport.java.util.Collections;
//
//public class ArraySplitMutator implements MultiplexMutator {
//
//  @Override
//  public List<InternalEvent> mutateEvent(InternalEvent ievent) throws UnsupportedMutationException {
//    Object payload = ievent.getEventObj().getPayload();
//    DeserializedEvent devent = ievent.getEventObj();
//    
//    if (devent instanceof GenericJsonEvent) {
//      
//    }
//
//    if (payload == null) {
//      return Collections.emptyList();
//    }
//
//    if (!(payload instanceof JsonArray)) {
//      throw new UnsupportedMutationException("Payload data is not a JsonArray");
//    }
//    
//    JsonArray arr = (JsonArray) payload;
//    
//    
//    ArrayList<InternalEvent> output = new ArrayList<InternalEvent>();    
//    for (JsonElement elm : arr) {
//      InternalEvent newEvent = new InternalEvent(output.toString(), ievent.getCtx(), ievent.getArrivalTime());
//      
//      
//    }
//
//    
//    return null;
//  }
//}
