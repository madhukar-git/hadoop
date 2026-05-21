public void handle(RMNodeEvent event) {
    LOG.debug("Processing {} of type {}", event.getNodeId(), event.getType());

    NodeState oldState;
    NodeState newState;
    boolean stateChanged;
    writeLock.lock();
    try {
      oldState = getState();
      try {
         stateMachine.doTransition(event.getType(), event);
      } catch (InvalidStateTransitionException e) {
        LOG.error("Can't handle this event at current state", e);
        LOG.error("Invalid event " + event.getType() + 
            " on Node  " + this.nodeId + " oldState " + oldState);
      }
      newState = getState();
      stateChanged = (oldState != newState);
    } finally {
      writeLock.unlock();
    }

    if (stateChanged) {
      LOG.info(nodeId + " Node Transitioned from " + oldState + " to "
          + newState);
      RMAuditLogger.logSuccess("YARN",
          RMAuditLogger.AuditConstants.NODE_STATE_TRANSITION,
          "RMNode " + nodeId + " from " + oldState + " to " + newState
              + " triggered by " + event.getType());
    }
  }
