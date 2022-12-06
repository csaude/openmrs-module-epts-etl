package fgh.sp.openmrs_changed_records_action.eip.model;


public class SenderRetryQueueItem extends BaseRetryQueueItem {

    public static final long serialVersionUID = 1;

     private Event event;

     private String destination;

    /**
     * Gets the event
     *
     * @return the event
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Sets the event
     *
     * @param event the event to set
     */
    public void setEvent(Event event) {
        this.event = event;
    }

    public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	@Override
    public String toString() {
        return getClass().getSimpleName() + " {destination=" + destination + ", attemptCount=" + getAttemptCount() + ", " + event + "}";
    }

}
