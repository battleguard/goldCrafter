import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
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


@ScriptManifest(authors = { "battleguard" }, keywords = { "simple alcher" }, name = "simple alcher", version = 3.3, description = "simple alcher, by battleguard")
public class simplealcher extends Script implements PaintListener,
		MessageListener {
	
	private Spell alchSpell = Spell.HIGH_LEVEL_ALCHEMY;
	private Timer resetTimer = null;
	private static Rectangle intersection = new Rectangle(0 , 0);
	private static boolean guiWait = true, START_UP = true, ON_START_DONE = false;;
	private alchGUI g = null;
	ArrayList<RSItem> alchItems = new ArrayList<RSItem>();
	private static String itemname = "";
	
	@Override
	public int loop() {
		if(!game.isLoggedIn()) {
			return 5000;
		}
		
		if(!ON_START_DONE) {
			if(startScript()) {
				ON_START_DONE = true;
				runClock = new Timer(0);
			} else {
				return -1;
			}
		}
		
		if (!resetTimer.isRunning() || (alchSpell.equals(Spell.LOW_LEVEL_ALCHEMY) && skills.getCurrentLevel(Skills.MAGIC) > 54)) {			
			setupItem();
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
	
	private boolean getItems = false;
	
	public boolean startScript() {
		readyTostart();
		
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				g = new alchGUI();
				if(!(MAGIC_LVL && RUNES && FIRE_STAFF && MAGIC_LVL)) {
					log("Please startup script with correct setup");
					sleep(4000);
					g.dispose();
					return;
				}
				while(!getItems);
				  g.getItems();
			}
		});
				
		if(!(MAGIC_LVL && RUNES && FIRE_STAFF && MAGIC_LVL)) {
			return false;
		}
		
		game.openTab(Tab.INVENTORY);
		final RSItem [] invItems = inventory.getItems(true);
		for(int i = 0; i < invItems.length; i++) {
			if(invItems[i].getID() > 0 && invItems[i].getID() != 995 && !invItems[i].getName().endsWith("rune") && invItems[i].getStackSize() > 1) {
				alchItems.add(invItems[i]);				
			}
		}
		getItems = true;
		
		while (guiWait)
			sleep(500);
		if(!START_UP) {
			return false;
		}
		skillData = skills.getSkillDataInstance();
		return setupItem();		
	}
	
	private boolean LOGGED_IN = false, RUNES = false, FIRE_STAFF = false, MAGIC_LVL = false;
	
	public void readyTostart() {
		if(game.isLoggedIn()) {
			LOGGED_IN = true;
		} else {
			return;
		}		
		if(!game.openTab(Tab.INVENTORY)) {
			sleep(5000);
		}
		RUNES = inventory.contains(561);
		FIRE_STAFF = equipment.containsOneOf(1387, 1393, 1401, 11736,11738,3053,3054) ? true : inventory.contains(554);
		MAGIC_LVL = skills.getCurrentLevel(Skills.MAGIC) > 20; 		
	}
	
	
	public void onFinish(){
		if(runClock.getElapsed() > 1 * 60 * 1000) {
			log(getStats().replaceAll("  ", " "));
		}			
	}
	
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
			// FIND BEST SPOT TO ALCH ITEM
			for(int i = 0; i < 27; i++) {
				if(invComp[i].getArea().intersects(alchSpot.getArea())) {
					final Rectangle spot = invComp[i].getArea().intersection(alchSpot.getArea());
					if((spot.width * spot.height) > (intersection.width * intersection.height)) {
						bestSlot = invComp[i];
						intersection = spot;
					}							
				}
			}
			
			// COULD NOT FIND AN INTERSECTION BETWEEN ALCH SPOT AND ALCH ITEM
			if(bestSlot == null) {
				log("Problem moving item to alch spot");
				return false;
			}
			
			// MOVE ALCH ITEM TO ALCH SPOT
			if(!alchItem.equals(bestSlot)) {
				alchItem.interact("Use " + alchItem.getText());
				sleep(200);
				mouse.move(alchItem.getPoint());
				sleep(200);
				mouse.drag(bestSlot.getPoint());
			}
			
			game.openTab(Tab.MAGIC);
			resetTimer = new Timer(5000);
			return true;
		} catch (Exception e) {
			log("Exception: " + e.toString());
			return false;
		}		
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
		final Point click = new Point((int) intersection.getCenterX(), (int) intersection.getCenterY());
		final RSComponent alchSpot = magic.getInterface().getComponent(alchSpell.getComponent());
		if(alchSpot != null && alchSpot.getArea().contains(click)) {
			sleep(100, 200);
			mouse.click(click, true);
			sleep(100, 200);
			mouse.click(true);
			sleep(100, 200);
			resetTimer.reset();
		}	
	}

	private static SkillData skillData = null;
	private static Timer runClock;
	private final NumberFormat k = new DecimalFormat("###,###,###");
	private static double xp, xpH, alchs, alchsH;
	
	
	@Override
	public void onRepaint(Graphics g) {
		if(ON_START_DONE) {
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
	}
	
	public String getStats() {
		return 	"clk:  " + runClock.toElapsedString() + "  |  xp: "+ k.format(xp) + "  |  xp/h:  "
				+ k.format(xpH) + "  |  a:  " + k.format(alchs) + "  |  a/h:  " + k.format(alchsH)
				+ "  |  lvl: " + skills.getCurrentLevel(Skills.MAGIC) + "  |  item:  " + itemname;
	}
	
	
    final ImageIcon checkMark = getImage("http://i.imgur.com/Q6D61.jpg");
    final ImageIcon xMark = getImage("http://i.imgur.com/BmmtJ.jpg");
    private ImageIcon getImage(final String url) {
        try {
            return new ImageIcon(ImageIO.read(new URL(url)));
        } catch(IOException e) {
            return null;
        }
    }
    
    public ImageIcon getImage(final boolean image) {
    	return image ? checkMark : xMark;
    }
	
	private ImageIcon getItemImage(final int ITEM_ID) {
	    try {
	        return new ImageIcon(ImageIO.read(new URL("http://services.runescape.com/m=itemdb_rs/3493_obj_sprite.gif?id=" + ITEM_ID)));
	    } catch(IOException e) {
	    	 try {
				return new ImageIcon(ImageIO.read(new URL("http://services.runescape.com/m=itemdb_rs/3493_obj_sprite.gif?id=" + (ITEM_ID - 1))));
	    	  } catch(IOException f) {
	    		  log("Could not find item Image with ID: " + ITEM_ID);
	    		 return xMark;
			}
	    }
	}
	

    
	
public class alchGUI extends JFrame {
	
	private static final long serialVersionUID = 1L;
	JButton startButton = new JButton("Start");
	JCheckBox [] itemCheckbox;
	ImageIcon [] itemImages;
	JPanel checkPanelCenter = new JPanel();	
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
		
		JPanel checkPanelLeft = new JPanel();
		checkPanelLeft.setLayout(new GridLayout(5, 1));
		checkPanelLeft.setPreferredSize(new Dimension(50, 35 * 5));
		checkPanelLeft.add(new JLabel(getImage(LOGGED_IN)));
		checkPanelLeft.add(new JLabel(getImage(RUNES)));
		checkPanelLeft.add(new JLabel(getImage(FIRE_STAFF)));
		checkPanelLeft.add(new JLabel(getImage(MAGIC_LVL)));
		checkPanelLeft.add(new JLabel(getImage(MAGIC_LVL && RUNES && FIRE_STAFF && MAGIC_LVL)));
				
		JPanel checkPanelRight = new JPanel();
		checkPanelRight.setLayout(new GridLayout(5, 1));
		checkPanelRight.setPreferredSize(new Dimension(160, 35 * 5));
		checkPanelRight.add(new JLabel("  Game Logged in"));
		checkPanelRight.add(new JLabel("  Nature runes in inv"));
		checkPanelRight.add(new JLabel("  Fire staff equipped"));
		checkPanelRight.add(new JLabel("  Magic lvl above 20"));
		checkPanelRight.add(new JLabel("  Ready to go"));
						
		checkPanelCenter.setLayout(new BoxLayout(checkPanelCenter, BoxLayout.X_AXIS));
		checkPanelCenter.add(checkPanelLeft);
		checkPanelCenter.add(checkPanelRight);
		
		getContentPane().add(checkPanelCenter);
		setLocationRelativeTo(getOwner());
		pack();	        
		setVisible(true);
	}
	
		void getItems() {
			itemCheckbox = new JCheckBox[alchItems.size()];
			itemImages = new ImageIcon[alchItems.size()];

			JPanel bottumPane = new JPanel();
			bottumPane.setLayout(new GridLayout(2, 1));
			bottumPane.setPreferredSize(new Dimension(210, height * 2));
			bottumPane.add(instructionLabel);
			bottumPane.add(startButton);

			getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
			getContentPane().add(bottumPane);
			pack();
			setVisible(true);

			for (int i = 0; i < alchItems.size(); i++) {
				itemCheckbox[i] = new JCheckBox(alchItems.get(i).getName());
				itemImages[i] = getItemImage(alchItems.get(i).getID());
			}

			for (int i = 0; i < alchItems.size(); i++) {
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
			getContentPane().remove(checkPanelCenter);
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
				"You do not have enough nature runes to cast this spell.")
				|| e.getMessage()
						.equals("You do not have enough fire runes to cast this spell.")) && e.getSender().equals("")) {
			log(e.getMessage());
			stopScript();
		}

	}
}