import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.GrandExchange.GEItem;
import org.rsbot.script.methods.Magic;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.methods.Game.Tab;
import org.rsbot.script.util.SkillData;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSItem;

@ScriptManifest(authors = { "battleguard" }, keywords = { "simple alcher" }, name = "simple alcher", version = 2.0, description = "simple alcher, by battleguard")
public class simpleAlcher extends Script implements PaintListener,
		MessageListener {

	private static boolean HIGH_ALCH = true;
	private Timer resetTimer = null;
	private static int ITEM_ID, NATURE_RUNE_PRICE, ITEM_PRICE, ALCH_AMOUNT, alchXP = 65;
	private static Rectangle intersection = new Rectangle(0 , 0);
	private static boolean guiWait = true, START_UP = true;
	private final alchGUI g = new alchGUI();

	public boolean onStart() {
		while (guiWait)
			sleep(500);
		if(!START_UP) {
			return false;
		}

		NATURE_RUNE_PRICE = grandExchange.lookup(561).getGuidePrice();		
		GEItem itemInfo = grandExchange.lookup(ITEM_ID);
		if(itemInfo == null) {
			itemInfo = grandExchange.lookup(ITEM_ID - 1);
		}		
		ITEM_PRICE = itemInfo.getGuidePrice();

		if (skills.getCurrentLevel(Skills.MAGIC) < 55) {
			alchXP = 31;
			HIGH_ALCH = false;
		}

		if (skills.getCurrentLevel(Skills.MAGIC) < 21) {
			log("Magic level too low");
			return false;
		}

		// move the alch item and alch spot to the same spot
		RSComponent alchItem = inventory.getItem(ITEM_ID).getComponent();
		game.openTab(Tab.MAGIC);
		sleep(1000);
		final RSComponent alchSpot = HIGH_ALCH ? magic.getInterface().getComponent(Magic.SPELL_HIGH_LEVEL_ALCHEMY) : 
					magic.getInterface().getComponent(Magic.SPELL_LOW_LEVEL_ALCHEMY);
		
		game.openTab(Tab.INVENTORY);
		sleep(1000);
		RSComponent [] invComp = interfaces.getComponent(679, 0).getComponents();
		RSComponent bestSlot = null;
		
		for(int i = 0; i < 27; i++) {
			if(invComp[i].getArea().intersects(alchSpot.getArea())) {
				final Rectangle spot = invComp[i].getArea().intersection(alchSpot.getArea());
				if((spot.width * spot.height) > (intersection.width * intersection.height)) {
					bestSlot = invComp[i];
					intersection = spot;
					log("Inventory slot " + i + " is new best slot");
				}							
			}
		}
		
		if(bestSlot == null) {
			log("Problem moving item to alch spot");
			return false;
		}
		alchItem.interact("Use " + alchItem.getText());
		sleep(200);
		mouse.drag(bestSlot.getPoint());
		

		// GET AMOUNT OF MONEY PER EACH ALCH
		final int oldMoney = inventory.getItem(995).getStackSize();
		game.openTab(Tab.MAGIC);
		resetTimer = new Timer(5000);
		Alch();
		sleep(1000);
		ALCH_AMOUNT = inventory.getItem(995).getStackSize() - oldMoney;
		ALCH_AMOUNT -= NATURE_RUNE_PRICE + ITEM_PRICE;
		
		skillData = skills.getSkillDataInstance();
		runClock = new Timer(0);
		return true;
	}

	@Override
	public int loop() {
		if (!resetTimer.isRunning()) {
			resetTimer.reset();
			game.openTab(Tab.MAGIC);

		}
		if (game.getTab() == Tab.MAGIC) {
			Alch();
			antiban.run();
		} else {
			if (inventory.getCount(ITEM_ID) == 0) {
				log("out of items");
				stopScript();
			}
		}
		return 100;
	}

	public Thread antiban = new Antiban();

	public class Antiban extends Thread {

		@Override
		public void run() {
			final int i = random(0, 100);
			if (i == 50) {
				camera.setAngle(random(0, 360));
				camera.setPitch(random(20, 100));
			}
		}
	}

	void Alch() {
		sleep(100, 200);
		mouse.click(new Point((int) intersection.getCenterX(),
				(int) intersection.getCenterY()), true);
		sleep(100, 200);
		mouse.click(true);
		sleep(100, 200);
		resetTimer.reset();
	}

	private static SkillData skillData = null;
	private static Timer runClock;
	private final NumberFormat k = new DecimalFormat("###,###,###");

	@Override
	public void onRepaint(Graphics g) {
		g.setColor(Color.BLACK);
		g.fill3DRect(0, 0, 600, 30, true);
		g.setColor(Color.RED);
		g.fillRect(intersection.x, intersection.y, intersection.width, intersection.height);
		g.setColor(Color.WHITE);

		final double xp = skillData.expGain(Skills.MAGIC);
		final double xpH = skillData.hourlyExp(Skills.MAGIC);
		final double alchs = xp / alchXP;
		final double alchsH = xpH / alchXP;
		final double gp = alchs * ALCH_AMOUNT;
		final double gpH = alchsH * ALCH_AMOUNT;

		g.drawString(
				"clk:  " + runClock.toElapsedString() + "  |  xp: "
						+ k.format(xp) + "  |  xp/h:  " + k.format(xpH)
						+ "  |  a:  " + k.format(alchs) + "  |  a/h:  "
						+ k.format(alchsH) + "  |  gp:  " + k.format(gp)
						+ "  |  gp/h:  " + k.format(gpH), 20, 20);

	}

	public class alchGUI extends JFrame {
		private static final long serialVersionUID = 1L;
		final JButton startButton = new JButton("Enter ID or Name and hit button");
		final JTextField textField1 = new JTextField("", 10);
	
		public alchGUI() {
			super("Simple Alcher");
			
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					START_UP = false;
					guiWait = false;
					log("Cancelling Startup of script");
					g.dispose();
				}
			});
			
			getContentPane().setLayout(new FlowLayout());
			getContentPane().add(textField1);
			getContentPane().add(startButton);
			startButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					RSItem item = null; 
					
					try {
						ITEM_ID = Integer.parseInt(textField1.getText());
						item = inventory.getItem(ITEM_ID);
					} catch (Exception exc) {
						item = inventory.getItem(textField1.getText());	
					}
					
					if(item != null) {							
						ITEM_ID = item.getID();
						if(ITEM_ID > 0) {
							guiWait = false;
							g.dispose();
							return;
						}
					}			
					log("You have entered the wrong item name or item id");
				}
			});
			setLocationRelativeTo(getOwner());
			setSize(450, 70);
			setVisible(true);			
		}
	}

	@Override
	public void messageReceived(MessageEvent e) {
		if (e.getMessage().equals(
				"You do not have enough Nature Runes to cast this spell.")
				|| e.getMessage()
						.equals("You do not have enough Fire Runes to cast this spell.")) {
			log(e.getMessage());
			stopScript();
		}

	}
}