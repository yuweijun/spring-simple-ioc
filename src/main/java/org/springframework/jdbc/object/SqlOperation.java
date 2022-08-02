/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.jdbc.object;

import java.sql.ResultSet;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * RdbmsOperation using a JdbcTemplate and representing a SQL-based
 * operation such as a query or update, as opposed to a stored procedure.
 *
 * <p>Configures a PreparedStatementCreatorFactory based on the
 * declared parameters.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: SqlOperation.java,v 1.11 2004-05-28 14:11:41 jhoeller Exp $
 */
public abstract class SqlOperation extends RdbmsOperation {

	private int resultSetType = ResultSet.TYPE_FORWARD_ONLY;

	private boolean updatableResults = false;

	/**
	 * Object enabling us to create PreparedStatementCreators
	 * efficiently, based on this class's declared parameters.
	 */
	private PreparedStatementCreatorFactory preparedStatementFactory;


	/**
	 * Set whether to use prepared statements that return a
	 * specific type of ResultSet.
	 * @param resultSetType the ResultSet type
	 * @see ResultSet#TYPE_FORWARD_ONLY
	 * @see ResultSet#TYPE_SCROLL_INSENSITIVE
	 * @see ResultSet#TYPE_SCROLL_SENSITIVE
	 */
	protected void setResultSetType(int resultSetType) {
		this.resultSetType = resultSetType;
	}

	/**
	 * Return whether prepared statements will return a specific
	 * type of ResultSet.
	 */
	protected int getResultSetType() {
		return resultSetType;
	}

	/**
	 * Set whether to use prepared statements capable of returning
	 * updatable ResultSets.
	 */
	protected void setUpdatableResults(boolean updatableResults) {
		this.updatableResults = updatableResults;
	}

	/**
	 * Return whether prepared statements will return updatable ResultSets.
	 */
	protected boolean isUpdatableResults() {
		return updatableResults;
	}
	

	/**
	 * Overridden method to configure the PreparedStatementCreatorFactory
	 * based on our declared parameters.
	 */
	protected final void compileInternal() {
		// validate parameter count
		int bindVarCount = 0;
		try {
			bindVarCount = JdbcUtils.countParameterPlaceholders(getSql(), '?', '\'');
		}
		catch (IllegalArgumentException ex) {
			// transform JDBC-agnostic error to data access error
			throw new InvalidDataAccessApiUsageException(ex.getMessage());
		}
		if (bindVarCount != getDeclaredParameters().size())
			throw new InvalidDataAccessApiUsageException("SQL '" + getSql() + "' requires " + bindVarCount +
			                                             " bind variables, but " + getDeclaredParameters().size() +
																									 " variables were declared for this object");

		this.preparedStatementFactory = new PreparedStatementCreatorFactory(getSql(), getDeclaredParameters());
		this.preparedStatementFactory.setResultSetType(this.resultSetType);
		this.preparedStatementFactory.setUpdatableResults(this.updatableResults);
		onCompileInternal();
	}

	/**
	 * Hook method that subclasses may override to post-process compilation.
	 * This implementation does nothing.
	 * @see #compileInternal
	 */
	protected void onCompileInternal() {
	}

	/**
	 * Return a PreparedStatementCreator to perform an operation
	 * with the given parameters.
	 * @param params parameter array. May be null.
	 */
	protected final PreparedStatementCreator newPreparedStatementCreator(Object[] params) {
		return this.preparedStatementFactory.newPreparedStatementCreator(params);
	}

	/**
	 * Return a PreparedStatementSetter to perform an operation
	 * with thhe given parameters.
	 * @param params parameter array. May be null.
	 */
	protected final PreparedStatementSetter newPreparedStatementSetter(Object[] params) {
		return this.preparedStatementFactory.newPreparedStatementSetter(params);
	}

}
