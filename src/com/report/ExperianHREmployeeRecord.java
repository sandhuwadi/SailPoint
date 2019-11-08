package com.report;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import sailpoint.api.SailPointContext;
import sailpoint.connector.Connector;
import sailpoint.connector.ConnectorFactory;
import sailpoint.object.Application;
import sailpoint.object.Attributes;
import sailpoint.object.LiveReport;
import sailpoint.object.QueryOptions;
import sailpoint.object.Sort;
import sailpoint.reporting.datasource.JavaDataSource;
import sailpoint.task.Monitor;
import sailpoint.tools.GeneralException;
import sailpoint.tools.JdbcUtil;
/**
 * This class is used in reports to pull HR Record for a given employee number
 * @author Swathi Chukkala
 *
 */
public class ExperianHREmployeeRecord implements JavaDataSource {
	private QueryOptions baseQueryOptions;
	private SailPointContext context;
	private List<String> applicationList;
	private Integer startRow;
	private Integer pageSize;
	private Monitor monitor;
	private List<Map<String, Object>> objectList = new ArrayList<Map<String, Object>>();

	private Iterator<Map<String, Object>> objects;
	private Map<String, Object> object;
	private String employeeNumber;
	String sqlQuery="select * from XEXP.EXP_HR_OIM_V where employee_number =?";
    Log logger = LogFactory.getLog("exp.task.experianCustomTask");

	@SuppressWarnings("unchecked")
	public void initialize(SailPointContext context, LiveReport report,
			Attributes<String, Object> arguments, String groupBy,
			List<Sort> sort) throws GeneralException {
		this.context = context;
		logger.info("entered initialize method ExperianHREmployeeRecord with :::arguments : "+arguments);
		baseQueryOptions = new QueryOptions();
		try {
			if(arguments.getList("applications")!=null) {

				this.applicationList= (List<String>)arguments.getList("applications");
				logger.info("initialize method application input list:::"+this.applicationList);
			} 
			if(arguments.getString("employeeNumber")!=null) {

				this.employeeNumber = arguments.getString("employeeNumber");
			} 
			
			prepare(this.employeeNumber);
		} catch (SQLException e) {
			logger.info("Exception occured in initialize method ExperianHREmployeeRecord :::");
			e.printStackTrace();
			logger.error(e.getMessage());
		}

	}

	/**
	 * This is the method which prepares data for the report.
	 *
	 */
	private void prepare(String employeeNumber) throws GeneralException, SQLException {

		try {
			logger.info("entered method prepare in HREmployeeRecord classs :: "+employeeNumber);
			Map<String, Object> itemMap = null;
			itemMap = new HashMap<String, Object>();
			String appName=this.applicationList.get(0);
			Connection con=getJDBCConnection(appName);
			
			PreparedStatement pstmt=con.prepareStatement(this.sqlQuery);
			pstmt.setString(1,employeeNumber);
			ResultSet rs=pstmt.executeQuery();
			ResultSetMetaData md = rs.getMetaData();
			int columns = md.getColumnCount();
			while (rs.next()){
				 itemMap = new HashMap<String, Object>();
			     for(int i=1; i<=columns; ++i){           
			    	 itemMap.put(md.getColumnName(i),rs.getObject(i));
			     }
			     Iterator<Entry<String, Object>> mapIterator = itemMap.entrySet().iterator();
			     while (mapIterator.hasNext()) {
			         Entry<String, Object> entry = mapIterator.next();
			         logger.info("Item map entries::::"+entry.getKey() + ": " + entry.getValue());
			     } 
			     objectList.add(itemMap);
			  }
			
			objects = objectList.iterator();
			logger.info("contructed objectList size :::"+objectList.size() );
		//	context.decache(custom);
			} catch (Exception e) {
			logger.info("Exception occured in HR View datasource class :: ");
			e.printStackTrace();
			logger.error(e.getMessage());
		}

	}
	public Connection getJDBCConnection(String applicationName){
		Connection connection=null;
		try{
		Application app = context.getObject(Application.class,applicationName);
		Connector conn = ConnectorFactory.getConnector(app, null); 
		
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("url",String.valueOf(app.getAttributeValue("url")));
		map.put("user", String.valueOf(app.getAttributeValue("user")));
		
		map.put("password", context.decrypt (String.valueOf(app.getAttributeValue("password"))));
		map.put("driverClass", String.valueOf(app.getAttributeValue("driverClass")));
		logger.info("Connection attributes :::" +app.getAttributeValue("url"));
		connection= JdbcUtil.getConnection(map);
		logger.info("Got the connection to Database ::"+connection);
		}catch(Exception ex){
			logger.info("Exception occured while gettubg HR connectionb");
			ex.printStackTrace();
			logger.error(ex.getMessage());
		}
		return connection;
	}
	public QueryOptions getBaseQueryOptions() {
		return baseQueryOptions;
	}

	public String getBaseHql() {
		return null;
	}

	public Object getFieldValue(String fieldName) throws GeneralException {

		logger.info("returning field name :::fieldName"+fieldName +" ::value::" +this.object.get(fieldName));
		return this.object.get(fieldName);
	}

	public int getSizeEstimate() throws GeneralException {

		if (this.applicationList != null) {
			return this.applicationList.size();
		} else {
			return 1;

		}
	}

	public void close() {

	}

	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
	}

	public Object getFieldValue(JRField jrField) throws JRException {
		String fieldName = jrField.getName();

		try {
			return getFieldValue(fieldName);
		} catch (GeneralException e) {
			throw new JRException(e);
		}
	}

	public boolean next() throws JRException {
		boolean hasMore = false;

		if (this.objects != null) {
			hasMore = this.objects.hasNext();

			if (hasMore) {
				this.object = this.objects.next();
			} else {
				this.object = null;
			}

		}
		return hasMore;
	}

	public void setLimit(int startRow, int pageSize) {
		this.startRow = startRow;
		this.pageSize = pageSize;

	}

}
