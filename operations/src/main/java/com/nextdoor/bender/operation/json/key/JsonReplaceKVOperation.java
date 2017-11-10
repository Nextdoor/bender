package com.nextdoor.bender.operation.json.key;

import com.google.gson.JsonObject;
import com.nextdoor.bender.operation.json.PayloadOperation;

public class JsonReplaceKVOperation extends PayloadOperation {
  private String key;
  private String value;

  public JsonReplaceKVOperation(String key, String value) {
    this.key = key;
    this.value = value;
  }

  @Override
  protected void perform(JsonObject payload) {
    if (payload.has(this.key))
         payload.addProperty(this.key, this.value);
  }
}
