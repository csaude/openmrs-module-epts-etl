package fgh.sp.openmrs_changed_records_action.eip.model;

import java.util.Date;

public abstract class BaseRetryQueueItem extends AbstractEntity {
	private static final long serialVersionUID = 1L;

	private String message;
    private String causeMessage;
    private Integer attemptCount = 1;
    private Date dateChanged;

    /**
     * Gets the message
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message
     *
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the causeMessage
     *
     * @return the causeMessage
     */
    public String getCauseMessage() {
        return causeMessage;
    }

    /**
     * Sets the causeMessage
     *
     * @param causeMessage the causeMessage to set
     */
    public void setCauseMessage(String causeMessage) {
        this.causeMessage = causeMessage;
    }

    /**
     * Gets the attemptCount
     *
     * @return the attemptCount
     */
    public Integer getAttemptCount() {
        return attemptCount;
    }

    /**
     * Sets the attemptCount
     *
     * @param attemptCount the attemptCount to set
     */
    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }

    /**
     * Gets the dateChanged
     *
     * @return the dateChanged
     */
    public Date getDateChanged() {
        return dateChanged;
    }

    /**
     * Sets the dateChanged
     *
     * @param dateChanged the dateChanged to set
     */
    public void setDateChanged(Date dateChanged) {
        this.dateChanged = dateChanged;
    }

}
