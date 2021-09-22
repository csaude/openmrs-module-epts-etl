package fgh.spi.changedrecordsdetector;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ServiceLoader;


/**
 * Classe respons&aacute;vel pela inicializacao e controlo das operacoes de arranque automatico.
 * 
 * As operacoes tratadas por esta classe sao disponibilizadas por meio de classes provedoras de servico as quais
 * disponibilizam funcionalidades agendadas.
 * 
 * 
 * @see GenericOperation
 *
 */
public abstract class GenericOperationsService<T extends GenericOperation> implements Serializable{
	private static final long serialVersionUID = -7205842955086955163L;
	
	private ServiceLoader<T> operationsLookup;
	protected Collection<T> operations;
	
	protected GenericOperationsService(){
		
		this.operationsLookup = ServiceLoader.load(getServiceClass());
		
		Iterator<T> operations = operationsLookup.iterator();
		
		this.operations = new ArrayList<T>();
		
		while(operations.hasNext()){
			this.operations.add(operations.next());
		}		
	}
	
	protected abstract Class<T> getServiceClass();
}
