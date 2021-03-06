/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Jens Kager, Fritz Praus
  Copyright (C) 2008-2009, Rainhard Raschbauer

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package fat;

import java.io.IOException;

public class FatTest {


	//private static final int TESTTYPE=FileStream.STREAM_TYPE_MMC;
	private static final int TESTTYPE=FileStream.STREAM_TYPE_NAND;

	
	// ####################################################################################
	// FileIn/OutputStream def Begin
	// ####################################################################################

	public static FileOutputStream FOS;
	public static FileInputStream FIS;

	// ####################################################################################
	// FileIn/OutputStream def End
	// ####################################################################################


/*********************************************************************************
*	public static void read_test()
*
*	prints the content of the FileInputStream FIS. The FileInputStream FIS has to be open.
*********************************************************************************/
	
	public static void read_test() {
	
		byte[] k = new byte[400];
		int y = 0;
		
		if (FIS == null) {
			System.out.println("FIS is null.");
			return;
		}
		
		// 1 Byte read
		System.out.print((char) FIS.read());
		System.out.print((char) FIS.read());
		System.out.print((char) FIS.read());
		
	
		
		// Read till array full or EOF
		y = FIS.read(k);
		int h = 0;
		
		while (y != -1) {
			for (int g = 0; g < y; g++) {

				System.out.print((char) k[g]);
				// FatInterface.printf(k[g],'s');
			}

			h++;
			
			// Read 300 byte from actual offset
			y = FIS.read(k, 0, 300);
		}

		for (int g = 0; g < y; g++) {

			System.out.print((char) k[g]);
			// FatInterface.printf(k[g],'s');
		}

	}
	
	
	
/*********************************************************************************
* 	public static void write_test() 
*
*	writes a char array the FileOutputStream FOS (all types of FOS.write are used). The FileOutputStream FOS has to be open.
*********************************************************************************/
	public static void write_test() throws IOException {
		int fj = 0;
		byte[] tes = new byte[518];
		
		if (FOS == null) {
			System.out.println("FOS is null");
			return;
		}

		 
		// filling the test string with chars
		for (fj = 0; fj < 512; fj++) {
			tes[fj] = (byte) 'F';
		}

		tes[512] = 0x0d;
		tes[513] = 0x0a;
		tes[514] = 0x0;


		// write till tes[x]==0
		FOS.write(tes);


/*		 512* 1Byte write      very very slow
		for (fj = 0; fj < 512; fj++) {
			
			// 512* 1Byte write	
			FOS.write((int) 'A');
		}

		FOS.write((int) 0x0d);
		FOS.write((int) 0x0a);
*/		
		
		// filling the test string with chars
		for (fj = 0; fj < 512; fj++) {
			tes[fj] = (byte) 'A';
		}

		
		// write 514 Bytes with offset 0
		FOS.write(tes,0,512);
	

		// 1 Byte write 
		FOS.write((int) 0x0d);
		// 1 Byte write 
		FOS.write((int) 0x0a);

	
		// filling the test string with chars
		for (fj = 0; fj < 512; fj++) {
			tes[fj] = (byte) 'T';
		}

		tes[512] = 0x0d;
		tes[513] = 0x0a;

		
		// write 514 Bytes with offset 0
		FOS.write(tes,0,514);
		
		
		// filling the test string with chars
		for (fj = 0; fj < 512; fj++) {
			tes[fj] = (byte) '1';
		}

		tes[512] = 0x0d;
		tes[513] = 0x0a;

		
		// write 514 Bytes with offset 0
		FOS.write(tes,0,514);
		
		// filling the test string with chars
		for (fj = 0; fj < 512; fj++) {
			tes[fj] = (byte) '6';
		}

		tes[512] = 0x0d;
		tes[513] = 0x0a;

		
		// write 514 Bytes with offset 0
		FOS.write(tes,0,514);
		
		FOS.write((int) 0x0d);
		FOS.write((int) 0x0a);
		FOS.write((int) 'E');
		FOS.write((int) 'N');
		FOS.write((int) 'D');
	}

	
/*********************************************************************************
* 	public static void run()
*
*	strats the test program  FileIn/OutputStream objacts. The test programm opens the FileOutputStream  FOS for 
*	the file "FAT-TEST.TXT" and runs then write_test(); afterwards the written file is printed with read_test();
*	
*********************************************************************************/
	public static void run() {

/*	Do not start the filename string with "/" !
* 	
*	"FAT-TEST.TXT" -> /FAT-TEST.TXT
*	"TEST/FAT-TEST.TXT"  ->  /TEST/FAT-TEST.TXT 
*/	
		

		String namea = "FATTEST0.TXT";
		System.out.print("Open File ");
		System.out.print(namea);
		System.out.println(" for write = ");
		try {
			System.out.println(FOS = new FileOutputStream(TESTTYPE, namea));
			System.out.println();
			System.out.println("Start write_test();");
			write_test();
			System.out.println("Finished write_test();");
			System.out.println();
			System.out.println();
			
			if (FOS != null) {
				System.out.println("Closing FOS");
				FOS.close();
			}
		} catch (IOException exc) {
			System.out.println("Write Exception: "+exc + " Message: "+ exc.getMessage());
			exc.printStackTrace();
		}
		

		System.out.println();
		String nameb = "FATTEST0.TXT";
		System.out.print("Open File ");
		System.out.print(nameb);
		System.out.print(" for read = ");
		
		try {
			System.out.println(FIS = new FileInputStream(TESTTYPE, nameb));
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println("Start read_test();");
			read_test();
			System.out.println();
			System.out.println();
			System.out.println("Finished read_test();");
			System.out.println();
			if (FIS != null) {
				System.out.println("Closing FIS");
				FIS.close();
			}
			
		} catch (IOException exc) {
			System.out.println("Read Exception caught: "+exc + " Message: "+ exc.getMessage());
			exc.printStackTrace();
		}
		
		//
		/*		
		System.out.println();
		System.out.println();
		System.out.println("         print Directory-listing of  dir");
		System.out.println();
		System.out.println();
		
		int[] Buf2 = new int[513];
		
		FatInterface.fat_load_reset();
		for (int a = 0; a < 240; a++) {
			System.out.println("a in main = " + a);
			Clustervar = FatInterface.fat_read_dir_ent(
					FOS.FOStream_Dir_cluster, a, Size, Dir_Attrib, Buf2);
			System.out.println("a in main = " + a);
			if (Clustervar == 0xffff) {
				System.out.println("a in main aus = " + a);
				break;
			}
			tmp = (Size[0] & 0x0000FFFF);
			System.out.println("Size = " + Size[0] + "Cluster = " + Clustervar
					+ " DirA = " + Dir_Attrib[0] + " FileName = ");
				
			FatInterface.printf(Buf2, 's');
		
		}
		System.out.println("Directory Print End");
*/

/*	
	System.out.println();
	System.out.println();
	System.out.println("              Print first 512 Bytes of FAT");
	System.out.println();
	System.out.println();
	FatInterface.printf_fat(0);
*/

	}

	
	public static void main(String[] args) {
/*
		System.out.println("FatMmc.mmc_init  start");

		if (FatMmc.mmc_init() != 0) {
			System.out.println("FatMmc.mmc_init  failed or no Card connected");
		}

		else {
			System.out.println("FatMmc.mmc_init  done");
			System.out.println();
			System.out.println();*/

			// init wird eh vom konstruktor filestream aufgerufen
			
			
			// Only uncomment ONE of the following actions!
			// ( createfs(), run(), dumpMedium() )
			// Otherwise, there will be multiple instances of MMC
			// or NAND objects, possibly leading to problems.

			// Create file system
			// createfs();

			// Write/read test
			run();

			// Dump medium data
			//dumpMedium();
			
//		}
	}
	
	public static void dumpMedium() {
		FatLowLevel fatlowlevel=null;
		
		switch (TESTTYPE) {
		case FileStream.STREAM_TYPE_MMC:
			fatlowlevel = new FatMmc();
			break;
		case FileStream.STREAM_TYPE_NAND:
			fatlowlevel = new FatNand();
			break;
		default:
			System.out.println("unknown media type");
		}
		
		fatlowlevel.Init();

		int[] sectorDump = new int[512];
		for (int sector = 0; sector < 10; sector++) {
			fatlowlevel.ReadSector(sector, sectorDump);
			System.out.println("Sector " + sector);
			FatItS.dump_sector(sectorDump);
		}
	}
	
	public static void createfs() {
		FatLowLevel fatlowlevel=null;
		int ret=-1;
		
		switch (TESTTYPE) {
		case FileStream.STREAM_TYPE_MMC:
			fatlowlevel = new FatMmc();
			break;
		case FileStream.STREAM_TYPE_NAND:
			fatlowlevel = new FatNand();
			break;
		default:
			System.out.println("unknown media type");
		}
		
		
		if (fatlowlevel!=null) {
			System.out.println("initializing fatlowlevel");
			ret=fatlowlevel.Init();
			if (ret!=0) {
				System.out.println("Error initializing fatlowlevel");
			}
			System.out.println("creating filesystem, please wait"); 
			FatItS.fat_mkfs(fatlowlevel);
		}
		System.out.println("finished creating filesytem");

	}

}
