import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.LayoutStyle;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Game.Tab;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.SkillData;
import org.rsbot.script.util.Timer;

import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.RSTilePath;

@ScriptManifest(authors = { "Battleguard" }, version = 2.00, description = "Al Kharid AIO Gem/Gold Crafter, by Battleguard", name = "Al Kharid AIO Gem/Gold Crafter")
public class goldCrafterCleanup extends Script implements PaintListener,
		MouseMotionListener {

	private final static int FURNACE_ID = 11666, BANKBOOTH_ID = 35647;

	private static boolean guiWait = true;
	private craftingGUI g = new craftingGUI();



	/*
	 * NEW CODE FOR GEM SUPPORT
	 */

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
	private final static int GOLD_RING_COMP = 82;// , GOLD_NECK_COMP = 68,
													// GOLD_AMMY_COMP = 53;
	private static int COMPONENT_ID = GOLD_RING_COMP;

	// PRICE OF ITEM USED FOR CALCULATING PROFITS
	private static int ITEM_PRICE, ITEM_ID;
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
		g.setVisible(true);
		while (guiWait)
			sleep(500);
		ITEM_PRICE -= grandExchange.lookup(GOLD_ID).getGuidePrice();
		log("Price minus goldbar" + ITEM_PRICE);
		if (GEM_ID != GOLD_ID) {
			ITEM_PRICE -= grandExchange.lookup(GEM_ID).getGuidePrice();
		}
		log("Price minus gem" + ITEM_PRICE);
		log("Thank you for starting Gold Crafter");
		pathToFurnace = walking.newTilePath(tilesToFurnace);
		return true;
	}

	@Override
	public int loop() {
		try {
			mouse.setSpeed(random(4, 6));
			curState = getState();
			switch (curState) {
			case depositing:
				bank.depositAllExcept(MOULD_ID);
				// deposit();
				// bankWithdrawal();
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
				// antiban
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
		return interfaces.getComponent(446, COMPONENT_ID).isValid();
	}

	/**
	 * Clicks the make all button at the furnace
	 */
	private void makeItem() {
		if (interfaces.getComponent(446, COMPONENT_ID).isValid()) {
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
				return FurnaceArea
						.contains(players.getMyPlayer().getLocation()) ? State.Crafting
						: State.To_Furnace;
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
	 * dumb code because sometime the bot thinks im out of bars when im not this happened because the bot was banking so fast
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

	private static Point mouseSpot;
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseSpot = e.getPoint();
	}

	private static SkillData skillData = null;
	private final static int idx = Skills.getIndex("crafting");
	private static Timer runClock = new Timer(0);
	private NumberFormat k = new DecimalFormat("###,###,###");
	
	

	
	private final static Rectangle paintBox = new Rectangle(5, 345, 510, 130);

	@Override
	public void onRepaint(Graphics g) {
		if (skillData == null) {
			skillData = skills.getSkillDataInstance();
		}
		if (paintBox.contains(mouseSpot)) {
			return;
		}

		final double xpGain = skillData.expGain(idx);
		final double xpHour = skillData.hourlyExp(idx);
		final double itemsMade = xpGain / EXP_PER;
		final double itemsHour = xpHour / EXP_PER;
		final double goldMade = itemsMade * ITEM_PRICE;
		final double goldHour = itemsHour * ITEM_PRICE;

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
	}

	/**
	 * Class that handles the GUI Finds the COMPONENT_ID, MOULD_ID, EXP_PER that
	 * we will be using
	 */
	public class craftingGUI extends JFrame {
		private static final long serialVersionUID = 1L;

		public craftingGUI() {
			initComponents();
		}

		private void button1ActionPerformed(ActionEvent e) {
			String gem = gemSelected.getSelectedItem().toString();
			String type = typeSelected.getSelectedItem().toString();

			if (gem.equals("Sapphire")) {
				GEM_ID = SAPPHIRE_ID;
				EXP_PER = SAPPHIRE_XP;
				COMPONENT_ID += 2;
			} else if (gem.equals("Emerald")) {
				GEM_ID = EMERALD_ID;
				EXP_PER = EMERALD_XP;
				COMPONENT_ID += 4;
			} else if (gem.equals("Ruby")) {
				GEM_ID = RUBY_ID;
				EXP_PER = RUBY_XP;
				COMPONENT_ID += 6;
			} else if (gem.equals("Diamond")) {
				GEM_ID = DIAMOND_ID;
				EXP_PER = DIAMOND_XP;
				COMPONENT_ID += 8;
			}

			if (type.equals("necklace")) {
				MOULD_ID = NECK_MOULD_ID;
				EXP_PER += 5;
				COMPONENT_ID -= 14;
			} else if (type.equals("amulet")) {
				MOULD_ID = AMMY_MOULD_ID;
				EXP_PER += 15;
				COMPONENT_ID -= 29;
			} else if (type.equals("bracelet")) {
				MOULD_ID = BRACELET_MOULD_ID;
				EXP_PER += 10;
				COMPONENT_ID -= 49;
			}

			ITEM_NAME = gem + " " + type;
			ITEM_PRICE = grandExchange.lookup(ITEM_NAME).getGuidePrice();
			ITEM_ID = grandExchange.getItemID(ITEM_NAME);
			log("Price of finished product" + ITEM_PRICE);

			guiWait = false;
			g.dispose();
		}

		private void initComponents() {
			// JFormDesigner - Component initialization - DO NOT MODIFY
			// //GEN-BEGIN:initComponents
			// Generated using JFormDesigner Evaluation license - Battleguard
			label1 = new JLabel();
			label2 = new JLabel();
			gemSelected = new JComboBox();
			startButton = new JButton();
			label3 = new JLabel();
			typeSelected = new JComboBox();

			// ======== this ========
			Container contentPane = getContentPane();

			// ---- label1 ----
			label1.setText("Al Kaharid Gold Crafter");
			label1.setFont(new Font("Tahoma", Font.PLAIN, 20));

			// ---- label2 ----
			label2.setText("Crafting Item:");
			label2.setFont(new Font("Tahoma", Font.PLAIN, 14));

			// ---- gemSelected ----
			gemSelected.setModel(new DefaultComboBoxModel(new String[] {
					"Gold", "Sapphire", "Emerald", "Ruby", "Diamond" }));

			// ---- startButton ----
			startButton.setText("Start");
			startButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});

			// ---- label3 ----
			label3.setText("Type to craft: ");
			label3.setFont(new Font("Tahoma", Font.PLAIN, 14));

			// ---- typeSelected ----
			typeSelected.setModel(new DefaultComboBoxModel(new String[] {
					"ring", "necklace", "bracelet", "amulet" }));

			GroupLayout contentPaneLayout = new GroupLayout(contentPane);
			contentPane.setLayout(contentPaneLayout);
			contentPaneLayout
					.setHorizontalGroup(contentPaneLayout
							.createParallelGroup()
							.addGroup(
									contentPaneLayout
											.createSequentialGroup()
											.addContainerGap()
											.addGroup(
													contentPaneLayout
															.createParallelGroup()
															.addGroup(
																	contentPaneLayout
																			.createSequentialGroup()
																			.addGroup(
																					contentPaneLayout
																							.createParallelGroup()
																							.addGroup(
																									contentPaneLayout
																											.createSequentialGroup()
																											.addComponent(
																													label2)
																											.addPreferredGap(
																													LayoutStyle.ComponentPlacement.UNRELATED)
																											.addComponent(
																													gemSelected,
																													GroupLayout.PREFERRED_SIZE,
																													GroupLayout.DEFAULT_SIZE,
																													GroupLayout.PREFERRED_SIZE))
																							.addComponent(
																									label1))
																			.addContainerGap(
																					23,
																					Short.MAX_VALUE))
															.addGroup(
																	GroupLayout.Alignment.TRAILING,
																	contentPaneLayout
																			.createSequentialGroup()
																			.addComponent(
																					startButton)
																			.addContainerGap())
															.addGroup(
																	contentPaneLayout
																			.createSequentialGroup()
																			.addComponent(
																					label3)
																			.addPreferredGap(
																					LayoutStyle.ComponentPlacement.RELATED)
																			.addComponent(
																					typeSelected,
																					0,
																					69,
																					Short.MAX_VALUE)
																			.addGap(65,
																					65,
																					65)))));
			contentPaneLayout
					.setVerticalGroup(contentPaneLayout
							.createParallelGroup()
							.addGroup(
									contentPaneLayout
											.createSequentialGroup()
											.addContainerGap()
											.addComponent(label1)
											.addGap(31, 31, 31)
											.addGroup(
													contentPaneLayout
															.createParallelGroup(
																	GroupLayout.Alignment.BASELINE)
															.addComponent(
																	label2)
															.addComponent(
																	gemSelected,
																	GroupLayout.PREFERRED_SIZE,
																	GroupLayout.DEFAULT_SIZE,
																	GroupLayout.PREFERRED_SIZE))
											.addPreferredGap(
													LayoutStyle.ComponentPlacement.RELATED,
													31, Short.MAX_VALUE)
											.addGroup(
													contentPaneLayout
															.createParallelGroup(
																	GroupLayout.Alignment.BASELINE)
															.addComponent(
																	label3)
															.addComponent(
																	typeSelected,
																	GroupLayout.PREFERRED_SIZE,
																	GroupLayout.DEFAULT_SIZE,
																	GroupLayout.PREFERRED_SIZE))
											.addGap(26, 26, 26)
											.addComponent(startButton)
											.addContainerGap()));
			pack();
			setLocationRelativeTo(getOwner());
			// JFormDesigner - End of component initialization
			// //GEN-END:initComponents
		}

		// JFormDesigner - Variables declaration - DO NOT MODIFY
		// //GEN-BEGIN:variables
		// Generated using JFormDesigner Evaluation license - Battleguard
		private JLabel label1;
		private JLabel label2;
		private JComboBox gemSelected;
		private JButton startButton;
		private JLabel label3;
		private JComboBox typeSelected;
		// JFormDesigner - End of variables declaration //GEN-END:variables
	}
}