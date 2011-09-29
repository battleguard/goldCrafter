// C:\ceg453\tone.c
// This program produces a 4 KHz square wave and prints
// a "Hello World" message to the screen every second.
// It also flashes LED1 at a frequency of 0.5 Hz (i.e.,
// on for one second and off for one second).
// Assume that the E clock frequency is 24 MHz.
#include <mc9s12c32.h>
void main() {
	unsigned int count = 0;
	TIOS |= 0x01; // use channel 0 for output compare
	TSCR1 = 0x90; // enable timer and allow fast flag clear
	TCTL2 = 0x01; // select toggle as the action for output compare
	TC0 = TCNT + 3000; // set up TC0 and clear C0F flag
	DDRA = 0xff; // set port A as output pins (for LED1)
	while (1) {
		while (!(TFLG1 & 0x01))
			; // wait until C0F flag is set
		TC0 = TCNT + 3000; // set up TC0 and clear C0F flag
		if (count++ == 8000) {
			puts("Hello World\r"); // same as printf("Hello World\n\r");
			PORTA ^= 0x01; // toggle PA0 (for LED1)
			count = 0;
		}
	}
}