package ie.gmit.sw;

import java.io.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.*;
import javax.servlet.http.*;

public class ServiceHandler extends HttpServlet {
	// set private variables
	private static final long serialVersionUID = 1L;
	private String remoteHost = null;
	private static long jobNumber = 0;
	private Callable call;
	private StringService strS;
	private static Result result = new Result();
	private mypool mypool;
	//private BlockingQueue<Callable> queue = new LinkedBlockingQueue<Callable>();
	private static callqueue queue =new callqueue();
	public void init() throws ServletException {
		ServletContext ctx = getServletContext();
		remoteHost = ctx.getInitParameter("RMI_SERVER"); //Reads the value from the <context-param> in web.xml
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
	
		
		//Initialise some request varuables with the submitted form info. These are local to this method and thread safe...
		String algorithm = req.getParameter("cmbAlgorithm");
		String s = req.getParameter("txtS");
		String t = req.getParameter("txtT");
		String taskNumber = req.getParameter("frmTaskNumber");


		out.print("<html><head><title>Distributed Systems Assignment</title>");		
		out.print("</head>");		
		out.print("<body>");
		
		// creates instances of taskNumberCreator
		creatable taskNumb= new taskNumberCreator();
		// creates task number
		taskNumb.createNumber();
		//sets jobNumber to created task number
		jobNumber = taskNumb.getTaskNumber();
		
		// checks if task number is null
		if (taskNumber == null) {
			// if null set taskNumber
			taskNumber = new String("T" + jobNumber);
			// jobNumber++;
			// initializes new call using constructor 
			call = new Call(s, t, algorithm, taskNumber);

			// Blocking queue information
			// http://tutorials.jenkov.com/java-util-concurrent/blockingqueue.html
			
			// adds a call to linked blocking queue
			queue.addcalltoqueue(call);
			
			
		} else {
			// Check out-queue for finished job
		}
		// checks if is empty
		if(!queue.isQueueEmpty()){
			// if queue not empty create new pool
			mypool = new mypool();
			// compares strings
			mypool.compareStrings(queue, result);
		}
		 // http://tutorials.jenkov.com/java-util-concurrent/blockingqueue.html
		
		
		//try {
		//	strS = (StringService) Naming.lookup("rmi://localhost:1099/compareStrings");
		//} catch (NotBoundException e) {
			
		//}
			
		
		///////////////////////RMI test /////////////////////////////
		// try {
		// strS = (StringService)
		// Naming.lookup("rmi://localhost:1099/compareStrings");
		// } catch (NotBoundException e) {
		// }
		// Get simple message
		// String RMI_Message = strS.getMessage();
		// out.print("RMI test"+ RMI_Message);
		/////////////////////// RMI test End ///////////////////////////////////
		
		// Get simple message
		//String RMI_Message = strS.getMessage();
		
		//out.print("RMI test"+ RMI_Message);
		out.print("<H1>Processing request for Job#: " + taskNumber + "</H1>");
		out.print("<div id=\"r\"></div>");
		
		out.print("<font color=\"#993333\"><b>");
		out.print("RMI Server is located at " + remoteHost);
		out.print("<br>Algorithm: " + algorithm);		
		out.print("<br>String <i>s</i> : " + s);
		out.print("<br>String <i>t</i> : " + t);
		out.print("<br>This servlet should only be responsible for handling client request and returning responses. Everything else should be handled by different objects.");
		out.print("Note that any variables declared inside this doGet() method are thread safe. Anything defined at a class level is shared between HTTP requests.");				
		out.print("</b></font>");

		out.print("<P> Next Steps:");	
		out.print("<OL>");
		out.print("<LI>Generate a big random number to use a a job number, or just increment a static long variable declared at a class level, e.g. jobNumber.");	
		out.print("<LI>Create some type of an object from the request variables and jobNumber.");	
		out.print("<LI>Add the message request object to a LinkedList or BlockingQueue (the IN-queue)");			
		out.print("<LI>Return the jobNumber to the client web browser with a wait interval using <meta http-equiv=\"refresh\" content=\"10\">. The content=\"10\" will wait for 10s.");	
		out.print("<LI>Have some process check the LinkedList or BlockingQueue for message requests.");	
		out.print("<LI>Poll a message request from the front of the queue and make an RMI call to the String Comparison Service.");			
		out.print("<LI>Get the <i>Resultator</i> (a stub that is returned IMMEDIATELY by the remote method) and add it to a Map (the OUT-queue) using the jobNumber as the key and the <i>Resultator</i> as a value.");	
		out.print("<LI>Return the result of the string comparison to the client next time a request for the jobNumber is received and the <i>Resultator</i> returns true for the method <i>isComplete().</i>");	
		out.print("</OL>");	
		
		out.print("<form name=\"frmRequestDetails\">");
		out.print("<input name=\"cmbAlgorithm\" type=\"hidden\" value=\"" + algorithm + "\">");
		out.print("<input name=\"txtS\" type=\"hidden\" value=\"" + s + "\">");
		out.print("<input name=\"txtT\" type=\"hidden\" value=\"" + t + "\">");
		out.print("<input name=\"frmTaskNumber\" type=\"hidden\" value=\"" + taskNumber + "\">");
		out.print("</form>");
		// creates new blocking queue what equals the values of the old blocking queue
		BlockingQueue<Callable> q = queue.getQueue();
		// chacks if size is greater then 0
		if(q.size() > 0){
			// gets job number of each item
			for(Callable item : q){
				out.print("<LI>Job Number ==> " + item.getJobNumber() + "</LI>");
			}
		}
		else{
			out.print("<LI>Queue is empty now.</LI>");
		}
		out.print("</OL>");
		
		out.print("</body>");	
		out.print("</html>");	
		
		out.print("<script>");
		out.print("var wait=setTimeout(\"document.frmRequestDetails.submit();\", 10000);");
		out.print("</script>");
		// checks if result is empty and result if its available
		if(!result.isResultsEmpty() && result.isResultReady(taskNumber)){
			out.print("<h3>Request is here:</h3>");
			// gets the result 
			out.print("<p style=\"font-size: 36px; font-weight: bold\">" + result.takeResult(taskNumber).getResult() + "</p>");
		}
	}
		
				
	

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
 	}
}