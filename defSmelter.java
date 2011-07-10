import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.loader.script.adapter.AddMethodAdapter.Method;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Hiscores.Stats;
import org.rsbot.script.methods.MethodProvider;
import org.rsbot.script.methods.Methods;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSWeb;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.util.SkillData;
import org.rsbot.script.methods.Calculations;


@ScriptManifest(authors = "Deffiliate", name = "DefSmelter", version = 0.1, description = "Smelts your ores into perfection, flawlessly.")
public class DefSmelter extends Script implements MessageListener,
		PaintListener, MouseListener, KeyListener 
{
	public boolean somethingSelected = false;
	public int childIndex = 0;
	long mouseTimer = 0;
	public double expPerBar;
	public int tin = 438;
	public int copper = 436;
	public int mith = 447;
	public int addy = 449;
	public int rune = 451;
	public int gold = 444;
	public int silver = 442;
	public int coalPrice = 0;
	public int ironPrice = 0;
	public int mithPrice = 0;
	public int addyPrice = 0;
	public int copperPrice = 0;
	public int tinPrice = 0;
	public int runePrice = 0;
	public int silverPrice = 0;
	public int goldPrice = 0;
	public int bronzeBPrice = 0;
	public int ironBPrice = 0;
	public int steelBPrice = 0;
	public int mithBPrice = 0;
	public int addyBPrice = 0;
	public int runeBPrice = 0;
	public int silverBPrice = 0;
	public int goldBPrice = 0;
	public int bronzeBProfit = 0;
	public int ironBProfit = 0;
	public int steelBProfit = 0;
	public int mithBProfit = 0;
	public int addyBProfit = 0;
	public int runeBProfit = 0;
	public int silverBProfit = 0;
	public boolean firstTimer = true;
	public int goldBProfit = 0;
	public boolean usingTin = false;
	public boolean usingCoal = false;
	public int secondaryNumber = 0;
	public int secondaryOre = 0;
	public boolean usingCoalBag = false;
	public Rectangle bronzeSelect = new Rectangle (76,132,21,18);
	public Rectangle ironSelect = new Rectangle (76,185,21,18);
	public Rectangle steelSelect = new Rectangle (76,237,21,18);
	public Rectangle mithSelect = new Rectangle (120,132,21,18);
	public Rectangle addySelect = new Rectangle (121,185,21,18);
	public Rectangle runeSelect = new Rectangle (123,237,21,18);
	public Rectangle silverSelect = new Rectangle (170,211,21,18);
	public Rectangle goldSelect = new Rectangle (170,158,21,18);
	public Rectangle bagSelect = new Rectangle (504,188,20,18);
	public boolean start = false;
	public int display = 0;
	public int bankX1 = 3269;
	public int bankX2 = 3271;
	public int bankY1 = 3164;
	public int bankY2 = 3170;
	public int furnaceX1 = 3274;
	public int furnaceX2 = 3278;
	public int furnaceY1 = 3184;
	public int furnaceY2 = 3188;
	public int mainOreID = 440;
	public int furnaceID = 11666;
	public boolean makingBronze = false;
	public boolean makingIron = false;
	public boolean makingSteel = false;
	public boolean makingMith = false;
	public boolean makingAddy = false;
	public double lastTimer = 0;
	public double millis = 0;
	public boolean makingRune = false;
	public boolean makingSilver = false;
	public boolean makingGold = false;
	public boolean bagFull = true;
	public int coalBag = 18339;
	public int coal = 453;
	public int iron = 440;
	public double expGained = 0;
	public double expHr;
	public int startExp = 0;
	public long startTime;
	public long timeRan;
	public boolean clickIFace = false;
	public int barsMade = 0;
	public boolean atAlKharid = true;
	public boolean atEdgeville = false;
	private static int profitPer = 0;
	RSTile bankTile = new RSTile (3269, 3167);
	RSTile furnaceTile = new RSTile(3275, 3186);
	int red = 180;
	int blue = 100;
	int green = 50;
	int colorTimer = 0;
	Image startScreen,optionsScreen,paint,optionsScreen2,mouseImage;
	public int mouseSpeed = 5;
	private static SkillData skillData = null;
	private final static int idx = Skills.getIndex("smithing");
	public enum state 
	{
	bank, walkToFurnace, walkToBank, smelt, checkBag, notSure
	};			
	
	public void onRepaint(Graphics g1) 
	{
		if (start)
		{
			double percentToLevel = skills.getPercentToLevel(Skills.SMITHING, skills.getCurrentLevel(Skills.SMITHING)+1)*0.01;
			int height = (int) (121 * percentToLevel);
			g1.drawImage(paint,-10,315,null);
			colorTimer = colorTimer + 1;
			if (colorTimer == 9){
			changeColor();
			}
			g1.setColor(new Color (red,green,blue,200));
			g1.fillRect(473, 469 - height, 20, height);
			timeRan = System.currentTimeMillis() - startTime;
			if (skillData == null) {
				skillData = skills.getSkillDataInstance();
			}	
			if (makingBronze)
			{
				profitPer = bronzeBProfit ;
			}
			if (makingIron)
			{
				profitPer = ironBPrice - ironPrice ;
			}
			if (makingSteel)
			{
				profitPer = steelBProfit ;
			}
			if (makingMith)
			{
				profitPer = mithBProfit ;
			}
			if (makingAddy)
			{
				profitPer = addyBProfit ;
			}
			if (makingRune)
			{
				profitPer = runeBProfit ;
			}
			if (makingSilver)
			{
				profitPer = silverBProfit ;
			}
			if (makingGold)
			{
				profitPer = goldBProfit ;
			}
			final double xpGain = skillData.expGain(idx);
			final double xpHour = skillData.hourlyExp(idx);
			final double barsMade = xpGain / expPerBar;
			final double barHour = xpHour / expPerBar;
			final double goldMade = barsMade * (profitPer);
			final double goldHour = barHour * (profitPer);
			g1.setColor(Color.LIGHT_GRAY);
			g1.drawString(getFormattedTime(timeRan), 91, 414);
			g1.drawString(Double.toString((int)xpGain) + "(" + Double.toString((int)xpHour) + ")", 164, 428);
			g1.drawString(Double.toString((int)goldMade) + "(" + Double.toString((int)goldHour) + ")", 170, 442);
			String TTL = getFormattedTime(skillData.timeToLevel(skills.SMITHING));
			g1.drawString(TTL, 115, 456);
		}
		else if (display == 2){
			g1.drawImage(optionsScreen, 25, 66, null);
			g1.setColor(new Color (61,145,64));
			if (makingBronze)
			{
				
				g1.fillRect(bronzeSelect.x,bronzeSelect.y,bronzeSelect.width,bronzeSelect.height);
			}
			if (makingIron)
			{
				
				g1.fillRect(ironSelect.x,ironSelect.y,ironSelect.width,ironSelect.height);
			}
			if (makingSteel)
			{
				
				g1.fillRect(steelSelect.x,steelSelect.y,steelSelect.width,steelSelect.height);
			}
			if (makingMith)
			{
				
				g1.fillRect(mithSelect.x,mithSelect.y,mithSelect.width,mithSelect.height);
			}
			if (makingAddy)
			{
				
				g1.fillRect(addySelect.x,addySelect.y,addySelect.width,addySelect.height);
			}
			if (makingRune)
			{
				
				g1.fillRect(runeSelect.x,runeSelect.y,runeSelect.width,runeSelect.height);
			}
			if (makingSilver)
			{
				
				g1.fillRect(silverSelect.x,silverSelect.y,silverSelect.width,silverSelect.height);
			}
			if (makingGold)
			{
				
				g1.fillRect(goldSelect.x,goldSelect.y,goldSelect.width,goldSelect.height);
			}
			if (usingCoalBag)
			{
				
				g1.fillRect(bagSelect.x,bagSelect.y,bagSelect.width,bagSelect.height);
			}
		}
		else if(display == 1){
			g1.drawImage(optionsScreen2, 25, 66, null);
			g1.setColor(Color.GREEN);
			if (atAlKharid){
				g1.fillRect(133, 156, 25, 25);
			}
			if (atEdgeville){
				g1.fillRect(132, 186, 25, 25);
			}
			g1.setColor(Color.YELLOW);
			if (mouseSpeed == 1){
				g1.drawRect(465, 245, 50, 15);
			}
			if (mouseSpeed == 2){
				g1.drawRect(465, 229, 50, 15);
			}
			if (mouseSpeed == 3){
				g1.drawRect(465, 214, 50, 15);
			}
			if (mouseSpeed == 4){
				g1.drawRect(465, 198, 50, 15);
			}
			if (mouseSpeed == 5){
				g1.drawRect(465, 182, 50, 15);
			}
			if (mouseSpeed == 6){
				g1.drawRect(465, 165, 50, 15);
			}
			if (mouseSpeed == 7){
				g1.drawRect(465, 149, 50, 15);
			}
			if (mouseSpeed == 8){
				g1.drawRect(465, 133, 50, 15);
			}
			if (mouseSpeed == 9){
				g1.drawRect(465, 117, 50, 15);
			}
			if (mouseSpeed == 10){
				g1.drawRect(465, 101, 50, 15);
			}
		}
		else if (display == 0){
			g1.drawImage(startScreen,25,66, null);
			g1.setColor (Color.lightGray);
			g1.drawString(Integer.toString(bronzeBProfit), 80, 161);
			g1.drawString(Integer.toString(ironBProfit), 80, 214);
			g1.drawString(Integer.toString(steelBProfit), 80, 267);
			g1.drawString(Integer.toString(mithBProfit), 480, 160);
			g1.drawString(Integer.toString(addyBProfit), 480, 214);
			g1.drawString(Integer.toString(runeBProfit), 480, 267);
		}
		g1.setColor(new Color(255,255,255,100));
		g1.fillOval(mouse.getLocation().x-15, mouse.getLocation().y-15, 30, 30);
		if (mouse.isPressed()){
			mouseTimer = System.currentTimeMillis();
		}
		if (System.currentTimeMillis() - mouseTimer < 1300){
			g1.setColor(new Color (0,200,0,80));
		}
		else {
			g1.setColor(new Color (0,0,150,80));
		}
		g1.fillOval(mouse.getLocation().x-9, mouse.getLocation().y-9, 18, 18);
		g1.drawImage(mouseImage,mouse.getLocation().x-19,mouse.getLocation().y-20, null);
		g1.setColor(Color.BLACK);
		g1.drawRect(mouse.getLocation().x-1, mouse.getLocation().y-1, 2, 2);
	}

	private void changeColor() {
		red = red + random(-20,20);
		green = green + random(-20,20);
		blue = blue + random(-20,20);
		if (red > 255){
			red = red - random(25,30);
		}
		if (green > 255){
			green = green - random(25,30);
		}
		if (blue > 255){
			blue = blue - random(25,30);
		}
		if (red < 0){
			red = red + random(25,30);
		}
		if (green < 0){
			green = green + random(25,30);
		}
		if (blue < 0){
			blue = blue + random(25,30);
		}
		colorTimer = 0;
	}

	public int loop() 
	{
		int newSpeed = mouseSpeed + random(-1,1);
		if (newSpeed >10){
			newSpeed = 10;
		}
		if (newSpeed <1){
			newSpeed = 1;
		}
		mouse.setSpeed(newSpeed);
		if (start && getState()!=null)
		{
		switch (getState())
		{
			case notSure :
			sleep (random(100,150));
			break;
			case checkBag:
				if (bagFull && inventory.contains(coalBag) && !getMyPlayer().isMoving() && getMyPlayer().isIdle())
				{
					if (atFurnace()&&!clickIFace){
						sleep (random(100,200));
						break;
					}
					log ("CheckBagInspection");
					inventory.getItem(coalBag).interact("Inspect");
					sleep (1000);
				}
			break;
			case bank:
				if (!bank.isOpen())
				{
					bank.open();
					sleep(1300,1400);
				}
				else 
				{
					if (usingCoalBag)
					{
						if (makingSteel)
						{
							if (bank.getCount(iron)<18)
							{
								log ("Out of iron, stopping script.");
								game.logout(false);
								sleep (5000);
								stopScript();
							}
							else if (bank.getCount(coal)<28)
							{
								log ("Out of coal, stopping script.");
								game.logout(false);
								sleep (5000);
								stopScript();
							}
							if (!bagFull){
								if (inventory.getCountExcept(coalBag,coal)>0)
								{
									bank.depositAllExcept(coalBag);
									sleep (1000,1200);
								}
								else
								{
									if (!inventory.isFull()){
										bank.withdraw(coal, 0);
										sleep (1300,1500);
									}
								}
								if (inventory.getCount(coal)==27)
								{
									inventory.getItem(coalBag).interact("Fill");
									sleep (1500,1700);
								}
							}
							else 
							{	
								if (inventory.getCountExcept(coalBag,coal,iron)>0)
								{
									bank.depositAllExcept(coalBag,coal,iron);
									sleep (1300,1500);
								}
								if (inventory.getCount(coal)>9)
								{
									bank.deposit(coal,inventory.getCount()-9);
									sleep (1300,1500);
								}
								if (inventory.getCount(iron)>18)
								{
									bank.deposit(iron, inventory.getCount(iron)-18);
									sleep (1300,1500);
								}
								if (inventory.getCount(coal)<9)
								{
									bank.withdraw(coal, 9-inventory.getCount(coal));
									sleep (1300,1500);
								}
								if (inventory.getCount(iron)<18)
								{
									bank.withdraw(iron, 18-inventory.getCount(iron));
									sleep (1300,1500);
								}
							}
							
						}
						if (makingMith)
						{
							if (bank.getCount(mith)<10)
							{
								log ("Out of mith, stopping script.");
								game.logout(false);
								sleep (5000);
								stopScript();
							}
							else if (bank.getCount(coal)<40)
							{
								log ("Out of coal, stopping script.");
								game.logout(false);
								sleep (5000);
								stopScript();
							}
							if (!bagFull){
								if (inventory.getCountExcept(coalBag,coal)>0)
								{
									bank.depositAllExcept(coalBag);
									sleep (1000,1200);
								}
								else
								{
									if (!inventory.isFull()){
										bank.withdraw(coal, 0);
										sleep (1300,1500);
									}
								}
								if (inventory.getCount(coal)==27)
								{
									inventory.getItem(coalBag).interact("Fill");
									sleep (1500,1700);
								}
							}
							else 
							{	
								if (inventory.getCountExcept(coalBag,coal,mith)>0)
								{
									bank.depositAllExcept(coalBag,coal,iron);
									sleep (1300,1500);
								}
								if (inventory.getCount(coal)>13)
								{
									bank.deposit(coal,inventory.getCount()-13);
									sleep (1300,1500);
								}
								if (inventory.getCount(mith)>10)
								{
									bank.deposit(iron, inventory.getCount(mith)-10);
									sleep (1300,1500);
								}
								if (inventory.getCount(coal)<13)
								{
									bank.withdraw(coal, 13-inventory.getCount(coal));
									sleep (1300,1500);
								}
								if (inventory.getCount(mith)<10)
								{
									bank.withdraw(mith, 10-inventory.getCount(mith));
									sleep (1300,1500);
								}
							}
							
						}
						if (makingAddy)
						{
							if (bank.getCount(addy)<7)
							{
								log ("Out of addy, stopping script.");
								game.logout(false);
								sleep (5000);
								stopScript();
							}
							else if (bank.getCount(coal)<42)
							{
								log ("Out of coal, stopping script.");
								game.logout(false);
								sleep (5000);
								stopScript();
							}
							if (!bagFull){
								if (inventory.getCountExcept(coalBag,coal)>0)
								{
									bank.depositAllExcept(coalBag);
									sleep (1000,1200);
								}
								else
								{
									if (!inventory.isFull()){
										bank.withdraw(coal, 0);
										sleep (1300,1500);
									}
								}
								if (inventory.getCount(coal)==27)
								{
									inventory.getItem(coalBag).interact("Fill");
									sleep (1500,1700);
								}
							}
							else 
							{	
								if (inventory.getCountExcept(coalBag,coal,addy)>0)
								{
									bank.depositAllExcept(coalBag,coal,iron);
									sleep (1300,1500);
								}
								if (inventory.getCount(coal)>15)
								{
									bank.deposit(coal,inventory.getCount()-15);
									sleep (1300,1500);
								}
								if (inventory.getCount(addy)>7)
								{
									bank.deposit(iron, inventory.getCount(addy)-7);
									sleep (1300,1500);
								}
								if (inventory.getCount(coal)<15)
								{
									bank.withdraw(coal, 15-inventory.getCount(coal));
									sleep (1300,1500);
								}
								if (inventory.getCount(addy)<7)
								{
									bank.withdraw(addy, 7-inventory.getCount(addy));
									sleep (1300,1500);
								}
							}
							
						}
						if (makingRune)
						{
							if (bank.getCount(rune)<6)
							{
								log ("Out of rune, stopping script.");
								game.logout(false);
								sleep (5000);
								stopScript();
							}
							else if (bank.getCount(coal)<48)
							{
								log ("Out of coal, stopping script.");
								game.logout(false);
								sleep (5000);
								stopScript();
							}
							if (!bagFull){
								if (inventory.getCountExcept(coalBag,coal)>0)
								{
									bank.depositAllExcept(coalBag);
									sleep (1000,1200);
								}
								else
								{
									if (!inventory.isFull()){
										bank.withdraw(coal, 0);
										sleep (1300,1500);
									}
								}
								if (inventory.getCount(coal)==27)
								{
									inventory.getItem(coalBag).interact("Fill");
									sleep (1500,1700);
								}
							}
							else 
							{	
								if (inventory.getCountExcept(coalBag,coal,rune)>0)
								{
									bank.depositAllExcept(coalBag,coal,iron);
									sleep (1300,1500);
								}
								if (inventory.getCount(coal)>21)
								{
									bank.deposit(coal,inventory.getCount()-21);
									sleep (1300,1500);
								}
								if (inventory.getCount(rune)>6)
								{
									bank.deposit(iron, inventory.getCount(rune)-6);
									sleep (1300,1500);
								}
								if (inventory.getCount(coal)<21)
								{
									bank.withdraw(coal, 21-inventory.getCount(coal));
									sleep (1300,1500);
								}
								if (inventory.getCount(rune)<6)
								{
									bank.withdraw(rune, 6-inventory.getCount(rune));
									sleep (1300,1500);
								}
							}
							
						}
					}
					else
					{
						if (makingBronze){
							if (bank.getCount(tin)<14||bank.getCount(copper)<14){
								log ("Out of ores, logging out.");
								game.logout(false);
								sleep (5000);
								stopScript();
							}
							if (inventory.getCountExcept(tin,copper)>0){
								bank.depositAllExcept(tin,copper);
								sleep (random(1200,1400));
							}
							if (inventory.getCount(tin)>14){
								bank.deposit(tin,inventory.getCount(tin)-14);
								sleep (random(1200,1400));
							}
							if (inventory.getCount(tin)<14){
								bank.withdraw(tin,14-inventory.getCount(tin));
								sleep (random(1200,1400));
							}
							if (inventory.getCount(copper)>14){
								bank.deposit(copper,inventory.getCount(copper)-14);
								sleep (random(1200,1400));
							}
							if (inventory.getCount(copper)<14){
								bank.withdraw(copper,14-inventory.getCount(copper));
								sleep (random(1200,1400));
							}
						} 
						if (makingIron){
							if (bank.getCount(iron)<28){
								log ("Out of ores, logging out.");
								game.logout(false);
								sleep (5000);
								stopScript();
							}
							if (inventory.getCountExcept(iron)>0){
								bank.depositAllExcept(iron);
								sleep (random(1200,1400));
							}
							if (inventory.getCount(iron)<28){
								bank.withdraw(iron,0);
								sleep (random(1200,1400));
							}
						}
						if (makingSteel){
							if (bank.getCount(coal)<18||bank.getCount(iron)<9){
								log ("Out of ores, logging out.");
								game.logout(false);
								sleep (5000);
								stopScript();
							}
							if (inventory.getCountExcept(iron,coal)>0){
								bank.depositAllExcept(iron,coal);
								sleep (random(1200,1400));
							}
							if (inventory.getCount(coal)>18){
								bank.deposit(coal,inventory.getCount(coal)-18);
								sleep (random(1200,1400));
							}
							if (inventory.getCount(coal)<18){
								bank.withdraw(coal,18-inventory.getCount(coal));
								sleep (random(1200,1400));
							}
							if (inventory.getCount(iron)>9){
								bank.deposit(iron,inventory.getCount(iron)-9);
								sleep (random(1200,1400));
							}
							if (inventory.getCount(iron)<9){
								bank.withdraw(iron,9-inventory.getCount(iron));
								sleep (random(1200,1400));
							}
						}
						if (makingMith){
							if (bank.getCount(mith)<5||bank.getCount(coal)<20){
								log ("Out of ores, logging out.");
								game.logout(false);
								sleep (5000);
								stopScript();
							}
							if (inventory.getCountExcept(mith,coal)>0){
								bank.depositAllExcept(mith,coal);
								sleep (random(1200,1400));
							}
							if (inventory.getCount(coal)>20){
								bank.deposit(coal,inventory.getCount(coal)-20);
								sleep (random(1200,1400));
							}
							if (inventory.getCount(coal)<20){
								bank.withdraw(coal,20-inventory.getCount(coal));
								sleep (random(1200,1400));
							}
							if (inventory.getCount(mith)>5){
								bank.deposit(mith,inventory.getCount(mith)-5);
								sleep (random(1200,1400));
							}
							if (inventory.getCount(mith)<5){
								bank.withdraw(mith,5-inventory.getCount(mith));
								sleep (random(1200,1400));
							}
						}
						if (makingAddy){
							if (bank.getCount(coal)<24||bank.getCount(addy)<4){
								log ("Out of ores, logging out.");
								game.logout(false);
								sleep (5000);
								stopScript();
							}
							if (inventory.getCountExcept(addy,coal)>0){
								bank.depositAllExcept(addy,coal);
								sleep (random(1200,1400));
							}
							if (inventory.getCount(coal)>24){
								bank.deposit(coal,inventory.getCount(coal)-24);
								sleep (random(1200,1400));
							}
							if (inventory.getCount(coal)<24){
								bank.withdraw(coal,24-inventory.getCount(coal));
								sleep (random(1200,1400));
							}
							if (inventory.getCount(addy)>4){
								bank.deposit(addy,inventory.getCount(addy)-4);
								sleep (random(1200,1400));
							}
							if (inventory.getCount(addy)<4){
								bank.withdraw(addy,4-inventory.getCount(addy));
								sleep (random(1200,1400));
							}
						}
						if (makingRune){
							if (bank.getCount(coal)<24||bank.getCount(rune)<3){
								log ("Out of ores, logging out.");
								game.logout(false);
								sleep (5000);
								stopScript();
							}
							if (inventory.getCountExcept(rune,coal)>0){
								bank.depositAllExcept(rune,coal);
								sleep (random(1200,1400));
							}
							if (inventory.getCount(coal)>24){
								bank.deposit(coal,inventory.getCount(coal)-24);
								sleep (random(1200,1400));
							}
							if (inventory.getCount(coal)<18){
								bank.withdraw(coal,24-inventory.getCount(coal));
								sleep (random(1200,1400));
							}
							if (inventory.getCount(rune)>3){
								bank.deposit(rune,inventory.getCount(rune)-3);
								sleep (random(1200,1400));
							}
							if (inventory.getCount(rune)<3){
								bank.withdraw(rune,3-inventory.getCount(rune));
								sleep (random(1200,1400));
							}
						}
						if (makingSilver){
							if (bank.getCount(silver)<28){
								log ("Out of ores, logging out.");
								game.logout(false);
								sleep (5000);
								stopScript();
							}
							if (inventory.getCountExcept(silver)>0){
								bank.depositAllExcept(silver);
								sleep (random(1200,1400));
							}
							if (inventory.getCount(silver)<28){
								bank.withdraw(silver,0);
								sleep (random(1200,1400));
							}
						}
						if (makingGold){
							if (bank.getCount(gold)<28){
								log ("Out of ores, logging out.");
								game.logout(false);
								sleep (5000);
								stopScript();
							}
							if (inventory.getCountExcept(gold)>0){
								bank.depositAllExcept(gold);
								sleep (random(1200,1400));
							}
							if (inventory.getCount(gold)<28){
								bank.withdraw(gold,0);
								sleep (random(1200,1400));
							}
						}
					}
					
				}
			break;
			case walkToBank:
				RSWeb bankWeb = null;
				bankWeb = web.getWeb(bankTile);
				bankWeb.step();
				sleep (random(100,200));
				if (random(0,5)==0 && distanceTo(bankTile)>5)
				camera.turnTo(bankTile);
				break;
				
			case walkToFurnace:
				RSWeb furnaceWeb = null;
				furnaceWeb = web.getWeb(furnaceTile);
				furnaceWeb.step();
				sleep (random(100,200));
				if (random(0,5)==0 && !objects.getNearest(furnaceID).isOnScreen())
				{
				camera.turnTo(furnaceTile);
				}
			break;
				
			case smelt:
				if (getMyPlayer().getAnimation()== -1 && !interfaces.get(916).getComponent(0).isValid())
				{
					if (clickIFace)
					{
						double timer = System.currentTimeMillis();
						while (System.currentTimeMillis() - timer < 3000 && getMyPlayer().getAnimation()== -1){
							sleep (10);
						}
					}
					if (getMyPlayer().getAnimation()== -1)
					{
						if (usingCoalBag && bagFull && inventory.getCount(secondaryOre)<secondaryNumber)
						{
							log ("inspection1");
							inventory.getItem(coalBag).interact("Inspect");
							sleep (random(1900,2200));
							break;
						}
						clickIFace = false;
						if (objects.getNearest(furnaceID).isOnScreen())
						{
							objects.getNearest(furnaceID).doClick();
							sleep (random(1000,1300));
							break;
						}
					}
				}
					try{
						if (interfaces.getComponent(905, childIndex).isValid()){
							interfaces.getComponent(905, childIndex).doClick();
							clickIFace = true;
							sleep (random(1500,1600));
						}
					}
					catch (NullPointerException e){
						
					}
				break;
		
		}
		}
		return 0;
	}


	private static String getFormattedTime(final long timeMillis) 
	{
		long millis = timeMillis;
		final long days = millis / (24 * 1000 * 60 * 60);
		millis -= days * (24 * 1000 * 60 * 60);
		final long hours = millis / (1000 * 60 * 60);
		millis -= hours * 1000 * 60 * 60;
		final long minutes = millis / (1000 * 60);
		millis -= minutes * 1000 * 60;
		final long _seconds = millis / 1000;
		String dayString = String.valueOf(days);
		String hoursString = String.valueOf(hours);
		String minutesString = String.valueOf(minutes);
		String secondsString = String.valueOf(_seconds);
		if (hours < 10) {
			hoursString = 0 + hoursString;
		}
		if (minutes < 10) {
			minutesString = 0 + minutesString;
		}
		if (_seconds < 10) {
			secondsString = 0 + secondsString;
		}
		return new String ( dayString + ":" + hoursString+":"+ minutesString +":"+
		secondsString );
	}

	public void messageReceived(MessageEvent me) 
	{
		if (me.getID()==MessageEvent.MESSAGE_ACTION || me.getID() == MessageEvent.MESSAGE_SERVER)
		{
			if( me.getMessage().contains("You add the coal to your bag"))
			{
				bagFull = true;
			}
			if( me.getMessage().contains("Your coal bag is already full."))
			{
				bagFull = true;
			}
			if( me.getMessage().contains("Your coal bag is empty."))
			{
				bagFull = false;
			}
			if (me.getMessage().contains("You retrieve a bar"))
			{
				barsMade = barsMade + 1;
			}
		}
	}

	public void mouseClicked(MouseEvent e) 
	{
		Rectangle startBox = new Rectangle (214,242,180,30);
		Rectangle startBox2 = new Rectangle (268,236,180,30);
		
		if (display == 2){
			if (startBox2.contains(e.getPoint())&&somethingSelected)
			{
				start = true;
			}
			if (bronzeSelect.contains(e.getPoint()))
			{
				makingBronze = true;
				makingIron = false;
				makingSteel = false;
				makingMith = false;
				makingAddy = false;
				makingRune = false;
				makingSilver = false;
				makingGold = false;
				usingTin = true;
				usingCoal = false;
				expPerBar = 6.2;
				mainOreID = tin;
				somethingSelected = true;
				secondaryNumber = 1;
				secondaryOre = copper;
				childIndex = 14;
				
			}
			else if (ironSelect.contains(e.getPoint()))
			{
				makingBronze = false;
				makingIron = true;
				makingSteel = false;
				makingMith = false;
				makingAddy = false;
				makingRune = false;
				makingSilver = false;
				makingGold = false;
				usingTin = false;
				usingCoal = false;
				expPerBar = 12.5;
				mainOreID = iron;
				somethingSelected = true;
				secondaryNumber = 1;
				secondaryOre = iron;
				childIndex = 16;
				
			}
			else if (steelSelect.contains(e.getPoint()))
			{
				makingBronze = false;
				makingIron = false;
				makingSteel = true;
				makingMith = false;
				makingAddy = false;
				makingRune = false;
				makingSilver = false;
				makingGold = false;
				usingCoal = true;
				usingTin = false;
				secondaryNumber = 2;
				expPerBar = 17.5;
				somethingSelected = true;
				mainOreID = iron;
				secondaryOre = coal;
				childIndex = 18;
			}
			else if (mithSelect.contains(e.getPoint()))
			{
				makingBronze = false;
				makingIron = false;
				makingSteel = false;
				makingMith = true;
				makingAddy = false;
				makingRune = false;
				makingSilver = false;
				makingGold = false;
				usingTin = false;
				usingCoal = true;
				secondaryNumber = 4;
				mainOreID = mith;
				expPerBar = 30;
				somethingSelected = true;
				secondaryOre = coal;
				childIndex = 20;
				
			}
			else if (addySelect.contains(e.getPoint()))
			{
				makingBronze = false;
				makingIron = false;
				makingSteel = false;
				makingMith = false;
				makingAddy = true;
				makingRune = false;
				makingSilver = false;
				makingGold = false;
				usingTin = false;
				usingCoal = true;
				secondaryNumber = 6;
				mainOreID = addy;
				expPerBar = 37.5;
				somethingSelected = true;
				secondaryOre = coal;
				childIndex = 21;
				
			}
			else if (runeSelect.contains(e.getPoint()))
			{
				makingBronze = false;
				makingIron = false;
				makingSteel = false;
				makingMith = false;
				makingAddy = false;
				makingRune = true;
				makingSilver = false;
				makingGold = false;
				usingTin = false;
				mainOreID = rune;
				usingCoal = true;
				secondaryNumber = 8;
				expPerBar = 50;
				somethingSelected = true;
				secondaryOre = coal;
				childIndex = 22;
				
			}
			else if (silverSelect.contains(e.getPoint()))
			{
				makingBronze = false;
				makingIron = false;
				makingSteel = false;
				makingMith = false;
				makingAddy = false;
				makingRune = false;
				makingSilver = true;
				makingGold = false;
				usingTin = false;
				mainOreID = silver;
				usingCoal = false;
				expPerBar = 13.7;
				somethingSelected = true;
				secondaryNumber = 1;
				secondaryOre = silver;
				childIndex = 17;
			}
			else if (goldSelect.contains(e.getPoint()))
			{
				makingBronze = false;
				makingIron = false;
				makingSteel = false;
				makingMith = false;
				makingAddy = false;
				makingRune = false;
				makingSilver = false;
				makingGold = true;
				usingTin = false;
				usingCoal = false;
				mainOreID = gold;
				expPerBar = 22.5;
				somethingSelected = true;
				secondaryNumber = 1;
				secondaryOre = gold;
				childIndex = 19;
			}
			if (bagSelect.contains(e.getPoint()))
			{
				if (!usingCoalBag)
				{
					usingCoalBag = true;
				}
				else {
					usingCoalBag = false;
				}
				if (makingBronze){
					log ("You can't use a coal bag with bronze.");
					usingCoalBag = false;
				}
				if (makingIron){
					log ("You can't use a coal bag with bronze.");
					usingCoalBag = false;
				}
				if (makingSilver){
					log ("You can't use a coal bag with bronze.");
					usingCoalBag = false;
				}
				if (makingGold){
					log ("You can't use a coal bag with bronze.");
					usingCoalBag = false;
				}
				
			}
			if (startBox2.contains(e.getPoint()))
			{
				start = true;
				display = 3;
			}
		}
		if (display == 1){
			Rectangle continueBox = new Rectangle (210, 233, 170, 25);
			Rectangle alKharidBox = new Rectangle (134, 157, 25, 25);
			Rectangle edgevilleBox = new Rectangle (132, 186, 25, 25);
			Rectangle upSpeed = new Rectangle (427, 164, 35, 21);
			Rectangle downSpeed = new Rectangle (427, 178, 35, 21);
			if (alKharidBox.contains(e.getPoint())){
				atAlKharid = true;
				atEdgeville = false;
				bankX1 = 3269;
				bankX2 = 3271;
				bankY1 = 3164;
				bankY2 = 3170;
				furnaceX1 = 3274;
				furnaceX2 = 3278;
				furnaceY1 = 3184;
				furnaceY2 = 3188;
				furnaceID = 11666;
			}
			if (edgevilleBox.contains(e.getPoint())){
				atAlKharid = false;
				atEdgeville = true;
				bankX1 = 3269;
				bankX2 = 3271;
				bankY1 = 3164;
				bankY2 = 3170;
				furnaceX1 = 3274;
				furnaceX2 = 3278;
				furnaceY1 = 3184;
				furnaceY2 = 3188;
				furnaceID = 11666;
			}
			if (upSpeed.contains(e.getPoint()) && mouseSpeed>1){
				mouseSpeed = mouseSpeed - 1;
			}
			if (downSpeed.contains(e.getPoint()) && mouseSpeed<10){
				mouseSpeed = mouseSpeed + 1;
			}
			if (continueBox.contains(e.getPoint())){
				display = 2;
			}
		}
		if (display == 0) 
		{
			if (startBox.contains(e.getPoint()))
			{
				display = 1;
			}
		}
	}

	public void mousePressed(MouseEvent e) 
	{

	}

	public void mouseReleased(MouseEvent e) 
	{

	}

	public void mouseEntered(MouseEvent e) 
	{

	}

	public void mouseExited(MouseEvent e) 
	
	{
		
	}
	public void keyPressed(KeyEvent e) 
	{

	}

	public void keyReleased(KeyEvent e)
	{
	}

	public void keyTyped(KeyEvent e) 
	{
	}

	public boolean onStart() 
	{
		if (!game.isLoggedIn())
		{
			game.login();
			sleep (1000);
			return false;
		}
		startExp = skills.getCurrentExp(Skills.SMITHING);
		log ("Loading images from host");
		startScreen = getImage("http://i54.tinypic.com/28bz32a.png");
		optionsScreen = getImage("http://i53.tinypic.com/bgdbm0.png");
		optionsScreen2 = getImage("http://i52.tinypic.com/new2tj.png");
		mouseImage = getImage("http://i55.tinypic.com/5x03fn.png");
		paint = getImage("http://i54.tinypic.com/2w39hev.png");
		log ("Loading Grand Exchange data.");
		coalPrice = grandExchange.lookup(coal).getGuidePrice();
		while (coalPrice == 0)
		{
			sleep (100);
		}
		ironPrice =  grandExchange.lookup(iron).getGuidePrice();
		while (ironPrice == 0)
		{
			sleep (100);
		}
		mithPrice =  grandExchange.lookup(mith).getGuidePrice();
		while (mithPrice == 0)
		{
			sleep (100);
		}
		addyPrice =  grandExchange.lookup(addy).getGuidePrice();
		while (addyPrice == 0)
		{
			sleep (100);
		}
		copperPrice = grandExchange.lookup(copper).getGuidePrice();
		while (copperPrice == 0)
		{
			sleep (100);
		}
		tinPrice =  grandExchange.lookup(tin).getGuidePrice();
		while (tinPrice == 0)
		{
			sleep (100);
		}
		runePrice =  grandExchange.lookup(rune).getGuidePrice();
		while (runePrice == 0)
		{
			sleep (100);
		}
		silverPrice = grandExchange.lookup(silver).getGuidePrice();
		while (silverPrice == 0)
		{
			sleep (100);
		}
		goldPrice = grandExchange.lookup(gold).getGuidePrice();
		while (goldPrice == 0)
		{
			sleep (100);
		}
		bronzeBPrice =  grandExchange.lookup(2349).getGuidePrice();
		while (bronzeBPrice == 0)
		{
			sleep (100);
		}
		ironBPrice = grandExchange.lookup(2351).getGuidePrice();
		while (ironBPrice == 0)
		{
			sleep (100);
		}
		steelBPrice =  grandExchange.lookup(2353).getGuidePrice();
		while (steelBPrice == 0)
		{
			sleep (100);
		}
	    mithBPrice =  grandExchange.lookup(2359).getGuidePrice();
	    while (mithBPrice == 0)
		{
			sleep (100);
		}
		addyBPrice =  grandExchange.lookup(2361).getGuidePrice();
		while (addyBPrice == 0)
		{
			sleep (100);
		}
		runeBPrice =  grandExchange.lookup(2363).getGuidePrice();
		while (runeBPrice == 0)
		{
			sleep (100);
		}
		silverBPrice =  grandExchange.lookup(2355).getGuidePrice();
		while (silverBPrice == 0)
		{
			sleep (100);
		}
		goldBPrice =  grandExchange.lookup(2357).getGuidePrice();
		while (goldBPrice == 0)
		{
			sleep (100);
		}
		log ("Calculating profits per bar.");
		bronzeBProfit = bronzeBPrice - (tinPrice + copperPrice);
		ironBProfit = ironBPrice - ironPrice*2;
		steelBProfit = steelBPrice - ((coalPrice * 2) + ironPrice);
		mithBProfit = mithBPrice - ((coalPrice *4) + mithPrice);
		addyBProfit = addyBPrice - ((coalPrice * 6) + addyPrice);
		runeBProfit = runeBPrice - ((coalPrice*8) + runePrice);
		silverBProfit = silverBPrice - silverPrice;
		goldBProfit = goldBPrice - goldPrice;
		if (inventory.contains(coalBag)){
				inventory.getItem(coalBag).interact("Inspect");
		}
		startTime = System.currentTimeMillis();
		return true;
	}

	private Image getImage(String url) 
	{
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {
			return null;
		}
	}

	public void onFinish() 
	{

		env.saveScreenshot(false);

	}

	private state getState()
	{
		if (usingCoalBag){
		if (!atBank())
		{
			if (!inventory.contains(mainOreID)){
				if (bagFull){
					return state.checkBag;
				}
				return state.walkToBank;
			}
			if (!bagFull && inventory.getCount(coal)<secondaryNumber)
			{
				return state.walkToBank;
			}
		}
		if (atBank()&&!inventory.contains(mainOreID)|| (usingCoal && !bagFull || inventory.getCount(coal)<secondaryNumber)||(usingTin && inventory.getCount(tin)<1))
		{
			return state.bank;
		}
		if (!atFurnace()&&inventory.contains(mainOreID)&& (usingCoal && inventory.getCount(coal)>=secondaryNumber)||(usingTin && inventory.getCount(tin)>0))
		{
			return state.walkToFurnace;
		}
		if (atFurnace()&&inventory.contains(mainOreID)&& (usingCoal && inventory.getCount(coal)>=secondaryNumber)||(usingTin && inventory.getCount(tin)>0))
		{
			return state.smelt;
		}
		return state.checkBag;
		}
		else {
			if (!atBank() && (!inventory.contains(mainOreID)||inventory.getCount(secondaryOre)<secondaryNumber)){
				return state.walkToBank;
			}
			if (atBank()&& (!inventory.contains(mainOreID)|| inventory.getCount(secondaryOre)<secondaryNumber))
			{
				return state.bank;
			}
			if (!atFurnace()&&inventory.contains(mainOreID)&& (inventory.getCount(secondaryOre)>=secondaryNumber))
			{
				return state.walkToFurnace;
			}
			if (atFurnace()&&inventory.contains(mainOreID)&&inventory.getCount(secondaryOre)>=secondaryNumber)
			{
				return state.smelt;
			}
			return state.notSure;
		}
	}
	

	private boolean atBank() {
		RSArea bank = new RSArea (bankX1, bankY1, bankX2, bankY2);
		if (bank.contains(getMyPlayer().getLocation())){
			return true;
		}
		return false;
	}
	private boolean atFurnace() {
		RSArea bank = new RSArea (furnaceX1, furnaceY1, furnaceX2, furnaceY2);
		if (bank.contains(getMyPlayer().getLocation())){
			return true;
		}
		return false;
	}
	public int distanceTo(final RSTile t) {
		return t == null ? -1 : (int) distanceBetween(getMyPlayer().getLocation(), t);
	}
	public double distanceBetween(final RSTile curr, final RSTile dest) {
		return Math.sqrt((curr.getX() - dest.getX()) * (curr.getX() - dest.getX()) + (curr.getY() - dest.getY()) * (curr.getY() - dest.getY()));
	}
}