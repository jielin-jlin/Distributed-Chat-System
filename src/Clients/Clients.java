package Clients;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.util.Vector;

import javax.swing.*;

public class Clients {

	private static Socket  cs;
	private static PrintWriter out;
	private static BufferedReader in;
	private static ServerSocket chatserversocket;
	private static Socket chatsocket;
	private static Socket csocket;
	private static BufferedReader chatin;
	private static PrintWriter chatout;
	private static BufferedReader inchat;
	private static PrintWriter outchat;
	private static JFrame frame;
	private static JScrollPane spane;
	private static JTextArea tarea;
	private static JTextField tfield;
	private static String uname;
	private static int portused;
	private static String talker;
	private static boolean chaton=false;
	private static boolean re=false;
	private static boolean se = false;
	
	public static void main(String[] args) throws IOException{
		uname = JOptionPane.showInputDialog("what is your username?");
		frame = new JFrame(uname);
		frame.setSize(410,300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);
		tarea = new JTextArea(5,30);
		spane = new JScrollPane(tarea);
		spane.setPreferredSize(new Dimension(380,200));
		spane.setVisible(true);
		tarea.setLineWrap(true);
		tarea.setWrapStyleWord(true);
		tarea.setEditable(false);
		tarea.setVisible(true);
		spane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		tfield = new JTextField();
		tfield.setVisible(true);
		tfield.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {
				String input = tfield.getText();
				if(input!=null)
				{
					if (input.equals("quit()"))
					{
						System.exit(0);
					}
					tarea.append("You> "+input+"\n");
					tarea.setCaretPosition(tarea.getDocument().getLength());
					tfield.setText("");
					try {
						output(input);
					} catch (UnknownHostException e1) {
						JOptionPane.showMessageDialog(null, "here");
						e1.printStackTrace();
					}
				}
			}
			
		});
		frame.setLayout(new BorderLayout());
		frame.add(tfield,BorderLayout.SOUTH);
		frame.add(spane,BorderLayout.NORTH);
		frame.pack();
		String hostname;
		String port;
		hostname = JOptionPane.showInputDialog("Please enter hostname: ");
		port = JOptionPane.showInputDialog("Please enter port number: ");
		int portn = Integer.parseInt(port);
		try
		{
			int portnum;
			int yesno;
			cs = new Socket(hostname,portn);
			out = new PrintWriter(cs.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
			output(uname);
			String inputline;
			if((inputline = in.readLine())!=null)
			{
				if(inputline.equals("username is in use"))
				{
					JOptionPane.showMessageDialog(null, inputline);
					System.exit(0);
				}
				int place = Integer.parseInt(inputline);
				place++;
				portused = portn+(128*(place));
				
			}
			while((inputline = in.readLine())!=null)
			{
				if(inputline.contains("requestchatwith"))
				{
					if(chaton==false)
					{
						String parts[] = inputline.split(" ");
						String origin = parts[1];
						String hostn = parts[2];
						String portnumber = parts[3];
						portnum = Integer.parseInt(portnumber);
						yesno=JOptionPane.showConfirmDialog(null, "requestchat from "+origin+" would you like to accept?");
						if(yesno==JOptionPane.YES_OPTION)
						{
							chaton = true;
							try
							{
								out.println("accept() "+origin+" "+uname);
								re=true;
								se=false;
								csocket = new Socket(hostn,portnum);
								chatout = new PrintWriter(csocket.getOutputStream(),true);
								chatin = new BufferedReader(new InputStreamReader(csocket.getInputStream()));
								talker=origin;
								new Thread(new chatthread2(csocket)).start();
							}
							catch(UnknownHostException e)
							{
								JOptionPane.showMessageDialog(null, "Connot connect to server,Unknown host");
								System.exit(0);
							}
							catch(IOException e)
							{
								JOptionPane.showMessageDialog(null, "Chat has ended");
								chaton = false;
								re = false;
								se = false;
								
							}
							
						}
						else
						{
							chaton = false;
							String reject = "reject()"+ " "+origin+" "+ uname;
							out.println(reject);
						}
					}
					else
					{
						String parts[] = inputline.split(" ");
						String origin = parts[1];
						String hostn = parts[2];
						String portnumber = parts[3];
						portnum = Integer.parseInt(portnumber);
						chaton = true;
						String reject = "reject()"+ " "+origin+" "+ uname;
						out.println(reject);
					}
					
				}
				else if(inputline.contains("reject()"))
				{
					chaton = false;
					se=false;
					re=false;
					chatserversocket.close();
				}
				else if(inputline.equals("clientisnotonline()"))
				{
					chaton = false;
					se=false;
					re=false;
					chatserversocket.close();
					chatsocket.close();
					tarea.append("the person you are trying to reach is offline\n");
					tarea.setCaretPosition(tarea.getDocument().getLength());
					tfield.setText("");
					
				}
				else
				{
					tarea.append("Server> "+inputline+"\n");
					tarea.setCaretPosition(tarea.getDocument().getLength());
					tfield.setText("");
				}
				
			}
		}
		catch(UnknownHostException e)
		{
			JOptionPane.showMessageDialog(null, "Connot connect to server,Unknown host");
			System.exit(0);
		}
		catch(IOException e)
		{
			JOptionPane.showMessageDialog(null, "Connot connect to server, IOException");
		}
	}
	public static void output(String inc) throws UnknownHostException
	{
		String[] parts = inc.split(" ");
		try
		{
			if(chaton)
			{
				if(re)
				{
					chatout.println(inc);
					if(inc.equals("endchat()"))
					{
						chaton=false;
						re=false;
						se=false;
					}
				}
				else
				{
					outchat.println(inc);
					if(inc.equals("endchat()"))
					{
						chaton=false;
						re=false;
						se=false;
					}
				}
			}
			else if(inc.contains("requestchatwith"))
			{
				InetAddress address = InetAddress.getLocalHost();
				String hostname = address.getHostName();
				chatserversocket = new ServerSocket(0);
				portused= chatserversocket.getLocalPort();
				inc = inc+" "+hostname+" "+portused;
				out.println(inc);
				chatserversocket.setSoTimeout(7000);
				chatsocket = chatserversocket.accept();
				talker = parts[1];
				re=false;
				se=true;
				chaton = true;
				inchat = new BufferedReader(new InputStreamReader(chatsocket.getInputStream()));
				outchat = new PrintWriter(chatsocket.getOutputStream(),true);
				new Thread(new chatthread(chatsocket)).start();
				
			}
			else if(inc.equals("signoff()"))
			{
				out.println(inc);
				frame.dispatchEvent(new WindowEvent(frame,WindowEvent.WINDOW_CLOSING));
			}
			else
				out.println(inc);
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
			tarea.append("requestchat failed\n");
			chaton=false;
			tarea.setCaretPosition(tarea.getDocument().getLength());
			tfield.setText("");
		}
		
	}
	static class chatthread implements Runnable
	{
		public chatthread(Socket chatsocket)
		{
		}
		@Override
		public void run() {
			String inin;
			try {
				while((inin=inchat.readLine())!=null)
				{
					if(inin.equals("endchat()"))
					{
						
						re=false;
						se=false;
						chaton=false;
						chatserversocket.close();
						break;
					}
					tarea.append(talker+"> "+inin+"\n");
					tarea.setCaretPosition(tarea.getDocument().getLength());
					tfield.setText("");
				}
			} catch (IOException e) {
				re=false;
				se=false;
				chaton=false;
			}
			re=false;
			se=false;
			chaton=false;
			
		}
		
	}
	static class chatthread2 implements Runnable
	{
		public chatthread2(Socket chatsocket)
		{
		}
		@Override
		public void run() {
			String inin;
			try {
				while((inin=chatin.readLine())!=null)
				{
					if(inin.equals("endchat()"))
					{
						
						re=false;
						se=false;
						chaton=false;
						chatserversocket.close();
						break;
					}
					tarea.append(talker+"> "+inin+"\n");
					tarea.setCaretPosition(tarea.getDocument().getLength());
					tfield.setText("");
				}
			} catch (IOException e) {
				re=false;
				se=false;
				chaton=false;
			}
			re=false;
			se=false;
			chaton=false;
			
		}
		
	}

}
