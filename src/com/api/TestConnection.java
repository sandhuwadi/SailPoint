package com.api;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.object.Identity;
import sailpoint.spring.SpringStarter;
import sailpoint.tools.GeneralException;
/*
 * Below standalone code connects to local enviroment
 */

public class TestConnection {
  public static void main(String [] args){
		SailPointContext context;
		SpringStarter starter = null ;
		Identity identity ;
		
		
		 try {
			 List<String> dbData = new ArrayList<String>();
			 List<Document> xmlData =new ArrayList<Document>();
			 starter = new SpringStarter("iiqBeans") ;
			 System.out.println("Started");
			 starter.start();  
			context = SailPointFactory.createContext();
			if(null != context){
				System.out.println("Connection is succesful");
			}
			// identity = context.getObject(Identity.class, "Dorothy.Robinson");
			Connection conn= context.getConnection();
			if(null != conn){
				System.out.println("DB Connection got stablished");
				
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT attributes from spt_task_result");
				while (rs.next()) {
					
					  String value = rs.getString("attributes");
					  dbData.add(value);
					}
			}
		//	String dspName=identity.getDisplayableName();  
		//	String lastName=identity.getLastname();
		   //  System.out.println("Identity @@@@@@@@@" + lastName);  
			
			
			System.out.println("Total data of the DB is as follows-->"+dbData.size());
			if(dbData.size()>0){
				for(String dbString :dbData){
					xmlData.add(Util.convertStringToXMLDocument(dbString));
				}
			}
			
			System.out.println("After XML CONVERSION SIZE OF List--->"+xmlData.size());
			
			System.out.println("XML PRINTING----"+xmlData.get(2).getFirstChild().getNodeName());
			System.out.println("Root element :" + xmlData.get(2).getDocumentElement().getNodeName());
			NodeList nList = xmlData.get(2).getElementsByTagName("entry");
			
			System.out.println("----------------------------"+nList.getLength());
			for (int temp = 0; temp < nList.getLength(); temp++){
				Node nNode = nList.item(temp);
				System.out.println("\nCurrent Element :" + nNode.getNodeName());
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					System.out.println("key : " + eElement.getElementsByTagName("value").item(0).getNodeValue());
				//	System.out.println("value : " + eElement.getElementsByTagName("value").item(0).getTextContent());
					
				}
			}
			
			
		
		} catch (GeneralException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 finally{
			starter.close();
		}
		 
		
		
	}
}
