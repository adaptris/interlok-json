package com.adaptris.core.services.path;

import com.adaptris.interlok.config.DataDestination;

public class Execution {
  
  private DataDestination sourceJsonPathExpression;
  
  private DataDestination targetDataDestination;
  
  public Execution() {
    
  }

  public DataDestination getSourceJsonPathExpression() {
    return sourceJsonPathExpression;
  }

  public void setSourceJsonPathExpression(DataDestination sourceJsonPathExpression) {
    this.sourceJsonPathExpression = sourceJsonPathExpression;
  }

  public DataDestination getTargetDataDestination() {
    return targetDataDestination;
  }

  public void setTargetDataDestination(DataDestination targetDataDestination) {
    this.targetDataDestination = targetDataDestination;
  }

}
