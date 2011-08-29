import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Magic.Book;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.methods.Game.Tab;
import org.rsbot.script.methods.Magic.Spell;
import org.rsbot.script.util.SkillData;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSItem;



@ScriptManifest(authors = { "battleguard" }, keywords = { "simple alcher" }, name = "simple alcher", version = 3.2, description = "simple alcher, by battleguard")
public class simpleAlcher extends Script implements PaintListener,
		MessageListener {
	
	private Spell alchSpell = Spell.HIGH_LEVEL_ALCHEMY;
	private Timer resetTimer = null;
	private static Rectangle intersection = new Rectangle(0 , 0);
	private static boolean guiWait = true, START_UP = true;
	private alchGUI g = null;
	ArrayList<RSItem> alchItems = new ArrayList<RSItem>();
	private static String itemname = "";
	
	public boolean onStart() {
		game.openTab(Tab.INVENTORY);
		final RSItem [] invItems = inventory.getItems(true);
		for(int i = 0; i < invItems.length; i++) {
			if(invItems[i].getID() > 0 && invItems[i].getID() != 995 && !invItems[i].getName().endsWith("rune") && invItems[i].getStackSize() > 1) {
				alchItems.add(invItems[i]);				
			}
		}		
		g = new alchGUI();
		
		while (guiWait)
			sleep(500);
		if(!START_UP) {
			return false;
		}
		skillData = skills.getSkillDataInstance();
		return setupItem();
	}
	
	public void onFinish(){
		if(runClock.getElapsed() > 1 * 60 * 1000) {
			log(getStats().replaceAll("  ", " "));
		}			
	}
	
	private static int counter = 0;
	public boolean setupItem() {
		try {
			game.openTab(Tab.MAGIC);
			if(alchItems.isEmpty() || !magic.getCurrentSpellBook().equals(Book.MODERN)) {
				log("out of items or wrong book");
				return false;
			}			
			final RSComponent alchSpells = interfaces.getComponent(192, 11);
			final RSComponent [] scrollComp = interfaces.getComponent(192, 94).getComponents();
			if(alchSpells.getTextureID() == 1701) {
				mouse.click(alchSpells.getPoint(), true);
				sleep(1000);
			}
			if(scrollComp.length > 0) {
				if(scrollComp[0].getArea().y != scrollComp[1].getArea().y) {
					mouse.move(scrollComp[1].getPoint());
					sleep(200);				
					mouse.click(scrollComp[1].getPoint(), true);
					sleep(200);
					mouse.drag(scrollComp[2].getPoint());				
				}
			}		
			intersection = new Rectangle(0 , 0);		
			itemname = alchItems.get(0).getName();
			
			if(skills.getCurrentLevel(Skills.MAGIC) < 21) {
				log("Magic lvl is too low");
				return false;
			}
			alchSpell = (skills.getCurrentLevel(Skills.MAGIC) < 55) ? Spell.LOW_LEVEL_ALCHEMY : Spell.HIGH_LEVEL_ALCHEMY;	
			if(!magic.hoverSpell(alchSpell)) {
				log("Please make sure " + alchSpell.getName() + " is visible");
				return false;
			}
			final RSComponent alchSpot = magic.getInterface().getComponent(alchSpell.getComponent());
			final RSComponent alchItem = inventory.getItem(alchItems.get(0).getID()).getComponent(); // GET ALCHITEM COMP
			final RSComponent [] invComp = interfaces.getComponent(679, 0).getComponents();
			if(!inventory.containsOneOf(alchItems.get(0).getID())) { // OUT OF ITEM
				alchItems.remove(0);
				return setupItem();
			}

			RSComponent bestSlot = null;		
			for(int i = 0; i < 27; i++) {
				if(invComp[i].getArea().intersects(alchSpot.getArea())) {
					final Rectangle spot = invComp[i].getArea().intersection(alchSpot.getArea());
					if((spot.width * spot.height) > (intersection.width * intersection.height)) {
						bestSlot = invComp[i];
						intersection = spot;
					}							
				}
			}
			
			if(bestSlot == null) {
				log("Problem moving item to alch spot");
				return false;
			}
			
			if(!alchItem.equals(bestSlot)) {
				alchItem.interact("Use " + alchItem.getText());
				sleep(200);
				mouse.move(alchItem.getPoint());
				sleep(200);
				mouse.drag(bestSlot.getPoint());
			}
			resetTimer = null;
			counter = 0;
			return true;
		} catch (Exception e) {
			log("Exception: " + e.toString());
			//return counter++ == 5 ? false : setupItem();
			return false;
		}		
	}
	

	@Override
	public int loop() {
		if(runClock == null) {
			runClock = new Timer(0);
		}
		
		if(alchSpell.equals(Spell.LOW_LEVEL_ALCHEMY) && skills.getCurrentLevel(Skills.MAGIC) > 54) {
			setupItem();
		}
		
		if (resetTimer == null || !resetTimer.isRunning()) {
			resetTimer = new Timer(5000);
			game.openTab(Tab.MAGIC);
		}
		
		if (game.getTab() == Tab.MAGIC) {
			Alch();
			antiban.run();
		} else {
			if (game.isLoggedIn() && inventory.getCount(alchItems.get(0).getID()) == 0 && game.getTab() == Tab.INVENTORY) {
				alchItems.remove(0);
				if (!setupItem()) {
					stopScript();
				}
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
	private static double xp, xpH, alchs, alchsH;
	
	
	@Override
	public void onRepaint(Graphics g) {
		g.setColor(Color.BLACK);		
		g.fill3DRect(0, 0, 600, 50, true);
		g.setColor(Color.RED);
		g.fill3DRect(50, 30, 450, 10, true);
		g.setColor(Color.GREEN);
		g.fill3DRect(50, 30, (int) (skillData.percentToLevel(Skills.MAGIC) * 4.5), 10, true);
		g.setColor(Color.WHITE);		
		xp = skillData.expGain(Skills.MAGIC);
		xpH = skillData.hourlyExp(Skills.MAGIC);
		alchs = xp / alchSpell.getExperience();
		alchsH = xpH / alchSpell.getExperience();
		g.drawString(getStats(), 20, 20);

	}
	
	public String getStats() {
		return 	"clk:  " + runClock.toElapsedString() + "  |  xp: "+ k.format(xp) + "  |  xp/h:  "
				+ k.format(xpH) + "  |  a:  " + k.format(alchs) + "  |  a/h:  " + k.format(alchsH)
				+ "  |  lvl: " + skills.getCurrentLevel(Skills.MAGIC) + "  |  item:  " + itemname;
	}
	
private Image getItemImage(final int ITEM_ID) {
    try {
        return ImageIO.read(new URL("http://services.runescape.com/m=itemdb_rs/3464_obj_sprite.gif?id=" + ITEM_ID));
    } catch(IOException e) {
    	 try {
			return ImageIO.read(new URL("http://services.runescape.com/m=itemdb_rs/3464_obj_sprite.gif?id=" + (ITEM_ID - 1)));
    	  } catch(IOException f) {
    		  return null;
		}
    }
}
public class alchGUI extends JFrame {
	
	private static final long serialVersionUID = 1L;
	JButton startButton = new JButton("Start");
	JCheckBox itemCheckbox[] = new JCheckBox[alchItems.size()];
	ImageIcon itemImages[] = new ImageIcon[alchItems.size()];
	
	JLabel instructionLabel = new JLabel("   loading Items...");
	final int height = 32;
		
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
		
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for(int i = itemCheckbox.length - 1; i >= 0; i--) {
					if(!itemCheckbox[i].isSelected()) {
						alchItems.remove(i);
					}
				}
				for(int i = 0; i < alchItems.size(); i++) {
					log("Item to be alched: " + alchItems.get(i).getName());
				}
				guiWait = false;
				g.dispose();
			}
		});
		
		JPanel bottumPane = new JPanel();
		bottumPane.setLayout(new GridLayout(2, 1));
		bottumPane.setPreferredSize(new Dimension(210, height * 2));
		bottumPane.add(instructionLabel);
		bottumPane.add(startButton);
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(bottumPane);
		setLocationRelativeTo(getOwner());	        
		pack();	        
		setVisible(true);
		
		for(int i = 0; i < alchItems.size(); i++) {
			itemCheckbox[i] = new JCheckBox(alchItems.get(i).getName());
			itemImages[i] = new ImageIcon(getItemImage(alchItems.get(i).getID()));
		}
		
		for(int i = 0; i < alchItems.size(); i++) {
			itemCheckbox[i] = new JCheckBox(alchItems.get(i).getName());
		}
		
		JPanel leftPane = new JPanel();
		leftPane.setLayout(new GridLayout(itemCheckbox.length, 1));
		leftPane.setPreferredSize(new Dimension(60, height * (itemCheckbox.length)));
		for (ImageIcon curImage : itemImages) {
			leftPane.add(new JLabel(curImage));
		}
		
		JPanel rightPane = new JPanel();
		rightPane.setLayout(new GridLayout(itemCheckbox.length, 1));
		rightPane.setPreferredSize(new Dimension(150, height * (itemCheckbox.length)));
		
		for (JCheckBox curCheckBox : itemCheckbox) {
			rightPane.add(curCheckBox);
		}
		
		JPanel centerPane = new JPanel();
		centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.X_AXIS));
		centerPane.add(leftPane);
		centerPane.add(rightPane);
		
		instructionLabel.setText("   Please check the items you wish to alch");
		getContentPane().remove(bottumPane);
		getContentPane().add(centerPane);
		getContentPane().add(bottumPane);
		pack();	        
		setVisible(true);							
	}
}

	@Override
	public void messageReceived(MessageEvent e) {		
		if ((e.getMessage().equals(
				"You do not have enough Nature Runes to cast this spell.")
				|| e.getMessage()
						.equals("You do not have enough Fire Runes to cast this spell.")) && e.getSender().equals("")) {
			log(e.getMessage());
			stopScript();
		}

	}
}