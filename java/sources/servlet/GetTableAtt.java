package servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import metabase.DataTreePath;
import metabase.NodeBase;
import metabase.TapNode;
import resources.RootClass;

/**
 * Servlet implementation class GetTableAtt
 * @version $Id$
 */
public class GetTableAtt extends RootServlet implements Servlet {
	private static final long serialVersionUID = 1L;

 
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		printAccess(request, true);
		response.setContentType("application/json; charset=UTF-8");

		String nodeKey = this.getParameter(request, "node");
		String table = this.getParameter(request, "table");
		String schema = this.getParameter(request, "schema");
		if( nodeKey == null ) {
			nodeKey = this.getParameter(request, "nodekey");
		}
		if( nodeKey == null || nodeKey.length() ==  0 ) {
			reportJsonError(request, response, "gettableatt: no node specified");
			return;
		}
		if( table == null || table.length() ==  0 ) {
			reportJsonError(request, response, "gettableatt: no table specified");
			return;
		}
		if( schema == null || schema.length() ==  0 ) {
			schema = "";
		}
		// TAP duplicates the schema name in the table name
		try {
			TapNode tn;
			if(  (tn = NodeBase.getNode(nodeKey)) == null ) {				
				reportJsonError(request, response, "Node " + nodeKey + " does not exist");
				return;
			}
			DataTreePath dataTreePath = new DataTreePath(schema, table, "");
			tn.buildJsonTableAttributes(dataTreePath);
			dumpJsonFile("/" + RootClass.WEB_NODEBASE_DIR + "/" + nodeKey + "/" + dataTreePath.getEncodedFileName() + "_att.json", response);
		} catch (Exception e) {
			reportJsonError(request, response, e);
			return;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
