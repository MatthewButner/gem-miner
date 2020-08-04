package com.Richturtle7.bots;
import com.runemate.game.api.hybrid.GameEvents;
import com.runemate.game.api.hybrid.RuneScape;
import com.runemate.game.api.hybrid.entities.GameObject;
import com.runemate.game.api.hybrid.entities.LocatableEntity;
import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.input.Mouse;
import com.runemate.game.api.hybrid.local.Camera;
import com.runemate.game.api.hybrid.local.Skill;
import com.runemate.game.api.hybrid.local.hud.interfaces.*;
import com.runemate.game.api.hybrid.location.Area;
import com.runemate.game.api.hybrid.location.Coordinate;
import com.runemate.game.api.hybrid.location.navigation.basic.BresenhamPath;
import com.runemate.game.api.hybrid.location.navigation.basic.CoordinatePath;
import com.runemate.game.api.hybrid.location.navigation.cognizant.RegionPath;
import com.runemate.game.api.hybrid.region.GameObjects;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.hybrid.region.Region;
import com.runemate.game.api.hybrid.util.StopWatch;
import com.runemate.game.api.hybrid.util.Time;
import com.runemate.game.api.hybrid.util.Timer;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.LoopingBot;
import org.w3c.dom.css.Rect;

import java.awt.*;
import java.util.Objects;

public class gem_miner extends LoopingBot {






    //int i = 1;
    private final Player self = Players.getLocal();

    Coordinate rockA = new Coordinate(3400, 3155, 0);
    Coordinate rockB = new Coordinate(3402, 3155, 0);
    Coordinate pivot = new Coordinate(3401, 3155, 0);
    boolean atBankLocal = false;
    boolean ROCKATURN = true;
    GameObject ROCKA = null;
    GameObject ROCKB = null;
    boolean adjustedPos = false;
    boolean onPivot = false;
    boolean worked = false;
    boolean needToStore = true;
    int set = 0;
    BresenhamPath pivotPath = null;
    BresenhamPath bankPath = null;
    InterfaceComponent interFace = null;

    private final Coordinate bankCord = new Coordinate(2444, 3083, 0);

    Coordinate BankA = new Coordinate(2843,9383,0);
    Coordinate BankB = new Coordinate(2841,9384,0);
    Area.Rectangular bankSpace = new Area.Rectangular(BankA,BankB);


    Coordinate MineA = new Coordinate(2842,9392,0);
    Coordinate MineB = new Coordinate(2854,9383,0);
    Area.Rectangular mineSpace = new Area.Rectangular(MineA,MineB);

    int rocksMined = 0;


    StopWatch timer = new StopWatch();
    StopWatch waitingTimer = new StopWatch();





    public void onLoop() {


        checkRuntime();
        // (66 , 0 , 63)

        // START IN shilo village bank


        if (atBank()) {
            if (needToBank() && Bank.isOpen()) { // if u needa bank but its closed
                bank_shit_already_open();
            } else if (needToBank() && Bank.isOpen() != true) {
                // if need 2 bank but it's not open
                bank_shit_not_open();
            } else {
                travel_mine();
                System.out.println("...");
            }
        } else {
            // well, if you're not in shilo bank, then otherwise...
            if (!atMine() && needToMine()) {
                System.out.println("TEST1");
                travel_mine();

            } else if (!atMine() && needToBank()) {
                System.out.println("TEST2");
                travel_bank();
            } else if (atMine() && !needToBank()) { // aka if inv is not full & ur at the mine
                System.out.println("TEST3");
                fuckin_mine();
            }
            else{
                Execution.delayUntil(this::travel_bank);
            }


        }

    }


    @Override
    public void onStart(String... arguments) {
        super.onStart(arguments);


        GameEvents.Universal.LOGIN_HANDLER.disable(); // disables login

        if (timer != null)
            timer.start();



    }

    public boolean checkRuntime(){
        if (timer.getRuntime() >= 15000)
        {
            System.out.println("Done");
            RuneScape.logout();
            waitingTimer.start();
            // wait 10 seconds

            return true;

        }
        else if (waitingTimer.getRuntime() >= 10000)
        {
            System.out.println("Loggin back in");
            waitingTimer.reset();
            waitingTimer.stop();
            // log back in
            GameEvents.Universal.LOGIN_HANDLER.enable();
            Execution.delayUntil(()->RuneScape.isLoggedIn());
            timer.stop();
            timer.reset();
            timer.start();

            return true;

        }


        return false;
    }

    public void bank_shit_already_open(){

       boolean deposited = DepositBox.depositAllExcept("");
       if (deposited)
           DepositBox.close();

    }
    public void bank_shit_not_open(){
        /*
        GameObject bankChest = GameObjects.newQuery().names("Bank Deposit Chest").results().first();
        InterfaceComponent interFace = Interfaces.newQuery().names("The Bank of Gielinor - Deposit Box").results().first();

        if (bankChest != null){
            Execution.delayUntil(()->bankChest.interact("Deposit"),100,5000);
            System.out.println("Interface detected");
            bank_shit_already_open();
        }

         */
        if (self.getArea().overlaps(bankSpace) && Inventory.isFull()){
            boolean didIt = DepositBox.open();

            if (didIt)
                bank_shit_already_open();
        }

    }
    public void fuckin_mine(){
        System.out.println("NEED TO MINE");

        ROCKA = get_rock();

        Execution.delayUntil(()->ROCKA != null,100,5000);

            int prevMiningExp = Skill.MINING.getExperience();

            if (Inventory.getEmptySlots() > 0 && ROCKA != null && ROCKA.isValid()) {

                boolean mine = ROCKA.interact("Mine");
                System.out.println("Delay check..");
                if (ROCKA.isValid() && mine == true){
                    Execution.delayUntil(() -> !ROCKA.isValid() || Skill.MINING.getExperience() > prevMiningExp, 100, 50000);
                    System.out.println("Tried to mine");
                    ROCKA = null;
                }
                else if (mine == false){
                    ROCKA=ROCKB;
                    Execution.delayUntil(()-> Camera.turnTo(ROCKB));
                }

                System.out.println("DONE");
                System.out.println("Mined ROCK-A\n");

                return;
            }
            else{
                System.out.println("Could not query");
            }
        }


    public boolean travel_bank(){
        BresenhamPath toBank = null;

        set = 0;
        // A->B->C->D






        toBank = BresenhamPath.buildTo(bankSpace);


        boolean step = toBank.step();









            System.out.println("Traveling to ");
            Execution.delayUntil(() -> atBank() || !playerIsAnimating(), 3000, 20000);

            /*
            if (step == false)
            {
                RegionPath r = RegionPath.buildTo(bankSpace.getRandomCoordinate());
                r.step();
            }
             */
            System.out.println("DONE");
            return true;




    }
    public boolean needToMine(){
        return (Inventory.isFull() != true);
    }
    public boolean travel_mine(){
        BresenhamPath toMine = null;

        set = 0;
        // A->B->C->D






        toMine = BresenhamPath.buildTo(mineSpace);


        boolean step = toMine.step();







            System.out.println("Traveling to ");
            Execution.delayUntil(() -> atMine() || !playerIsAnimating(), 3000, 20000);



            /*
            if (step == false)
            {
                RegionPath r = RegionPath.buildTo(mineSpace.getRandomCoordinate());
                r.step();
            }
            */

            System.out.println("DONE");
            return true;





    }


    public boolean playerIsAnimating(){
        if (self.getAnimationId() != -1){
            System.out.println("Player is animating");
            return true;
        }
        else
            return false;
    }
    public boolean atMine(){

        //Object tempArea = self.getArea().getCoordinates();

        if (self.distanceTo(mineSpace) <= 1)
            return true;
        else
            return false;

    }
    public boolean atBank(){
        if (self.distanceTo(bankSpace) <= 1)
            return true;
        else
            return false;
    }
    public boolean needToBank()
    {
        return Inventory.isFull();
    }

    public GameObject get_rock(){


        LocatableEntity myArea = (LocatableEntity)self;
        GameObject temp = GameObjects.newQuery().colorSubstitutions(new Color(48, 48, 48),new Color(66 , 0 , 63)).within(mineSpace).results().nearestTo(myArea);

        ROCKB =  GameObjects.newQuery().colorSubstitutions(new Color(48, 48, 48),new Color(66 , 0 , 63)).within(mineSpace).results().nearestTo(temp);

            return temp;

    }








}
