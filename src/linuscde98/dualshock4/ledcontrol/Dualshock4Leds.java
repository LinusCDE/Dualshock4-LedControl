package linuscde98.dualshock4.ledcontrol;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Dualshock4Leds {

	public static void main(String[] args) {
		
		// Check for Unix
		if(File.listRoots().length != 1 || !File.listRoots()[0].getPath().equalsIgnoreCase("/")){
			JOptionPane.showMessageDialog(null, "This program is meant to work only with Unix-Systems (Linux, OS X, BSD, ...)", "Operating system not supported!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		for(int i = 0; i < args.length; i++){
			if(args[i].equalsIgnoreCase("--list") || args[i].equals("-l")){
				
				String[] devices = UnixLedAPI.listControllers();
				
				for(String dev : devices)
					System.out.println(dev);
				
				System.exit(0);
			}
		}
		
		new Dualshock4Leds();
	}
	
	public JFrame win;
	public JComboBox<String> controllers;
	public JButton refreshControllers;
	public JButton changeColor;
	public JLabel currentColor;
	public JPanel colorPane;
	
	public HashMap<String/*Controller name*/, String/*Device ID*/> controllerList = new HashMap<String, String>();
	
	/**
	 * Updates the list of available controllers in the window
	 */
	public void reloadControllers(){
		colorPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "---"));
		colorPane.setEnabled(false);
		currentColor.setIcon(null);
		currentColor.setText("---");
		
		String[] devids = UnixLedAPI.listControllers();
		
		controllerList.clear();
		
		for(int i = 0; i < devids.length; i++){
			int capacity = UnixLedAPI.getBatteryCapacity(devids[i]);
			controllerList.put("DS4-Controller " + (i+1) + (capacity != -1 ? " (Accu: " + capacity + "%)" : ""), devids[i]);
		}
		
		controllers.setModel(new SimpleComboBoxModel<String>(controllerList.keySet().toArray(new String[]{})));
		SwingUtilities.updateComponentTreeUI(controllers);
	}
	
	/**
	 * Update color-data for selected controller in the window
	 */
	public void refreshSelected(){
		int index = controllers.getSelectedIndex();
		if(!(index < controllerList.size() && index >= 0)) return;
		
		String devname = controllers.getModel().getElementAt(index);
		String devid = controllerList.get(devname);
		if(!UnixLedAPI.isValidDualshock4(devid)){
			currentColor.setIcon(null);
			currentColor.setText("Color: Not found!");
			return;
		}
		
		int[] clr = UnixLedAPI.readRGB(devid);
		if(clr == null){
			currentColor.setIcon(null);
			currentColor.setText("Color: Read-Failure!");
			return;
		}
		Color c = new Color(clr[0], clr[1], clr[2]);
		
		try{
			
			int size = currentColor.getHeight();
			BufferedImage bimg = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = bimg.createGraphics();
			g2d.setColor(c);
			g2d.fillRect(0, 0, size, size);
			g2d.dispose();
			currentColor.setIcon(new ImageIcon(bimg));
			
			
		}catch(Exception ex){}
		String hexclr = "#" + Integer.toHexString(c.getRGB()).substring(2).toUpperCase();
		currentColor.setText(" HEX: " + hexclr + " | RGB: " + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue());
		colorPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), devname));
		colorPane.setEnabled(true);
		
		SwingUtilities.updateComponentTreeUI(colorPane);
	}
	
	/**
	 * Prompts user to choose a new colors and flushes it to the controller
	 */
	public void chooseAndChangeColor(){
		int index = controllers.getSelectedIndex();
		if(!(index < controllerList.size() && index >= 0)) return;
		
		String devname = controllers.getModel().getElementAt(index);
		String devid = controllerList.get(devname);
		if(devid == null) return;
		
		if(!UnixLedAPI.isValidDualshock4(devid)){
			JOptionPane.showMessageDialog(null, "Controller not found!", "Fehler", JOptionPane.ERROR_MESSAGE);
			reloadControllers();
			return;
		}
		
		int[] clr = UnixLedAPI.readRGB(devid);
		if(clr == null){
			JOptionPane.showMessageDialog(null, "Color not readable!", "Fehler", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if(!UnixLedAPI.isAccessible(devid)){
			JOptionPane.showMessageDialog(null, "The color can't be changed.\nYou may need Root-Access.", "Insufficient privileges!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		Color c = new Color(clr[0], clr[1], clr[2]);
		Color newcolor = JColorChooser.showDialog(win, "Choose color for " + devname, c);
		if(!UnixLedAPI.writeRGB(devid, newcolor.getRed(), newcolor.getGreen(), newcolor.getBlue())){
			JOptionPane.showMessageDialog(null, "Color could not be changed!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		refreshSelected();
	}
	
	/**
	 * Constructor - builds window and adds some actionlisteners to window-components
	 */
	public Dualshock4Leds(){
		
		// Building basic window
		win = new JFrame("Dualshock 4 - Colortool");
		win.setSize(600, 400);
		win.setLayout(null); // disabling automatic arrangement to allow positions by pixels
		
		// Creating components and configuring them
		controllers = new JComboBox<String>(new String[]{});
		refreshControllers = new JButton("Refresh list");
		
		colorPane = new JPanel();
		colorPane.setLayout(null);
		colorPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "---"));
		colorPane.setEnabled(false);
		
		changeColor = new JButton("Change color");
		currentColor = new JLabel("Color: ---");
		
		int y = colorPane.getInsets().top;
		
		currentColor.setBounds(10, y, 400, 20);
		y += 22;
		changeColor.setBounds(10, y, 200, 25);
		y += 25;
		colorPane.add(currentColor);
		colorPane.add(changeColor);
		int paneHeight = y + 5 + colorPane.getInsets().bottom;
		
		y = 5;
		controllers.setBounds(5, y, 300, 25);
		refreshControllers.setBounds(10 + 300, y, 200, 25);
		y += 27;
		colorPane.setBounds(5, y, 500, paneHeight);
		y += paneHeight + 40;
		win.add(controllers);
		win.add(refreshControllers);
		win.add(colorPane);
		
		win.setSize(520, y);
		win.setLocationRelativeTo(null);
		
		controllers.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
			refreshSelected();
			}
		});
		
		changeColor.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				chooseAndChangeColor();
			}
		});
		
		refreshControllers.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				reloadControllers();
			}
		});
		
		reloadControllers();
		win.setVisible(true);
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
}
