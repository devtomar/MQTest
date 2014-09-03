/**
 *     Copyright (C) 2008, International Business Machines Corporation.   
 *                       All Rights Reserved.            
 *                                                                      
 *                    by  Maurizio Delle Fratte          
 *                                                                      
 *   This program is provided on an \"AS IS\" basis, no warranty is     
 *   expressed or implied, including merchantability or fitness for     
 *   a particular purpose.  IBM retains all rights to this program      
 *   and does not transfer any rights for distribution or replication   
 *   of this program except for the following:                          
 *                                                                      
 *     1. Backup/Archive copies taken as a normal course of             
 *        system maintenance.                                           
 *     2. Copying the program to a similar customer-owned system within 
 *        the same enterprise.                                          
 *                                                                      
 *   The customer agrees to restrict access to this program as they     
 *   would their own proprietary code, and to notify IBM should         
 *   unauthorized distribution occur.                                   
 *                                                                      
 *    This program is distributed on an \"as is\" basis,                
 *         no warranty is expressed or implied.
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import javax.swing.*;
import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * Main class of MQ Server Ping monitoring
 * @author maurizio.dellefratte@it.ibm.com
 */
public class MQPing 
	extends JFrame
    implements ActionListener
{

	/**
	 * Constructor method.
	 * @param nfile is optional configuration file
	 */
	public MQPing(String nfile)
    {
        copyRight = 
        " Copyright (C) 2008, International Business Machines Corporation.    \n"+
        "                                    All Rights Reserved.             \n"+
        "                                                                     \n"+
        "                                 by  Maurizio Delle Fratte           \n"+
        "                                                                     \n"+
        " This program is provided on an \"AS IS\" basis, no warranty is      \n"+
        " expressed or implied, including merchantability or fitness for      \n"+
        " a particular purpose.  IBM retains all rights to this program       \n"+
        " and does not transfer any rights for distribution or replication    \n"+
        " of this program except for the following:                           \n"+
        "                                                                     \n"+
        "   1. Backup/Archive copies taken as a normal course of              \n"+
        "      system maintenance.                                            \n"+
        "   2. Copying the program to a similar customer-owned system within  \n"+
        "      the same enterprise.                                           \n"+
        "                                                                     \n"+
        " The customer agrees to restrict access to this program as they      \n"+
        " would their own proprietary code, and to notify IBM should          \n"+
        " unauthorized distribution occur.                                    \n"+
        "                                                                     \n"+
        "  This program is distributed on an \"as is\" basis,                 \n"+
        "       no warranty is expressed or implied.                          ";
        token = ",";
        comments = "#";
        panelTitle = ">";
        snail= "@";
        qmgr = "";
        ipAddress = "";
        mqmgr = null;
        vecInd = null;
        el_per_panel = null;
        mqPort = 0;
        num = 0;
        time = 0;
        textMode = 0;
        textSize = 12;
        gotask = false;
        contentP = new JPanel(new BorderLayout());
        response = new JPanel();
        title = new JPanel();
        butpan = new JPanel(new FlowLayout(1));
        messages = new JTextField(21);
        in = MQPing.class.getResourceAsStream("mq_list.txt");
        image = new ImageIcon(ClassLoader.getSystemResource("Icons/ibm_blu.gif"));
        iconQuestion = new ImageIcon(ClassLoader.getSystemResource("Icons/question.gif"));
        createWindow();
        loadAddress(nfile);
    }

	/**
	 * Method createWindow.
	 * It creates main windows of application
	 */
	public void createWindow()
    {
        setTitle("MQ Server Ping");
        setIconImage(image.getImage());
        setJMenuBar(createMenuBar());
        messages.setText("");
        messages.setEditable(false);
        stat = new JButton("Start");
        stat.addActionListener(this);
        butpan.add(stat);
        stat = new JButton("Exit");
        stat.addActionListener(this);
        butpan.add(stat);
        contentP.add(new JScrollPane(response), "Center");
        contentP.add(messages, "South");
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(contentP, "Center");
        getContentPane().add(butpan, "South");
        setSize(310, 700);
        setLocation(200, 100);
        setResizable(false);
        setDefaultCloseOperation(0);
        toFront();
        setVisible(true);
    }

	/**
	 * Method createMenuBar.
	 * It creates main menu bar
	 * @return barMenu
	 */
    private JMenuBar createMenuBar()
    {
        JMenuBar barMenu = new JMenuBar();
        barMenu.add(MenuOptions());
        barMenu.add(subMenuAbout());
        return barMenu;
    }

    /**
     * Method MenuOptions.
     * It creates button Options in menu bar
     * @return subMenu
     */
    private JMenu MenuOptions()
    {
        JMenu subMenu = new JMenu("Options");
        subMenu.add(subMenuLoad());
        subMenu.addSeparator();
        subMenu.add(subMenuTask());
        subMenu.addSeparator();
        subMenu.add(subMenuShow());
        return subMenu;
    }

    /**
     * MethodsubMenuLoad.
     * It creates a button Load in main menu bar
     * @return subMenu
     */
    private JMenuItem subMenuLoad()
    {
        final JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File("."));
        fc.setFileSelectionMode(0);
        fc.setAcceptAllFileFilterUsed(false);
        fc.addChoosableFileFilter(new ImageFilter());
        JMenuItem subMenu = new JMenuItem("Load", 76);
        ActionListener aclis = new ActionListener() {

        	/**
        	 * Method actionPerformed.
        	 * It defines the action of file choosing
        	 */
            public void actionPerformed(ActionEvent e)
            {
                if(fc.showOpenDialog(MQPing.this) != 0)
                    return;
                File file = fc.getSelectedFile();
                String fileName = file.getParent() + "/" + file.getName();
                if(!loadAddress(fileName))
                    messages.setText("No Data Available");
                else
                    messages.setText("Data Loaded");
            }

        };
        subMenu.addActionListener(aclis);
        return subMenu;
    }

    /**
     * Method subMenuTask.
     * It defines panel where you modify the frequency of ping
     * @return subMenu
     */
    private JMenuItem subMenuTask()
    {
        JMenuItem subMenu = new JMenuItem("Run continuously", 82);
        JPanel settingPanel = new JPanel(new FlowLayout(1));
        Box mainSettingPanel = Box.createVerticalBox();
        JLabel myText = new JLabel();
        final JTextField infoText = new JTextField(5);
        JLabel myText2 = new JLabel();
        final JFrame win = new JFrame();
        myText.setFont(new Font("Courier", 1, 12));
        myText.setText("Insert delay:");
        myText2.setText("(1 unit -> 30 seconds)");
        infoText.setFont(new Font("Courier", 0, 12));
        infoText.setText((new StringBuffer(String.valueOf(time / 30000))).toString());
        settingPanel.add(myText);
        settingPanel.add(infoText);
        mainSettingPanel.add(settingPanel);
        mainSettingPanel.add(myText2);
        win.getContentPane().add(mainSettingPanel);
        win.setLocation(100, 150);
        win.setSize(300, 100);
        win.setResizable(false);
        win.setDefaultCloseOperation(0);
        ActionListener actxt = new ActionListener() {

        	/**
        	 * Method actionPerformed.
        	 * It makes you insert a new delay time
        	 */
            public void actionPerformed(ActionEvent e)
            {
                int oldtime = time;
                try
                {
                    time = 30000 * Integer.parseInt(infoText.getText());
                }
                catch(NumberFormatException nfe)
                {
                    time = oldtime;
                    infoText.setText((new StringBuffer(String.valueOf(time / 30000))).toString());
                }
                if(time == 0)
                {
                    gotask = false;
                } else
                {
                    gotask = false;
                    gotask = true;
                    loopTask();
                }
                win.dispose();
            }

        };
        infoText.addActionListener(actxt);
        ActionListener aclis = new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                JMenuItem source = (JMenuItem)e.getSource();
                if(source.getText().equals("Run continuously"))
                    win.setVisible(true);
            }

        };
        subMenu.addActionListener(aclis);
        return subMenu;
    }

    /**
     * Method subMenuShow.
     * It creates a panel to show you the configuration
     * @return subMenu
     */
    private JMenuItem subMenuShow()
    {
        JMenuItem subMenu = new JMenuItem("Show", 83);
        final JPanel settingPanel = new JPanel(new FlowLayout(1));
        final JTextArea infoSetting = new JTextArea(20, 30);
        infoSetting.setFont(new Font("Courier", 0, 12));
        infoSetting.setEditable(false);
        ActionListener aclis = new ActionListener() {

        	/**
        	 * Method actionPerformed.
        	 * It reads and shows the configuration
        	 */
            public void actionPerformed(ActionEvent e)
            {
                if(num != 0)
                {
                    for(int i = 0; i < num; i++)
                        infoSetting.append((String)mqmgr.get(i) + "\n");

                    messages.setText("Data Loaded");
                } else
                {
                    infoSetting.setText("");
                    messages.setText("No Data Available");
                }
                settingPanel.add(new JScrollPane(infoSetting));
                JMenuItem source = (JMenuItem)e.getSource();
                if(source.getText().equals("Show"))
                    JOptionPane.showMessageDialog(MQPing.this, settingPanel, "Show Current Settings", -1);
            }

        };
        subMenu.addActionListener(aclis);
        return subMenu;
    }

    /**
     * Method subMenuAbout.
     * It shows the disclaimer panel
     * @return subMenu
     */
    private JMenuItem subMenuAbout()
    {
        JTextArea infoSetting = new JTextArea(10, 30);
        final JFrame win = new JFrame();
        infoSetting.setText(copyRight);
        infoSetting.setFont(new Font("Arial", 0, 14));
        infoSetting.setEditable(false);
        infoSetting.setForeground(Color.BLACK);
        infoSetting.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(""), BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        JMenuItem subMenu = new JMenuItem("About");
        win.getContentPane().add(infoSetting);
        win.setLocation(100, 150);
        win.setSize(450, 420);
        win.setResizable(false);
        ActionListener aclis = new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                JMenuItem source = (JMenuItem)e.getSource();
                if(source.getText().equals("About"))
                    win.setVisible(true);
            }

        };
        subMenu.addActionListener(aclis);
        return subMenu;
    }

    /**
     * Method loadAddress.
     * It reads the configuration from input file
     * @param name
     * @return out
     */
    private boolean loadAddress(String name)
    {
        boolean out = false;
        BufferedReader br = null;
        String s = "";
        num = 0;
        try
        {
            if(name.equals(""))
                br = new BufferedReader(new InputStreamReader(in));
            else
                br = new BufferedReader(new FileReader(name));
            mqmgr = new Vector();
            while((s = br.readLine()) != null) 
            {
                s = ckspace(s);
                if(excludedLines(s) && !s.startsWith(comments))
                {
					if(s.startsWith(snail))
					{
						int old=time;
						try 
						{ 
							time = 30000 * Integer.parseInt(s.substring(1, s.length())); 
						} 
						catch ( Exception e)	
						{ 
							time=old; 
						}
					}
                    if(num == 0)
                    {
                        if(!s.startsWith(panelTitle))
                            mqmgr.add(panelTitle + "no title");
                        else
                            mqmgr.add(s);
                    } else
                    {
                        mqmgr.add(s);
                    }
                    num++;
                }
            }
            br.close();
        }
        catch(Exception e)
        {
            messages.setText("Data not available");
            validate();
            mqmgr = null;
            num = 0;
        }
        response.removeAll();
        if(num > 0)
        {
            el_per_panel = new Vector();
            int j = 0;
            for(int i = 1; i < num; i++)
                if(((String)mqmgr.get(i)).startsWith(panelTitle))
                {
                    el_per_panel.add((new StringBuffer(String.valueOf(j))).toString());
                    j = 0;
                } else
                {
                    j++;
                }

            int numRows = mqmgr.size();
            response.setLayout(new GridLayout(1, 1, 0, 0));
            el_per_panel.add((new StringBuffer(String.valueOf(j))).toString());
            out = true;
            response.add(dispList(num));
            setSize(310, 145 + (numRows >= 20 ? '\u01E0' : 24 * numRows));
            messages.setText("Data loaded");
        } else
        {
            messages.setText("Data Not Available");
            out = false;
        }
        validate();
        return out;
    }

    /**
     * Method getServer.
     * It reads and validates data of the configuration file
     * @param j
     * @return out
     */
    private boolean getServer(int j)
    {
        boolean out;
        try
        {
            StringTokenizer st1 = new StringTokenizer((String)mqmgr.get(j), token);
            String par[] = new String[3];
            for(int y = 0; st1.hasMoreTokens(); y++)
                par[y] = st1.nextToken();

            qmgr = par[0];
            ipAddress = par[1];
            mqPort = Integer.parseInt(par[2]);
            out = true;
        }
        catch(Exception e)
        {
            qmgr = "INCORRECT !";
            messages.setText("Uncorrect Data");
            validate();
            out = false;
        }
        return out;
    }

    /**
     * Method dispList.
     * It composes the list of servers in the main panel
     * @param nu
     * @return mainPanel
     */
    private Box dispList(int nu)
    {
        vecInd = new Vector();
        Box mainPanel = Box.createVerticalBox();
        int i = 0;
        int pcount = 0;
        for(; i < nu; i++)
            if(((String)mqmgr.get(i)).startsWith(panelTitle))
            {
                if(pcount != 0)
                    mainPanel.add(locPanel);
                locPanel = new JPanel();
                locPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(((String)mqmgr.get(i)).substring(1, ((String)mqmgr.get(i)).length())), BorderFactory.createEmptyBorder(0, 0, 0, 0)));
                locPanel.setLayout(new GridLayout(Integer.parseInt((String)el_per_panel.get(pcount)), 0, 0, 0));
                pcount++;
            } else
            {
                locPanel.add(dispLine(i));
            }

        mainPanel.add(locPanel);
        return mainPanel;
    }

    /**
     * Method dispLine.
     * It composes each line of the list of servers  
     * @param lineNumber
     * @return myPanel
     */
    private JPanel dispLine(int lineNumber)
    {
        getServer(lineNumber);
        pan1 = new JPanel();
        pan1.setLayout(new BorderLayout());
        pan2 = new JPanel();
        pan2.setLayout(new BorderLayout());
        textQMGR = new JTextField(13);
        textQMGR.setEditable(false);
        textQMGR.setText(qmgr);
        textQMGR.setFont(new Font("Courier", textMode, textSize));
        textQMGR.setBorder(BorderFactory.createEmptyBorder());
        pan1.add(textQMGR, "West");
        textADDR = new JTextField(15);
        textADDR.setEditable(false);
        textADDR.setText(ipAddress);
        textADDR.setFont(new Font("Courier", textMode, textSize));
        textADDR.setBorder(BorderFactory.createEmptyBorder());
        pan1.add(textADDR, "Center");
        textPORT = new JTextField(6);
        textPORT.setEditable(false);
        textPORT.setText((new StringBuffer(String.valueOf(mqPort))).toString());
        textPORT.setFont(new Font("Courier", textMode, textSize));
        textPORT.setBorder(BorderFactory.createEmptyBorder());
        pan1.add(textPORT, "East");
        labelStatus = new JLabel();
        labelStatus.setIcon(iconQuestion);
        pan2.add(pan1, "West");
        pan2.add(labelStatus, "East");
        JPanel myPanel = new JPanel();
        myPanel.setLayout(new GridLayout(1, 1, 0, 0));
        myPanel.add(pan2);
        vecInd.add(labelStatus);
        return myPanel;
    }

    /**
     * Method ping.
     * It runs a ping action for each server in the list
     * and it updates the main panel.
     */
    private void ping()
    {
        try
        {
            flag = vecInd.size();
        }
        catch(NullPointerException npe)
        {
            return;
        }
        int count = 0;
        for(int z = 0; z < mqmgr.size(); z++)
            if(!((String)mqmgr.get(z)).startsWith(panelTitle))
                count++;

        if(count == 0)
            messages.setText("No Data Available");
        else
            try
            {
                int i = 0;
                int q = 0;
                for(; i < mqmgr.size(); i++)
                    if(!((String)mqmgr.get(i)).startsWith(panelTitle))
                    {
                        getServer(i);
                        myPing = new PingThread(i, (JLabel)vecInd.get(q), ipAddress, mqPort);
                        myPing.start();
                        q++;
                    }

                myPing.join();
                Calendar cal = new GregorianCalendar();
                int hours = cal.get(11);
                int minutes = cal.get(12);
                messages.setText("Scanning ended - last refresh:" + hours + ":" + minutes);
            }
            catch(Exception e)
            {
                messages.setText("No Data Available");
            }
    }

    /**
     * Method actionPerformed.
     * It verifies the pressure of the buttons 'Start' and 'Exit'.
     */
    public void actionPerformed(ActionEvent e)
    {
        if(e.getActionCommand().equals("Exit"))
        {
            dispose();
            System.exit(0);
        } else
        if(e.getActionCommand().equals("Start"))
            try
            {
                setCursor(new Cursor(3));
                ping();
                setCursor(new Cursor(0));
            }
            catch(Exception en)
            {
                en.printStackTrace();
            }
    }

    /**
     * Method excludedLines.
     * It removes lines of comment while reading the configuration file
     * @param lineTest
     * @return <line of the server>
     */
    private boolean excludedLines(String lineTest)
    {
        if(lineTest.startsWith(comments))
            return false;
        return !lineTest.equals("");
    }

    /**
     * Method ckspace.
     * It removes extra spaces while reading the configuration file.
     * @param istr
     * @return ostr
     */
    private String ckspace(String istr)
    {
        String ostr;
        String exchange;
        for(ostr = (new StringBuffer(String.valueOf(istr))).toString(); ostr.startsWith(" "); ostr = exchange.substring(1, ostr.length()))
            exchange = (new StringBuffer(String.valueOf(ostr))).toString();

        if(ostr.length() > 0)
        {
            for(; ostr.endsWith(" "); ostr = exchange.substring(0, ostr.length() - 1))
                exchange = (new StringBuffer(String.valueOf(ostr))).toString();

        }
        return ostr;
    }

    /**
     * Method loopTask.
     * It runs the task in loop
     */
    private void loopTask()
    {
        (new Thread() {

            public void run()
            {
                while(gotask && time != 0) 
                {
                    ping();
                    try
                    {
                        Thread.sleep(time);
                    }
                    catch(InterruptedException interruptedexception) { }
                }
            }

        }).start();
    }

    /**
     * Main method.
     * It startd the program and optionally reads input configuration file
     * from command line
     * @param args
     */
    public static void main(String args[])
    {
        String textfile = "";
        if(args.length > 0)
            textfile = args[0];
        try
        {
            new MQPing(textfile);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private static final long serialVersionUID = 1L;
    String token;
    String comments;
    String panelTitle;
    String qmgr;
    String ipAddress;
    String snail;
    Vector mqmgr;
    Vector vecInd;
    Vector el_per_panel;
    int mqPort;
    int num;
    int time;
    int textMode;
    int textSize;
    int flag;
    boolean gotask;
    JPanel contentP;
    JPanel response;
    JPanel title;
    JPanel butpan;
    JPanel pan1;
    JPanel pan2;
    JPanel locPanel;
    JTextField textQMGR;
    JTextField textADDR;
    JTextField textPORT;
    JTextField messages;
    JLabel labelStatus;
    JButton stat;
    PingThread myPing;
    InputStream in;
    ImageIcon image;
    ImageIcon iconQuestion;
    String copyRight;

}

/**
 * Class ImageFilter.
 * It verifies input file type
 * @author maurizio.dellefratte@it.ibm.com
 */
class ImageFilter extends FileFilter
{

    ImageFilter()
    {
    }

    public boolean accept(File f)
    {
        return f.getName().toLowerCase().endsWith(".txt") || f.isDirectory();
    }

    public String getDescription()
    {
        return " *.txt ";
    }
}

/**
 * Class PingThread.
 * It manages the activity of ping.
 * @author maurizio.dellefratte@it.ibm.com
 */
class PingThread extends Thread
{

	/**
	 * Constructor method.
	 * It sets the variables to the initial values
	 * @param n
	 * @param ico
	 * @param ipAddress
	 * @param mqPort
	 */
    PingThread(int n, JLabel ico, String ipAddress, int mqPort)
    {
        timeOut = 3000;
        iconYes = new ImageIcon(ClassLoader.getSystemResource("Icons/yes.gif"));
        iconNo = new ImageIcon(ClassLoader.getSystemResource("Icons/no.gif"));
        ipAddress1 = ipAddress;
        mqPort1 = mqPort;
        ico1 = ico;
    }

   
    /**
     * Method run.
     * It starts the activity of ping
     */
    public void run()
    {
        try
        {
            clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(ipAddress1, mqPort1), timeOut);
            clientSocket.close();
            ico1.setIcon(iconYes);
        }
        catch(IOException ie)
        {
            ico1.setIcon(iconNo);
        }
    }

    int timeOut;
    Socket clientSocket;
    String ipAddress1;
    int mqPort1;
    JLabel ico1;
    ImageIcon iconYes;
    ImageIcon iconNo;
}
