package org.openmrs.module.eptssync.engine;

import java.util.Date;

import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDownInitializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

public class SyncProgressMeter implements  TimeCountDownInitializer{
	/**
	 * Utilitarios do sistema
	 */
	private CommonUtilities utilities = CommonUtilities.getInstance();
	
	/**
	 * Constante usada para indicar o estado de erro
	 */
	public static final int STATUS_ERROR = -1;
	
	public static final String STATUS_NOT_INITIALIZED="NOT INITIALIZED";
	public static final String STATUS_RUNNING="RUNNING";
	public static final String STATUS_PAUSED = "PAUSED";
	public static final String STATUS_STOPPED="STOPPED";
	public static final String STATUS_SLEEPING="SLEEPING";
	public static final String STATUS_FINISHED="FINISHED";
	
	private String id;
	
	private String designation;
	/**
	 * Total de registos
	 */
	private int total;
	
	/**
	 * Estado corrente
	 */
	private String status;
	
	/**
	 * Registos processados
	 */
	private int processed;
	
	/**
	 * Mensagem do estado corrente da migracao
	 */
	private String statusMsg;
	
	/**
	 * Indica se existe um erro
	 */
	private boolean statusError;
	
	//private boolean _default;
	
	private TimeCountDown updateControl;
	
	/**
	 * Indica se este {@link MigrationProgressMeter} encontra-se actualizado ou nao.
	 * <p>Com base no valor deste atributo pode se evitar o "refresh" sempre que o mesmo se encontrar 
	 * updated
	 */
	private boolean updated;
	
	/**
	 * Indica o intervalo de tempo durante o qual este {@link SyncProgressMeter} sera considerado updated
	 */
	private int refreshInterval;
	private Date startTime;
	private Date finishTime;
	private double elapsedTime;
	private TimeController timer;
	
	public SyncProgressMeter() {
		this.status = STATUS_NOT_INITIALIZED;
	}
	
	public SyncProgressMeter(String statusMsg, int total, int processed){
		this();
		
		refresh(statusMsg, total, processed);
		
		this.id = "meter_default_id" + this.hashCode();
		
		//Assume-se que por omissao qualquer meter encontra-se desactualizado, de tal maneiras que 
		//qualquer chamada ao refresh deve ser executada
		this.updated = false;
	}
	
	private SyncProgressMeter (String id){
		this();
		
		//this._default = true;
		this.id = id;
	}
	
	public static SyncProgressMeter defaultProgressMeter(String id){
		return new SyncProgressMeter(id);
	}
	
	public Date getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}
	
	public void setElapsedTime(double elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public double getElapsedTime() {
		return getTimer() != null ? getTimer().getDuration(TimeController.DURACAO_IN_MINUTES) : this.elapsedTime;
	}

	public String getStatus() {
		return status;
	}

	public void setProcessed(int processed) {
		this.processed = processed;
	}

	/**
	 * Refrsca a informacao do estado actual da migracao, recalcunlando a percentagem de progresso
	 * 
	 * @param statusMsg Mensagem do corrente estado da migracao
	 * @param total de registos em migracao
	 * @param processed Registos processados
	 * @param timer Temporizador da migracao
	 */
	public synchronized void refresh(String statusMsg, int total, int processed){
		this.total = total;
		this.processed = processed;
		
		this.statusMsg = statusMsg;
		
		if (refreshInterval > 0 && this.updateControl == null){
			this.updateControl = TimeCountDown.wait(this, 60*this.refreshInterval, ""); 
			this.updated = true;
		}
		else
		if (this.updateControl != null){
			this.updated = true;
			
			this.updateControl.restart();
		}
	}

	public String getHumanReadbleTime() {
		return getTimer() != null ? getTimer().toString() : "00:00:00";
	}
	
	@JsonIgnore
	public TimeController getTimer() {
		return this.timer;
	}
	
	public void changeRefreshInterval(int refreshInterval){
		this.refreshInterval = refreshInterval;
		
		if (refreshInterval > 0){
		}
		else {
			this.updateControl = null;
			this.updated = false;
		}
	}
	
	/**
	 * 
	 * @return a hora de inicio da migracao
	 */
	public Date getStartTime(){
		return this.startTime;
	}
	/**
	 * 
	 * @return a hora de inicio da migracao formatada
	 */
	public String getFormatedStartTime(){
		return getStartTime() != null ? DateAndTimeUtilities.formatToDDMMYYYY_HHMISS(getStartTime()) : null;
	}
	/**
	 * 
	 * @param exception de erro na migracao
	 */
	public void reportError(Exception exception) {
		this.statusMsg = exception.getLocalizedMessage();
		statusError = true;
	}

	/**
	 * @return the statusError
	 */
	public boolean isStatusError() {
		return statusError;
	}

	/**
	 * @return the statusMsg
	 */
	public String getStatusMsg() {
		return statusMsg;
	}

	/**
	 * @return the total
	 */
	public int getTotal() {
		return (int)total;
	}

	/**
	 * @return Quantidade de registos processados
	 */
	public int getProcessed() {
		return (int)processed;
	}

	public int getRemain() {
		return this.total - this.processed;
	}
	
	/**
	 * @return a percentagem de progresso da migracao
	 */
	public double getProgress() {
		double progress = 0;
		
		if (this.total > 0) {
			
			double processedAsDouble = this.processed;
			double totalAsDouble = this.total;
			
			progress = Double.parseDouble(utilities.getNumberInXPrecision((processedAsDouble / totalAsDouble)*100, 2));
		}
		
		return progress;
	}
	
	public boolean isRunning() {
		return this.status.equals(SyncProgressMeter.STATUS_RUNNING);
	}
	
	public boolean isPaused() {
		return this.status.equals(SyncProgressMeter.STATUS_PAUSED);
	}
	
	public boolean isStopped() {
		return this.status.equals(SyncProgressMeter.STATUS_STOPPED);
	}
	
	public boolean isSleeping() {
		return this.status.equals(SyncProgressMeter.STATUS_SLEEPING);
	}
	
	public boolean isFinished() {
		return this.status.equals(SyncProgressMeter.STATUS_FINISHED);
	}

	public void changeStatusToSleeping() {
		this.status = SyncProgressMeter.STATUS_SLEEPING;
		this.statusMsg = SyncProgressMeter.STATUS_SLEEPING;
		
	}
	
	public void changeStatusToRunning() {
		this.status = SyncProgressMeter.STATUS_RUNNING;
		this.statusMsg = SyncProgressMeter.STATUS_RUNNING;
		
		tryToInitializeTimer();
		
		this.getTimer().start();
	}
	
	public void changeStatusToStopped() {
		this.status = SyncProgressMeter.STATUS_STOPPED;	
		this.statusMsg = SyncProgressMeter.STATUS_STOPPED;
		
		tryToInitializeTimer();
		
		this.getTimer().stop();
	}
	
	public void changeStatusToFinished() {
		this.status = SyncProgressMeter.STATUS_FINISHED;	
		this.statusMsg = SyncProgressMeter.STATUS_FINISHED;
		
		this.finishTime = DateAndTimeUtilities.getCurrentDate();
		
		tryToInitializeTimer();
		
		this.getTimer().stop();
	}
	
	private void tryToInitializeTimer() {
		if (this.getTimer() == null) {
			this.timer = new TimeController();
			this.startTime = this.timer.getStartTime();
		}
	}
	
	@JsonIgnore
	public String parseToJSON(){
		try {
			return new ObjectMapperProvider().getContext(SyncProgressMeter.class).writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @return o valor do atributo {@link #id}
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Modifica o valor do atributo {@link #id} para o valor fornecido pelo parâmetro <code>id</code>
	 * 
	 * @param id novo valor para o atributo {@link #id}
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof String) {
			return utilities.isStringIn(this.id, obj.toString());
		}
		
		if (!(obj instanceof SyncProgressMeter)) return false;
		
		SyncProgressMeter otherMeter = (SyncProgressMeter)obj;
		
		if (utilities.isStringIn(this.id, otherMeter.id)) return true;
		
		return false;
	}
	
	public String getDetailedRemaining(){
		int remaining = total - processed;
		
		return remaining + "(" + (100 - this.getProgress()) + "%)";
	}
	
	public String getDetailedProgress(){
		return this.processed + "(" + this.getProgress() + "%)";
	}
	
	/**
	 * @return o valor do atributo {@link #designation}
	 */
	public String getDesignation() {
		return designation;
	}
	
	/**
	 * Modifica o valor do atributo {@link #designation} para o valor fornecido pelo parâmetro <code>designation</code>
	 * 
	 * @param designation novo valor para o atributo {@link #designation}
	 */
	public void setDesignation(String designation) {
		this.designation = designation;
	}
	

	@Override
	@JsonIgnore
	public String getThreadNamingPattern() {
		String pathern = "["+this.getClass().getCanonicalName() + "]" + "[%d]";
		
		pathern = "[ProgressMeter]" + pathern;
		
		return pathern;
	}
	
	/**
	 * Indica se este meter esta actualizado ou nao.
	 * <p>Cada meter pode definir um intervalo dentro do qual o mesmo pode ser actualizado
	 * Qualquer tentativa de "actualizacao" de um meter que esteja "actualizado" podera ser ignorado
	 * evitando dessa forma consumo de recursos desnecessario
	 * 
	 * @return true se este meter se encontrar actualizado ou false no caso contrario
	 */
	public boolean isUpdated(){
		return this.updated;
	}
	
	@Override
	public void onFinish() {
		this.updated = false;
	}

	public void retrieveTimer() {
		if (getStartTime() != null) {
			this.timer = TimeController.retrieveTimer(getStartTime(), getElapsedTime());
		}
	}

	public static SyncProgressMeter fullInit(String status, Date startTime, Date stopTime, int total, int processed) {
		SyncProgressMeter progressMeter = new SyncProgressMeter();
		
		progressMeter.status = status;
		progressMeter.startTime = startTime;
		progressMeter.total = total;
		progressMeter.processed = processed;
		progressMeter.elapsedTime = TimeController.computeElapsedTime(startTime, stopTime);
		
		progressMeter.timer = new TimeController(startTime, progressMeter.elapsedTime);
		
		if (progressMeter.isFinished()) progressMeter.finishTime = stopTime;
		
		return progressMeter;
	}
}