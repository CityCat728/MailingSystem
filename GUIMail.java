import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JScrollPane;

public class GUIMail {

	final int SMTP_PORT=25;	//SMTP port number in order to connection
	private String smtp_server="stumail.nutn.edu.tw";	//Server address
	private String my_email_addr="";	//email address My self 
	
	
	final int POP_PORT=110;	//Port number of POP Connection
	BufferedReader pop_in=null;	//The data flow of reading
	PrintWriter pop_out=null;	//The data flow of writing
	Socket pop=null;	//Socket
	private String Mode="";
	
	
	private JFrame frame;
	private JTextField emailTF;
	private JTextField passwordTF;
	private JTextField subjectTF;
	public  JTextArea messageTA;
	private JTextArea recipientTA;
	
	//POP
	private JTextArea listTA;
	private JTextField numberTF;
	private JLabel noticeLabel;
	private JTextArea noticeTA;
	private String login="N";
	private JButton searchBtn;
	private JButton confirmBtn;
	private JButton deleteBtn;
	private JButton replyBtn;
	private JButton forwardBtn;
	private JTextArea textArea;
	private JButton exitBtn;
	private JLabel noticeLabel2;
	
	
	public void sendCommandAndResultCheck(Socket smtp,
			BufferedReader smtp_in,PrintWriter smtp_out,String command,int success_code) throws IOException
	{
		smtp_out.print(command+"\r\n");	//Send the command in mail
		smtp_out.flush();
		System.out.println("send> "+command);	//Display the content in deliver letter
		resultCheck(smtp,smtp_in,smtp_out,success_code);	//Check the result
	}
	
	
	public void resultCheck(Socket smtp,BufferedReader smtp_in,
			PrintWriter smtp_out,int success_code) throws IOException
	{
		String res=smtp_in.readLine();	//read the code of response
		System.out.println("recv> "+res);	//Display the content in sending
		//The code of response isn't match to expected one
		if(Integer.parseInt(res.substring(0,3))!=success_code)
		{
			smtp.close();
			throw new RuntimeException(res);
		}
	}
	
	//Send method
	//Continuing the SMTP connection
	public void send(String subject,String[] to,String[] msgs)
			throws IOException
			{
				//Connect
				Socket smtp=new Socket(smtp_server,SMTP_PORT);
				BufferedReader smtp_in=new BufferedReader(new InputStreamReader(smtp.getInputStream()));
				PrintWriter smtp_out=new PrintWriter(smtp.getOutputStream());
				
				resultCheck(smtp,smtp_in,smtp_out,220);
				
				//Send HELO
				String myname=InetAddress.getLocalHost().getHostName();
				sendCommandAndResultCheck(smtp,smtp_in,smtp_out,"HELO "
						+myname,250);
				//Send MAIL FROM
				sendCommandAndResultCheck(smtp,smtp_in,smtp_out,"MAIL FROM:"
						+my_email_addr,250);
				//Send Auth
				sendAuth(smtp,smtp_in,smtp_out);
				//Send RCPT TO
				for(int i=0;i<to.length;i++)
				{
					sendCommandAndResultCheck(smtp,smtp_in,smtp_out,"RCPT TO:"+to[i],250);
				}
				

				//Send letter in the DATA command way
				sendCommandAndResultCheck(smtp,smtp_in,smtp_out,"DATA",354);
				
				smtp_out.print("FROM:"+my_email_addr+"\r\n");
				smtp_out.print("Subject:"+subject+"\r\n");
				System.out.println("send> "+"Subject:"+subject);
				smtp_out.print("\r\n");
				//Send the content in the letter
				for(int i=0;i<msgs.length-1;++i)
				{
					smtp_out.print(msgs[i]+"\r\n");
					System.out.println("send> "+msgs[i]);
				}
				sendCommandAndResultCheck(smtp,smtp_in,smtp_out,"\r\n.",250);
				//Send QUIT
				sendCommandAndResultCheck(smtp,smtp_in,smtp_out,"QUIT",221);
				//Close the connection
				smtp.close();
			}
	
	//Be responsible for dealing with parameters
	public void mainproc()
	{
		
		String usage="java Mail [-s subject] to-addr ...";
		String subject="";
		Vector<String> to_list=new Vector<String>();

		//The letter must have at least the recipient
		if(subjectTF.getText().equals(""))
			subject="(No Subject)";
		else
			subject=subjectTF.getText();

		
		
//		int i=0;
		String[] tokens = recipientTA.getText().split("\n");
		for(String token:tokens)
		{
			to_list.addElement(token);
//			System.out.println(i+"."+token);
//			i++;
		}
		
		
		
		//When there is at least 1 recipient or more then 1
		if(to_list.size()>0)
		{
			try {
				String[] to=new String [to_list.size()];
				to_list.copyInto(to);
				//setAddress();
				String[] msgs=setMsgs();
				send(subject,to,msgs);
			}catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("usage: "+usage);
			
		}
	}
	
	
	//Authentication
	void sendAuth(Socket smtp,BufferedReader smtp_in,PrintWriter smtp_out)  throws IOException
	{
		if (smtp == null) return;
		
		//Sending the verification command to server
		sendCommandAndResultCheck(smtp,smtp_in,smtp_out,"AUTH LOGIN",334);
		
			Base64 b = new Base64();
			String typeId=emailTF.getText()+"@stumail.nutn.edu.tw";
			String typeP=passwordTF.getText();
			String id = b.encode((typeId).getBytes());
			String p = b.encode((typeP).getBytes());
			auth(smtp_in,smtp_out,id,334);
			auth(smtp_in,smtp_out,p,235);

	}
	
	

	void auth(BufferedReader smtp_in,PrintWriter smtp_out,String s,int success_code)  throws IOException
	{
		
			   	smtp_out.write(s);
			   	smtp_out.write("\r\n");
			   	smtp_out.flush();
				
			   	String res=smtp_in.readLine();
				System.out.println("recv> "+res);
				/*if(Integer.parseInt(res.substring(0,3))!=334)
				{
					System.out.println("執行驗證發生錯誤");
					throw new RuntimeException(res);
				}*/
	}
	

	//Message that means the content of letter
	public String[] setMsgs()
	{

		Vector<String> msgs_list=new Vector<String>();
		String[] msgs=null;
	
		try {

			String[] tokens = messageTA.getText().split("\n");
			
			for (String token:tokens)
			{
				msgs_list.addElement(token);
//				System.out.println(token);
			}
		
			if(!msgs_list.get(msgs_list.size()-1).equals("."))
			msgs_list.addElement("\n.");
			
//			System.out.println("Size:"+msgs_list.size());
//			System.out.println(msgs_list);
			
			msgs=new String[msgs_list.size()];
			msgs_list.copyInto(msgs);
			
	}catch(Exception e)
	{
		e.printStackTrace();
		System.exit(1);
	}
		
		return(msgs);
	}
	
	
	//POP class
	
	//Transaction method
		public void transaction(int method) throws IOException
		{

				//QUIT
//				if(buf.equalsIgnoreCase("Q"))
//					cont=false;
				//LIST
				if(method==1)
					getLines("LIST");
				//RETR
				else if(method==2)
				{
					getLines("RETR "+numberTF.getText());
				}
				//DELE
				else if(method==3)
				{
					getSingleLine("DELE "+numberTF.getText());
				}
		}
	
	
	
		//getLines Method
		public void  getLines(String command) throws IOException
		{
			boolean cont=true;
			String buf=null;
			//Deliver Command
			pop_out.print(command+"\r\n");
			pop_out.flush();
			
			String res=pop_in.readLine();//Read the response
			System.out.println(res);
			
			//If the return response is + OK ...
			if(!("+OK".equals(res.substring(0,3))))
			{
				noticeLabel.setText("Wrong Entry!!Please check again.");
				numberTF.setEditable(false);
				confirmBtn.setEnabled(false);
				//pop.close();//close the connection
				throw new RuntimeException(res);
			}
			
			String[] tokens=res.split(" ");
			if(tokens.length>=3)
			{
				if(tokens[0].equals("+OK")&&tokens[2].equals("octects."))
				{
					replyBtn.setEnabled(true);
					forwardBtn.setEnabled(true);
				}
			}
			
			
			
			
			if(res.equals("-ERR Message does not exist."))
			{
				noticeLabel.setText("Wrong Entry!!Please check again.");
			}
			
			while(cont)//Read multiple lines
			{
				buf=pop_in.readLine();//Read only one line
				
				if(Mode.equals("L"))
				{
					if(!buf.equals("."))
						listTA.append(buf+"\n");
					System.out.println(buf);
				}
				if(Mode.equals("S"))
					textArea.append(buf+"\n");
				//Use a period as the end of a sentence
				if(".".equals(buf))
					cont=false;
			}
		}
	
	
		
		//getSingleLine Method
		public void getSingleLine(String command) throws IOException
		{
			pop_out.print(command+"\r\n");
			pop_out.flush();
			System.out.println(command);
			String res=pop_in.readLine();//Read response
			System.out.println(res);
			String[] tokens=res.split(" ");

			if(res.equals("-ERR Invalid username or password."))
			{
				noticeTA.setText("Wrong Entry!!\nPlease check again.");
				//login="N";
			}
			if(tokens[0].equals("+OK")&&tokens[1].equals(my_email_addr))
			{
				noticeTA.setText("Sign in successfully!");
				login="Y";
			}
			
			if(res.equals("-ERR Message does not exist."))
			{
				noticeLabel.setText("The letter doesn't exist!");
			}
			
			if(tokens[0].equals("+OK")&&tokens[1].equals("Message")&&tokens[3].equals("deleted."))
			{
				noticeLabel.setText("Delete successfully!");
				getLines("LIST");
			}
			
			//If return response is + OK ...
			if(!("+OK".equals(res.substring(0,3))))
			{
				//pop.close();
				//throw new RuntimeException(res);
			}
			

		}
	
	
	
		
		//Authorization Method
		//Setting and Authorization of TCP connection
		public void authorization() throws IOException
		{

			String pop_server=null;
			String username=null;
			String password=null;
			
			//Set up the address of POP Server
			pop_server=smtp_server;

			
			//Connect to the server
			pop=new Socket(pop_server,POP_PORT);
			pop_in=new BufferedReader(new InputStreamReader(pop.getInputStream()));
			pop_out=new PrintWriter(pop.getOutputStream());
			//Get the messages
			String res=pop_in.readLine();	//Read response
			System.out.println(res);
			//If return response is + OK ...
			if(!("+OK").equals(res.substring(0,3)))
			{
				pop.close();	//close connection
				throw new RuntimeException(res);
			}	

			
			//Get certification information
			username=emailTF.getText()+"@stumail.nutn.edu.tw";
			password=passwordTF.getText();


			//Make use of Command USER and PASS to confirm
			getSingleLine("USER "+username);
			getSingleLine("PASS "+password);
		}
	
	
	
	
		//update Method
		public void update() throws IOException
		{
			//QUIT
			getSingleLine("QUIT");
			pop.close();	//close the connection
		}
	
	
		
		//mainproc Method
//		public void mainprocPOP() throws IOException
//		{
//				authorization();
//				transaction();
//				update();
//				
//			else
//			{
//				System.out.println("usage:java Pop");
//			}
//		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUIMail window = new GUIMail();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUIMail() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("My OUTLOOK");
		frame.setBounds(100, 100, 650, 650);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		emailTF = new JTextField();
		emailTF.setFont(new Font("新細明體", Font.PLAIN, 14));
		emailTF.setText("s10659003");
		emailTF.setBounds(81, 10, 87, 21);
		frame.getContentPane().add(emailTF);
		emailTF.setColumns(10);
		
		JLabel lblEmail = new JLabel("Accout");
		lblEmail.setFont(new Font("新細明體", Font.PLAIN, 16));
		lblEmail.setBounds(10, 12, 61, 15);
		frame.getContentPane().add(lblEmail);
		
		passwordTF = new JTextField();
		passwordTF.setFont(new Font("新細明體", Font.PLAIN, 14));
		passwordTF.setText("asd7864136");
		passwordTF.setBounds(81, 41, 217, 21);
		frame.getContentPane().add(passwordTF);
		passwordTF.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setFont(new Font("新細明體", Font.PLAIN, 16));
		lblPassword.setBounds(10, 44, 61, 15);
		frame.getContentPane().add(lblPassword);
		
		JLabel lblReceip = new JLabel("Recipient : ");
		lblReceip.setFont(new Font("新細明體", Font.PLAIN, 18));
		lblReceip.setBounds(10, 129, 87, 23);
		frame.getContentPane().add(lblReceip);
		
		JLabel lblSubject = new JLabel("Subject    :");
		lblSubject.setFont(new Font("新細明體", Font.PLAIN, 18));
		lblSubject.setBounds(10, 102, 76, 17);
		frame.getContentPane().add(lblSubject);
		
		subjectTF = new JTextField();
		subjectTF.setBounds(91, 102, 146, 21);
		frame.getContentPane().add(subjectTF);
		subjectTF.setColumns(10);
		//When the letter is ready to send out
		JButton sendBtn = new JButton("Send");
		sendBtn.setEnabled(false);
		sendBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				my_email_addr=emailTF.getText()+"@stumail.nutn.edu.tw";
				if(!recipientTA.getText().equals(""))
					mainproc();
				noticeLabel2.setText("Send successfully!");
				subjectTF.setText("");
				messageTA.setText("");
				recipientTA.setText("");
			}
		});
		sendBtn.setFont(new Font("新細明體", Font.PLAIN, 18));
		sendBtn.setBounds(156, 581, 87, 23);
		frame.getContentPane().add(sendBtn);
		
		JLabel lblNewLabel = new JLabel("Mailing Area");
		lblNewLabel.setFont(new Font("新細明體", Font.BOLD, 20));
		lblNewLabel.setBounds(10, 71, 103, 21);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblReceiveArea = new JLabel("Receiving Area");
		lblReceiveArea.setFont(new Font("新細明體", Font.BOLD, 20));
		lblReceiveArea.setBounds(319, 71, 120, 21);
		frame.getContentPane().add(lblReceiveArea);
		
		JLabel lblNewLabel_1 = new JLabel("@stumail.nutn.edu.tw");
		lblNewLabel_1.setFont(new Font("新細明體", Font.PLAIN, 16));
		lblNewLabel_1.setBounds(167, 12, 142, 19);
		frame.getContentPane().add(lblNewLabel_1);
		//Before the execution user would like to do,sign in first
		JButton signBtn = new JButton("Sign in");
		signBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				my_email_addr=emailTF.getText()+"@stumail.nutn.edu.tw";
							
				try {			
					authorization();
					if(login.equals("Y"))
					{
						Mode="L";
						emailTF.setEditable(false);
						passwordTF.setEditable(false);
						signBtn.setEnabled(false);
						searchBtn.setEnabled(true);
						deleteBtn.setEnabled(true);
						//replyBtn.setEnabled(true);
						//forwardBtn.setEnabled(true);
						sendBtn.setEnabled(true);
						getLines("LIST");
						frame.setTitle("My OUTLOOK "+emailTF.getText());
					}
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		signBtn.setFont(new Font("新細明體", Font.PLAIN, 18));
		signBtn.setBounds(319, 24, 87, 23);
		frame.getContentPane().add(signBtn);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 154, 299, 96);
		frame.getContentPane().add(scrollPane);
		
		recipientTA = new JTextArea();
		scrollPane.setViewportView(recipientTA);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 285, 299, 286);
		frame.getContentPane().add(scrollPane_1);
		
		messageTA = new JTextArea();
		scrollPane_1.setViewportView(messageTA);
		
		JLabel lblContent = new JLabel("Content   : ");
		lblContent.setFont(new Font("新細明體", Font.PLAIN, 18));
		lblContent.setBounds(10, 260, 87, 21);
		frame.getContentPane().add(lblContent);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(319, 447, 120, 124);
		frame.getContentPane().add(scrollPane_2);
		
		listTA = new JTextArea();
		scrollPane_2.setViewportView(listTA);
		
		JScrollPane scrollPane_3 = new JScrollPane();
		scrollPane_3.setBounds(319, 102, 305, 327);
		frame.getContentPane().add(scrollPane_3);
		
		textArea = new JTextArea();
		scrollPane_3.setViewportView(textArea);
		
		numberTF = new JTextField();
		numberTF.setEditable(false);
		numberTF.setBounds(563, 515, 61, 23);
		frame.getContentPane().add(numberTF);
		numberTF.setColumns(10);
		
		confirmBtn = new JButton("Confirm");
		confirmBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(Mode.equals("S"))
				{
					try {
						authorization();
						transaction(2);
						noticeLabel.setText("Search successfully!");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				
				else if(Mode.equals("D"))
				{
					try {
						authorization();
						transaction(3);
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}
				
				
				numberTF.setEditable(false);
				confirmBtn.setEnabled(false);
					

			}
		});
		confirmBtn.setEnabled(false);
		confirmBtn.setBounds(449, 548, 175, 23);
		frame.getContentPane().add(confirmBtn);
		
		searchBtn = new JButton("Search");
		searchBtn.setEnabled(false);
		searchBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Mode="S";
				noticeLabel.setText("Enter the letter number that you would like to search");
				numberTF.setEditable(true);
				confirmBtn.setEnabled(true);
				numberTF.setText("");
			}
		});
		searchBtn.setFont(new Font("新細明體", Font.PLAIN, 16));
		searchBtn.setBounds(449, 447, 87, 23);
		frame.getContentPane().add(searchBtn);
		
		deleteBtn = new JButton("Delete");
		deleteBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Mode="D";
				noticeLabel.setText("Enter the letter number that you would like to delete");
				numberTF.setEditable(true);
				confirmBtn.setEnabled(true);
				numberTF.setText("");
			}
		});
		deleteBtn.setEnabled(false);
		deleteBtn.setFont(new Font("新細明體", Font.PLAIN, 16));
		deleteBtn.setBounds(537, 447, 87, 23);
		frame.getContentPane().add(deleteBtn);
		
		replyBtn = new JButton("Reply");
		replyBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
			}
		});
		replyBtn.setEnabled(false);
		replyBtn.setFont(new Font("新細明體", Font.PLAIN, 16));
		replyBtn.setBounds(449, 477, 87, 23);
		frame.getContentPane().add(replyBtn);
		
		forwardBtn = new JButton("Forward");
		forwardBtn.setEnabled(false);
		forwardBtn.setFont(new Font("新細明體", Font.PLAIN, 16));
		forwardBtn.setBounds(537, 477, 87, 23);
		frame.getContentPane().add(forwardBtn);
		
		JLabel lblNewLabel_2 = new JLabel("Letter Number");
		lblNewLabel_2.setFont(new Font("新細明體", Font.PLAIN, 16));
		lblNewLabel_2.setBounds(460, 518, 91, 21);
		frame.getContentPane().add(lblNewLabel_2);
		
		noticeLabel = new JLabel("");
		noticeLabel.setBounds(253, 575, 305, 29);
		frame.getContentPane().add(noticeLabel);
		
		noticeTA = new JTextArea();
		noticeTA.setEditable(false);
		noticeTA.setBounds(416, 8, 208, 54);
		frame.getContentPane().add(noticeTA);
		
		exitBtn = new JButton("Exit");
		exitBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(login.equals("N"))
					System.exit(0);
				try {
					update();
					System.exit(0);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		exitBtn.setFont(new Font("新細明體", Font.PLAIN, 18));
		exitBtn.setBounds(557, 578, 67, 23);
		frame.getContentPane().add(exitBtn);
		
		noticeLabel2 = new JLabel("");
		noticeLabel2.setBounds(10, 581, 136, 23);
		frame.getContentPane().add(noticeLabel2);
	}
}
