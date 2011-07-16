import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.rsbot.script.methods.Magic;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.methods.Game.Tab;
import org.rsbot.script.util.SkillData;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSComponent;

@ScriptManifest(authors = { "battleguard" }, keywords = { "simple alcher" }, name = "simple alcher", version = 1.0, description = "simple alcher, by battleguard")
public class simpleAlcher extends Script implements PaintListener,
		MessageListener {

	private static boolean HIGH_ALCH = true;
	private Timer resetTimer = null;
	private static int ITEM_ID, NATURE_RUNE_PRICE, ITEM_PRICE, ALCH_AMOUNT,
			alchXP = 65;
	private static Rectangle intersection;
	private static boolean guiWait = true;
	private final alchGUI g = new alchGUI();

	public boolean onStart() {
		while (guiWait)
			sleep(500);

		NATURE_RUNE_PRICE = grandExchange.lookup(561).getGuidePrice();
		ITEM_PRICE = grandExchange.lookup(ITEM_ID - 1).getGuidePrice();

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
		RSComponent alchSpot;
		if (HIGH_ALCH)
			alchSpot = magic.getInterface().getComponent(
					Magic.SPELL_HIGH_LEVEL_ALCHEMY);
		else
			alchSpot = magic.getInterface().getComponent(
					Magic.SPELL_LOW_LEVEL_ALCHEMY);

		game.openTab(Tab.INVENTORY);
		sleep(1000);
		if (!alchSpot.getArea().intersects(alchItem.getArea())) {
			mouse.move(alchItem.getPoint());
			sleep(200);
			mouse.drag(alchSpot.getPoint());
		}
		sleep(1000);
		alchItem = inventory.getItem(ITEM_ID).getComponent();
		intersection = alchSpot.getArea().intersection(alchItem.getArea());

		// GET AMOUNT OF MONEY PER EACH ALCH
		final int oldMoney = inventory.getItem(995).getStackSize();
		game.openTab(Tab.MAGIC);
		resetTimer = new Timer(5000);
		Alch();
		sleep(1000);
		ALCH_AMOUNT = inventory.getItem(995).getStackSize() - oldMoney;
		ALCH_AMOUNT -= NATURE_RUNE_PRICE + ITEM_PRICE;

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
			int i = random(0, 100);
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

		if (skillData == null) {
			skillData = skills.getSkillDataInstance();
			runClock = new Timer(0);
		}

		g.setColor(Color.BLACK);
		g.fill3DRect(0, 0, 600, 30, true);
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
		JButton startButton = new JButton("Enter ID or Name and hit button");
		JTextField textField1 = new JTextField("", 10);

		public alchGUI() {
			super("Simple Alcher");

			getContentPane().setLayout(new FlowLayout());
			getContentPane().add(textField1);
			getContentPane().add(startButton);

			startButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						ITEM_ID = Integer.parseInt(textField1.getText());
					} catch (Exception exc) {
						ITEM_ID = inventory.getItem(textField1.getText())
								.getID();
					}

					guiWait = false;
					g.dispose();
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