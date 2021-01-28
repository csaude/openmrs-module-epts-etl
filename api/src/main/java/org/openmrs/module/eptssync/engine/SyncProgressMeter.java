package org.openmrs.module.eptssync.engine;

import java.util.Date;

import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;
import org.openmrs.module.eptssync.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDownInitializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

public class SyncProgressMeter implements  TimeCountDownInitializer{
	/**
	 * Utilitarios do sistema
	 */
	public CommonUtilities utilities = CommonUtilities.getInstance();
	
	/**
	 * Constante usada para indicar o estado de erro
	 */
	public static final int STATUS_ERROR = -1;
	
	/**
	 * Total de registos
	 */
	private int total;
	
	/**
	 * Registos processados
	 */
	private int processed;
	
	/**
	 * Progresso da migracao
	 */
	private double progress;
	
	/**
	 * Mensagem do estado corrente da migracao
	 */
	private String statusMsg;
	
	/**
	 * Tempo decorrido
	 */
	private String time;
	
	/**
	 * Indica se existe um erro
	 */
	private boolean statusError;
	
	/**
	 * Monitor corrente da migracao
	 */
	private MonitoredOperation monitor;
	
	private String id;
	
	private String designation;
	
	private boolean _default;
	
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
	
	public SyncProgressMeter(MonitoredOperation monitor, String statusMsg, int total, int processed){
		this.monitor = monitor;
		
		refresh(statusMsg, total, processed);
		
		this.id = "meter_default_id" + this.hashCode();
		
		//Assume-se que por omissao qualquer meter encontra-se desactualizado, de tal maneiras que 
		//qualquer chamada ao refresh deve ser executada
		this.updated = false;
	}
	
	private SyncProgressMeter (String id){
		this._default = true;
		this.id = id;
	}
	
	public static SyncProgressMeter defaultProgressMeter(String id){
		return new SyncProgressMeter(id);
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
		//if (_default) throw new ForbiddenOperationException("You cannot refresh a default progress meter!");
		
		this.total = total;
		this.processed = processed;
		
		if (this.total > 0) {
			
			double processedAsDouble = this.processed;
			double totalAsDouble = this.total;
			
			this.progress = Double.parseDouble(utilities.getNumberInXPrecision((processedAsDouble / totalAsDouble)*100, 2));
		}else {
			this.progress = 0;
		}
		
		this.statusMsg = statusMsg;
		
		this.time = getTimer() != null ? getTimer().toString() : "00:00:00";
		
		
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

	@JsonIgnore
	public TimeController getTimer() {
		return this.monitor != null ? this.monitor.getTimer() : null;
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
	@JsonIgnore
	public Date getStartTime(){
		return getTimer() != null ? getTimer().getStartTime() : null;
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
	 * @return the time
	 */
	public String getTime() {
		return time;
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
		return progress;
	}
	
	/**
	 * Verifica se a migracao esta em curso
	 * 
	 * @return {@code true} se a migracao esta em curso, ou {@code false} caso contrario
	 */
	public boolean isRunning(){
		return this.monitor != null ? this.monitor.isRunning() : false;
	}
	
	public boolean isSleeping(){
		return this.monitor != null ? this.monitor.isSleeping() : false;
	}
	
	@JsonIgnore
	public String parseToJSON(){
		try {
			return new ObjectMapperProvider().getContext(SyncProgressMeter.class).writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	@JsonIgnore
	public MonitoredOperation getMonitor() {
		return monitor;
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
	
	/**
	 * @return o valor do atributo {@link #utilities}
	 */
	public CommonUtilities getUtilities() {
		return utilities;
	}
	
	public String getDetailedRemaining(){
		int remaining = total - processed;
		
		return remaining + "(" + (100 - this.progress) + "%)";
	}
	
	public String getDetailedProgress(){
		return this.processed + "(" + this.progress + "%)";
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
	public String getThreadNamingPattern() {
		String pathern = "["+this.getClass().getCanonicalName() + "]" + "[%d]";
		
		if (getMonitor() != null){
			pathern =  "[" + getMonitor().getClass().getCanonicalName() +"]" + pathern; 
		}
		else pathern = "[Aknown Monitor]" + pathern;
		
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
}