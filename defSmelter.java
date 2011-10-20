#include <mc9s12c32.h>

unsigned int COUNT_VAL, DUTY_CYCLE=10;

#pragma interrupt_handler tc0_isr
void tc0_isr()
{
     COUNT_VAL = (PORTT & 0x01) ? 240 * DUTY_CYCLE : 12000;
     TC0=TCNT + (PortA & 0x01) ? 240000- COUNT_VAL : COUNT_VAL;
}

void main()
{
     TSCR1 = 0x09; //fast flag clear
     TIOS|=0x01; // use channel 0 for output compare
     DDRT &= ~0x01;//port T as input
     TC0=TCNT+12000;
     TCTL2=0x01; // select toggle as the action for output compare


     //reg isr
     *(void (**)()) 0x0fee = tc0_isr;
     INTR_ON();
     TIE = 0x01; // locally enable TC0 interrupt

    while(1);
}
