package Server;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.Vector;

import javax.swing.*;
public class Server {

	private static JFrame frame;
	private static JScrollPane spane;
	private static JTextArea tarea;
	private static JTextField tfield;
	private static Vector<String> names= new Vector<String>();
	private static Vector<PrintWriter> pw = new Vector<PrintWriter>();
	private static Vector<BufferedReader> br = new Vector<BufferedReader>();
	private static boolean found=false;
	
	public static void main(String[] args){
		frame = new JFrame("Server");
		frame.setSize(410,300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);
		tarea = new JTextArea(5,30);
		JScrollPane spane = new JScrollPane(tarea);
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
					tarea.append("Server> "+input+"\n");
					tarea.setCaretPosition(tarea.getDocument().getLength());
					tfield.setText("");
				}
			}
			
		});
		frame.setLayout(new BorderLayout());
		frame.add(tfield,BorderLayout.SOUTH);
		frame.add(spane,BorderLayout.NORTH);
		frame.pack();
		String serverport;
		serverport = JOptionPane.showInputDialog("Please enter the port number you wish to connect to: ");
		int port = Integer.parseInt(serverport);
		ServerSocket ss;
		Socket cs;
		try 
		{
			ss = new ServerSocket(port);
			while(true)
			{
				cs = ss.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
				PrintWriter out = new PrintWriter(cs.getOutputStream(),true);
				String inline;
				inline = in.readLine();
				if(names.contains(inline))
				{
					out.println("username is in use");
				}
				else
				{
					tarea.append(inline+" is now online\n");
					tarea.setCaretPosition(tarea.getDocument().getLength());
					tfield.setText("");
					new Thread(new MakeThread(cs,inline)).start();
				}
					
			}
			
		} 
		catch (IOException e) 
		{
			JOptionPane.showMessageDialog(null, "Cannot connect to Server");
			e.printStackTrace();
			System.exit(0);
		}
	}
	static class MakeThread implements Runnable
	{
		private Socket cs;
		private BufferedReader in;
		private PrintWriter out;
		private String username;

		public MakeThread(Socket cs,String username)
		{
			this.cs=cs;
			this.username=username;
		}

		@Override
		public void run() {
			try
			{
				in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
				out = new PrintWriter(cs.getOutputStream(),true);
				names.addElement(username);
				pw.addElement(out);
				br.addElement(in);
				int place = names.indexOf(username);
				out.println(place);
				String inputline;
				while((inputline = in.readLine())!=null)
				{
					if(inputline.contains("requestchatwith"))
						requestchatwith(inputline,username);
					else if(inputline.contains("reject()"))
					{
						String[] partn = inputline.split(" ");
						String origin = partn[1];
						String target = partn[2];
						for(int i=0;i<names.size();i++)
						{
							if(names.elementAt(i).equals(origin))
							{
								pw.elementAt(i).println("reject()");
								i = names.size();
							}
						}
					}
					else if (inputline.contains("accept()"))
					{
						String[] partn = inputline.split(" ");
						String origin = partn[1];
						String target = partn[2];
						for(int i=0;i<names.size();i++)
						{
							if(names.elementAt(i).equals(origin))
							{
								pw.elementAt(i).println("accept()");
								i = names.size();
							}
						}
					}
					else if(inputline.equals("signoff()"))
					{
						if(names.contains(username))
						{
							tarea.append(username+" is now offline\n");
							tarea.setCaretPosition(tarea.getDocument().getLength());
							tfield.setText("");
							int indexofname = names.indexOf(username);
							names.remove(username);
							pw.remove(indexofname);
							br.remove(indexofname);
							try {
								cs.close();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
						}
					}
					tarea.append(username+"> "+inputline+"\n");
					tarea.setCaretPosition(tarea.getDocument().getLength());
					tfield.setText("");
				}
			}
			catch(IOException e)
			{
				if(names.contains(username))
				{
					tarea.append(username+" is now offline\n");
					tarea.setCaretPosition(tarea.getDocument().getLength());
					tfield.setText("");
					int indexofname = names.indexOf(username);
					names.remove(username);
					pw.remove(indexofname);
					br.remove(indexofname);
					try {
						cs.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}
			}
			
			
		}
		
	}
	public static void requestchatwith(String input,String origin)
	{
		found=false;
		String[] parts = input.split(" ");
		if(parts.length == 4)
		{
			String target = parts[1];
			String hostname = parts[2];
			String portnumber = parts[3];
			String rcw = "requestchatwith"+" "+origin+" "+hostname+" "+portnumber;
			for(int i=0;i<names.size();i++)
			{
				if(names.elementAt(i).equals(target))
				{
					pw.elementAt(i).println(rcw);
					found=true;
					i =names.size();
				}
			}
			if(found==false)
			{
				for(int t=0;t<names.size();t++)
				{
					if(names.elementAt(t).equals(origin))
					{
						pw.elementAt(t).println("clientisnotonline()");
						t =names.size();
					}
				}
			}
			
		}
		else
		{
			tarea.append("request chat fail\n");
			tarea.setCaretPosition(tarea.getDocument().getLength());
			tfield.setText("");
		}
		
		
	}

}
