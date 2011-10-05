import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Game.Tab;
import org.rsbot.script.methods.GrandExchange.GEItem;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.SkillData;
import org.rsbot.script.util.Timer;

import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.RSTilePath;

@ScriptManifest(authors = { "Battleguard" }, version = 2.04, description = "Al Kharid AIO Gem/Gold Crafter, by Battleguard", name = "Al Kharid AIO Gem/Gold Crafter")
public class goldCrafter extends Script implements PaintListener, MouseListener {
	
	
	private final static int FURNACE_ID = 11666;
	private static boolean guiWait = true, START_SCRIPT = true;
	
	private final static int RING_MOULD_ID = 1592, NECK_MOULD_ID = 1597,
			AMMY_MOULD_ID = 1595, BRACELET_MOULD_ID = 11065;;
	private static int MOULD_ID = RING_MOULD_ID;

	// ID OF GEMS
	private final static int GOLD_ID = 2357, SAPPHIRE_ID = 1607,
			EMERALD_ID = 1605, RUBY_ID = 1603, DIAMOND_ID = 1601;
	private static int GEM_ID = GOLD_ID;

	// XP USED TO CALCULATE STATS
	private final static int GOLD_XP = 15, SAPPHIRE_XP = 40, EMERALD_XP = 55,
			RUBY_XP = 70, DIAMOND_XP = 85;
	private static int EXP_PER = GOLD_XP;

	// COMPONENTS FOR INTERFACE AT FURNACE
	private final static int GOLD_RING_COMP = 82;
	private static int COMPONENT_ID = GOLD_RING_COMP;

	// PRICE OF ITEM USED FOR CALCULATING PROFITS
	private static int ITEM_PRICE;
	private static String ITEM_NAME;

	private enum State {
		withdrawling, depositing, To_Bank, To_Furnace, Crafting, at_Bank
	}

	private State curState = null;
	private final static RSArea FurnaceArea = new RSArea(
			new RSTile(3274, 3184), new RSTile(3279, 3188));

	private final static RSTile[] tilesToFurnace = { new RSTile(3269, 3167),
			new RSTile(3276, 3170), new RSTile(3278, 3176),
			new RSTile(3281, 3181), new RSTile(3278, 3186),
			new RSTile(3275, 3186) };
	private RSTilePath pathToFurnace;

	public boolean onStart() {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new gui();
			}
		});
		while(guiWait) 
			sleep(500);
		pathToFurnace = walking.newTilePath(tilesToFurnace);
		return START_SCRIPT;
	}

	@Override
	public int loop() {
		try {
			mouse.setSpeed(random(6, 8));
			curState = getState();
			switch (curState) {
			case depositing:
				bank.depositAllExcept(MOULD_ID);
				break;
			case withdrawling:
				bankWithdrawal();
				break;
			case To_Bank:
				walkToBankNew(BANK_TILE);
				break;
			case To_Furnace:
				startedCrafting = false;
				clickOnFurnace = false;
				walkToFurnace();
				break;
			case Crafting:
				craftItems2();
				break;
			case at_Bank:
				bank.open();
				break;
			}
		} catch (Exception e) {
			log("A problem occured");
			log(e);
		}
		return random(75, 150);
	}
	

	/**
	 * Withdrawals 27 gold bars and will also withdrawal the appropriate mould
	 * to make item. Will stop script if you have run out of gold bars or do not
	 * have a mould.
	 */
	private void bankWithdrawal() {
		if (bank.isOpen()) {
			try {
				if (!inventory.contains(MOULD_ID)) {
					if (bank.getCount(MOULD_ID) == 0) {
						log("You do not have a Mould");
						stopScript();
					}
					bank.withdraw(MOULD_ID, 1);
				}
				if (bank.getCount(GOLD_ID) == 0 || bank.getCount(GEM_ID) == 0) {
					doubleCheck();
				}

				if (GEM_ID == GOLD_ID) {
					bank.withdraw(GOLD_ID, 0);
				} else {
					bank.withdraw(GOLD_ID, 13);
					bank.withdraw(GEM_ID, 13);
				}
			} catch (Exception e) {
				log("Problem withdraweling items from bank");
				log(e);
			}
		}
	}

	private static boolean startedCrafting = false, clickOnFurnace = false;
	private static int prevXP;
	private static Timer furnaceReset = new Timer(5000);

	/**
	 * Clicks on furnace and returns if click was successful
	 * 
	 * @returns true if click was successful
	 */
	boolean clickFurnace() {
		RSItem bar = inventory.getItem(GOLD_ID);
		RSObject furnace = objects.getNearest(FURNACE_ID);
		if (!inventory.isItemSelected() && bar != null) {
			inventory.selectItem(bar);
		}
		if (furnace == null || !furnace.isOnScreen()) {
			camera.turnTo(furnace);
			return false;
		}
		if (furnace.interact("Use")) {
			return true;
		}
		return false;
	}

	/**
	 * After clicking on furnace we will wait 1 second
	 */
	void waitingForInterface() {
		if (!furnaceReset.isRunning()) {
			if (clickFurnace()) {
				furnaceReset = new Timer(1000);
				clickOnFurnace = true;
			}
		} else {
			if (getMyPlayer().isMoving()) {
				furnaceReset.reset();
			}
		}
	}

	private void craftItems2() {
		if (!startedCrafting) {			
			if (isInterfaceOpen()) {
				makeItem();
				startedCrafting = true;
				prevXP = skills.getCurrentExp(Skills.CRAFTING);
				furnaceReset = new Timer(5000);
			} else {
				waitingForInterface();
			}
		} else {
			//log("currently crafting");
			int currentXP = skills.getCurrentExp(Skills.CRAFTING);
			if (!walking.isRunEnabled()) {
				walking.setRun(true);
				sleep(1000);
			}
			getReadyforClick(BANK_TILE);
			if (currentXP > prevXP) {
				prevXP = currentXP;
				furnaceReset.reset();
			} else {
				if (!furnaceReset.isRunning()) {
					startedCrafting = false;
				}
			}
		}
	}

	private final static RSTile BANK_TILE = new RSTile(3268, 3167);

	private void walkToBankNew(final RSTile bankTile) {
		if (getMyPlayer().isMoving() || bank.isOpen()) {
			if (calc.distanceBetween(players.getMyPlayer().getLocation(),
					BANK_TILE) > 7) {
			}
			return;
		}
		final RSObject bankBooth = objects.getTopAt(bankTile);
		if (bankBooth != null) {
			camera.turnTo(bankBooth);
			camera.setPitch(0);
		}

		if (bankBooth != null && bankBooth.isOnScreen()) {
			bankBooth.interact("Use-quickly Bank booth");
			sleep(2000);
		} else {
			pathToFurnace.reverse();
			pathToFurnace.traverse();
			pathToFurnace.reverse();
		}
	}

	private void getReadyforClick(final RSTile bankTile) {
		if (inventory.getCount(GOLD_ID) < 4) {
			final RSObject bankBooth = objects.getTopAt(bankTile);
			if (bankBooth == null) {
				log("error could not find bankbooth");
				return;
			}

			if (!bankBooth.getModel().contains(mouse.getLocation())) {
				if (!bankBooth.doClick(false)) {
					camera.setPitch(0);
					camera.turnTo(bankBooth);
				}

			}
		} else {
			antiban();
		}

	}

	/**
	 * Method to make sure we click the furnace After clicking furnace for the
	 * first time we will not click again until 2 seconds have passed
	 */
	void furnaceMethod() {
		if (!clickOnFurnace) {
			RSItem bar = inventory.getItem(GOLD_ID);
			RSObject furnace = objects.getNearest(FURNACE_ID);
			if (!inventory.isItemSelected() && bar != null) {
				inventory.selectItem(bar);
			}
			if (furnace == null || !furnace.isOnScreen()) {
				return;
			}
			furnace.interact("Use");
			clickOnFurnace = true;
			furnaceReset = new Timer(1500);
		} else {
			if (!furnaceReset.isRunning()) {
				clickOnFurnace = false;
			}
		}
	}

	/**
	 * Walking method to furnace also makes sure we are running
	 */

	private void walkToFurnace() {
		pathToFurnace.traverse();
		if (camera.getPitch() < 25 || camera.getPitch() > 35) {
			camera.setPitch(30);
			sleep(500);
		}
		if (camera.getAngle() < 85 || camera.getAngle() > 95) {
			camera.setAngle(89);
			sleep(500);
		}
	}

	/**
	 * Checks to see if we are ready to hit make all
	 */
	private boolean isInterfaceOpen() {
		try {
			return interfaces.getComponent(446, 13).getText().equals("What would you like to make?");
		} catch (Exception e) {			
		}
		return false;
		/*
		 try {
		 
			return interfaces.getComponent(446, 13).getText().equals("What would you like to make?") ? true : false;
		} catch(Exception e) {
			log("Exception occured " + e);
			return false;
		}
		*/		
	}

	/**
	 * Clicks the make all button at the furnace
	 */
	private void makeItem() {
		if(isInterfaceOpen()) {
			interfaces.getComponent(446, COMPONENT_ID).interact("Make All");
		}
	}

	/**
	 * Gets the current state of the BOT
	 * 
	 * @return returns the current State of the BOT
	 */
	private State getState() {
		try {
			if (inventory.contains(GOLD_ID) && inventory.contains(GEM_ID)
					&& inventory.contains(MOULD_ID)) {
				return FurnaceArea.contains(players.getMyPlayer().getLocation()) ? State.Crafting : State.To_Furnace;
			} else {
				if (!bank.isOpen()) {
					return State.To_Bank;
				} else {
					// return State.depositing;
					return (inventory.getCountExcept(MOULD_ID) == 0) ? State.withdrawling
							: State.depositing;
				}
			}
		} catch (Exception e) {
			log("An exception has occured in the getState method");
			log(e);
			return State.To_Bank;
		}
	}

	/**
	 * dumb code because sometime the bot thinks im out of bars when im not this
	 * happened because the bot was banking so fast
	 */
	void doubleCheck() {
		for (int i = 0; i < 10; i++) {
			if (bank.getCount(GOLD_ID) != 0 && bank.getCount(GEM_ID) != 0) {
				return;
			}
			sleep(500);
		}
		log("you are out of bars or gems");
		stopScript();
		
		


	}

	



	public void onFinish() {
		log("Thank you for using the script!");
		log("Time Ran  " + runClock.toElapsedString());
		log("State:  " + curState);
		log("Items Made: " + k.format(skillData.expGain(idx) / EXP_PER));
		log("Items Per Hour: " + k.format(skillData.hourlyExp(idx) / EXP_PER));
	}

	/**
	 * ANTIBAN method, this will only be accessed during crafting at furnace
	 */
	private void antiban() {
		int b = random(0, 1000);
		if (b >= 0 && b <= 50) {
			mouse.moveSlightly();
			sleep(200, 600);
			mouse.moveRandomly(150, 350);
		} else if (b > 50 && b <= 100) {
			camera.setPitch(random(30, 50));
			sleep(400, 600);
		} else if (b > 100 && b <= 150) {
			mouse.moveOffScreen();
			sleep(600, 1200);
		} else if (b > 150 && b <= 200) {
			camera.setAngle('W');
			sleep(400, 600);
		} else if (b == 500) {
			game.openTab(Tab.STATS, true);
			skills.doHover(Skills.INTERFACE_CRAFTING);
			sleep(800, 1200);
		}
	}

	private static SkillData skillData = null;
	private final static int idx = Skills.getIndex("crafting");
	private static Timer runClock = new Timer(0);
	private NumberFormat k = new DecimalFormat("###,###,###");
	private Timer noXPTimer;

	private final static Rectangle paintBox = new Rectangle(5, 345, 510, 130);
	private static int oldXP;

	
    //START: Code generated using Enfilade's Easel
    private Image getImage(String url) {
        try {
            return ImageIO.read(new URL(url));
        } catch(IOException e) {
            return null;
        }
    }
	

    
	private final Rectangle TAB1_BOX = new Rectangle(25, 360, 120, 29);
	private final Rectangle TAB2_BOX = new Rectangle(170, 360, 120, 29);
	private final Rectangle TAB3_BOX = new Rectangle(316, 360, 120, 29);
	private final Rectangle HIDE_BOX = new Rectangle(413, 439, 92, 21);
	

    private final Image experience = getImage("http://i.imgur.com/HDHbP.png");
    private final Image profit = getImage("http://i.imgur.com/al5T1.png");
    private final Image overall = getImage("http://i.imgur.com/mLlyR.png");
    private final Image hide = getImage("http://i.imgur.com/AoJP6.png");
    private final Image percentBar = getImage("http://i.imgur.com/KRaWK.png");

	private enum Curpaint {
		TAB1, TAB2, TAB3
	};
	
	private boolean hidePaint = false;

	private Curpaint SHOWING_TAB = Curpaint.TAB3;
	
	@Override
	public void onRepaint(Graphics g) {
		
		if (skillData == null) {
			skillData = skills.getSkillDataInstance();
		}
		
		if(oldXP != skillData.exp(idx)) {
			noXPTimer = new Timer(2 * 60 * 1000);
		}
		if(!noXPTimer.isRunning()) {			
			log("We have not got any XP for 2 minutes, state = " + getState());
		}
		
		final double lvlGain = skillData.levelsGained(idx);
		final double xpGain = skillData.expGain(idx);
		final double xpHour = skillData.hourlyExp(idx);
		final double itemsMade = xpGain / EXP_PER;
		final double itemsHour = xpHour / EXP_PER;
		final double goldMade = itemsMade * ITEM_PRICE;
		final double goldHour = itemsHour * ITEM_PRICE;
		final int percent = skills.getPercentToNextLevel(Skills.CRAFTING);
		final double xpTNL = skillData.expToLevel(idx) % 1000;
		
		g.setFont(new Font("Gayatri", Font.BOLD, 16));
		g.setColor(Color.BLACK);
		
		if(hidePaint) {
			g.drawImage(hide, 0, 300, null);
			return;
		}

		
		switch (SHOWING_TAB) {
		case TAB1: 
			g.drawImage(experience, 0, 302, null);
			g.drawString(Timer.format(skillData.timeToLevel(idx)), 141, 438);
			g.drawString(k.format(lvlGain), 370, 437);
			int barWidth = (percent * percentBar.getWidth(null)) / 100;
			g.setColor(Color.YELLOW);
			g.drawImage(percentBar, 72, 401, barWidth, percentBar.getHeight(null), null);		
			g.setColor(Color.WHITE);
			g.setFont(new Font("Gayatri", Font.BOLD, 11));
			g.drawString(percent + "% to " + (skills.getCurrentLevel(idx)+ 1) + " Crafting ( " + k.format(xpTNL) + "k XP till Level)", 140, 412);
			g.setFont(new Font("Gayatri", Font.BOLD, 16));
			g.setColor(Color.BLACK);
			break;
		case TAB2: 
			g.drawImage(profit, 0, 302, null);
			g.drawString(k.format(goldMade) + "gp", 126, 415);
			g.drawString(k.format(goldHour) + "gp", 167, 435);
			
			break;
		case TAB3: 
			g.drawImage(overall, 0, 302, null);
			// IMAGE SIZE = 5 x 294, 530 x 181			
			g.drawString(k.format(xpGain), 180, 416);
			g.drawString(k.format(xpHour), 167, 435);
			g.drawString(k.format(itemsMade), 348, 413);
			g.drawString(k.format(itemsHour), 346, 435);
			break;
		}
		g.drawString("" + curState, 141, 455);
		g.drawString(runClock.toElapsedString(), 286, 457);
		

		
		
		/*
		// PAINT SETUP
		g.setColor(Color.BLACK);
		g.setFont(new Font("Bodoni MT", 0, 13));
		g.fill3DRect(5, 345, 510, 130, true);
		g.setColor(Color.WHITE);

		// TEXT DATA
		g.drawString("F2P Al Kharid AIO Gold Crafter, by Battleguard", 10, 360);
		g.drawString("Time Ran  " + runClock.toElapsedString(), 10, 380);
		g.drawString("State:  " + curState, 10, 400);
		g.drawString("Gold Made: " + k.format(goldMade) + "gp", 10, 420);
		g.drawString("Gold Per Hour: " + k.format(goldHour) + "gp", 10, 440);
		g.drawString("XP Gained: " + k.format(xpGain), 300, 360);
		g.drawString("XP Per Hour: " + k.format(xpHour), 300, 380);
		g.drawString("Items Made: " + k.format(itemsMade), 300, 410);
		g.drawString("Items Per Hour: " + k.format(itemsHour), 300, 430);

		// CODE FOR PROGRESS BAR
		g.setColor(Color.white);
		g.fill3DRect(20, 450, 450, 20, false);
		int barWidth = (skills.getPercentToNextLevel(Skills.CRAFTING) * 450) / 100;
		g.setColor(Color.green);
		g.fill3DRect(20, 450, barWidth, 20, true);
		g.setColor(Color.black);
		g.drawString("Cur lvl: " + skills.getCurrentLevel(Skills.CRAFTING)
				+ "  " + skills.getPercentToNextLevel(Skills.CRAFTING) + "%",
				220, 464);
		 */
	}

	public class gui extends JFrame {
		private static final long serialVersionUID = 1L;
		
		private final String [] gems = {"Gold bar", "Sapphire", "Emerald","Ruby","Diamond","Dragonstone","Onyx"};
		private final String [] type = {"ring", "necklace", "amulet","bracelet"};
		
		
		private final JLabel headerLbl = new JLabel("Alkharid gold & jewelry crafting", JLabel.CENTER);
		private final JLabel locationLbl = new JLabel("Select Gem ", JLabel.RIGHT);
		private final JLabel barLbl = new JLabel("Select Type ", JLabel.RIGHT);
		private final JLabel profitLbl = new JLabel("Profit per item: --: ", JLabel.RIGHT);
		
		private final JComboBox gemsCombo = new JComboBox(gems);
		private final JComboBox typeCombo = new JComboBox(type);
		
		
		private final JButton calcBtn = new JButton("Calc profit");
		private final JButton startBtn = new JButton("Start");		
		private final Font headerFont = new Font("Book antiqua", Font.PLAIN, 18);
		private final Font normalFont = new Font("Book antiqua", Font.PLAIN, 16);
		
		GridBagConstraints c;
		Container pane;		
		
				
		void setFonts(Font font, Component... comps) {
			for (Component curcomp : comps) {
				curcomp.setFont(font);
			}
		}
		
		void addBorder(JLabel... lbls) {
			final Border border = LineBorder.createGrayLineBorder();
			for (JLabel lbl : lbls) {
				lbl.setBorder(border);
			}
		}
		
		void addToGrid(Component comp, int gridx, int gridy, int gridwidth, double weightx) {
			c.gridx = gridx;
			c.gridy = gridy;
			c.gridwidth = gridwidth;
			c.weightx = weightx;
			pane.add(comp, c);
		}
		
		int saveVariables() {						
			final int[] mouldIDTable = {1592, 1597, 1595, 11065};			
			final int[][] xpTable = {
					{15, 40, 55, 70, 85, 100, 115},   // RINGS
					{20, 5, 60, 75, 90, 105, 120},    // NECKLACES
					{30, 65, 70, 85, 100, 150, 165},  // AMULETS
					{25, 60, 65, 80, 95, 110, 400}    // BRACELETS
			};			
			final int[][] compTable = {
					{82, 84, 86, 88, 90, 92, 94},    // RINGS
					{68, 70, 72, 74, 76, 78, 80},    // NECKLACES
					{53, 55, 57, 59, 61, 63, 65},    // AMULETS
					{33, 35, 37, 39, 41, 43, 45}     // BRACELETS
			};
			
			final int typeIdx = typeCombo.getSelectedIndex();
			final int gemIdx = gemsCombo.getSelectedIndex();
			
			MOULD_ID = mouldIDTable[typeIdx];
			EXP_PER = xpTable[typeIdx][gemIdx];
			COMPONENT_ID = compTable[typeIdx][gemIdx];
			
			/**
			 * COMPONENT INFO
			 * 
			 * RINGS
			 * 82, 84, 86, 88, 90, 92, 94
			 * 
			 * NECKLACES
			 * 68, 70, 72, 74, 76, 78, 80
			 * 
			 * AMULETS
			 * 53, 55, 57, 59, 61, 63, 65
			 * 
			 * BRACELETS
			 * 33, 35, 37, 39, 41, 43, 45
			 */
			
			
			
			GEItem itemInfo;
			GEItem GemInfo = grandExchange.lookup(gemsCombo.getSelectedItem().toString());
			if(GemInfo.getID() == 2357) {
				itemInfo = grandExchange.lookup("Gold " + typeCombo.getSelectedItem().toString());
				ITEM_PRICE = itemInfo.getGuidePrice() - GemInfo.getGuidePrice(); 
			} else {
				itemInfo = grandExchange.lookup(GemInfo.getName() + " " + typeCombo.getSelectedItem().toString());
				ITEM_PRICE = itemInfo.getGuidePrice() - GemInfo.getGuidePrice() -  grandExchange.lookup(2357).getGuidePrice(); 
			}
			
			ITEM_NAME = itemInfo.getName();
			GEM_ID = GemInfo.getID();
			
			log("Making " + itemInfo.getName() + ", ID of item: " + itemInfo.getID() + " XP per: " + EXP_PER);
			log("It will require: " + GemInfo.getName() + ", ID of gem: " + GemInfo.getID());					
			log("Profit per item: " + ITEM_PRICE);
			return ITEM_PRICE;
		}
		
		public gui() {
			super("Alkharid gold & jewelry crafting"); // name the window that it the GUI pops in
			
			addBorder(headerLbl, locationLbl, barLbl, profitLbl);			
			setFonts(headerFont, headerLbl, locationLbl, barLbl, profitLbl);
			setFonts(normalFont, startBtn, gemsCombo, typeCombo, calcBtn);
			
			pane = new Container();
			pane.setLayout(new GridBagLayout());
			c = new GridBagConstraints();			
			c.fill = GridBagConstraints.HORIZONTAL;
			c.ipady = c.ipadx = 5; // SET INDIX Y PADDING
			c.insets = new Insets(10,10,10,10); // ADD SPACING BETWEEN GRID COMPONENTS
			
			pane.setPreferredSize(new Dimension(400, 250));
			addToGrid(headerLbl, 0, 0, 2, 1.0);
			addToGrid(locationLbl, 0, 1, 1, .80);						
			addToGrid(barLbl, 0, 2, 1, .80);
			addToGrid(profitLbl, 0, 3, 1, .80);
			addToGrid(gemsCombo, 1, 1, 1, .20);
			addToGrid(typeCombo, 1, 2, 1, .20);
			addToGrid(calcBtn, 1, 3, 1, .20);	
			addToGrid(startBtn, 0, 4, 2, 1.0);
			
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					START_SCRIPT = guiWait = false;
					log("Cancelling Startup of script"); 
					dispose();
				}
			});			
			
			startBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					saveVariables();
					guiWait = false;
					dispose();
				}
			});
			
			calcBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {				
					profitLbl.setText("Profit per item: " + saveVariables() + "gp ");
					setVisible(true);
				}
			});
			
			getContentPane().add(pane);
			setLocationRelativeTo(getOwner());
			pack();	        
			setVisible(true);
		}
	}



	@Override
	public void mouseClicked(MouseEvent e) {
		if(HIDE_BOX.contains(e.getPoint())) {
			hidePaint ^= true;
		}		
		if (TAB1_BOX.contains(e.getPoint())) {
			SHOWING_TAB = Curpaint.TAB1;
		} else if (TAB2_BOX.contains(e.getPoint())) {
			SHOWING_TAB = Curpaint.TAB2;
		} else if (TAB3_BOX.contains(e.getPoint())) {
			SHOWING_TAB = Curpaint.TAB3;
		}	
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}