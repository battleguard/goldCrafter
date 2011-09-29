// C:\ceg453\tone.c
//This program produces a C note or 261.6Hz frequency 
// for a 1/8 measure (560ms) and pauses for 80ms between notes
#include <mc9s12c32.h>
void main()
{
  unsigned int count = 0;
 

  TIOS|=0x01; // use channel 0 for output compare
  TSCR1=0x90; // enable timer and allow fast flag clear
  TCTL2=0x01; // select toggle as the action for output compare
  TC0=TCNT+45872; // set up TC0 and clear C0F flag

 
  while(1)
  {   
    while(!(TFLG1&0x01)); // Wait till Flag ()TCO == TCNT)
    TC0=TCNT+45872; // Update TCO to not set off flag until 45872 clocks & clear C0F
    
    // If count equals Pause or Duration count based on what the TCTL2 is set to enter if
    if(count++ == (TCTL2 == 0x01 ? 146 : 21)) { 
         TCTL2 ^= 0x01; // Toggle the output compare to toggle or of
         count = 0; // Clear count toastart counting again for the next puase or note duration
    }
  }
}