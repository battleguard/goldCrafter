// C:\ceg453\ADC_scan.c
// This program reads 80 ADC samples from channel 7 and
// prints out their values.
// The conversion is 8-bit instead of 10-bit.
// WARNING: Use an oscilloscope to make sure the analog signal is 
//          within the 0 to 5 volt range before connecting it to EVB.
//          For best observation results, use a 2 KHz sinusoidal wave,
//          0 to 5 volt peak to peak. 
#include <mc9s12c32.h>

void iprint(int n)
{ 
   int a;
   if( n > 9 ) { 
      a = n / 10;
      n -= 10 * a;
      iprint(a);
   }
   putchar('0'+n);
}


void ATD_init(void)
{
  int i;
  
  ATDCTL2 = 0xe0; // enable ADT and use fast flag clear
  for(i=0; i<50; i++) asm("nop"); // a delay of at least 5 micro-sec
  ATDCTL3 = 0x0A; // conversion length : 1 sample
  ATDCTL4 = 0xA5; // 8-bit resolution;
                  // 14 (=2+4+8) ATD clock cycles/sample for conversion 
                  // 2 (=24/12) MHz ATD clock
				  // (Modify this register to get faster conversion.)
}

int getHertz() {
  unsigned int samples = 0, T1 = 0, T2 = 0;

  ATD_init();
  ATDCTL5 = 0x27; // start an A/D conversion, scan, single channel (7)
  
  while(ATDDR0H > 50) { // wait till at the bottum of a wave
   	 while(!(ATDSTAT0 & 0x80));
  }
  while(T2 == 0) {
    while(!(ATDSTAT0 & 0x80)); // wait until conversion is complete
	samples++;
	if(ATDDR0H > 125  && ATDDR0H < 129) { // (126-128)
	   if(T1 == 0) T1 = samples; else T2 = samples; 
	   while(ATDDR0H > 50) { // wait till at the bottum of wave
	       while(!(ATDSTAT0 & 0x80));
		   samples++;
	   }
	}
  }
  return((47619 / (T2 - T1)) * 3);
}

void main()
{
  iprint(getHertz());
  asm("JMP $FC00"); // return to Monitor
}