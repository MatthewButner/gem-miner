package com.Richturtle7.bots;

import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.input.Keyboard;
import com.runemate.game.api.hybrid.local.hud.InteractablePoint;
import com.runemate.game.api.hybrid.local.hud.interfaces.Chatbox;
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceComponent;
import com.runemate.game.api.hybrid.local.hud.interfaces.Trade;
import com.runemate.game.api.hybrid.queries.ChatboxQueryBuilder;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.LoopingBot;
import com.runemate.game.api.script.framework.core.EventDispatcher;
import com.runemate.game.api.script.framework.listeners.ChatboxListener;
import com.runemate.game.api.script.framework.listeners.events.MessageEvent;

import java.util.EventListener;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;






public class dicer extends LoopingBot implements ChatboxListener {

    private final Player self = Players.getLocal();

    private Player otherPlayer = null;

    private String otherName;

    InterfaceComponent TradeInterface;

    Scanner s;

    int counter = 0;

    private boolean hasTrade = false;

    private boolean oweMoney = false;

    private boolean tookMoney = false;

    private long moneyValue = 0;


public void onLoop(){


    if (hasTrade == true){

        // instantitate Player object
        otherPlayer = Players.newQuery().names(otherName).results().first();
        boolean worked = Execution.delayUntil(() -> otherPlayer != null, 100, 3000);

        if (otherPlayer != null){
            // proceed

            if (!no_trade_screen()){ // meaning there IS a trading screen
                System.out.println("Trading confirmation screen detected");

                // check offer, and confirm

                if (Trade.atOfferScreen() && !oweMoney){
                    handle_offer();
                }
                else if (Trade.atConfirmationScreen() && !oweMoney){
                    if (handle_confirm())
                        tookMoney = true;
                }
                else if (Trade.atConfirmationScreen() && oweMoney){
                    // if you OWE him money, and the trade window is opened
                    if (give_confirm()){
                        oweMoney = false;
                        tookMoney = false;
                        hasTrade = false;
                        otherPlayer = null;
                    }

                }
                else if (Trade.atOfferScreen() && oweMoney){
                    give_offer();
                }
            }
            else if(no_trade_screen() && tookMoney){
                // roll the dice
                int roll = roll_dice();
                if (roll <= 55){
                    // you win
                    System.out.println("You win! Rolled a " + Integer.toString(roll));
                    tookMoney = false;
                    oweMoney = true;

                }
                else if (roll > 55){
                    System.out.println("You lose! Rolled a " + Integer.toString(roll));
                    // you lose
                    tookMoney = false;
                }


            }
            else if (oweMoney && no_trade_screen()){
                // then you owe him $ but you needa trade him first
                boolean successTrade = Execution.delayUntil(()->sendTrade(otherName),100,5000);

                if (!successTrade){
                    System.out.println("Could not trade player!!!!");
                    oweMoney = false;
                }

            }




        }
        else{
            System.out.print("Could not newQuery ");
            System.out.print(otherName);
        }
    }
    else{

        //System.out.println("No incoming trades");

    }






    //System.out.println("Press any key: ");

    //s.next();





    //Keyboard.type("Testing ",false);
    //Keyboard.type(Integer.toString(counter),true);



}

public boolean give_confirm(){

    Trade.accept();
    Execution.delayUntil(()->Trade.hasOtherPlayerAccepted(),100,20000);

    return true;


}

public boolean give_offer(){
    // some stuff to fix
    Execution.delayUntil(()->Trade.offer("Coins", (int)(moneyValue*2)),100,3500);

    Trade.accept();
    Execution.delayUntil(()->Trade.hasOtherPlayerAccepted(),100,20000);

    return Trade.hasOtherPlayerAccepted();
}

public int roll_dice(){
    Random random = new Random();
    int randomInt = random.nextInt(100) + 1; // 1-100
    return randomInt;
}

public boolean handle_confirm(){

    for (int i = 0; i < 3; i++){
        if (!(Trade.getTransferValue() >= 1.0)){
            Execution.delayUntil(()->Trade.getTransferValue() >= 1.0,100,5000);
            if (i==2)
                return false;
        }
        else{
            // otherwise, if we have a good enough transfer val..
                 // set the value
            moneyValue = Trade.getTransferValue();
            Execution.delayUntil(()->Trade.accept(),100,3500);
            return true;
        }
    }

    return true;


}

public boolean handle_offer(){
     // finish later
    for (int i = 0; i < 3; i++){
        if (!(Trade.getTransferValue() >= 1.0)){
            Execution.delayUntil(()->Trade.getTransferValue() >= 1.0,100,5000);
            if (i==2)
                return false;
        }
        else{
            // otherwise, if we have a good enough transfer val..
            Execution.delayUntil(()->Trade.accept(),100,3500);
            return true;
        }
    }

    return true;
}

public boolean no_trade_screen(){
    return (Trade.Screen.NONE.isOpen());
}

public boolean sendTrade(String otherName){

    for (int i = 0; i < 4; i++){
        boolean worked = Players.newQuery().names(otherName).results().first().interact("Trade with",otherName);
        if (worked)
            break;

        if (i==4){
            System.out.println("4th iteration, didnt work");
            return false;
        }
    }

    return true;
}



public void onStart(String... args){

    super.onStart(args);

    getEventDispatcher().addListener((ChatboxListener) this );

    s = new Scanner(System.in);

}


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getType() == Chatbox.Message.Type.TRADE && !hasTrade){
            System.out.println("You've got an incoming trade");
            Execution.delayUntil(()->setTradeVariables(messageEvent),100,3000);
        }
        else{
            System.out.println("Nothin yet");
        }
    }


    public boolean setTradeVariables(MessageEvent messageEvent){
        otherName = messageEvent.getSpeaker();
        Execution.delayUntil(()->otherName != null,100,3000);
        if (otherName != null){
            hasTrade = true;
            return true;
        }
        else{
            return false;
        }

    }


}
