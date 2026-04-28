package org.openmrs.module.epts.etl.utilities.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class TestConcurrency {
	
	public static void main(String[] args) throws Exception {
		runInConcurrency();
	}
	
	public static void runInConcurrency() throws DBException {
		OpenConnection conn1 = QuickTest.openConnection();
		
		List<CompletableFuture<Void>> tasks = new ArrayList<>(2);
		
		tasks.add(CompletableFuture.runAsync(() -> {
			runTask("Task 1", conn1);
		}));
		
		tasks.add(CompletableFuture.runAsync(() -> {
			runTask("Task 2", conn1);
		}));
		
		CompletableFuture<Void> allOf = CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
		allOf.join();
		
		System.out.println("All tasks completed");
		
		conn1.markAsSuccessifullyTerminated();
		conn1.finalizeConnection();
	}
	
	public static void runTask(String taskId, Connection conn) throws RuntimeException {
		System.out.println("Starting Task 1");
		
		try {
			int result = testConcurrency(taskId, "insert into tmp(name) values(' " + taskId + " ')", conn);
			
			System.out.println("Thread " + taskId + " result: " + result);
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Integer testConcurrency(String threadId, String sql, Connection connection) throws SQLException {
		PreparedStatement st = null;
		
		try {
			st = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			
			System.out.println("Thread " + threadId + " is starting the execution of statment...");
			
			st.execute();
			
			System.out.println("Thread " + threadId + " finished execution of statment...");
			
			ResultSet rs = st.getGeneratedKeys();
			
			if (rs != null && rs.next()) {
				
				System.out.println("Thread " + threadId + " is retrieving the result of statment...");
				
				return rs.getInt(1);
			} else
				return 0;
			
		}
		finally {
			try {
				System.out.println("Thread " + threadId + " is finishing...");
				
				if (threadId.equals("Task 1")) {
					System.out.println(threadId + "is Skipping close the statment");
				} else {
					st.close();
				}
				
				System.out.println("Thread " + threadId + " finalized the db operation!");
				
				st = null;
			}
			catch (NullPointerException e) {
				st = null;
			}
			catch (SQLException e) {
				st = null;
				throw new DBException(e);
			}
			st = null;
		}
	}
}
