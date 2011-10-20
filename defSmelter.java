// C:\CEG453\tone_interrupt.c
// This program produces a 4 KHz square wave and prints 
//   a "Hello World" message to the screen every second.
// It also flashes LED1 at a frequency of 0.5 Hz (i.e.,
//   on for one second and off for one second).
// Assume that the E clock frequency is 24 MHz.
#include <mc9s12c32.h>

//unsigned int count=0;
//GLOBALS
unsigned int COUNT_VAL;
unsigned int DUTY_CYCLE=99;
  
#pragma interrupt_handler tc0_isr
void tc0_isr()
{
    if(!(PTP & 0x20)) // SW2 (i.e., PP5) is pressed down & 0x01)//if switch PortT  high change duty cyle
    {
         COUNT_VAL= 240 * DUTY_CYCLE;
    }
    else//default to 50%
    {
        COUNT_VAL= 12000;
    }
	
    //check in high or low
    if(!(PTT & 0x01))
    {
          TC0= TCNT + (24000- COUNT_VAL);
    }
    else 
	{
         TC0=TCNT+ COUNT_VAL;
    }
    //COUNT_VAL = 240 * (!(PTP & 0x20) ?  DUTY_CYCLE : 50;
    //TCO = TCNT + !(PTT & 0x01) ? 24000- COUNT_VAL : COUNT_VAL;

}

void main()
{
  TIOS|=0x01; // use channel 0 for output compare
  TSCR1=0x90; // enable timer and allow fast flag clear
  TCTL2=0x01; // select toggle as the action for output compare
  TC0=TCNT+12000; // set up TC0 and clear C0F flag
  DDRA = 0xff; // set port A as output pins (for LED1)

  *(void (**)()) 0x0fee = tc0_isr;
  // *(int *) 0x0fee = (int) tc0_isr;
  INTR_ON();
  TIE = 0x01; // locally enable TC0 interrupt
  
  while(1)
  {	
	puts("fmnbh\r");
  }
}